package org.hl7.davinci.endpoint.rems.database.drugs;

import org.hl7.davinci.endpoint.rems.database.requirement.Requirement;
import org.hl7.davinci.endpoint.rems.database.rems.Rems;
import com.fasterxml.jackson.annotation.JsonManagedReference;


import javax.persistence.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drug")
public class Drug {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "codeSystem", nullable = true)
    private String codeSystem;

    @Column(name = "code", nullable = true)
    private String code;

    @Column(name = "createdAt", nullable = false)
    private String createdAt;

    @OneToMany(mappedBy="drug", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Requirement> requirements = new ArrayList<>();

    // ToDo: Revist this relationship mapping between drug and rems, currently broken
    // @OneToMany(mappedBy="drug", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    // private List<Rems> rems = new ArrayList<>();

    public Drug() {
        this.createdAt = ZonedDateTime.now().format(DateTimeFormatter.ofPattern( "uuuu.MM.dd.HH.mm.ss" ));

    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Requirement> getRequirements() {
        return this.requirements;
    }

    public void setRequirements(List<Requirement> requirements) {
        this.requirements = requirements;
    }

    public void addRequirement(Requirement requirement)  {
        this.requirements.add(requirement);
    }

    // ToDo: Revist this relationship mapping between drug and rems, currently broken
    // public List<Rems> getRems() {
    //     return this.rems;
    // }

    // public void setRems(List<Rems> rems) {
    //     this.rems = rems;
    // }

    // public void addRems(Rems rem)  {
    //     this.rems.add(rem);
    // }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCodeSystem() {
        return this.codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
