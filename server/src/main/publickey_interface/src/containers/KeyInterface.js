import React, {Component} from 'react';

import '../index.css';
import KJUR, {KEYUTIL} from 'jsrsasign';
import KeyEntry from '../components/KeyEntry';
import EditEntry from '../components/EditEntry';



export default class KeyInterface extends Component{
    constructor(props){
        super(props);
        this.state = { 
            jwtJson: [],
            createNew: false,
            editing: ""
        };

        this.deleteContent = this.deleteContent.bind(this);
        this.newItem = this.newItem.bind(this);
        this.exitNewItem = this.exitNewItem.bind(this);
        this.isUnique = this.isUnique.bind(this);
        this.updateIdCB = this.updateIdCB.bind(this);
        this.returnItem = this.returnItem.bind(this);
        this.submitContent = this.submitContent.bind(this);
        this.saveData = this.saveData.bind(this);
        this.initData = this.initData.bind(this);

    }

    componentDidMount(){
        this.initData();
    }
    async saveData(){
        const jwtData = this.state.jwtJson;
        const result = {};
        var key;
        // convert the array back into JSON
        jwtData.forEach(element=>{
            key = Object.keys(element)[0];
            result[key] = element[key];
        });
        await fetch('http://localhost:8090/api/public', {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Contetn': 'application/json'
            },
            body: JSON.stringify(result)
            }).then(response=>{
                console.log("Saved the data")
            });
    }

    async initData(){
        var jwtData = await fetch('http://localhost:8090/api/public', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
            }).then(response=>{
                return response.json();
            }).catch(error=>{
                console.log("Couldn't load data, make sure the server is running.")
            });
        if(jwtData){
            const peopleArray =[];
            Object.keys(jwtData).map(key =>{
                peopleArray.push({[key]:jwtData[key]})
            })
            this.setState({jwtJson:peopleArray});
        }

    }
    updateIdCB(oldId, newId, newContent){

        this.setState(prevState =>{
            var intermed = prevState.jwtJson;
            var updatedIdArray = intermed.map(key=>{
                if(Object.keys(key)[0]==oldId){

                    return {[newId]:newContent};
                }else{
                    return key;
                }
            });
            return {jwtJson: updatedIdArray};
        });
           
    }
    deleteContent = (id) =>{
        this.setState(prevState =>{
            var intermed = prevState.jwtJson;
            console.log(intermed);
            // filters object from array if it has the same ID passed into
            // the function.
            return {jwtJson: intermed.filter(key=>Object.keys(key)[0] !== id)};
        });
    }

    exitNewItem = (save) =>{
            this.setState({createNew:false});
            this.setState({editing:""});
    }

    newItem(){

        this.setState({createNew:true});
        this.setState({editing:"editing"});
        // this.setState({jwtJson: [{"aslkaslk":{"null":"null"}}, ...this.state.jwtJson]});
        // console.log(this.state.jwtJson);
    }

    isUnique(kid){
        const length = this.state.jwtJson.filter(key=>Object.keys(key)[0] == kid).length;
        return length===0;
    }

    submitContent(kid,jwt){
        this.setState({createNew:false});
        this.setState({editing:""});
        const newEntry = {[kid]:jwt};
        this.setState(prevState =>{
            return {jwtJson: [newEntry,...prevState.jwtJson]}
        });

        
    }
    returnItem(){
        if(this.state.createNew){
            return <EditEntry 
            deleteCB={this.exitNewItem}
            isUnique={this.isUnique}
            submitContent={this.submitContent}
            />
        }
    }

    

    render() {
        var i = 0;
        var keyID;
        var keyContent;
            return (
                <div>
                <h1 className="titleHeader" >Public Keys</h1>
                <button className="newEntryButton" onClick={this.newItem}><span className="glyphicon glyphicon-plus-sign"></span></button>
                <button className="newEntryButton" onClick={this.saveData}><span className="glyphicon glyphicon-floppy-disk"></span></button>
                <button className="newEntryButton reloadButton" onClick={this.initData}><span className="glyphicon glyphicon-retweet"></span></button>

                <div className = "borderDiv">
                </div>
                {this.returnItem()}
                {console.log(this.state.jwtJson)}
                {this.state.jwtJson.map(key => {
                    keyID = Object.keys(key)[0];
                    i+=0.2;
                    keyContent = JSON.stringify(key[keyID]);
                  return <KeyEntry 
                  extraClass = {this.state.editing}
                  deleteCB={this.deleteContent} 
                  updateIdCB={this.updateIdCB}
                  kid = {keyID} 
                  delay={i+"s"} 
                  jwt ={keyContent} 
                  key={keyID}/>
                })}
                </div>
            )

    }
}



