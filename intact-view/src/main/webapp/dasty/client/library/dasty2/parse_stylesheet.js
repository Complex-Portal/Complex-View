//------------------------------------------------------------------------------------------	
// "PASRSE A STYLESHEET XML"
//------------------------------------------------------------------------------------------	
            function parseStylesheetXML() {
                if (http_request.readyState == 4) {
                    if (http_request.status == 200) {
						document.getElementById("system_information").innerHTML = "... loading stylesheet from the server";
                        var xmldoc = http_request.responseXML;
						
						var excep = xmldoc.getElementsByTagName('exception').item(0);
						
                        var category_node = xmldoc.getElementsByTagName('CATEGORY').item(0);
					if(category_node)
					  {
						var category_attrs = category_node.attributes;
						for(var g=category_attrs.length-1; g>=0; g--)
							{
							if (category_attrs[g].name == 'id') { var category_id = category_attrs[g].value; }
							}
						
						   var type_node = category_node.childNodes;
						   type_loop: 
						   for (var s = 0; s < type_node.length; s++) 
						   {
						   	 if (type_node[s].nodeName == 'TYPE')
							 { 
									var type_attrs = type_node[s].attributes;
									for(var i=type_attrs.length-1; i>=0; i--)
									{
										if (type_attrs[i].name == 'id') { var type_id = type_attrs[i].value; }
									 }
								   var glyph_node = type_node[s].childNodes;
								   glyph_loop: 
								   for (var gl = 0; gl < glyph_node.length; gl++) 
								   {
										if (glyph_node[gl].nodeName == 'GLYPH')
										{
										   var shape_node = glyph_node[gl].childNodes;
										   shape_loop: 
										   for (var f = 0; f < shape_node.length; f++) 
										   {
												if (shape_node[f].nodeName == 'ARROW' || shape_node[f].nodeName =='ANCHORED_ARROW' || shape_node[f].nodeName =='BOX' || shape_node[f].nodeName =='CROSS' || shape_node[f].nodeName =='EX' || shape_node[f].nodeName =='HIDDEN' || shape_node[f].nodeName =='LINE' || shape_node[f].nodeName =='SPAN' || shape_node[f].nodeName =='TEXT' || shape_node[f].nodeName =='TOOMANY' || shape_node[f].nodeName =='TRIANGLE' || shape_node[f].nodeName =='PRIMERS')
												{
												   var shape_nodeName = shape_node[f].nodeName;
												   var color_node = shape_node[f].childNodes;
												   color_loop: 
												   for (var c = 0; c < color_node.length; c++) 
												   {
														if (color_node[c].nodeName == 'FGCOLOR')
														{
															var fgcolor_data = color_node[c].firstChild.data;
														}	
														else if (color_node[c].nodeName == 'BGCOLOR')
														{
															var bgcolor_data = color_node[c].firstChild.data;
														}	
												   } // FOR color_loop
												 // TEST	
													// var opa = document.getElementById("display_test");
													 //var content_opa = opa.innerHTML;
													// opa.innerHTML = (content_opa + " <br>BF:" + fgcolor_data + " BG:" + bgcolor_data + " TYPEID:" + type_id);
									
												 var stylesheet_properties = {type_id: type_id, shape_nodeName: shape_nodeName, fgcolor_data: fgcolor_data, bgcolor_data: bgcolor_data};
												 stylesheet_properties_info.push(stylesheet_properties);
												} // IF 'SHAPE' 
										   } // FOR shape_loop
										} // IF 'SHAPE' 
								   } // FOR shape_loop	
							 }
						   }
					    } // if(category_node)
					if (excep != null)
						{
							document.getElementById("system_information").innerHTML = "<span style=\"color:#CC0000\">" + stylesheet_url[0][0] + " Das Stylesheet warning:<br/><a href=\"" + stylesheet_url[0][1] + "\" target=\"_blank\">" + excep.firstChild.data + "</a><br/>Dasty2 could not load data from the servers</span>";
						}
					else
						{				 
							makeFeatureRequest();
						}
                    } else {
                        alert('There was a problem with the request: '+http_request.status);
                    }
				//makeRequest('xml/features2_c.xml', 'featureXML');
				//makeRequest('xml/fea_A4_Human.xml', 'featureXML');
				//makeRequest('xml/fea_A4_Human_short.xml', 'featureXML');
				//makeRequest('xml/fea_A4_Human_mod2.xml', 'featureXML');
				//alert("hello");
				//makeFeatureRequest();
                }

            }
      //