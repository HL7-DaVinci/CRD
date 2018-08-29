# Coverage Requirements Discovery (CRD) Reference Implementation (RI)

The Coverage Requirements Discovery (CRD) Reference Implementation (RI) is an implementation of an open healthcare standard developed by the Da Vinci Project within the HL7 Standards Organization. The CRD RI will be published as a project under the HL7-DaVinci GitHub organization https://github.com/HL7-DaVinci.
 
The CRD RI is software that simulates a system at a healthcare payer organization. This system handles healthcare providers requests to understand what documentation is necessary prior to prescribing a particular treatment. Users are able to formulate a request for durable medical equipment coverage requirements, such as “what are the documentation requirements for prescribing home oxygen therapy (HCPCS E0424) to a 65 year old male living in MA?”. This type of question is not asked in plain English through a user interface, but submitted through a web service using the FHIR standard. The CRD RI consults a small, example database and provides a response, such as a PDF with the requirements back to the requesting system.
 
This software lets EHR vendors and payer organizations examine how the proposed standard will work and test their own implementations of the standard.

## Building and running from the command line
1. Clone the repo
	* `git clone https://github.com/HL7-DaVinci/CRD.git`
1. Install dependencies (gradle, postgresql)
1. Build the code
	* `gradle build`
1. Running from the command line
	* Run the fhir server
		* `gradle :server:bootRun`
	* Run the testing client
		* `gradle :testingClient:run`
	* Run the creator tool
		* `gradle :creator:run -PappArgs="['/tmp/']"`

## Seting up the DB
1. Install and run postgresql
1. Run the gradle task, it will create a database named 'dme' and populate it with a rules table, and the sample rules will be loaded from the csv file.
  * `gradle setupDb` 

This task can also be run to reset the data in the table to match the csv.

## Setting up a KeyCloak instance
1. Download and unzip KeyCloak Server from [here](https://www.keycloak.org/downloads.html)
2. From command line navigate to the directory KeyCloak was downloaded to and then type `unzip keycloak-<Version>.Final.zip` followed by `cd keycloak-<Version>.Final/bin`
3. Run `./standalone.sh -Djboss.socket.binding.port-offset=100` from command line to start the server.  It should run on port 8180
4. Navigate to the KeyCloak instance in a browser, it should be at [http://localhost:8180/](http://localhost:8180)
5. When prompted, create a new administrative username and password.
6. Create two realms, one for the client fhir server, and one for the CRD implementation, choose any name.  Both realms follow the same following steps, the only difference should be the names of the clients
7. Make two clients by navigating to the `Clients` tab
	* Make one client public and the other bearer-only with the `Access-type` dropdown.
	* Find the `Web Origins` input and add the address of the client fhir server and the CRD server.  Alternatively just 	       put `*` in `Web Origins` if running everything locally.
	* Add a redirect URL, can generally work fine as the base url of the server such as [http://localhost:8080](http://localhost:8080)
8. Navigate to the `Roles` tab and make a new role called `user`
	* Navigate to the `Users` tab and make a new user.  
	* Give the new user a password in the `credentials` tab
	* Go to `Role Mappings` and add the `user` role
9. Modify config files to point at the your new clients and realms
	* Change `server/resources/application.yml` keycloak settings to point at the bearer only client in one of the realms
	* Change `fhir-server/resources/fhirServer.properties` to have the client ID and secret of the bearer only client in the second realm
	* Change `request-generator/src/properties.json` to include the second realm and public client ID

## Developing
### Setup

1. Install IntelliJ Idea
2. Create new project from existing sources
3. Choose the CRD folder
4. Specify that it is a gradle project

### Running from the UI

* Select the gradle tool window
* Choose CRD --> :server --> Tasks --> application --> bootRun
* Run org.hl7.davinci.testClient.TestClient.main()
