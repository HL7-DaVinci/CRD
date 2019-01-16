import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import { BrowserRouter as Router, Route, Link } from "react-router-dom";

import RequestLog from './containers/RequestLog';
import KeyInterface from './containers/KeyInterface';
import DataTable from './components/DataTable';
import HomePage from './components/HomePage';
import {withRouter} from 'react-router-dom';

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
class App extends Component {
  render() {
    return (
      <div>
      <nav className="navbar navbar-fixed-top headerEntry">
      <div className="container padHelp">
          <span className = "navbar-brand headerIcon"> <a href="/"><span className="glyphicon glyphicon-fire"></span></a></span>
          <a className={"navbar-brand headerLink " +
            (this.props.location.pathname=="/"?"active":"")} href="/">Home</a>

          <a className={"navbar-brand headerLink " +
            (this.props.location.pathname=="/public"?"active":"")} href="/public">Keys</a>
          <a className={"navbar-brand headerLink " +
            (this.props.location.pathname=="/data"?"active":"")} href="/data">Rules</a>
          <a className={"navbar-brand headerLink " +
            (this.props.location.pathname=="/requests"?"active":"")} href="/requests">Log</a>
      </div>
      </nav>
        <Route exact={true} path='/' component={HomePage}/>
        <Route path='/public' component={publicKey}/>
        <Route path='/requests' component={RequestLog}/>
        <Route path='/data' component={DataTable}/>

      </div>
    );
  }
}

export default withRouter(App);
