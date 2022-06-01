package org.hl7.davinci.endpoint.rems.database.requirement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hl7.davinci.endpoint.rems.database.drugs.Drug;
import org.hl7.davinci.endpoint.rems.database.fhir.RemsFhir;
import org.hl7.davinci.endpoint.rems.database.rems.Rems;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "metRequirement")
public class MetRequirement {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "createdAt", nullable = false)
    private String createdAt;

    @Column(name = "completed", nullable = false)
    private boolean completed ;

    // FHIR resource which defines the requirement (task, questionnaire, etc)
    @JoinColumn(name = "completedResource", nullable = true)
    @OneToOne(fetch = FetchType.LAZY)
    private RemsFhir completedResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REQUIREMENT_ID")
    @JsonIgnore
    private Requirement requirement;

    @ManyToOne
    @JoinColumn(name="REMS_REQUEST")
    @JsonIgnore
    private Rems remsRequest;

    @OneToMany(mappedBy="parentMetRequirement", fetch = FetchType.LAZY)
    private List<MetRequirement> childMetRequirements = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="PARENT_MET_REQUIREMENT")
    @JsonIgnore
    private MetRequirement parentMetRequirement;

    public MetRequirement() {
        this.createdAt = ZonedDateTime.now().format(DateTimeFormatter.ofPattern( "uuuu.MM.dd.HH.mm.ss" ));
        this.completed = false;
    }

    public RemsFhir getCompletedResource() {
        return this.completedResource;
    }

    public void setCompletedResource(RemsFhir requirement) {
        this.completedResource = requirement;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public void setRequirement(Requirement requirement) {
        this.requirement = requirement;
    }

    public List<MetRequirement> getChildren() {
        return this.childMetRequirements;
    }
    
    public void setChildren(List<MetRequirement> requirements) {
        this.childMetRequirements = requirements;
    }
    
    public void addChild(MetRequirement requirement)  {
        this.childMetRequirements.add(requirement);
    }

    public MetRequirement getParent() {
        return this.parentMetRequirement;
    }
    
    public void setParent(MetRequirement requirement) {
        this.parentMetRequirement = requirement;
    }

}
