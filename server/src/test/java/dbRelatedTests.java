//import fhir.restful.database.Datum;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.sql.*;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO: implement some tests using the spring defined db (use jpa)


//class dbRelatedTests {

  //    private static String TEST_DB_NAME = "dme_ephemeral";
//    private static String DB_SERVER_URL = "localhost";
//
//
//
//    /**
//     * You can pass in a resource name (i.e. a file in the resources folder) and get the absolute path
//     * @param resourceFileName
//     * @return the absolute path of the resource file
//     */
//    String getPathOfResource(String resourceFileName) {
//        URL resource = this.getClass().getResource(resourceFileName);
//        try {
//            Path a = Paths.get(resource.toURI()).toAbsolutePath();
//            return a.toString();
//
//        }
//        catch(URISyntaxException e){
//            return null;
//        }
//    }
//
//
//    /**
//     * Drop and recreate the testing db (dme_ephemeral)
//     * @throws SQLException
//     */
//    void resetTestingDb() throws SQLException{
//        Connection master_connection = DriverManager.getConnection("jdbc:postgresql://"+DB_SERVER_URL+"/postgres");
//        PreparedStatement st = master_connection.prepareStatement(
//                "SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = '"+TEST_DB_NAME+"' AND pid <> pg_backend_pid();" +
//                        "DROP DATABASE IF EXISTS "+TEST_DB_NAME+"; CREATE DATABASE "+TEST_DB_NAME+";");
//        st.execute();
//        st.close();
//    }
//
//    /**
//     * Drops and creates the cdr table, and populates it with a csv
//     * @param csv_filename The csv used to populate the csv table
//     * @throws SQLException
//     */
//    void setCRDtableToCsv(String csv_filename) throws SQLException {
//        String csv_path = getPathOfResource(csv_filename);
//        Connection connection = DriverManager.getConnection("jdbc:postgresql://"+DB_SERVER_URL+"/"+TEST_DB_NAME);
//        PreparedStatement st = connection.prepareStatement(
//                "DROP TABLE IF EXISTS cdr;"+
//                        "CREATE TABLE crd (patient_age_range_low smallint null, patient_age_range_high smallint null, patient_gender char(1) null, patient_plan_id varchar(20), equipment_code varchar(20), no_auth_needed bool, info_link varchar(1000));" +
//                        "COPY crd FROM '"+csv_path+"' DELIMITER ',' NULL AS 'null' CSV HEADER;");
//        st.execute();
//        st.close();
//    }
//
//
//    @Test
//    @DisplayName("Test that the database queries return as expected based on different queries and different data")
//    void testDataBaseQueries() throws SQLException {
//        //reset and populate db
//        resetTestingDb();
//        setCRDtableToCsv("crd_table_basic.csv");
//
//        Connection connection = DriverManager.getConnection("jdbc:postgresql://"+DB_SERVER_URL+"/"+TEST_DB_NAME);
//        DbQueries dbq = new DbQueries(connection);
//
//
//        //make patient info and query db
//        int patient_age = 40;
//        String patient_gender = "M";
//        String patient_plan_id = "12345";
//        String equipment_code = "E0601";
//
//        DbResponse r = dbq.getInfo(patient_age, patient_gender, patient_plan_id, equipment_code);
//        assertEquals("http://images.mentalfloss.com/sites/default/files/styles/mf_image_16x9/public/istock-511366776.jpg",r.getInfoLink());
//        assertEquals(false,r.getNoAuthNeeded());
//
//        //change patient info and query db
//        patient_age = 55;
//        r = dbq.getInfo(patient_age, patient_gender, patient_plan_id, equipment_code);
//        assertEquals("https://cdn2.vectorstock.com/i/1000x1000/44/21/old-fashioned-dancing-penguin-in-comic-hat-vector-2664421.jpg",r.getInfoLink());
//        assertEquals(true,r.getNoAuthNeeded());
//    }





//}
