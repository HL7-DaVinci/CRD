# CRD RI Server
This subproject contains the CDS Service that can respond to CDS Hook requests. The application is written using [Spring Boot](https://spring.io/projects/spring-boot) and uses [HAPI FHIR](http://hapifhir.io/) for parsing and generating FHIR resources.

This RI offers very basic responses to CRD requests. It will use the patient's gender and age as well as the code passed in as a part of the request (from something like the DeviceRequest or MedicationRequest) to consult a small `coverage_requirement_rules` table. This table allows a user to set up documentation requirement rules based on age, gender and code.

Users are able to create and edit entries in the coverage requirements rules through a web based interface. The goal of this functionality is to allow requesting systems to vary the payload of the request and see different response cards returned.

## Seting up the database
1. Install and run postgresql
1. Clone the repo
  * `git clone https://github.com/HL7-DaVinci/CRD.git`
1. Run the gradle task, it will create a database named 'dme' and populate it with a rules table, and the sample rules will be loaded from the csv file.
  * `cd CRD/server`
  * `gradle setupDb`

This task can also be run to reset the data in the table to match the csv.

## Running the server
Assuming the current directory is still `server`:

`gradle bootRun`

This will start the server running on http://localhost:8090.

## Server endpoints
|Relative URL|Endpoint Description|
|----|----|
|`/`|Web page with basic RI information|
|`/data`|Web-based administrative interface|
|`/cds-services/`|CDS Hook Discovery endpoint|
|`/cds-services/order-review-crd`|CDS Hook endpoint for order-review|
|`/cds-services/medication-prescribe-crd`|CDS Hook endpoint for medication-prescribe|

## Configuration
As a Spring Boot application, configuration information is maintained in [application.yml](src/main/resources/application.yml).
