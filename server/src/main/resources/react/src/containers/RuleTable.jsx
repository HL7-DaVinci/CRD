import React, { Component } from 'react';

class Table extends Component {

	state = { rules: [] };
	
	componentDidMount() {
		fetch(http://localhost:8090/api/data)
		.then(response => response.json())
		.then(rules => this.setState({rules}));
	}
	
	render() {
		return (
			<ul>
			{
				this.state.rules.map(function(rule) {
				return <li key={rule.id}>{rule.id}</li>;})
				}
				</ul>
				
		);
	}
}

export default App;