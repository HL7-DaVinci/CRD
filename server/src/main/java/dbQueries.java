import java.sql.*;


public class dbQueries {

    private Connection connection;



    dbQueries(Connection connection) {
        this.connection = connection;
    }


    public dbResponse getInfo(int patientAge, String patientGender, String patientPlanId, String equipmentCode
        ) throws SQLException {

        PreparedStatement st = connection.prepareStatement(
                "SELECT no_auth_needed, info_link FROM crd WHERE " +
                        "(patient_age_range_low <= ? or patient_age_range_low is null) " +
                        "and (patient_age_range_high >= ? or patient_age_range_high is null) " +
                        "and (patient_gender = ? or patient_gender is null) " +
                        "and (patient_plan_id = ?) " +
                        "and (equipment_code = ?) " +
                        "LIMIT 1");  //keep it to one row for now - could be changed in the future

        st.setInt(1, patientAge);
        st.setInt(2, patientAge);
        st.setString(3, patientGender);
        st.setString(4, patientPlanId);
        st.setString(5, equipmentCode);
        ResultSet rs = st.executeQuery();

        dbResponse r = null;
        if (rs.next())
        {
            Boolean noAuthNeeded = rs.getBoolean("no_auth_needed");
            String infoLink = rs.getString("info_link");
            r = new dbResponse(infoLink, noAuthNeeded);
        } else {
            //no results
        }
        rs.close();
        st.close();

        return r;



    }

}
