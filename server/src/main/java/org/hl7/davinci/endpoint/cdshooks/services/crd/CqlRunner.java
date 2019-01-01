package org.hl7.davinci.endpoint.cdshooks.services.crd;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.bind.JAXBException;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.ExpressionDef;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumService;
import org.hl7.fhir.dstu3.model.Resource;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;
import org.opencds.cqf.cql.execution.Context;
import org.opencds.cqf.cql.execution.CqlLibraryReader;
import org.opencds.cqf.cql.execution.LibraryLoader;


public class CqlRunner {
//  private FhirContext fhirContext = FhirContext.forDstu3();
  private Resource deviceRequestResource;
  private Resource patientResource;
  private String cql;

  public CqlRunner(String cql, Resource patientResource, Resource deviceRequestResource) {
    this.deviceRequestResource = deviceRequestResource;
    this.patientResource = patientResource;
    this.cql = cql;
  }




  private String loadFile(String filePath) throws Exception{
    Path path = Paths.get(getClass().getClassLoader()
        .getResource(filePath).toURI());
    byte[] fileBytes = Files.readAllBytes(path);
    return new String(fileBytes);
  }

//  private Resource loadFhirResourceFile(String resourceFilePath) {
//    return (Resource)fhirContext.newJsonParser().parseResource(
//        new InputStreamReader(getClass().getResourceAsStream(resourceFilePath)));
//  }

  public HashMap<String, Object> execute() {
//    String patientJson = loadFile("patient.json");
//    String deviceRequestJson = loadFile("deviceRequestSTU3.json");
//    String patientXml = loadFile("patient.xml");

//    Resource resource = loadFhirResourceFile("patient.json");
//    String basecql = loadFile("age.cql");

//    IParser parser = fhirContext.newJsonParser();
//    Resource patientResource = (Resource) parser.parseResource(patientJson);
//    Resource deviceRequestResource = (Resource) parser.parseResource(deviceRequestJson);
//    Resource resource = (Resource)fhirContext.newXmlParser().parseResource(patientXml);

    Library library = null;

    try {
      library = translate(cql);
    } catch (Exception e){
      throw new RuntimeException(e);
    }

    Context context = new Context(library);

    context.registerLibraryLoader(getLibraryLoader());

    BaseFhirDataProvider provider = new FhirDataProviderStu3().setEndpoint("http://fhirtest.uhn.ca/baseDstu3");
    context.registerDataProvider("http://hl7.org/fhir", provider);

//    context.setParameter(null, "Patient", patientResource);
    context.setParameter(null, "DeviceRequest", deviceRequestResource);
    context.setParameter(null, "Patient", patientResource);



    // NOTE: you can serialize the library, and then stop importing and translating the cql.
    // In a very simple test, this saved ~2.2 seconds (from ~4.1 to 1.9), over 50%
    // you need to make some class(es) in cql-execution serializable (just add implements serializable)
//    FileOutputStream fileOutputStream
//        = new FileOutputStream("library.txt");
//    ObjectOutputStream objectOutputStream
//        = new ObjectOutputStream(fileOutputStream);
//    objectOutputStream.writeObject(library);
//    objectOutputStream.flush();
//    objectOutputStream.close();
//
//    FileInputStream fileInputStream
//        = new FileInputStream("library.txt");
//    ObjectInputStream objectInputStream
//        = new ObjectInputStream(fileInputStream);
//    Library library2 = (Library) objectInputStream.readObject();
//    objectInputStream.close();





    HashMap<String, Object> results = new HashMap<>();
    for (ExpressionDef expressionDef: library.getStatements().getDef()) {
      System.out.println("Evaluating expression "+expressionDef.getName());
      String name = expressionDef.getName();
      Object result = expressionDef.evaluate(context);
      results.put(expressionDef.getName(), result);
    }

    System.out.println(results);
    return results;
  }

  private Library translate(String cql) throws Exception {
    String ucumEssence = loadFile("ucum-essence.xml");

    ArrayList<CqlTranslator.Options> options = new ArrayList<>();
    options.add(CqlTranslator.Options.EnableDateRangeOptimization);
//    UcumService ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
    UcumService ucumService = new UcumEssenceService(new ByteArrayInputStream(ucumEssence.getBytes(StandardCharsets.UTF_8)));
    CqlTranslator translator = CqlTranslator.fromText(cql, getModelManager(), getLibraryManager(), ucumService, options.toArray(new CqlTranslator.Options[options.size()]));
    if (translator.getErrors().size() > 0) {
      ArrayList<String> errors = new ArrayList<>();
      for (CqlTranslatorException error : translator.getErrors()) {
        TrackBack tb = error.getLocator();
        String lines = tb == null ? "[n/a]" : String.format("[%d:%d, %d:%d]",
            tb.getStartLine(), tb.getStartChar(), tb.getEndLine(), tb.getEndChar());
        errors.add(lines + error.getMessage());
      }
      throw new IllegalArgumentException(errors.toString());
    }

    Library library = null;
    try {
      library = CqlLibraryReader.read(new StringReader(translator.toXml()));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (JAXBException e) {
      e.printStackTrace();
    }

    return library;
  }

  private ModelManager modelManager;
  private ModelManager getModelManager() {
    if (modelManager == null) {
      modelManager = new ModelManager();
    }

    return modelManager;
  }

  private LibraryManager libraryManager;
  private LibraryManager getLibraryManager() {
    if (libraryManager == null) {
      libraryManager = new LibraryManager(getModelManager());
      libraryManager.getLibrarySourceLoader().clearProviders();
      libraryManager.getLibrarySourceLoader().registerProvider(new TestLibrarySourceProviderClone());
    }
    return libraryManager;
  }

  private LibraryLoader libraryLoader;
  private LibraryLoader getLibraryLoader() {
    if (libraryLoader == null) {
      libraryLoader = new TestLibraryLoaderClone(libraryManager);
    }
    return libraryLoader;
  }
}
