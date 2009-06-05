// JavaScript Document
//------------------------------------------------------------------------------------------	
// SORTING
//------------------------------------------------------------------------------------------			
function sorting(sortby)
	{
	if(one_feature_list.length == 0 && isExpanded == 0)
	 	{
			var row = 0;
			

			for(var a = 0; a < new_feature_list_info2.length; a++)
				{
			//var opa = document.getElementById("display_test");
			//var content_opa = opa.innerHTML;
			//opa.innerHTML = (content_opa + " <br>------------<br>new_feature_list_info2[a]: " + new_feature_list_info2[a]);
				 if(new_feature_list_info2[a])
				   {
					for(var b = 0; b < new_feature_list_info2[a].length; b++)
						{
							one_feature_list[row] = new_feature_list_info2[a][b];
							one_feature_list[row]["xmlnumber"] = a;
							row++
						}
				   } // if(new_feature_list_info2[a])
				}
				
			
		//for(var lu = 0; lu < one_feature_list.length; lu++)
			//{
				//var new_feature_list = one_feature_list;
				//var opa = document.getElementById("display_test");
				//var content_opa = opa.innerHTML;
				//opa.innerHTML = (content_opa + " <br>------------<br>new_feature_list[" + lu + "][features]: " + new_feature_list[lu]["features"] + "::::: new_feature_list[" + lu + "][feature_group]: " + new_feature_list[lu]["feature_group"] + "::::: new_feature_list[" + lu + "][type]: " + new_feature_list[lu]["type"] + new_feature_list[lu]["category"] + new_feature_list[lu]["server"] + new_feature_list[lu]["xmlnumber"]);
			//}
	 	} //if(one_feature_list.length == 0 && isExpanded == 0)

	 
	 if (sortby)
	 	{ 
		  document.getElementById("system_information").innerHTML = "Dasty2 is sorting the graphic by " + sortby + " ...";
		  //alert("zoom beggining");
		  
/* NOT NECESSARY BECAUSE SORTING INCLUDED IN THE GRAPHIC  
		//------------------------------------------------
		// DRAW +/- ICON
		//------------------------------------------------
			var img_display_type = document.getElementById("menu_mo_img_type");
			var img_display_category = document.getElementById("menu_mo_img_category");
			var img_display_server = document.getElementById("menu_mo_img_server");
			var img_display_version = document.getElementById("menu_mo_img_version");
			
			var minus_icon = "<img src=\"img/noradio01.gif\" border=\"0\" align=\"absbottom\">&nbsp;";
			var plus_icon = "<img src=\"img/radio01.gif\" border=\"0\" align=\"absbottom\">&nbsp;";
			
			if(sortby == "type")
				{
					img_display_type.innerHTML = plus_icon; img_display_category.innerHTML = minus_icon;
					img_display_server.innerHTML = minus_icon; img_display_version.innerHTML = minus_icon;
				}
			else if (sortby == "category")
				{
					img_display_type.innerHTML = minus_icon; img_display_category.innerHTML = plus_icon;
					img_display_server.innerHTML = minus_icon; img_display_version.innerHTML = minus_icon;
				}
			else if (sortby == "server")
				{
					img_display_type.innerHTML = minus_icon; img_display_category.innerHTML = minus_icon;
					img_display_server.innerHTML = plus_icon; img_display_version.innerHTML = minus_icon;
				}
			else if (sortby == "version")
				{
					img_display_type.innerHTML = minus_icon; img_display_category.innerHTML = minus_icon;
					img_display_server.innerHTML = minus_icon; img_display_version.innerHTML = plus_icon;
				}
			
*/		  
		  
		  
		  //-------------------------------------------------
		  // ASC DES by Type
		  //-------------------------------------------------
		  if(sortOrderType == -1)
		     {
				sortOrderType = 1; // Descending
				sortOrderType2 = -1;
			 }
		   else
		     {
				 sortOrderType = -1; // Ascending
				 sortOrderType2 = 1;
			 }
			 
	      //-------------------------------------------------
		  // ASC DES by Method
		  //-------------------------------------------------
		  if(sortOrderMethod == -1)
		     {
				sortOrderMethod = 1; // Descending
				sortOrderMethod2 = -1;
			 }
		   else
		     {
				 sortOrderMethod = -1; // Ascending
				 sortOrderMethod2 = 1;
			 }			 
			 
		  //-------------------------------------------------
		  // ASC DES by Category
		  //-------------------------------------------------
		  if(sortOrderCategory == -1)
		     {
				sortOrderCategory = 1; // Descending
				sortOrderCategory2 = -1;
			 }
		   else
		     {
				 sortOrderCategory = -1; // Ascending
				 sortOrderCategory2 = 1;
			 }
		  //-------------------------------------------------
		  // ASC DES by Server
		  //-------------------------------------------------
		  if(sortOrderServer == -1)
		     {
				sortOrderServer = 1; // Descending
				sortOrderServer2 = -1;
			 }
		   else
		     {
				 sortOrderServer = -1; // Ascending
				 sortOrderServer2 = 1;
			 }
			 
		  //-------------------------------------------------
		  // ASC DES by Version
		  //-------------------------------------------------
		  if(sortOrderVersion == -1)
		     {
				sortOrderVersion = -1; // Ascending
				sortOrderVersion2 = 1;
			 }
		   else
		     {
				 sortOrderVersion = 1; // Descending
				 sortOrderVersion2 = -1;
			 }
		  //---------------------------------------------------------------------
		  // SORT "one_feature_list" by "sortby". Sort by indicate a colum number
		  //---------------------------------------------------------------------
		  if(isExpanded == 0)
		  	{
				sortCompactArray(sortby);
			}
		  else
		    {
				sortExpandedArray(sortby);
			}
		}	
	 
	 resetSortingVariables(sortby);
	 
	 var graphic_content = document.getElementById("display_graphic");
     graphic_content.innerHTML = '';
	 if(isExpanded == 0)
	 	{
			createGraphic2(0, "display_graphic", sequence_info.sequence_start, sequence_info.sequence_stop, "sorting");
		}
	 else
	 	{
			createGraphic2(0, "display_graphic", sequence_info.sequence_start, sequence_info.sequence_stop, "expanding");
		}
	 //Sortable.create('gr_list0',{ghosting:true,constraint:false}); // Simpler
	 Sortable.create('gr_list0', {onUpdate:function(){new Effect.Highlight('gr_list0', {startcolor:'#CCCCCC'});}});
	 
	 if (sortby) { document.getElementById("system_information").innerHTML = "<span style=\"color:#999999\">... Dasty2 finished to sort the graphic by " + sortby + "</span>"; }
	 
	} // function sorting(sortby)


  
function sortCompactArray(sortby)
  {

	switch (sortby)
	{
		case "type" :
			one_feature_list.sort(sortByType);
			break;
		case "method" :
			one_feature_list.sort(sortByMethod);
			break;
		case "category" :
			one_feature_list.sort(sortByCategory);
			break;
		case "server" :
			one_feature_list.sort(sortByServer);
			break;
		case "version" :
			one_feature_list.sort(sortByVersion);
			break;
	}
  } // function sortArray(column)
  
  
function sortExpandedArray(sortby)
  {

	switch (sortby)
	{
		case "type" :
			expanded_feature_list.sort(sortByType);
			break;
		case "method" :
			expanded_feature_list.sort(sortByType);
			break;
		case "category" :
			expanded_feature_list.sort(sortByCategory);
			break;
		case "server" :
			expanded_feature_list.sort(sortByServer);
			break;
		case "version" :
			expanded_feature_list.sort(sortByVersion);
			break;
	}
  } // function sortArray(column)
  
  
function sortByType(a, b)
  {
	var x = a.type.toLowerCase();
	var y = b.type.toLowerCase();
	return ((x < y) ? sortOrderType : ((x > y) ? sortOrderType2 : 0));
  } // function sortByType(a, b)

  
function sortByMethod(a, b)
  {
	var x = a.method.toLowerCase();
	var y = b.method.toLowerCase();
	return ((x < y) ? sortOrderMethod : ((x > y) ? sortOrderMethod2 : 0));
  } // function sortByType(a, b)
  

function sortByCategory(a, b)
  {
	var x = a.category.toLowerCase();
	var y = b.category.toLowerCase();
	return ((x < y) ? sortOrderCategory : ((x > y) ? sortOrderCategory2 : sortByType(a, b)));
  } // function sortByCategory(a, b)
  
function sortByServer(a, b)
  {
	var x = a.server.toLowerCase();
	var y = b.server.toLowerCase();
	return ((x < y) ? sortOrderServer : ((x > y) ? sortOrderServer2 : sortByType(a, b)));
  } // function sortByServer(a, b)

function sortByVersion(a, b)
  {
	var x = a.version;
	var y = b.version;
	return ((x < y) ? sortOrderVersion : ((x > y) ? sortOrderVersion2 : sortByServer(a, b)));
  } // function sortByVersion(a, b)

function resetSortingVariables(sortby)
	{
		if(sortby != "type") {sortOrderType = 0}
		if(sortby != "method") {sortOrderMethod = 0}
		if(sortby != "category") {sortOrderCategory = 0}
		if(sortby != "server") {sortOrderServer = 0}
		if(sortby != "version") {sortOrderVersion = 0}
	}
