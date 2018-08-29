package org.hl7.davinci.cdshooks;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import com.sun.tools.javac.jvm.Code;
import org.hl7.davinci.JacksonHapiSerializer;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that supports the representation of prefetch information in a CDS Hook request.
 * It appears that for CRD, prefetch information will be the same, regardless of hook type (order-review or
 * medication-prescribe).
 */
public class CrdPrefetch {
  static final Logger logger = LoggerFactory.getLogger(CrdPrefetch.class);

  // Patient
  @JsonSerialize(using = JacksonHapiSerializer.class)
  private Patient patient;

  // Relevant Coverage
  @JsonSerialize(using = JacksonHapiSerializer.class)
  private Coverage coverage;

  // Authoring Practitioner
  @JsonSerialize(using = JacksonHapiSerializer.class)
  private Practitioner provider;

  // Authoring Organization
  @JsonSerialize(using = JacksonHapiSerializer.class)
  private Organization organization;

  // Requested performing Practitioner
  @JsonSerialize(using = JacksonHapiSerializer.class)
  private Practitioner performingPractitioner;

  // Requested performing Organization
  @JsonSerialize(using = JacksonHapiSerializer.class)
  private Organization performingOrganization;

  // Requested Location
  @JsonSerialize(using = JacksonHapiSerializer.class)
  private Location location;

  // associated Medication
  @JsonSerialize(using = JacksonHapiSerializer.class)
  private Medication medication;

  // associated Device
  @JsonSerialize(using = JacksonHapiSerializer.class)
  private Device device;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  private Organization insurer;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  private PractitionerRole practitionerRole;

  @JsonIgnore
  private FhirContext fhirContext;

  /**
   * Constructor that creates a FhirContext used in parsing the FHIR resources
   * out of the request.
   */
  public CrdPrefetch() {
    fhirContext = FhirContext.forR4();
  }

  public Patient getPatient() {
    return patient;
  }

  public Coverage getCoverage() {
    return coverage;
  }

  public Practitioner getProvider() {
    return provider;
  }

  public Organization getOrganization() { return organization; }

  public Practitioner getPerformingPractitioner() { return performingPractitioner; }

  public Organization getPerformingOrganization() { return performingOrganization; }

  public Location getLocation() {
    return location;
  }

  public Medication getMedication() { return medication; }

  public Device getDevice() { return device; }

  public Organization getInsurer() {
    return insurer;
  }

  public PractitionerRole getPractitionerRole() {
    return practitionerRole;
  }



  public void setPatient(Patient patient) {
    this.patient = patient;
  }

  public void setCoverage(Coverage coverage) {
    this.coverage = coverage;
  }

  public void setProvider(Practitioner provider) {
    this.provider = provider;
  }

  public void setOrganization(Organization organization) { this.organization = organization; }

  public void setPerformingPractitioner(Practitioner performingPractitioner) { this.performingPractitioner = performingPractitioner; }

  public void setPerformingOrganization(Organization performingOrganization) { this.performingOrganization = performingOrganization; }

  public void setLocation(Location location) {
    this.location = location;
  }

  public void setMedication(Medication medication) { this.medication = medication; }

  public void setDevice(Device device) { this.device = device; }

  public void setInsurer(Organization insurer) {
    this.insurer = insurer;
  }

  public void setPractitionerRole(PractitionerRole practitionerRole) {
    this.practitionerRole = practitionerRole;
  }

  public void setFhirContext(FhirContext fhirContext) {
    this.fhirContext = fhirContext;
  }

  /**
   * Parse the FHIR Patient from the JSON request.
   * @param fhirResource is the input JSON node containing the Patient FHIR resource.
   */
  @JsonSetter("patient")
  public void setPatientFhirResource(JsonNode fhirResource) {
    String fhirString = fhirResource.toString();
    try {
      patient = fhirContext.newJsonParser().parseResource(Patient.class, fhirString);
    } catch (Exception e) {
      logger.warn("failed to parse patient: " + e.getMessage());
    }
  }

  /**
   * Parse the FHIR Coverage from the JSON request.
   * @param fhirResource is the input JSON node containing the Coverage FHIR resource.
   */
  @JsonSetter("coverage")
  public void setCoverageFhirResource(JsonNode fhirResource) {
    String fhirString = fhirResource.toString();
    try {
      coverage = fhirContext.newJsonParser().parseResource(Coverage.class, fhirString);
    } catch (Exception e) {
      logger.warn("failed to parse coverage: " + e.getMessage());
    }
  }

  /**
   * Parse the FHIR Practitioner from the JSON request.
   * @param fhirResource is the input JSON node containing the Practitioner FHIR resource.
   */
  @JsonSetter("provider")
  public void setProviderFhirResource(JsonNode fhirResource) {
    String fhirString = fhirResource.toString();
    try {
      provider = fhirContext.newJsonParser().parseResource(Practitioner.class, fhirString);
    } catch (Exception e) {
      logger.warn("failed to parse provider: " + e.getMessage());
    }
  }

  /**
   * Parse the FHIR Organization from the JSON request.
   * @param fhirResource is the input JSON node containing the Organization FHIR resource.
   */
  @JsonSetter("organization")
  public void setOrganizationFhirResource(JsonNode fhirResource) {
    String fhirString = fhirResource.toString();
    try {
      organization = fhirContext.newJsonParser().parseResource(Organization.class, fhirString);
    } catch (Exception e) {
      logger.warn("failed to parse organization: " + e.getMessage());
    }
  }

  /**
   * Parse the FHIR Practitioner from the JSON request.
   * @param fhirResource is the input JSON node containing the Practitioner FHIR resource.
   */
  @JsonSetter("performingPractitioner")
  public void setPerformingPractitionerFhirResource(JsonNode fhirResource) {
    String fhirString = fhirResource.toString();
    try {
      performingPractitioner = fhirContext.newJsonParser().parseResource(Practitioner.class, fhirString);
    } catch (Exception e) {
      logger.warn("failed to parse performingPractitioner: " + e.getMessage());
    }
  }

  /**
   * Parse the FHIR Organization from the JSON request.
   * @param fhirResource is the input JSON node containing the Organization FHIR resource.
   */
  @JsonSetter("performingOrganization")
  public void setPerformingOrganizationFhirResource(JsonNode fhirResource) {
    String fhirString = fhirResource.toString();
    try {
      performingOrganization = fhirContext.newJsonParser().parseResource(Organization.class, fhirString);
    } catch (Exception e) {
      logger.warn("failed to parse performingOrganization: " + e.getMessage());
    }
  }

  /**
   * Parse the FHIR Location from the JSON request.
   * @param fhirResource is the input JSON node containing the Location FHIR resource.
   */
  @JsonSetter("location")
  public void setLocationFhirResource(JsonNode fhirResource) {
    String fhirString = fhirResource.toString();
    try {
      location = fhirContext.newJsonParser().parseResource(Location.class, fhirString);
    } catch (Exception e) {
      logger.warn("failed to parse location: " + e.getMessage());
    }
  }

  /**
   * Parse the FHIR CodeableConcept from the JSON request.
   * @param fhirResource is the input JSON node containing the CodeableConcept FHIR resource.
   */
  @JsonSetter("medication")
  public void setMedicationFhirResource(JsonNode fhirResource) {
    String fhirString = fhirResource.toString();
    try {
      medication = fhirContext.newJsonParser().parseResource(Medication.class, fhirString);
    } catch (Exception e) {
      logger.warn("failed to parse medication: " + e.getMessage());
    }
  }

  /**
   * Parse the FHIR CodeableConcept from the JSON request.
   * @param fhirResource is the input JSON node containing the CodeableConcept FHIR resource.
   */
  @JsonSetter("device")
  public void setDeviceFhirResource(JsonNode fhirResource) {
    String fhirString = fhirResource.toString();
    try {
      device = fhirContext.newJsonParser().parseResource(Device.class, fhirString);
    } catch (Exception e) {
      logger.warn("failed to parse device: " + e.getMessage());
    }
  }

  /**
   * Parse the FHIR Organization from the JSON request.
   * @param fhirResource is the input JSON node containing the Organization FHIR resource.
   */
  @JsonSetter("insurer")
  public void setInsurerFhirResource(JsonNode fhirResource) {
    String fhirString = fhirResource.toString();
    try {
      insurer = fhirContext.newJsonParser().parseResource(Organization.class, fhirString);
    } catch (Exception e) {
      logger.warn("failed to parse insurer: " + e.getMessage());
    }
  }

  /**
   * Parse the FHIR PractitionerRole from the JSON request.
   * @param fhirResource is the input JSON node containing the PractitionerRole FHIR resource.
   */
  @JsonSetter("practitionerRole")
  public void setPractitionerRoleFhirResource(JsonNode fhirResource) {
    String fhirString = fhirResource.toString();
    try {
      practitionerRole = fhirContext.newJsonParser().parseResource(PractitionerRole.class, fhirString);
    } catch (Exception e) {
      logger.warn("failed to parse practitionerRole: " + e.getMessage());
    }
  }
}
