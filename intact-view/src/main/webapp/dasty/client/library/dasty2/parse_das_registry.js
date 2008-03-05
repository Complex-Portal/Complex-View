//------------------------------------------------------------------------------------------	
<!-- "PASRSE DAS REGISTRY"
//------------------------------------------------------------------------------------------	
function parseDasRegistryXML()
{
//DasRegistryHelper.prototype.parseResponse = function(response, filterLabel)   {
    
	// var servers = new Array();
	das_registry_label = [];
	das_registry_label.push("any");
	var serverCount = 0;

    // Get SOURCE elements
	
 // if (elements_http_request[2].readyState == 4)
 if (http_request.readyState == 4)
 {
  // if (elements_http_request[2].status == 200)
  if (http_request.status == 200)
  {
	document.getElementById("system_information").innerHTML = "... loading list of servers from the DAS registry";
    // var xmldoc = elements_http_request[2].responseXML;
	var xmldoc = http_request.responseXML;
	
	var excep = xmldoc.getElementsByTagName('exception').item(0);
	
    var sources = xmldoc.getElementsByTagName("SOURCE");
    for (var i=0; i < sources.length; i++)
	{
        var source = sources.item(i);
        var id = source.getAttributeNode("title").value;
		
		if (id != 's3dm')
		{
		
		var registry_uri = source.getAttributeNode("uri").value;
        // Get VERSION elements
        var versions = source.getElementsByTagName("VERSION");
        for (var j=0; j < versions.length; j++)
		{
            var version = versions.item(j);
            // Get PROPERTY elements
            var properties = version.getElementsByTagName("PROP");
            for (var k=0; k < properties.length; k++)
			{
                var property = properties.item(k);
                var name = property.getAttributeNode("name").value;
                if (name == "label")
				 {
                    var label = property.getAttributeNode("value").value;
					
					var drl_length = das_registry_label.length;
					//if(drl_length == 0) {das_registry_label[0] == label}
					
					var new_label = 0;
					
					das_registry_label_loop:
					for (var m=0; m < drl_length; m++)
						{
							if (das_registry_label[m] == label)
								{
									new_label = 1;
									break das_registry_label_loop;
								}
							else
								{
									new_label = 0;
								}
						}
					if (new_label == 0) { das_registry_label.push(label); }
					
					
					
					
                    if (label.toUpperCase() == filterLabel.toUpperCase() || filterLabel.toUpperCase() == "ANY")
					    {
                        var url = "";
                        // Get CAPABILITY elements
                        var capabilities = version.getElementsByTagName("CAPABILITY");
                        for (var m=0; m < capabilities.length; m++)
						  {
                            var capability = capabilities.item(m);
                            var type = capability.getAttributeNode("type").value;
                            if (type == "das1:features")
							  {
                                var uri = capability.getAttributeNode("query_uri").value;
                                url = uri.substring(0, uri.indexOf("features"));
                              }
                          }
                        
						//if (url != "" && url != 'http://www.ebi.ac.uk/msd-srv/msdmotif/das/s3dm/')
						if (url != "")	
							{
                            var server = new Object();
                            server.id  = id;
							server.url = proxy_url + '?m=features&q=' + query_id + '&t=' + timeout + '&s=' + url;
							
							if(dasty_mainpage_name.indexOf("interactor") != -1)
								{
									if(id.toUpperCase() == 'INTACT' || id.toUpperCase() == 'CHEBI')
										{
                            				server.url = proxy_url + '?m=features&q=' + dgi_id + '&t=' + timeout + '&s=' + url;
										}
								}
							
							server.registry_uri = registry_uri;
                            //server.label = label;
                            //servers[serverCount] = server;
							feature_url[serverCount] = server;
                            serverCount++					
                          }
                    	} // if (label == filterLabel || filterLabel = "any")
               	 	} // if (name == "label")
            } // for (var k=0; k < properties.length; k++)
        } // for (var j=0; j < versions.length; j++)
	  } //if (id != 's3dm')
    } // for (var i=0; i < sources.length; i++)
    //return servers;
//	printOnTest(
	
	// TEST -------------------------------------
	//for(var b = 0; b < das_registry_label.length; b++)
		//{
			//	var opa = document.getElementById("display_test");
			//	var content_opa = opa.innerHTML;
			//	opa.innerHTML = (content_opa + " <br>------------<br>das_registry_label: " + das_registry_label[b]);	
		//}
	// TEST -------------------------------------
    if (excep != null)
		{
		  document.getElementById("system_information").innerHTML = "<span style=\"color:#CC0000\">Das Registry warning:<br/><a href=\"" + das_registry_url + "\" target=\"_blank\">" + excep.firstChild.data + "</a><br/>Dasty2 could not load data from the servers</span>";
		}
	else
		{
		  createLabelOptions(das_registry_label, "feature_label_list_select")
		  makeSequenceRequest();
		}
  //makeFeatureRequest();		
  } else {
    alert('There was a problem with the request: '+http_request.status+" "+http_request.statusText);
  } //if (http_request.status == 200)
 }
}

//------------------------------------------------------------------------------------------	
// CREATE LABELS IN SEARCH FIELD
//------------------------------------------------------------------------------------------	
function createLabelOptions(das_registry_label, tagId)
	{
        var div = document.getElementById("feature_label_list");
		var content = "";
		content = content + "<label><a style=\"text-decoration:none;\" href=\"http://www.dasregistry.org/help_label.jsp\" target=\"_blank\"><img src=\"img/info01.gif\" border=\"0\">&nbsp;Registry label:</a>&nbsp;<select id=\"feature_label_list_select\" name=\"feature_label_list_select\" class=\"label_list\">"; // align=\"baseline\"
		for ( var i = 0; i < das_registry_label.length; i++ )
			{
				if(filterLabel.toUpperCase() == das_registry_label[i].toUpperCase())
					{
						content = content + " <option value=\"" + das_registry_label[i] + "\" selected=\"selected\">" + das_registry_label[i] + "</option>";
					}
				else
					{
						content = content + " <option value=\"" + das_registry_label[i] + "\">" + das_registry_label[i] + "</option>";
					}
			}
		content = content + "</select></label>&nbsp;&nbsp;";
		div.innerHTML = content;
		
	}
//

//function createLabelOptions(das_registry_label, tagId)
	//{	
		//for ( var i = 0; i < das_registry_label.length; i++ )
			//{
				//var form_select = document.getElementById("feature_label_list_select");
				//var content_form_select = form_select.innerHTML;
				//form_select.innerHTM =  (content_form_select + " <option value=\"" + das_registry_label[i] + "\">" + das_registry_label[i] + "</option>");		
			//}
	//}