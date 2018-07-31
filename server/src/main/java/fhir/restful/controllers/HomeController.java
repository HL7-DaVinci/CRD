package fhir.restful.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import fhir.restful.database.DataService;
import fhir.restful.database.Datum;

import java.util.Calendar;
import java.util.List;

import org.hl7.davinci.CrdRequestCreator;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.EligibilityRequest;
import org.hl7.fhir.r4.model.EligibilityResponse;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Parameters;

import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


/**
 * Defines pages by searching for returned string in the resources/templates directory.
 * Making changes here will alter the home page.
 * The "Model" parameter can be given attributes which can be referenced in the html
 * Thymeleaf provides the ability to reference and use the attributes.
 */
@Controller
public class HomeController {
  static final Logger logger = LoggerFactory.getLogger(HomeController.class);


  @Autowired
  private DataService dataService;

  /**
   * Maps the home page (the base url) and gives the model some attributes.
   * @param model an object that describes the structure and variables available for some page
   * @return a string which maps the page to some html file in the /templates directory
   */
  @RequestMapping("/")
  public String index(Model model) {
    List<Datum> data = dataService.findAll();
    model.addAttribute("allPosts", data);
    return "index";
  }

  @RequestMapping("/login")
  public String login(Model model) {
    return "login";
  }

  /**
   * Maps the database to a specific page, hooks up the model with relevant information.
   * @param model an object with attributes that can be accessed and put on the html page
   * @return a string which corresponds with the html file to be displayed
   */
  @GetMapping("/data")
  public String data(Model model) {
    List<Datum> foo = dataService.findAll();
    model.addAttribute("dataEntries", foo);
    List<String> bar = Datum.getFields();
    model.addAttribute("headers", bar);
    model.addAttribute("datum", new Datum());

    return "data";
  }

  /**
   * Accepts form submissions to create new entries in the database, then redirects to the
   * data page to keep the user on the same page and show the change to the database.
   * @param datum the data object to be added to the database
   * @param errors any errors incurred from the form submission
   * @return an object that contains the model and view of the data page
   */
  @PostMapping("/data")
  public ModelAndView saveDatum(@ModelAttribute Datum datum, BindingResult errors) {


    if (errors.hasErrors()) {
      logger.warn("There was a error " + errors);
    }
    dataService.create(datum);

    return new ModelAndView("redirect:data");
  }


}
