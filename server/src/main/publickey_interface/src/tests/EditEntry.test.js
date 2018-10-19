import React from 'react';
import { expect } from 'chai';
import { mount, shallow } from 'enzyme';
import Enzyme from 'enzyme';
import { spy } from 'sinon';
import ReactSixteenAdapter from 'enzyme-adapter-react-16';
import EditEntry from '../components/EditEntry';
Enzyme.configure({ adapter: new ReactSixteenAdapter() });
const wrapper = shallow(<EditEntry />);

describe("Check edit box functionality",()=>{
    it("generates keyID",()=>{
        var keyID = wrapper.instance().getKeyID("-----BEGIN CERTIFICATE-----MIICMzCCAZygAwIBAgIJALiPnVsvq8dsMA0GCSqGSIb3DQEBBQUAMFMxCzAJBgNVBAYTAlVTMQwwCgYDVQQIEwNmb28xDDAKBgNVBAcTA2ZvbzEMMAoGA1UEChMDZm9vMQwwCgYDVQQLEwNmb28xDDAKBgNVBAMTA2ZvbzAeFw0xMzAzMTkxNTQwMTlaFw0xODAzMTgxNTQwMTlaMFMxCzAJBgNVBAYTAlVTMQwwCgYDVQQIEwNmb28xDDAKBgNVBAcTA2ZvbzEMMAoGA1UEChMDZm9vMQwwCgYDVQQLEwNmb28xDDAKBgNVBAMTA2ZvbzCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAzdGfxi9CNbMf1UUcvDQh7MYBOveIHyc0E0KIbhjK5FkCBU4CiZrbfHagaW7ZEcN0tt3EvpbOMxxc/ZQU2WN/s/wPxph0pSfsfFsTKM4RhTWD2v4fgk+xZiKd1p0+L4hTtpwnEw0uXRVd0ki6muwV5y/P+5FHUeldq+pgTcgzuK8CAwEAAaMPMA0wCwYDVR0PBAQDAgLkMA0GCSqGSIb3DQEBBQUAA4GBAJiDAAtY0mQQeuxWdzLRzXmjvdSuL9GoyT3BF/jSnpxz5/58dba8pWenv3pj4P3w5DoOso0rzkZy2jEsEitlVM2mLSbQpMM+MUVQCQoiG6W9xuCFuxSrwPISpAqEAuV4DNoxQKKWmhVv+J0ptMWD25Pnpxeq5sXzghfJnslJlQND-----END CERTIFICATE-----");
        expect(keyID).to.equal("WZppmYt5d9G-6EOxBtOCdrnqS8zH_4I2LBfPprV9gjk");
    });
    it("automatically generates keyID",()=>{
        wrapper.find(".checkBox").simulate('click');
        wrapper.find(".jwtTextArea").simulate('change', {target: {value:"-----BEGIN PUBLIC KEY-----MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCqGKukO1De7zhZj6+H0qtjTkVxwTCpvKe4eCZ0FPqri0cb2JZfXJ/DgYSF6vUpwmJG8wVQZKjeGcjDOL5UlsuusFncCzWBQ7RKNUSesmQRMSGkVb1/3j+skZ6UtW+5u09lHNsj6tQ51s1SPrCBkedbNf0Tp0GbMJDyR4e9T04ZZwIDAQAB-----END PUBLIC KEY-----"}});
        expect(wrapper.state().kid).to.equal("7NQlxI5TqY9CtGYwf5yw8p3XXpT7SJNMol2xvbyUtGU");

    });
})