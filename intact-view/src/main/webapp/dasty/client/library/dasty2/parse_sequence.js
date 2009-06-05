/**
* Used to transform the sequence data to highlight the 3D structure from the sequence panel.
* @param seqData String with the sequence.
*/
function transformSeqData(seqData){ //return seqData;
	if(seqData == null || seqData.length == 0)
		return "";
	var seq = "";
	var seqDataArray = new Array;
	seqDataArray = seqData.toArray();
	for(var i=0;i<seqDataArray.length;i++){
		seq +="<span id='aa_"+(i+1)+"' onmousedown='addPos("+(i+1)+");' onmouseup='highlightStru("+(i+1)+");'>"+seqDataArray[i]+"</span>";
		if ((i+1)%sequence_limit==0){
			seq +="<br/>";
		}
	}	
	return seq;
}
/**
 * Parse a sequence XML and populate query and sequence fields
 * @param xmldoc
 */            
function parseSequenceXML(xmldoc) { 
	document.getElementById("system_information").innerHTML = "... loading sequence information from the Reference Server";

	var excep = xmldoc.getElementsByTagName('exception').item(0);
    var root_node = xmldoc.getElementsByTagName('DASSEQUENCE').item(0);

	if(root_node) {
		if (root_node.hasChildNodes()) {
			var children = root_node.childNodes;
			sequence_loop: 
			for (var j = 0; j < children.length; j++) {
				if (children[j].nodeName == 'SEQUENCE') {
					if(useHighlight) {
						sequence = transformSeqData(children[j].firstChild.data);
					} else {
						sequence = children[j].firstChild.data;
					}
					var sequence_attrs = children[j].attributes;
					for(var i=sequence_attrs.length-1; i>=0; i--) {
						if (sequence_attrs[i].name == 'id') { var sequence_id = sequence_attrs[i].value; }
						else if (sequence_attrs[i].name == 'version') { var sequence_version = sequence_attrs[i].value; }
						else if (sequence_attrs[i].name == 'start') { var sequence_start = sequence_attrs[i].value; }
						else if (sequence_attrs[i].name == 'stop') { var sequence_stop = sequence_attrs[i].value; }
						else if (sequence_attrs[i].name == 'moltype') { var sequence_moltype = sequence_attrs[i].value; };
					}
					var sequence_length = sequence_stop - (parseInt(sequence_start) - 1);
					var sequence_row = {sequence: sequence, sequence_id: sequence_id, sequence_version: sequence_version, sequence_start: sequence_start, sequence_stop: sequence_stop, sequence_moltype: sequence_moltype, sequence_length: sequence_length };
					 
					sequence_info = sequence_row;
					 
					createQueryInformationField(sequence_info, "display_query");
					createSequenceField("display_seque"); 
					 
					break sequence_loop;
				}
			}
		}
	} // if(root_node)
	if (excep != null) {
		document.getElementById("system_information").innerHTML = "<span style=\"color:#CC0000\">Das Reference Server warning:<br/><a href=\"" + sequence_url[0][1] + "\" target=\"_blank\">" + excep.firstChild.data + "</a><br/>Dasty2 could not load data from the servers</span>";
	} else {	
		makeStylesheetRequest();
	}
}
