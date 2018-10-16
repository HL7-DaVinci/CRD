import React, {Component} from 'react';
import './RequestLog.css';
import '../components/request.css';
import RequestEntry from '../components/RequestEntry';
let tempDatabase={
}

let entriesPerPage = 90;
export default class RequestLog extends Component {
    constructor(props){
        super(props);
        this.state={
            data:[],
            dataToShow:null,
            page:1

        };
         
        this.generateData = this.generateData.bind(this);
        this.getPage = this.getPage.bind(this);
        this.renderPageNumbers = this.renderPageNumbers.bind(this);
        this.getData = this.getData.bind(this);
    }

    
    componentDidMount(){
        this.getData();
        this.getPage(1);
    }

    async getData(){
        const requestData = await fetch('http://localhost:8090/api/requests', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
            }).then(response=>{
                return response.json();
            }).catch(error=>{
                console.log("Couldn't load data, make sure the server is running.")
            });
        if(requestData){
            this.setState({data:requestData});
            console.log(requestData);
            var webdata = requestData.requestBody;
            this.getPage(1);
        }


    }
    getPage(pageNumber){
        const startIndex = (pageNumber-1)*entriesPerPage;
        this.setState({dataToShow:this.state.data.slice(startIndex,startIndex+entriesPerPage)})
        this.setState({page:pageNumber});
    }

    renderPageNumbers(){
        const returnArray = [];
        for(var i = 1; i<= Math.ceil(this.state.data.length/entriesPerPage);i++){
            returnArray.push(i);
        }
        return returnArray;
    }
    generateData(){
        // temp function to make data
        const currentTime = Date.now();
        const numberOfDataPoints = 92;
        var currentElement = {};
        var variation = 1200000000;
        var returnList = [];
        for(var i = 0; i<numberOfDataPoints; i++){
            var negPos = Math.random()<0.5 ? -1:1;

            currentElement = {
                "timeStamp": currentTime+Math.random()*negPos*variation,
                "fhirVersion": Math.random()<0.5?"stu3":"r4",
                "hookType": Math.random()<0.5?"MedicationRequest":"OrderReview",
                "success": Math.random()>0.05
            }
            returnList.push(currentElement);

        }
        return returnList;
    }

    compareTime(a,b){
        if (a.timestamp < b.timestamp){
            return 1;
        }
        if(a.timestamp > b.timestamp){
            return -1;

        }
        return 0;
    }




     render() {
         // page should only render when switching pages. 
         // on page switch we scroll to top automatically.
        window.scrollTo(0, 0)
         return (
             <div>
                 <div>
                     {/* header section.  */}
                    <div className="requestHeader fixed">
                        <div className="element timestamp">
                            Timestamp 
                        </div>
                        <div className="element fhirversion">
                            FHIR version
                        </div>
                        <div className="element hooktype">
                            Hook Type
                        </div>
                        <div className="element successElement">
                            Success Status
                        </div>

                        <div className="pageTab">
                            {this.state.page}
                        </div>
                    </div>

                    <div className="break">
                    {/* this manually prevents the topmost request entry
                    from being hidden underneath the header */}
                    </div>
                    <div className="requestEntries">
                        {this.state.dataToShow?this.state.dataToShow.map(element=>{
                            return <RequestEntry 
                            data={element}
                            key={element.timestamp}
                            />
                        }):<p></p>}
                    </div>
                 </div>

                 <div className="pageNumber">
                     {this.renderPageNumbers().map(number=>{
                         return <button 
                         key={number}
                         className={"orderButton " + [this.state.page===number?"active":""]}
                         onClick={()=> this.getPage(number)}
                         >
                         {number}
                         </button>
                     })}
                 </div>
                
             </div>
         )
    }
} 