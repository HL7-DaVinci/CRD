package org.hl7.davinci.endpoint.rems.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import org.hl7.davinci.endpoint.rems.database.requirement.MetRequirement;
import org.hl7.davinci.endpoint.rems.database.requirement.MetRequirementRepository;
import org.hl7.davinci.endpoint.rems.database.requirement.Requirement;

import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.rems.database.drugs.Drug;
import org.hl7.davinci.endpoint.rems.database.drugs.DrugsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import  org.hl7.davinci.endpoint.rems.database.rems.Rems;
import  org.hl7.davinci.endpoint.rems.database.rems.RemsRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * Provides the REST interface that can be interacted with at [base]/api/fhir and [base]/fhir.
 */
@RestController
public class RemsController {
    private static final Logger logger = Logger.getLogger(Application.class.getName());

    @Autowired
    private DrugsRepository drugsRepository;

    @Autowired
    private RemsRepository remsRepository;

    @Autowired
    private MetRequirementRepository metRequirementsRepository;

    @GetMapping(value = "/drug/{id}")
    @CrossOrigin
    public ResponseEntity<Drug> getRequirements(HttpServletRequest request, @PathVariable String id) throws IOException {
        Drug drug = drugsRepository.findById(id).get();
        return processRequirements(drug);
    }

    private ResponseEntity<Drug> processRequirements(Drug drug) {
        if (drug == null) {
            logger.warning("drug not found, return error (404)");
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_JSON_VALUE))
                .body(drug);
    }
    
      @PostMapping(value = "/rems")
      @CrossOrigin
      public ResponseEntity<Object> postRems(@RequestBody String jsonData) {
        Rems remsRequest = new Rems();
        MetRequirement returnMetReq = new MetRequirement();
        Boolean returnRemsRequest = false;

        JsonNode remsObject = JacksonUtil.toJsonNode(jsonData);

        String id = UUID.randomUUID().toString().replace("-", "");

        // extract params and questionnaire response identifier
        JsonNode params = getResource(remsObject, remsObject.get("entry").get(0).get("resource").get("focus").get("parameters").get("reference").textValue());
        JsonNode questionnaireResponse = getQuestionnaireResponse(remsObject);
        String[] questionnaireStringArray = questionnaireResponse.get("questionnaire").textValue().split("/");
        String requirementId = questionnaireStringArray[questionnaireStringArray.length - 1];

        // stakeholder and medication references
        String prescriptionReference = "";
        String practitionerReference = "";
        String pharmacistReference = "";
        String patientReference = "";
        for (JsonNode param : params.get("parameter")) {
            if (param.get("name").textValue().equals("prescription")) {
              prescriptionReference = param.get("reference").textValue();
            }
            else if (param.get("name").textValue().equals("prescriber")) {
              practitionerReference = param.get("reference").textValue();
            }
            else if (param.get("name").textValue().equals("pharmacy")) {
              pharmacistReference = param.get("reference").textValue();
            }      else if (param.get("name").textValue().equals("source-patient")) {
              patientReference = param.get("reference").textValue();
            }
        }

        // obtain drug information from database
        JsonNode presciption = getResource(remsObject, prescriptionReference);
        String prescriptionSystem = presciption.get("medicationCodeableConcept").get("coding").get(0).get("system").textValue();
        String prescriptionCode = presciption.get("medicationCodeableConcept").get("coding").get(0).get("code").textValue();
        Drug drug = drugsRepository.findDrugByCode(prescriptionSystem, prescriptionCode).get(0);

        // iterate through each requirement of the drug
        for (Requirement requirement : drug.getRequirements()) {

            // if the requirement is the one submitted continue
            if (requirement.getResource().getId().equals(requirementId)) {

              // if the req submitted is a patient enrollment form and requires creating a new case
              if (requirement.getCreateNewCase()) {
                returnRemsRequest = true;
                // create new rems request and add the created metReq to it
                remsRequest.setCase_number(id);
                remsRequest.setStatus("Pending");
                remsRequest.setResource(remsObject);
                remsRepository.save(remsRequest);

                // figure out which stakeholder the req corresponds to 
                String reqStakeholder = requirement.getStakeholder();
                String reqStakeholderReference = reqStakeholder.equals("prescriber") ? practitionerReference : (reqStakeholder.equals("pharmacist") ? pharmacistReference : patientReference);

                // create the metReq that was submitted
                MetRequirement metReq = new MetRequirement();
                metReq.setRequirement(requirement);
                metReq.setFunctionalId(reqStakeholderReference);
                metReq.setCompleted(true);
                metReq.addRemsRequest(remsRequest);
                remsRequest.addMetRequirement(metReq);
                metRequirementsRepository.save(metReq);

                // create child metReqs for this req
                for (Requirement subRequirement : requirement.getChildRequirements()) {
                  Boolean foundSubMatch = false;
                  // check if sub req has already been submitted 
                  for (MetRequirement possibleMetSubRequirement : subRequirement.getMetRequirements()) {
                    if ((possibleMetSubRequirement.getFunctionalId().equals(pharmacistReference) && subRequirement.getStakeholder().equals("pharmacist"))
                            || (possibleMetSubRequirement.getFunctionalId().equals(practitionerReference) && subRequirement.getStakeholder().equals("prescriber"))
                            || (possibleMetSubRequirement.getFunctionalId().equals(patientReference) && subRequirement.getStakeholder().equals("patient"))) {
                      
                      foundSubMatch = true;
                      possibleMetSubRequirement.setParentMetRequirement(metReq);
                      metReq.addChildMetRequirements(possibleMetSubRequirement);
                      metRequirementsRepository.save(possibleMetSubRequirement);
                      break;

                    }
                  }

                  // create a new sub met req if one is not found, set completed status to false 
                  if (!foundSubMatch) {
                    // figure out which stakeholder the req corresponds to 
                    String reqStakeholder2 = subRequirement.getStakeholder();
                    String reqStakeholderReference2 = reqStakeholder2.equals("prescriber") ? practitionerReference : (reqStakeholder2.equals("pharmacist") ? pharmacistReference : patientReference);

                    MetRequirement falseSubMetReq = new MetRequirement();
                    falseSubMetReq.setRequirement(subRequirement);
                    falseSubMetReq.setFunctionalId(reqStakeholderReference2);
                    falseSubMetReq.setCompleted(false);
                    falseSubMetReq.setParentMetRequirement(metReq);
                    metReq.addChildMetRequirements(falseSubMetReq);
                    metRequirementsRepository.save(falseSubMetReq);
                  }
                }

                metRequirementsRepository.save(metReq);

                // iterate through all other reqs again to create corresponding false metReqs / assign to existing 
                for (Requirement requirement2 : drug.getRequirements()) {
                  // skip if the req found is the same as in the outer loop and has already been processed
                  if (!requirement2.getResource().getId().equals(requirementId)) {
                    Boolean foundMatch = false;
                    MetRequirement newMetReq = new MetRequirement();
                    // check if an existing metReq has already been submitted 
                    for (MetRequirement possibleMetRequirement : requirement2.getMetRequirements()) {
                      if ((possibleMetRequirement.getFunctionalId().equals(pharmacistReference) && requirement2.getStakeholder().equals("pharmacist"))
                      || (possibleMetRequirement.getFunctionalId().equals(practitionerReference) && requirement2.getStakeholder().equals("prescriber"))
                      || (possibleMetRequirement.getFunctionalId().equals(patientReference) && requirement2.getStakeholder().equals("patient"))) {
                        foundMatch = true;
                        newMetReq = possibleMetRequirement;
                        newMetReq.addRemsRequest(remsRequest);
                        remsRequest.addMetRequirement(newMetReq);
                        metRequirementsRepository.save(newMetReq);
                        break;

                      }
                    }
                    // create a new metReq if one was not found, set completed status to false 
                    if (!foundMatch) {
                      // figure out which stakeholder the req corresponds to 
                      String reqStakeholder3 = requirement2.getStakeholder();
                      String reqStakeholderReference3 = reqStakeholder3.equals("prescriber") ? practitionerReference : (reqStakeholder3.equals("pharmacist") ? pharmacistReference : patientReference);

                      newMetReq.setRequirement(requirement2);
                      newMetReq.setCompleted(false);
                      newMetReq.setFunctionalId(reqStakeholderReference3);
                      newMetReq.addRemsRequest(remsRequest);
                      remsRequest.addMetRequirement(newMetReq);
                      metRequirementsRepository.save(newMetReq);

                    }
                    // iterate through all other child reqs to create corresponding false metReqs / assign to existing 
                    for (Requirement subRequirement2 : requirement2.getChildRequirements()) {
                      Boolean foundSubMatch2 = false;
                      // check if sub req has already been submitted 
                      for (MetRequirement possibleMetSubRequirement2 : subRequirement2.getMetRequirements()) {
                        if ((possibleMetSubRequirement2.getFunctionalId().equals(pharmacistReference) && subRequirement2.getStakeholder().equals("pharmacist"))
                        || (possibleMetSubRequirement2.getFunctionalId().equals(practitionerReference) && subRequirement2.getStakeholder().equals("prescriber"))
                        || (possibleMetSubRequirement2.getFunctionalId().equals(patientReference) && subRequirement2.getStakeholder().equals("patient"))) {
                          
                          foundSubMatch2 = true;
                          possibleMetSubRequirement2.setParentMetRequirement(newMetReq);
                          if (!foundMatch) {
                            newMetReq.addChildMetRequirements(possibleMetSubRequirement2);
                          }
                          metRequirementsRepository.save(possibleMetSubRequirement2);
                          break;
                        }
                      }

                      // create a new met req if one is not found, set completed status to false 
                      if (!foundSubMatch2) {
                        // figure out which stakeholder the req corresponds to 
                        String reqStakeholder4 = subRequirement2.getStakeholder();
                        String reqStakeholderReference4 = reqStakeholder4.equals("prescriber") ? practitionerReference : (reqStakeholder4.equals("pharmacist") ? pharmacistReference : patientReference);

                        MetRequirement falseSubMetReq2 = new MetRequirement();
                        falseSubMetReq2.setRequirement(subRequirement2);
                        falseSubMetReq2.setFunctionalId(reqStakeholderReference4);
                        falseSubMetReq2.setCompleted(false);
                        falseSubMetReq2.setParentMetRequirement(newMetReq);
                        newMetReq.addChildMetRequirements(falseSubMetReq2);
                        metRequirementsRepository.save(falseSubMetReq2);
                      }
                    }
                    metRequirementsRepository.save(newMetReq);
                  }
                }
                remsRepository.save(remsRequest);


              } else {
                // look through open met Reqs for matching cases and set status to true 
                Boolean foundMetReq3 = false;
                for (MetRequirement possibleMetRequirement3 : requirement.getMetRequirements()) {
                  if ((possibleMetRequirement3.getFunctionalId().equals(pharmacistReference) && requirement.getStakeholder().equals("pharmacist"))
                  || (possibleMetRequirement3.getFunctionalId().equals(practitionerReference) && requirement.getStakeholder().equals("prescriber"))
                  || (possibleMetRequirement3.getFunctionalId().equals(patientReference) && requirement.getStakeholder().equals("patient"))) {
                    
                    foundMetReq3 = true;
                    possibleMetRequirement3.setCompleted(true);
                    // possibleMetRequirement3.setCompletedRequirement(remsObject);
                    metRequirementsRepository.save(possibleMetRequirement3);
                    returnMetReq = possibleMetRequirement3;
                  }
                }

                if (!foundMetReq3) {
                  // figure out which stakeholder the req corresponds to 
                  String reqStakeholder5 = requirement.getStakeholder();
                  String reqStakeholderReference5 = reqStakeholder5.equals("prescriber") ? practitionerReference : (reqStakeholder5.equals("pharmacist") ? pharmacistReference : patientReference);

                  MetRequirement submittedMetReq = new MetRequirement();
                  submittedMetReq.setRequirement(requirement);
                  submittedMetReq.setCompleted(true);
                  // submittedMetReq.setCompletedRequirement(remsObject);
                  submittedMetReq.setFunctionalId(reqStakeholderReference5);
                  metRequirementsRepository.save(submittedMetReq);

                  for (Requirement childRequirement2 : requirement.getChildRequirements()) {
                    Boolean foundMetReq6 = false;
                    for (MetRequirement possibleMetRequirement6 : childRequirement2.getMetRequirements()) {
                      if ((possibleMetRequirement6.getFunctionalId().equals(pharmacistReference) && childRequirement2.getStakeholder().equals("pharmacist"))
                      || (possibleMetRequirement6.getFunctionalId().equals(practitionerReference) && childRequirement2.getStakeholder().equals("prescriber"))
                      || (possibleMetRequirement6.getFunctionalId().equals(patientReference) && childRequirement2.getStakeholder().equals("patient"))) {
                        
                        foundMetReq6 = true;
                        possibleMetRequirement6.setParentMetRequirement(submittedMetReq);
                        submittedMetReq.addChildMetRequirements(possibleMetRequirement6);
                        metRequirementsRepository.save(possibleMetRequirement6);
                      }
                    }
    
                    if (!foundMetReq6) {
                      String reqStakeholder6 = childRequirement2.getStakeholder();
                      String reqStakeholderReference6 = reqStakeholder6.equals("prescriber") ? practitionerReference : (reqStakeholder6.equals("pharmacist") ? pharmacistReference : patientReference);
    
                      MetRequirement submittedMetReqChild = new MetRequirement();
                      submittedMetReqChild.setRequirement(childRequirement2);
                      submittedMetReqChild.setCompleted(false);
                      submittedMetReqChild.setFunctionalId(reqStakeholderReference6);
                      submittedMetReqChild.setParentMetRequirement(submittedMetReq);
                      submittedMetReq.addChildMetRequirements(submittedMetReqChild);
                      metRequirementsRepository.save(submittedMetReqChild);
                    }
                  }
                  metRequirementsRepository.save(submittedMetReq);
                  returnMetReq = submittedMetReq;
                }


              }

              break;

            }

            // check sub requirements - note sub reqs can not initiate a new rems request, only a parent level requirement can start a new rems case
            // only handle one level of sub requirements
            for (Requirement subRequirement : requirement.getChildRequirements()) {
              Boolean foundMetReq4 = false;
              for (MetRequirement possibleMetRequirement4 : subRequirement.getMetRequirements()) {
                if ((possibleMetRequirement4.getFunctionalId().equals(pharmacistReference) && requirement.getStakeholder().equals("pharmacist"))
                || (possibleMetRequirement4.getFunctionalId().equals(practitionerReference) && requirement.getStakeholder().equals("prescriber"))
                || (possibleMetRequirement4.getFunctionalId().equals(patientReference) && requirement.getStakeholder().equals("patient"))) {
                  
                  foundMetReq4 = true;
                  possibleMetRequirement4.setCompleted(true);
                  // possibleMetRequirement4.setCompletedRequirement(remsObject);
                  metRequirementsRepository.save(possibleMetRequirement4);
                  returnMetReq = possibleMetRequirement4;
                }
              }

              if (!foundMetReq4) {
                // figure out which stakeholder the req corresponds to 
                String reqStakeholder6 = subRequirement.getStakeholder();
                String reqStakeholderReference6 = reqStakeholder6.equals("prescriber") ? practitionerReference : (reqStakeholder6.equals("pharmacist") ? pharmacistReference : patientReference);

                MetRequirement submittedMetReq4 = new MetRequirement();
                submittedMetReq4.setRequirement(subRequirement);
                submittedMetReq4.setCompleted(true);
                // submittedMetReq4.setCompletedRequirement(remsObject);
                submittedMetReq4.setFunctionalId(reqStakeholderReference6);
                metRequirementsRepository.save(submittedMetReq4);
                returnMetReq = submittedMetReq4;
              }
                // MetRequirement subMetReq = new MetRequirement();
                // subMetReq.setRequirement(subRequirement);
                // // subMetReq.setRemsRequest(remsRequest);
                // subMetReq.setParentMetRequirement(metReq);
                // // remsRequest.addMetRequirement(subMetReq);
                // metReq.addChildMetRequirements(subMetReq);
                // metRequirementsRepository.save(subMetReq);
            }
        }

            // check status of each rems request once requirements are parsed through
            // could not set rems - drug relationship without java bean issue, for now iterate through all rems requests 
            // ToDo : Assign relationship and only iterate through the rems requests associated with the current drug instead of all rems requests for all drug
            // Optimization: filter on only Pending rems requests (custom SQL query would need to be created in the repository most likely)
            List<Rems> rems = remsRepository.findRems();
            for (Rems rem : rems) {
              if (rem.getStatus().equals("Pending")) {
                Boolean foundFalse = false;
                for (MetRequirement finalMetReq : rem.getMetRequirements()) {
                  if (!finalMetReq.getCompleted()) {
                    foundFalse = true; 
                    break;
                  }
  
                  for (MetRequirement finalSubMetReq : finalMetReq.getChildMetRequirements()) {
                    if (!finalSubMetReq.getCompleted()) {
                      foundFalse = true; 
                      break;
                    }
                  }
                  if (foundFalse) {
                    break;
                  }
                }
                
                if (!foundFalse) {
                  rem.setStatus("Approved");
                  remsRepository.save(rem);
                  if (rem.getCase_number().equals(remsRequest.getCase_number())) {
                    remsRequest.setStatus("Approved");
                  }
                }
              }
            }
        
        // return MetReq unless a new case is created in which case return the Rems request
        if (returnRemsRequest) {
          return ResponseEntity.ok().body(remsRequest);
        } else  {
          return ResponseEntity.ok().body(returnMetReq);
        }
      }
    
      @CrossOrigin
      @GetMapping("/rems/{id}")
      public ResponseEntity<Object> getRems(@PathVariable String id) {
        Rems rems = remsRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, id + " not found"));
        return ResponseEntity.ok().body(rems);
      }

      public JsonNode getResource(JsonNode bundle, String resourceReference) {
        String[] temp = resourceReference.split("/");
        String _resourceType = temp[0];
        String _id = temp[1];
      
        for (int i = 0; i < bundle.get("entry").size(); i++) {
          if ((bundle.get("entry").get(i).get("resource").get("resourceType").textValue().equals(_resourceType))
            && (bundle.get("entry").get(i).get("resource").get("id").textValue().equals(_id))) {
            return bundle.get("entry").get(i).get("resource");
          }
        }
        return null;
      }

      public JsonNode getQuestionnaireResponse(JsonNode bundle) {
        String _resourceType = "QuestionnaireResponse";
      
        for (int i = 0; i < bundle.get("entry").size(); i++) {
          if ((bundle.get("entry").get(i).get("resource").get("resourceType").textValue().equals(_resourceType))) {
            return bundle.get("entry").get(i).get("resource");
          }
        }
        return null;
      }
    
}