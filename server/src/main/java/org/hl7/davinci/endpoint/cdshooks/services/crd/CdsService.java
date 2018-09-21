package org.hl7.davinci.endpoint.cdshooks.services.crd;

import org.cdshooks.CdsRequest;
import org.cdshooks.CdsResponse;
import org.cdshooks.Hook;
import org.cdshooks.Prefetch;
import org.hl7.davinci.FhirComponentT;
import org.hl7.davinci.UtilitiesInterface;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.components.PrefetchHydrator;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleFinder;
import org.hl7.davinci.r4.Utilities;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public abstract class CdsService<bundleTypeT extends IBaseBundle, requestTypeT,
    patientTypeT extends IDomainResource, codeableConceptTypeT extends ICompositeType> {

  static final Logger logger = LoggerFactory.getLogger(CdsService.class);

  /**
   * The {id} portion of the URL to this service which is available at {baseUrl}/cds-services/{id}.
   * REQUIRED
   */
  public String id = null;

  /** The hook this service should be invoked on. REQUIRED */
  public Hook hook = null;

  /** The human-friendly name of this service. RECOMMENDED */
  public String title = null;

  /** The description of this service. REQUIRED */
  public String description = null;

  /**
   * An object containing key/value pairs of FHIR queries that this service is requesting that the
   * EHR prefetch and provide on each service call. The key is a string that describes the type of
   * data being requested and the value is a string representing the FHIR query. OPTIONAL
   */
  public Prefetch prefetch = null;

  public FhirComponentT fhirComponent =null;

  public String fhirVersion = null;

  UtilitiesInterface utilities = null;

  Class<?> requestClass = null;

  @Autowired
  CoverageRequirementRuleFinder ruleFinder;
  /**
   * Create a new cdsservice.
   * @param id  Will be used in the url, should be unique.
   * @param hook  Which hook can call this.
   * @param title Human title.
   * @param description Human description.
   * @param prefetch What to prefetch.
   */
  public CdsService(String id, Hook hook, String title,String description,
                    Prefetch prefetch, FhirComponentT fhirComponent, String fhirVersion) {
    if (id == null) {
      throw new NullPointerException("CDSService id cannot be null");
    }
    if (hook == null) {
      throw new NullPointerException("CDSService hook cannot be null");
    }
    if (description == null) {
      throw new NullPointerException("CDSService description cannot be null");
    }
    this.id = id;
    this.hook = hook;
    this.title = title;
    this.description = description;
    this.prefetch = prefetch;
    this.fhirComponent = fhirComponent;
    this.fhirVersion = fhirVersion;
    if (fhirVersion.equalsIgnoreCase("stu3")) {
      this.utilities = new org.hl7.davinci.stu3.Utilities();
    }else if (fhirVersion.equalsIgnoreCase("r4")) {
      this.utilities = new Utilities();
    }
    this.requestClass = requestClass;

  }

  public CdsResponse handleRequest(@Valid @RequestBody CdsRequest request) {
    logger.info("handleRequest: start");
    logger.info(

        this.title + ":" + request.getContext()
    );

    FhirComponentT fhirComponents = this.fhirComponent.getInstance();

    PrefetchHydrator prefetchHydrator = new PrefetchHydrator<bundleTypeT>(this, request,
        fhirComponents.getFhirContext());
    prefetchHydrator.hydrate();
    CdsResponse response = new CdsResponse();
    List<requestTypeT> requestList = getRequests(request);
    if (requestList == null) {
      logger.error("Prefetch " + this.title + " not a bundle");
      response.addCard(CardBuilder.summaryCard(
          this.title + " could not be (pre)fetched in this request "));
      return response;
    }
    System.out.println(requestList);
    for (requestTypeT genericRequest : requestList) {

      patientTypeT patient = null;
      codeableConceptTypeT cc = null;
      try {
        cc = getCc(genericRequest);
      } catch (FHIRException fe) {
        response
            .addCard(CardBuilder.summaryCard("Unable to parse the medication code out of the request"));
      }

      // See if the patient is in the prefetch
      try {
        patient = getPatient(genericRequest);
      } catch (Exception e) {
        response
            .addCard(CardBuilder.summaryCard("No patient could be (pre)fetched in this request"));
      }

      if (patient != null && cc != null) {
        Map<String, String> info = getInfo(patient,cc);
        int patientAge = Integer.parseInt(info.get("patientAge"));
        List<CoverageRequirementRule> coverageRequirementRules = ruleFinder
            .findRules(patientAge, info.get("patientGender").charAt(0), (String) info.get("ccCode"),
                (String) info.get("ccSystem"));
        if (coverageRequirementRules.size() == 0) {
          response.addCard(CardBuilder.summaryCard("No documentation rules found"));
        } else {
          for (CoverageRequirementRule rule: coverageRequirementRules) {
            response.addCard(CardBuilder.transform(rule));
          }
        }
      }
    }
    CardBuilder.errorCardIfNonePresent(response);
    logger.info("handleRequest: end");
    return response;
  }

  // Implement this in child class
  public abstract codeableConceptTypeT getCc(requestTypeT request) throws FHIRException;

  public abstract patientTypeT getPatient(requestTypeT request);

  public abstract List<requestTypeT> getRequests(CdsRequest request);

  public Map<String, String> getInfo(IBaseResource patient,ICompositeType cc) {
    Map<String,String> returnMap = new HashMap<>();
    if (fhirVersion.equalsIgnoreCase("stu3")) {
      Patient patientStu3 = (Patient) patient;
      CodeableConcept ccStu3 = (CodeableConcept) cc;

      returnMap.put("patientAge", Integer.toString(org.hl7.davinci.stu3.Utilities.calculateAge(patientStu3)));
      returnMap.put("patientGender", patientStu3.getGender().toCode());
      returnMap.put("ccCode", ccStu3.getCoding().get(0).getCode());
      returnMap.put("ccSystem", ccStu3.getCoding().get(0).getCode());
      return returnMap;
    } else if (fhirVersion.equalsIgnoreCase("r4")) {
      org.hl7.fhir.r4.model.Patient patientR4 = (org.hl7.fhir.r4.model.Patient) patient;
      org.hl7.fhir.r4.model.CodeableConcept ccR4 = (org.hl7.fhir.r4.model.CodeableConcept) cc;
      returnMap.put("patientAge", Integer.toString(Utilities.calculateAge(patientR4)));
      returnMap.put("patientGender", patientR4.getGender().toCode());
      returnMap.put("ccCode" , ccR4.getCoding().get(0).getCode());
      returnMap.put("ccSystem", ccR4.getCoding().get(0).getSystem());
      return returnMap;
    }
    return null;
  }
}


