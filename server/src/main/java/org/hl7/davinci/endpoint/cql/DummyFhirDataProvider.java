package org.hl7.davinci.endpoint.cql;


import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.dstu3.model.Enumeration;
import org.opencds.cqf.cql.data.fhir.BaseDataProviderStu3;
import org.opencds.cqf.cql.runtime.Code;
import org.opencds.cqf.cql.runtime.Interval;

/**
 * Created by Christopher Schuler on 6/19/2017.
 */
public class DummyFhirDataProvider extends BaseDataProviderStu3 {

  public DummyFhirDataProvider() {
    this("org.hl7.fhir.dstu3.model");
  }

  public DummyFhirDataProvider(String packageName) {
    setPackageName(packageName);
    FhirContext fhirContext = new org.hl7.davinci.stu3.FhirComponents().getFhirContext();
    setFhirContext(fhirContext);
  }

  public Iterable<Object> retrieve(String context, Object contextValue, String dataType, String templateId,
      String codePath, Iterable<Code> codes, String valueSet, String datePath, String dateLowPath,
      String dateHighPath, Interval dateRange) {

    throw new RuntimeException("All data must be passed in as parameters");
  }

  @Override
  public void setValue(Object target, String path, Object value) {
    if (target instanceof Enumeration && path.equals("value")) {
      ((Enumeration)target).setValueAsString((String)value);
      return;
    }

    super.setValue(target, path, value);
  }

  @Override
  protected Object resolveProperty(Object target, String path) {
    if (target instanceof Enumeration && path.equals("value")) {
      return ((Enumeration)target).getValueAsString();
    }

    return super.resolveProperty(target, path);
  }
}

