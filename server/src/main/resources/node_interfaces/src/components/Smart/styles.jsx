import { makeStyles } from '@material-ui/core/styles';

export default makeStyles(
  theme => ({
    inputs: {
        marginTop: '15px',
        height:'310px',
    },
    resourceBar: {
        border: '1px solid black',
        borderRadius: '15px',
        height: '40px',
        padding: '9px',
        transition: 'height .5s',
        overflow:'hidden',
    },
    openBar: {
        height: '350px'
    },
    genderBox: {
        // border: '1px solid #111',
        display: 'inline-block',
        borderRadius: '5px',
        width: '120px',
        padding: '0 15px 15px 15px',
    },
    ageBox: {
        marginLeft:'15px',
        display: 'inline-block',

    },
    stateOptions: {
        marginLeft:'15px',
        width: '160px'
    },
    menu: {
        position: 'fixed',
        zIndex: 1000,
        top: '20%',
        left: '35%',
        height: '400px',
        minWidth: '400px',
        backgroundColor: 'white',
        border: '1px solid #444',
        borderRadius: '5px',
        float:'left',
    },
    backdrop: {
        position: 'fixed',
        height: '100%',
        width: '100%',
        top: 0,
        left: 0,
        zIndex: 999,
        backgroundColor: '#999A'
    },
    loadButton:{
        marginTop: '15px',
    },
    menuHeader: {
        position: 'fixed',
        backgroundColor: 'white',
        zIndex:1002,
        padding:'5px',
        height: '30px',
        width:'385px',
        borderBottom: '1px solid black'
    },
    jsonInput: {
        float: 'right',
    },
    orgBox: {
        float:'left',
    }
  }),

  { name: 'index', index: 1 }
);
