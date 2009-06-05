//------------------------------------------------------------------------------------------	
// CREATE QUERY INFORMATION FIELD
//------------------------------------------------------------------------------------------	
	  
	  		function createQueryInformationField(object, tagId)
			{
			var div = document.getElementById(tagId);
			
	        var a = document.createElement("span");
			a.setAttribute("id", "display_query_id_title");
			a.setAttribute("class", "title");
			a.setAttribute("className", "title");
			a.appendChild(document.createTextNode("Sequence ID: "));				  
			div.appendChild(a);
			
			var uniprot_link = document.createElement("a");
			uniprot_link.setAttribute("target", "_uniprot");
			uniprot_link.setAttribute("href", "http://www.uniprot.org/uniprot/" + object["sequence_id"]);			
			
				var uniprot_acc = document.createTextNode(object["sequence_id"]);
			
			uniprot_link.appendChild(uniprot_acc);
			
			var b = document.createElement("span");
			b.setAttribute("id", "display_query_id_content");
			b.appendChild(uniprot_link);				  
			div.appendChild(b);		
			
			var c = document.createElement("br");
			div.appendChild(c);
			
			var d = document.createElement("span");
			d.setAttribute("id", "display_query_length_title");
			d.setAttribute("class", "title");
			d.setAttribute("className", "title");
			d.appendChild(document.createTextNode("Sequence length: "));				  
			div.appendChild(d);
			
			var e = document.createElement("span");
			e.setAttribute("id", "display_query_length_content");
			e.appendChild(document.createTextNode(object["sequence_length"]));				  
			div.appendChild(e);
			
			document.getElementById('zoom_start_px').value = 1;
			document.getElementById('zoom_end_px').value = object["sequence_length"];
			
			}
	  //