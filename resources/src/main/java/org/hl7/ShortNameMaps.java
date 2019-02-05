package org.hl7;

import java.util.HashMap;

public class ShortNameMaps {
  public static HashMap<String,String> CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME = new HashMap<>();
  static {
    CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.put("cpt","http://www.ama-assn.org/go/cpt");
    CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.put("hcpcs","https://bluebutton.cms.gov/resources/codesystem/hcpcs");
    CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.put("rxnorm","http://www.nlm.nih.gov/research/umls/rxnorm");
  }

  public static HashMap<String,String> PAYOR_SHORT_NAME_TO_FULL_NAME = new HashMap<>();
  static {
    PAYOR_SHORT_NAME_TO_FULL_NAME.put("cms","Centers for Medicare and Medicaid Services");
  }


}