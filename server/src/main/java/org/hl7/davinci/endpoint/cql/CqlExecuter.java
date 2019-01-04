package org.hl7.davinci.endpoint.cql;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.execution.ExpressionDef;
import org.cqframework.cql.elm.execution.Library;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.opencds.cqf.cql.execution.Context;
import org.opencds.cqf.cql.execution.CqlLibraryReader;
import org.opencds.cqf.cql.execution.Variable;

import org.hl7.davinci.endpoint.database.CoverageRequirementRuleCriteria;


import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CqlExecuter {
  String rule;
  Library library = null;
  Context context = null;

  public CqlExecuter(String rule) {
    this.rule = rule;

    ArrayList<String> errors = new ArrayList<String>();

    try {
      // First try and parse the input as ELM.
      System.out.println("CqlExecuter::CqlExecuter: Trying to parse as ELM...");
      library = CqlLibraryReader.read(new ByteArrayInputStream(
          rule.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      System.out.println("CqlExecuter::CqlExecuter:  Trying to parse as CQL...");
      // Parsing the input as ELM failed; try and parse it as CQL.
      String elm = maybeCqlToElm(rule, errors);
      if (elm == null) {
        errors.add("The source code failed to parse either as ELM or as CQL.");
        errors.add("The prior errors were during the attempt to parse as CQL.");
        errors.add("The next errors were during the attempt to parse as ELM:");
        errors.add(e.toString());
        return;
      }
      try {
        library = CqlLibraryReader.read(new ByteArrayInputStream(
            elm.getBytes(StandardCharsets.UTF_8)));
      } catch (Exception e2) {
        errors.add("Translation of CQL to ELM succeeded; however"
            + " parsing that ELM failed due to errors:");
        errors.add(e2.toString());
      }
    }

    context = new Context(library);

    /*
    printDefine("Age Range Low");
    printDefine("Age Range High");
    printDefine("Gender Code");
    printDefine("Patient Address State");
    printDefine("Provider Address State");
    printDefine("Equipment Code");
    printDefine("Code System");
    printDefine("No Auth Needed");
    printDefine("Info Link");
    */

    errors.forEach((error)->System.out.println(error));
  }

  private void printDefine(String define) {
    ExpressionDef ref = context.resolveExpressionRef(define);
    System.out.println(ref.getName() + ": " + ref.evaluate(context).toString());
  }

  private static void addVariable(Context context, String name, Object value) {
    Variable var = new Variable();
    var.setName(name);
    var.setValue(value);
    context.push(var);
  }

  public CqlRule execute(CoverageRequirementRuleCriteria criteria) {
    CqlRule rule = null;

    try {
      System.out.println("CqlExecuter::execute: " + criteria.toString());

      addVariable(context, "age", criteria.getAge());
      addVariable(context, "gender", ((Character) criteria.getGenderCode()).toString());
      addVariable(context, "patientState", criteria.getPatientAddressState());
      addVariable(context, "providerState", criteria.getProviderAddressState());
      addVariable(context, "equipmentCode", criteria.getEquipmentCode());
      addVariable(context, "codeSystem", criteria.getCodeSystem());

      Boolean match = (Boolean) context.resolveExpressionRef("Applies").getExpression().evaluate(context);

      String link = context.resolveExpressionRef("Info Link").evaluate(context).toString();
      Boolean required = (Boolean) context.resolveExpressionRef("Documentation Required").evaluate(context);

      // finally output result of rule
      if (match) {
        rule = new CqlRule();

        rule.setInfoLink(link);
        rule.setNoAuthNeeded(!required);
        rule.setAgeRangeLow((int) context.resolveExpressionRef("Age Range Low").getExpression().evaluate(context));
        rule.setAgeRangeHigh((int) context.resolveExpressionRef("Age Range High").getExpression().evaluate(context));

        String genderString = context.resolveExpressionRef("Gender Code").getExpression().evaluate(context).toString();
        Character genderCode = null;
        if (!genderString.isEmpty()) {
          genderCode = genderString.charAt(0);
        }
        rule.setGenderCode(genderCode);

        rule.setEquipmentCode(context.resolveExpressionRef("Equipment Code").getExpression().evaluate(context).toString());
        rule.setCodeSystem(context.resolveExpressionRef("Code System").getExpression().evaluate(context).toString());
        rule.setPatientAddressState(context.resolveExpressionRef("Patient Address State").getExpression().evaluate(context).toString());
        rule.setProviderAddressState(context.resolveExpressionRef("Provider Address State").getExpression().evaluate(context).toString());

        if (required) {
          System.out.println("CqlExecuter::execute: Documentation Required: " + link);
        } else {
          System.out.println("CqlExecuter::execute: Documentation Not Required");
        }
      } else {
        System.out.println("CqlExecuter::execute: Did not match rule");
      }
    } catch (IllegalArgumentException e) {
      System.out.println("CqlExecuter::execute: ERROR: " + e.getMessage());
    }

    return rule;
  }

  private static String maybeCqlToElm(String maybeCql, ArrayList<String> errors) {
    ModelManager modelManager = new ModelManager();
    LibraryManager libraryManager = new LibraryManager(modelManager);

    ArrayList<CqlTranslator.Options> options = new ArrayList<>();
    options.add(CqlTranslator.Options.EnableDateRangeOptimization);

    CqlTranslator translator = CqlTranslator.fromText(
        maybeCql, modelManager, libraryManager,
        options.toArray(new CqlTranslator.Options[options.size()]));

    if (translator.getErrors().size() > 0) {
      if (errors != null) {
        collectErrors(errors, translator.getErrors());
      }
      return null;
    }

    String elm = translator.toXml();

    if (translator.getErrors().size() > 0) {
      if (errors != null) {
        collectErrors(errors, translator.getErrors());
      }
      return null;
    }

    return elm;
  }

  private static void collectErrors(ArrayList<String> errors,
                                    Iterable<CqlTranslatorException> exceptions) {
    errors.add("Translation of CQL to ELM failed due to errors:");
    for (CqlTranslatorException error : exceptions) {
      TrackBack tb = error.getLocator();
      String lines = tb == null ? "[n/a]" : String.format(
          "%s[%d:%d, %d:%d]",
          (tb.getLibrary() == null ? ""
              : tb.getLibrary().getId()
              + (tb.getLibrary().getVersion() == null ? ""
              : "-" + tb.getLibrary().getVersion()
          )
          ),
          tb.getStartLine(), tb.getStartChar(),
          tb.getEndLine(), tb.getEndChar()
      );
      errors.add(lines + error.getMessage());
    }
  }
}
