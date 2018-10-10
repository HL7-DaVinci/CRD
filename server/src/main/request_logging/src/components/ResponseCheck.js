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

     render() {
         return (
             <div>
                 <div className="responseResults">

                     {this.props.results.length?this.props.results.map(element=>{
                         return (

                             Object.keys(element).map(key=>{
                                 if(element[key]===true){
                                     element[key]="true";
                                 }else if(element[key]===false){
                                     element[key]="false";
                                 }
                                 if(key=="id"){
                                    return <div key={key} className="ruleInfoHeader">Rule Id: {element[key]}</div>
                                 }else{
                                    return <div key={key} className={"infoEntry " + [element[key]?"":"faded"]}>{key}: {element[key]}</div>
                                 }
                         })
                        )
                     })
                    :
                    "No rules found"
                    }
                 </div>
             </div>

         )
        }
    }
