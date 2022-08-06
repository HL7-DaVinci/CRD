package org.hl7.davinci;

import org.hl7.fhir.instance.model.api.IBaseResource;

public class PrefetchTemplateElement {

  private String key;
  private String query;
  private Class<? extends IBaseResource> returnType;

  /**
   * Describes a prefetch element. Note that the key and query are exposed in the service
   * endpoint, whereas the returnType is for internal use only.
   * @param key The key that will be associated with the result of the query in the prefetch.
   * @param query The query to execute against the fhir server.
   * @param returnType The type to cast the result of a successful query to (e.g. a Bundle)
   */
  public PrefetchTemplateElement(String key, Class<? extends IBaseResource> returnType, String query) {
    this.key = key;
    this.query = query;
    this.returnType = returnType;
  }

  public String getKey() {
    return key;
  }

  public String getQuery() {
    return query;
  }

  public Class<? extends IBaseResource> getReturnType() {
    return returnType;
  }

  @Override
  public String toString(){
    return "[" + key + ", " + query + ", " + returnType + "]";
  }
}