import React, {Component} from 'react';
import DataTable from './DataTable';
//import './HomePage.css';
import {getBaseUrl} from '../Utils';

let baseUrl = getBaseUrl();
const links = [
    {
        name: "FHIR STU3 endpoint",
        link: baseUrl + "stu3/cds-services",
        description: "The provider systems would interact with the CDS Hooks endpoints. These endpoints provide the JSON descriptions of the CDS Hooks services."
    },
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
     render() {
         return (
             <div >
                     <h1 className="header" id="henlo">Coverage Requirements Discovery (CRD) Reference Implementation (RI)</h1>
    <p>
        Welcome to the reference implementation of the Coverage Requirements Discovery (CRD) service. This service operates following the implementation guide published
        at H7 and developed by the <a href="http://www.hl7.org/about/davinci/index.cfm?ref=common">Da Vinci Project</a>.
        It is implemented with <a href="https://cds-hooks.org/" target="#">CDS Hooks</a>.
    </p>
    <p>
        This service returns responses to CRD queries based on a small database of examples it maintains.
        Example responses can be managed through the <a href="/data">administrative interface</a>.
    </p>
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