        // get the URL parameters received from the authorization server
        var state = getUrlParameter("state");  // session key
        var code = getUrlParameter("code");    // authorization code
        
        // load the app parameters stored in the session
        var params = JSON.parse(sessionStorage[state]);  // load app session
        var tokenUri = params.tokenUri;
        var clientId = params.clientId;
        var secret = params.secret;
        var serviceUri = params.serviceUri;
        var redirectUri = params.redirectUri;
        
        // Prep the token exchange call parameters
        var data = {
            code: code,
            grant_type: 'authorization_code',
            redirect_uri: redirectUri
        };
        var options;
        if (!secret) {
            data['client_id'] = clientId;
        }
        options = {
            url: tokenUri,
            type: 'POST',
            data: data
        };
        if (secret) {
            options['headers'] = {'Authorization': 'Basic ' + btoa(clientId + ':' + secret)};
        }
        
        // obtain authorization token from the authorization service using the authorization code
        $.ajax(options).done(function(res){
            // should get back the access token and the patient ID
            var accessToken = res.access_token;
            var patientId = res.patient;
                    
            // and now we can use these to construct standard FHIR
            // REST calls to obtain patient resources with the
            // SMART on FHIR-specific authorization header...
            // Let's, for example, grab the patient resource and
            // print the patient name on the screen
            var url = serviceUri + "/Patient/" + patientId;
            $.ajax({
                url: url,
                type: "GET",
                dataType: "json",
                headers: {
                    "Authorization": "Bearer " + accessToken
                },
            }).done(function(pt){
                var name = pt.name[0].given.join(" ") +" "+ pt.name[0].family.join(" ");
                document.body.innerHTML += "<h3>Patient: " + name + "</h3>";
            });
        });
        
        // Convenience function for parsing of URL parameters
        // based on http://www.jquerybyexample.net/2012/06/get-url-parameters-using-jquery.html
        function getUrlParameter(sParam)
        {
            var sPageURL = window.location.search.substring(1);
            var sURLVariables = sPageURL.split('&');
            for (var i = 0; i < sURLVariables.length; i++) 
            {
                var sParameterName = sURLVariables[i].split('=');
                if (sParameterName[0] == sParam) {
                    var res = sParameterName[1].replace(/\+/g, '%20');
                    return decodeURIComponent(res);
                }
            }
        }