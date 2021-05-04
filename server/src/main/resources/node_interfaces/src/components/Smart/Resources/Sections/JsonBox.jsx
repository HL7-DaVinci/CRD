import React, { useEffect, useState } from 'react';
import useStyles from '../../styles';
import PropTypes from 'prop-types';
import JSONInput from 'react-json-editor-ajrm';
import locale from 'react-json-editor-ajrm/locale/en';

export default function JsonBox(props) {
    const classes = useStyles();
    const { callback, placeholder } = props;
    const [open, setOpen] = useState(false)

    return (
        <>
            <div className={classes.jsonInput}>
                {open ? 
                <>
                <button onClick={()=>{setOpen(false)}}>hide</button>
                    <JSONInput
                        id          = 'ex2'
                        placeholder = { placeholder }
                        locale      = { locale }
                        height      = '280px'
                        onChange = {callback}
                    />
                </>
                :
                <button onClick={()=>{setOpen(true)}}> Edit Json </button>

                }

            </div>
        </>
    );
}

JsonBox.propTypes = {
    onClose: PropTypes.func,
    selectedValue: PropTypes.string,
    open: PropTypes.bool
};
