!function(t){function n(r){if(e[r])return e[r].exports;var o=e[r]={i:r,l:!1,exports:{}};return t[r].call(o.exports,o,o.exports,n),o.l=!0,o.exports}var e={};n.m=t,n.c=e,n.d=function(t,e,r){n.o(t,e)||Object.defineProperty(t,e,{enumerable:!0,get:r})},n.r=function(t){"undefined"!=typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(t,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(t,"__esModule",{value:!0})},n.t=function(t,e){if(1&e&&(t=n(t)),8&e)return t;if(4&e&&"object"==typeof t&&t&&t.__esModule)return t;var r=Object.create(null);if(n.r(r),Object.defineProperty(r,"default",{enumerable:!0,value:t}),2&e&&"string"!=typeof t)for(var o in t)n.d(r,o,function(n){return t[n]}.bind(null,o));return r},n.n=function(t){var e=t&&t.__esModule?function(){return t.default}:function(){return t};return n.d(e,"a",e),e},n.o=function(t,n){return Object.prototype.hasOwnProperty.call(t,n)},n.p="/",n(n.s=382)}({10:function(t,n,e){var r=e(6),o=e(23),i=e(12),u=e(15),c=e(27),a="prototype",s=function(t,n,e){var f,l,p,v,d=t&s.F,y=t&s.G,h=t&s.S,x=t&s.P,g=t&s.B,m=y?r:h?r[n]||(r[n]={}):(r[n]||{})[a],b=y?o:o[n]||(o[n]={}),S=b[a]||(b[a]={});for(f in y&&(e=n),e)p=((l=!d&&m&&void 0!==m[f])?m:e)[f],v=g&&l?c(p,r):x&&"function"==typeof p?c(Function.call,p):p,m&&u(m,f,p,t&s.U),b[f]!=p&&i(b,f,v),x&&S[f]!=p&&(S[f]=p)};r.core=o,s.F=1,s.G=2,s.S=4,s.P=8,s.B=16,s.W=32,s.U=64,s.R=128,t.exports=s},100:function(t,n,e){var r=e(13),o=e(9),i=e(33);t.exports=e(8)?Object.defineProperties:function(t,n){o(t);for(var e,u=i(n),c=u.length,a=0;c>a;)r.f(t,e=u[a++],n[e]);return t}},101:function(t,n,e){var r=e(43),o=Math.max,i=Math.min;t.exports=function(t,n){return 0>(t=r(t))?o(t+n,0):i(t,n)}},102:function(t,n,e){var r=e(19),o=e(55),i=e(44)("IE_PROTO"),u=Object.prototype;t.exports=Object.getPrototypeOf||function(t){return t=o(t),r(t,i)?t[i]:"function"==typeof t.constructor&&t instanceof t.constructor?t.constructor.prototype:t instanceof Object?u:null}},103:function(t,n,e){e(8)&&"g"!=/./g.flags&&e(13).f(RegExp.prototype,"flags",{configurable:!0,get:e(64)})},12:function(t,n,e){var r=e(13),o=e(37);t.exports=e(8)?function(t,n,e){return r.f(t,n,o(1,e))}:function(t,n,e){return t[n]=e,t}},13:function(t,n,e){var r=e(9),o=e(71),i=e(61),u=Object.defineProperty;n.f=e(8)?Object.defineProperty:function(t,n,e){if(r(t),n=i(n,!0),r(e),o)try{return u(t,n,e)}catch(t){}if("get"in e||"set"in e)throw TypeError("Accessors not supported!");return"value"in e&&(t[n]=e.value),t}},14:function(t){t.exports=function(t){try{return!!t()}catch(t){return!0}}},15:function(t,n,e){var r=e(6),o=e(12),i=e(19),u=e(30)("src"),c="toString",a=Function[c],s=(""+a).split(c);e(23).inspectSource=function(t){return a.call(t)},(t.exports=function(t,n,e,c){var a="function"==typeof e;a&&(i(e,"name")||o(e,"name",n)),t[n]===e||(a&&(i(e,u)||o(e,u,t[n]?""+t[n]:s.join(n+""))),t===r?t[n]=e:c?t[n]?t[n]=e:o(t,n,e):(delete t[n],o(t,n,e)))})(Function.prototype,c,function(){return"function"==typeof this&&this[u]||a.call(this)})},18:function(t){t.exports=function(t){if(null==t)throw TypeError("Can't call method on  "+t);return t}},19:function(t){var n={}.hasOwnProperty;t.exports=function(t,e){return n.call(t,e)}},21:function(t,n,e){for(var r=e(22),o=e(33),i=e(15),u=e(6),c=e(12),a=e(31),s=e(4),f=s("iterator"),l=s("toStringTag"),p=a.Array,v={CSSRuleList:!0,CSSStyleDeclaration:!1,CSSValueList:!1,ClientRectList:!1,DOMRectList:!1,DOMStringList:!1,DOMTokenList:!0,DataTransferItemList:!1,FileList:!1,HTMLAllCollection:!1,HTMLCollection:!1,HTMLFormElement:!1,HTMLSelectElement:!1,MediaList:!0,MimeTypeArray:!1,NamedNodeMap:!1,NodeList:!0,PaintRequestList:!1,Plugin:!1,PluginArray:!1,SVGLengthList:!1,SVGNumberList:!1,SVGPathSegList:!1,SVGPointList:!1,SVGStringList:!1,SVGTransformList:!1,SourceBufferList:!1,StyleSheetList:!0,TextTrackCueList:!1,TextTrackList:!1,TouchList:!1},d=o(v),y=0;y<d.length;y++){var h,x=d[y],g=v[x],m=u[x],b=m&&m.prototype;if(b&&(b[f]||c(b,f,p),b[l]||c(b,l,x),a[x]=p,g))for(h in r)b[h]||i(b,h,r[h],!0)}},22:function(t,n,e){"use strict";var r=e(50),o=e(72),i=e(31),u=e(24);t.exports=e(62)(Array,"Array",function(t,n){this._t=u(t),this._i=0,this._k=n},function(){var t=this._t,n=this._k,e=this._i++;return!t||e>=t.length?(this._t=void 0,o(1)):o(0,"keys"==n?e:"values"==n?t[e]:[e,t[e]])},"values"),i.Arguments=i.Array,r("keys"),r("values"),r("entries")},23:function(t){var n=t.exports={version:"2.5.7"};"number"==typeof __e&&(__e=n)},24:function(t,n,e){var r=e(73),o=e(18);t.exports=function(t){return r(o(t))}},27:function(t,n,e){var r=e(53);t.exports=function(t,n,e){return r(t),void 0===n?t:1===e?function(e){return t.call(n,e)}:2===e?function(e,r){return t.call(n,e,r)}:3===e?function(e,r,o){return t.call(n,e,r,o)}:function(){return t.apply(n,arguments)}}},30:function(t){var n=0,e=Math.random();t.exports=function(t){return"Symbol(".concat(void 0===t?"":t,")_",(++n+e).toString(36))}},31:function(t){t.exports={}},32:function(t){var n={}.toString;t.exports=function(t){return n.call(t).slice(8,-1)}},33:function(t,n,e){var r=e(74),o=e(54);t.exports=Object.keys||function(t){return r(t,o)}},36:function(t){t.exports=!1},37:function(t){t.exports=function(t,n){return{enumerable:!(1&t),configurable:!(2&t),writable:!(4&t),value:n}}},38:function(t,n,e){var r=e(43),o=Math.min;t.exports=function(t){return 0<t?o(r(t),9007199254740991):0}},382:function(t,n,e){"use strict";e.r(n);var r,o=e(21),i=(e.n(o),e(77)),u=(e.n(i),e(40)),c=(e.n(u),e(48)),a="7c47a01b-b7d8-41cf-a290-8ed607108e70",s=null,f=c.a.getUrlParameter("iss"),l=c.a.getUrlParameter("launch"),p="patient/*.read launch",v=Math.round(1e8*Math.random()).toString(),d=(window.location.protocol+"//"+window.location.host+window.location.pathname).replace("launch","index"),y=new XMLHttpRequest;y.open("GET",f+"/metadata?_format=json"),y.setRequestHeader("Content-Type","application/json"),y.setRequestHeader("Accept","application/json"),y.onload=function(){if(200!==y.status){var t="Conformance statement request failed. Returned status: "+y.status;return document.body.innerText=t,void console.error(t)}try{r=JSON.parse(y.responseText)}catch(t){var n="Unable to parse conformance statement.";return document.body.innerText=n,void console.error(n)}!function(t){var n,e;t.rest[0].security.extension.filter(function(t){return"http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris"===t.url})[0].extension.forEach(function(t){"authorize"===t.url?n=t.valueUri:"token"===t.url&&(e=t.valueUri)}),sessionStorage[v]=JSON.stringify({clientId:a,secret:s,serviceUri:f,redirectUri:d,tokenUri:e}),window.location.href=n+"?response_type=code&client_id="+encodeURIComponent(a)+"&scope="+encodeURIComponent(p)+"&redirect_uri="+encodeURIComponent(d)+"&aud="+encodeURIComponent(f)+"&launch="+encodeURIComponent(l)+"&state="+v}(r)},y.send()},39:function(t,n,e){var r=e(13).f,o=e(19),i=e(4)("toStringTag");t.exports=function(t,n,e){t&&!o(t=e?t:t.prototype,i)&&r(t,i,{configurable:!0,value:n})}},4:function(t,n,e){var r=e(51)("wks"),o=e(30),i=e(6).Symbol,u="function"==typeof i;(t.exports=function(t){return r[t]||(r[t]=u&&i[t]||(u?i:o)("Symbol."+t))}).store=r},40:function(t,n,e){"use strict";e(103);var r=e(9),o=e(64),i=e(8),u="toString",c=/./[u],a=function(t){e(15)(RegExp.prototype,u,t,!0)};e(14)(function(){return"/a/b"!=c.call({source:"a",flags:"b"})})?a(function(){var t=r(this);return"/".concat(t.source,"/","flags"in t?t.flags:!i&&t instanceof RegExp?o.call(t):void 0)}):c.name!=u&&a(function(){return c.call(this)})},43:function(t){var n=Math.ceil,e=Math.floor;t.exports=function(t){return isNaN(t=+t)?0:(0<t?e:n)(t)}},44:function(t,n,e){var r=e(51)("keys"),o=e(30);t.exports=function(t){return r[t]||(r[t]=o(t))}},45:function(t,n,e){"use strict";var r=e(12),o=e(15),i=e(14),u=e(18),c=e(4);t.exports=function(t,n,e){var a=c(t),s=e(u,a,""[t]),f=s[0],l=s[1];i(function(){var n={};return n[a]=function(){return 7},7!=""[t](n)})&&(o(String.prototype,t,f),r(RegExp.prototype,a,2==n?function(t,n){return l.call(t,this,n)}:function(t){return l.call(t,this)}))}},48:function(t,n,e){"use strict";var r=e(77),o=(e.n(r),e(78)),i=(e.n(o),e(80)),u=(e.n(i),{getUrlParameter:function(t){for(var n,e=window.location.search.substring(1).split("&"),r=0;r<e.length;r++)if((n=e[r].split("="))[0]===t){var o=n[1].replace(/\+/g,"%20");return decodeURIComponent(o)}}});n.a=u},50:function(t,n,e){var r=e(4)("unscopables"),o=Array.prototype;null==o[r]&&e(12)(o,r,{}),t.exports=function(t){o[r][t]=!0}},51:function(t,n,e){var r=e(23),o=e(6),i="__core-js_shared__",u=o[i]||(o[i]={});(t.exports=function(t,n){return u[t]||(u[t]=void 0===n?{}:n)})("versions",[]).push({version:r.version,mode:e(36)?"pure":"global",copyright:"© 2018 Denis Pushkarev (zloirock.ru)"})},52:function(t,n,e){var r=e(7),o=e(6).document,i=r(o)&&r(o.createElement);t.exports=function(t){return i?o.createElement(t):{}}},53:function(t){t.exports=function(t){if("function"!=typeof t)throw TypeError(t+" is not a function!");return t}},54:function(t){t.exports=["constructor","hasOwnProperty","isPrototypeOf","propertyIsEnumerable","toLocaleString","toString","valueOf"]},55:function(t,n,e){var r=e(18);t.exports=function(t){return Object(r(t))}},6:function(t){var n=t.exports="undefined"!=typeof window&&window.Math==Math?window:"undefined"!=typeof self&&self.Math==Math?self:Function("return this")();"number"==typeof __g&&(__g=n)},61:function(t,n,e){var r=e(7);t.exports=function(t,n){if(!r(t))return t;var e,o;if(n&&"function"==typeof(e=t.toString)&&!r(o=e.call(t)))return o;if("function"==typeof(e=t.valueOf)&&!r(o=e.call(t)))return o;if(!n&&"function"==typeof(e=t.toString)&&!r(o=e.call(t)))return o;throw TypeError("Can't convert object to primitive value")}},62:function(t,n,e){"use strict";var r=e(36),o=e(10),i=e(15),u=e(12),c=e(31),a=e(99),s=e(39),f=e(102),l=e(4)("iterator"),p=!([].keys&&"next"in[].keys()),v="keys",d="values",y=function(){return this};t.exports=function(t,n,e,h,x,g,m){a(e,n,h);var b,S,O,w=function(t){return!p&&t in L?L[t]:function(){return new e(this,t)}},_=n+" Iterator",j=x==d,T=!1,L=t.prototype,P=L[l]||L["@@iterator"]||x&&L[x],M=P||w(x),R=x?j?w("entries"):M:void 0,E="Array"==n&&L.entries||P;if(E&&((O=f(E.call(new t)))!==Object.prototype&&O.next&&(s(O,_,!0),!r&&"function"!=typeof O[l]&&u(O,l,y))),j&&P&&P.name!==d&&(T=!0,M=function(){return P.call(this)}),(!r||m)&&(p||T||!L[l])&&u(L,l,M),c[n]=M,c[_]=y,x)if(b={values:j?M:w(d),keys:g?M:w(v),entries:R},m)for(S in b)S in L||i(L,S,b[S]);else o(o.P+o.F*(p||T),n,b);return b}},63:function(t,n,e){var r=e(9),o=e(100),i=e(54),u=e(44)("IE_PROTO"),c=function(){},a="prototype",s=function(){var t,n=e(52)("iframe"),r=i.length;for(n.style.display="none",e(76).appendChild(n),n.src="javascript:",(t=n.contentWindow.document).open(),t.write("<script>document.F=Object<\/script>"),t.close(),s=t.F;r--;)delete s[a][i[r]];return s()};t.exports=Object.create||function(t,n){var e;return null===t?e=s():(c[a]=r(t),e=new c,c[a]=null,e[u]=t),void 0===n?e:o(e,n)}},64:function(t,n,e){"use strict";var r=e(9);t.exports=function(){var t=r(this),n="";return t.global&&(n+="g"),t.ignoreCase&&(n+="i"),t.multiline&&(n+="m"),t.unicode&&(n+="u"),t.sticky&&(n+="y"),n}},7:function(t){t.exports=function(t){return"object"==typeof t?null!==t:"function"==typeof t}},71:function(t,n,e){t.exports=!e(8)&&!e(14)(function(){return 7!=Object.defineProperty(e(52)("div"),"a",{get:function(){return 7}}).a})},72:function(t){t.exports=function(t,n){return{value:n,done:!!t}}},73:function(t,n,e){var r=e(32);t.exports=Object("z").propertyIsEnumerable(0)?Object:function(t){return"String"==r(t)?t.split(""):Object(t)}},74:function(t,n,e){var r=e(19),o=e(24),i=e(75)(!1),u=e(44)("IE_PROTO");t.exports=function(t,n){var e,c=o(t),a=0,s=[];for(e in c)e!=u&&r(c,e)&&s.push(e);for(;n.length>a;)r(c,e=n[a++])&&(~i(s,e)||s.push(e));return s}},75:function(t,n,e){var r=e(24),o=e(38),i=e(101);t.exports=function(t){return function(n,e,u){var c,a=r(n),s=o(a.length),f=i(u,s);if(t&&e!=e){for(;s>f;)if((c=a[f++])!=c)return!0}else for(;s>f;f++)if((t||f in a)&&a[f]===e)return t||f||0;return!t&&-1}}},76:function(t,n,e){var r=e(6).document;t.exports=r&&r.documentElement},77:function(t,n,e){e(45)("replace",2,function(t,n,e){return[function(r,o){"use strict";var i=t(this),u=null==r?void 0:r[n];return void 0===u?e.call(i+"",r,o):u.call(r,i,o)},e]})},78:function(t,n,e){e(45)("split",2,function(t,n,r){"use strict";var o=e(79),i=r,u=[].push,c="split",a="length",s="lastIndex";if("c"=="abbc"[c](/(b)*/)[1]||4!="test"[c](/(?:)/,-1)[a]||2!="ab"[c](/(?:ab)*/)[a]||4!="."[c](/(.?)(.?)/)[a]||1<"."[c](/()()/)[a]||""[c](/.?/)[a]){var f=void 0===/()??/.exec("")[1];r=function(t,n){var e=this+"";if(void 0===t&&0===n)return[];if(!o(t))return i.call(e,t,n);var r,c,l,p,v,d=[],y=(t.ignoreCase?"i":"")+(t.multiline?"m":"")+(t.unicode?"u":"")+(t.sticky?"y":""),h=0,x=void 0===n?4294967295:n>>>0,g=new RegExp(t.source,y+"g");for(f||(r=new RegExp("^"+g.source+"$(?!\\s)",y));(c=g.exec(e))&&!((l=c.index+c[0][a])>h&&(d.push(e.slice(h,c.index)),!f&&1<c[a]&&c[0].replace(r,function(){for(v=1;v<arguments[a]-2;v++)void 0===arguments[v]&&(c[v]=void 0)}),1<c[a]&&c.index<e[a]&&u.apply(d,c.slice(1)),p=c[0][a],h=l,d[a]>=x));)g[s]===c.index&&g[s]++;return h===e[a]?(p||!g.test(""))&&d.push(""):d.push(e.slice(h)),d[a]>x?d.slice(0,x):d}}else"0"[c](void 0,0)[a]&&(r=function(t,n){return void 0===t&&0===n?[]:i.call(this,t,n)});return[function(e,o){var i=t(this),u=null==e?void 0:e[n];return void 0===u?r.call(i+"",e,o):u.call(e,i,o)},r]})},79:function(t,n,e){var r=e(7),o=e(32),i=e(4)("match");t.exports=function(t){var n;return r(t)&&(void 0===(n=t[i])?"RegExp"==o(t):!!n)}},8:function(t,n,e){t.exports=!e(14)(function(){return 7!=Object.defineProperty({},"a",{get:function(){return 7}}).a})},80:function(t,n,e){e(45)("search",1,function(t,n,e){return[function(e){"use strict";var r=t(this),o=null==e?void 0:e[n];return void 0===o?new RegExp(e)[n](r+""):o.call(e,r)},e]})},9:function(t,n,e){var r=e(7);t.exports=function(t){if(!r(t))throw TypeError(t+" is not an object!");return t}},99:function(t,n,e){"use strict";var r=e(63),o=e(37),i=e(39),u={};e(12)(u,e(4)("iterator"),function(){return this}),t.exports=function(t,n,e){t.prototype=r(u,{next:o(1,e)}),i(t,n+" Iterator")}}});