package org.hl7.davinci.endpoint.rems.database.requirement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hl7.davinci.endpoint.rems.database.drugs.Drug;
import org.hl7.davinci.endpoint.rems.database.fhir.RemsFhir;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "requirement")
public class Requirement {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "timestamp", nullable = false)
    private String timestamp;

    @Column(name = "completed", nullable = false)
    private boolean completed ;

    @Column(name = "description", nullable = true)
    private String description;

    // this should be a reference to a FHIR resource
    @JoinColumn(name = "requirement", nullable = false)
    @OneToOne
    private RemsFhir requirement;

    @ManyToOne
    @JoinColumn(name="DRUG_ID")
    @JsonIgnore
    private Drug drug;

    public Requirement() {
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern( "uuuu.MM.dd.HH.mm.ss" ));
        this.completed = false;
    }

    public RemsFhir getRequirement() {
        return this.requirement;
    }

    public void setRequirement(RemsFhir requirement) {
        this.requirement = requirement;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }

}
