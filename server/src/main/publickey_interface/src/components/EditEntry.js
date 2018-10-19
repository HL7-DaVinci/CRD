import React, {Component} from 'react';
import KJUR, {KEYUTIL} from 'jsrsasign';
let animationClasses = "animated animatedFadeInUp fadeInUp keyEntry editEntry";
export default class EditEntry extends Component {
    constructor(props){
        super(props);
        this.state={
            kid: "",
            jwt: "",
            editMode: true,
            generateKeyID: false,
            uniqueKey: true
        };

        this.updateContent = this.updateContent.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.deleteContent = this.deleteContent.bind(this);
        this.generateKeyID = this.generateKeyID.bind(this);
        this.submitContent = this.submitContent.bind(this);

        
    }

    handleChange(event, element) {
            this.setState({[element]: event.target.value});
            if(element==="jwt" & this.state.generateKeyID){
                this.setState({kid: this.getKeyID(event.target.value)})
            }
    }

    getKeyID(value){
        var encodedKey = null;
        try{
            encodedKey=JSON.parse(value);
        }catch(e){
            // the key is not JSON formatted
            encodedKey=value;
        }
        keyID = "";
        try{
            var pubKey = KEYUTIL.getKey(encodedKey);
            // this part won't fail
            var jwkPub = KEYUTIL.getJWKFromKey(pubKey);
            var keyID = KJUR.jws.JWS.getJWKthumbprint(jwkPub);
        }catch(e){
            // the key cannot be retrieved.
            console.log(e);
        }
        return keyID;
    }
    
    generateKeyID(){
        if(!this.state.generateKeyID){
            // the key ID should be generated when the box gets ticked.
            this.setState({kid:this.getKeyID(this.state.jwt)});
        }
        this.setState({generateKeyID: !this.state.generateKeyID})

      }
    updateContent(event){
        try{
            var jwtPub = JSON.parse(this.state.jwt);
        }catch(e){
            jwtPub = this.state.jwt;
        }
        try{
            var pubKey = KEYUTIL.getKey(jwtPub);

            var jwkPub = KEYUTIL.getJWKFromKey(pubKey);
            this.setState({jwt: JSON.stringify(jwkPub)})
            var keyID = KJUR.jws.JWS.getJWKthumbprint(jwkPub);
            // this.props.updateIdCB(this.state.kid,keyID, jwkPub);
            this.setState({kid: keyID});

            
        }catch(e){
        }

        
        
        event.preventDefault();
        if(!this.state.editMode){
            this.setState({editMode:true});
        }else{
            this.setState({editMode:false});
        }
    }

    deleteContent(){
        console.log("deleting");
        this.props.deleteCB(false);
    }

    submitContent(event){
        event.preventDefault();
        const unique = this.props.isUnique(this.state.kid);
        if(!unique){
            // implement a modal or an on screen notification
            // instead of an alert.  Key clashes should be rare
            // anyway.
            alert("Key ID must be unique");
        }else{
            this.props.submitContent(this.state.kid,this.state.jwt);
        }
        //this.props.submitContent(this.state.kid,this.state.jwt);
    }


    render() {
        const styles = {
            'animationDelay':this.props.delay
          }

            return (
                <div id="editEntry">
                <div
                className={animationClasses}
                value={this.state.value}
                style={styles}
                >
                <div className="jwtContent">
                <button id="editDeleteButton" className="editingButton" onClick={this.deleteContent}>X</button>
                <form onSubmit={this.submitContent}>
                <div className = "kidBox">
                <div className="kidBox">Key ID: </div>
                    <input
                    value={this.state.kid}
                    className="editInput"
                    onChange={(value)=> this.handleChange(value,"kid")}>
                    </input>
                </div>

                    {/* <div>ID: {this.state.kid}</div> */}

                <div>
                    <div className="kidBox">Public Key: </div>
                    <textarea
                    value={this.state.jwt}
                    className="editInput jwtTextArea"
                    onChange={(value)=> this.handleChange(value,"jwt")}>
                    </textarea>
                </div>

                </form>
                <span>Generate Key ID: </span><button className={"editModeButton checkBox "+ (this.state.generateKeyID?"checkBoxPressed":"")} onClick={this.generateKeyID}></button>
                 <button className="clickButton" onClick={this.submitContent}>Submit</button>
                {/* <span>  Submit: </span><button className="clickButton checkBox" ></button> */}

    
                </div>


            
                </div>
    
                </div>
            )

       

        
    }
}