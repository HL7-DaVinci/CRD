Coverage Requirements Discovery (CRD) Reference Implementation (RI)
===================================================================

The Coverage Requirements Discovery (CRD) Reference Implementation (RI) is an implementation of an open healthcare standard developed by the Da Vinci Project within the HL7 Standards Organization. The CRD RI will be published as a project under the HL7-DaVinci GitHub organization https://github.com/HL7-DaVinci.
 
The CRD RI is software that simulates a system at a healthcare payer organization. This system handles healthcare providers requests to understand what documentation is necessary prior to prescribing a particular treatment. Users are able to formulate a request for durable medical equipment coverage requirements, such as “what are the documentation requirements for prescribing home oxygen therapy (HCPCS E0424) to a 65 year old male living in MA?”. This type of question is not asked in plain English through a user interface, but submitted through a web service using the FHIR standard. The CRD RI consults a small, example database and provides a response, such as a PDF with the requirements back to the requesting system.
 
This software lets EHR vendors and payer organizations examine how the proposed standard will work and test their own implementations of the standard.

<h3>Building</h3>

> gradle build

<h3>Developing</h3>
<h4>Setup</h4>

* Install IntelliJ Idea
* Create new project from existing sources
* Choose the CRD folder
* Specify that it is a gradle project

<h4>Running</h4>

* Select the gradle tool window
* Choose CRD -> :server -> Tasks -> gretty -> appRun
* Run TestClient.main()