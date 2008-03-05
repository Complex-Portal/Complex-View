//------------------------------------------------------------------------------------------	    
// CREATE NON POSITIONAL FEATURE TABLE
//------------------------------------------------------------------------------------------	
	function createNPFeatureTable(featureXML_num, array, tagId, npf_num)
	{
		
//printOnTest(">/:::>/:::>/:::>/:::>/:::>/:::>/:::>/:::");
//printOnTest(featureXML_num);
//printOnTest(npf_num);
//printOnTest(feature_url.length);
//printOnTest(array.length);

	if(npf_num == 0)
	 	{
	  // There are not positional features
	  	}
	 else
	 	{		
			// var title = ["Category", "Type", "Feature ID", "Note", "Method", "Score"];
			//var title = ["Type" => , "Feature ID", "Note"];
			//var title_var = ["type_category", "type_data", "feature_id", "note_data", "method_data", "score_data"];
			var title_var = non_positional_features_coulmns;

			/**
			* The first time that a DAS source is loaded:
			*/
			var countNPF = dasty2.countNPF
			if(countNPF == 0)
				{
					dasty2.countNPF++;
					//Principio
					var title = new Array();
					title["type_category"] = "CATEGORY";
					title["type_data"] = "TYPE";
					title["type_id"] = "TYPE ID";
					title["feature_id"] = "FEATURE ID";
					title["note_data"] = "NOTE";
					title["method_data"] = "METHOD";
					title["score_data"] = "S.";
					title["annotation_server"] = "DAS SOURCE";
					title["link_data"] = "";
					
					var title_width = new Array();
					title_width["type_category"] = col_category_width;
					title_width["type_data"] = col_type_width;
					title_width["type_id"] = col_type_width;
					title_width["feature_id"] = col_id_width;
					title_width["note_data"] = "";
					title_width["method_data"] = "";
					title_width["score_data"] = 40;
					title_width["annotation_server"] = col_server_width;
					title_width["link_data"] = 5;
	
	
					
					var mybody = document.getElementById(tagId);
					var mytable = document.createElement("table");
					mytable.setAttribute("id","non_positional_features");
					mytable.setAttribute("class","sortable feature_table");
					mytable.setAttribute("className","sortable feature_table");
					mytable.style.cssText = "width:" + non_positional_feature_table_width + ";";
				
					//mytable.setAttribute("class","sortable");
					//mytable.setAttribute("className","sortable");
					
					//var mythead = document.createElement('thead');
					
					var mytbody = document.createElement('tbody');
					mytbody.setAttribute("id","non_positional_features_tbody");
					
					var mycurrent_row = document.createElement("tr");
					mycurrent_row.setAttribute("class", "feature_table_row_title_decor");
					mycurrent_row.setAttribute("className", "feature_table_row_title_decor");
					for(var i = 0; i < title_var.length; i++)
					  {
						  var mycurrent_cell = document.createElement("th");
						  if(title_var[i] == "note_data" || title_var[i] == "link_data")
						  	{
								mycurrent_cell.setAttribute("class", "feature_table_cell_title_decor" + i + " unsortable");
						 		mycurrent_cell.setAttribute("className", "feature_table_cell_title_decor" + i + " unsortable");
							}
						  else
						  	{
								mycurrent_cell.setAttribute("class", "feature_table_cell_title_decor" + i);
						  		mycurrent_cell.setAttribute("className", "feature_table_cell_title_decor" + i);
							}
							
						  if(title_width[title_var[i]] != "")
						  	{
						  		mycurrent_cell.style.cssText = "width:" + title_width[title_var[i]] + "px;";
							}
						  var content = document.createTextNode(title[title_var[i]]);
						  mycurrent_cell.appendChild(content);
						  mycurrent_row.appendChild(mycurrent_cell);
					  }
					  
					//mythead.appendChild(mycurrent_row);
					mytbody.appendChild(mycurrent_row);
					//mytable.appendChild(mythead);
					mytable.appendChild(mytbody);
					mybody.appendChild(mytable);
				}

						// Lo del medio

			var mytbody = document.getElementById("non_positional_features_tbody");
			var npfTypes = new Array();
			var newType = false;

			
			for(var j = 0; j < array.length; j++)
				{	
					  if (array[j]["start_data"]==0 && array[j]["end_data"]==0)
						 { // non positional feature	
						 
							newType = false;
							
							if(typeof npfTypes[array[j]["type_id"]] == "undefined")
								{
									newType = true;
								}
								
							if(newType == true)
								{
									npfTypes[array[j]["type_id"]] = 0;
								}
							else
								{
									npfTypes[array[j]["type_id"]]++;
								}
					
	
							var mycurrent_row = document.createElement("tr");
							
							var tr_name = "npf_item" + featureXML_num + "_" + array[j]["type_id"] + "_" + npfTypes[array[j]["type_id"]];
							var tr_id_name = [];
							tr_id_name.push(tr_name);
							//printOnTest(">>>>>>>>>>>>>>>>>>>>>>>>>>>>");
							//printOnTest(array[j]["annotation_server"]);
							//printOnTest(tr_id_name);
							
							dasty2.IdlinesPerType.push([array[j]["type_id"], tr_id_name, 1]);
							dasty2.IdlinesPerCategory.push([array[j]["type_category"], tr_id_name, 1]);
							dasty2.IdlinesPerServer.push([array[j]["annotation_server"], tr_id_name, 1]);	
							
							
							
					  		mycurrent_row.setAttribute("id", tr_id_name);
							mycurrent_row.style.cssText = "display: table-row;"; //display: block;
							
							if(dasty2.decor_tr_npf == 0)
								{
									mycurrent_row.setAttribute("class", "feature_table_row_decor0");
									mycurrent_row.setAttribute("className", "feature_table_row_decor0");
									dasty2.decor_tr_npf++;
								}
							else
								{
									mycurrent_row.setAttribute("class", "feature_table_row_decor1");
									mycurrent_row.setAttribute("className", "feature_table_row_decor1");
									dasty2.decor_tr_npf--;
								}
									  
							for(var i = 0; i < title_var.length; i++)
								{
									var mycurrent_cell = document.createElement("td");
									//mycurrent_cell.setAttribute("class", "featuretable_cell_decor" + i);
									//mycurrent_cell.setAttribute("className", "featuretable_cell_decor" + i);
									
									if(title_var[i] == "link_data")
										{
											for(var w = 0; w < array[j][title_var[i]].length; w++)
												{
													var content = document.createElement("img");
													content.setAttribute("src", dasty_path+"img/ico_info2.gif");
													content.setAttribute("alt", array[j][title_var[i]][w]);
													content.setAttribute("border","0");
													
													var link_content = document.createElement("a");
													link_content.setAttribute("target", "_featurelinks");
													link_content.setAttribute("href", array[j]["link_href"][w]);
													link_content.appendChild(content);	

													mycurrent_cell.appendChild(link_content);
												}
												
										}
									else if(title_var[i] == "note_data")
										{
											
											for(var w = 0; w < array[j][title_var[i]].length; w++)
												{
													var content = document.createTextNode(array[j][title_var[i]][w]);
													mycurrent_cell.appendChild(content);
													var br = document.createElement("br");
													mycurrent_cell.appendChild(br);
												}
										}
									else
										{
											var content = document.createTextNode(array[j][title_var[i]]);
											mycurrent_cell.appendChild(content);
										}
									
									//title_width["link_data"] = 5;
					
					//title["link_href"] = "LINK";
					
					
					
									
									mycurrent_row.appendChild(mycurrent_cell);
								}
							mytbody.appendChild(mycurrent_row);
						 } // if (array[j]["start_data"]==0 && array[j]["end_data"]==0)
					
					
				
					
					
				} // for(var j = 0; j < array.length; j++)
			
	


		
		
		
	    } //if(npf_num == 0)
	//makeFeatureRequest();
	
			/*
			printOnTest(" /  .    /  .    /  .    /  .   ");
			printOnTest("No feature res: " + no_feature_results_count);
			printOnTest("Count_display_groups: " + count_displayed_groups);
			printOnTest("Feature URL: " + feature_url.length);
			*/
				
    }
	
	