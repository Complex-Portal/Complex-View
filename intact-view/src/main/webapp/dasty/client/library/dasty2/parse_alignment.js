// Fernando's code to get started with pasing alignments
var testing = false,  		  // used for testing
	alignments = new Array(), // array of parsed alignments
	nalignments = 0;		  // number of alignments	



// <------------- definition of an alignment -------------->

/**
*  defines an alignObject contained withing an alignment
*  @params: accessionid,type,dbsource: attributes of the alignmentObject
*           header,title,description: this three are properties of alignObjectDetail for a PDB
*/
function alignObject(accessionid,type,dbsource,header,title,description){
	this.accessionid = accessionid;
	this.type = type;
	this.dbsource = dbsource;
	this.header = header;
	this.title =title;
	this.description = description;
}

/**
*  defines a segment contained withing an alignment block
*  @params: objid: intObjectId, start: start attribute, end: end attribute
*/
function segment(objid,start,end){
	this.objid = objid;
	this.start = start;
	this.end = end;
}
/**
 *  Compare 2 segments and return a boolean
 */
segment.prototype.equals=function(segm){
	if ((this.objid==segm.objid)&&(this.start==segm.start)&&(this.end==segm.end)){
		return true;
	}
	return false;
}

/**
 *  defines a block contained withing an alignment
 *  @params: order: blockOrder attribute, segments: segments found in block
 */
function block(order,segments){
	this.order = order;
	this.segments = segments;
}

/**
 *  defines an alignment
 *  @params: atype: alignType attribute, blocks: blocks found in the alignment
 */
function alignment(atype,alignObjects,blocks){
	this.atype = atype;
	this.alignObjects = alignObjects;
	this.blocks = blocks;
	
}

// <------------ Loading and Parsing --------------->

/**
 * parse the xml alignment file received in doc
 */
function doParseAligment(doc){ 
	var alignElms = doc.documentElement.getElementsByTagName("alignment");
	if (alignElms.length==0){
		alignments=null;
	}
	if(testing){
		document.getElementById("nalgn").innerHTML = alignElms.length;
	}
	adata = document.getElementById("algdata");
	var s = "";
	for(var i=0;i<alignElms.length;i++){
		algn = alignElms.item(i);
		atype = algn.attributes.getNamedItem("alignType").value;
		blocks = new Array();nblocks = 0;
		alignObjs = new Array();nalobjs = 0;
		for(var j=0;j<algn.childNodes.length;j++){
			//if(algn.childNodes[j].nodeType == document.ELEMENT_NODE){ // Rafael
				if(algn.childNodes[j].nodeName == "block"){
					blocks[nblocks++] = parseBlock(algn.childNodes[j]);
				}else
				if(algn.childNodes[j].nodeName == "alignObject"){
					alignObjs[nalobjs++] = parseAlignObject(algn.childNodes[j]);
				}
			//}
		}
		alignments[nalignments++] = new alignment(atype,alignObjs,blocks);
		alg = new alignment(atype,blocks);
	}
	if(testing){
		var segUniprot=new segment('P00974',40,93);
		var segPDB=segmentUniprot2segmentPDB(segUniprot);
		var extra='';
		if (segPDB!=null){
			extra='<br/><br/><br/><b>ID:</b> '+segPDB.objid+'<br/><b>Start:</b> '+segPDB.start+'<br/><b>End:</b> '+segPDB.end;
		}
		adata.innerHTML = listAlignments()+extra;
	}
	iniStructPanel();
}

/**
 * parse an xml alignObject
 */
function parseAlignObject(obj){
	accessionid = obj.attributes.getNamedItem("dbAccessionId").value;
	type = obj.attributes.getNamedItem("type").value;
	dbsrc = obj.attributes.getNamedItem("dbSource").value;
	adetails = new Array();ndets = 0;header="";title="";descr="";
	for(var k=0;k<obj.childNodes.length;k++){
		if(obj.childNodes[k].nodeName == "alignObjectDetail"){
			detail = obj.childNodes[k];
			if(detail.attributes.length>0)
			{
				property = detail.attributes.getNamedItem("property").value; // printOnTest("type:"+type+" ,prop:"+property);
				switch(property){
					case "header":
						header = getNodeData2(detail);
					break;
					case "title":
						title = getNodeData2(detail);
					break;
					case "molecule description":
						descr = getNodeData2(detail);
					break;
				}
			}
		}
	}
	return new alignObject(accessionid,type,dbsrc,header,title,descr);
}

/**
 * return content if text node, empty string otherwise
 */
function getNodeData2(node){
	if(!node.hasChildNodes)
		return "";
	for(var j=0;j<node.childNodes.length;j++){
		//if(node.childNodes[j].nodeType == document.TEXT_NODE){ // Rafael
			return "value:"+node.childNodes[j].data;	
		//}
	}
	return "";
}

/**
* parse an xml alignment block
*/
function parseBlock(blk){
	blockOrder = blk.attributes.getNamedItem("blockOrder").value;
	segments = new Array();nsegms = 0;
	for(var k=0;k<blk.childNodes.length;k++){
		if(blk.childNodes[k].nodeName == "segment"){
			segm = blk.childNodes[k];
			if(segm.attributes.length>0)
			{
				intObjectId = segm.attributes.getNamedItem("intObjectId").value;
				start = segm.attributes.getNamedItem("start").value;
				end = segm.attributes.getNamedItem("end").value;
				segments[nsegms++] = new segment(intObjectId,start,end);
			}
		}
	}
	return new block(blockOrder,segments);
}

function listAlignments(){
	s = "";
	for(i=0;i<alignments.length;i++){
		s+= (i+1)+".<b>AlignmentType:</b> "+alignments[i].atype+"<br>";
		if(alignments[i].blocks!=null)
		for(j=0;j<alignments[i].blocks.length;j++){
			s+="...Found block with order="+alignments[i].blocks[j].order+"<br>";
			for(k=0;k<alignments[i].blocks[j].segments.length;k++){
				s+=".....Block contains segment objectid="+alignments[i].blocks[j].segments[k].objid+" start="+alignments[i].blocks[j].segments[k].start+" end="+alignments[i].blocks[j].segments[k].end+"<br>"; 
			}
		}
		
		if(alignments[i].alignObjects!=null)
		for(j=0;j<alignments[i].alignObjects.length;j++){
			s+="...Found alignObject with accessionid="+alignments[i].alignObjects[j].accessionid+"<br>";
			s+="......type: "+alignments[i].alignObjects[j].type+"<br>";
			s+="......dbsource: "+alignments[i].alignObjects[j].dbsource+"<br>";
			s+="......header: "+alignments[i].alignObjects[j].header+"<br>";
			s+="......title: "+alignments[i].alignObjects[j].title+"<br>";
			s+="......description: "+alignments[i].alignObjects[j].description+"<br>";
		}
	}
	return s;
}

/**
*	Assume that a block has the segment[0] as the PDB segment and the segment[1] as the Uniprot segment
*   then go through the alignments to get the PDB segment which is with the Uniprot segment.
*   Return null if it doesn't have a match.
*/
function segmentUniprot2segmentPDB(segmentUniprot){
	for(i=0;i<alignments.length;i++){
		if(alignments[i].blocks!=null)
		for(j=0;j<alignments[i].blocks.length;j++){
			if (alignments[i].blocks[j].segments[1].equals(segmentUniprot)){
				return alignments[i].blocks[j].segments[0];
			}
		}
	}
	return null;
}

function extrapolatePosition(position,pdb){
	var term=0;
	var top=0
	var bottom=0
	var entro=false;
	//Find the block with the PDB block target
	for(i=0;i<alignments.length;i++){
		if(alignments[i].blocks!=null)
		for(j=0;j<alignments[i].blocks.length;j++){
			if (alignments[i].blocks[j].segments[0].objid==pdb){
				term= alignments[i].blocks[j].segments[1].start - alignments[i].blocks[j].segments[0].start;
				top=alignments[i].blocks[j].segments[0].end;
				bottom=alignments[i].blocks[j].segments[0].start;
				entro=true;
			}
		}
	}
	//If don't found the block choose the first alignment
	if (entro!=true){
		if (alignments.length>0){
			if(alignments[0].blocks!=null){
				term= alignments[0].blocks[0].segments[1].start - alignments[0].blocks[0].segments[0].start;
				top=alignments[0].blocks[0].segments[0].end;
				bottom=alignments[0].blocks[0].segments[0].start;
			}
		}
	}
	if (1>position-term)
		return bottom;
	if (position-term>top)
		return top;
	return position-term;
}
function extrapolateSegment(segmentQuery){
	var term=0;
	var top=0
	var bottom=0
	var entro=false;
//	segmentUniprot=null;
//	segmentPDB=null;
	if ((segmentUniprot!=null)&&(segmentPDB!=null)){
		var segmentResult;
		segmentResult= new segment();
		segmentResult.objid=segmentPDB.objid;
		if ((Math.round(segmentQuery.start)<Math.round(segmentUniprot.start))&&(Math.round(segmentQuery.end)<Math.round(segmentUniprot.start))){
			segmentResult.start=0;
			segmentResult.end=0;
		}else if ((Math.round(segmentQuery.start)>Math.round(segmentUniprot.end))&&(Math.round(segmentQuery.end)>Math.round(segmentUniprot.end))){
			segmentResult.start=0;
			segmentResult.end=0;
		}else{
			if (Math.round(segmentQuery.start)<Math.round(segmentUniprot.start)){
				segmentResult.start=segmentPDB.start;
			} else{
				segmentResult.start=segmentQuery.start - (segmentUniprot.start - segmentPDB.start);
			}
			if (Math.round(segmentQuery.end)>Math.round(segmentUniprot.end)){
				segmentResult.end=segmentPDB.end;
			} else{
				segmentResult.end=segmentQuery.end - (segmentUniprot.end - segmentPDB.end);
			}
		}
		return segmentResult;
	}
	return null;
}
 

/**
*  get an array of the parsed structure with the name of the pdb's files
*/
function getPDBfileNames(){
	var fileNames = new Array();
	for(i=0;i<alignments.length;i++){
		//printOnTest(alignments[i].alignObjects);
		if(alignments[i].blocks!=null)
		for(j=0;j<alignments[i].alignObjects.length;j++){
			if (alignments[i].alignObjects[j].dbsource == "PDB"){
				fileNames[i]=alignments[i].alignObjects[j].accessionid;
			}
		}
	}
	//alert(fileNames.length);
	return fileNames;
	
}