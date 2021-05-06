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
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Dialog from '@material-ui/core/Dialog';
import { stateOptions } from '../../constants';

export default function OrgSelect(props) {
    const classes = useStyles();
    const { onClose, selectedValue, open } = props;

    const handleClose = () => {
      onClose(selectedValue);
    };
  
    const handleListItemClick = (value) => {
      onClose(value);
    };
    return (
        <>
            <Dialog onClose={handleClose} aria-labelledby="simple-dialog-title" open={open}>
                <DialogTitle id="simple-dialog-title">Choose Organization</DialogTitle>
                <List>
                    {['a','b','c'].map((e) => (
                    <ListItem button onClick={() => handleListItemClick(e)} key={e}>
                        <ListItemText primary={e} />
                    </ListItem>
                    ))}
                </List>
            </Dialog>
        </>
    );
}

OrgSelect.propTypes = {
    onClose: PropTypes.func,
    selectedValue: PropTypes.string,
    open: PropTypes.bool
};
