import React, {Component} from 'react';
import './detail.css';
import './request.css';
export default class ResponseCheck extends Component {
    constructor(props){
        super(props);
        this.state={
            responseInfo: {}
        };
         
    }

    componentDidMount(){
        // temporarily hard code the data
        this.setState({responseInfo: {"error":Math.random()<0.5,"resultFound":Math.random()<0.5,ruleFound:Math.random()<0.5}})
    }

     render() {
         return (
             <div>
                 <div className={"errorDetail " + [this.state.responseInfo.error?"success":"failure"]}>
                    Error
                 </div>
                 <div className={"errorDetail " + [this.state.responseInfo.resultFound?"success":"failure"]}>
                    ResultFound
                 </div>
                 <div className={"errorDetail " + [this.state.responseInfo.ruleFound?"success":"failure"]}>
                    RuleFound
                 </div>
             </div>

         )
        }
    }
