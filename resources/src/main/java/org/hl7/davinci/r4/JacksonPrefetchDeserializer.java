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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JacksonPrefetchDeserializer extends StdDeserializer<CrdPrefetch> {

  static final Logger logger = LoggerFactory.getLogger(JacksonPrefetchDeserializer.class);

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
    for (final JsonNode currentPrefetch : prefetchArray) {
      String prefetchKey = currentPrefetch.fieldNames().next();
      JsonNode currentPrefetchEntries = currentPrefetch.get(prefetchKey).get("entry");
      for (final JsonNode currentPrefetchElement : currentPrefetchEntries) {
        String prefetchString = mapper.writeValueAsString(currentPrefetchElement);
        Resource parsedResource = (Resource) fhirComponents.getJsonParser().parseResource(prefetchString);
        logger.info("Prefetch Deserializer::Parsed prefetch '" + prefetchKey + "' resource '" + parsedResource.getId() + "'.");
        addToCrdPrefetchRequest(crdPrefetch, prefetchKey, parsedResource);
      }
    }
    logger.info("Prefetch Deserializer::Deserialized Prefetch - " + crdPrefetch);
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
  public static void addToCrdPrefetchRequest(CrdPrefetch crdPrefetch, String prefetchKey, Resource resource) {
    ResourceType resourceType = resource.getResourceType();
    switch (prefetchKey) {
      case "deviceRequestBundle":
        if (crdPrefetch.getDeviceRequestBundle() == null) {
          crdPrefetch.setDeviceRequestBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getDeviceRequestBundle(), resource);
        break;
      case "coverageBundle":
        if (crdPrefetch.getCoverageBundle() == null) {
          crdPrefetch.setCoverage(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getCoverageBundle(), resource);
        break;
      case "medicationRequestBundle":
        if (crdPrefetch.getMedicationRequestBundle() == null) {
          crdPrefetch.setMedicationRequestBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getMedicationRequestBundle(), resource);
        break;
      case "nutritionOrderBundle":
        if (crdPrefetch.getNutritionOrderBundle() == null) {
          crdPrefetch.setNutritionOrderBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getNutritionOrderBundle(), resource);
        break;
      case "serviceRequestBundle":
        if (crdPrefetch.getServiceRequestBundle() == null) {
          crdPrefetch.setServiceRequestBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getServiceRequestBundle(), resource);
        break;
      case "supplyRequestBundle":
        if (crdPrefetch.getSupplyRequestBundle() == null) {
          crdPrefetch.setSupplyRequestBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getSupplyRequestBundle(), resource);
        break;
      case "appointmentBundle":
        if (crdPrefetch.getAppointmentBundle() == null) {
          crdPrefetch.setAppointmentBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getAppointmentBundle(), resource);
        break;
      case "encounterBundle":
        if (crdPrefetch.getEncounterBundle() == null) {
          crdPrefetch.setEncounterBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getEncounterBundle(), resource);
        break;
      case "medicationDispenseBundle":
        if (crdPrefetch.getMedicationDispenseBundle() == null) {
          crdPrefetch.setMedicationDispenseBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdPrefetch.getMedicationDispenseBundle(), resource);
        break;
      case "medicationStatementBundle":
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
  private static void addNonDuplicateResourcesToBundle(Bundle bundle, Resource ...resourcesToAdd) {
    for (Resource resource : resourcesToAdd) {
      if (!bundle.getEntry().stream()
          .anyMatch(bundleEntry -> bundleEntry.getResource().getId().equals(resource.getId()))) {
        BundleEntryComponent bec = new BundleEntryComponent();
        bec.setResource(resource);
        bundle.addEntry(bec);
      }
    }
  }
  
}
