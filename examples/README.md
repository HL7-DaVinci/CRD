# CRD Request/Response Examples

This directory contains example request/response flows for CRD. They are broken down by FHIR version and the by the CDS
Hook type. The requests populate the prefetch with the data necessary to satisfy the request.

In this case, requests were submitted to the CRD Reference Implementation. Right now, the RI makes decisions based on the submitted patient's age, gender and the requested code/code system. The CRD RI consults a small database to check whether
documentation requirements are present. The deafult rules are listed in the [data.sql](https://github.com/HL7-DaVinci/CRD/blob/master/server/src/main/resources/data.sql). These examples use codes within this set to hit both the documentation requirements vs. not present states.

From a testing perspective, when there are documentation requirements found, the CRD RI will return a CDS Hooks Card with a Link present. If no documentation requirements were found, either because the code is in the database but states that there are no requirements or the code is not in the database, the RI will return a Card without Links or Suggestions.
