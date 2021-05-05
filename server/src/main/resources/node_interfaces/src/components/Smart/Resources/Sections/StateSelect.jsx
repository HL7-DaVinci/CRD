import React, { useEffect, useState } from 'react';
import useStyles from '../../styles';
import PropTypes from 'prop-types';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';
import TextField from '@material-ui/core/TextField';
import Select from '@material-ui/core/Select';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import { stateOptions } from '../../constants';

export default function StateSelect(props) {
    const classes = useStyles();
    const { callback, state, label, options} = props;

    return (
        <>
            <FormControl className={classes.stateOptions}>
                <InputLabel id="demo-simple-select-label">{label}</InputLabel>
                <Select
                    labelId="demo-simple-select-label"
                    id="demo-simple-select"
                    value={state || ''}
                    onChange={callback}
                >
                    {options.map((option) => {
                        return  <MenuItem key={option.key} value={option.value}>{option.text}</MenuItem>

                    })}
                    <MenuItem value={''}>
                        <em>None</em>
                    </MenuItem>
                </Select>
            </FormControl>
        </>
    );
}

StateSelect.propTypes = {
    callback: PropTypes.func
};
