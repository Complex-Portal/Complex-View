// JavaScript Document

//------------------------------------------------------------------------------------------	
// REQUEST INFORMATION FROM DIFFERENT XML SPECIFIYING A LOCAL URL.
//------------------------------------------------------------------------------------------		
			
	  function newid(id, label)
		{
			//var labelName = "";
			//if(label == null || label == ""){labelName = default_filterLabel} else {labelName = label};
			
			//var opa = document.getElementById("display_test");
			//var content_opa = opa.innerHTML;
			//opa.innerHTML = (content_opa + " <br>------------<br>info: " + document.getElementById(label) );
			
			var new_feature_id = document.getElementById(id).value;
			
			if(document.getElementById(label) == null)
				{
					filterLabel = default_filterLabel;
				}
			else
				{
					filterLabel = document.getElementById(label).value;
				}
			

			//var new_feature_label = document.getElementById(label).value;
			
			//var display_query = document.getElementById("display_query");
			//display_query.innerHTML = "";
			//var display_seque = document.getElementById("display_seque");
			//display_seque.innerHTML = "";
			//var display_server_checking = document.getElementById("display_server_checking");
			//display_server_checking.innerHTML = "";
			//var display_feature_details = document.getElementById("display_feature_details");
			//display_feature_details.innerHTML = "";
			//var display_graphic = document.getElementById("display_graphic");
			//display_graphic.innerHTML = "";
			//var display_nonpositional = document.getElementById("display_nonpositional");
			//display_nonpositional.innerHTML = "";
			
			//var display_test = document.getElementById("display_test");
			//display_test.innerHTML = "";
			
			query_id = new_feature_id;
			//filterLabel = new_feature_label;
			
			if (dasty_url_control == 0)
				{
					var display_query = document.getElementById("display_query");
					display_query.innerHTML = "";
					var display_seque = document.getElementById("display_seque");
					display_seque.innerHTML = "";
					var display_server_checking = document.getElementById("display_server_checking");
					display_server_checking.innerHTML = "";
					var display_feature_details = document.getElementById("display_feature_details");
					display_feature_details.innerHTML = "";
					
					var display_ontology_types = document.getElementById("display_maniputation_options3_type_div");
					display_graphic.innerHTML = "";
					var display_ontology_types = document.getElementById("display_maniputation_options3_category_div");
					display_graphic.innerHTML = "";
					
					
					var display_graphic = document.getElementById("display_graphic");
					display_graphic.innerHTML = "";
					var display_nonpositional = document.getElementById("display_nonpositional");
					display_nonpositional.innerHTML = "";
					
					var display_test = document.getElementById("display_test");
					display_test.innerHTML = "";
					
					start_globals();
				}
			else
				{
					document.location.href = createDastyURL();
				}
		}
				
//