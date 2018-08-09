package endpoint.controllers;

import endpoint.cdshooks.models.CdsResponse;
import endpoint.cdshooks.services.crd.CrdCdsRequest;
import endpoint.cdshooks.services.crd.CrdCdsService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CdsHooksController {
  static final String URL_BASE = "/cds-services/";

  @Autowired private CrdCdsService crdCdsService;

  static final Class clz = CrdCdsRequest.class;

  @PostMapping(value = URL_BASE + CrdCdsService.ID)
  public CdsResponse handleRequest(@Valid @RequestBody CrdCdsRequest request) {
    return crdCdsService.handleRequest(request);
  }
}
