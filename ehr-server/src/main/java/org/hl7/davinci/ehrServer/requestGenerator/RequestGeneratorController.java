package org.hl7.davinci.ehrServer.requestGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;


@Controller()
public class RequestGeneratorController {
  static final Logger logger = LoggerFactory.getLogger(RequestGeneratorController.class);

  @RequestMapping(value= {"/", "/reqgen", "/index", "/index.html"}, method=RequestMethod.GET)
  public String index() {
    logger.info("RequestGenerator page requested");
    return "index";
  }

}
