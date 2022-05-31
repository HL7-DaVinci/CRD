package org.hl7.davinci.endpoint.rems.database.requirement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hl7.davinci.endpoint.rems.database.drugs.Drug;
import org.hl7.davinci.endpoint.rems.database.fhir.RemsFhir;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "createdAt", nullable = false)
    private String createdAt;

    @Column(name = "description", nullable = true)
    private String description;

    // FHIR resource which defines the requirement (task, questionnaire, etc)
    @JoinColumn(name = "requirement", nullable = false)
    @OneToOne
    private RemsFhir requirement;

    @OneToMany(mappedBy="requirement")
    private List<MetRequirement> metRequirements = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="DRUG_ID")
    @JsonIgnore
    private Drug drug;

    @OneToMany(mappedBy="parentRequirement", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Requirement> childRequirements = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PARENT_REQUIREMENT")
    @JsonIgnore
    private Requirement parentRequirement;

    public Requirement() {
        this.createdAt = ZonedDateTime.now().format(DateTimeFormatter.ofPattern( "uuuu.MM.dd.HH.mm.ss" ));
    }

    public RemsFhir getRequirement() {
        return this.requirement;
    }

    public void setRequirement(RemsFhir requirement) {
        this.requirement = requirement;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
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

    public List<Requirement> getChildren() {
        return this.childRequirements;
    }
    
    public void setChildren(List<Requirement> requirements) {
        this.childRequirements = requirements;
    }
    
    public void addChild(Requirement requirement)  {
        this.childRequirements.add(requirement);
    }

    public Requirement getParent() {
        return this.parentRequirement;
    }
    
    public void setParent(Requirement requirement) {
        this.parentRequirement = requirement;
    }

    public List<MetRequirement> getMetRequirements() {
        return this.metRequirements;
    }
    
    public void setMetRequirements(List<MetRequirement> metRequirements) {
        this.metRequirements = metRequirements;
    }
    
    public void addChild(MetRequirement metRequirement)  {
        this.metRequirements.add(metRequirement);
    }

}
