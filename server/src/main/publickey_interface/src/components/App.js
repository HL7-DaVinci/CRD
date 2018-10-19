import React, {Component} from 'react';
import KeyInterface from '../containers/KeyInterface';
export default class App extends Component {
    render() {
        return (
            <div>
                <KeyInterface doFetch={true}/>
            </div>
        );
    }
}