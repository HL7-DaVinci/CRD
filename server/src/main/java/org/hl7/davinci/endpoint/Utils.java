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
      URL url = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath());

      logger.info("test: scheme   : " + request.getHeader(HttpHeaders.X_FORWARDED_PROTO) + ", " + request.getScheme());
      logger.info("test: host     : " + request.getHeader(HttpHeaders.HOST) + ", " + request.getServerName() + ", " + request.getHeader(HttpHeaders.X_FORWARDED_HOST));
      logger.info("test: port     : " + request.getServerPort());
      logger.info("test: forwarded: " + request.getHeader(HttpHeaders.FORWARDED));

      return url;
    } catch (MalformedURLException e) {
      throw new RuntimeException("Unable to get current server URL");
    }
  }

}
