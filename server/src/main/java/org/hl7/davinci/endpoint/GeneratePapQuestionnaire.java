package org.hl7.davinci.endpoint;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.QuestionnaireItemType;
import org.hl7.fhir.ValueSetCompose;
import org.hl7.fhir.dstu3.model.*;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.time.*;
import java.util.List;

public class GeneratePapQuestionnaire {
    public static java.util.List<Extension> populateExtention(String cqlName){
        java.util.List<Extension> lExtension = new java.util.ArrayList<Extension>();
        Extension extension = new Extension();
        extension.setUrl("http://hl7.org/fhir/StructureDefinition/cqif-calculatedValue");
        extension.setValue(new StringType().setValue(cqlName));
        lExtension.add(extension);
        return lExtension;
    }

    public static void main(String[] args) {
        FhirContext f = FhirContext.forDstu3();
        Questionnaire q = new Questionnaire();

        q.addIdentifier().setId("urn:hl7:davinci:crd:positive-airway-pressure-questionnaire");
        q.setTitle("Positive Airway Pressure Questionnaire");
        q.setStatus(Enumerations.PublicationStatus.DRAFT);
        java.util.List<Extension> headerExtension = new java.util.ArrayList<Extension>();
        Extension extension = new Extension();
        extension.setUrl("http://hl7.org/fhir/StructureDefinition/cqif-library");
        extension.setValue(new Reference().setReference("urn:hl7:davinci:crd:library-positive-airway-pressure-prepopulate"));
        headerExtension.add(extension);
        q.setExtension(headerExtension);
        q.addSubjectType("Patient");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String createdOn = "2019-05-31";
        try {
            Date createdDate = formatter.parse(createdOn);
            q.setDate(createdDate);
        }
        catch (Exception e){
            // TODO: Add a message about failing to parse
        }


        q.setPublisher("Da Vinci DTR");

        // Question 1 Patient Information
        Questionnaire.QuestionnaireItemComponent patInfo = q.addItem().setLinkId("1");
        patInfo.setType(Questionnaire.QuestionnaireItemType.GROUP);
        Questionnaire.QuestionnaireItemComponent patInfoLastName = patInfo.addItem().setLinkId("1.1");
        patInfoLastName.setText("Last Name:");
        patInfoLastName.setType(Questionnaire.QuestionnaireItemType.STRING);
        patInfoLastName.setExtension(populateExtention("PatientLastName"));
        patInfoLastName.setRequired(true);

        Questionnaire.QuestionnaireItemComponent patInfoFirstName = patInfo.addItem().setLinkId("1.2");
        patInfoFirstName.setText("First Name:");
        patInfoFirstName.setType(Questionnaire.QuestionnaireItemType.STRING);
        patInfoFirstName.setExtension(populateExtention("PatientFirstName"));
        patInfoFirstName.setRequired(true);

        Questionnaire.QuestionnaireItemComponent patInfoMidInit = patInfo.addItem().setLinkId("1.3");
        patInfoMidInit.setText("Middle Initial:");
        patInfoMidInit.setType(Questionnaire.QuestionnaireItemType.STRING);
        patInfoMidInit.setExtension(populateExtention("PatientMiddleInitial"));
        patInfoMidInit.setRequired(true);

        Questionnaire.QuestionnaireItemComponent patInfoDob = patInfo.addItem().setLinkId("1.4");
        patInfoDob.setText("Date of Birth:");
        patInfoDob.setType(Questionnaire.QuestionnaireItemType.DATE);
        patInfoDob.setExtension(populateExtention("PatientDateOfBirth"));
        patInfoDob.setRequired(true);

        Questionnaire.QuestionnaireItemComponent patInfoGender = patInfo.addItem().setLinkId("1.5");
        patInfoGender.setText("Gender:");
        patInfoGender.setType(Questionnaire.QuestionnaireItemType.CHOICE);
        Coding gender = new Coding();
        gender.setDisplay("Male");
        gender.setCode("male");
        patInfoGender.addOption(new Questionnaire.QuestionnaireItemOptionComponent().setValue(gender));
        gender = new Coding();
        gender.setDisplay("Female");
        gender.setCode("female");
        patInfoGender.addOption(new Questionnaire.QuestionnaireItemOptionComponent().setValue(gender));
        patInfoGender.setExtension(populateExtention("PatientGender"));
        patInfoGender.setRequired(true);

        Questionnaire.QuestionnaireItemComponent patInfoMedicareId = patInfo.addItem().setLinkId("1.6");
        patInfoMedicareId.setText("Medicare ID:");
        patInfoMedicareId.setType(Questionnaire.QuestionnaireItemType.STRING);
        patInfoMedicareId.setExtension(populateExtention("PatientMedicareId"));
        patInfoMedicareId.setRequired(true);


        // Question 2 - Provider Information
        Questionnaire.QuestionnaireItemComponent provInfo = q.addItem().setLinkId("2");
        provInfo.setType(Questionnaire.QuestionnaireItemType.GROUP);
        provInfo.setText("Provider who is performing face-to-face evaluation");

        Questionnaire.QuestionnaireItemComponent provInfoLastName = provInfo.addItem().setLinkId("2.1");
        provInfoLastName.setText("Last Name");
        provInfoLastName.setType(Questionnaire.QuestionnaireItemType.STRING);
        provInfoLastName.setExtension(populateExtention("OrderingProviderLastName"));
        provInfoLastName.setRequired(false);

        Questionnaire.QuestionnaireItemComponent provInfoFirstName = provInfo.addItem().setLinkId("2.2");
        provInfoFirstName.setText("First Name");
        provInfoFirstName.setType(Questionnaire.QuestionnaireItemType.STRING);
        provInfoFirstName.setExtension(populateExtention("OrderingProviderFirstName"));
        provInfoFirstName.setRequired(false);

        Questionnaire.QuestionnaireItemComponent provInfoMiddleInitial = provInfo.addItem().setLinkId("2.3");
        provInfoMiddleInitial.setText("Middle Initial");
        provInfoMiddleInitial.setType(Questionnaire.QuestionnaireItemType.STRING);
        provInfoMiddleInitial.setExtension(populateExtention("OrderingProviderMiddleInitial"));
        provInfoMiddleInitial.setRequired(false);


        // Question 3 - Face-to-Face evaluation
        Questionnaire.QuestionnaireItemComponent f2fDate = q.addItem().setLinkId("3");
        f2fDate.setText("Date of F2F evaluation (MM/DD/YYYY):");
        f2fDate.setType(Questionnaire.QuestionnaireItemType.GROUP);
        Questionnaire.QuestionnaireItemComponent f2fDateBox = f2fDate.addItem().setLinkId("3.1");
        f2fDateBox.setType(Questionnaire.QuestionnaireItemType.DATE);


        // Question 4 - Patient Diagnosis
        Questionnaire.QuestionnaireItemComponent patDiag = q.addItem().setLinkId("4");
        patDiag.setType(Questionnaire.QuestionnaireItemType.GROUP);
        patDiag.setText("Patient diagnosis:");
        Questionnaire.QuestionnaireItemComponent patDiagOsa = patDiag.addItem().setLinkId("4.1");
        patDiagOsa.setType(Questionnaire.QuestionnaireItemType.CHOICE);
        Coding codeOsa = new Coding();
        codeOsa.setCode("OSA");
        codeOsa.setDisplay("Obstructive Sleep Apnea");
        patDiagOsa.addOption(new Questionnaire.QuestionnaireItemOptionComponent().setValue(codeOsa));
        patDiagOsa.setExtension(populateExtention("OsaDiagnosis"));

        Questionnaire.QuestionnaireItemComponent patDiagOther = patDiag.addItem().setLinkId("4.2");
        patDiagOther.setText("Other:");
        patDiagOther.setType(Questionnaire.QuestionnaireItemType.CHOICE);
        Coding codeOther = new Coding();
        codeOther.setCode("Other");
        codeOther.setDisplay("Yes");
        patDiagOther.addOption(new Questionnaire.QuestionnaireItemOptionComponent().setValue(codeOther));

        Questionnaire.QuestionnaireItemComponent patDiagOtherDescr = patDiag.addItem().setLinkId("4.3");
        patDiagOtherDescr.setText("Other (describe):");
        patDiagOtherDescr.setType(Questionnaire.QuestionnaireItemType.TEXT);
        patDiagOtherDescr.setExtension(populateExtention("OtherDiagnoses"));

        // Question 5 - Order Start Date
        Questionnaire.QuestionnaireItemComponent orderStart = q.addItem().setLinkId("5");
        orderStart.setType(Questionnaire.QuestionnaireItemType.GROUP);
        Questionnaire.QuestionnaireItemComponent orderStartDate = orderStart.addItem().setLinkId("5.1");
        orderStartDate.setText("Order start date, if different from date of order (MM/DD/YYYY):");
        orderStartDate.setType(Questionnaire.QuestionnaireItemType.DATE);


        // Question 6 - Order type
        Questionnaire.QuestionnaireItemComponent orderType = q.addItem().setLinkId("6");
        orderType.setType(Questionnaire.QuestionnaireItemType.GROUP);
        orderType.setText("Type of order:");
        Questionnaire.QuestionnaireItemComponent orderTypeDevice = orderType.addItem().setLinkId("6.1");
        orderTypeDevice.setText("Device:");
        orderTypeDevice.setType(Questionnaire.QuestionnaireItemType.CHOICE);
        Coding codeOrderType = new Coding();
        codeOrderType.setDisplay("Initial");
        codeOrderType.setCode("initial");
        orderTypeDevice.addOption(new Questionnaire.QuestionnaireItemOptionComponent().setValue(codeOrderType));
        codeOrderType = new Coding();
        codeOrderType.setDisplay("Revision or change in equipment");
        codeOrderType.setCode("revision");
        orderTypeDevice.addOption(new Questionnaire.QuestionnaireItemOptionComponent().setValue(codeOrderType));
        codeOrderType = new Coding();
        codeOrderType.setDisplay("Replacement");
        codeOrderType.setCode("replacement");
        orderTypeDevice.addOption(new Questionnaire.QuestionnaireItemOptionComponent().setValue(codeOrderType));
        Questionnaire.QuestionnaireItemComponent orderSupplies = orderType.addItem().setLinkId("6.2");
        orderSupplies.setText("Supplies:");
        orderSupplies.setType(Questionnaire.QuestionnaireItemType.CHOICE);
        Coding codeOrderSupply = new Coding();
        codeOrderSupply.setDisplay("Initial");
        codeOrderSupply.setCode("initial");
        orderSupplies.addOption(new Questionnaire.QuestionnaireItemOptionComponent().setValue(codeOrderSupply));
        codeOrderSupply = new Coding();
        codeOrderSupply.setDisplay("Reorder");
        codeOrderSupply.setCode("reorder");
        orderSupplies.addOption(new Questionnaire.QuestionnaireItemOptionComponent().setValue(codeOrderSupply));
        codeOrderSupply = new Coding();
        codeOrderSupply.setDisplay("Other");
        codeOrderSupply.setCode("other");
        orderSupplies.addOption(new Questionnaire.QuestionnaireItemOptionComponent().setValue(codeOrderSupply));
        Questionnaire.QuestionnaireItemComponent orderSuppliesOther = orderType.addItem().setLinkId("6.3");
        orderSuppliesOther.setType(Questionnaire.QuestionnaireItemType.TEXT);
        orderSuppliesOther.setText("Other (description):");

        // Question 7 - Device Order
        Questionnaire.QuestionnaireItemComponent deviceOrder = q.addItem().setLinkId("7");
        deviceOrder.setType(Questionnaire.QuestionnaireItemType.GROUP);
        Questionnaire.QuestionnaireItemComponent deviceDescr = deviceOrder.addItem().setLinkId("7.1");
        deviceDescr.setType(Questionnaire.QuestionnaireItemType.TEXT);
        deviceDescr.setText("Device Order (description of device):");

        Questionnaire.QuestionnaireItemComponent deviceSelected = deviceOrder.addItem().setLinkId("7.2");
        deviceSelected.setType(Questionnaire.QuestionnaireItemType.CHOICE);
        deviceSelected.setRepeats(true);
        Coding papDevice = new Coding();
        papDevice.setDisplay("E0601 Continuous Positive Airway Pressure device");
        papDevice.setCode("E0601");
        deviceSelected.addOption(new Questionnaire.QuestionnaireItemOptionComponent().setValue(papDevice));
        papDevice = new Coding();
        papDevice.setDisplay("E0470 Respiratory assist device, bi-level w/o backup");
        papDevice.setCode("E0470");
        deviceSelected.addOption(new Questionnaire.QuestionnaireItemOptionComponent().setValue(papDevice));
        deviceSelected.setExtension(populateExtention("PapDeviceRequested"));


        // Question 8 - Supply Order
        // TODO: Need to determine appropriate source


        // Question 9 - Signoff
        Questionnaire.QuestionnaireItemComponent signOff = q.addItem().setLinkId("9");
        signOff.setType(Questionnaire.QuestionnaireItemType.GROUP);
        Questionnaire.QuestionnaireItemComponent signature = signOff.addItem().setLinkId("9.1");
        signature.setText("Signature:");
        signature.setType(Questionnaire.QuestionnaireItemType.STRING);
        Questionnaire.QuestionnaireItemComponent signingProvider = signOff.addItem().setLinkId("9.2");
        signingProvider.setText("Name (Printed):");
        signingProvider.setType(Questionnaire.QuestionnaireItemType.STRING);
        signingProvider.setExtension(populateExtention("OrderingProviderFullName"));
        signingProvider.setRequired(true);
        Questionnaire.QuestionnaireItemComponent signingDate = signOff.addItem().setLinkId("9.3");
        signingDate.setType(Questionnaire.QuestionnaireItemType.DATE);
        signingDate.setText("Date (MM/DD/YYYY):");
        signingDate.setRequired(true);
        signingDate.setExtension(populateExtention("Today"));
        Questionnaire.QuestionnaireItemComponent signingNpi = signOff.addItem().setLinkId("9.4");
        signingNpi.setText("NPI:");
        signingNpi.setType(Questionnaire.QuestionnaireItemType.STRING);
        signingNpi.setExtension(populateExtention("OrderingProviderNPI"));
        signingNpi.setRequired(true);

        System.out.println(f.newJsonParser().setPrettyPrint(true).encodeResourceToString(q));

        try {
            java.io.FileWriter filewriter = new java.io.FileWriter("/Users/kmulcahy/git/davinci/crd/server/src/main/jib/smartAppFhirArtifacts/positive-airway-pressure-questionnaire.json");
            java.io.PrintWriter printWriter = new PrintWriter(filewriter);
            printWriter.write(f.newJsonParser().setPrettyPrint(true).encodeResourceToString(q));
            printWriter.close();
        }
        catch (Exception e){
            // TODO: handle IOException
        }
    }
}
