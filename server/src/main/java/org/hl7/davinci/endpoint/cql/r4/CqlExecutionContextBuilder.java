package org.hl7.davinci.endpoint.cql.r4;

import ca.uhn.fhir.context.FhirContext;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.Library;
import org.hl7.davinci.endpoint.cql.CqlExecution;
import org.hl7.davinci.endpoint.cql.LocalLibraryLoader;
import org.hl7.davinci.endpoint.cql.CqlRule;
import org.hl7.fhir.r4.model.Resource;

import org.opencds.cqf.cql.engine.data.CompositeDataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;
import org.opencds.cqf.cql.engine.fhir.retrieve.RestFhirRetrieveProvider;
import org.opencds.cqf.cql.engine.fhir.searchparam.SearchParameterResolver;

import java.util.HashMap;
import java.util.Map;

public class CqlExecutionContextBuilder {

  public static String CQL_VERSION = "4.0.0";

  public static Context getExecutionContext(CqlRule cqlRule, HashMap<String, Resource> cqlParams, String baseUrl) {
    ModelManager modelManager = new ModelManager();
    LibraryManager libraryManager = new LibraryManager(modelManager);
    libraryManager.getLibrarySourceLoader().clearProviders();

    Library library = null;
    LibraryLoader libraryLoader = null;

    if (cqlRule.isPrecompiled()) {
      //todo
    } else {
      libraryManager.getLibrarySourceLoader().registerProvider(cqlRule.getRawCqlLibrarySourceProvider(CQL_VERSION));
      libraryLoader = new LocalLibraryLoader(libraryManager);
      try {
        library = CqlExecution.translate(cqlRule.getRawMainCqlLibrary(CQL_VERSION), libraryManager, modelManager);
      } catch (Exception e){
        throw new RuntimeException(e);
      }
    }

    FhirContext fhirContext = FhirContext.forR4();
    Context context = new Context(library);
    context.registerLibraryLoader(libraryLoader);
    context.setExpressionCaching(true);

    R4FhirModelResolver modelResolver = new R4FhirModelResolver();
    RestFhirRetrieveProvider retrieveProvider = new RestFhirRetrieveProvider(new SearchParameterResolver(fhirContext), fhirContext.newRestfulGenericClient("http://fhirtest.uhn.ca/baseR4"));
    CompositeDataProvider provider = new CompositeDataProvider(modelResolver, retrieveProvider);
    context.registerDataProvider("http://hl7.org/fhir", provider);

    for (Map.Entry<String, org.hl7.fhir.r4.model.Resource> entry : cqlParams.entrySet()) {
      context.setParameter(null, entry.getKey(), entry.getValue());
    }

    context.setParameter(null, "base_url", baseUrl);

    return context;
  }
}
