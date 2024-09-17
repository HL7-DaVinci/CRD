package org.hl7.davinci.r4.crdhook.encounterstart;

import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;



    /**
     * Class that contains the different prefetch template elements used in crd requests.
     * The templates are based on https://build.fhir.org/ig/HL7/davinci-crd/hooks.html#prefetch
     */

    public class CrdPrefetchTemplateElements {

        public static final PrefetchTemplateElement PATIENT = new PrefetchTemplateElement(
                "patient",
                Patient.class,
                "Patient/{{context.patientId}}");

        public static final PrefetchTemplateElement COVERAGE_REQUEST_BUNDLE = new PrefetchTemplateElement(
                "coverageBundle",
                Bundle.class,
                "Coverage?patient={{context.patientId}}");

        public static final PrefetchTemplateElement ENCOUNTER_BUNDLE = new PrefetchTemplateElement(
                "encounterBundle",
                Bundle.class,
                "Encounter?_id={{context.encounterId}}"
                        + "&_include=Encounter:patient"
                        + "&_include=Encounter:service-provider"
                        + "&_include=Encounter:practitioner"
                        + "&_include=Encounter:location");


    }

