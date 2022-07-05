package org.hl7.davinci.endpoint.rems.database.requirement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JoinColumn(name = "resource", nullable = true)
    @OneToOne
    private RemsFhir resource;

    @OneToMany(mappedBy="requirement")
    @JsonIgnore
    private List<MetRequirement> metRequirements = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="DRUG_ID")
    @JsonIgnore
    private Drug drug;

    @OneToMany(mappedBy="parentRequirement", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Requirement> childRequirements = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="PARENT_REQUIREMENT", nullable = true)
    @JsonBackReference
    private Requirement parentRequirement;

    public Requirement() {
        this.createdAt = ZonedDateTime.now().format(DateTimeFormatter.ofPattern( "uuuu.MM.dd.HH.mm.ss" ));
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RemsFhir getResource() {
        return this.resource;
    }

    public void setResource(RemsFhir resource) {
        this.resource = resource;
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

    public List<Requirement> getChildRequirements() {
        return this.childRequirements;
    }
    
    public void setChildRequirements(List<Requirement> requirements) {
        this.childRequirements = requirements;
    }
    
    public void addChildRequirements(Requirement requirement)  {
        this.childRequirements.add(requirement);
    }

    public Requirement getParentRequirement () {
        return this.parentRequirement;
    }
    
    public void setParentRequirement(Requirement requirement) {
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
