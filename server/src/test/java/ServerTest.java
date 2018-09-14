import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import java.io.IOException;

import org.hl7.davinci.endpoint.database.DataRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


/**
 * NOTE: Currently you should manually run "gradle setupDb" before running these tests.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class ServerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private DataRepository repository;

  private String token;

  public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsBytes(object);
  }

  @Before
  public void setUp() {
    CoverageRequirementRule retVal = new CoverageRequirementRule();
    retVal.setInfoLink("https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PMDFactSheet07_Quark19.pdf");
    retVal.setEquipmentCode("abc123");
    retVal.setCodeSystem("https://bluebutton.cms.gov/resources/codesystem/hcpcs");
    retVal.setNoAuthNeeded(true);
    retVal.setAgeRangeHigh(42);
    retVal.setAgeRangeLow(0);
    retVal.setGenderCode('M');
    this.repository.save(retVal);
  }

  /**
   * Build the test data.
   */
  public static CoverageRequirementRule makeTestDatum() {
    CoverageRequirementRule retVal = new CoverageRequirementRule();
    retVal.setInfoLink("test.com");
    retVal.setEquipmentCode("abc123");
    retVal.setCodeSystem("https://bluebutton.cms.gov/resources/codesystem/hcpcs");
    retVal.setNoAuthNeeded(true);
    retVal.setAgeRangeHigh(42);
    retVal.setAgeRangeLow(0);
    retVal.setGenderCode('M');
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
        .andExpect(content().string(containsString("equipmentCode")));
  }

  @Test
  public void checkGetOne() throws Exception {
    this.mockMvc.perform(get("/api/data/1"))
        .andExpect(status()
            .isOk())
        .andExpect(content().string(containsString("https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PMDFactSheet07_Quark19.pdf")))
        .andExpect(content().string(not(containsString("https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PAP_DocCvg_Factsheet_ICN905064.pdf"))));
  }

  @Test
  public void checkPost() throws Exception {
    MvcResult result = this.mockMvc.perform(post("/api/data")
        .contentType(MediaType.APPLICATION_JSON)
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
    this.mockMvc.perform(put("/api/data/5").contentType(MediaType.APPLICATION_JSON)
        .content(convertObjectToJsonBytes(makeTestDatum())))
        .andExpect(status().isNoContent());
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
