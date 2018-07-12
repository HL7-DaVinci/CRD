package org.hl7.davinci;


import ca.uhn.fhir.model.api.annotation.Block;
import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.r4.model.BackboneElement;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.EligibilityRequest;
import org.hl7.fhir.r4.model.Reference;

import java.util.List;

@ResourceDef(name="EligibilityRequest")
public class DaVinciEligibilityRequest extends EligibilityRequest {
    @Child(name="serviceInformation", max=Child.MAX_UNLIMITED)
    @Extension(url="http://hl7.org/davinci/crd/eligibilityRequest/serviceInformation", definedLocally=false, isModifier=false)
    private List<ServiceInformation> serviceInformation;

    @Child(name="requestType", max=Child.MAX_UNLIMITED)
    @Extension(url="http://hl7.org/davinci/crd/eligibilityRequest/requestType", definedLocally=false, isModifier=false)
    private List<CodeableConcept> requestType;

    public List<ServiceInformation> getServiceInformation() {
        return serviceInformation;
    }

    public void setServiceInformation(List<ServiceInformation> serviceInformation) {
        this.serviceInformation = serviceInformation;
    }

    public List<CodeableConcept> getRequestType() {
        return requestType;
    }

    public void setRequestType(List<CodeableConcept> requestType) {
        this.requestType = requestType;
    }

    @Block
    public static class ServiceInformation extends BackboneElement {
        @Child(name="serviceRequestType")
        @Extension(url="http://hl7.org/davinci/crd/eligibilityRequest/serviceInformation/serviceRequestType",
                definedLocally=false, isModifier=false)
        private CodeableConcept serviceRequestType;

        @Child(name="serviceInformationReference")
        @Extension(url="http://hl7.org/davinci/crd/eligibilityRequest/serviceInformation/serviceInformationReference",
                definedLocally=false, isModifier=false)
        private Reference serviceInformationReference;

        @Child(name="service", max=Child.MAX_UNLIMITED)
        @Extension(url="http://hl7.org/davinci/crd/eligibilityRequest/serviceInformation/patientContext", definedLocally=false, isModifier=false)
        private List<Reference> patientContext;

        public Reference getServiceInformationReference() {
            return serviceInformationReference;
        }

        public void setServiceInformationReference(Reference serviceInformationReference) {
            this.serviceInformationReference = serviceInformationReference;
        }

        public List<Reference> getPatientContext() {
            return patientContext;
        }

        public void setPatientContext(List<Reference> patientContext) {
            this.patientContext = patientContext;
        }

        @Override
        public BackboneElement copy() {
            ServiceInformation clone = new ServiceInformation();
            clone.setServiceRequestType(getServiceRequestType());
            clone.setServiceInformationReference(getServiceInformationReference());
            clone.setPatientContext(getPatientContext());
            return clone;
        }

        @Override
        public boolean isEmpty() {
            return super.isEmpty() && ElementUtil.isEmpty(serviceRequestType, serviceInformationReference, patientContext);
        }

        public CodeableConcept getServiceRequestType() {
            return serviceRequestType;
        }

        public void setServiceRequestType(CodeableConcept serviceRequestType) {
            this.serviceRequestType = serviceRequestType;
        }
    }
}
