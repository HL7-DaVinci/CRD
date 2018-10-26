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

    async componentDidMount(){
        if(this.props.doFetch){
            var jwtData = await fetch('http://localhost:8090/api/public', {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
                }).then(response=>{
                    return response.json();
                }).catch(error=>{
                    console.log("Could not load data, make sure the server is running.")
                });
            this.initData(jwtData);
        }

    }
    async saveData(keyObject){
        // const jwtData = this.state.jwtJson;
        // const result = {};
        // var key;
        // // convert the array back into JSON
        // jwtData.forEach(element=>{
        //     key = Object.keys(element)[0];
        //     result[key] = element[key];
        // });
        const keyId = Object.keys(keyObject)[0];

        const key = keyObject[keyId];
        const result = {"id":keyId,"key":key};
        if(this.props.doFetch){
            await fetch('http://localhost:8090/api/public', {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content': 'application/json'
                },
                body: JSON.stringify(result)
                }).then(response=>{
                    console.log("Saved the data")
                }).catch(error=>{
                    console.log("Could not save data");
                });
        }

    }

    async deleteData(id){
        if(this.props.doFetch){
            await fetch('http://localhost:8090/api/public/'+id, {
                method: 'DELETE',
                headers: {
                    'Accept': 'application/json',
                    'Content': 'application/json'
                }
                }).then(response=>{
                    console.log("Deleted the data")
                }).catch(error=>{
                    console.log("Could not save data");
                });;
        }

    }
    async editData(oldId,keyObject){
        const keyId = Object.keys(keyObject)[0];
        const key = keyObject[keyId];
        const result = {"id":keyId,"key":JSON.stringify(key)};
        if(this.props.doFetch){
            await fetch('http://localhost:8090/api/public/'+oldId, {
                method: 'PUT',
                headers: {
                    'Accept': 'application/json',
                    'Contetn': 'application/json'
                },
                body: JSON.stringify(result)
                }).then(response=>{
                    console.log("Saved the data")
                }).catch(error=>{
                    console.log("Could not save data");
                });;
        }
    }
    initData(jwtData){
        if(jwtData){
            const peopleArray =[];
            Object.keys(jwtData).map(key =>{
                const id = jwtData[key]["id"];
                peopleArray.push({[id]:jwtData[key]["key"]})
            })
            this.setState({jwtJson:peopleArray});
        }

    }
    updateIdCB(oldId, newId, newContent){
        let changeBool = true;
        this.state.jwtJson.map(element=>{

            if(JSON.stringify(element[oldId])==JSON.stringify(newContent)){
                changeBool = false;
            }
        });
        if(changeBool){
            this.setState(prevState =>{
                var intermed = prevState.jwtJson;
                var updatedIdArray = intermed.map(key=>{
                    if(Object.keys(key)[0]==oldId){
                        console.log(newContent);
                        console.log(key[oldId]);
                        return {[newId]:JSON.stringify(newContent)};
                    }else{
                        return key;
                    }
                });
                return {jwtJson: updatedIdArray};
            }, ()=>this.editData(oldId,{[newId]:newContent}));
        }

           
    }
    deleteContent = (id) =>{
        this.setState(prevState =>{
            var intermed = prevState.jwtJson;
            console.log(intermed);
            // filters object from array if it has the same ID passed into
            // the function.
            return {jwtJson: intermed.filter(key=>Object.keys(key)[0] !== id)};
        }, ()=>{this.deleteData(id)});
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
        },()=>{this.saveData({[kid]:jwt})});

        
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

                <button id="addButton" className="newEntryButton" onClick={this.newItem}><span className="glyphicon glyphicon-plus-sign"></span></button>

                <button className="newEntryButton reloadButton" onClick={this.initData}><span className="glyphicon glyphicon-retweet"></span></button>

                <div className = "borderDiv">
                </div>
                {this.returnItem()}

                {this.state.jwtJson.map(key => {
                    keyID = Object.keys(key)[0];
                    i+=0.2;
                    keyContent = key[keyID];

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



