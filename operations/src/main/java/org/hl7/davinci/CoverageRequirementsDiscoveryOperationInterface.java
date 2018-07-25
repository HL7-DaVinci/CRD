package org.hl7.davinci;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Parameters;

/**
 * The interface that defines the coverage-requirements-discovery.
 */
public interface CoverageRequirementsDiscoveryOperationInterface {

  @Operation(name = "$coverage-requirements-discovery", idempotent = true)
  Parameters coverageRequirementsDiscovery(
      @OperationParam(name = "request") Parameters.ParametersParameterComponent request,
      @OperationParam(name = "endpoint") Endpoint endpoint,
      @OperationParam(name = "requestQualification") CodeableConcept requestQualification
  );

}
