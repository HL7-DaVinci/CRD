import React from "react";
import './DataTable.css';

const Select = props => {
  let currentState = props.currentState;
  return (
    
    <div>
      <div className="selectBoxWrapper">

      <select
      className="selectBox"
      defaultValue={props.currentState}
      onChange={props.onChangeCB}
      >
        {props.options.map(option => {
          return (
            <option 
            key={option.key} 
            value={option.value} 
            label={option.text}
            >
              {option.value}
            </option>
          );
        })}

      </select>
      </div>
    </div>


  );
};

export default Select;