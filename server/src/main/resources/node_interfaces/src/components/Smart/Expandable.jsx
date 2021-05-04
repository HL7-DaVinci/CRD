import React, { useState } from 'react';
import useStyles from './styles';
import PropTypes from 'prop-types';


export default function Expandable(props) {
    const classes = useStyles();
    const { title, Fragment, callback } = props;
    const [open, setOpen] = useState(false);
    const resourceClass = open ? `${classes.resourceBar} ${classes.openBar}` : classes.resourceBar;

    return (
        <div className={resourceClass} >
            <div onClick={() => { setOpen(!open) }}>{title}</div>
            <hr style={{marginTop: '9px'}}/>
            <div className={classes.inputs}>
                {Fragment}
            </div>
        </div>
    );
}

Expandable.propTypes = {
    Fragment: PropTypes.object.isRequired,
    title: PropTypes.string.isRequired,
    callback: PropTypes.func
};
