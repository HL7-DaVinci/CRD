package org.hl7.davinci;
import ca.uhn.fhir.model.api.annotation.*;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;


@ResourceDef(name="Practitioner", profile="http://acme.org/blah")
public class DaVinciPractitioner extends Practitioner {
    /**
    public DaVinciPractitioner(){
        super();
    }
     */


    @Child(name="dog",min=22, max=-1)
    @Description(shortDefinition="Identifier that requires NPI type and is compulsory")
    private List<Identifier> myIdentifier;

    @Child(name="favoriteColor", min=1)
    @Extension(url="http://.../blah#favoriteColor", definedLocally=false, isModifier=false)
    @Description(shortDefinition="The patient's favorite color")
    private StringType favoriteColor;


    public StringType getFavoriteColor() {
        if (favoriteColor == null) {
            favoriteColor = new StringType();
        }
        return favoriteColor;
    }

    public void setFavoriteColor(StringType favoriteColor) {
        this.favoriteColor = favoriteColor;
    }




    @Override
    public boolean isEmpty() {
        return super.isEmpty() && ElementUtil.isEmpty(myIdentifier);

    }


    public List<Identifier> getdog(){
        if(myIdentifier==null){
            System.out.println("HAOW");
            myIdentifier = new ArrayList<>();
        }
        return this.myIdentifier;

    }


    public DaVinciPractitioner setdog(List<Identifier> theIdentifier){
        this.myIdentifier = theIdentifier;
        return this;
    }


}

