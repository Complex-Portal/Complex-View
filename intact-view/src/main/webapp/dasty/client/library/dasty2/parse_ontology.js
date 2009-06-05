// Fernando's code to get started with parsing ontologies
var testing = false;  	 // used for testing
	
/**
 * called with url of ontology file to load and parse
 * @param otype ontology to parse (either 1 for type or 2 for category)
 * @param url URL to do the ajax call
 */
function parseOntology(otype,url){
	loadOntology(otype,url);
}

// <------------ Loading and Parsing --------------->

/**
 * parse the xml ontology file received in doc
 * @param doc XML document to parse
 * @param otype: ontology to parse (either 1 for type or 2 for category)
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
		relations = new Array();definition = "";is_definition = false;rel_count = 0;isroot = 0;isobsolete = 0;
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
				}else // Rafael ->
				if(xmlterm.childNodes[j].nodeName == "def"){
					for(var h=0;h<xmlterm.childNodes[j].childNodes.length;h++){
						if(xmlterm.childNodes[j].childNodes[h].nodeName == "defstr"){
							definition = getNodeData(xmlterm.childNodes[j].childNodes[h]);
							is_definition = true;
						}
					}
					// <- Rafael
				}else
				if(xmlterm.childNodes[j].nodeName == "is_obsolete"){
					isobsolete = getNodeData(xmlterm.childNodes[j]);
				}
			//} // if(xmlterm.childNodes[j].nodeType == document.ELEMENT_NODE)
		}
		// Rafael ->
		if(is_definition == false){
			definition = "Description not available";
		}
		// <- Rafael
		if(isobsolete==0){
			if(isroot!=null && isroot==1){
				this.rootptr = 0;
				this.terms[0] = addTerm(id,name,relations,otype,definition);
				this.termsidx[id] = 0;
			}else{ 
				if(this.rootptr==-1){ 
					this.terms[0]= new Array();
					this.terms[this.nterms] = addTerm(id,name,relations,otype,definition);
					this.rootptr = 0;
				}else{
					this.terms[this.nterms] = addTerm(id,name,relations,otype,definition);
				}
				this.termsidx[id] = this.nterms++;
			}
		}
	}
}

/**
 * Return a new term in Tree component format
 * @param id Term ID
 * @param name Term Name
 * @param relations Relations found for the term
 * @param otype ontology to parse (either 1 for type or 2 for category)
 * @param definition Description of the term to show in the system information when the node is selected.
 */
function addTerm(id,name,relations,otype,definition){
	term = new Array(); 
	term['dataContainer'] = id;
	term['caption'] = name;
	term['visible'] = false;
	term['relations'] = relations;
	term['onClick']  = "javascript:printOnSystemInformation('" + id + "; " + definition + "')";
	if(otype==1)
		term['onChangeCheckbox'] = display_ontology_types;
	else
		term['onChangeCheckbox'] = display_ontology_categories;
	return term;
}

/**
 * Return content if text node, empty string otherwise
 * @param node Node to extract the info
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
 * Generate the data structure required by the Tree component
 * @param otype ontology to parse (either 1 for type or 2 for category)
 */
doParseOntology.prototype.genTreeData = function(otype){
	if(this.rootptr==-1){
		// error condition
		printOnTest("Dasty2 could not load the ontology information");
		document.getElementById("system_information").innerHTML = "Dasty2 could not load the ontology information";
		//alert("root not found");
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
	other['isOpen'] = false;
	other['isChecked'] = 2;
	dasty2.temporalArray2=dasty2.temporalArray2.concat(dasty2.temporalArray);
	for (var i=0;i<dasty2.temporalArray2.length;i++){
		var termO = addTerm(dasty2.temporalArray2[i],dasty2.temporalArray2[i],null,otype,"Description not available");
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
* 
*/
/**
 * Family reunion: connects a child with its parent term
 * @param p Parent Node
 * @param child Child Node
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

/**
 * Running on the tree recursively on a bottom-up way, setting the ancestors as vissibles
 * @param child this node and its ancestors will be tagged as vissible 
 */
function setAncestorsVisible(child){
	nodeLoop: 
	for(var i=0;i<collapsedOntologyTypeTerms.length;i++){
		if(child['caption'] == collapsedOntologyTypeTerms[i]){
			child['isOpen'] = false;
			break nodeLoop;
		} else {
			child['isOpen'] = true;
		}
	}

	child['visible'] = true;
	child['isChecked'] = 2;
	if (child['parent']!=null){
		setAncestorsVisible(child['parent']);
	}
}

/**
 * After the algorithm has tagged as vissible just the relevant nodes you can call this function 
 * to delete all the no vissibles (recursive running on the tree in a top down way)
 * @param parent node to eliminate in case is not vissible
 */
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
 * List parsed terms for testing purposes
 * @param t list to show
 * @param indent identation string
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
 * List parsed terms for testing purposes.
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
 * Display ontology tree
 * @param ontoType ontology to parse (either 1 for type or 2 for category)
 */
doParseOntology.prototype.showOntologyTree = function(ontoType){
	if(ontoType==1){
		t = new Bs_Tree();
		t.imageDir = 'library/blueshoes46/components/tree/img/win98/';
		t.checkboxSystemImgDir = 'library/blueshoes46/components/checkbox/img/win2k_noBorder/';
		t.useCheckboxSystem      = true;
		t.checkboxSystemWalkTree = 3;
		t.initByArray(this.terms);
		t.drawInto('display_maniputation_options3_type_div');
	}else if(ontoType==2){
		category_tree = new Bs_Tree();
		category_tree.imageDir = 'library/blueshoes46/components/tree/img/win98/';
		category_tree.checkboxSystemImgDir = 'library/blueshoes46/components/checkbox/img/win2k_noBorder/';
		category_tree.useCheckboxSystem      = true;
		category_tree.checkboxSystemWalkTree = 3;
		category_tree.initByArray(this.terms);
		category_tree.drawInto('display_maniputation_options3_category_div');
	}
}

/**
 * Load the xml ontology file from the server (This is the only ajax call out of request_xml.js)
 * @param otype 1: Types 2:Categories
 * @param url URL to do the ajax call
 */
function loadOntology(otype,url){
	new Ajax.Request(url,{
		method: 'get',
		onSuccess: function(transport){
			var parser = new doParseOntology(transport.responseXML,otype);
			parser.genTreeData(otype);
			if(testing){
				adata = document.getElementById("algdata");
			}
			parser.showOntologyTree(otype);
		}
	});
}


