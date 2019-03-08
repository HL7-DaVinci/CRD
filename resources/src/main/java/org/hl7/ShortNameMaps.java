package org.hl7;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;


public class ShortNameMaps {
  public static BiMap<String,String> CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME = HashBiMap.create();
  static {
    CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.put("cpt","http://www.ama-assn.org/go/cpt");
    CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.put("hcpcs","https://bluebutton.cms.gov/resources/codesystem/hcpcs");
    CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.put("rxnorm","http://www.nlm.nih.gov/research/umls/rxnorm");
  }

  public static BiMap<String,String> PAYOR_SHORT_NAME_TO_FULL_NAME = HashBiMap.create();
  static {
    PAYOR_SHORT_NAME_TO_FULL_NAME.put("cms","Centers for Medicare and Medicaid Services");
  }


}