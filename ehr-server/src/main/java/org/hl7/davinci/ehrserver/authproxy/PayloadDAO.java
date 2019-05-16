package org.hl7.davinci.ehrserver.authproxy;

import org.hl7.davinci.ehrserver.requestgenerator.database.Key;

public interface PayloadDAO {

  public void createPayload(Payload payload);


  // create a new user record in the users table
//  public void create(String launchId, String launchUrl, String patientId, String template, String request);

  // get a user with the passed id
  public Payload getPayload(String id);


  // update launch id to be a code
  public void updateCode(String launchId, String code);

  // get the context
  public Payload findContextByCode(String code);

  public void updateRedirect(String launchId, String redirectUri);
}
