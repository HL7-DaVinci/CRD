import React, { useEffect, useState } from 'react';
import useStyles from '../styles';
import PropTypes from 'prop-types';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';
import TextField from '@material-ui/core/TextField';
import Select from '@material-ui/core/Select';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import { codeSystems, cptCodes, hcpcsCodes, rxNormCodes } from '../constants';
import StateSelect from './Sections/StateSelect';
import JsonBox from './Sections/JsonBox';


export default function DeviceRequestInfo(props) {
    const classes = useStyles();
    const { callback, deviceRequest } = props;
    const handleChange = (element, value) => {
        callback('deviceRequest', element, value)
    }

    const handleJson = (e) => {
        if (e.jsObject) {
            callback('deviceRequest', 'resource', e.jsObject);
        }
    }


    const loadOrg = () => {
    }

    const getCode = () => {
        if (deviceRequest.codeCodeableConcept && deviceRequest.codeCodeableConcept.coding && deviceRequest.codeCodeableConcept.coding.length > 0) {
            return deviceRequest.codeCodeableConcept.coding[0].code;
        }
    }

    const getSystem = () => {
        if (deviceRequest.codeCodeableConcept && deviceRequest.codeCodeableConcept.coding && deviceRequest.codeCodeableConcept.coding.length > 0) {
            return deviceRequest.codeCodeableConcept.coding[0].system;
        }
    }
    const handleCode = (event) => {
        if (deviceRequest.codeCodeableConcept && deviceRequest.codeCodeableConcept.coding && deviceRequest.codeCodeableConcept.coding[0].system) {
            const codeableConcept = {
                "coding": [
                    {
                        "system": deviceRequest.codeCodeableConcept.coding[0].system,
                        "code": event.target.value,
                    }
                ]
            }
            handleChange('codeCodeableConcept', codeableConcept);
        } else if (deviceRequest.codeCodeableConcept && deviceRequest.codeCodeableConcept.coding){
            const codeableConcept = {
                "coding": [
                    {
                        "code": event.target.value,
                    }
                ]
            }
            handleChange('codeCodeableConcept', codeableConcept);
        }
    }

    const handleSystem = (event) => {
        const codeableConcept = {
            "coding": [
                {
                    "system": event.target.value,
                }
            ]
        }
        handleChange('codeCodeableConcept', codeableConcept);
    }

    const renderCode = () => {
        let options = [];
        if( deviceRequest.codeCodeableConcept && deviceRequest.codeCodeableConcept.coding) {
            const system =  deviceRequest.codeCodeableConcept.coding[0].system;
            if (system === 'https://bluebutton.cms.gov/resources/codesystem/hcpcs') {
                options = hcpcsCodes;
            }else if (system === 'http://www.ama-assn.org/go/cpt') {
                options = cptCodes;
            } else if (system === 'http://www.nlm.nih.gov/research/umls/rxnorm') {
                options = rxNormCodes;
            }
        }
        return (
            <StateSelect 
            callback={handleCode} 
            state={getCode()} 
            label="Code" 
            element="code" 
            options={options}
            />
        )
    }

    return (
        <div>
            <div style={{display: 'inline-block'}}>
                <StateSelect 
                    callback={handleSystem} 
                    state={getSystem()} 
                    label="CodeSystem" 
                    element="system" 
                    options={codeSystems}
                />
                {renderCode()}
            </div>
            <JsonBox 
                callback={handleJson}
                placeholder={deviceRequest}
            />
        </div>
    );
}

DeviceRequestInfo.propTypes = {
    callback: PropTypes.func
};
