package org.hl7.davinci.endpoint.cdshooks.services.crd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import org.cdshooks.CdsRequest;
import org.cdshooks.CdsResponse;
import org.cdshooks.Hook;
import org.cdshooks.Prefetch;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.FhirComponentsT.Version;
import org.hl7.davinci.PatientInfo;
import org.hl7.davinci.PractitionerRoleInfo;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.components.PrefetchHydrator;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleFinder;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleQuery;
import org.hl7.davinci.endpoint.database.RequestLog;
import org.hl7.davinci.endpoint.database.RequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Component
public abstract class CdsService<requestTypeT extends CdsRequest<?, ?>> {

  static final Logger logger = LoggerFactory.getLogger(CdsService.class);

  /**
   * The {id} portion of the URL to this service which is available at {baseUrl}/cds-services/{id}.
   * REQUIRED
   */
  public String id = null;

  /**
   * The hook this service should be invoked on. REQUIRED
   */
  public Hook hook = null;

  /**
   * The human-friendly name of this service. RECOMMENDED
   */
  public String title = null;

  /**
   * The description of this service. REQUIRED
   */
  public String description = null;

  /**
   * An object containing key/value pairs of FHIR queries that this service is requesting that the
   * EHR prefetch and provide on each service call. The key is a string that describes the type of
   * data being requested and the value is a string representing the FHIR query. OPTIONAL
   */
  public Prefetch prefetch = null;
  Class<?> requestClass = null;
  @Autowired
  RequestService requestService;
  @Autowired
  private CoverageRequirementRuleFinder ruleFinder;
  private List<PrefetchTemplateElement> prefetchElements = null;
  private FhirComponentsT fhirComponents;

  /**
   * Create a new cdsservice.
   *
   * @param id Will be used in the url, should be unique.
   * @param hook Which hook can call this.
   * @param title Human title.
   * @param description Human description.
   * @param prefetchElements List of prefetch elements, will be in prefetch template.
   * @param fhirComponents Fhir components to use
   */
  public CdsService(String id, Hook hook, String title, String description,
      List<PrefetchTemplateElement> prefetchElements, FhirComponentsT fhirComponents) {
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
    this.prefetchElements = prefetchElements;
    prefetch = new Prefetch();
    for (PrefetchTemplateElement prefetchElement : prefetchElements) {
      this.prefetch.put(prefetchElement.getKey(), prefetchElement.getQuery());
    }
    this.fhirComponents = fhirComponents;
  }

  public List<PrefetchTemplateElement> getPrefetchElements() {
    return prefetchElements;
  }

  public CdsResponse handleRequest(@Valid @RequestBody requestTypeT request) {

    boolean[] timeline = new boolean[5];

    logger.info("handleRequest: start");
    logger.info(this.title + ":" + request.getContext());
    // authorized
    timeline[0] = true;

    // create the RequestLog
    String requestStr;
    try {
      ObjectMapper mapper = new ObjectMapper();
      ObjectWriter w = mapper.writer();
      requestStr = w.writeValueAsString(request);
    } catch (Exception e) {
      logger.error("failed to write request json: " + e.getMessage());
      requestStr = "error";
    }
    RequestLog requestLog = new RequestLog(requestStr.getBytes(), new Date().getTime());
    requestLog = requestService.create(requestLog);
    requestLog.setFhirVersion(this.fhirComponents.getFhirVersion().toString());
    requestLog.setHookType(this.id);
    requestLog.setTimeline(timeline);
    requestService.edit(requestLog);

    // Parsed request
    timeline[1] = true;
    requestLog.setTimeline(timeline);
    requestService.edit(requestLog);

    PrefetchHydrator prefetchHydrator = new PrefetchHydrator(this, request,
        this.fhirComponents);
    prefetchHydrator.hydrate();

    // hydrated
    timeline[2] = true;
    requestLog.setTimeline(timeline);
    requestService.edit(requestLog);

    CdsResponse response = new CdsResponse();

    List<CoverageRequirementRuleQuery> queries;
    try {
      queries = this.getQueries(request);
    } catch (RequestIncompleteException e) {
      response.addCard(CardBuilder.summaryCard(e.getMessage()));
      logger.error(e.getMessage());
      requestLog.setResults(e.getMessage());
      requestService.edit(requestLog);
      return response;
    }
    // got requests
    timeline[3] = true;
    requestLog.setTimeline(timeline);
    requestService.edit(requestLog);

    List<String> codes = new ArrayList<>();
    List<String> codeSystems = new ArrayList<>();
    List<CoverageRequirementRule> rules = new ArrayList<>();
    for (CoverageRequirementRuleQuery query : queries) {
      query.execute();
      rules.addAll(query.getResponse()); // will be zero or more
      codes.add(query.getCriteria().getEquipmentCode());
      codes.add(query.getCriteria().getCodeSystem());
    }
    requestLog.setPatientAge(queries.get(0).getCriteria().getAge());
    requestLog.setPatientGender(String.valueOf(queries.get(0).getCriteria().getGenderCode()));
    requestLog.setPatientAddressState(queries.get(0).getCriteria().getPatientAddressState());
    requestLog.setProviderAddressState(queries.get(0).getCriteria().getProviderAddressState());
    requestLog.setCode(String.join(", ", codes));
    requestLog.setCodeSystem(String.join(", ", codeSystems));

    if (rules.size() == 0) {
      response.addCard(CardBuilder.summaryCard("No documentation rules found"));
      requestLog.setResults("No documentation rules found");
    } else {
      requestLog.addRulesFound(rules);
      requestLog.setResults(String.valueOf(rules.size()) + " documentation rule(s) found");
      for (CoverageRequirementRule rule : rules) {
        response.addCard(CardBuilder.transform(rule));
      }
    }
    // Searched
    timeline[4] = true;
    requestLog.setTimeline(timeline);
    requestService.edit(requestLog);
    CardBuilder.errorCardIfNonePresent(response);
    logger.info("handleRequest: end");
    return response;
  }



  protected List<CoverageRequirementRuleQuery> resourcesToQueries(List<?> codings, Object patient,
      Object practitionerRole,
      PatientInfo patientInfo, PractitionerRoleInfo practitionerRoleInfo)
      throws RequestIncompleteException {

    List<CoverageRequirementRuleQuery> queries = new ArrayList<>();
    boolean doR4 = (fhirComponents.getFhirVersion() == Version.R4);

    if (codings == null || codings.size() == 0) {
      throw new RequestIncompleteException("Unable to parse a device code out of the request.");
    }
    if (patient == null) {
      throw new RequestIncompleteException("No patient could be (pre)fetched in this request.");
    }
    if (practitionerRole == null) {
      if (doR4) {
        throw new RequestIncompleteException("Unable to find the practitioner role.");
      }
      // ignore for stu3 for now
    }
    for (Object coding : codings) {
      String code;
      String codeSystem;
      if (doR4) {
        org.hl7.fhir.r4.model.Coding c = ((org.hl7.fhir.r4.model.Coding) coding);
        code = c.getCode();
        codeSystem = c.getSystem();
      } else {
        org.hl7.fhir.dstu3.model.Coding c = ((org.hl7.fhir.dstu3.model.Coding) coding);
        code = c.getCode();
        codeSystem = c.getSystem();
      }
      if (code == null || codeSystem == null) {
        logger.error("Found coding with a null code or system.");
        continue;
      }
      CoverageRequirementRuleQuery query = new CoverageRequirementRuleQuery(ruleFinder);
      query.getCriteria().setAge(patientInfo.getPatientAge())
          .setGenderCode(patientInfo.getPatientGenderCode())
          .setPatientAddressState(patientInfo.getPatientAddressState())
          .setCodeSystem(codeSystem)
          .setEquipmentCode(code)
          .setProviderAddressState(practitionerRoleInfo.getLocationAddressState());
      queries.add(query);
    }
    return queries;
  }

  private List<CoverageRequirementRuleQuery> getQueries(requestTypeT request)
      throws RequestIncompleteException {
    List<CoverageRequirementRuleQuery> queries = makeQueries(request);
    if (queries.size() == 0) {
      throw new RequestIncompleteException(
          "Unable to (pre)fetch any supported resources from the bundle.");
    }
    return queries;
  }

  // Implement this in child class
  public abstract List<CoverageRequirementRuleQuery> makeQueries(requestTypeT request)
      throws RequestIncompleteException;

}


