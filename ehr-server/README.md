# EHR FHIR Server
This subproject hosts a HAPI FHIR server that is based on the [hapi-fhir-jpaserver-example](https://github.com/jamesagnew/hapi-fhir/tree/master/hapi-fhir-jpaserver-example).

## Setup the Request Generator
The request generator can be setup automatically with a gradle task:
1. Navigate to the `/ehr-server` folder in a terminal
2. Run `gradle loadRequestBuilder`, this operation should take about a minute

To setup the request generator manually:
1. Download the code from [request generator repository](https://github.com/HL7-DaVinci/crd-request-generator)
2. Navigate to the directory containing the request generator code
3. Run `npm install`
4. Run `npm run-script build`
5. Move the build folder that gets created into `ehr-server/src/main/webapp/WEB-INF/`

The request generator will run when the server runs.

## Running the server
`gradle appRun`

This will start the server running on http://localhost:8080/ehr-server.

## Adding resources to the database
The FHIR server will persist FHIR resources between restarts. You can delete the folder `target` to clear all resources.

To load the data from the json files in fhirResourcesToLoad, run the following script:  
`gradle loadData` (this must be run while the FHIR server is running and `use_oauth` is false in `ehr-server/src/main/resources/fhirServer.properties`)

## Server endpoints
|Relative URL|Endpoint Description|
|----|----|
|`/`|Web page with basic RI information|
|`/ehr-server/`|Base server endpoint|
|`/ehr-server/stu3`|EHR FHIR Server endpoint|
|`/ehr-server/reqgen`|Request Generator|

