package org.hl7.davinci.endpoint.cdshooks.services.crd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cdshooks.*;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.endpoint.config.YamlConfig;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.r4.crdhook.DiscoveryExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public abstract class CdsAbstract<requestTypeT extends CdsRequest<?, ?>>{
    static final Logger logger = LoggerFactory.getLogger(CdsAbstract.class);

    /**
     * The {id} portion of the URL to this service which is available at
     * {baseUrl}/cds-services/{id}. REQUIRED
     */
    public String id;

    /**
     * The hook this service should be invoked on. REQUIRED
     */
    public Hook hook;

    /**
     * The human-friendly name of this service. RECOMMENDED
     */
    public String title;

    /**
     * The description of this service. REQUIRED
     */
    public String description;

    /**
     * An object containing key/value pairs of FHIR queries that this service is
     * requesting that the EHR prefetch and provide on each service call. The key is
     * a string that describes the type of data being requested and the value is a
     * string representing the FHIR query. OPTIONAL
     */
    public Prefetch prefetch;

    private final List<PrefetchTemplateElement> prefetchElements;

    protected FhirComponentsT fhirComponents;

    private final DiscoveryExtension extension;

    @Autowired
    @JsonIgnore
    public YamlConfig myConfig;

    public CdsAbstract(String id, Hook hook, String title, String description,
                      List<PrefetchTemplateElement> prefetchElements, FhirComponentsT fhirComponents,
                      DiscoveryExtension extension) {

        if (id == null) {
            throw new NullPointerException("CDSService id cannot be null");
        }
        if (hook == null) {
            throw new NullPointerException("CDSService hook cannot be null");
        }
        if (description == null) {
            throw new NullPointerException("CDSService description cannot be null");
        }
        this.id = id;
        this.hook = hook;
        this.title = title;
        this.description = description;
        this.prefetchElements = prefetchElements;
        prefetch = new Prefetch();
        for (PrefetchTemplateElement prefetchElement : prefetchElements) {
            this.prefetch.put(prefetchElement.getKey(), prefetchElement.getQuery());
        }
        this.fhirComponents = fhirComponents;
        this.extension = extension;
    }

    public DiscoveryExtension getExtension() { return extension; }

    public List<PrefetchTemplateElement> getPrefetchElements() {
        return prefetchElements;
    }

    protected Link smartLinkBuilder(String patientId, String fhirBase, URL applicationBaseUrl, String questionnaireUri,
                                  String reqResourceId, CoverageRequirementRuleCriteria criteria, boolean priorAuthRequired, String label) {
        URI configLaunchUri = myConfig.getLaunchUrl();
        questionnaireUri = applicationBaseUrl + "/fhir/r4/" + questionnaireUri;

        String launchUrl;
        if (myConfig.getLaunchUrl().isAbsolute()) {
            launchUrl = myConfig.getLaunchUrl().toString();
        } else {
            try {
                launchUrl = new URL(applicationBaseUrl.getProtocol(), applicationBaseUrl.getHost(),
                        applicationBaseUrl.getPort(), applicationBaseUrl.getFile() + configLaunchUri.toString(), null).toString();
            } catch (MalformedURLException e) {
                String msg = "Error creating smart launch URL";
                logger.error(msg);
                throw new RuntimeException(msg);
            }
        }

        if (fhirBase != null && fhirBase.endsWith("/")) {
            fhirBase = fhirBase.substring(0, fhirBase.length() - 1);
        }
        if (patientId != null && patientId.startsWith("Patient/")) {
            patientId = patientId.substring(8);
        }

        // PARAMS:
        // template is the uri of the questionnaire
        // request is the ID of the device request or medrec (not the full URI like the
        // IG says, since it should be taken from fhirBase

        String filepath = "../../getfile/" + criteria.getQueryString();

        String appContext = "template=" + questionnaireUri + "&request=" + reqResourceId;
        appContext = appContext + "&fhirpath=" + applicationBaseUrl + "/fhir/";

        appContext = appContext + "&priorauth=" + (priorAuthRequired ? "true" : "false");
        appContext = appContext + "&filepath=" + applicationBaseUrl + "/";
        if (myConfig.getUrlEncodeAppContext()) {
            logger.info("CdsService::smartLinkBuilder: URL encoding appcontext");
            appContext = URLEncoder.encode(appContext, StandardCharsets.UTF_8).toString();
        }

        logger.info("smarLinkBuilder: appContext: " + appContext);

        if (myConfig.isAppendParamsToSmartLaunchUrl()) {
            launchUrl = launchUrl + "?iss=" + fhirBase + "&patientId=" + patientId + "&template=" + questionnaireUri
                    + "&request=" + reqResourceId;
        } else {
            // TODO: The iss should be set by the EHR?
            launchUrl = launchUrl;
        }

        Link link = new Link();
        link.setType("smart");
        link.setLabel(label);
        link.setUrl(launchUrl);

        link.setAppContext(appContext);

        return link;
    }

    public abstract CdsResponse handleRequest(requestTypeT request, URL applicationBaseUrl);
}
