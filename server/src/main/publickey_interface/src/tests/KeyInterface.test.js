import React from 'react';
import { expect } from 'chai';
import { mount, shallow } from 'enzyme';
import Enzyme from 'enzyme';
import { spy } from 'sinon';
import ReactSixteenAdapter from 'enzyme-adapter-react-16';
import KeyInterface from '../containers/KeyInterface';
Enzyme.configure({ adapter: new ReactSixteenAdapter() });
const wrapper = mount(<KeyInterface doFetch={false}/>)
describe("Component mounting tests", function() {
  it('Renders header', function() {
    expect(wrapper.find('h1').text()).to.equal("Public Keys");
  });
  it("Renders buttons",()=>{
    expect(wrapper.find("button").getElements().length).to.equal(2);
  });


  it("Renders key entry",()=>{
    wrapper.setState({jwtJson:[{"hello":"theKey"}]})
    expect(wrapper.find(".jwtContent").find(".kidBox").text()).to.equal("ID: hello");
  })
});

describe("Buttons function properly", function(){

  it("Allows editing of key",()=>{
    wrapper.find(".buttonContent").find(".addButton").simulate('click');
    expect(wrapper.find(".jwtContent").find(".editInput").exists()).to.equal(true);

  });

  it("Updates key when changes are made", ()=>{
    wrapper.find(".jwtContent").find(".editInput").simulate('change', {target: {value: 'newKey'}});
    wrapper.find(".buttonContent").find(".addButton").simulate('click');
    expect(wrapper.find(".jwtContent").find(".keyData").text()).to.equal("newKey");
  });

  it("deletes key entries",()=>{
    expect(wrapper.find(".buttonContent").find(".deleteButton").exists()).to.equal(true);
    wrapper.find(".buttonContent").find(".deleteButton").simulate('click');
    expect(wrapper.find(".buttonContent").exists()).to.equal(false);
  });

  it("Opens edit window ",()=>{
    wrapper.find("#addButton").simulate('click');
    expect(wrapper.find("#editEntry").exists()).to.equal(true);
  });

  it("Closes edit window",()=>{
    wrapper.find("#editDeleteButton").simulate('click');
    expect(wrapper.find("#editEntry").exists()).to.equal(false);
  });

  it("Submits new entries",()=>{
    wrapper.find("#addButton").simulate('click');
    wrapper.find(".checkBox").simulate('click');
    wrapper.find("#editEntry").find(".jwtTextArea").simulate('change', {target: {value:"-----BEGIN PUBLIC KEY-----MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCqGKukO1De7zhZj6+H0qtjTkVxwTCpvKe4eCZ0FPqri0cb2JZfXJ/DgYSF6vUpwmJG8wVQZKjeGcjDOL5UlsuusFncCzWBQ7RKNUSesmQRMSGkVb1/3j+skZ6UtW+5u09lHNsj6tQ51s1SPrCBkedbNf0Tp0GbMJDyR4e9T04ZZwIDAQAB-----END PUBLIC KEY-----"}});
    wrapper.find("#editEntry").find(".clickButton").simulate('click');
    expect(wrapper.find(".keyData").text()).to.equal('{"kty":"RSA","n":"qhirpDtQ3u84WY-vh9KrY05FccEwqbynuHgmdBT6q4tHG9iWX1yfw4GEher1KcJiRvMFUGSo3hnIwzi-VJbLrrBZ3As1gUO0SjVEnrJkETEhpFW9f94_rJGelLVvubtPZRzbI-rUOdbNUj6wgZHnWzX9E6dBmzCQ8keHvU9OGWc","e":"AQAB"}');
  })

});

describe("Function unit testing",()=>{

  it("Submits content",()=>{
    wrapper.instance().submitContent("example","testKey");
    expect(wrapper.state().jwtJson[0].example).to.equal("testKey");

  });

  it("Initializes data",()=>{
    const data = [{id:"ex1",key:"ex1key"},{id:"ex2",key:"ex2key"}];
    wrapper.instance().initData(data);
  });

  it("Allows ID changes",()=>{
    wrapper.setState({jwtJson:[{ex1:"ex1key"}]});
    wrapper.instance().updateIdCB("ex1","ex2","ex2key");
    expect(wrapper.state().jwtJson[0].ex2).to.equal('"ex2key"');
  });

})