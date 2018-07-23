package fhir.restful.Database;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


//patient_age_range_low, patient_age_range_high,
// patient_gender, patient_plan_id, equipment_code,
// no_auth_needed, info_link
@Entity
public class Datum {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String patient_age_range_low;

    @Column(nullable = false)
    private String patient_age_range_high;

    @Column(nullable = false)
    private String patient_gender;

    @Column(nullable = false)
    private String patient_plan_id;

    @Column(nullable = false)
    private String equipment_code;

    @Column(nullable = false)
    private String no_auth_needed;

    @Column(nullable = false)
    private String info_link;

    public Datum(){

    }
    public Datum(String patient_age_range_low, String patient_age_range_high, String patient_gender,
                 String patient_plan_id, String equipment_code, String no_auth_needed, String info_link) {
        this.patient_age_range_low = patient_age_range_low;
        this.patient_age_range_high = patient_age_range_high;
        this.patient_gender = patient_gender;
        this.patient_plan_id = patient_plan_id;
        this.equipment_code = equipment_code;
        this.no_auth_needed = no_auth_needed;
        this.info_link = info_link;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatient_age_range_low() {
        return patient_age_range_low;
    }

    public void setPatient_age_range_low(String patient_age_range_low) {
        this.patient_age_range_low = patient_age_range_low;
    }

    public String getPatient_age_range_high() {
        return patient_age_range_high;
    }

    public void setPatient_age_range_high(String patient_age_range_high) {
        this.patient_age_range_high = patient_age_range_high;
    }

    public String getPatient_gender() {
        return patient_gender;
    }

    public void setPatient_gender(String patient_gender) {
        this.patient_gender = patient_gender;
    }

    public String getPatient_plan_id() {
        return patient_plan_id;
    }

    public void setPatient_plan_id(String patient_plan_id) {
        this.patient_plan_id = patient_plan_id;
    }

    public String getEquipment_code() {
        return equipment_code;
    }

    public void setEquipment_code(String equipment_code) {
        this.equipment_code = equipment_code;
    }

    public String getNo_auth_needed() {
        return no_auth_needed;
    }

    public void setNo_auth_needed(String no_auth_needed) {
        this.no_auth_needed = no_auth_needed;
    }

    public String getInfo_link() {
        return info_link;
    }

    public void setInfo_link(String info_link) {
        this.info_link = info_link;
    }



    @Override
    public String toString(){
        return this.equipment_code;
    }

    public static List<String> getFields() {
        List<String> fieldList = new ArrayList<>();
        for (Field field : Datum.class.getDeclaredFields()) {
            String name = field.getName();
            fieldList.add(name);
        }
        return fieldList;
    }


}
