package org.hl7.davinci.endpoint.cql;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.FhirLibrarySourceProvider;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumService;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundle;
import org.hl7.fhir.dstu3.model.Resource;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.execution.Context;
import org.opencds.cqf.cql.execution.CqlLibraryReader;
import org.opencds.cqf.cql.execution.LibraryLoader;


public class CqlExecutionContextBuilder {

  public static Context getExecutionContextStu3(CqlBundle cqlPackage, HashMap<String,Resource> cqlParams) {
    ModelManager modelManager = new ModelManager();
    LibraryManager libraryManager = new LibraryManager(modelManager);
    libraryManager.getLibrarySourceLoader().clearProviders();

    Library library = null;
    LibraryLoader libraryLoader = null;

    if (cqlPackage.isPrecompiled()){
      //todo
    } else {
      libraryManager.getLibrarySourceLoader().registerProvider(cqlPackage.getRawCqlLibrarySourceProvider());
      libraryLoader = new LocalLibraryLoader(libraryManager);
      try {
        library = translate(cqlPackage.getRawMainCqlLibrary(), libraryManager, modelManager);
      } catch (Exception e){
        throw new RuntimeException(e);
      }
    }

    Context context = new Context(library);
    context.registerLibraryLoader(libraryLoader);
    context.setExpressionCaching(true);

    BaseFhirDataProvider provider = new DummyFhirDataProvider();
    context.registerDataProvider("http://hl7.org/fhir", provider);
    BaseFhirDataProvider provider1 = new DummyFhirDataProvider("org.hl7.davinci.stu3.fhirresources");
    context.registerDataProvider("http://hl7.org/fhir", provider1);

    for (Map.Entry<String, Resource> entry : cqlParams.entrySet()) {
      context.setParameter(null, entry.getKey(), entry.getValue());
    }
    return context;
  }

  public static String translateToElm(String cql) throws Exception {
    ModelManager modelManager = new ModelManager();
    LibraryManager libraryManager = new LibraryManager(modelManager);
    libraryManager.getLibrarySourceLoader().registerProvider(new FhirLibrarySourceProvider());

    ArrayList<CqlTranslator.Options> options = new ArrayList<>();
    options.add(CqlTranslator.Options.EnableDateRangeOptimization);

    UcumService ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
    CqlTranslator translator = CqlTranslator.fromText(cql, modelManager, libraryManager, ucumService, options.toArray(new CqlTranslator.Options[options.size()]));
    libraryManager.getLibrarySourceLoader().clearProviders();

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

    return translator.toJson();
  }

  private static Library translate(String cql, LibraryManager libraryManager, ModelManager modelManager) throws Exception {
    ArrayList<CqlTranslator.Options> options = new ArrayList<>();
    options.add(CqlTranslator.Options.EnableDateRangeOptimization);
    UcumService ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
    CqlTranslator translator = CqlTranslator.fromText(cql, modelManager, libraryManager, ucumService, options.toArray(new CqlTranslator.Options[options.size()]));
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
}
