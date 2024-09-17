package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.components.QueryBatchRequest;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.crdhook.encounterstart.EncounterStartRequest;
import org.opencds.cqf.cql.engine.execution.Context;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("r4_EncounterStartService")
public class EncounterStartService extends CdsService<EncounterStartRequest> {
    @Override
    public List<CoverageRequirementRuleResult> createCqlExecutionContexts(EncounterStartRequest request, FileStore fileStore, String baseUrl) throws RequestIncompleteException {
        return null;
    }

    @Override
    protected CardBuilder.CqlResultsForCard executeCqlAndGetRelevantResults(Context context, String topic) {
        return null;
    }

    @Override
    protected void attemptQueryBatchRequest(EncounterStartRequest request, QueryBatchRequest qbr) {

    }
}
