package org.hl7.davinci;

import ca.uhn.fhir.model.api.annotation.Block;
import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.r4.model.*;

import java.util.List;

@ResourceDef(name="DaVinciEligibilityResponse")
public class DaVinciEligibilityResponse extends EligibilityResponse {
    @Child(name="coverageDocumentation", max=Child.MAX_UNLIMITED)
    @Extension(url="http://hl7.org/davinci/crd/eligibilityResponse/coverageDocumentation", definedLocally=false, isModifier=false)
    private List<CoverageDocumentation> coverageDocumentation;

    public List<CoverageDocumentation> getCoverageDocumentation() {
        return coverageDocumentation;
    }

    public void setCoverageDocumentation(List<CoverageDocumentation> coverageDocumentation) {
        this.coverageDocumentation = coverageDocumentation;
    }

    @Block
    public static class CoverageDocumentation extends BackboneElement {
        @Child(name="requestType")
        @Extension(url="http://hl7.org/davinci/crd/eligibilityResponse/coverageDocumentation/requestType",
                definedLocally=false, isModifier=false)
        private List<CodeableConcept> requestType;

        @Child(name="serviceRequestType")
        @Extension(url="http://hl7.org/davinci/crd/eligibilityResponse/coverageDocumentation/serviceRequestType",
                definedLocally=false, isModifier=false)
        private List<CodeableConcept> serviceRequestType;

        @Child(name="service")
        @Extension(url="http://hl7.org/davinci/crd/eligibilityResponse/coverageDocumentation/service",
                definedLocally=false, isModifier=false)
        private List<Reference> service;

        @Child(name="responseType")
        @Extension(url="http://hl7.org/davinci/crd/eligibilityResponse/coverageDocumentation/responseType",
                definedLocally=false, isModifier=false)
        private CodeableConcept responseType;

        @Child(name="reference")
        @Extension(url="http://hl7.org/davinci/crd/eligibilityResponse/coverageDocumentation/reference",
                definedLocally=false, isModifier=false)
        private List<UriType> reference;

        @Child(name="validThrough")
        @Extension(url="http://hl7.org/davinci/crd/eligibilityResponse/coverageDocumentation/validThrough",
                definedLocally=false, isModifier=false)
        private DateType validThrough;

        @Child(name="display")
        @Extension(url="http://hl7.org/davinci/crd/eligibilityResponse/coverageDocumentation/display",
                definedLocally=false, isModifier=false)
        private String display;

        @Child(name="disclaimer")
        @Extension(url="http://hl7.org/davinci/crd/eligibilityResponse/coverageDocumentation/disclaimer",
                definedLocally=false, isModifier=false)
        private String disclaimer;

        @Child(name="endpoint")
        @Extension(url="http://hl7.org/davinci/crd/eligibilityResponse/coverageDocumentation/endpoint",
                definedLocally=false, isModifier=false)
        private Endpoint endpoint;

        @Override
        public BackboneElement copy() {
            CoverageDocumentation clone = new CoverageDocumentation();
            clone.setRequestType(getRequestType());
            clone.setServiceRequestType(getServiceRequestType());
            clone.setService(getService());
            clone.setResponseType(getResponseType());
            clone.setReference(getReference());
            clone.setValidThrough(getValidThrough());
            clone.setDisplay(getDisplay());
            clone.setDisclaimer(getDisclaimer());
            clone.setEndpoint(getEndpoint());
            return clone;
        }

        @Override
        public boolean isEmpty() {
            return super.isEmpty() && ElementUtil.isEmpty(
                    requestType, serviceRequestType, service, responseType, reference,
                    validThrough, display, disclaimer, endpoint);
        }

        public List<CodeableConcept> getRequestType() {
            return requestType;
        }

        public void setRequestType(List<CodeableConcept> requestType) {
            this.requestType = requestType;
        }

        public List<CodeableConcept> getServiceRequestType() {
            return serviceRequestType;
        }

        public void setServiceRequestType(List<CodeableConcept> serviceRequestType) {
            this.serviceRequestType = serviceRequestType;
        }

        public List<Reference> getService() {
            return service;
        }

        public void setService(List<Reference> service) {
            this.service = service;
        }

        public CodeableConcept getResponseType() {
            return responseType;
        }

        public void setResponseType(CodeableConcept responseType) {
            this.responseType = responseType;
        }

        public List<UriType> getReference() {
            return reference;
        }

        public void setReference(List<UriType> reference) {
            this.reference = reference;
        }

        public DateType getValidThrough() {
            return validThrough;
        }

        public void setValidThrough(DateType validThrough) {
            this.validThrough = validThrough;
        }

        public String getDisplay() {
            return display;
        }

        public void setDisplay(String display) {
            this.display = display;
        }

        public String getDisclaimer() {
            return disclaimer;
        }

        public void setDisclaimer(String disclaimer) {
            this.disclaimer = disclaimer;
        }

        public Endpoint getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(Endpoint endpoint) {
            this.endpoint = endpoint;
        }
    }
}
