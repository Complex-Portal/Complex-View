//------------------------------------------------------------------------------------------		  
// AJAX FUNCTION TO CREATE A http_resquest OBJECT // WHICH WILL FACILITATE THE PARSING OF XML FILES
//------------------------------------------------------------------------------------------	
            function ajax()
			{

                if (window.XMLHttpRequest) { // Mozilla, Safari,...
                    http_request = new XMLHttpRequest();
                    if (http_request.overrideMimeType) {
                        http_request.overrideMimeType('text/xml');
                    }
                } else if (window.ActiveXObject) { // IE
                    try {
                        http_request = new ActiveXObject("Msxml2.XMLHTTP");
                    } catch (e) {
                        try {
                        http_request = new ActiveXObject("Microsoft.XMLHTTP");
                        } catch (e) {}
                    }
                }

                if (!http_request) {
                    alert('Giving up :( Cannot create an XMLHTTP instance');
                    return false;
                }
				
				if(query_id_null == 1)
					{
						document.getElementById("system_information").innerHTML = "<span style=\"color:#CC0000\">Please enter a \"Protein ID\" to start to use Dasty2.</span>";
					}
				else
					{
						if(use_das_registry == true)
							{
								makeDasRegistryRequest();
							}
						else
							{
								das_registry_label = [];
								das_registry_label.push("any");
								createLabelOptions(das_registry_label, "feature_label_list_select");
								makeSequenceRequest();
							}
					}
				
			}	
			
			
			
            function ajax_feature(featureXML_num)
			{

                if (window.XMLHttpRequest) { // Mozilla, Safari,...
                    feature_http_request[featureXML_num] = new XMLHttpRequest();
                    if (feature_http_request[featureXML_num].overrideMimeType) {
                        feature_http_request[featureXML_num].overrideMimeType('text/xml');
                    }
                } else if (window.ActiveXObject) { // IE
                    try {
                        feature_http_request[featureXML_num] = new ActiveXObject("Msxml2.XMLHTTP");
                    } catch (e) {
                        try {
                        feature_http_request[featureXML_num] = new ActiveXObject("Microsoft.XMLHTTP");
                        } catch (e) {}
                    }
                }

                if (!feature_http_request[featureXML_num]) {
                    alert('Giving up :( Cannot create an XMLHTTP instance');
                    return false;
                }
			}	
			
			
			// "ajax_elements(num)" not in uset yet
            function ajax_elements(num)
			{
				// num 0 = sequence 
				// num 1 = stylesheet
				// num 2 = das_registry


                if (window.XMLHttpRequest) { // Mozilla, Safari,...
                    elements_http_request[num] = new XMLHttpRequest();
                    if (elements_http_request[num].overrideMimeType) {
                        elements_http_request[num].overrideMimeType('text/xml');
                    }
                } else if (window.ActiveXObject) { // IE
                    try {
                        elements_http_request[num] = new ActiveXObject("Msxml2.XMLHTTP");
                    } catch (e) {
                        try {
						elements_http_request[num] = new ActiveXObject("Microsoft.XMLHTTP");
                        } catch (e) {}
                    }
                }

                if (!elements_http_request[num]) {
                    alert('Giving up :( Cannot create an XMLHTTP instance');
                    return false;
                }
			}				