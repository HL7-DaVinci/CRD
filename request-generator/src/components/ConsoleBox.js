import React, {Component} from 'react';

export default class ConsoleBox extends Component {
    constructor(props){
        super(props);
        this.state = { 
            showStatus: "hideConsole",
            headerStatus: "collapseHeader"
            }

            this.toggleConsole = this.toggleConsole.bind(this);

        };


        
  handleAddition = (e, { value }) => {
    this.setState({
      options: [{ text: value, value }, ...this.state.options],
    })
  }

  handleChange = (e, { value }) => {
    this.props.updateCB(this.props.elementName, value)
    this.setState({ currentValue: value })
  }

  toggleConsole(){
      if(this.state.showStatus==="showConsole"){
        this.setState({showStatus:"hideConsole"});
        this.setState({headerStatus:"collapseHeader"});
      }else{
        this.setState({showStatus:"showConsole"});
        this.setState({headerStatus:"showHeader"});
      }
  }

  render() {

    try{
        var objDiv = document.getElementById("your_div");
        objDiv.scrollTop = objDiv.scrollHeight;
    }catch(e){

    }
      let i = 0;
    return (
        <div>

                <a className={this.state.headerStatus + " consoleHeader"} onClick={this.toggleConsole} >

                </a>
            <div id="your_div" className = {this.state.showStatus + " consoleMain resize"}>

                {this.props.logs.map(element => {
                    i++;
                    return <div key = {i}> > <span className={element.type}>{element.content}</span></div>
                }) }
            </div>
        </div>

    )
  }
}