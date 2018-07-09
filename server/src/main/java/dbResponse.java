public class dbResponse {
    private String infoLink;
    private Boolean noAuthNeeded;

    dbResponse(String infoLink, Boolean noAuthNeeded){
        this.infoLink = infoLink;
        this.noAuthNeeded = noAuthNeeded;
    }

    public String getInfoLink() {
        return infoLink;
    }

    public void setInfoLink(String infoLink) {
        this.infoLink = infoLink;
    }

    public Boolean getNoAuthNeeded() {
        return noAuthNeeded;
    }

    public void setNoAuthNeeded(Boolean noAuthNeeded) {
        this.noAuthNeeded = noAuthNeeded;
    }

}
