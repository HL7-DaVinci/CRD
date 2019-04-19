package org.hl7.davinci.ehrserver.requestgenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller()
public class RequestGeneratorController {
  static final Logger logger = LoggerFactory.getLogger(RequestGeneratorController.class);


  @RequestMapping(value = {"/"}, method = RequestMethod.GET)
  public String index() {
    logger.info("RequestGenerator page requested");
    return "index";
  }

}
