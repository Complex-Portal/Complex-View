/**
 * Parse the DAS registry document
 * @param xmldoc XMLResponse of the ajax call for the registry
 */
function parseDasRegistryXML(xmldoc){
	das_registry_label = [];
	das_registry_label.push("any");
	var serverCount = 0;
	
	document.getElementById("system_information").innerHTML = "... loading list of servers from the DAS registry";
	
	var excep = xmldoc.getElementsByTagName('exception').item(0);
	
	// Get SOURCE elements
	var sources = xmldoc.getElementsByTagName("SOURCE");
	for (var i=0; i < sources.length; i++) {
		var source = sources.item(i);
		var id = source.getAttributeNode("title").value;
		
		if (id != 's3dm') {
			
			var registry_uri = source.getAttributeNode("uri").value;
			// Get VERSION elements
			var versions = source.getElementsByTagName("VERSION");
			for (var j=0; j < versions.length; j++) {
				var version = versions.item(j);
				// Get PROPERTY elements
				var labelsFound = 0; //RC 250508
				var properties = version.getElementsByTagName("PROP");
				for (var k=0; k < properties.length; k++) {
					var property = properties.item(k);
					var name = property.getAttributeNode("name").value;
					if (name == "label") {
						var label = property.getAttributeNode("value").value;
						var drl_length = das_registry_label.length;
						var new_label = 0;
						
						das_registry_label_loop:
						for (var m=0; m < drl_length; m++) {
							if (das_registry_label[m] == label) {
								new_label = 1;
								break das_registry_label_loop;
							} else {
								new_label = 0;
							}
						}
						if (new_label == 0) { das_registry_label.push(label); }
						
						if (label.toUpperCase() == filterLabel.toUpperCase() || filterLabel.toUpperCase() == "ANY") {
							labelsFound++; //RC 250508							
							var url = "";
							// Get CAPABILITY elements
							var capabilities = version.getElementsByTagName("CAPABILITY");
							for (var m=0; m < capabilities.length; m++) {
								var capability = capabilities.item(m);
								var type = capability.getAttributeNode("type").value;
								if (type == "das1:features") {
									var uri = capability.getAttributeNode("query_uri").value;
									url = uri.substring(0, uri.indexOf("features"));
							    }
							}
							
							if (url != "" && labelsFound==1) {	//RC 250508
								var server = new Object();
								server.id  = id;
								server.url = proxy_url + '?m=features&q=' + query_id + '&t=' + timeout + '&s=' + url;
								
								if(dasty_mainpage_name.indexOf("interactor") != -1) {
									if(id.toUpperCase() == 'INTACT' || id.toUpperCase() == 'CHEBI') {
							    		server.url = proxy_url + '?m=features&q=' + dgi_id + '&t=' + timeout + '&s=' + url;
									}
								}
								
								server.registry_uri = registry_uri;
								feature_url[serverCount] = server;
							    serverCount++
							} // if (url != "" && labelsFound==1)
						} // if (label == filterLabel || filterLabel = "any")
					} // if (name == "label")
				} // for (var k=0; k < properties.length; k++)
			} // for (var j=0; j < versions.length; j++)
		} //if (id != 's3dm')
	} // for (var i=0; i < sources.length; i++)

    if (excep != null){
		document.getElementById("system_information").innerHTML = "<span style=\"color:#CC0000\">Das Registry warning:<br/><a href=\"" + das_registry_url + "\" target=\"_blank\">" + excep.firstChild.data + "</a><br/>Dasty2 could not load data from the servers</span>";
	} else {
		createLabelOptions(das_registry_label, "feature_label_list_select");
		if(serverCount == 0) {
			document.getElementById("system_information").innerHTML = "<span style=\"color:#CC0000\">Das Registry warning:<br/>Dasty2 did not find protein sequence annotation server for this label. Please chose another label.</span>";
		} else {
	 		makeSequenceRequest();
		}
	}
}

/**
 * Create the Registry Labels in the Search field
 * @param das_registry_label List of  ragistry labels
 * @param tagId tag id of the element to replace. NOTE: is not been used. the function is always looking for feature_label_list
 */
function createLabelOptions(das_registry_label, tagId) {	
	var div = document.getElementById("feature_label_list");
	var content = "";
	content = content + "<label><a style=\"text-decoration:none;\" href=\"http://www.dasregistry.org/help_label.jsp\" target=\"_blank\"><img src=\"img/info01.gif\" border=\"0\">&nbsp;Registry label:</a>&nbsp;<select id=\"feature_label_list_select\" name=\"feature_label_list_select\" class=\"label_list\">"; // align=\"baseline\"
	for ( var i = 0; i < das_registry_label.length; i++ ) {
		if(filterLabel.toUpperCase() == das_registry_label[i].toUpperCase()) {
			content = content + " <option value=\"" + das_registry_label[i] + "\" selected=\"selected\">" + das_registry_label[i] + "</option>";
		} else {
			content = content + " <option value=\"" + das_registry_label[i] + "\">" + das_registry_label[i] + "</option>";
		}
	}
	content = content + "</select></label>&nbsp;&nbsp;";
	div.innerHTML = content;	
}