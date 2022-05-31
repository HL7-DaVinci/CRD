package  org.hl7.davinci.endpoint.rems.database.rems;
import org.hl7.davinci.endpoint.rems.database.requirement.MetRequirement;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Type;
import com.fasterxml.jackson.databind.JsonNode;


@Entity
@Table(name = "rems")
@TypeDef(name = "json", typeClass = JsonType.class, defaultForType = JsonNode.class )
public class Rems {
  @Id
  @Column(name = "case_number", nullable = false, length = 100)
  private String case_number;


  @Column(name = "status", nullable = false, length = 100)
  private String status;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "resource", nullable = false, length = 10000000)
  private JsonNode resource;

  @OneToMany(mappedBy="remsRequest", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<MetRequirement> metRequirements = new ArrayList<>();

  public void Rems() {}

  public String getCase_number() {
    return this.case_number;
  }

  public void setCase_number(String id) {
    this.case_number = id;
  }

  public String getStatus() {
    return this.status;
  }

  public void setStatus(String statusParam) {
    this.status = statusParam;
  }

  public List<MetRequirement> getMetRequirements() {
    return this.metRequirements;
}

public void setMetRequirement(List<MetRequirement> metRequirements) {
    this.metRequirements = metRequirements;
}

public void addMetRequirement(MetRequirement metRequirement)  {
    this.metRequirements.add(metRequirement);
}

public JsonNode getResource() {
  return this.resource;
}

public void setResource(JsonNode resource) {
  this.resource = resource;
}
}
