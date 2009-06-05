// JavaScript Document
//------------------------------------------------------------------------------------------	
// EXPANDING
//------------------------------------------------------------------------------------------			
function expanding()
	{
	document.getElementById("system_information").innerHTML = "Dasty2 is modifying the columns of the graphic ...";
	if(isExpanded == 0)	
	  {
	  isExpanded = 1;
	  e=document.getElementById('id_column');
	  e.style.display = 'block';
	  
	  var plus_icon = "<img src=\"img/tick01.gif\" border=\"0\" align=\"absbottom\">&nbsp;";
	  var icon = document.getElementById("menu_mo_img_expand");
	  icon.innerHTML = plus_icon;
	  
	  if(expanded_feature_list.length == 0)
	 	{
			var row = 0;
			for(var a = 0; a < new_feature_list_info2.length; a++)
				{
				  if(new_feature_list_info2[a])
				   {
					for(var b = 0; b < new_feature_list_info2[a].length; b++)
						{
							for(var c = 0; c < new_feature_list_info2[a][b]["features"].length; c++)
								{
									expanded_feature_list[row] = [];
									//expanded_feature_list[row]["features"] = [];
									expanded_feature_list[row]["features"] = [new_feature_list_info2[a][b]["features"][c]];
									expanded_feature_list[row]["feature_group"] = new_feature_list_info2[a][b]["feature_group"];
									expanded_feature_list[row]["method"] = new_feature_list_info2[a][b]["method"];
									expanded_feature_list[row]["type"] = new_feature_list_info2[a][b]["type"];
									expanded_feature_list[row]["category"] = new_feature_list_info2[a][b]["category"];
									//expanded_feature_list[row]["feature_id"] = new_feature_list_info2[a][b]["feature_id"];
									expanded_feature_list[row]["feature_label"] = new_feature_list_info2[a][b]["feature_label"];
									expanded_feature_list[row]["server"] = new_feature_list_info2[a][b]["server"];
									expanded_feature_list[row]["xmlnumber"] = a;
									expanded_feature_list[row]["version"] = annotation_version[a];
									//one_feature_list[row] = new_feature_list_info2[a][b];
									//one_feature_list[row]["xmlnumber"] = a;
									row++
								} // for(var c = 0; c < new_feature_list_info2[a][b]["features"].length; c++)
						} // for(var b = 0; b < new_feature_list_info2[a].length; b++)
					} // if(new_feature_list_info2[a])
				} // for(var a = 0; a < new_feature_list_info2.length; a++)
		} // if(expanded_feature_list.length == 0)
	  var graphic_content = document.getElementById("display_graphic");
      graphic_content.innerHTML = '';
	  createGraphic2(0, "display_graphic", sequence_info.sequence_start, sequence_info.sequence_stop, "expanding");
	  //Sortable.create('gr_list0',{ghosting:true,constraint:false}); // Simpler
	  Sortable.create('gr_list0', {onUpdate:function(){new Effect.Highlight('gr_list0', {startcolor:'#CCCCCC'});}});
	  
	  }
	else
	  {
		  isExpanded = 0;
		  e=document.getElementById('id_column');
		  e.style.display = 'none';
		  
		  var minus_icon = "<img src=\"img/notick01.gif\" border=\"0\" align=\"absbottom\">&nbsp;";
		  var icon = document.getElementById("menu_mo_img_expand");
		  icon.innerHTML = minus_icon;
		  
		  sorting();
		  
	  }
	document.getElementById("system_information").innerHTML = "<span style=\"color:#999999\">... Dasty2 finished to modify the columns of the graphic.</span>";
	
	
//--------------------------------

	

		//for(var lu = 0; lu < expanded_feature_list.length; lu++)
			//{
				//var new_feature_list = expanded_feature_list;
				//var opa = document.getElementById("display_test");
				//var content_opa = opa.innerHTML;
				//opa.innerHTML = (content_opa + " <br>------------<br>new_feature_list[" + lu + "][features]: " + new_feature_list[lu]["features"] + "::::: new_feature_list[" + lu + "][feature_group]: " + new_feature_list[lu]["feature_group"] + "::::: new_feature_list[" + lu + "][type]: " + new_feature_list[lu]["type"] + new_feature_list[lu]["category"] + new_feature_list[lu]["server"] + new_feature_list[lu]["xmlnumber"]);
			//}
	}

	
