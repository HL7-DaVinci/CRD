package org.hl7.davinci.endpoint.cql.r4;

import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.Library;
import org.hl7.davinci.endpoint.cql.CqlExecution;
import org.hl7.davinci.endpoint.cql.LocalLibraryLoader;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundle;
import org.hl7.fhir.r4.model.Resource;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.execution.Context;
import org.opencds.cqf.cql.execution.LibraryLoader;

import java.util.HashMap;
import java.util.Map;

public class CqlExecutionContextBuilder {

  public static String CQL_VERSION = "4.0.0";

  public static Context getExecutionContext(CqlBundle cqlPackage, HashMap<String, Resource> cqlParams) {
    ModelManager modelManager = new ModelManager();
    LibraryManager libraryManager = new LibraryManager(modelManager);
    libraryManager.getLibrarySourceLoader().clearProviders();

    Library library = null;
    LibraryLoader libraryLoader = null;

    if (cqlPackage.isPrecompiled()){
      //todo
    } else {
      libraryManager.getLibrarySourceLoader().registerProvider(cqlPackage.getRawCqlLibrarySourceProvider(CQL_VERSION));
      libraryLoader = new LocalLibraryLoader(libraryManager);
      try {
        library = CqlExecution.translate(cqlPackage.getRawMainCqlLibrary(CQL_VERSION), libraryManager, modelManager);
      } catch (Exception e){
        throw new RuntimeException(e);
      }
    }

    Context context = new Context(library);
    context.registerLibraryLoader(libraryLoader);
    context.setExpressionCaching(true);

    BaseFhirDataProvider provider = new DummyFhirDataProvider();
    context.registerDataProvider("http://hl7.org/fhir", provider);
    BaseFhirDataProvider provider1 = new DummyFhirDataProvider("org.hl7.davinci.r4.fhirresources");
    context.registerDataProvider("http://hl7.org/fhir", provider1);

    for (Map.Entry<String, org.hl7.fhir.r4.model.Resource> entry : cqlParams.entrySet()) {
      context.setParameter(null, entry.getKey(), entry.getValue());
    }
    return context;
  }
}
