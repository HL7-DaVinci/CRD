import React, {Component} from 'react'
import Tooltip from 'react-tooltip-lite'

import './detail.css'
export default class ErrorDetail extends Component {
    constructor(props){
        super(props);
        this.state={
            events:{
                "Authorized":{
                    status: this.props.timeline?this.props.timeline[0]:false,
                    hovertext: "CRD gained authorization to access all necessary services" },
                "Parsed":{
                    status: this.props.timeline?this.props.timeline[1]:false,
                    hovertext: "Request data parsed" },
                "Hydrated":{
                    status: this.props.timeline?this.props.timeline[2]:false,
                    hovertext: "Prefetched associated data and added to request data" },
                "CQL Fetched":{
                    status: this.props.timeline?this.props.timeline[3]:false,
                    hovertext: "Retrieved relevant CQL" },
                "CQL Executed":{
                    status: this.props.timeline?this.props.timeline[4]:false,
                    hovertext: "Executed CQL using prefetched and request data" },
            }
        };
    }

    render() {
        return (
            <div>
                <div className="errorDetailBox">
                {
                    Object.keys(this.state.events).map((key,i)=>{
                        return (
                            <div key = {key}>
                                <div className="singleRow withNode">
                                    <div className="timelineEvent">
                                        <span className="labelFormat"> 
                                            <Tooltip content={this.state.events[key].hovertext} 
                                                    direction="right" tagName="span" useDefaultStyles="true">
                                                {key}
                                            </Tooltip>
                                        </span>
                                    </div>
                                    <a className={"node circle "+[this.state.events[key].status?"success":"failure"]}>
                                        {/* ignoring top nub at top node moves the relative positioning of the lower nub */}
                                        {i!==0?<span className="topDivet"></span>:<span className="topDivet invis"></span>}
                                        {i!==Object.keys(this.state.events).length-1?<span className="bottomDivet">
                                        </span>:null}
                                    </a>
                                </div>
                                {/* stops the last node from getting a lower nub */}
                                {
                                    i!==Object.keys(this.state.events).length-1 
                                    ?
                                        <div className="singleRow">
                                            <div className="timelineEvent emptyEdge"></div>
                                            <div className="node edge" > | </div>
                                        </div>
                                    :
                                        null
                                }
                            </div>
                        )
                    })
                }
                </div>
            </div>
        )
    }
} 