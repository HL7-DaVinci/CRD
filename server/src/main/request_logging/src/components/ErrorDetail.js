import React, {Component} from 'react';

import './detail.css'
export default class ErrorDetail extends Component {
    constructor(props){
        super(props);
        this.state={
            events:{"Authorized":this.props.timeline?this.props.timeline[0]:false, 
            "Parsed":this.props.timeline?this.props.timeline[1]:false, 
            "Hydrated":this.props.timeline?this.props.timeline[2]:false, 
            "Extracted":this.props.timeline?this.props.timeline[3]:false,
            "Searched":this.props.timeline?this.props.timeline[4]:false}
        };
         
    }

    render() {
        return (
            <div>

                <div className="errorDetailBox">
                {Object.keys(this.state.events).map((key,i)=>{
                    return (
                        <div key = {key}>
                        <div className="singleRow withNode"><div className="timelineEvent">
                        <span className="labelFormat"> {key}</span>
                        </div><a className={"node circle "+[this.state.events[key]?"success":"failure"]}>
                        {/* ignoring top nub at top node moves the relative positioning of the lower nub */}
                        {i!==0?<span className="topDivet"></span>:<span className="topDivet invis"></span>}
                        
                        {i!==Object.keys(this.state.events).length-1?<span className="bottomDivet">
                        </span>:null}

                        </a>

                    </div>
                    {/* stops the last node from getting a lower nub */}
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