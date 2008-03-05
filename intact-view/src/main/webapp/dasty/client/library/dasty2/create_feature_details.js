//------------------------------------------------------------------------------------------	
// CREATE FEATURE DETAILS FIELD // display_feature_details
//------------------------------------------------------------------------------------------	

			function createFeatureDetails(object_row, xmlnumber)
			{
					var fetureDetails = feature_info[xmlnumber][object_row];
					var tagId = "display_feature_details";
					
					fields = ["feature_id", "feature_label", "type_data", "type_id", "type_category", "method_data", "start_data", "end_data", "score_data", "orientation_data", "phase_data", "note_data", "link_data"];
					fields_names = ["Feature ID: ", "Feature label: ", "Type: ", "Type ID: ", "Category: ", "Method: ", "Start: ", "End: ", "Score: ", "Orientation: ", "Phase: ", "Note: ", "Link: "];
					
					var div = document.getElementById(tagId);
					div.innerHTML = '';
					
					
					for(var i = 0; i < fields.length; i++) 
						{
							var feature_details_content = fetureDetails[fields[i]];
							//printOnTest(fields[i] + " / " + typeof(feature_details_content) + " / " + feature_details_content.length);
							
							if(typeof(feature_details_content) == "object") // Content is an array of information. Ex: note_data
								{
									
									for(var a = 0; a < feature_details_content.length; a++) 
										{	
										
											if(fields[i] == "link_data")
												{
													var content_link = fetureDetails["link_href"][a];
												}
											else
												{
													var content_link = null;
												}
											
											var fieldElements = createFieldFeatureDetails(fields[i], fields_names[i], feature_details_content[a], content_link);
											div.appendChild(fieldElements[0]);
											div.appendChild(fieldElements[1]);
											div.appendChild(fieldElements[2]);	
											
										}
								}
							else // Content just have one variable
								{
									var fieldElements = createFieldFeatureDetails(fields[i], fields_names[i], feature_details_content, null);
									
									div.appendChild(fieldElements[0]);
									div.appendChild(fieldElements[1]);
									div.appendChild(fieldElements[2]);	
								
								}
						}
					var contentDiv = div.innerHTML;
					div.innerHTML = contentDiv + "<br><a target=\"_blast\" href=\"http://beta.uniprot.org/blast/?about=" + sequence_info.sequence_id + "[" + fetureDetails["start_data"] + "-" + fetureDetails["end_data"] + "]\"><img  border=\"0\" src=\"img/blast_icon.gif\" /></a>";
				 
			}	  
			
			
function createFieldFeatureDetails(fieldType, fieldTitle, content, fdlink)
	{	
		var textNode = document.createTextNode(content);
	
		var a = document.createElement("span");
		a.setAttribute("id", "display_feature_details_" + fieldType);
		a.setAttribute("class", "title");
		a.setAttribute("className", "title");
		a.appendChild(document.createTextNode(fieldTitle));				  
							
		var b = document.createElement("span");
		b.setAttribute("id", "display_feature_details_" + fieldType + "_content");
		//b.appendChild(document.createTextNode(content));	
		
		if(fdlink != null)
			{
				var link_href = document.createElement("a");
				link_href.setAttribute("target", "_featurelinks");
				link_href.setAttribute("href", fdlink);
				link_href.appendChild(textNode);	
				b.appendChild(link_href);	
			}
		else
			{
				b.appendChild(textNode);	
			}
							
		var c = document.createElement("br");
		
		var fieldElements = [a, b, c];
		
		return fieldElements;
	}