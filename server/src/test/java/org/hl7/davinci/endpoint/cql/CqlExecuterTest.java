package org.hl7.davinci.endpoint.cql;

import org.hl7.davinci.endpoint.database.CoverageRequirementRuleCriteria;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CqlExecuterTest {
  @Test
  public void testExecute() {
    String sql = null;
    try {
      sql = readFile("src/main/resources/rules/cms/cpt/94660.cql");
    } catch (Exception e) {
      System.out.println("Failed to open CQL file: \n" + e.getMessage());
      assertTrue(false);
    }

    CqlExecuter executer = new CqlExecuter(sql);

    CoverageRequirementRuleCriteria criteria = new CoverageRequirementRuleCriteria();
    CqlRule rule = null;

    criteria.setAge(45)
        .setGenderCode('M')
        .setPatientAddressState("MA")
        .setProviderAddressState("MA")
        .setEquipmentCode("94660")
        .setCodeSystem("http://www.ama-assn.org/go/cpt");
    rule = executer.execute(criteria);
    assertNotNull(rule);
    assertFalse(rule.getNoAuthNeeded());
    assertEquals(rule.getInfoLink(), "https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PAP_DocCvg_Factsheet_ICN905064.pdf");

    criteria.setAge(110)
        .setGenderCode('M')
        .setPatientAddressState("MA")
        .setProviderAddressState("MA")
        .setEquipmentCode("94660")
        .setCodeSystem("http://www.ama-assn.org/go/cpt");
    rule = executer.execute(criteria);
    assertNull(rule);

    criteria.setAge(45)
        .setGenderCode('F')
        .setPatientAddressState("MA")
        .setProviderAddressState("MA")
        .setEquipmentCode("94660")
        .setCodeSystem("http://www.ama-assn.org/go/cpt");
    rule = executer.execute(criteria);
    assertNotNull(rule);
    assertFalse(rule.getNoAuthNeeded());
    assertEquals(rule.getInfoLink(), "https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PAP_DocCvg_Factsheet_ICN905064.pdf");

    criteria.setAge(45)
        .setGenderCode('M')
        .setPatientAddressState("VT")
        .setProviderAddressState("MA")
        .setEquipmentCode("94660")
        .setCodeSystem("http://www.ama-assn.org/go/cpt");
    rule = executer.execute(criteria);
    assertNotNull(rule);
    assertFalse(rule.getNoAuthNeeded());
    assertEquals(rule.getInfoLink(), "https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PAP_DocCvg_Factsheet_ICN905064.pdf");

    criteria.setAge(45)
        .setGenderCode('M')
        .setPatientAddressState("MA")
        .setProviderAddressState("VT")
        .setEquipmentCode("94660")
        .setCodeSystem("http://www.ama-assn.org/go/cpt");
    rule = executer.execute(criteria);
    assertNotNull(rule);
    assertFalse(rule.getNoAuthNeeded());
    assertEquals(rule.getInfoLink(), "https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PAP_DocCvg_Factsheet_ICN905064.pdf");

    criteria.setAge(45)
        .setGenderCode('M')
        .setPatientAddressState("MA")
        .setProviderAddressState("MA")
        .setEquipmentCode("4")
        .setCodeSystem("http://www.ama-assn.org/go/cpt");
    rule = executer.execute(criteria);
    assertNull(rule);

    criteria.setAge(45)
        .setGenderCode('M')
        .setPatientAddressState("MA")
        .setProviderAddressState("MA")
        .setEquipmentCode("94660")
        .setCodeSystem("https://someplace");
    rule = executer.execute(criteria);
    assertNull(rule);
  }

  private String readFile(String fileName) throws Exception {
    Path filePath = FileSystems.getDefault().getPath(fileName).toAbsolutePath().normalize();

    byte[] fileContent = Files.readAllBytes(filePath);

    CharsetDecoder cd = StandardCharsets.UTF_8.newDecoder();
    cd.onMalformedInput(CodingErrorAction.REPORT);
    cd.onUnmappableCharacter(CodingErrorAction.REPORT);

    return cd.decode(ByteBuffer.wrap(fileContent)).toString();
  }
}
