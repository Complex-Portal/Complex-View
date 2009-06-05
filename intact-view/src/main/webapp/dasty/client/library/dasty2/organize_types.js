//------------------------------------------------------------------------------------------	
// reorganize features ID in Types
//------------------------------------------------------------------------------------------
 function organizeIdTypes(featureXML_num, fids, same_fids)
 {
 	//------------------------------------------------------------------------------------------	
	// put the features with the same ID in the same line
	//------------------------------------------------------------------------------------------
	 //alert("fids[0][1]:" + fids[0][1]);
	 //alert("same_fids:" + same_fids);
	 //alert("array:" + array[1]["feature_id"]);
	 var do_push = 0;
	 var non_same_fids = [];
	 var new_feature_list = [];
	 var line = [];
	 var line_length = 0;
	 var eline_length = 0;
	 var start_end = [];
	 var con_eline01 = 0;
	 var con_eline02 = 0;
	 var con_eline = 0;
	 var feature_included = 0;
	 var con_line = 0;
	 var array_row_num = 0;
	 var nfl_length =0;
	 var last_c = 0;
	 var line_id_name = [];
	 var line_name = "";
	 var lineIdValue = 1;
	 
	 var finfo = feature_info[featureXML_num];
	 
	 
	 // new_feature_list_info = [];
	 //new_feature_list_info2 = [];
	 
	 
	 
	//------------------------------------------------------------------------------------------	
	// create an array with the features with the same id (same feature group id)
	//------------------------------------------------------------------------------------------
	 
	 for(var i = 0; i < same_fids.length; i++)
	   {
	    new_feature_list[i] = [];
		new_feature_list[i]["line"] = i;
		new_feature_list[i]["features"] = [];		// FEATURES: Contains references to the features for the array "feature_info"
		new_feature_list[i]["feature_group"] = 1;	// FEATURE GROUP: boolean "1/0". "1" means feature with the same ID o group
		new_feature_list[i]["type"] = finfo[fids[same_fids[i]][1][0]]["type_id"];		// TYPE
		new_feature_list[i]["type_data"] = finfo[fids[same_fids[i]][1][0]]["type_data"];		// TYPE
		new_feature_list[i]["category"] = finfo[fids[same_fids[i]][1][0]]["type_category"];	// CATEGORY
		new_feature_list[i]["method"] = finfo[fids[same_fids[i]][1][0]]["method_data"];	// METHOD
		//new_feature_list[i]["feature_id"] = finfo[fids[same_fids[i]][1][0]]["feature_id"];	// ID		
		//new_feature_list[i]["feature_label"] = '';	// ID
		new_feature_list[i]["server"] = finfo[fids[same_fids[i]][1][0]]["annotation_server"];	// SERVER
		new_feature_list[i]["registry_uri"] = finfo[fids[same_fids[i]][1][0]]["annotation_server_uri"];	// SERVER ID (REGISTRY URI)
		new_feature_list[i]["version"] = annotation_version[finfo[fids[same_fids[i]][1][0]]["xmlnumber"]];	// ANNOTATION VERSION boolean "1/0". "1" means the feature file has the same version than the sequence file.
		
		var featureLabelsPerLines = '';
		for(var a = 0; a < fids[same_fids[i]][1].length; a++)
		   {
		      new_feature_list[i]["features"].push(fids[same_fids[i]][1][a]); 
		   }
		   
		line_name = "gr_item" + featureXML_num + "_" + new_feature_list[i]["type"] + "_" + new_feature_list[i]["line"];  
		line_id_name.push(line_name);
		
		
		for(var n = 0; n < excluded_das_sources.length; n++)
			{
				if(excluded_das_sources[n].toLowerCase() == new_feature_list[0]["server"].toLowerCase())
					{
						lineIdValue = 0;
					}
			}
		dasty2.line_id_name2[line_name] = lineIdValue;
		dasty2.line_id_name2_length++;
		
	   }
	   
	   if(same_fids.length > 0)
	   		{
				dasty2.IdlinesPerType.push([new_feature_list[0]["type"], line_id_name, line_id_name.length]);
				dasty2.IdlinesPerCategory.push([new_feature_list[0]["category"], line_id_name, line_id_name.length]);
				dasty2.IdlinesPerServer.push([new_feature_list[0]["server"], line_id_name, line_id_name.length]);
			}		
	   
	   line_id_name = [];
	   //add_new_type = 0;
	 
	//------------------------------------------------------------------------------------------	
	// create an array with the feature id that are not reapeted => non_same_fids
	//------------------------------------------------------------------------------------------
	 for(var i = 0; i < fids.length; i++)
	   {
	     do_push = 0;
	     for(var a = 0; a < same_fids.length; a++)
		    {
		        if(i == same_fids[a]) { do_push = 1; }
		    }
		 if(do_push == 0)
		    {
		        non_same_fids.push(i);
		    }
	    }
			
	//------------------------------------------------------------------------------------------	
	// look for the rest of features IDs, check their ends and starts and try to overlap in the same line if possible
	//------------------------------------------------------------------------------------------
	 for(var i = 0; i < non_same_fids.length; i++)
	   {
	    array_row_num = fids[non_same_fids[i]][1][0];

         if (i == 0)
		   { 
		     line[i] = [];
			 line[i].push(array_row_num);
			 start_end[i] = [];
			 start_end[i][0] = []; // START DATA
			 start_end[i][0].push(finfo[array_row_num]["start_data"]);
			 start_end[i][1] = []; // END DATA
			 start_end[i][1].push(finfo[array_row_num]["end_data"]);
		   }
		  else // (i == 0)
		   {
		     con_line = 0;
		     line_length = line.length;
			 line_loop: 
		     for(var a = 0; a < line_length; a++)
			    {
				   con_eline = 0;
				   con_eline01 = 0;
				   con_eline02 = 0;
				   eline_length = line[a].length;
				   for(var b = 0; b < eline_length; b++)
				      {
					  	 start01 = finfo[array_row_num]["start_data"];
						 start02 = start_end[a][0][b];
						 end01 = finfo[array_row_num]["end_data"];
						 end02 = start_end[a][1][b];
						 
						 con_eline01 = end02 - start01;
						 con_eline02 = end01 - start02;
						 
						 if(con_eline01 < 0) { con_eline++ }
						 if(con_eline02 < 0) { con_eline++ } 
					  }
				   if(con_eline == eline_length)
				      { // include in the line
						 line[a].push(array_row_num);
						 start_end[a][0].push(finfo[array_row_num]["start_data"]);
						 start_end[a][1].push(finfo[array_row_num]["end_data"]);
						 break line_loop;
					  }
				   else
				      { // not include in the line
					  	 con_line++;
					  }
				  } // for(var a = 0; a < line_length; a++)
			      if(con_line == line_length)
					   { // include new line
							 line[line_length] = [];
							 line[line_length].push(array_row_num);
							 start_end[line_length] = [];
							 start_end[line_length][0] = []; // START DATA
							 start_end[line_length][0].push(finfo[array_row_num]["start_data"]);
							 start_end[line_length][1] = []; // END DATA
							 start_end[line_length][1].push(finfo[array_row_num]["end_data"]);
					   }
		     } // if (i == 0)
   	   } // for(var i = 0; i < non_same_fids.length; i++)

	//------------------------------------------------------------------------------------------	
	// concatenate "new_feature_list" with "line"
	//------------------------------------------------------------------------------------------

	nfl_length = new_feature_list.length;
	for(var i = 0; i < line.length; i++)
	   {
	    new_feature_list[i+nfl_length] = [];
		new_feature_list[i+nfl_length]["line"] = i+nfl_length;
		new_feature_list[i+nfl_length]["features"] = [];	// Contains references to the features for the array "feature_info"
		new_feature_list[i+nfl_length]["feature_group"] = 0;		// boolean. 1 mean feature with the same ID o group
		new_feature_list[i+nfl_length]["type"] = finfo[fids[non_same_fids[i]][1][0]]["type_id"];		// TYPE ID
		new_feature_list[i+nfl_length]["type_data"] = finfo[fids[non_same_fids[i]][1][0]]["type_data"];		// TYPE DATA
		new_feature_list[i+nfl_length]["method"] = finfo[fids[non_same_fids[i]][1][0]]["method_data"];	// CATEGORY 
		new_feature_list[i+nfl_length]["category"] = finfo[fids[non_same_fids[i]][1][0]]["type_category"];	// CATEGORY 
		//new_feature_list[i+nfl_length]["feature_id"] = finfo[fids[non_same_fids[i]][1][0]]["feature_id"];	// ID
		//new_feature_list[i+nfl_length]["feature_label"] = finfo[fids[non_same_fids[i]][1][0]]["feature_label"];	// ID	
		//new_feature_list[i+nfl_length]["feature_label"] = '';
		new_feature_list[i+nfl_length]["server"] = finfo[fids[non_same_fids[i]][1][0]]["annotation_server"];	// SERVER
		new_feature_list[i+nfl_length]["registry_uri"] = finfo[fids[non_same_fids[i]][1][0]]["annotation_server_uri"];	// SERVER ID (REGISTRY URI)
		new_feature_list[i+nfl_length]["version"] = annotation_version[finfo[fids[non_same_fids[i]][1][0]]["xmlnumber"]];	// ANNOTATION VERSION
		
		var featureLabelsPerLines = '';
		for(var a = 0; a < line[i].length; a++)
		   {
		      new_feature_list[i+nfl_length]["features"].push(line[i][a]);		  
		   }
		   
		line_name = "gr_item" + featureXML_num + "_" + new_feature_list[i+nfl_length]["type"] + "_" + new_feature_list[i+nfl_length]["line"];
		line_id_name.push(line_name);
		
		for(var n = 0; n < excluded_das_sources.length; n++)
			{
				if(excluded_das_sources[n].toLowerCase() == new_feature_list[nfl_length]["server"].toLowerCase())
					{
						lineIdValue = 0;
					}
			}
		dasty2.line_id_name2[line_name] = lineIdValue;
		dasty2.line_id_name2_length++;
		
	   }

	   if(line_id_name.length > 0)
	   		{
				dasty2.IdlinesPerType.push([new_feature_list[nfl_length]["type"], line_id_name, line_id_name.length]);
				dasty2.IdlinesPerCategory.push([new_feature_list[nfl_length]["category"], line_id_name, line_id_name.length]);
				dasty2.IdlinesPerServer.push([new_feature_list[nfl_length]["server"], line_id_name, line_id_name.length]);
			}
	

	for(var b = 0; b < new_feature_list.length; b++)
	   {
		 new_feature_list_info2[featureXML_num].push(new_feature_list[b]);
	   }
	
	//create_graphic2("display_graphic", segment_start, segment_stop);
				
 } // function reorganizeIdTypes(array) 