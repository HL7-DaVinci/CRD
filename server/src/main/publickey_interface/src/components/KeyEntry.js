import React, {Component} from 'react';
import KJUR, {KEYUTIL} from 'jsrsasign';

let showContent = true;
export default class KeyEntry extends Component {
    constructor(props){
        super(props);
        this.state={
            animationClasses:"animated animatedFadeInUp fadeInUp keyEntry",
            kid: this.props.kid,
            jwt: this.props.jwt,
            editMode: false,
            showContent: true,
            data: "text/json;charset=utf-8," + encodeURIComponent(this.props.jwt)
        };
        this.updateContent = this.updateContent.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.deleteContent = this.deleteContent.bind(this);


        
    }

    componentDidMount(){
        try{
            var jwtPub = JSON.parse(this.state.jwt);
        }catch(e){
            jwtPub = this.state.jwt;
        }
        try{
            var pubKey = KEYUTIL.getKey(jwtPub);
            var jwkPub = KEYUTIL.getJWKFromKey(pubKey);
            this.setState({jwt: JSON.stringify(jwkPub)})
            this.setState({data: "text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(jwkPub))});
        }catch(e){
        }
    }
    
    handleChange(event) {
            this.setState({jwt: event.target.value});
      }
    updateContent(event){
        event.preventDefault();
        this.setState({animationClasses:"keyEntry"});
        console.log(this.state.animationClasses)
        try{
            var jwtPub = JSON.parse(this.state.jwt);
        }catch(e){
            jwtPub = this.state.jwt;
        }
        try{
            var pubKey = KEYUTIL.getKey(jwtPub);
            var jwkPub = KEYUTIL.getJWKFromKey(pubKey);
            this.setState({jwt: JSON.stringify(jwkPub)})
            // Dynamically generating a key id is counterintuitive when you 
            // can just add a new key that will auto-generate an ID.  
            // In the unlikely event that users want to be able to directly
            // edit an existing key (aka, acutally editing the text in
            // the PEM string, or the JSON) this could be useful, but I doubt that anybody
            // will be changing individual characters in an encoded string.  
            
            var keyID = KJUR.jws.JWS.getJWKthumbprint(jwkPub);
            if(keyID !=this.state.kid){
                this.props.updateIdCB(this.state.kid,keyID, jwkPub);
                this.setState({kid: keyID});
            }
            this.setState({data: "text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(jwkPub))});
            

            
        }catch(e){
            this.props.updateIdCB(this.state.kid,this.state.kid,this.state.jwt);
        }

        
        

        if(!this.state.editMode){
            this.setState({editMode:true});
        }else{
            this.setState({editMode:false});
        }
    }

    deleteContent(){
        this.props.deleteCB(this.state.kid);
    }


    render() {
        const styles = {
            'animationDelay':this.props.delay
          }
          if(this.state.showContent){
            return (
                <div>
                <div
                className={this.state.animationClasses +" "+ this.props.extraClass}
                value={this.state.value}
                style={styles}
                >
                <div className="jwtContent">
                <form onSubmit={this.updateContent} className="keyEntryForm">
                <div className="kidBox">ID: {this.state.kid}
                </div>
                    {/* <div>ID: {this.state.kid}</div> */}
                {this.state.editMode ? 
                <div>
                    <input
                    value={this.state.jwt}
                    className="editInput"
                    onChange={(value)=> this.handleChange(value)}>
                    </input>
                </div>
                :
                <div className="keyData">{this.state.jwt}</div>}
                </form>
    
    
                </div>
                <div className="buttonContent">
                <button className={"editingButton addButton "+ this.props.extraClass} onClick={this.state.editMode?this.updateContent:()=>{this.setState({editMode: !this.state.editMode})}}>{this.state.editMode? <span className="glyphicon glyphicon-remove"></span>:<span className="glyphicon glyphicon-pencil"></span>}</button>
                    <button className={"editingButton deleteButton "+this.props.extraClass} onClick={this.deleteContent}><span className="glyphicon glyphicon-trash"></span></button>
                    <a href={"data:"+this.state.data} download={this.state.kid + ".json"}><button className={"editingButton downloadButton "+this.props.extraClass} ><span className="glyphicon glyphicon-download-alt"></span></button></a>
                </div>
    
            
                </div>
    
                </div>
            )
          }else{
              return null;
          }
       

        
    }
}