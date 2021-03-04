import React, {Component} from 'react';
import DataTable from './DataTable';
//import './HomePage.css';
import {getBaseUrl} from '../Utils';
import {getHostOrg} from '../Utils';

let baseUrl = getBaseUrl();
let hostOrg = getHostOrg();

const links = [
    {
        name: "FHIR R4 endpoint",
        link: baseUrl + "r4/cds-services",
        description: "The provider systems would interact with the CDS Hooks endpoints. These endpoints provide the JSON descriptions of the CDS Hooks services."
    },
    {
        name: "Rules endpoint",
        link: baseUrl + "api/data",
        description: "Endpoint for retrieving and manipulating rules."
    },
    {
        name: "Requests endpoint",
        link: baseUrl + "api/requests",
        description: "Endpoint for retrieving the requests made to the server as JSON."
    },
    {
        name: "Public Key endpoint",
        link: baseUrl + "api/public",
        description: "Endpoint for retrieving keys from and adding keys to the server database"
    },
    
]
export default class HomePage extends Component {
    constructor(props){
        super(props);
        this.state={
            requestInfo: {}
        };
         
    }
    componentDidMount(){
        document.body.style.marginLeft = "15px"// Set the style
    }
    displayHostTest(){
        switch (hostOrg) {
            case "AHRQ":
                return <p>This server hosts the prototype of the Documentation Requirement Lookup Service (DRLS) clinical decision support (CDS) service. The DRLS CDS Service is a software project that conforms to the implementation guides for <a href="http://build.fhir.org/ig/HL7/davinci-crd/">Coverage Requirements Discovery (CRD)</a> and <a href="http://build.fhir.org/ig/HL7/davinci-dtr/">Documentation Templates and Rules (DTR)</a> developed by the MITRE Corporation for the Centers for Medicare &amp; Medicaid Services (CMS), and under the <a href="http://www.hl7.org/about/davinci/index.cfm?ref=common">Da Vinci Project</a> within the <a href="http://www.hl7.org/">HL7 standards developing organization</a>. It was developed in support of the <a href="https://www.cms.gov/Research-Statistics-Data-and-Systems/Monitoring-Programs/Medicare-FFS-Compliance-Programs/LookupServiceInitiative.html">DRLS Initiative</a> led by CMS, with hosting provided by the <a href="https://www.ahrq.gov/">Agency for Healthcare Research and Quality (AHRQ)</a> as part of an inter-agency collaboration. In addition, several DRLS CDS artifacts are being hosted on AHRQ’s <a href="https://cds.ahrq.gov/">CDS Connect</a> repository.
                    <br/><br/>CRD is implemented with <a href="https://cds-hooks.org/" target="#">CDS Hooks</a>. It returns responses to CRD queries based on a small database of example rules. The example rules can be managed through the <a href="/data">administrative interface</a>.</p>;
            break;
            case "HSPC":
                return <p>This server hosts the prototype of the Documentation Requirement Lookup Service (DRLS) clinical decision support (CDS) service. The DRLS CDS Service is a software project that conforms to the implementation guides for <a href="http://build.fhir.org/ig/HL7/davinci-crd/">Coverage Requirements Discovery (CRD)</a> and <a href="http://build.fhir.org/ig/HL7/davinci-dtr/">Documentation Templates and Rules (DTR)</a> developed by the MITRE Corporation for the Centers for Medicare &amp; Medicaid Services (CMS), and under the <a href="http://www.hl7.org/about/davinci/index.cfm?ref=common">Da Vinci Project</a> within the <a href="http://www.hl7.org/">HL7 standards developing organization</a>. It was developed in support of the <a href="https://www.cms.gov/Research-Statistics-Data-and-Systems/Monitoring-Programs/Medicare-FFS-Compliance-Programs/LookupServiceInitiative.html">DRLS Initiative</a> led by CMS, with hosting provided by the <a href="https://www.hspconsortium.org/">Healthcare Services Platform Consortium (HSPC)</a>. In addition, several DRLS CDS artifacts are being hosted on <a href="https://www.ahrq.gov/">Agency for Healthcare Research and Quality (AHRQ)</a>’s <a href="https://cds.ahrq.gov/">CDS Connect</a> repository.
                    <br/><br/>CRD is implemented with <a href="https://cds-hooks.org/" target="#">CDS Hooks</a>. It returns responses to CRD queries based on a small database of example rules. The example rules can be managed through the <a href="/data">administrative interface</a>.</p>;
            break;
            default:
                return <p>CRD is implemented with <a href="https://cds-hooks.org/" target="#">CDS Hooks</a>. It returns responses to CRD queries based on a small database of example rules. The example rules can be managed through the <a href="/data">administrative interface</a>.</p>
        }
    }
     render() {
         return (
             <div >
                 <h1 className="header" id="henlo">Coverage Requirements Discovery (CRD) Reference Implementation (RI)</h1>
                 {this.displayHostTest()}
                <table className="table">
                    <thead>
                    <tr>
                        <th>

                        </th>
                        <th>
                            URI
                        </th>
                        <th>
                            Description
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                        {links.map((element)=>{
                            return(<tr key = {element.name}>
                            <th scope="row">
                                {element.name}
                            </th>
                            <td>
                                <a href={element.link}>
                                    {element.link}
                                </a>
                            </td>
                            <td>
                                {element.description}
                            </td>
                        </tr>)
                        })}
                    </tbody>

                </table>
             </div>

         )

    }
} 
