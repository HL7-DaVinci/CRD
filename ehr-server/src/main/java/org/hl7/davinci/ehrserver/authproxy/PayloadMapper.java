package org.hl7.davinci.ehrserver.authproxy;

import org.hl7.davinci.ehrserver.requestgenerator.database.Key;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PayloadMapper implements RowMapper<Payload> {

  @Override
  public Payload mapRow(ResultSet rs, int rowNum) throws SQLException {
    Payload payload = new Payload();
    payload.setLaunchId(rs.getString("launchId"));
    payload.setLaunchUrl(rs.getString("launchUrl"));
    payload.setRedirectUri(rs.getString("redirectUri"));
    Parameters parameters = new Parameters();
    parameters.setPatientId(rs.getString("patientId"));
    AppContext appContext = new AppContext();
    appContext.setTemplate(rs.getString("template"));
    appContext.setRequest(rs.getString("request"));
    appContext.setFilepath(rs.getString("filepath"));
    parameters.setAppContext(appContext.toString());
    payload.setParameters(parameters);
    return payload;
  }
}
