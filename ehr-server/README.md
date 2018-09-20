# EHR FHIR Server
This subproject hosts a HAPI FHIR server that is based on the [hapi-fhir-jpaserver-example](https://github.com/jamesagnew/hapi-fhir/tree/master/hapi-fhir-jpaserver-example).

## Adding resources to the database


## Running the server
`gradle appRun`

This will start the server running on http://localhost:8080/ehr-server.

## Server endpoints
|Relative URL|Endpoint Description|
|----|----|
|`/`|Web page with basic RI information|
|`/ehr-server/`|Base server endpoint|
|`/ehr-server/r4`|EHR FHIR Server endpoint|

