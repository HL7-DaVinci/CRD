package org.hl7.davinci.endpoint.cdshooks.services.crd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.cdshooks.CdsRequest;
import org.cdshooks.CdsResponse;
import org.cdshooks.Hook;
import org.cdshooks.Prefetch;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.components.PrefetchHydrator;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hl7.davinci.endpoint.database.RequestLog;
import org.hl7.davinci.endpoint.database.RequestService;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public abstract class CdsService<requestTypeT extends CdsRequest<?,?>> {

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

  private List<PrefetchTemplateElement> prefetchElements = null;

  private FhirComponentsT fhirComponents;

  public List<PrefetchTemplateElement> getPrefetchElements() {
    return prefetchElements;
  }

  Class<?> requestClass = null;

  @Autowired
  CoverageRequirementRuleFinder ruleFinder;

  @Autowired
  RequestService requestService;
  /**
   * Create a new cdsservice.
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

  public CdsResponse handleRequest(@Valid @RequestBody requestTypeT request) {

    boolean[] timeline = new boolean[5];

    logger.info("handleRequest: start");
    // authorized
    timeline[0] = true;

    logger.info(
        this.title + ":" + request.getContext()
    );

    PrefetchHydrator prefetchHydrator = new PrefetchHydrator(this, request,
        this.fhirComponents);
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
    requestLog.setFhirVersion(this.fhirVersion);
    requestLog.setHookType(this.id);
    requestLog.setTimeline(timeline);
    requestService.edit(requestLog);

    // Parsed request
    timeline[1] = true;
    requestLog.setTimeline(timeline);
    requestService.edit(requestLog);

    PrefetchHydrator prefetchHydrator = new PrefetchHydrator<bundleTypeT>(this, request,
        fhirComponents.getFhirContext());
    prefetchHydrator.hydrate();

    // hydrated
    timeline[2] = true;
    requestLog.setTimeline(timeline);
    requestService.edit(requestLog);

    CdsResponse response = new CdsResponse();

    // got requests
    timeline[3] = true;
    requestLog.setTimeline(timeline);
    requestService.edit(requestLog);

    requestLog.setPatientAge(ri.getPatientAge());
    requestLog.setPatientGender(ri.getPatientGender());
    requestLog.setCode(ri.getCode());
    requestLog.setCodeSystem(ri.getCodeSystem());

    List<CoverageRequirementRule> coverageRequirementRules;
    try {
      coverageRequirementRules = this.findRules(request);
    } catch (RequestIncompleteException e) {
      response.addCard(CardBuilder.summaryCard(e.getMessage()));
      requestLog.setResults(e.getMessage());
      logger.error(e.getMessage());
      requestService.edit(requestLog);
      return response;
    }

    if (coverageRequirementRules.size() == 0) {
      response.addCard(CardBuilder.summaryCard("No documentation rules found"));
      requestLog.setResults(errorStr);
    } else {
      requestLog.addRulesFound(coverageRequirementRules);
      requestLog.setResults(String.valueOf(coverageRequirementRules.size()) + " documentation rule(s) found");
      for (CoverageRequirementRule rule : coverageRequirementRules) {
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

  // Implement this in child class
  public abstract List<CoverageRequirementRule> findRules(requestTypeT request) throws RequestIncompleteException;

}


