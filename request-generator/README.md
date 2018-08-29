# CRD Request Generator
This subproject provides a small web application that is capable of generating CRD requests and displaying the CDS Hooks cards that are provided as a response. This project is written in JavaScript is runs in [node.js](https://nodejs.org/en/).

## Running the request generator
1. Install and run node.js
1. Clone the repo
  * `git clone https://github.com/HL7-DaVinci/CRD.git`
1. Install the dependencies
  * `cd CRD/request-generator`
  * `npm install`
1. Run the application
  * `npm start`

This should open a browser window directed to http://localhost:3000. The request-generator assumes the CRD server is running on `localhost:8090`. This can be changed in [RequestBuilder.js](src/containers/RequestBuilder.js).