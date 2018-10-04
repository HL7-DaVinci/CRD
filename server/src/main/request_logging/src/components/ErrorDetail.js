import React, {Component} from 'react';
import './detail.css'
export default class ErrorDetail extends Component {
    constructor(props){
        super(props);
        this.state={
            events:{"Parsed":Math.random()<0.5, 
            "Hydrated":Math.random()<0.5, 
            "Extracted":Math.random()<0.5, 
            "Authorized":Math.random()<0.5,
            "Searched":Math.random()<0.5}
        };
         
    }

    render() {
        return (
            <div>

                <div className="errorDetailBox">
                {Object.keys(this.state.events).map((key,i)=>{
                    return (
                        <div>
                        <div className="singleRow withNode"><div className="timelineEvent">
                        <span className="labelFormat"> {key}</span>
                        </div><a className={"node circle "+[this.state.events[key]?"success":"failure"]}>

                        </a>
                    </div>
                    {i!==Object.keys(this.state.events).length-1?
                                        <div className="singleRow">
                                        <div className="timelineEvent emptyEdge">
                                        
                                        </div><div className="node edge" >
                                        |
                                        </div>
                                    </div>:null}

                    </div>
                    )
                })}

                </div>

             </div>
         )

    }
} 