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
            requestInfo: {}
        };
         
    }

     render() {
         return (
             <div>
                    <div className="detailWindow">

                    <div className="col1">
                    <div className="healthInfoBox">
                            <HealthInfo />
                    </div>
                    <div className="responseCheckBox">
                            <ResponseCheck />
                    </div>
                    </div>
                    <div className="col1">
                    <ErrorDetail />
                    </div>

                    <div className="col1 processTime">
                    <span>2.56s </span>
                        <span className="glyphicon glyphicon-time"> </span>
                    </div>

                </div>
             </div>
         )

    }
} 