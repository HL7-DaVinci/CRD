import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbQueries {

  private Connection connection;


  DbQueries(Connection connection) {
    this.connection = connection;
  }


  /**
   * Generates the response from the database when given patient info.
   * @param patientAge age of the patient
   * @param patientGender gender of the patient
   * @param patientPlanId care plan ID of the patient
   * @param equipmentCode code of the DME
   * @return the response from the database
   * @throws SQLException if the query is invalid
   */
  public DbResponse getInfo(int patientAge, String patientGender,
                            String patientPlanId, String equipmentCode
  ) throws SQLException {

    PreparedStatement st = connection.prepareStatement(
        "SELECT no_auth_needed, info_link FROM crd WHERE "
            + "(patient_age_range_low <= ? or patient_age_range_low is null) "
            + "and (patient_age_range_high >= ? or patient_age_range_high is null) "
            + "and (patient_gender = ? or patient_gender is null) "
            + "and (patient_plan_id = ?) "
            + "and (equipment_code = ?) "
            + "LIMIT 1");  //keep it to one row for now - could be changed in the future

    st.setInt(1, patientAge);
    st.setInt(2, patientAge);
    st.setString(3, patientGender);
    st.setString(4, patientPlanId);
    st.setString(5, equipmentCode);
    ResultSet rs = st.executeQuery();

    DbResponse r = null;
    if (rs.next()) {
      Boolean noAuthNeeded = rs.getBoolean("no_auth_needed");
      String infoLink = rs.getString("info_link");
      r = new DbResponse(infoLink, noAuthNeeded);
    } else {
      //no results
    }
    rs.close();
    st.close();

    return r;


  }

}
