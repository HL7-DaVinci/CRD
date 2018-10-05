# CRD Request Generator
This subproject provides a small web application that is capable of generating CRD requests and displaying the CDS Hooks cards that are provided as a response. This project is written in JavaScript is runs in [node.js](https://nodejs.org/en/).

## Running the request generator standalone
1. Install and run node.js
2. Clone the repo
  * `git clone https://github.com/HL7-DaVinci/CRD.git`
3. Install the dependencies
  * `cd CRD/request-generator`
  * `npm install`
4. Run the application
  * `npm start`

## Build the request generator to run with the ehr-server
1. Install and run node.js
2. Clone the repo
	* `git clone https://github.com/HL7-DaVinci/CRD.git`
3. Install the dependencies
  * `cd CRD/request-generator`
  * `npm install`
4. Modify the package.json file with the "homepage" that the ehr-server will be running at (http://localhost:8080/ehr-server/reqgen)
5. Build
  * `npm run-script build`
6. Start the ehr-server
  * See the ehr-server README for more information 

This should open a browser window directed to http://localhost:3000. The request-generator assumes the CRD server is running on `localhost:8090`. This can be changed in [RequestBuilder.js](src/containers/RequestBuilder.js).