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
            showResults: false,
            showCards: false
        };
         this.showRequestBody = this.showRequestBody.bind(this);
         this.showResults = this.showResults.bind(this);
         this.showCards = this.showCards.bind(this);

         // process the topics array
         this.resultsCount = 0;
         this.topics = '';
         var i;
         if (this.props.data.topics != null) {
            for (i=0; i< this.props.data.topics.length; i++) {
                if (this.props.data.topics[i] != null) {
                    this.resultsCount = this.resultsCount + 1;

                    if (i == 0) {
                        this.topics = this.props.data.topics[i];
                    } else {
                        this.topics = this.topics + ', ' + this.props.data.topics[i];
                    }
                }
            }
        }
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

    showCards(){
        this.setState(prevState=>{
            return {showCards:!prevState.showCards}
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
                        if(this.state.showCards){
                            this.setState({showCards:false})
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
                        <div className={"errorDetail " + [this.state.showCards?"filled":"empty"]} onClick={this.showCards}>
                            Show Cards
                        </div>
                        <div className={"errorDetail " + [this.state.showResults?"filled":"empty"]} onClick={this.showResults}>
                            Topic Results <span>[ {this.resultsCount} ]</span>
                        </div>
                    </div>

                </div>
                {this.state.showRequestBody?
                 <div className="requestBody">
                 { this.unfurlJson(JSON.parse(atob(this.props.data.requestBody))) }
                 </div>
                 :
                 null}

                 {this.state.showCards?
                 <div className = "requestBody">
                 { this.unfurlJson(JSON.parse(this.props.data.cardList)) }
               </div>
                  :
                null}
                 {this.state.showResults?
                 <div className="requestBody">&nbsp;&nbsp;Topics: {this.topics}</div>
                 :null}

             </div>
         )

    }
}
