package org.hl7.davinci.endpoint.cql;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import javax.xml.bind.JAXBException;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.FhirLibrarySourceProvider;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumService;
import org.opencds.cqf.cql.engine.execution.CqlLibraryReader;


public class CqlExecution {

  public static String translateToElm(String cql, LibrarySourceProvider librarySourceProvider) throws Exception {
    ModelManager modelManager = new ModelManager();
    LibraryManager libraryManager = new LibraryManager(modelManager);
    libraryManager.getLibrarySourceLoader().registerProvider(new FhirLibrarySourceProvider());
    if (librarySourceProvider != null) {
      libraryManager.getLibrarySourceLoader().registerProvider(librarySourceProvider);
    }

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

  public static String translateToElm(String cql) throws Exception {
    return translateToElm(cql, null);
  }

  public static Library translate(String cql, LibraryManager libraryManager, ModelManager modelManager) throws Exception {
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
