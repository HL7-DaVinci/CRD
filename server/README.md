# CRD RI Server

This subproject contains the CDS Service that can respond to CDS Hook requests. The application is written using [Spring Boot](https://spring.io/projects/spring-boot) and uses [HAPI FHIR](http://hapifhir.io/) for parsing and generating FHIR resources.

This RI offers very basic responses to CRD requests. It will use the patient's gender and age as well as the code passed in as a part of the request (from something like the DeviceRequest, ServiceRequest, or MedicationRequest) to consult a small `coverage_requirement_rules` table. This table allows a user to set up documentation requirement rules based on age, gender and code.

Users are able to create and edit entries in the coverage requirements rules through a web based interface. The goal of this functionality is to allow requesting systems to vary the payload of the request and see different response cards returned.

## Running the server

If this is the first time the server is being run you must first configure the application for file retrieval.

There are 2 approaches to do this, based on how you use the CDS Library:

<!-- prettier-ignore -->
|                 | Embeded CDS Library  | Separate CDS Library |
|-----------------|----------------------|----------------------|
| Most suited for | Production           | Local Development    |
| Description | [Default] This approach embeds the master branch of the CDS Libary into the CRD. _This is the easiest approach._ | This approach clones the CDS Library as a separate project. _This is the more flexible approach for developers._ |
| When to use | This is useful when you need only the latest version of the CDS Library or when you are developing CRD and not the CDS Library. | This is useful when you want to control which branch of the CDS Library you are using (e. g., if you need to test a pull request, or if you are actively developing rules for the CDS Library). |
| Directions  | Follow the directions in the section [Setup Directions for **Embedded** CDS Library](#Setup-Directions-for-Embedded-CDS-Library) | Follow the directions in the section [Setup Directions for **Separate** CDS Library](#Setup-Directions-for-Separate-CDS-Library) |

### Setup Directions for **Embedded** CDS Library

This is the easiest way to get started. You can optionally specify the branch otherwise it will default to use the `master` branch of CDS Library

1. go to `/server` subdirectory of CRD

   `cd server`

2. embed CDS Library:

	a. Default `master` branch:

   `gradle embedCdsLibrary`
   
   b. Specify `other` branch:
   
   `gradle embedCdsLibrary -Pbranch=other`

3. start the server:

   `gradle bootRun`

   This will start the server running on http://localhost:8090 with the CDS-Library at `server/CDS-Library`

### Setup Directions for **Separate** CDS Library

This is the more flexible approach for developers.

1.  go to a directory outside of the CDS directory (we will use `~/CDS-Library` as an example):

    `cd ~/CDS-Library`

2.  clone the CDS Library to the current working directory:

    `git clone https://github.com/HL7-DaVinci/CDS-Library.git`

3.  go back to the CRD directory (we will use `~/crd` as an example)

    `cd ~/crd`

4.  edit `application.yml` so that the following values are in place (using `~/CDS-Library` as an example)

        {
          spring:
            profiles:
              active: localDb
          localDb:
            path: ~/CDS-Library/
        }

5.  go to the server subdirectory of CRD:

    `cd server`

6.  start the server

    `gradle bootRun`

    This will start the server running on http://localhost:8090 with the CDS-Library at `~/CDS-Library`.

### Using ValueSets from VSAC

ValueSets from VSAC may be used by DTR rules. CRD will attempt to fetch these ValueSets from the VSAC SVS API and convert them to FHIR R4
ValueSets so they will be available for the DTR SMART App. While rulesets are being loaded from the CDS-Library, CRD will look for VSAC ValueSets referenced by OID and attempt to load them. (NOTE: It will not error out if fails to fetch ValueSets.) To load valuesets CRD needs NLM/VSAC credentials. These can be provided in two ways.

1.  Setting environment variable `VSAC_API_KEY`. Example in bash:

        $ export VSAC_API_KEY=vsac_api_key
    _Note: Single quotes means ignore special characters_

2.  Providing credentials when reloading rules from the rule listing UI at http://localhost:8090/data. The credentials can optionally be provided before hitting "Reload Data".

If the credentials are not provided, CRD will look for ValueSets in the `server/ValueSetCache` folder. This folder will be created if it doesn't exist when CRD is started. Its location can also be changed by modifying the `valueSetCachePath` in `application.yml`. When the server is able to retrieve ValueSets, it will fill the cache folder. This means that if you know your cache folder has all the valuesets that your rules need, you do not need to provide VSAC credentials at all.

## Server endpoints

<!-- prettier-ignore -->
| Relative URL                               | Endpoint Description|
|--------------------------------------------|---------------------|
| `/`                                        | Web page with basic RI information |
| `/data`                                    | Web-based administrative interface |
| `/cds-services/`                           | CDS Hook Discovery endpoint |
| `/cds-services/order-select-crd`           | CDS Hook endpoint for order-select |
| `/cds-services/order-sign-crd`             | CDS Hook endpoint for order-sign |
| `/files/`                                  | File retrieval endpoint |
| `/fhir/`                                   | Endpoint for retrieving FHIR Resources (Quesionnaire and Library) needed by DTR |
| `/reload`                                  | Rebuild the database of rules |
| `/fetchFhirUri/`                           | Used by the smart application to fetch fhir resources by URI |
| `/launch`                                  | Launch page for CRD SMART app |
| `/index`                                   | Index page for CRD SMART app |

## Configuration

As a Spring Boot application, configuration information is maintained in [application.yml](src/main/resources/application.yml).

### File Retrieval

The application can be configured to retrieve CQL files for running the CRD rules as well as support CQL files and FHIR Resources from a couple of different locations. These support files are now stored in a separate repository called [CDS-Library](https://github.com/HL7-DaVinci/CDS-Library).

#### Local Database

To access the files locally to where the server is running, the CDS-Library repository must be cloned. This can be done with `git clone` or by running the `gradle embedCdsLibrary` command from within the server folder. These will pull down the latest version found in master. The tool then needs to be configured to access the repository in the correct location with the `localDb` profile.

    application.yml:
    	{
    		spring:
    			profiles:
    				active: localDb
    		localDb:
    			path: CDS-Library/
    	}

#### GitHub

To access files directly in a GitHub repo without needing to clone or pull anything down, the application needs to be configured to use the `gitHub` profile. The token must be configured through GitHub.

    application.yml:
    	{
    		spring:
    			profiles:
    				active: gitHub
    		gitHubConfig:
    			username: user
    			token: token
    			repository: HL7-DaVinci/CDS-Library
    			branch: master
    			rule-path: .
    	}

##### Token Generation

To generate a GitHub token, follow these steps and place your token into the token field above along with your GitHub username.

1. Log into [GitHub](http://www.github.com)
2. Go to personal settings by choosing your avatar in the top right and choosing “Settings”
3. Select “Developer settings” on the bottom left
4. Choose “Personal Access Tokens” on the left
5. Select “Generate new token”
6. Log into GitHub with your password
7. Select the checkboxes next to “repo” and “read:packages”
8. Press “Geneate token”
9. There will be a token displayed that you must copy and save for use with the application, I don’t believe there is any way to have the token shown again. If you lose it you will have to revoke the other one and create another.


#### CDS Connect

To access files directly on CDS Connect, the application.yml configuration file must be configured with the proper credentials.

    application.yml:
    	{
    		spring:
    			profiles:
    				active: cdsConnect
    		cdsConnect:
	    		url: https://cds.ahrq.gov
    			username: user
    			password: pass
    	}

##### Topic / Artifact Configuration

Each of the Rules must be stored in their own topic. The name of the artifact shall be the name of the topic. The payer must be configured with the code and codesystem within the CRD section must also be configured with the topic name. The TopicMetadata must be pasted into the "Artifact Representation" Inclusions box. First set the editor to "source" so most formatting will be removed. The files and resources will be attached to the "Logic Files" within the "Artifact Representation". All common, shared files must be attached to another artifact named "Shared". It shall have simple TopicMetadata in the Inclusions field as simply as:

	{ "topic": shared", "fhirVersions": [ "R4" ] }
	
Files must be named as described in the documentation for the [CDS-Library](https://github.com/HL7-DaVinci/CDS-Library).

## Security

The server is protected with JSON Web Tokens (JWT), an industry standard for authentication. Tokens are generated in the `request-generator` using SHA256 for hashing and RSA-2048 encryption. The tokens DO NOT encrypt the data that is sent to the server, they simply provide a signature that can be verified using a public key. The tokens are only used to verify that the request comes from a trusted source.

To do this, the signature of the token is decoded with a public key. This key should be available at some endpoint from the issuer of the token. The server will save any public key that it uses to verify a signature with. JWTs are not sent with the public key inside the JSON, instead the public key is retrieved from whatever URL is specified in the `jku` section of the token. The server will issue a `GET` request to the endpoint using the key's ID, which is specified in the `kid` section of the token. The `GET` request will be to the url `jku/kid`, meaning that it expects the public key it needs to be locatable by the server using the ID at the base url. In other words, the server will not search through JSON to find the correct public key, it expects the endpoint specified in the `jku` to be able to return the correct key when appended with `/kid`, where `kid` is replaced with whatever KeyID was send in the JWT.

The key should be returned to the server as a JSON object with the algorithm type, exponent, and modulus as fields. It should be of the form:
`{kty:"RSA",n:<modulus>,e:<exponent>}`

Any new key the server encounters is saved into a database and can be referenced again if another request with the same `kid` comes in. This allows the server to operate without recieving a `jku` from every request and performing the `GET` request.

After the public key is acquired, it is used to verify the signature of the token. If the signature is successful, the request is allowed to go through. If the signature is rejected, the request is rejected, but the token is still saved to the servers database.

This functionality can be turned off and on by changing the `checkJWt` property in [application.yml](src/main/resources/application.yml). Setting it to false will cause the server to automatically approve all incoming requests.

## Building Web Apps (the UI)

To edit the web apps, the code in `src/main/resources/node_interfaces` should be updated. These changes will not be reflected by the server. To build the jsx files, run `npm run-script buildx`. This command will run a bash script that will automatically build the files out and move them to the correct directory to be hosted by the spring server.

You can also run the gradle task `buildReact` in the `server` directory to do the same thing. Alternatively, running the bash script itself using `./buildout.sh` with `node_interfaces` as a working directory.

## The embedded CDS-Library

As described above, the CDS-Library can be cloned from github for embedded use with the "localDb". To clone with a different branch named some-other-branch:

    CRD$ gradle :server:embedCdsLibrary -Pbranch=some-other-branch

To update the embedded CDS-Library repository:

    CRD$ gradle :server:updateCdsLibrary

To switch branches on the already cloned CDS-Library repository:

    CRD$ gradle :server:updateCdsLibrary -Pbranch=some-other-branch

## The integrated smart app

The CRD server can embed the DTR smart application from https://github.com/HL7-DaVinci/dtr.
Run the gradle task `embedDtr` to automatically clone the repo (master) and build into the appropriate location. This task must be manually run to pull down new versions of DTR.

Once done, the application can be accessed at (e.g.) `localhost:8090/smart/index.html`. This files are in `src/main/resources/static/smart` and should be commited. Note that files should be accessed by their full name, (e.g. `smart/index.html` vs `smart/`).

## The CRD Smart App

The CRD server has a built in standalone SMART app which lets the user make CRD requests without going through a workflow.  The standalone smart app is accessed at the `/launch` page.

To launch the app, visit `/launch` and input an `iss`, or FHIR server.  If the CRD standalone app is not yet registered with the FHIR server, you can get a client ID by registering it.  Otherwise, you should have a client ID for the specific FHIR server for this app.

After the `iss` and `client_id` are input, click launch and wait go through any authorization prompts. After, the SMART app will load.  The app allows users to generate a CDS hook by inputting simple information, like patient age and gender or location.  The CRD server will respond normally to these generated hooks, allowing users to test what-if scenarios.



