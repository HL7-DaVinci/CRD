package org.hl7.davinci.endpoint.rems.database.fhir;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.Type;
import org.hl7.fhir.instance.model.api.IBaseResource;

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

    @Type(type = "json")
    @Column(columnDefinition = "json", name = "complianceBundle", nullable = false, length = 10000000)
    private JsonNode resource;



    public RemsFhir() {
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern( "uuuu.MM.dd.HH.mm.ss" ));

    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JsonNode getResource() {
        return this.resource;
    }

    public void setResource(JsonNode resource) {
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
