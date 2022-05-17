package org.hl7.davinci.endpoint.rems.database.fhir;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "rems_fhir")
public class RemsFhir {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "resourceType", nullable = false)
    private String resourceType;

    @Column(name = "timestamp", nullable = false)
    private String timestamp;

    @Lob
    @Column(name = "resource", nullable = false)
    private String resource;

    public RemsFhir() {
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern( "uuuu.MM.dd.HH.mm.ss" ));

    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResource() {
        return this.resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
