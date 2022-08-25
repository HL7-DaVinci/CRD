package org.hl7.davinci.endpoint.rems.database.requirement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @Column(name = "functionalId", nullable = false)
    private String functionalId;

    @Column(name = "completed", nullable = false)
    private Boolean completed ;

    // FHIR resource which defines the requirement (task, questionnaire, etc)
    @JoinColumn(name = "completedRequirement", nullable = true)
    @OneToOne(fetch = FetchType.EAGER)
    private RemsFhir completedRequirement;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="REQUIREMENT_ID")
    private Requirement requirement;

    @ManyToMany
    // @JoinColumn(name="REMS_REQUEST")
    @JsonBackReference
    private List<Rems> remsRequest = new ArrayList<>();
    
    @OneToMany(mappedBy="parentMetRequirement", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<MetRequirement> childMetRequirements = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="PARENT_MET_REQUIREMENT", nullable = true)
    @JsonBackReference
    private MetRequirement parentMetRequirement;

    public MetRequirement() {
        this.createdAt = ZonedDateTime.now().format(DateTimeFormatter.ofPattern( "uuuu.MM.dd.HH.mm.ss" ));
        this.completed = false;    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Rems> getRemsRequest() {
        return this.remsRequest;
    }

    public void setRemsRequest(List<Rems> requests) {
        this.remsRequest = requests;
    }

    public void addRemsRequest(Rems request)  {
        this.remsRequest.add(request);
    }

    public RemsFhir getCompletedRequirement() {
        return this.completedRequirement;
    }

    public void setCompletedRequirement(RemsFhir requirement) {
        this.completedRequirement = requirement;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getFunctionalId() {
        return this.functionalId;
    }

    public void setFunctionalId(String id) {
        this.functionalId = id;
    }

    public Boolean getCompleted() {
        return this.completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Requirement getRequirement() {
        return this.requirement;
    }

    public void setRequirement(Requirement requirement) {
        this.requirement = requirement;
    }
    public List<MetRequirement> getChildMetRequirements() {
        return this.childMetRequirements;
    }
    
    public void setChildMetRequirements(List<MetRequirement> requirements) {
        this.childMetRequirements = requirements;
    }
    
    public void addChildMetRequirements(MetRequirement requirement)  {
        this.childMetRequirements.add(requirement);
    }

    public MetRequirement getParentMetRequirement () {
        return this.parentMetRequirement;
    }
    
    public void setParentMetRequirement(MetRequirement requirement) {
        this.parentMetRequirement = requirement;
    }

}
