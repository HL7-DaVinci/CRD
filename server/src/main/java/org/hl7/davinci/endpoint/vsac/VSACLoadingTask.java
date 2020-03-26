package org.hl7.davinci.endpoint.vsac;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.hl7.davinci.endpoint.vsac.errors.VSACException;

public class VSACLoadingTask {

  public static void main(String[] args) {
    System.out.println(System.getProperty("log4j.rootLogger"));
    
    System.out.println("VSAC ValueSet Loader Starting");
    Logger.getLogger("org.apache.http").setLevel(Level.INFO);

    String username = System.getenv("VSAC_USERNAME");
    String password = System.getenv("VSAC_PASSWORD");

    if (username == null || password == null) {
      System.err.println("VSAC_USERNAME and/or VSAC_PASSWORD not found in environment variables.");
      System.exit(1);
    }

    try {
      VSACLoader vsacLoader = new VSACLoader(username, password);
      System.out.println(vsacLoader.getTGT());
      System.out.println(vsacLoader.getTicket());
      System.out.println(vsacLoader.getValueSet("2.16.840.1.113762.1.4.1114.7"));
    } catch(VSACException ve) {
      ve.printStackTrace();
      System.exit(1);
    }
  }
}

