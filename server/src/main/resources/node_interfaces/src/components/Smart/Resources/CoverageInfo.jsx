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
import DialogTitle from '@material-ui/core/DialogTitle';
import Dialog from '@material-ui/core/Dialog';
import { coverageCodes, organizations } from '../constants';
import StateSelect from './Sections/StateSelect';
import { stateOptions } from '../constants';
import JsonBox from './Sections/JsonBox';

export default function CoverageInfo(props) {
    const classes = useStyles();
    const [backdrop, setBackdrop] = useState(false);
    const { callback, coverage, organization } = props;
    const { classValue, type, orgName } = coverage;
    const handleChange = (element, value) => {
        callback('coverage', element, value)
    }

    const handleChangeOrg = (element, value) => {
        callback('coverageOrg', element, value);
    }

    const handleJson = (e) => {
        if (e.jsObject) {
            callback('coverage', 'resource', e.jsObject.coverage);
            callback('coverageOrg', 'resource', e.jsObject.organization);
        }
    }

    const loadOrg = () => {
    }

    const getClassValue = () => {
        if(coverage.class && coverage.class.length > 0) {
            return coverage.class[0].value
        }
    }

    const getCode = () => {
        if(coverage.class && coverage.class.length > 0 && coverage.class[0].type) {
            return coverage.class[0].type.code;
        }    
    }

    const getOrg = () => {
        if(organization.name) {
            return organization.name;
        }
    }

    const handleClassValue = (event) => {
        if( coverage.class && coverage.class.length > 0) {
            const classList = {...coverage.class[0]}; // copy coverage.class
            classList.value = event.target.value;
            handleChange('class', [classList]);
        } else {
            const covClass = [{
                    "type": {
                        "system": "http://hl7.org/fhir/coverage-class",
                    },
                    "value": event.target.value
                }]
            handleChange('class', covClass);
        }
    }

    const handleOrg = (event) => {
        handleChangeOrg('name', event.target.value);
    }

    const handleCode = (event) => {
        if( coverage.class && coverage.class.length > 0) {
            const classList = {...coverage.class[0]}; // copy coverage.class
            classList.type = {
                "system": "http://hl7.org/fhir/coverage-class",
                "code": event.target.value
            };
            handleChange('class', [classList]);
        } else {
            const covClass = [{
                "type": {
                    "system": "http://hl7.org/fhir/coverage-class",
                    "code": event.target.value
                },
            }];
            handleChange('class', covClass);
        }
    }

    console.log(getOrg());
    return (
        <div>
            <div style={{display: 'inline-block'}}>
            <TextField
                id="outlined-number"
                label="Class Value"
                type="text"
                style={{
                    width: 'auto',
                }}
                InputLabelProps={{
                    shrink: true,
                }}
                value={getClassValue() || ''}
                onChange={handleClassValue}
            />
            <StateSelect 
                callback={handleCode} 
                state={getCode()} 
                label="Code" 
                element="type" 
                options={coverageCodes}
            />
            <hr />
            <div className={classes.orgBox}>
                Organization
                <div>
                    <StateSelect 
                        callback={handleOrg} 
                        state={getOrg()} 
                        label="Name" 
                        element="name" 
                        options={organizations}
                    />
                </div>
            </div>

            </div>
            <JsonBox 
                callback={handleJson}
                placeholder={{coverage, organization}}
            />
        </div>
    );
}

CoverageInfo.propTypes = {
    callback: PropTypes.func
};
