import React from 'react';
import { expect } from 'chai';
import { mount, shallow } from 'enzyme';
import Enzyme from 'enzyme';
import { spy } from 'sinon';
import ReactSixteenAdapter from 'enzyme-adapter-react-16';
import RequestBuilder from '../containers/RequestBuilder';
import KJUR, {KEYUTIL} from 'jsrsasign';
Enzyme.configure({ adapter: new ReactSixteenAdapter() });
const wrapper = mount(<RequestBuilder createJWT={false}/>);

describe("Function testing", function() {
    it('logs to console', function() {
        wrapper.instance().consoleLog("foobar", "warningClass")
        expect(wrapper.state().logs).to.eql([{content:"foobar", type:'warningClass'}]); 
    });
    it('creates valid JWT', function() {
        var prvkey = { kty: 'RSA',
        n: 'dItzvOOpxzGgUOQo_NGDGWipIYP3UzeL_Ti4vihfY9CV3jxvrql9cndW3vVaudlGVBEU8_C0t_lKE9Z6KGESKoMqDsaYLevlNc02YD9U-09skO_d7Qxt0sQFm9c58mhWUgdEhX5rGdIFtY0AqiJhD0dJMr3UkNoMpFzGSM0Et9Lt9ZfT9UEbJqutBrNNGeTKdxwa4J6oy50xz9VN5S8y4Yw5aWzofZ6zU7EAi_xdjN9FsewqtEcFxsvny5wNo35mGnOL8EuAOU-zZRDs-Gdd6BiQZmWflcQJqtz5f6DkJdV9Ta0sN2hE09hMtsTE-de5LniVzCH4IhjGJ4_13lL0mw',      e: 'AQAB',
        d: 'H8XhAM8keyzZYxRA5GQOFMBG099RbzuGi7uKCjWhg-na4eKiJAELNUi5w0Eqwd4tYT_i8XpfGoT5IB1p4lZMiJCHx812sZZNP_S5bVX2yijmFUF_xF-OFVbhnJH5t0-gsO5QTQChHEH40n0lgDytL2gk2ZONPwxjQ_ATEsfqEF8tKg0F-DD1qSfY9xUrBIqkotP9z11IjwV1hUhgtJqbFqtllvezrczHC0sf1NeBQZpTk4-OniU9aAYa2NM_rfjCkq-Jg2EeGp4u7Yx3ZRwUwo_SsVXUeff7dx8Uhxd9YIGsSFY9yBnG-HvzfM87WOULsORjGE31OG_7KFUDSdLK0Q',
        p: '2yR2h8VWhgD7v4oZlWLGLzA2C3jxDuQxTdVTwbV8-Y8fGDpw7RgGObW0dKBnGLh29VgcuBKlvylSEOnY3nyP4JiawNcl3br2igzyevKoz5iU_4rEHpOVyAL2687P2pe9gd-jBGNycHZ0KSxjE1IPX7DzEFD2pAHmIiKGFnzYwjM',
        q: 'iCV6EvyCFwea8xq1g0AAK8_dwUwnDBSGqXdgdtZ01Rt8RUynUoh6XBJUBIyGlMdnTu8XwlQWoRuKnpLQM4ExQQM2rrcpmMEVnG5oZLtnRWK_iiLDKc-GOYfEaojE8A0VNJz058Md486G4oXqT2CQ_eRIEYOUPuZz5YmdK7kiq_k',
        dp: 'FMFEVKGVC0YGkeKDf8mGGawIQlfnBNEJ9mQw9ZS5zG0IteO7FEmFNWwHNlCG0ymNYrlApxJlDlDci-uJ9bB4c_4gEgmOdYyikvqc8JYQ9PBvlDBRWNADjJjzR-wjdYGPmkBTETRV_17Iq6mfjJHmENsSekYgxfHEGRGJ_5E9ek8',
        dq: 'ci8kXAolPRVm0l9jPqoNurECplytHweUDUuOvvyMTjNYHXL1nlXScaf5iTsmoF9A-MW0IoqN1B8aOnadYaUH3yPH1-7MV04RRvqeWWWHbn0scblH_6gKRfPlOwhsS3xdfZvqFvrg9a_AFvoDkwFFeXrdKrs1OS_koe2xwPrr5lk',
        qi: 'pMY6Jg9Wq_2zWtEWa5Do3rWRMmax2T5VeiJnabts0ykLQ2FybHIrFLZRpJead3bpPGtiL5Ck9mpkSwF8g0UBZH8xlgldnWPpvq3QjpVoKZN6doGo5vthNlti-OfTPviOevYTvXykJAfHtrgxEVVmR-k4efUiyqqMj9BNGpiByPo' };
        var pubkey =  { kty: 'RSA',
        n: 'dItzvOOpxzGgUOQo_NGDGWipIYP3UzeL_Ti4vihfY9CV3jxvrql9cndW3vVaudlGVBEU8_C0t_lKE9Z6KGESKoMqDsaYLevlNc02YD9U-09skO_d7Qxt0sQFm9c58mhWUgdEhX5rGdIFtY0AqiJhD0dJMr3UkNoMpFzGSM0Et9Lt9ZfT9UEbJqutBrNNGeTKdxwa4J6oy50xz9VN5S8y4Yw5aWzofZ6zU7EAi_xdjN9FsewqtEcFxsvny5wNo35mGnOL8EuAOU-zZRDs-Gdd6BiQZmWflcQJqtz5f6DkJdV9Ta0sN2hE09hMtsTE-de5LniVzCH4IhjGJ4_13lL0mw',
        e: 'AQAB' };
        var signedJWT = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ikdtem51SDkxaTRpMG5mUlplQUdiQWlfbHZCWG5VZGl1NlNaUWpyTUJTcjAiLCJqa3UiOiJodHRwOi8vbG9jYWxob3N0OjMwMDEvcHVibGljX2tleXMifQ.eyJpc3MiOiJsb2NhbGhvc3Q6MzAwMCIsImF1ZCI6InI0L29yZGVyLXJldmlldy1zZXJ2aWNlcyIsImlhdCI6MTAwLCJleHAiOjUwMCwianRpIjoiZm9vYmFyIn0.M-EOb0mzIyiTgIdlls-tTOjrGUFQFk3Szs8o1N364T6V3SAKvWxs1Z_07NDEK8ZwJmC_Gqrr7TNkBV95V-tUvFVeOdtbRZxqFwhGfbSyQe8xTdqRXJRlLqzZZzbECZISnPlMNxsROVJiM7GVG9LxRaeAiulBbS3oOy9bNOdQaVo_bbSK3YPqE3j-5l7qMlOQO889k_sjIRz1kURSC2R39QIGcL9Cwxg17OKTkUfTkQfQH8x6XqOsQv8kgyzLtecUfYqds1lhg-It5U1Kl1_chGt9KpyfxvMTVBiH3hcWKsX3sJKX8N9s_lm6kAF12K9-4vA0g2XNdwAvqXfKBUewuw";
        var keypair = {};
        keypair.pubKeyObj = KEYUTIL.getKey(pubkey);
        keypair.prvKeyObj = KEYUTIL.getKey(prvkey);
        wrapper.setState({"keypair": keypair}); //  jti   start end
        // fix the random portions of the key        |      |   |
        var result = wrapper.instance().createJwt("foobar",100,500);
        // returns promise
        result.then(function(value){
            expect(signedJWT).to.equal(value);
        });

    });

    it('updates state elements', function() {
        wrapper.instance().updateStateElement("age",90);
        expect(wrapper.state().age).to.equal(90)
    });
  });

  describe("Components Mount", function() {
      it("mounts checkboxes",function(){
          expect(wrapper.find("#checkbox").length).to.equal(2);
      });
      it("mounts console box",function(){
          expect(wrapper.find("#your_div").length).to.equal(1);
      });
      it("mounts dropdown box",function(){
          // the dropdown is a semantic-ui component and the class
          // dropdownCode refers to two components despite only one
          // getting rendered
          expect(wrapper.find(".dropdownCode").length).to.equal(2);
        //   expect(wrapper.find("#henlo").length).to.equal(1);
      });

      it("mounts state dropdowns",function(){
          expect(wrapper.find(".dropdownState").length).to.equal(4);
      })
      it("mounts text input",function(){
        expect(wrapper.find(".input-text").length).to.equal(1);
    });
    it("mounts toggle", function(){
        expect(wrapper.find(".genderBlockMale").length).to.equal(1);
        expect(wrapper.find(".genderBlockFemale").length).to.equal(1);
    })
  })