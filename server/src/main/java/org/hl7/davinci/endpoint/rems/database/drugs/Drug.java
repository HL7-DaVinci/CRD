package org.hl7.davinci.endpoint.rems.database.drugs;

import org.hl7.davinci.endpoint.rems.database.requirement.Requirement;

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

    @OneToMany(mappedBy="drug")
    private List<Requirement> requirements = new ArrayList<>();

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

    public void setResource(List<Requirement> requirements) {
        this.requirements = requirements;
    }

    public void addRequirement(Requirement requirement)  {
        this.requirements.add(requirement);
    }
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
