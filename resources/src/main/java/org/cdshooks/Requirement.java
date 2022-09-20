package org.cdshooks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Requirement {

    private String url;
    private String label;
    private String type;
    static final Logger logger = LoggerFactory.getLogger(Requirement.class);

    public Requirement(Object url, String label, String type) {
        if(url != null) {
            this.url = url.toString();
        } else {
            logger.info(String.format("-- No %s defined",label));
            this.url = null;
        }
        this.label = label;
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(Object url) {
        if(url != null){
            this.url = url.toString();
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
