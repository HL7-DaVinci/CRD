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
import StateSelect from './Sections/StateSelect';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import { coverageCodes } from '../constants';
import { stateOptions } from '../constants';
import JsonBox from './Sections/JsonBox';

export default function PractitionerInfo(props) {
    const classes = useStyles();
    const { callback, practitioner, role, org, location } = props;
    const [backdrop, setBackdrop] = useState(false);

    const handleChange = (element, value) => {
        callback('practitioner', element, value)
    }

    const handleJson = (e) => {
        if (e.jsObject) {
            callback('practitioner', 'resource', e.jsObject.practitioner);
            callback('practitionerOrg', 'resource', e.jsObject.organization);
            callback('practitionerRole', 'resource', e.jsObject.practitionerRole);
            callback('practitionerLocation', 'resource', e.jsObject.location);


        }
    }

    const handleState = (event) => {
        const loc = {
            "line": [
                "100 Good St"
            ],
            "city": "Bedford",
            'state': event.target.value,
        }
        callback('practitionerLocation', 'address', loc);
    }

    const handleOrg = (event) => {
        callback('practitionerOrg','name', event.target.value);
    }

    const getOrg = () => {
        return org.name;
    }

    const getState = () => {
        if(location.address) {
            return location.address.state
        }
    }

    return (
        <div>
            <div style={{display: 'inline-block'}}>

            <StateSelect callback={handleState} state = {getState()} element="state" label="State" options={stateOptions}></StateSelect>
            <hr />
            <div className={classes.orgBox}>
                Organization
                <div>
                    <TextField
                        id="outlined-number"
                        label="Name"
                        type="text"
                        style={{
                            width: 'auto',
                        }}
                        InputLabelProps={{
                            shrink: true,
                        }}
                        value={getOrg() || ''}
                        onChange={handleOrg}
                    />
                </div>

            </div>
            </div>
            <JsonBox 
                callback={handleJson}
                placeholder={{practitioner, 'practitionerRole':role, 'organization':org, 'location':location}}
            />
        </div>
    );
}

PractitionerInfo.propTypes = {
    callback: PropTypes.func
};
