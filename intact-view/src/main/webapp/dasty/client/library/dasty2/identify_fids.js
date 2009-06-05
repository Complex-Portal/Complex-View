//------------------------------------------------------------------------------------------	  
// Create arrays "fids" and "same_fids" to be able to group features in the graphic.
//------------------------------------------------------------------------------------------	
function lookforSameIdType(featureXML_num, type_counter)
 {	
	//----------------------------------------------------------------------------------
	// Look for features of the same feature ID and put this info in fids
	// fids = [feature_id, [ids]]
	//----------------------------------------------------------------------------------
	var same_fids = []; // rows in fids with more than one feature id.
	var fids = []; // feature ids
	var feature_row_num = 0;
	var add_fids = 0;
	var add_same_fids = 0;
	var tcounter = type_counter; // example: type_counter[0][1]; number of features rows for one type
	var finfo = feature_info[featureXML_num];
	
	//alert(finfo[0]["feature_id"]);
	
	for(var i = 0; i < tcounter.length; i++)
	{
	  feature_row_num = tcounter[i];
	  fids_length = fids.length;
	  if (fids_length == 0)
	   {
		  fids[fids_length] = [];
		  fids[fids_length][0] = finfo[feature_row_num]["feature_id"];
		  fids[fids_length][1] = [];
		  fids[fids_length][1].push(feature_row_num);
	   }
	  else
	   {
		  add_fids = 0;
		  for(var m = 0; m < fids_length; m++)
		     {
				   if(fids[m][0] == finfo[feature_row_num]["feature_id"])
					  {
						fids[m][1].push(feature_row_num);
						add_fids = 1;
						//----------------------------------------------------------------------------------
						// Get same_fids
						// same_fids = [fids rows where there is more than one feature ID]
						//----------------------------------------------------------------------------------
						same_fids_length = same_fids.length;
						if (same_fids_length == 0)
						   {
								 same_fids[same_fids_length] = m;
						   }
						 else
						   { 
								 add_same_fids = 0;
								 for(var e = 0; e < same_fids_length; e++)
								   {
									 if(same_fids[e] == m) {add_same_fids = 1;}
								   }
								 if(add_same_fids == 0) {same_fids[same_fids_length] = m;}
						   }
						 //----------------------------------------------------------------------------------	
					  }
			 }
		  if(dasty2_grouping == false) // RC. 03.05.08
		  	{
		  		same_fids = [];
		  		add_fids = 0;
			}
		  if(add_fids == 0)
		     {
			   fids[fids_length] = [];
			   fids[fids_length][0] = finfo[feature_row_num]["feature_id"];
			   fids[fids_length][1] = [];
			   fids[fids_length][1].push(feature_row_num);
			 }
	     }				   
	 }
	 //reorganizeIdTypes(finfo, fids, same_fids); 
	 organizeIdTypes(featureXML_num, fids, same_fids); 
	 
						   //for(var lu = 0; lu < fids.length; lu++)
						   //{
						   //var opa = document.getElementById("display_test");
						   //var content_opa = opa.innerHTML;
						   //opa.innerHTML = (content_opa + " <br>------------<br>fids[" + lu +"][0]:" + fids[lu][0] + " fids[" + lu +"][0]:" + fids[lu][1]);
						   //}
						   
						   //for(var lu = 0; lu < same_fids.length; lu++)
						   //{
						   //var opa = document.getElementById("display_test");
						   //var content_opa = opa.innerHTML;
						   //opa.innerHTML = (content_opa + " <br>------------<br>same_fids[" + lu + "]:" + same_fids[lu]);
						   //}	

 }	  