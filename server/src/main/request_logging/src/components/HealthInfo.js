import React, {Component} from 'react';
import './detail.css';
import './request.css';
export default class HealthInfo extends Component {
    constructor(props){
        super(props);
        this.state={
            requestInfo: {}
        };
         
    }

    componentDidMount(){
        // temporarily hard code the data
        this.setState({requestInfo: {"age":54,"gender":"-","code":"A342F4","codeSystem":"HCPCS"}})
    }

     render() {
         return (
             <div>
                <div className="healthInfoHeader">
                    Request Info
                </div>
                <div className="healthInfo">
                    {Object.keys(this.state.requestInfo).map(key=>{
                        return(  
                            <div key = {key} className="infoEntry">
                            <span className="infoTitle">{key}</span>: {this.state.requestInfo[key]}
                            </div>
                        )
                    })}
                </div>
             </div>

         )

    }
} 