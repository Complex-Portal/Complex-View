// Fernando's code to get started with parsing ontologies
var testing = false;  	 // used for testing
	
/**
* called with url of ontology file to load and parse
* @params: otype: ontology to parse (either 1 for type or 2 for category)
*/
function parseOntology(otype,url){
	loadOntology(otype,url);
}

// <------------ Loading and Parsing --------------->

/**
* parse the xml ontology file received in doc
*/
function doParseOntology(doc,otype){ 
	//Preparing the arrays of types and categories. The graphic must be loaded
	dasty2.temporalArray=[];
	dasty2.temporalArray2=[];
	dasty2.leaves=[];
	
	if(otype==1){
		//Types
		dasty2.temporalArray2=dasty2.typesLoaded;
	}else{
		//Categories
		dasty2.temporalArray2=dasty2.categoriesLoaded;
	}
	for(var i=0;i<dasty2.temporalArray2.length;i++){
		if ((dasty2.temporalArray2[i].indexOf("SO:")!=-1)||(dasty2.temporalArray2[i].indexOf("ECO:")!=-1)||(dasty2.temporalArray2[i].indexOf("MOD:")!=-1)){
			dasty2.temporalArray=dasty2.temporalArray.concat(dasty2.temporalArray2.splice(i,1));
			i--;
		}
	}	
	
	
	xmlterms = doc.documentElement.getElementsByTagName("term");

	
	if(testing){
		document.getElementById("nalgn").innerHTML = xmlterms.length;
	}
	this.terms = new Array(), // array of parsed ontology terms
	this.nterms = 1;		  	 // number of terms in ontology. Init to 1, pos. 0 reserved for root	
	this.rootptr = -1;		 // pointer to root
	var s = "";this.termsidx = new Array();
	for(var i=0;i<xmlterms.length;i++){
		xmlterm = xmlterms.item(i);
		relations = new Array();rel_count = 0;isroot = 0;isobsolete = 0;
		for(var j=0;j<xmlterm.childNodes.length;j++){
			//if(xmlterm.childNodes[j].nodeType == document.ELEMENT_NODE){ // Rafael
				if(xmlterm.childNodes[j].nodeName == "id"){
					id = getNodeData(xmlterm.childNodes[j]);
				}else
				if(xmlterm.childNodes[j].nodeName == "name"){
					name = getNodeData(xmlterm.childNodes[j]);
				}else
				if(xmlterm.childNodes[j].nodeName == "is_a"){
					relations[rel_count] = new Array();
					relations[rel_count]['type'] = 'is_a';
					relations[rel_count++]['termid'] = getNodeData(xmlterm.childNodes[j]);
				}else
				if(xmlterm.childNodes[j].nodeName == "has_a"){
					relations[rel_count] = new Array();
					relations[rel_count]['type'] = 'has_a';
					relations[rel_count++]['termid'] = getNodeData(xmlterm.childNodes[j]);
				}else
				if(xmlterm.childNodes[j].nodeName == "is_root"){
					isroot = getNodeData(xmlterm.childNodes[j]);
				}else
				if(xmlterm.childNodes[j].nodeName == "is_obsolete"){
					isobsolete = getNodeData(xmlterm.childNodes[j]);
				}
			//} // if(xmlterm.childNodes[j].nodeType == document.ELEMENT_NODE)
		}
		if(isobsolete==0){
			if(isroot!=null && isroot==1){
				this.rootptr = 0;
				this.terms[0] = addTerm(id,name,relations,otype);
				this.termsidx[id] = 0;
			}else{ 
				if(this.rootptr==-1){ 
					this.terms[0]= new Array();
					this.terms[this.nterms] = addTerm(id,name,relations,otype);
					this.rootptr = 0;
				}else{
					this.terms[this.nterms] = addTerm(id,name,relations,otype);
				}
				this.termsidx[id] = this.nterms++;
			}
		}
	}
}

/**
* return a new term in Tree component format
* @params: id=term id, name = term name, relations = relations found for the term
*/
function addTerm(id,name,relations,otype){
	term = new Array(); 
	term['dataContainer'] = id;
	term['caption'] = name;
	term['visible'] = false;
	term['relations'] = relations;
	if(otype==1)
		term['onChangeCheckbox'] = display_ontology_types;
	else
		term['onChangeCheckbox'] = display_ontology_categories;
	return term;
}

/**
* return content if text node, empty string otherwise
*/
function getNodeData(node){
	if(!node.hasChildNodes)
		return "";
	for(var j=0;j<node.childNodes.length;j++){
		//if(node.childNodes[j].nodeType == document.TEXT_NODE){	 // Rafael
			return node.childNodes[j].data;	
		//}
	}
	return "";
}

/**
* generate the data structure required by the Tree component
*/
doParseOntology.prototype.genTreeData = function(otype){
	if(this.rootptr==-1){
		// error condition
		alert("root not found");
	}
	for(i=0;i<this.terms.length;i++){
		if(this.terms[i]['relations']!=null && this.terms[i]['relations'].length>0){
			for(j=0;j<this.terms[i]['relations'].length;j++){
				if(this.terms[i]['relations'][j]['type']=='is_a'){
					if(this.terms[this.termsidx[this.terms[i]['relations'][j]['termid']]]==null)
						alert("null for i="+i+" and dc="+this.terms[i]['dataContainer']+" rel with="+this.terms[i]['relations'][j]['termid']+" idx="+this.termsidx[this.terms[i]['relations'][j]['termid']]);
					parent(this.terms[this.termsidx[this.terms[i]['relations'][j]['termid']]],this.terms[i]
					);
				}
			}
		}
	}
	n=this.terms.length;i=0;
	while(i<n && n>0){
		if(this.terms[i]['added']!=null){ 
			this.terms.splice(i,1);
			n=this.terms.length;i=0;
		}else
			i++;
	}
	
	//tag all the leaves and its ancesters as visibles
	for(var i=0;i<dasty2.leaves.length;i++){
			setAncestorsVisible(dasty2.leaves[i]);
	}  
	
	//Delete the not visible children
	for(var i=0;i<this.terms.length;i++){
		delNotVisibleChildren(this.terms[i]);
	}
	
	//Add the subtree of non-ontology terms
	var other = addTerm("other_types","Other Terms (Not in the ontology)",null,otype);
	other['visible'] = true;
	other['isOpen'] = true;
	other['isChecked'] = 2;
	dasty2.temporalArray2=dasty2.temporalArray2.concat(dasty2.temporalArray);
	for (var i=0;i<dasty2.temporalArray2.length;i++){
		var termO = addTerm(dasty2.temporalArray2[i],dasty2.temporalArray2[i],null,otype);
		termO['visible'] = true;
		termO['isOpen'] = true;
		termO['isChecked'] = 2;
		if(other['children']==null){
			other['children'] = new Array();
			other['children'][0] = termO;
		}else{
			other['children'][other['children'].length] = termO;
		}
	}
	this.terms[this.terms.length] = other;
}

/**
* family reunion: connects a child with its parent term
*/
function parent(p,child){
	if(p['children']==null){
		p['children'] = new Array();
		p['children'][0] = child;
	}else{
		p['children'][p['children'].length] = child;
	}
	child['parent']=p;
	if (isInArray(child['dataContainer'])){
		dasty2.leaves[dasty2.leaves.length]=child;
	}
	child['added'] = true;
}

function setAncestorsVisible(child){
	child['visible'] = true;
	child['isOpen'] = true;
	child['isChecked'] = 2;
	if (child['parent']!=null){
		setAncestorsVisible(child['parent']);
	}
}
function delNotVisibleChildren(parent){
	if(parent['children']!=null){
		for(var i=0;i<parent['children'].length;i++){
			var child=parent['children'][i];
			if (child['visible']!=true){
				parent['children'].splice(i,1);
				i--;
			}else{
				delNotVisibleChildren(child);
			}
		}
	}
}


/**
* list parsed terms for testing purposes
*/
function listTerms(t,indent){
	var s= indent+t['dataContainer']+"<br>";
	if(t['children']!=null){
		for(var j=0;j<t['children'].length;j++){
			s+=listTerms(t['children'][j],indent+".");
		}
	}
	return s;
}

/**
* list parsed terms for testing purposes
*/
function listTerms2(){
	s = "";
	for(i=0;i<this.terms.length;i++){
		s+= (i+1)+".<b>TermId:</b> "+this.terms[i]['dataContainer']+"<br>";
		if(this.terms[i]['relations']!=null)
		for(j=0;j<this.terms[i]['relations'].length;j++){
			s+="...Found that "+this.terms[i]['dataContainer']+" "+this.terms[i]['relations'][j]['type']+" "+this.terms[i]['relations'][j]['termid']+"<br>";
		}
	}
	return s;
}

/**
* display ontology for testing purposes
*/
doParseOntology.prototype.showOntologyTree = function(ontoType){
	if(ontoType==1){
		t = new Bs_Tree();
		t.imageDir = dasty_path+'library/blueshoes46/components/tree/img/win98/';
		t.checkboxSystemImgDir = dasty_path+'library/blueshoes46/components/checkbox/img/win2k_noBorder/';
		t.useCheckboxSystem      = true;
		//t.useAutoSequence =  false;
		t.checkboxSystemWalkTree = 3;
		t.initByArray(this.terms);
		//prune(t,dasty2.typesLoaded,ontoType);
		t.drawInto('display_maniputation_options3_type_div');
	}else if(ontoType==2){
		category_tree = new Bs_Tree();
		category_tree.imageDir = dasty_path+'library/blueshoes46/components/tree/img/win98/';
		category_tree.checkboxSystemImgDir = dasty_path+'library/blueshoes46/components/checkbox/img/win2k_noBorder/';
		category_tree.useCheckboxSystem      = true;
		category_tree.checkboxSystemWalkTree = 3;
		category_tree.initByArray(this.terms);
		//prune(category_tree,dasty2.categoriesLoaded,ontoType);
		category_tree.drawInto('display_maniputation_options3_category_div');
	}
}

/**
* load the xml ontology file from the server
*/
function loadOntology(otype,url){
	var http_request = false;
	if (window.XMLHttpRequest) { 
		http_request = new XMLHttpRequest();
	} else if (window.ActiveXObject) { // IE.
		try {
			http_request = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
				http_request = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) {}
		}
	}
	http_request.open('GET', url, true);
	http_request.onreadystatechange = function(){
		if (http_request.readyState == 4) {
			if (http_request.status == 200) { 
				if(http_request.responseText && testing)
					document.getElementById("xmlalgn").value= http_request.responseText;
				//if(http_request.responseXML && http_request.responseXML.contentType=="text/xml") // Rafa
				if(http_request.responseXML)
						{	
						parser = new doParseOntology(http_request.responseXML,otype);
						parser.genTreeData(otype);
						if(testing){
							adata = document.getElementById("algdata");
						}
						parser.showOntologyTree(otype);
				}else
					alert("wrong type ontology");	
			}else
				alert("wrong code");
		}
	}
	http_request.send(null);
}
