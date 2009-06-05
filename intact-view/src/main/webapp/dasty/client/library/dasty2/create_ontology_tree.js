/*
* data used for some of the javascript tree examples. share! :-)
*/
function create_ontology_tree() 
{
	parseOntology(1, onto_path_type);
	parseOntology(2, onto_path_category);
}

/**
* Create Server Tree for the Server secction in the Ontology panel
*/

function create_server_tree() 
{
	var elementChecked = 2;
	var server_tree_element = new Array;
	server_tree_element[0] = new Array;
	server_tree_element[0]['dataContainer'] = "Servers";
	server_tree_element[0]['caption']          = "Servers";
	server_tree_element[0]['isOpen']           = true;
	server_tree_element[0]['isChecked']        = 2;
	server_tree_element[0]['children']         = new Array;
	
	for(var c = 0; c < dasty2.loadedDasSources.length; c++)
		{
			elementChecked = 2;
			for(var n = 0; n < excluded_das_sources.length; n++)
				{
					if(excluded_das_sources[n].toLowerCase() == dasty2.loadedDasSources[c].toLowerCase())
						{
							elementChecked = 0;
						}
				}
			server_tree_element[0]['children'][c] = new Array;
			server_tree_element[0]['children'][c]['dataContainer'] = dasty2.loadedDasSources[c];
			server_tree_element[0]['children'][c]['caption'] = dasty2.loadedDasSources[c];
			server_tree_element[0]['children'][c]['isChecked'] = elementChecked;
			server_tree_element[0]['children'][c]['onChangeCheckbox'] = display_ontology_servers;
			server_tree_element[0]['children'][c]['onClick'] = "javascript:printOnSystemInformation('" + dasty2.loadedDasSources[c] + " DAS Source')";
		}
	server_tree = new Bs_Tree();
	server_tree.imageDir = 'library/blueshoes46/components/tree/img/win98/';
	server_tree.checkboxSystemImgDir = 'library/blueshoes46/components/checkbox/img/win2k_noBorder/';
	server_tree.useCheckboxSystem = true;
	server_tree.checkboxSystemWalkTree = 3;
	server_tree.initByArray(server_tree_element);
	server_tree.drawInto('display_maniputation_options3_server_div');
	
}


/**
* Hide or display ontology types on the graphic
*/

function display_ontology_servers(treeElement)
	{
		if(treeElement.isChecked == 2 || treeElement.isChecked == 0)
			{
				display_ontology(treeElement, "server");
			}
	}

function display_ontology_types(treeElement)
	{
		if(treeElement.isChecked == 2 || treeElement.isChecked == 0)
			{
				display_ontology(treeElement, "type");
			}
	}
	
function display_ontology_categories(treeElement)
	{
		if(treeElement.isChecked == 2 || treeElement.isChecked == 0)
			{
				display_ontology(treeElement, "category");
			}
	}


function display_ontology(treeElement, classification)
{
		var linesPerClassification = lookForIdLines(treeElement.dataContainer, classification);
		var display_block = 'block';
			
		for(var j = 0; j < linesPerClassification[0].length; j++)
			{
				display_block = 'block';
				//printOnTest("LINES: " + linesPerClassification[j]);
				if(linesPerClassification[0][j].indexOf("npf_item") != -1)
					{
						if(browser_name != "Microsoft Internet Explorer")
							{
								display_block = "table-row";
							}
					}
				
				var e=document.getElementById(linesPerClassification[0][j]);
				var element_display = e.style.display;
				if(treeElement.isChecked == 2 && element_display == 'none')
					{
						e.style.display = display_block;
						var style_change = true;
						
						if(typeof dasty2.line_id_name2[linesPerClassification[0][j]] != "undefined")
							{
								dasty2.line_id_name2[linesPerClassification[0][j]] = 1;
							}

					}
				else if(treeElement.isChecked == 0 && element_display == display_block)
					{
						e.style.display = 'none';
						var style_change = true;
						
						if(typeof dasty2.line_id_name2[linesPerClassification[0][j]] != "undefined")
							{
								dasty2.line_id_name2[linesPerClassification[0][j]] = 0;
							}

					}
				//printOnTest(linesPerClassification[j]);
			}
			
		var height_difference = parseInt((linesPerClassification[0].length - linesPerClassification[1]) * (height_graphic_feature + 7));
		
		for(var j = 0; j < vertical_bars; j++)
			{
				if(treeElement.isChecked == 2 && style_change == true)
					{
						var new_height = parseInt(getPxHeightFromStyle("gr_scalebar_div_" + j)) + height_difference;
					}
				else if(treeElement.isChecked == 0 && style_change == true)
					{
						var new_height = parseInt(getPxHeightFromStyle("gr_scalebar_div_" + j)) - height_difference;
					}


				//printOnTest("gr_scalebar_div_" + j);
				//printOnTest(getPxHeightFromStyle("gr_scalebar_div_" + j));
				//printOnTest(new_height);
				
				resize_height = document.getElementById("gr_scalebar_div_" + j);
				resize_height.style.height = new_height + "px";
				//resize_height.style.cssText = "height:" + new_height + "px";
				
				//printOnTest(resize_height.style.height);
				
			}
		//dasty2.IdlinesPerType[treeElement.dataContainer].length
				//
}

	   // TYPE name: dasty2.IdlinesPerType[w][0]
	   // Lines ID with one TYPE: dasty2.IdlinesPerType[w][1]
	   // CATEGORY name: dasty2.IdlinesPerType[w][2]
	   // Number of lines: dasty2.IdlinesPerType[w][3]
	   
function lookForIdLines(target, classification)
	{
		var npf_item_perClassification = 0;
		var idLines = [];
		if(classification == "type")
			{
				for(var j = 0; j < dasty2.IdlinesPerType.length; j++)
					{
						if(dasty2.IdlinesPerType[j][0].toLowerCase() == target.toLowerCase())
							{
								for(var r = 0; r < dasty2.IdlinesPerType[j][2]; r++)
									{
										if(dasty2.IdlinesPerType[j][1][r].indexOf("npf_item") != -1)
											{	
												npf_item_perClassification++;
											}
										idLines.push(dasty2.IdlinesPerType[j][1][r]);
									}
							}
					}
			}
	 	else if(classification == "category")
 			{
				for(var j = 0; j < dasty2.IdlinesPerCategory.length; j++)
					{
						var category_name = dasty2.IdlinesPerCategory[j][0].toLowerCase();
						if(target == "")
							{
								if(category_name == target)
									{
										for(var r = 0; r < dasty2.IdlinesPerCategory[j][2]; r++)
											{
												if(dasty2.IdlinesPerCategory[j][1][r].indexOf("npf_item") != -1)
													{	
														npf_item_perClassification++;
													}
												idLines.push(dasty2.IdlinesPerCategory[j][1][r]);
											}
									}
							}
						else
							{
								if(category_name.indexOf(target.toLowerCase()) > -1)
									{
										for(var r = 0; r < dasty2.IdlinesPerCategory[j][2]; r++)
											{
												if(dasty2.IdlinesPerCategory[j][1][r].indexOf("npf_item") != -1)
													{	
														npf_item_perClassification++;
													}
												idLines.push(dasty2.IdlinesPerCategory[j][1][r]);
											}
									}
							}
						
					}
			}
		else if(classification == "server")
 			{
				for(var j = 0; j < dasty2.IdlinesPerServer.length; j++)
					{
						if(dasty2.IdlinesPerServer[j][0].toLowerCase() == target.toLowerCase())
							{
								for(var r = 0; r < dasty2.IdlinesPerServer[j][2]; r++)
									{
										if(dasty2.IdlinesPerServer[j][1][r].indexOf("npf_item") != -1)
											{	
												npf_item_perClassification++;
											}
										idLines.push(dasty2.IdlinesPerServer[j][1][r]);
									}
							}
					}
			}
 		
		//printOnTest(target + " / " +idLines + " / " + idLines.length);
 		return [idLines, npf_item_perClassification];
 		
 	}

function printOnSystemInformation(message)
	{
		document.getElementById("system_information").innerHTML = message;
	}

/**¨
  *		The algorithm go through the tree from the root to the leafs, when 
  *		find a leaf calls the method isType to see if is a currently used type. 
  *		If is not, the leaf is deleted.  
  *		When all the leafs are deleted its parent is also deleted
  *		@return 
  *			0: if the element wasn't pruned 
  *			1: if the element was pruned 
  *
  **/
function recursivePrune(elem){
	
	var children=elem.getChildren();
	var i=0;
	for (i=0;i<children.length;i++){
		var child = children[i];
		if (child.hasChildren()){
			i=i-recursivePrune(child);
		}else{
			if (!isInArray(child.dataContainer)){
				dasty2.temporalTree.removeElement(child.id);
				i--;
			}else{
				child.isOpen=true;
				child.isChecked=2;
			}
		}
	}

	if (i==0){
		if (!isInArray(elem.dataContainer)){
			dasty2.temporalTree.removeElement(elem.id);
			return 1;
		}
	}
	elem.isOpen=true;
	elem.isChecked=2;


	return 0;
}

/**
* the prune function is not used since it has been included in the parsing
*/

function prune(tree,array,ontoType){
//	printOnTest("Arreglo original");
//	printArray(array);
	dasty2.temporalArray=[];
	for(var i=0;i<array.length;i++){
		if ((array[i].indexOf("SO:")!=-1)||(array[i].indexOf("ECO:")!=-1)||(array[i].indexOf("MOD:")!=-1)){
			dasty2.temporalArray=dasty2.temporalArray.concat(array.splice(i,1));
			i--;
		}
	}
//	printOnTest("Arreglos antes de la poda");
//	printArray(array);
//	printArray(dasty2.temporalArray);

	dasty2.temporalTree=tree;
	var elem=dasty2.temporalTree.getElement(0);
	
	recursivePrune(elem);
	
	var root=elem.getChildren();
	
	
	var elementData = new Array;
	elementData.caption 		= 'Other Terms (Not in the ontology)';
	elementData.dataContainer	= "other_types";
	elementData.isOpen			= false;
	elementData.isChecked		= 2;
	elementData.children		= new Array;
	if (ontoType==1){
		elementData.onChangeCheckbox = display_ontology_types;
	}else{
		elementData.onChangeCheckbox = display_ontology_categories;
	}

	var newElement=elem.addChildByArray(elementData);
	
	for (var i=0;i<array.length;i++){
		var term = new Array;
		term.caption 		= array[i];
		term.dataContainer	= array[i];
		term.isOpen			= true;
		term.isChecked		= 2;
		if (ontoType==1){
			term.onChangeCheckbox = display_ontology_types;
		}else{
			term.onChangeCheckbox = display_ontology_categories;
		}

		newElement.addChildByArray(term);
	}

//	tree.draw();
//	printOnTest("Arreglos despues de la poda");
//	printArray(array);
//	printArray(dasty2.temporalArray);
	
	//Adding terms o

}

function isInArray(value){
	var i=0;
	for(i=0;i<dasty2.temporalArray.length;i++){
		if (dasty2.temporalArray[i].indexOf(value)!=-1){
			dasty2.temporalArray.splice(i,1);
			return true;
		}
	}
	return false;
}