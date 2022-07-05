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

    public void updateRemsRequestStatus(String uid) {
        try {
          TimeUnit.SECONDS.sleep(30);
        }
        catch(Exception e)
        {
            System.out.println(e);
          }
        Rems rems = remsRepository.findById(uid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, uid + " not found"));
        rems.setStatus("Approved");
        remsRepository.save(rems);
      }
    
      public void updateRemsRequestStatusInBackground (final String uid) {
        Thread t = new Thread(() -> updateRemsRequestStatus(uid));
        t.start();
      }
    
      @PostMapping(value = "/rems")
      @CrossOrigin
      public ResponseEntity<Object> postRems(@RequestBody String jsonData) {
        JsonNode remsObject = JacksonUtil.toJsonNode(jsonData);
        String id = UUID.randomUUID().toString().replace("-", "");

        JsonNode params = getResource(remsObject, remsObject.get("entry").get(0).get("resource").get("focus").get("parameters").get("reference").textValue());
        
        String prescriptionReference = "";
        for (JsonNode param : params.get("parameter")) {
            if (param.get("name").textValue().equals("prescription")) {
                prescriptionReference = param.get("reference").textValue();
            }
        }

        JsonNode presciption = getResource(remsObject, prescriptionReference);
        String prescriptionSystem = presciption.get("medicationCodeableConcept").get("coding").get(0).get("system").textValue();
        String prescriptionCode = presciption.get("medicationCodeableConcept").get("coding").get(0).get("code").textValue();
        Drug drug = drugsRepository.findDrugByCode(prescriptionSystem, prescriptionCode).get(0);



        Rems remsRequest = new Rems();
        remsRequest.setCase_number(id);
        remsRequest.setStatus("Pending");
        remsRequest.setResource(remsObject);
        remsRepository.save(remsRequest);

        for (Requirement requirement : drug.getRequirements()) {
            MetRequirement metReq = new MetRequirement();
            metReq.setRequirement(requirement);
            metReq.setRemsRequest(remsRequest);
            remsRequest.addMetRequirement(metReq);
            metRequirementsRepository.save(metReq);

            for (Requirement subRequirement : requirement.getChildRequirements()) {
              MetRequirement subMetReq = new MetRequirement();
              subMetReq.setRequirement(subRequirement);
              subMetReq.setRemsRequest(remsRequest);
              subMetReq.setParentMetRequirement(metReq);
              remsRequest.addMetRequirement(subMetReq);
              metRequirementsRepository.save(subMetReq);
            }
        }
        remsRepository.save(remsRequest);
        updateRemsRequestStatusInBackground(id);
        return ResponseEntity.ok().body(remsRequest);
    
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
    
}