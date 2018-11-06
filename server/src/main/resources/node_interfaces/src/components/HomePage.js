import React, {Component} from 'react';
import DataTable from './DataTable';
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
                     <h1 className="header" id="henlo">Coverage Requirements Discovery Reference Implementation</h1>
    <p>
        Welcome to the reference implementation of the Coverage Requirements Discovery (CRD) service. This service operates following the implementation guide published
        at H7 and developed by the <a href="http://www.hl7.org/about/davinci/index.cfm?ref=common">Da Vinci Project</a>.
        It is implemented as a <a href="https://cds-hooks.org/">CDS Hook</a>.
    </p>
    <p>
        This service returns responses to CRD queries based on a small database of examples it maintains.
        Example responses can be managed through the <a href="/data">administrative interface</a>.
    </p>
                <DataTable></DataTable>
             </div>

         )

    }
} 