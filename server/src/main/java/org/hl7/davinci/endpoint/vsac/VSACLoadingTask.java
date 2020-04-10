package org.hl7.davinci.endpoint.vsac;

import java.lang.System.Logger;

import org.hl7.davinci.endpoint.vsac.errors.VSACException;
//import org.slf4j.LoggerFactory;
//import org.slf4j.event.Level;


import org.apache.commons.logging.LogFactory;

public class VSACLoadingTask {

  public static void quietLogging() {
    String[] loggers = {
      "org.apache.http",
      "org.apache.http.wire",
      "org.apache.http.headers",
      "org.apache.http.wire.content",
      "ca.uhn.fhir.context"
    };
    for (String ln : loggers) {
      // Try java.util.logging as backend
      java.util.logging.Logger.getLogger(ln).setLevel(java.util.logging.Level.WARNING);

      // Try another backend
      ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ln);
      logger.setLevel(ch.qos.logback.classic.Level.WARN);
    }
  }

  public static void main(String[] args) {
    quietLogging();

    System.out.println("VSAC ValueSet Loader Starting");

    String username = System.getenv("VSAC_USERNAME");
    String password = System.getenv("VSAC_PASSWORD");

    if (username == null || password == null) {
      System.err.println("VSAC_USERNAME and/or VSAC_PASSWORD not found in environment variables.");
      System.exit(1);
    }

    try {
      VSACLoader vsacLoader = new VSACLoader(username, password);
      System.out.println(vsacLoader.getTGT());
      //System.out.println(vsacLoader.getTicket());
      System.out.println(vsacLoader.getValueSetJSON("2.16.840.1.113762.1.4.1114.7"));
      System.out.println(vsacLoader.getValueSetJSON("2.16.840.1.113762.1.4.1219.3"));
    } catch(VSACException ve) {
      ve.printStackTrace();
      System.exit(1);
    }
  }
}

