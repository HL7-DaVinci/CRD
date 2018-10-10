import React, {Component} from 'react';
import HealthInfo from './HealthInfo';
import './detail.css';
import './request.css';
import ErrorDetail from './ErrorDetail';

import ResponseCheck from './ResponseCheck';
export default class DetailEntry extends Component {
    constructor(props){
        super(props);
        this.state={
            slideIn: "slideInStart",
            requestInfo: {},
            showRequestBody:false,
            showResults: false
        };
         this.showRequestBody = this.showRequestBody.bind(this);
         this.showResults = this.showResults.bind(this);
    }

    showRequestBody(){
        this.setState(prevState=>{
            return {showRequestBody:!prevState.showRequestBody}
        });
    }

    showResults(){
        this.setState(prevState=>{
            return {showResults:!prevState.showResults}
        });
    }
    unfurlJson(jsonData){
        var divStyle = {
            marginLeft:20
          };

        if(jsonData){
            return Object.keys(jsonData).map(element=>{
                // we don't want to show the actual token that was used
                if(element==="access_token"){
                    return(              
                    <div className="jsonData" key={element} style={divStyle}>

                    <span className="elementKey">{element}</span>: <span className="elementBody">-</span>
                </div>
                    )
                }
                return ( 
                <div className="jsonData" key={element} style={divStyle}>

                    <span className="elementKey">{element}</span>: <span className="elementBody">{jsonData[element]===null?"null":typeof jsonData[element] === "object"?this.unfurlJson(jsonData[element]):jsonData[element]}</span>
                </div>
                )
            });
        }

    }

     render() {
         return (
             <div>
                    <div className="detailWindow" 
                    onClick={()=>{
                        if(this.state.showRequestBody){
                            this.setState({showRequestBody:false})
                        }
                        if(this.state.showResults){
                            this.setState({showResults: false})
                        }
                    
                    }}>

                    <div className="col1">
                    <div className="healthInfoBox">
                            <HealthInfo data={this.props.data} />
                    </div>
                    {/* <div className="responseCheckBox">
                            <ResponseCheck />
                    </div> */}
                    </div>
                    <div className="col1">
                    <ErrorDetail timeline={this.props.data.timeline}/>
                    </div>

                    <div className="col1 processTime">
                        <div className={"errorDetail " + [this.state.showRequestBody?"filled":"empty"]} onClick={this.showRequestBody}>
                            Show Request Body
                        </div>
                        <div className={"errorDetail " + [this.state.showResults?"filled":"empty"]} onClick={this.showResults}>
                            Results <span>[ {this.props.data.rulesFound.length} ]</span>
                        </div>
                    </div>

                </div>
                {this.state.showRequestBody?
                 <div className="requestBody">
                 { this.unfurlJson(JSON.parse(atob(this.props.data.requestBody))) }
                 </div>
                 :
                 null}
                 {this.state.showResults?
                 <div className="requestBody">
                 <ResponseCheck results={this.props.data.rulesFound}></ResponseCheck></div>
                 :null
                 }
               

             </div>
         )

    }
} 