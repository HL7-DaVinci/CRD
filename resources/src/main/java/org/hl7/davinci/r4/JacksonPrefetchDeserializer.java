package org.hl7.davinci.r4;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

import java.io.IOException;

public class JacksonPrefetchDeserializer extends StdDeserializer<CrdPrefetch> {

  public JacksonPrefetchDeserializer() {
    this(CrdPrefetch.class);
  }

  protected JacksonPrefetchDeserializer(Class<CrdPrefetch> vc) {
    super(vc);
  }

  @Override
  public CrdPrefetch deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    ObjectMapper mapper = (ObjectMapper) p.getCodec();
    JsonNode prefetchArray = mapper.readTree(p);
    if (!prefetchArray.isArray()) {
      throw new RuntimeException("Input prefetch must be in array form.");
    }
    CrdPrefetch crdPrefetch = new CrdPrefetch();
    FhirComponents fhirComponents = new FhirComponents();
    for(final JsonNode currentPrefetch : prefetchArray){
      String prefetchKey = currentPrefetch.fieldNames().next();
      JsonNode prefetchValue = currentPrefetch.get(prefetchKey).get("entry").get(0);
      String prefetchString = mapper.writeValueAsString(prefetchValue);
      Resource parsedResource = (Resource) fhirComponents.getJsonParser().parseResource(prefetchString);
      System.out.println("PARSED RESOURCE: " + parsedResource);
      addToCrdPrefetchRequest(crdPrefetch, parsedResource);
      System.out.println("PARSED PREFETCH: " + crdPrefetch);
    }
    System.out.println("DESERIALIZED PREFETCH: " + crdPrefetch);
    return crdPrefetch;
  }

  /**
   * Adds the given resource to the given CrdPrefetch based on the given resource
   * type.
   * 
   * @param crdPrefetch
   * @param resource
   * @param requestType
   */
  public static void addToCrdPrefetchRequest(CrdPrefetch crdPrefetch, Resource resource) {
    ResourceType resourceType = resource.getResourceType();
    System.out.println("Resource type: " + resourceType);
    switch (resourceType) {
      case DeviceRequest:
        if (crdPrefetch.getDeviceRequestBundle() == null) {
          crdPrefetch.setDeviceRequestBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getDeviceRequestBundle(), resource);
        break;
      case Coverage:
        if (crdPrefetch.getCoverage() == null) {
          crdPrefetch.setCoverage(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getCoverage(), resource);
        break;
      case MedicationRequest:
        if (crdPrefetch.getMedicationRequestBundle() == null) {
          crdPrefetch.setMedicationRequestBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getMedicationRequestBundle(), resource);
        break;
      case NutritionOrder:
        if (crdPrefetch.getNutritionOrderBundle() == null) {
          crdPrefetch.setNutritionOrderBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getNutritionOrderBundle(), resource);
        break;
      case ServiceRequest:
        if (crdPrefetch.getServiceRequestBundle() == null) {
          crdPrefetch.setServiceRequestBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getServiceRequestBundle(), resource);
        break;
      case SupplyRequest:
        if (crdPrefetch.getSupplyRequestBundle() == null) {
          crdPrefetch.setSupplyRequestBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getSupplyRequestBundle(), resource);
        break;
      case Appointment:
        if (crdPrefetch.getAppointmentBundle() == null) {
          crdPrefetch.setAppointmentBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getAppointmentBundle(), resource);
        break;
      case Encounter:
        if (crdPrefetch.getEncounterBundle() == null) {
          crdPrefetch.setEncounterBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getEncounterBundle(), resource);
        break;
      case MedicationDispense:
        if (crdPrefetch.getMedicationDispenseBundle() == null) {
          crdPrefetch.setMedicationDispenseBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getMedicationDispenseBundle(), resource);
        break;
      case MedicationStatement:
        if (crdPrefetch.getMedicationStatementBundle() == null) {
          crdPrefetch.setMedicationStatementBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getMedicationStatementBundle(), resource);
        break;
      default:
        throw new RuntimeException("Unexpected resource type for draft order request. Given " + resourceType + ".");
    }
  }

    /**
   * Adds non-duplicate resources that do not already exist in the bundle to the bundle.
   */
  private static void addNonDuplicateResourcesToBundle(Bundle bundle, Resource resourceToAdd) {
    // for (BundleEntryComponent resourceEntry : resourcesToAdd) {
    //   if (!bundle.getEntry().stream()
    //       .anyMatch(bundleEntry -> bundleEntry.getResource().getId().equals(resourceEntry.getResource().getId()))) {
    //     bundle.addEntry(resourceEntry);
    //   }
    // }
    BundleEntryComponent bec = new BundleEntryComponent();
    bec.setResource(resourceToAdd);
    bundle.addEntry(bec);
  }
  
}
