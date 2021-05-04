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
import { stateOptions } from '../constants';
import JsonBox from './Sections/JsonBox';

export default function PatientInfo(props) {
    const classes = useStyles();
    const { callback, patient } = props;

    const handleChange = (element, value) => {
        callback('patient', element, value)
    }

    const handleJson = (e) => {
        if (e.jsObject) {
            callback('patient', 'resource', e.jsObject);
        }
    }

    const getApproxBirthdate = (age) => {
        var d = new Date();
        var n = d.getFullYear();
        if (age) {
            const birthYear = n - age;
            let newDate = new Date(birthYear, d.getMonth(), d.getDate());
            let ye = new Intl.DateTimeFormat('en', { year: 'numeric' }).format(newDate);
            let mo = new Intl.DateTimeFormat('en', { month: '2-digit' }).format(newDate);
            let da = new Intl.DateTimeFormat('en', { day: '2-digit' }).format(newDate);
            const dateString = `${ye}-${mo}-${da}`;            
            return dateString;
        }
    }

    const getAge = () => {
        const dateString = patient.birthDate;
        var today = new Date();
        var birthDate = new Date(dateString);
        var age = today.getFullYear() - birthDate.getFullYear();
        var m = today.getMonth() - birthDate.getMonth();
        if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }
        return age;
    }

    const getState = () => {
        if(patient.address) {
            return patient.address[0].state
        } else {
            return ''
        }
    }

    const handleState = (event) => {
        const address = [{
            "use": "home",
            "type": "both",
            "state": event.target.value
        }];
        handleChange('address', address);
    }

    const handleAge = (event) => {
        const age = getApproxBirthdate(event.target.value);
        handleChange('birthDate', age);
    }

    const renderGender = () => {
        return (
            <div className={classes.genderBox}>
                <FormControl component="fieldset">
                    <FormLabel component="legend">Gender</FormLabel>
                    <RadioGroup aria-label="gender" name="gender1" value={patient.gender || ''} onChange={(event) => {handleChange('gender', event.target.value)}}>
                        <FormControlLabel value="female" control={<Radio />} label="Female" />
                        <FormControlLabel value="male" control={<Radio />} label="Male" />
                        <FormControlLabel value="other" control={<Radio />} label="Other" />
                    </RadioGroup>
                </FormControl>
            </div>
        )
    }

    const renderAge = () => {
        return (
          <div className={classes.ageBox}>
            <TextField
                id="outlined-number"
                label="Age"
                type="number"
                style={{
                    width: '60px',
                }}
                InputLabelProps={{
                    shrink: true,
                }}
                value={getAge() || ''}
                onChange={handleAge}
            />
          </div>
        )
    }
    return (
        <div>
            {renderGender()}
            {renderAge()}
            <StateSelect callback={handleState} state={getState()} label="State" element="address" options={stateOptions}></StateSelect>
            <JsonBox 
                callback={handleJson}
                placeholder={patient}
            />
        </div>
    );
}

PatientInfo.propTypes = {
    callback: PropTypes.func
};
