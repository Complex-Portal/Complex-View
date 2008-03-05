//------------------------------------------------------------------------------------------	
// "PASRSE A FEATURE XML AND POPULATE GRAPHIC AND NP TABLE"
//------------------------------------------------------------------------------------------	
            function parseFeatureXML(featureXML_num) {
                if (feature_http_request[featureXML_num].readyState == 4) {
                    document.getElementById("system_information").innerHTML = "... loading features from " + feature_url[featureXML_num].id;
                    if (feature_http_request[featureXML_num].status == 200) {

					  // DRAWING PROGRESS BAR
					  servers_count++;
					  var percentage = parseInt(servers_count*100/feature_url.length);
					  if(percentage == 100)
					  	{
							allSourcesLoaded = true;
						}
				  /*
					  printOnTest(". / . / . / . / . / . / . / . / ");
			printOnTest("count_displayed_groups: " + count_displayed_groups);
			printOnTest("feature_url.length:" + feature_url.length);
			printOnTest("featureXML_num:" + featureXML_num);
			
			printOnTest("servers_count:" + servers_count);
			*/
			
					  
				      progBar(percentage);
						
					  var npf_num = 0; // Count number of non positional features
					  var row_num = 0;
					  var update = 0;
					  var type_counter = [];
					  var tc_length = 0;
					  feature_info[featureXML_num] = [];
					  var parsing_error = 0;
					  
					  var url = feature_url[featureXML_num].url;
					  
					  feature_url[featureXML_num].XML_num = featureXML_num;
					  feature_url[featureXML_num].general_order = servers_count;
					  
					  var firstRequestNumberTemp = dasty2.firstRequestNumber;
						
                      var xmldoc = feature_http_request[featureXML_num].responseXML;
					  var excep = xmldoc.getElementsByTagName('exception').item(0);
					  if (excep != null)
					  	{
							warning_count++;
							var server_checking = document.getElementById("display_server_checking").innerHTML;
							document.getElementById("display_server_checking").innerHTML = server_checking + "<br/><span style=\"color:#FF6600\"><strong>" + servers_count + ".- </strong><a href=\"" + url + "\" target=\"_blank\">" + feature_url[featureXML_num].id + "</a> ... Warning: " + excep.firstChild.data + "</span>";
							feature_url[featureXML_num].state = 3;
					  	}
					  
					  var tf = xmldoc.getElementsByTagName('FEATURE').item(0);
					  if (tf != null)
					  {
						results_XML_order[feature_results_count] = featureXML_num;
						feature_results_count++;
						var server_checking = document.getElementById("display_server_checking").innerHTML;
						document.getElementById("display_server_checking").innerHTML = server_checking + "<br/><span style=\"color:#006600\"><strong>" + servers_count + ".- </strong><a href=\"" + url + "\" target=\"_blank\">" + feature_url[featureXML_num].id + "</a> ... have feature annotations.</span>";
						feature_url[featureXML_num].state = 1;
						//feature_url[featureXML_num].results_order = feature_results_count;
						
                        var gff_node = xmldoc.getElementsByTagName('GFF').item(0);
						
						// var opa = document.getElementById("display_test");
						// var content_opa = opa.innerHTML;
						// opa.innerHTML = (content_opa + " <br>------------<br>tf:" + tf);		
						
						var gff_attrs = gff_node.attributes;
						gff_loop: 
						for(var g=gff_attrs.length-1; g>=0; g--)
							{
							if (gff_attrs[g].name == 'version') { var gff_version = gff_attrs[g].value; }
							else if (gff_attrs[g].name == 'href') { var gff_href = gff_attrs[g].value; };
							}
							
							// var opa = document.getElementById("display_test");
							// var content_opa = opa.innerHTML;
							// opa.innerHTML = (content_opa + " <br>------------<br>gff_href:" + gff_href);
							
						
						   var segment_node = gff_node.childNodes;
						   segment_loop: 
						   for (var s = 0; s < segment_node.length; s++) 
						   {
						   		if (segment_node[s].nodeName == 'SEGMENT')
								{
									//var segment_data = segment_node[s].firstChild.data;
									var segment_attrs = segment_node[s].attributes;
									for(var i=segment_attrs.length-1; i>=0; i--)
									{
										if (segment_attrs[i].name == 'id') { var segment_id = segment_attrs[i].value; }
										else if (segment_attrs[i].name == 'version')
										  {
											var segment_version = segment_attrs[i].value;
											if(segment_version == sequence_info.sequence_version)
												{
													annotation_version[featureXML_num] = 1; // ok
												}
											else
												{
													annotation_version[featureXML_num] = 0; // different
												}
										  }
										else if (segment_attrs[i].name == 'start')
										  { 
											//if(segment_attrs[i].value == '')
											  //{
											  var segment_start = sequence_info.sequence_start;
											  //} // sequence_info is glogal!
											//else
											  //{ var segment_start = segment_attrs[i].value;}
										  }
										else if (segment_attrs[i].name == 'stop')
										  {
											//if(segment_attrs[i].value == '' || segment_attrs[i].value == '-1')
											  //{ 
											  var segment_stop = sequence_info.sequence_stop;
											  //} // sequence_info is glogal!
											//else
											  //{ 
											  //var segment_stop = segment_attrs[i].value;
											  //}
										  }
									 }
								   var feature_node = segment_node[s].childNodes;
								   feature_loop: 
								   for (var f = 0; f < feature_node.length; f++) 
								   {
									   var feature_id = '';
									   var feature_label = '';
									   var type_data = '';
									   var type_id = '';
									   var type_category = '';
									   var method_data = '';
									   var method_id = '';
									   var start_data = '';
									   var end_data = '';
									   var score_data = '';
									   var orientation_data = '';
									   var phase_data = '';
									   var note_data = [];
									   var link_data = [];
									   var link_href = [];
									   var group_id = '';
									   
										if (feature_node[f].nodeName == 'FEATURE')
										{
											//var segment_data = segment_node[s].firstChild.data;
											var feature_attrs = feature_node[f].attributes;
											for(var i=feature_attrs.length-1; i>=0; i--)
											{
												if (feature_attrs[i].name == 'id') { feature_id = feature_attrs[i].value; }
												else if (feature_attrs[i].name == 'label') { feature_label = feature_attrs[i].value; };
											 }
										   // Exception for "http://cathwww.biochem.ucl.ac.uk:9000/das/cath_pdb/"
										 if(feature_id == "Query_failed") { break feature_loop; parsing_error = 1; }

										   var feature_child_node = feature_node[f].childNodes;
										   feature_child_loop: 
										   for (var c = 0; c < feature_child_node.length; c++) 
										   {
												if (feature_child_node[c].nodeName == 'TYPE')
												{
													if(feature_child_node[c].firstChild == null)
														{
															//var type_data = "";
															var type_data_temp = "";
															type_data = "";
														}
													else
														{
															//var type_data = feature_child_node[c].firstChild.data;
															var type_data_temp = feature_child_node[c].firstChild.data;
															type_data = feature_child_node[c].firstChild.data;
														}
													var feature_child_attrs = feature_child_node[c].attributes;
													for(var i=feature_child_attrs.length-1; i>=0; i--)
													{
														if (feature_child_attrs[i].name == 'id')	
															{
																type_id = feature_child_attrs[i].value;
																if (type_data_temp == "")
																	{
																		type_data = type_id;
																	}
																//if (type_data_temp.toLowerCase() == type_id.toLowerCase() || type_data_temp == "" )
																	//{
																	//	// do nothing
																	//}
																//else
																	//{
																	//	type_data = type_data_temp + " (" + type_id + ")";
																	//}
															}
														else if (feature_child_attrs[i].name == 'category')
															{
																type_category = feature_child_attrs[i].value;
															}
													} // for(var i=feature_child_attrs.length-1; i>=0; i--)
													
													
													
													/**
													* Create an list of non redundant types
													*/
													
													//if(type_id != "")
														//{
															var typesLoadedLength = dasty2.typesLoaded.length;
															var typeDuplicated = 0;
															for(var w = 0; w < typesLoadedLength; w++)
																{
																	if(dasty2.typesLoaded[w].toUpperCase() == type_id.toUpperCase())
																		{
																			typeDuplicated = 1;
																		}
																}
																if(typeDuplicated == 0)
																	{
																			dasty2.typesLoaded.push(type_id);
																	}
														//}
													
													/**
													* Create an list of non redundant categories
													*/
													
													//if(type_category != "")
														//{
															var categoriesLoadedLength = dasty2.categoriesLoaded.length;
															var categoryDuplicated = 0;
															for(var w = 0; w < categoriesLoadedLength; w++)
																{
																	if(dasty2.categoriesLoaded[w].toUpperCase() == type_category.toUpperCase())
																		{
																			categoryDuplicated = 1;
																		}
																}
																if(categoryDuplicated == 0)
																	{
																			dasty2.categoriesLoaded.push(type_category);
																	}
														//}
													
															
													
													
													
												}	 
												else if (feature_child_node[c].nodeName == 'METHOD')
												{
													if(feature_child_node[c].firstChild == null)
														{
															var method_data_temp = "";
															method_data = "";
														}
													else
														{
															method_data = feature_child_node[c].firstChild.data;
															var method_data_temp = feature_child_node[c].firstChild.data;
														}
													//var method_data = feature_child_node[c].firstChild.data;
													var feature_child_attrs = feature_child_node[c].attributes;
													for(var i=feature_child_attrs.length-1; i>=0; i--)
													{
														if (feature_child_attrs[i].name == 'id')
															{
																method_id = feature_child_attrs[i].value;
																if (method_data_temp == "")
																	{
																		method_data = method_id;
																	}
																if (method_data_temp.toLowerCase() == method_id.toLowerCase() || method_data_temp == "" )
																	{
																		// do nothing
																	}
																else
																	{
																		method_data = method_data_temp + " (" + method_id + ")";
																	}
															}
													 }	 
												}
												else if (feature_child_node[c].nodeName == 'START')
												{
													start_data = feature_child_node[c].firstChild.data;
												}
													else if (feature_child_node[c].nodeName == 'END')
												{
													end_data = feature_child_node[c].firstChild.data;
												}
												else if (feature_child_node[c].nodeName == 'SCORE')
												{
													if(feature_child_node[c].firstChild == null)
														{
															score_data = "0.0";
														}
													else
														{
															score_data = feature_child_node[c].firstChild.data;
														}
												}
												else if (feature_child_node[c].nodeName == 'ORIENTATION')
												{
													orientation_data = feature_child_node[c].firstChild.data;
												}
												else if (feature_child_node[c].nodeName == 'PHASE')
												{
													phase_data = feature_child_node[c].firstChild.data;
												}
												else if (feature_child_node[c].nodeName == 'NOTE')
												{
													note_data.push(feature_child_node[c].firstChild.data);
												}
												else if (feature_child_node[c].nodeName == 'LINK')
												{
													link_data.push(feature_child_node[c].firstChild.data);
													var feature_child_attrs = feature_child_node[c].attributes;
													for(var i=feature_child_attrs.length-1; i>=0; i--)
													{
														if (feature_child_attrs[i].name == 'href') { link_href.push(feature_child_attrs[i].value); };
													 }
												}
												else if (feature_child_node[c].nodeName == 'GROUP')
												{
													var feature_child_attrs = feature_child_node[c].attributes;
													for(var i=feature_child_attrs.length-1; i>=0; i--)
													{
														if (feature_child_attrs[i].name == 'id') { group_id = feature_child_attrs[i].value; };
													}
												}
										   // FOR feature_child_loop
										   }
										 //} // if(feature_id != "Query_failed")
										   
										 //if(type_category) { /* defined */ } else { var type_category = ''; } 
										 
										 var feature_row = {feature_id: feature_id, feature_label: feature_label, type_data: type_data, type_id: type_id, type_category: type_category, method_data: method_data, method_id: method_id, start_data: start_data, end_data: end_data, score_data: score_data, orientation_data: orientation_data, phase_data: phase_data, note_data: note_data, link_data: link_data, link_href: link_href, group_id: group_id, annotation_server: feature_url[featureXML_num].id, annotation_server_uri: feature_url[featureXML_num].registry_uri, xmlnumber: featureXML_num };
										 //feature_info[row_num] = feature_row; 
										 feature_info[featureXML_num][row_num] = feature_row;
										 
										 if (start_data==0 && end_data==0)
		  									{
											  npf_num++;	
											}

							if (start_data == 0 && end_data ==0)
									{ // non positional feature
									} else {
										//----------------------------------------------------------------------------------
										// ARRAY THAT SAYS WHERE A "type" IS HAPPENING IN THE "feature_info" ARRAY
										//----------------------------------------------------------------------------------
										tc_length = type_counter.length;
										
										if(tc_length == 0)
										  {
											type_counter[0] = [];
											type_counter[0][0] = type_id;
											type_counter[0][1] = [];
											type_counter[0][1] = [row_num];
										   }
										
										tc_update = 0;
										for(var h = 0; h < tc_length; h++)
										  {									  
											if(type_counter[h][0] == type_id)
											  {
											    type_counter[h][1].push(row_num);
												tc_update = 1;
											  }
										   }
										   
										if(tc_update == 0 && tc_length != 0)
										  {
											 type_counter[tc_length] = [];
											 type_counter[tc_length][0] = type_id;
											 type_counter[tc_length][1] = [];
											 type_counter[tc_length][1] = [row_num];
										   }
											
									 } // finish => if (start_data == 0 && end_data ==0)
										row_num++;
										// IF 'FEATURE'   	
										}
								   // FOR feature_loop	
								   }
								}
						   }	
						   
						  if(parsing_error == 0)
						   {
								createNPFeatureTable(featureXML_num, feature_info[featureXML_num], "display_nonpositional", npf_num);
								grouping(featureXML_num, type_counter,  segment_start, segment_stop);
								
								
								if(firstRequestNumberTemp <= dasty2.validFirstSources.length)		
									{
										if(firstRequestNumberTemp == dasty2.validFirstSources.length)
						  					{
												dasty2.firstRequestNumber++;
												makeFeatureRequest2();
											}
						   				else
						   					{
												makeFeatureRequest(dasty2.firstRequestNumber);
											}
						   			}
						   }
						  else
						   {
							    createGraphic2(featureXML_num, "display_graphic", 0, 0, "noresults");
								//printOnTest('first' + dasty2.makeAsyncRequest);
								if(firstRequestNumberTemp <= dasty2.validFirstSources.length)		
									{
										if(firstRequestNumberTemp == dasty2.validFirstSources.length)
						  					{
												dasty2.firstRequestNumber++;
												makeFeatureRequest2();
											}
						   				else
						   					{
												makeFeatureRequest(dasty2.firstRequestNumber);
											}
									}
						   }

					    } // if (tf != "null" && tf != "" && tf != null)
					   else
					    {
							
						  //var opa = document.getElementById("display_test");
						  //var content_opa = opa.innerHTML;
						  //opa.innerHTML = (content_opa + " <br>------------<br>tf:" + tf);		
							
							
						  //var opa = document.getElementById("display_test");
					      //var content_opa = opa.innerHTML;
						  //opa.innerHTML = (content_opa + " <br>------------<br>featureXML_num:" + featureXML_num);
						  
						  if (excep == null)
					  		{
							  no_feature_results_count++;
							  var server_checking = document.getElementById("display_server_checking").innerHTML;
							  document.getElementById("display_server_checking").innerHTML = server_checking + "<br/><span style=\"color:#CC0000\"><strong>" + servers_count + ".- </strong><a href=\"" + url + "\" target=\"_blank\">" + feature_url[featureXML_num].id + "</a> ... do not have feature annotations available.</span>";
							  feature_url[featureXML_num].state = 2;
							}
						  
						  createGraphic2(featureXML_num, "display_graphic", 0, 0, "noresults");
						  
						  //printOnTest('second' + dasty2.makeAsyncRequest);
						  if(firstRequestNumberTemp <= dasty2.validFirstSources.length)		
								{
									if(firstRequestNumberTemp == dasty2.validFirstSources.length)
						  				{
											dasty2.firstRequestNumber++;
											makeFeatureRequest2();
										}
					  				else
					   					{
											makeFeatureRequest(dasty2.firstRequestNumber);
										}
								}					
							
					    } // if (tf != "null" && tf != "" && tf != null)
						
                    } else {
                        alert('There was a problem with the request: Parse feature - '+feature_http_request[featureXML_num].status+" "+feature_http_request[featureXML_num].statusText);
                    } // if (feature_http_request[featureXML_num].status == 200)
                } // if (feature_http_request[featureXML_num].readyState == 4)
				
				//alert(standby.length);
				
            } // function parseFeatureXML()
      //
	  
	  
	  
	  
/*
			printOnTest("- - - - - - - - - - - - - - - - - ");
			printOnTest("count_displayed_groups: " + count_displayed_groups);
			printOnTest("feature_url.length:" + feature_url.length);
			
			printOnTest("servers_count:" + servers_count);
			printOnTest("warning_count:" + warning_count);
			printOnTest("no_feature_results_count:" + no_feature_results_count);
			printOnTest("feature_results_count:" + feature_results_count);
			
			
			
						if(servers_count == feature_url.length)						
							{

							}				
							
*/