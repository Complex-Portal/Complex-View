//------------------------------------------------------------------------------------------	
// CREATE GRAPHIC
//------------------------------------------------------------------------------------------			

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
// When Dasty2 is executed and displays asyncronously annotations, the function
// createGraphic2 is called as many times as DAS sources are retrieved by AJAX. After
// any other operation like sorting, zoom, etc. the function crreateGraphic2 is called
// just once.

// So the firt time the graphic is composed of several <UL>, as many as DAS sources
// displyed. Nonthless after a modification is performed on the graphic, the annotations
// are rearragend in just one <UL>
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

function createGraphic2(featureXML_num, tagId, segment_start, segment_stop, origin)
{
	 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
	 // The origin attributes 'sorting' and 'exapanding' are defined in this function when the
	 // user wants to modify the view of the first graphic loaded. Then the first graphic is
	 // deleted from the main display, the annotation are rearraged acording to the users 
	 // request and a new graphic is created by calling the createGraphic2 function. If origin
	 // is different to one of these two mentioned before Dasty2 will perform a multiple
	 // asyncronous load.
	 
	 // Dasty2 doesn't display one annotation per line but it rearrages as many annotation
	 // one line as possible considering they belong to the same type. Information about this
	 // rearragement is stored in the "feature_list" variable. "finfo" contains all the
	 // properties of the annotation being loaded like attribute, note, link, etc. This
	 // information comes from parse feature.
	 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
	 
	 if(origin == 'sorting')
	 	{
			var feature_list = one_feature_list;
			var finfo = feature_info;
			features_row_number = feature_list.length; // features_row_number is Global
		}
	 else if(origin == 'expanding')
		{
			var feature_list = expanded_feature_list;
			var finfo = feature_info;
			features_row_number = feature_list.length; // features_row_number is Global
		}
	 else if(origin == 'noresults')
	 	{
			var feature_list = [];
		}
	 else
	    {
			var feature_list = new_feature_list_info2[featureXML_num];
			var finfo = feature_info[featureXML_num];
			features_row_number = features_row_number + feature_list.length; // features_row_number is Global
	    }
	 
	 var firstTimeSortByType_temp = dasty2.firstTimeSortByType;
	

	 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
	 // Defining variables used to create the graphic
	 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
	 
	 if(feature_list.length > 0)
     {
		show_col_graphic = 1;
		
		if(isExpanded == 1 && show_col_id == 1)
			{
				var show_col_id_temp = 1;
			}
		else
			{
				var show_col_id_temp = 0;
			}
			
		var seq_start = segment_start -1;
		var seq_stop = segment_stop;
		var seq_width = (seq_stop - seq_start);
		var gr_gap = 1;
		var cols = show_col_category + show_col_type + show_col_id_temp + show_col_graphic + show_col_warning + show_col_server;
		var total_gap = (cols + 1) * gr_gap;
		
		var width_ul = graphic_width; // 523 for short and 700 for long display in the dasty2 website. Graphic_width is global!
		var width_div_category = col_category_width * show_col_category; // col_category_width is global!
		var width_div_type = col_type_width * show_col_type; // col_type_width is global!
		var width_div_id = col_id_width * show_col_id_temp; // col_id_width is global!
		var width_div_server = col_server_width * show_col_server; // col_server_width is global!
		var width_div_warning = col_warning_width * show_col_warning; // col_server_width is global!
		var width_div_graphic = width_ul - (width_div_category + width_div_type + width_div_id + width_div_warning + width_div_server) - (total_gap);
		width_div_graphic_correction = width_div_graphic - 2; // Border correction. The border is actually adding two pixels more.
		
		
		//var left_div_category = (show_col_category * gr_gap) - gr_gap; //show_col_category * gr_gap;
		//var left_div_type = left_div_category + width_div_category + (show_col_type * gr_gap);
		var left_div_type = (show_col_type * gr_gap) - gr_gap; //show_col_category * gr_gap;
		var left_div_id = left_div_type + width_div_type + (show_col_id_temp * gr_gap);
		var left_div_graphic = left_div_id + width_div_id + (show_col_graphic * gr_gap);
		var left_div_warning = left_div_graphic + width_div_graphic + (show_col_server * gr_gap);
		var left_div_server = left_div_warning + width_div_warning + (show_col_warning * gr_gap);
		var left_div_category = left_div_server + width_div_server + (show_col_server * gr_gap);
		
		var height_div = height_graphic_feature + 6; // height_graphic_feature is global!

		var feature_unique_id = [];

		if(browser_name == "Microsoft Internet Explorer")
			{
				var height_graphic_feature_correction = height_graphic_feature; //IE6
				var height_li = height_div -2; //IE6
				//printOnTest("Microsoft Internet Explorer");
			}
		else
			{
				var height_graphic_feature_correction = height_graphic_feature;
				var height_li = height_div;
			}
		
        var div = document.getElementById(tagId);
	
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
	// SHOW ZOOM SLIDE BAR
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		if(show_slide_bar == 1)
		  {		
			if(origin == "grouping")
			  {
			  	if(show_slide_bar_temp == 1)
				  {
					 var slidebar_ul = createSlideBar(width_ul,left_div_graphic, width_div_graphic);
					 div.appendChild(slidebar_ul);

					 activateSlideBar(zoom_start,zoom_end,'gr_slidebar_div_thumb_01','gr_slidebar_div_thumb_02','gr_slidebar_div_track','zoom_start_px','zoom_end_px');
					 show_slide_bar_temp = 0;
				  } // if(show_graphic_tittle_temp == 1)
		 	  }
			else
		  	  {
				var slidebar_ul = createSlideBar(width_ul,left_div_graphic, width_div_graphic);
				div.appendChild(slidebar_ul);
				activateSlideBar(zoom_start + 1,zoom_end,'gr_slidebar_div_thumb_01','gr_slidebar_div_thumb_02','gr_slidebar_div_track','zoom_start_px','zoom_end_px');
		  	  } // if(origin == "grouping")
		   } // (show_graphic_tittle == 1)				
		
		
	
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 	
		// SHOW SCALE-BAR
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		if(show_scale_bar == 1)
		  {		
			if(origin == "grouping")
			  {
			  	if(show_scale_bar_temp == 1)
				  {
					 var scalebar_ul = createScaleBar(vertical_bars, seq_width, width_ul, left_div_graphic, width_div_graphic);
					 div.appendChild(scalebar_ul);
					 show_scale_bar_temp = 0;
				  } // if(show_graphic_tittle_temp == 1)
		 	  }
			else
		  	  {
				var scalebar_ul = createScaleBar(vertical_bars, seq_width, width_ul, left_div_graphic, width_div_graphic);
				div.appendChild(scalebar_ul);
				//alert(bar_px_distance_list);
				//alert(bar_aa_distance_list);
				//fillScaleBar();
		  	  } // if(origin == "grouping")
		   } // (show_graphic_tittle == 1)	
		   
			
			
			
			
			
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		// SHOW TITTLE
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		if(show_graphic_tittle == 1)
		  {		
			if(origin == "grouping")
			  {
			  	if(show_graphic_tittle_temp == 1)
				  {
					 createGraphicTittle(tagId, width_ul, left_div_category, width_div_category, left_div_type, width_div_type, left_div_id, width_div_id, left_div_graphic, width_div_graphic, left_div_server, width_div_server, height_div, show_col_id_temp, left_div_warning, width_div_warning);
					 show_graphic_tittle_temp = 0;
				  } // if(show_graphic_tittle_temp == 1)
		 	  }
			else
		  	  {
			  		 createGraphicTittle(tagId, width_ul, left_div_category, width_div_category, left_div_type, width_div_type, left_div_id, width_div_id, left_div_graphic, width_div_graphic, left_div_server, width_div_server, height_div, show_col_id_temp, left_div_warning, width_div_warning);
		  	  } // if(origin == "grouping")
		   } // (show_graphic_tittle == 1)			
			
				
				
				
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
	// DEFINE UL TAG
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 	
     var my_ul = document.createElement("ul");
		
	 if(origin == 'sorting' || origin == 'expanding')
	 	{		
		  my_ul.setAttribute("id", "gr_list0");
		}
	  else
	    {
		  my_ul.setAttribute("id", "gr_list" + featureXML_num);
		}
		
		my_ul.setAttribute("class", "gr_list_class");
		my_ul.setAttribute("className", "gr_list_class");
		//my_ul.setAttribute("style", "width:" + width_ul + "px;");
		my_ul.style.cssText = "width:" + width_ul + "px;";
		
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		// Reading the information about the rearragement of annotations (one or many annotation per line)
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		
		for(var c = 0; c < feature_list.length; c++)
		 {
			 
			 	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			    // TAKING COLORS FROM THE STYLESHEET
				// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
				feature_color_loop: 
				for(var p = 0; p < stylesheet_properties_info.length; p++)
				  {
				    if(stylesheet_properties_info[p]["type_id"] == feature_list[c]["type"] )
					  {
					   feature_border_color = stylesheet_properties_info[p]["fgcolor_data"];
					   feature_background_color = stylesheet_properties_info[p]["bgcolor_data"];
					   var aborder_type = 'solid';
					   break feature_color_loop;
					  } else
					  {
					   feature_border_color = stylesheet_properties_info[0]["fgcolor_data"];
					   feature_background_color = stylesheet_properties_info[0]["bgcolor_data"];
					   var aborder_type = 'dotted';
					  }
				  }	
				  
				  
				  
				// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			    // DEFINING COLORS FROM THE BACKGOUNG OF THE LINES IN THE GRAPHIC. Alterning two colors
				// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
				if(color_line_background == 1)
					{
					// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 	
					// DEFINING COLORS BY LINE
					// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
					if(feature_list[c]["version"] == 1)
						{
							if (feature_list[c]["feature_group"] == 1)
								{
									if ( bg_color == 2 || bg_color == 4 || bg_color == 6 || bg_color == 8) { bg_color = 3 } else { bg_color = 4 }
								}
							else
								{
									if ( bg_color == 2 || bg_color == 4 || bg_color == 6 || bg_color == 8) { bg_color = 1 } else { bg_color = 2 }
								}						
						}
					else
						{
							if (feature_list[c]["feature_group"] == 1)
								{
									if ( bg_color == 2 || bg_color == 4 || bg_color == 6 || bg_color == 8) { bg_color = 7 } else { bg_color = 8 }
								}
							else
								{
									if ( bg_color == 2 || bg_color == 4 || bg_color == 6 || bg_color == 8) { bg_color = 5 } else { bg_color = 6 }
								}		
						}
					} // if(color_line_background == 1)
				else
					{
						if ( bg_color == 2 ) { bg_color = 1 } else { bg_color = 2 }
					}
					
					
					
		  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		  // FINDING das sources loaded.
		  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 	
		  if(dasty2.loadedDasSources.length == 0)
			{   
			   var lds_temp = dasty2.loadedDasSources_temp.length;
			   var duplicated_source = 0;
			   
			   for(var w = 0; w < lds_temp; w++)
					{
						if(dasty2.loadedDasSources_temp[w] == feature_list[c]["server"])
							{
								duplicated_source = 1;
							}
					}
				
				if(duplicated_source == 0)
					{
						dasty2.loadedDasSources_temp.push(feature_list[c]["server"]);
					}
			}
		   
	
				
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			// CREATE LISTS <LI> (one line) inside the <UL>
 			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
						
				var mycurrent_li = document.createElement("li");
				
				  if(origin == 'sorting' || origin == 'expanding')
					{
					  var feature_xmlnumber = feature_list[c]["xmlnumber"];
					  var li_id_name = "gr_item" + feature_xmlnumber + "_" + feature_list[c]["type"] + "_" + feature_list[c]["line"];
					  mycurrent_li.setAttribute("id", li_id_name);
					}
				  else
					{
					  var li_id_name = "gr_item" + featureXML_num + "_" + feature_list[c]["type"] + "_" + feature_list[c]["line"];
					  mycurrent_li.setAttribute("id", li_id_name);
					}
					
				  if(dasty2.line_id_name2[li_id_name] == 1)
				  	{
						dasty2.countVisibleLines++;
						mycurrent_li.style.cssText = "height:" + height_li + "px; display:block;";
					}
				  else
				  	{
						mycurrent_li.style.cssText = "height:" + height_li + "px; display:none;";
					}


			
			
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			// CREATE CATEGORY COLUMN INSIDE THE LIST <LI>
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			
			if (show_col_category == 1)
			   {			
				var mycurrent_div_category = document.createElement("div");
				mycurrent_div_category.setAttribute("class", "gr_div gr_row_0" + bg_color + " gr_cell_01");
				mycurrent_div_category.setAttribute("className", "gr_div gr_row_0" + bg_color + " gr_cell_01");
				//mycurrent_div_category.setAttribute("style", "top:0px; left:" + left_div_category + "px; width:" + width_div_category + "px; height:" + height_div + "px;");	
				mycurrent_div_category.style.cssText = "top:0px; left:" + left_div_category + "px; width:" + width_div_category + "px; height:" + height_div + "px;";
				
				//var mycurrent_category_text = document.createElement("a");
				//mycurrent_category_text.setAttribute("class", "gr_text_01");
				//mycurrent_category_text.setAttribute("className", "gr_text_01");
				//mycurrent_category_text.setAttribute("href", "");
				
				var mycurrent_category_text = document.createElement("span");
				mycurrent_category_text.setAttribute("class", "gr_text_01");
				mycurrent_category_text.setAttribute("className", "gr_text_01");
				
				var mycurrent_category_text_content = document.createTextNode(feature_list[c]["category"]);
				
				mycurrent_category_text.appendChild(mycurrent_category_text_content);
				mycurrent_div_category.appendChild(mycurrent_category_text);
				
				mycurrent_li.appendChild(mycurrent_div_category);
			   }
	
	
	
	
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			// CREATE TYPE COLUMN INSIDE LIST <LI>
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			if (show_col_type == 1)
			   {			
				var mycurrent_div_type = document.createElement("div");
				mycurrent_div_type.setAttribute("class", "gr_div gr_row_0" + bg_color + " gr_cell_01");
				mycurrent_div_type.setAttribute("className", "gr_div gr_row_0" + bg_color + " gr_cell_01");
			
				mycurrent_div_type.style.cssText = "top:0px; left:" + left_div_type + "px; width:" + width_div_type + "px; height:" + height_div + "px;";	
				
				var mycurrent_type_text_content = document.createTextNode(feature_list[c]["type_data"]);
				
				var useOlsUrl = false;
				for(var w = 0; w < ontologyPrefix.length; w++)
					{
						if(feature_list[c]["type"].indexOf(ontologyPrefix[w]) != -1)
							{
								useOlsUrl = true;
							}
					}
					
				if(useOlsUrl == true)
					{
						var mycurrent_type_text = document.createElement("a");
						mycurrent_type_text.setAttribute("class", "gr_text_01");
						mycurrent_type_text.setAttribute("className", "gr_text_01");
						mycurrent_type_text.setAttribute("target", "_type");
						mycurrent_type_text.setAttribute("href", "http://www.ebi.ac.uk/ontology-lookup/?termId=" + feature_list[c]["type"]);
						
					}
				else
					{
						var mycurrent_type_text = document.createElement("span");
						mycurrent_type_text.setAttribute("class", "gr_text_01");
						mycurrent_type_text.setAttribute("className", "gr_text_01");
					}
					
				mycurrent_type_text.appendChild(mycurrent_type_text_content);
				mycurrent_div_type.appendChild(mycurrent_type_text);

				
				mycurrent_li.appendChild(mycurrent_div_type);
			   }
			
			
			
			
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			// CREATE ID COLUMN INSIDE LIST <LI>
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			
			if (show_col_id_temp == 1)
			  {			
				var mycurrent_div_id = document.createElement("div");
				mycurrent_div_id.setAttribute("class", "gr_div gr_row_0" + bg_color + " gr_cell_01");
				mycurrent_div_id.setAttribute("className", "gr_div gr_row_0" + bg_color + " gr_cell_01");
				//mycurrent_div_id.setAttribute("style", "top:0px; left:" + left_div_id + "px; width:" + width_div_id + "px; height:" + height_div + "px;");	
				mycurrent_div_id.style.cssText = "top:0px; left:" + left_div_id + "px; width:" + width_div_id + "px; height:" + height_div + "px;";
				
				var mycurrent_id_text = document.createElement("a");
				mycurrent_id_text.setAttribute("class", "gr_text_01");	
				mycurrent_id_text.setAttribute("className", "gr_text_01");
				//mycurrent_id_text.setAttribute("href", "");
				
				//var id_row = feature_list[c]["features"][0];
				//var mycurrent_id_text_content = document.createTextNode(finfo[feature_xmlnumber][id_row]["feature_id"]);
				var mycurrent_id_text_content = document.createTextNode(feature_list[c]["feature_id"]);
				
				mycurrent_id_text.appendChild(mycurrent_id_text_content);
				mycurrent_div_id.appendChild(mycurrent_id_text);
				
				mycurrent_li.appendChild(mycurrent_div_id);
			  }
			  
			  
			  
				
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			// CREATE FEATURE COLUMN INSIDE LIST <LI>
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
				
				var mycurrent_div_graphic = document.createElement("div");
				mycurrent_div_graphic.setAttribute("class", "gr_div gr_row_0" + bg_color + " gr_cell_02");	
				mycurrent_div_graphic.setAttribute("className", "gr_div gr_row_0" + bg_color + " gr_cell_02");	
				//mycurrent_div_graphic.setAttribute("style", "top:0px; left:" + left_div_graphic + "px; width:" + width_div_graphic + "px; height:" + height_div + "px;");	
				mycurrent_div_graphic.style.cssText = "top:0px; left:" + left_div_graphic + "px; width:" + width_div_graphic + "px; height:" + height_div + "px;";
				
				
				
					// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
					// RECALCULATE FEATURE SIZE TO FIT IT IN THE GRAPHIC
					// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
					
					for(var n = 0; n < feature_list[c]["features"].length; n++)
					{
					  var row = feature_list[c]["features"][n];
						
					  if(origin == 'sorting' || origin == 'expanding')
					   {	
						// var feature_xmlnumber is defined in the creation of "LISTs"
						var feature_id = finfo[feature_xmlnumber][row]["feature_id"];
						var feature_start = finfo[feature_xmlnumber][row]["start_data"];
						var feature_end =finfo[feature_xmlnumber][row]["end_data"];
					   }
					  else
					   {
						var feature_id = finfo[row]["feature_id"];
						var feature_start = finfo[row]["start_data"]; 
						var feature_end =finfo[row]["end_data"];
						var feature_xmlnumber = finfo[row]["xmlnumber"];
					   }
					   var feature_width = feature_end - feature_start;
					   
					   var original_feature_start = feature_start;
					   var original_feature_end = feature_end;
					   var original_feature_width = feature_width;
					   
					   feature_start = feature_start -1;
					   feature_width = feature_end - feature_start; // It is 1 pixel bigger
			   
			   
			   
			   
					   // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
					   // ZOOM IMPLEMENTATION
					   // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
					   if( zoom_end != 0)
							{
								var zoom_width = zoom_end - zoom_start;
								feature_start = feature_start - zoom_start;
								feature_end = feature_start + feature_width;
								
								var temp_feature_start = feature_start;
								var temp_feature_end = feature_end;
		
								if(temp_feature_start > zoom_end)
								  {
									  feature_start = -1;
								  } // -1 => out of range
								else if(temp_feature_start < 0)
								  {
									  feature_start = 0;
								  } // (feature_start > zoom_end)
								  
								var new_zoom_width = zoom_width; 
								
								if(temp_feature_end > new_zoom_width && temp_feature_start > new_zoom_width)
								  {
									  feature_end = -1;
								  }
								else if(temp_feature_end > new_zoom_width)
								  {
									  feature_end = zoom_width;
								  }
								else if (temp_feature_end < 0)
								  {
									  feature_end = -1;
								  }  // -1 => out of range
								  
								var seq_width = zoom_width;
								feature_width = feature_end - feature_start;
							}
						else
							{
								var temp_feature_start = feature_start;
								var temp_feature_end = feature_end;
								
								if(temp_feature_start > (seq_stop -1))
								  {
									  feature_start = -1;
								  } // -1 => out of range
								// else if(temp_feature_start < seq_stop && temp_feature_end > seq_stop)
								if(temp_feature_end > seq_stop)
								  {
									  feature_end = seq_stop;
								  }
							}


				// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
				// IF ANNOTATION IS OUT OF THE RAGE OF AMINOACIDS TO DISPLAY THEN DON'T DISPLAY, OTHERWISE DISPLAY
				// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
				if(feature_start == -1 || feature_end == -1)
					{
						// Don't create features
					}
				else
					{
						var feature_start_value = ((width_div_graphic_correction * feature_start) / seq_width);
						var feature_end_value = ((width_div_graphic_correction * feature_end) / seq_width);
						var feature_width_value = ((width_div_graphic_correction * feature_width) / seq_width);	
		
						var mycurrent_graphic_feature = document.createElement("a");
						mycurrent_graphic_feature.setAttribute("id", feature_id + "_" + feature_start); // feature_id is not unique
						mycurrent_graphic_feature.setAttribute("class", "feature");	
						mycurrent_graphic_feature.setAttribute("className", "feature");	
						//mycurrent_graphic_feature.setAttribute("href", "");
						
						mycurrent_graphic_feature.style.cssText = "font-size:1px; top:2px; left:" + feature_start_value + "px; width:" + feature_width_value + "px; height:" + height_graphic_feature_correction + "px; border: 1px " + aborder_type + " " + feature_border_color + "; background-color:" + feature_background_color + ";";
						
						mycurrent_graphic_feature.onmouseover = new Function("this.style.backgroundColor = '" + feature_border_color + "';" +  "feature_mouse_action(findPosX(this), findPosY(this), '" + feature_id + "', " + feature_xmlnumber + ", " + row + ", 'mouseover');");
						
						mycurrent_graphic_feature.onmouseout = new Function("this.style.backgroundColor = '" + feature_background_color + "';");
	
						mycurrent_graphic_feature.onclick = new Function("highlightSequence(" + original_feature_start + ", " + original_feature_end + ", " + sequence_limit + ", '" + feature_border_color + "'); " + "feature_mouse_action(findPosX(this), findPosY(this), '" + feature_id + "', " + feature_xmlnumber + ", " + row + ", 'mouseclick');");

						feature_unique_id.push(feature_id + "_" + feature_start);
					
						mycurrent_div_graphic.appendChild(mycurrent_graphic_feature); // DIV with the Graphic
					} // if(feature_start == -1 || feature_end == -1)
			   
			} // for(var n = 0; n < feature_list[c]["features"].length; n++)	

			mycurrent_li.appendChild(mycurrent_div_graphic);

			
			
			

			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			// WARNING ICONS
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			if (show_col_warning == 1)
			   {			
				var mycurrent_div_warning = document.createElement("div");
				mycurrent_div_warning.setAttribute("class", "gr_div gr_row_0" + bg_color + " gr_cell_01");
				mycurrent_div_warning.setAttribute("className", "gr_div gr_row_0" + bg_color + " gr_cell_01");

				mycurrent_div_warning.style.cssText = "top:0px; left:" + left_div_warning + "px; width:" + width_div_warning + "px; height:" + height_div + "px;";
				
				var mycurrent_warning_img_01 = document.createElement("img");
				if(feature_list[c]["version"] == 1)
					{
						mycurrent_warning_img_01.setAttribute("src", dasty_path+"img/checkmark.gif");
					}
				else
					{
						mycurrent_warning_img_01.setAttribute("src", dasty_path+"img/warning.gif");
					}
				
				var mycurrent_warning_img_02 = document.createElement("img");
				if (feature_list[c]["feature_group"] == 1)
					{
						mycurrent_warning_img_02.setAttribute("src", dasty_path+"img/group.gif");
					}
				else
					{
						mycurrent_warning_img_02.setAttribute("src", dasty_path+"img/group2.gif");
					}

				mycurrent_div_warning.appendChild(mycurrent_warning_img_01);
				mycurrent_div_warning.appendChild(mycurrent_warning_img_02);
				
				mycurrent_li.appendChild(mycurrent_div_warning);
			   }


	
	
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			// SERVERs
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			if (show_col_server == 1)
			   {			
				var mycurrent_div_server = document.createElement("div");
				mycurrent_div_server.setAttribute("class", "gr_div gr_row_0" + bg_color + " gr_cell_01");
				mycurrent_div_server.setAttribute("className", "gr_div gr_row_0" + bg_color + " gr_cell_01");

				mycurrent_div_server.style.cssText = "top:0px; left:" + left_div_server + "px; width:" + width_div_server + "px; height:" + height_div + "px;";
				
				var mycurrent_server_text = document.createElement("a");
				mycurrent_server_text.setAttribute("class", "gr_text_01");	
				mycurrent_server_text.setAttribute("className", "gr_text_01");
				mycurrent_server_text.setAttribute("target", "_dasregistry")
				mycurrent_server_text.setAttribute("href", "http://www.dasregistry.org/showdetails.jsp?auto_id=" + feature_list[c]["registry_uri"]);
				
				var mycurrent_server_text_content = document.createTextNode(feature_list[c]["server"]);
				
				mycurrent_server_text.appendChild(mycurrent_server_text_content);
				mycurrent_div_server.appendChild(mycurrent_server_text);
				
				mycurrent_li.appendChild(mycurrent_div_server);
	
			   }
			
			my_ul.appendChild(mycurrent_li);
			
		   
	
		   
		   
		 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		 // FINISHED Reading the information about the rearragement of annotations
		 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -   
		 } // for(var c = 0; c < feature_list.length; c++)
		
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		// CLOSING UL TAG
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 			 
	    div.appendChild(my_ul);




	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
	// Tooltip Manager creates HTML code embeded below the BODY for each annotation to
	// be able to display feature deatails Pop-Ups on on mouse-over.
	// I don't like but this is what I've got at the moment.
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 	
	for(var n = 0; n < feature_unique_id.length; n++)
			{
				TooltipManager.addHTML(feature_unique_id[n], "display_feature_details");
			}


	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
	// FINISH condition if there are annotations
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
	} // if(feature_list.length > 0) 


		if(origin == "grouping" || origin == "noresults")
			{
			count_displayed_groups++;		
			
		     // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			 // ONCE ALL THE ANNOTATIONS HAVE BEEN LOADED FROM ALL THE SERVERS ...
    		 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			if(count_displayed_groups == feature_url.length) // feature_url is global!
				{
						 
						 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 	
						 // CREATE POSITIONAL VERTICAL LINES ON THE GRAPHIC
						 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
						 fillScaleBar();
						 dasty2.countVisibleLines = 0;
						 
						 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
						 // MESSAGE TO DISPLAY WHEN FINISH LOADING 
						 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
						 document.getElementById("system_information").innerHTML = "<span style=\"color:#999999\">... Dasty2 has finished loading the data.</span>";
						
						 
						 
						 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
						 // CREATE TABLE WITH DAS SOURCES DISPLAYED IN DASTY
						 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
						 if(dasty2.loadedDasSources.length == 0)
							{
								dasty2.loadedDasSources = dasty2.loadedDasSources_temp;
							}
							
						 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
						 // CREATE ONTOLOGY
						 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 	
						 if (isOntologyTreeVisible==true)
						 	{
							 create_ontology_tree();
						    }	
						 create_server_tree();
						 
						 
						 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
						 // MAKE NPF SORTABLE
						 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
						 sortables_init();
						 
						 
						 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
						 // MESSAGE - NO ENTRIES FOUND
						 // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
						 if(feature_results_count == 0)
							{
								var system_information = document.getElementById("system_information").innerHTML
								document.getElementById("system_information").innerHTML = system_information + "<br><span style=\"color:#CC0000\">There were no entries found with the Protein ID: \"<strong>" + query_id + "</strong>\" and the Registry label: \"<strong>" + filterLabel + "</strong>\" </span>";
							}
		
						 
						
						if(firstTimeSortByType_temp == true)
							{
								dasty2.firstTimeSortByType = false;
								sorting('type');
							}
							
							
				// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			    // AFTER EVERY ASYCRONOUS LOADING ...
    		    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 	
				/*
				else
					{
	
						// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
						// CREATE SORTABLE LISTS BETWEEN UL - It just works with the first <UL>s
						// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
						var gr_list_array = [];
						for(var fu = 0; fu < results_XML_order.length; fu++)
							  {
								  gr_list_array.push('gr_list' + results_XML_order[fu]);
							  }
						
						for(var fu = 0; fu < results_XML_order.length; fu++)
							  {
								if(fu== 0)
										{
											Sortable.create('gr_list' + results_XML_order[fu], {dropOnEmpty:true,containment:gr_list_array,constraint:false});
										}
									  else
										{
											Sortable.create('gr_list' + results_XML_order[fu], {dropOnEmpty:true,handle:'handle',containment:gr_list_array,constraint:false});
										} // if(fu== 0)
							   } // for(var fu = 0; fu < results_XML_order.length; fu++)
											
					 } //if(feature_url.length == 1)
				*/
					 

				} // if(count_displayed_groups == feature_url.length)
				
	 		} // if(origin == "grouping" || origin == "noresults")
		else 
			{
				fillScaleBar();
				dasty2.countVisibleLines = 0;
			}
			
} // function createGraphic(tagId, segment_start, segment_stop)	





// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
// Calculate Position X and Y for the mouse
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

  function findPosX(obj)
  {
    var curleft = 0;
    if(obj.offsetParent)
        while(1) 
        {
          curleft += obj.offsetLeft;
          if(!obj.offsetParent)
            break;
          obj = obj.offsetParent;
        }
    else if(obj.x)
        curleft += obj.x;
    return curleft;
  }

  function findPosY(obj)
  {
    var curtop = 0;
    if(obj.offsetParent)
        while(1)
        {
          curtop += obj.offsetTop;
          if(!obj.offsetParent)
            break;
          obj = obj.offsetParent;
        }
    else if(obj.y)
        curtop += obj.y;
    return curtop;
  }