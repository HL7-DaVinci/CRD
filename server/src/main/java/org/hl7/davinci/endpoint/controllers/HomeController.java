package org.hl7.davinci.endpoint.controllers;

import javax.servlet.http.HttpServletRequest;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.database.DataService;
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
import org.springframework.web.servlet.HandlerMapping;
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
  @Autowired
  private YamlConfig config;

  @RequestMapping("/")
  public String index(Model model, final HttpServletRequest request) {
    model.addAttribute("contextPath", request.getContextPath());
    model.addAttribute("hostOrg", config.getHostOrg());
    return "index";
  }

  @GetMapping("/data")
  public String data(Model model, final HttpServletRequest request) {
    model.addAttribute("contextPath", request.getContextPath());
    request.getContextPath();
    return "index";
  }

  /**
   * Accepts form submissions to create new entries in the database, then redirects to the
   * data page to keep the user on the same page and show the change to the database.
   * @param datum the data object to be added to the database
   * @param errors any errors incurred from the form submission
   * @return an object that contains the model and view of the data page
   */
  @PostMapping("/data")
  public ModelAndView saveDatum(@ModelAttribute CoverageRequirementRule datum,
      BindingResult errors) {


    if (errors.hasErrors()) {
      logger.warn("There was a error " + errors);
    }
    dataService.create(datum);

    return new ModelAndView("redirect:index");
  }

  @GetMapping("/public")
  public String public_key(Model model, final HttpServletRequest request) {
    model.addAttribute("contextPath", request.getContextPath());
    return "index";
  }

  @GetMapping("/requests")
  public String request_log(Model model, final HttpServletRequest request) {
    model.addAttribute("contextPath", request.getContextPath());
    return "index";
  }




}
