package org.hl7.davinci.endpoint.components;

import org.apache.commons.lang.StringUtils;
import org.cdshooks.CdsRequest;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.endpoint.cdshooks.services.crd.r4.FhirRequestProcessor;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.davinci.r4.crdhook.appointmentbook.AppointmentBookRequest;
import org.hl7.davinci.r4.crdhook.orderdispatch.OrderDispatchRequest;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import ca.uhn.fhir.context.FhirContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A Query Batch Request can be used to populate fields in a CDS Request that a Prefetch may have missed.
 */
public class QueryBatchRequest {

  private static final Logger logger = LoggerFactory.getLogger(QueryBatchRequest.class);
  private static final String PRACTIONER_ROLE = "PractitionerRole";

  private final FhirComponentsT fhirComponents;

  public QueryBatchRequest(FhirComponentsT fhirComponents) {
    this.fhirComponents = fhirComponents;
  }

  /**
   * Backfills the missing required values of the response that prefetch may have missed.
   * This implementation pulls the IDs of the required references from the request object's draft
   * orders, checks which of those values are missing from the current CRD response, builds the
   * Query Batch JSON request using
   * http://build.fhir.org/ig/HL7/davinci-crd/hooks.html#fhir-resource-access,
   * then populates the CRD response with the response from the Query Batch.
   */
  public void performQueryBatchRequest(CdsRequest<?, ?> cdsRequest, CrdPrefetch crdPrefetch) {
    logger.info("***** ***** Performing Query Batch Request.");
    // Get the IDs of references in the request's draft orders.
    Bundle draftOrdersBundle = cdsRequest.getContext().getDraftOrders();

    // Perform the query batch request for each of the draft orders.
    for(BundleEntryComponent bec : draftOrdersBundle.getEntry()) {
      this.performBundleQueryBatchRequest(bec.getResource(), crdPrefetch, cdsRequest);
    }
  }

  public void performDispatchQueryBatchRequest(OrderDispatchRequest request, CrdPrefetch prefetch) {
    logger.info("Performing Query Batch Request for OrderDispatch.");

    // Validate Task and associated context
    Task task = request.getContext().getTask();
    if (task == null || task.getFocus() == null || StringUtils.isBlank(task.getFocus().getReference())) {
      logger.warn("Task or its focus (ServiceRequest reference) is missing. Skipping query batch request.");
      return;
    }

    // Extract references from Task
    String serviceRequestRef = task.getFocus().getReference();
    String patientId = request.getContext().getPatientId();
    String performer = request.getContext().getPerformer();

    // Collect references to query
    List<String> resourceReferences = new ArrayList<>();
    resourceReferences.add(serviceRequestRef);

    if (StringUtils.isNotBlank(patientId)) {
      resourceReferences.add("Patient/" + patientId);
    }

    if (StringUtils.isNotBlank(performer)) {
      resourceReferences.add(performer);
    }

    // Remove duplicates and already-fetched resources
    resourceReferences = resourceReferences.stream()
            .distinct()
            .filter(ref -> !prefetch.containsRequestResourceId(ref))
            .collect(Collectors.toList());

    if (resourceReferences.isEmpty()) {
      logger.info("All necessary references are already pre-fetched. No Query Batch Request needed.");
      return;
    }

    // Build and execute the Query Batch Request
    try {
      Bundle queryBatchRequestBundle = buildQueryBatchRequestBundle(resourceReferences);
      String queryBatchRequestBody = FhirContext.forR4().newJsonParser().encodeResourceToString(queryBatchRequestBundle);

      logger.info("Executing Query Batch Request: {}", queryBatchRequestBody);

      Bundle queryResponseBundle = (Bundle) FhirRequestProcessor.executeFhirQueryBody(
              queryBatchRequestBody, request, this.fhirComponents, HttpMethod.POST
      );

      if (queryResponseBundle == null || queryResponseBundle.getEntry().isEmpty()) {
        logger.warn("Query Batch Request returned no data.");
        return;
      }

      logger.info("Processing Query Batch Response.");
      queryResponseBundle = extractNestedBundledResources(queryResponseBundle);

      // Add resources to CRD Prefetch
      FhirRequestProcessor.addToCrdPrefetchRequest(prefetch, ResourceType.Task, queryResponseBundle.getEntry());
    } catch (Exception e) {
      logger.error("Error during Query Batch Request for OrderDispatch: {}", e.getMessage(), e);
    }
  }

  public void performAppointmentQueryBatchRequest(AppointmentBookRequest appointmentBookRequestRequest, CrdPrefetch crdPrefetch) {
    logger.info("***** ***** Performing Query Batch Request.");
    // Get the IDs of references in the request's appointments.

    Bundle appoinmentsBundle = appointmentBookRequestRequest.getContext().getAppointments();

    // Perform the query batch request for each of the draft orders.
    for(BundleEntryComponent bec : appoinmentsBundle.getEntry()) {
      this.performBundleQueryBatchRequest(bec.getResource(), crdPrefetch, appointmentBookRequestRequest);
    }
  }

  private void performBundleQueryBatchRequest(Resource resource, CrdPrefetch crdResponse, CdsRequest<?, ?> cdsRequest) {
    ResourceType requestType = resource.getResourceType();
    // The list of references that should be queried in the batch request.
    List<String> requiredReferences = new ArrayList<String>();
    // Extract the references by iterating through the JSON.
    Gson gson = new Gson();
    final JsonObject jsonObject = gson.toJsonTree(resource).getAsJsonObject();
    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
      FhirRequestProcessor.extractReferenceIds(requiredReferences, entry.getValue());
    }

    // Filter out references that already exist in the CRD Response.
    requiredReferences = requiredReferences.stream()
        .filter(referenceId -> !crdResponse.containsRequestResourceId(referenceId))
        .collect(Collectors.toList());

    logger.info("References to query: " + requiredReferences);
    if (requiredReferences.isEmpty()) {
      logger.info("A Query Batch Request is not needed: all references have already already fetched.");
      return;
    }

    // Build the Query Batch Request JSON.
    Bundle queryBatchRequestBundle = buildQueryBatchRequestBundle(requiredReferences);
    String queryBatchRequestBody = FhirContext.forR4().newJsonParser().encodeResourceToString(queryBatchRequestBundle);

    // Make the query batch request to the EHR server.
    Bundle queryResponseBundle = null;
    try {
      logger.info("Executing Query Batch Request: " + queryBatchRequestBody);
      queryResponseBundle = (Bundle) FhirRequestProcessor.executeFhirQueryBody(queryBatchRequestBody, cdsRequest, this.fhirComponents, HttpMethod.POST);
      queryResponseBundle = extractNestedBundledResources(queryResponseBundle);
      logger.info("Extracted Query Batch Resources: "
          + (queryResponseBundle).getEntry().stream().map(entry -> entry.getResource()).collect(Collectors.toList()));
    } catch (Exception e) {
      logger.error("Failed to backfill prefetch with Query Batch Request " + queryBatchRequestBody, e);
    }

    if (queryResponseBundle == null) {
      logger.error("No response recieved from the Query Batch Request.");
      return;
    }

    // Add the request resource to the query batch response as it may be missing.
    // Coverage and Subject are not automatically being
    // linked to the request object. It seems to somehow automatically link during
    // standard prefetch, but not here so we're doing it manually.
    List<Coverage> coverages = FhirRequestProcessor.extractCoverageFromBundle(queryResponseBundle);
    List<Patient> patients = FhirRequestProcessor.extractPatientsFromBundle(queryResponseBundle);
    FhirRequestProcessor.addInsuranceAndSubject(resource, patients, coverages);

    // Add the query batch response resources to the CRD Prefetch request.
    logger.info("Query Batch Response Entries: " + queryResponseBundle.getEntry());
    FhirRequestProcessor.addToCrdPrefetchRequest(crdResponse, requestType, queryResponseBundle.getEntry());
    logger.info("Post-Query Batch CRDResponse: " + crdResponse);
  }

  /**
   * Builds a query batch request bundle based on the given references to request.
   * 
   * @param resourceReferences
   * @return
   */
  private static Bundle buildQueryBatchRequestBundle(List<String> resourceReferences) {
    // http://build.fhir.org/ig/HL7/davinci-crd/hooks.html#fhir-resource-access
    Bundle queryBatchBundle = new Bundle();
    queryBatchBundle.setType(BundleType.BATCH);
    for (String reference : resourceReferences) {
      if (reference.contains(PRACTIONER_ROLE)) {
        reference = QueryBatchRequest.buildPractionerRoleQuery(reference);
      }
      BundleEntryComponent entry = new BundleEntryComponent();
      BundleEntryRequestComponent request = new BundleEntryRequestComponent();
      request.setMethod(HTTPVerb.GET);
      request.setUrl(reference);
      entry.setRequest(request);
      queryBatchBundle.addEntry(entry);
    }
    return queryBatchBundle;
  }

  /**
   * Adds support for PractitionerRole nested requests.
   * 
   * @param reference
   * @return
   */
  private static String buildPractionerRoleQuery(String reference) {
    String[] referenceIdSplit = reference.split("/");
    String referenceId = referenceIdSplit[referenceIdSplit.length - 1];
    String query = "PractitionerRole?_id=" + referenceId
        + "&_include=PractitionerRole:organization&_include=PractitionerRole:practitioner&_include=PractitionerRole:location";
    return query;
  }

  /**
   * Extracts the resources inside a bundled bundle to be at the top level of the
   * bundle, making them no longer nested.
   * 
   * @param resource
   * @return
   */
  private static Bundle extractNestedBundledResources(Bundle bundle) {
    List<BundleEntryComponent> entriesToAdd = new ArrayList<>();
    List<BundleEntryComponent> entriesToRemove = new ArrayList<>();
    for (int bundleIndex = 0; bundleIndex < bundle.getEntry().size(); bundleIndex++) {
      BundleEntryComponent entry = bundle.getEntry().get(bundleIndex);
      if (entry.getResource().getResourceType().equals(ResourceType.Bundle)) {
        Bundle bundledBundle = (Bundle) entry.getResource();
        entriesToAdd.addAll(bundledBundle.getEntry());
        entriesToRemove.add(entry);
      }
    }
    bundle.getEntry().addAll(entriesToAdd);
    bundle.getEntry().removeAll(entriesToRemove);
    return bundle;
  }

}
