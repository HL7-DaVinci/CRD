import React from 'react';
import { expect } from 'chai';
import { mount, shallow } from 'enzyme';
import Enzyme from 'enzyme';
import { spy } from 'sinon';
import ReactSixteenAdapter from 'enzyme-adapter-react-16';
import KeyEntry from '../components/KeyEntry';
Enzyme.configure({ adapter: new ReactSixteenAdapter() });
const wrapper = shallow(<KeyEntry kid="exKey1" jwt="-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCqGKukO1De7zhZj6+H0qtjTkVxwTCpvKe4eCZ0
FPqri0cb2JZfXJ/DgYSF6vUpwmJG8wVQZKjeGcjDOL5UlsuusFncCzWBQ7RKNUSesmQRMSGkVb1/
3j+skZ6UtW+5u09lHNsj6tQ51s1SPrCBkedbNf0Tp0GbMJDyR4e9T04ZZwIDAQAB
-----END PUBLIC KEY-----" updateIdCB={()=>{
    return null;}} />)

describe("Function unit tests",()=>{
    it("mounts and transforms data",()=>{
        expect(wrapper.state().jwt).to.equal('{"kty":"RSA","n":"qhirpDtQ3u84WY-vh9KrY05FccEwqbynuHgmdBT6q4tHG9iWX1yfw4GEher1KcJiRvMFUGSo3hnIwzi-VJbLrrBZ3As1gUO0SjVEnrJkETEhpFW9f94_rJGelLVvubtPZRzbI-rUOdbNUj6wgZHnWzX9E6dBmzCQ8keHvU9OGWc","e":"AQAB"}');
    });
    it("updates keyID",()=>{
        wrapper.find(".buttonContent").find(".addButton").simulate('click',{ preventDefault() {} });
        wrapper.find(".jwtContent").find(".editInput").simulate('change', {target: {value: '{"kty":"RSA","n":"DtQ3u84WY-vh9KrY05FccEwqbynuHgmdBT6q4tHG9iWX1yfw4GEher1KcJiRvMFUGSo3hnIwzi-VJbLrrBZ3As1gUO0SjVEnrJkETEhpFW9f94_rJGelLVvubtPZRzbI-rUOdbNUj6wgZHnWzX9E6dBmzCQ8keHvU9OGWc","e":"AQAB"}'}});
        wrapper.find(".buttonContent").find(".addButton").simulate('click',{ preventDefault() {} });
        expect(wrapper.state().kid).to.equal("3yObrRusroCjpa4zecMMP8Pyc8uXWWGu25XhI1Kbixg")
    })
});