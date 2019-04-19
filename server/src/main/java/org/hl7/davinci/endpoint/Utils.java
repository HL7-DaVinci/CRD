package org.hl7.davinci.endpoint;

import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;

public class Utils {

  public static URL getApplicationBaseUrl(HttpServletRequest request) {
    try {
      URL url = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath());
      return url;
    } catch (MalformedURLException e) {
      throw new RuntimeException("Unable to get current server URL");
    }
  }

}
