# CRD RI Server
This subproject contains the CDS Service that can respond to CDS Hook requests. The application is written using [Spring Boot](https://spring.io/projects/spring-boot) and uses [HAPI FHIR](http://hapifhir.io/) for parsing and generating FHIR resources.

This RI offers very basic responses to CRD requests. It will use the patient's gender and age as well as the code passed in as a part of the request (from something like the DeviceRequest or MedicationRequest) to consult a small `coverage_requirement_rules` table. This table allows a user to set up documentation requirement rules based on age, gender and code.

Users are able to create and edit entries in the coverage requirements rules through a web based interface. The goal of this functionality is to allow requesting systems to vary the payload of the request and see different response cards returned.

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
|`/fetchFhirUri/`|Used by the smart application to fetch fhir resources by URI|

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

## Building Web Apps (the UI)

To edit the web apps, the code in `src/main/resources/node_interfaces` should be updated.  These changes will not be reflected by the server.  To build the jsx files, run `npm run-script buildx`.  This command will run a bash script that will automatically build the files out and move them to the correct directory to be hosted by the spring server.

You can also run the gradle task `buildReact` in the `server` directory to do the same thing.  Alternatively, running the bash script itself using `./buildout.sh` with `node_interfaces` as a working directory.


## The integrated smart app

The CRD server can embed the DTR smart application from https://github.com/HL7-DaVinci/dtr.
Run the gradle task `embedDtr` to automatically clone the repo (master) and build into the appropriate location. This task must be manually run to pull down new versions of DTR.

Once done, the application can be accessed at (e.g.) `localhost:8090/smart/index.html`. This files are in `src/main/resources/static/smart` and should be commited. Note that files should be accessed by their full name, (e.g. `smart/index.html` vs `smart/`).


## Integration Testing

Integration tests will set up an actual running instance of this server as well as `ehr-server` to monitor a the handling of requests from beginning to end.

Integration tests can be run with `gradle integrationTest`.

