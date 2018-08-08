import _ from 'lodash';
import {Component} from 'react';
function component () {

  var element = document.createElement('div');

  /* lodash is used here for bundling demonstration purposes */
  element.innerHTML = _.join(['Andy', 'is', 'on', 'vacation'], ' ');

  return element;
}

document.body.appendChild(component());

