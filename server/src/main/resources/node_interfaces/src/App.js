import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import { BrowserRouter as Router, Route, Link } from "react-router-dom";

import RequestLog from './containers/RequestLog';
import KeyInterface from './containers/KeyInterface';
import DataTable from './components/DataTable';
import HomePage from './components/HomePage';
import {withRouter} from 'react-router-dom';
import {getBaseUrl} from './Utils';

const publicKey = () => (
  <div>
    <KeyInterface doFetch={true}/>
  </div>
)

const Category = () => (
  <div>
    <h2 className="App-header">Category</h2>
  </div>
)

let baseUrl = getBaseUrl();

class App extends Component {
  render() {
      const homeUrl = baseUrl;
      const publicUrl = baseUrl + 'public';
      const requestUrl = baseUrl + 'requests';
      const dataUrl = baseUrl + 'data';
    return (
      <div>
      <nav className="navbar navbar-fixed-top headerEntry">
      <div className="container padHelp">
          <span className = "navbar-brand headerIcon"> <span className="glyphicon glyphicon-fire"></span></span>
          <a className={"navbar-brand headerLink " +
            (this.props.location.pathname==homeUrl?"active":"")} href={homeUrl}>Home</a>

          <a className={"navbar-brand headerLink " +
            (this.props.location.pathname==publicUrl?"active":"")} href={publicUrl}>Keys</a>
          <a className={"navbar-brand headerLink " +
            (this.props.location.pathname==dataUrl?"active":"")} href={dataUrl}>Rules</a>
          <a className={"navbar-brand headerLink " +
            (this.props.location.pathname==requestUrl?"active":"")} href={requestUrl}>Log</a>
      </div>
      </nav>
        <Route exact={true} path={homeUrl} component={HomePage}/>
        <Route path={publicUrl} component={publicKey}/>
        <Route path={requestUrl} component={RequestLog}/>
        <Route path={dataUrl} component={DataTable}/>

      </div>
    );
  }
}

export default withRouter(App);
