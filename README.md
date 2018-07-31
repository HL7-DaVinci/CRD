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
		* `gradle :server:appRun`
	* Run the testing client
		* `gradle :testingClient:run`
	* Run the creator tool
		* `gradle :creator:run -PappArgs="['/tmp/']"`

## Seting up the DB
1. Install and run postgresql
1. Run the gradle task, it will create a database named 'dme' and populate it with a rules table, and the sample rules will be loaded from the csv file.
  * `gradle setupDb` 

This task can also be run to reset the data in the table to match the csv.
  
## Developing
### Setup

1. Install IntelliJ Idea
2. Create new project from existing sources
3. Choose the CRD folder
4. Specify that it is a gradle project

### Running from the UI

* Select the gradle tool window
* Choose CRD --> :server --> Tasks --> application --> bootRun
* Run TestClient.main()

### Running from command line

* run "gradle build"
* run "gradle bootRun"