import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.JsonObject;
import fhir.restful.Application;
import fhir.restful.database.Datum;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=Application.class)
@AutoConfigureMockMvc
public class serverTest {

  @Autowired
  private MockMvc mockMvc;

  public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsBytes(object);
  }

  public static Datum makeTestDatum(){
    Datum retVal = new Datum();
    retVal.setInfoLink("test.com");
    retVal.setEquipmentCode("a");
    retVal.setNoAuthNeeded("ad");
    retVal.setPatientAgeRangeHigh("42");
    retVal.setPatientAgeRangeLow("hello");
    retVal.setPatientGender("M");
    retVal.setPatientPlanId("hoop");
    return retVal;
  }

  @Test
  public void checkHomePage() throws Exception {
    this.mockMvc.perform(get("/")).andExpect(status().isOk());
  }

  @Test
  public void checkGetAll() throws Exception {
    this.mockMvc.perform(get("/api/data"))
        .andExpect(status()
            .isOk())
        .andExpect(content().string(containsString("patientGender")));
  }

  @Test
  public void checkGetOne() throws Exception {
    this.mockMvc.perform(get("/api/data/1"))
        .andExpect(status()
            .isOk())
        .andExpect(content().string(containsString("https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PAP_DocCvg_Factsheet_ICN905064.pdf")))
        .andExpect(content().string(not(containsString("https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PMDFactSheet07_Quark19.pdf"))));
  }

  @Test
  public void checkPost() throws Exception {
    MvcResult result = this.mockMvc.perform(post("/api/data").contentType(MediaType.APPLICATION_JSON)
        .content(convertObjectToJsonBytes(makeTestDatum())))
        .andExpect(status().isCreated())
        .andReturn();
    this.mockMvc.perform(get(result.getResponse().getRedirectedUrl()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("test.com")));
  }

  @Test
  public void checkPut() throws Exception {
    this.mockMvc.perform(put("/api/data/1").contentType(MediaType.APPLICATION_JSON)
        .content(convertObjectToJsonBytes(makeTestDatum())))
        .andExpect(status().isNoContent());
    this.mockMvc.perform(get("/api/data/1"))
        .andExpect(content().string(containsString("test.com")));
  }

  @Test
  public void checkDelete() throws Exception {
    this.mockMvc.perform(delete("/api/data/5"))
        .andExpect(status().isOk());
    this.mockMvc.perform(get("/api/data/5"))
        .andExpect(status()
            .isNotFound());
  }

  @Test
  public void checkNotFound() throws Exception {

    this.mockMvc.perform(get("/api/data/77777777"))
        .andExpect(status()
            .isNotFound());
  }

  @Test
  public void checkNotExist() throws Exception {
    this.mockMvc.perform(get("/api/data/seven"))
        .andExpect(status()
            .isBadRequest());
  }



}