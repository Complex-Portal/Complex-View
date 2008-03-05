//------------------------------------------------------------------------------------------	
// CREATE SEQUENCE TEXT CONTENT
//------------------------------------------------------------------------------------------	
	  
	  		function createSequenceField(tagId)
			{
        	var sequenceDiv = document.createElement("div");
			sequenceDiv.setAttribute("id", "display_seque_protein");
			sequenceDiv.setAttribute("class", "sequence_format");
			sequenceDiv.setAttribute("className", "sequence_format");

//			sequenceDiv.setAttribute("onmouseup", "getSequenceRangeSelected(this)");
			//sequenceDiv.appendChild(document.createTextNode(text));				  
			document.getElementById(tagId).appendChild(sequenceDiv);
			
			//highlightSequence(0, 0, sequence_limit); // this function will take the "sequence" global variable with the aa sequence
			
			//makeRequest('xml/server_list.xml', 'serverListXML');
			//makeStylesheetRequest();
			sequenceDiv.innerHTML = sequence;//(new_sequence);		
			}
	  //