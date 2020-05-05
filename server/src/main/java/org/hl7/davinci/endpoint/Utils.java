package org.hl7.davinci.endpoint;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import com.google.common.net.HttpHeaders;

public class Utils {

  public static URL getApplicationBaseUrl(HttpServletRequest request) {
    private static Logger logger = Logger.getLogger(Application.class.getName());
    try {
      // grab the forwarded values if they are not null
      String serverName = ((request.getHeader(HttpHeaders.X_FORWARDED_HOST) != null) ? request.getHeader(HttpHeaders.X_FORWARDED_HOST) : request.getServerName());
      String scheme = ((request.getHeader(HttpHeaders.X_FORWARDED_PROTO) != null) ? request.getHeader(HttpHeaders.X_FORWARDED_PROTO) : request.getScheme());
      int port = ((request.getHeader(HttpHeaders.X_FORWARDED_PORT) != null) ? Integer.parseInt(request.getHeader(HttpHeaders.X_FORWARDED_PORT)) : request.getServerPort());
      // int port = request.getServerPort();
      logger.info("----> TEST: port: " + port + ", " + request.getHeader(HttpHeaders.X_FORWARDED_PORT) + ", " + request.getServerPort());
      URL url = null;

      // grab the last forwarded url
      String[] serverParts = serverName.split(", ");
      serverName = serverParts[serverParts.length - 1];

      // set the port if not the default http port (80)
      if (port != 80) {
        url = new URL(scheme, serverName, port, request.getContextPath());
      } else {
        url = new URL(scheme, serverName, request.getContextPath());
      }

      return url;
    } catch (MalformedURLException e) {
      throw new RuntimeException("Unable to get current server URL");
    }
  }

}
