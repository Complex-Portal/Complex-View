//------------------------------------------------------------------------------------------	
// GROUPING
//------------------------------------------------------------------------------------------			
	function grouping(featureXML_num, type_counter,  segment_start, segment_stop)
	  { 
	  	new_feature_list_info2[featureXML_num] = [];
	  	for(var a = 0; a < type_counter.length; a++)
	  		{
				lookforSameIdType(featureXML_num, type_counter[a][1]);
			}
		createGraphic2(featureXML_num, "display_graphic", segment_start, segment_stop, "grouping");
		
		//alert(new_feature_list_info2[featureXML_num].length);
		
		//for(var lu = 0; lu < new_feature_list_info2[featureXML_num].length; lu++)
		//	{
		//		var new_feature_list = new_feature_list_info2[featureXML_num];
		//		var opa = document.getElementById("display_test");
		//		var content_opa = opa.innerHTML;
		//		opa.innerHTML = (content_opa + " <br>------------<br>new_feature_list[" + lu + "][features]: " + new_feature_list[lu]["features"] + "::::: new_feature_list[" + lu + "][feature_group]: " + new_feature_list[lu]["feature_group"] + "::::: new_feature_list[" + lu + "][type]: " + new_feature_list[lu]["type"] + new_feature_list[lu]["category"] + new_feature_list[lu]["server"]);
		//	}
	  }