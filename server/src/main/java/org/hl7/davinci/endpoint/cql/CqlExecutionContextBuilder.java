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
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumService;
import org.hl7.fhir.dstu3.model.Resource;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.execution.Context;
import org.opencds.cqf.cql.execution.CqlLibraryReader;


public class CqlExecutionContextBuilder {

  public static Context getExecutionContextStu3(String cql, HashMap<String,Resource> cqlParams) {
    return new CqlExecutionContextBuilder().buildExecutionContextStu3(cql, cqlParams);
  }

  public CqlExecutionContextBuilder() {}

  private Context buildExecutionContextStu3(String cql, HashMap<String,Resource> cqlParams) {
    Library library = null;

    try {
      library = translate(cql);
    } catch (Exception e){
      throw new RuntimeException(e);
    }

    Context context = new Context(library);
    context.setExpressionCaching(true);
    context.registerLibraryLoader(getLibraryLoader());

    BaseFhirDataProvider provider = new DummyFhirDataProvider();
    context.registerDataProvider("http://hl7.org/fhir", provider);
    BaseFhirDataProvider provider1 = new DummyFhirDataProvider("org.hl7.davinci.stu3.fhirresources");
    context.registerDataProvider("http://hl7.org/fhir", provider1);

    for (Map.Entry<String, Resource> entry : cqlParams.entrySet()) {
      context.setParameter(null, entry.getKey(), entry.getValue());
    }
    return context;
  }

  private Library translate(String cql) throws Exception {
    ArrayList<CqlTranslator.Options> options = new ArrayList<>();
    options.add(CqlTranslator.Options.EnableDateRangeOptimization);
    UcumService ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
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
      libraryManager.getLibrarySourceLoader().registerProvider(new LocalLibrarySourceProvider());
    }
    return libraryManager;
  }

  private org.opencds.cqf.cql.execution.LibraryLoader libraryLoader;
  private org.opencds.cqf.cql.execution.LibraryLoader getLibraryLoader() {
    if (libraryLoader == null) {
      libraryLoader = new LocalLibraryLoader(libraryManager);
    }
    return libraryLoader;
  }
}
