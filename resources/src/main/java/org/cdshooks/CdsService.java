package org.cdshooks;

import org.springframework.stereotype.Component;

@Component
public abstract class CdsService {
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

  /**
   * Create a new cdsservice.
   * @param id  Will be used in the url, should be unique.
   * @param hook  Which hook can call this.
   * @param title Human title.
   * @param description Human description.
   * @param prefetch What to prefetch.
   */
  public CdsService(String id, Hook hook, String title, String description, Prefetch prefetch) {
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
  }
}
