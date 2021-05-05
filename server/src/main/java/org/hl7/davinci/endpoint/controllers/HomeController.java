package org.hl7.davinci.endpoint.controllers;

import javax.servlet.http.HttpServletRequest;
import org.hl7.davinci.endpoint.config.YamlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


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
  private YamlConfig config;

  @RequestMapping("/")
  public String index(Model model, final HttpServletRequest request) {
    logger.info("HomeController::index(): /");
    model.addAttribute("contextPath", request.getContextPath());
    model.addAttribute("hostOrg", config.getHostOrg());
    return "index";
  }

  @GetMapping("/data")
  public String data(Model model, final HttpServletRequest request) {
    logger.info("HomeController::data(): /data");
    model.addAttribute("contextPath", request.getContextPath());
    request.getContextPath();
    return "index";
  }

  @GetMapping("/fhirview")
  public String fhirview(Model model, final HttpServletRequest request) {
    logger.info("HomeController::fhirview(): /fhirview");
    model.addAttribute("contextPath", request.getContextPath());
    return "index";
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


  @GetMapping("/launch")
  public String smartlaunch(Model model, final HttpServletRequest request) {
    model.addAttribute("contextPath", request.getContextPath());
    return "index";
  }

  @GetMapping("/index")
  public String smartIndex(Model model, final HttpServletRequest request) {
    model.addAttribute("contextPath", request.getContextPath());
    return "index";
  }

}
