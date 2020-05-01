package org.hl7.davinci.endpoint;


import com.google.common.net.HttpHeaders;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

public class Utils {
  private static Logger logger = Logger.getLogger(Application.class.getName());

  public static URL getApplicationBaseUrl(HttpServletRequest request) {
    try {
      URL url = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath());

      return url;
    } catch (MalformedURLException e) {
      throw new RuntimeException("Unable to get current server URL");
    }
  }

  public static URL getApplicationBaseUrlTest(HttpServletRequest request) {
    try {

      String serverName = ((request.getHeader(HttpHeaders.X_FORWARDED_HOST) != null) ? request.getHeader(HttpHeaders.X_FORWARDED_HOST) : request.getServerName());
      String scheme = ((request.getHeader(HttpHeaders.X_FORWARDED_PROTO) != null) ? request.getHeader(HttpHeaders.X_FORWARDED_PROTO) : request.getScheme());
      int port = request.getServerPort();
      URL url = null;

      // grab the last forwarded url
      String[] serverParts = serverName.split(", ");
      logger.info("test: num parts: " + serverParts.length + " full: " + serverName);
      serverName = serverParts[serverParts.length - 1];

      if (port != 80) {
        url = new URL(scheme, serverName, port, request.getContextPath());
      } else {
        url = new URL(scheme, serverName, request.getContextPath());
      }
      URL oldUrl = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath());
      //URL url = new URL(scheme, serverName, request.getContextPath());

      logger.info("test: scheme   : " + request.getHeader(HttpHeaders.X_FORWARDED_PROTO) + ", " + request.getScheme() + " --> " + scheme);
      logger.info("test: host     : " + request.getHeader(HttpHeaders.HOST) + ", " + request.getServerName() + ", " + request.getHeader(HttpHeaders.X_FORWARDED_HOST) + " --> " + serverName);
      logger.info("test: port     : " + request.getServerPort());
      logger.info("test: forwarded: " + request.getHeader(HttpHeaders.FORWARDED));
      logger.info("test: context p: " + request.getContextPath());
      logger.info("test: ---------------------------------------");
      logger.info("test: Old URL  : " + oldUrl.toString());
      logger.info("test: URL      : " + url.toString());

      return url;
    } catch (MalformedURLException e) {
      throw new RuntimeException("Unable to get current server URL");
    }
  }

}
