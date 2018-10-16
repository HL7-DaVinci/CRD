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

## Security
The server is protected with JSON Web Tokens (JWT), an industry standard for authentication.  Tokens are generated in the `request-generator` using SHA256 for hashing and RSA-2048 encryption.  The tokens DO NOT encrypt the data that is sent to the server, they simply provide a signature that can be verified using a public key.  The tokens are only used to verify that the request comes from a trusted source. 

To do this, the signature of the token is decoded with a public key.  This key should be available at some endpoint from the issuer of the token.  The server will save any public key that it uses to verify a signature with.  JWTs are not sent with the public key inside the JSON, instead the public key is retrieved from whatever URL is specified in the `jku` section of the token.  The server will issue a `GET` request to the endpoint using the key's ID, which is specified in the `kid` section of the token.  The `GET` request will be to the url `jku/kid`, meaning that it expects the public key it needs to be locatable by the server using the ID at the base url.  In other words, the server will not search through JSON to find the correct public key, it expects the endpoint specified in the `jku` to be able to return the correct key when appended with `/kid`, where `kid` is replaced with whatever KeyID was send in the JWT.  

The key should be returned to the server as a JSON object with the algorithm type, exponent, and modulus as fields.  It should be of the form:
`{kty:"RSA",n:<modulus>,e:<exponent>}`

Any new key the server encounters is saved into a database and can be referenced again if another request with the same `kid` comes in.  This allows the server to operate without recieving a `jku` from every request and performing the `GET` request.  

After the public key is acquired, it is used to verify the signature of the token.  If the signature is successful, the request is allowed to go through.  If the signature is rejected, the request is rejected, but the token is still saved to the servers database.  

This functionality can be turned off and on by changing the `checkJWt` property in [application.yml](src/main/resources/application.yml).  Setting it to false will cause the server to automatically approve all incoming requests.  

## Building Web Apps

React based pages like the `/public` and `/requests` must be built into static javascript and css to be served.

The react .jsx code should be edited directly in their respective folders in `/server/resources` and then be built.
To build them, follow these steps:

1.  Navigate into the desired directory in a terminal or command prompt
2.  Run `npm run build`
3.  Transfer the .js and .css files to the static folder
4.  Change the respective .html file to reference the new files




