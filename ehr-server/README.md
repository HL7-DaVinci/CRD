# EHR FHIR Server
This subproject hosts a HAPI FHIR server that is based on the [hapi-fhir-jpaserver-example](https://github.com/jamesagnew/hapi-fhir/tree/master/hapi-fhir-jpaserver-example).

## Setup the Request Generator
1. See the Request Generator README to build the Request Generator.
2. Create a symbolic link to the Request Generator
  * `cd ehr-server/src/main/webapp/WEB-INF/`
  * `ln -s ../../../../../request-generator/build reqgen`

## Running the server
`gradle appRun`

This will start the server running on http://localhost:8080/ehr-server.

## Adding resources to the database
`gradle loadData`

## Server endpoints
|Relative URL|Endpoint Description|
|----|----|
|`/`|Web page with basic RI information|
|`/ehr-server/`|Base server endpoint|
|`/ehr-server/r4`|EHR FHIR Server endpoint|
|`/ehr-server/reqgen`|Request Generator|

