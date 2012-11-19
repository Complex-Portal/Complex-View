


//------------------------------------------------------------------------------------------
//PRINT FUNCTION
//------------------------------------------------------------------------------------------
function printOnTest(arg)
	{
		var opa = document.getElementById("display_test");
		var content_opa = opa.innerHTML;
		opa.innerHTML = (content_opa + " <br>------------<br>Test:" + arg);
	}

//------------------------------------------------------------------------------------------
// FUNCTIONS TO HIGHLIGHT SEQUENCE AND STRUCTURE
//------------------------------------------------------------------------------------------

	/**
	* stores start position of a new highlight request
	*/
	function addPos(x){
		//document.getElementById("output").innerHTML = x;
		selStart = x;
	}

	/**
	* highlight the structure as per highlighted sequence
	*/
	function highlightStru(x){
		var segUniprot;
		if (x>selStart){
			segUniprot=new segment(sequence_info.sequence_id,selStart,x);
		}else {
			segUniprot=new segment(sequence_info.sequence_id,x,selStart);
		}
		var segPDB=extrapolateSegment(segUniprot);
		if (segPDB!=null)
	    	selectPDBResidues(segPDB.start,segPDB.end);
	}


function highlightSequence(fstart, fend, aa_num_per_line, color)
	{
		fstart-=1;fend-=1;
	   	for(var i=0;i<sequence_info["sequence_length"];i++){
			var spanBase = document.getElementById("aa_"+(i+1));
	   		if ((i>=fstart)&&(i<=fend)){
	   			 spanBase.setAttribute("class","highlightSequence");
	   			 spanBase.style.color=color;
//	   			 spanBase.style.border-bottom="1px";
	   		}else{
	   			 spanBase.setAttribute("class","");

	   			 spanBase.style.color="black";
//	   			 spanBase.style.border-bottom="1px";
	   		}
	   	}
	}
	//

//------------------------------------------------------------------------------------------
// FUNCTION TO SHOW AND HIDE CONTENT
//------------------------------------------------------------------------------------------
	function changeDisplayState(child, img_span, img_num)
	{
				//d=document.getElementById(father);
				e=document.getElementById(child);
				if (e.style.display == 'none' || e.style.display =="") {
					e.style.display = 'block';
					if( img_span == null || img_span == "")
				  		{
							// Do nothing
					  	}
					else
						{
							var img_display = document.getElementById(img_span);
							img_display.innerHTML = "<img src=\"img/minus0" + img_num + ".gif\" border=\"0\" align=\"absbottom\">&nbsp;";
						}

				} else {
					e.style.display = 'none';
					if( img_span == null || img_span == "")
				  		{
							// Do nothing
					  	}
					else
						{
							var img_display = document.getElementById(img_span);
							img_display.innerHTML = "<img src=\"img/plus0" + img_num + ".gif\" border=\"0\" align=\"absbottom\">&nbsp;";
						}
				}
	}


//------------------------------------------------------------------------------------------
// CHECK AND PUT THE RIGHT +/- ICONS IN THE MAIN HTML PAGE
//------------------------------------------------------------------------------------------
	function checkSectionIcons()
		{

					var plus_icon = "<img src=\"img/minus02.gif\" border=\"0\" align=\"absbottom\">&nbsp;";
					var plus_icon_white = "<img src=\"img/minus01.gif\" border=\"0\" align=\"absbottom\">&nbsp;";

					var display_query_box = document.getElementById("display_query_box_div");
					var menu_query_box_img = document.getElementById("menu_query_box_img");
					if (display_query_box.style.display == 'block')
						{
							menu_query_box_img.innerHTML = plus_icon;
						}

					var display_checking = document.getElementById("display_checking_div");
					var menu_checking_img = document.getElementById("menu_checking_img");
					if (display_checking.style.display == 'block')
						{
							menu_checking_img.innerHTML = plus_icon;
						}

					var display_server_checking = document.getElementById("display_server_checking_div");
					var menu_server_checking_img = document.getElementById("menu_server_checking_img");
					if (display_server_checking.style.display == 'block')
						{
							menu_server_checking_img.innerHTML = plus_icon;
						}

					var display_query = document.getElementById("display_query_div");
					var menu_query_img = document.getElementById("menu_query_img");
					if (display_query.style.display == 'block')
						{
							menu_query_img.innerHTML = plus_icon;
						}

					var display_seque = document.getElementById("display_seque_div");
					var menu_seque_img = document.getElementById("menu_seque_img");
					if (display_seque.style.display == 'block')
						{
							menu_seque_img.innerHTML = plus_icon;
						}

					var display_feature_details = document.getElementById("display_feature_details_div");
					var menu_feature_details_img = document.getElementById("menu_feature_details_img");
					if (display_feature_details.style.display == 'block')
						{
							menu_feature_details_img.innerHTML = plus_icon;
						}

					var display_maniputation_options = document.getElementById("display_maniputation_options_div");
					var menu_maniputation_options_img = document.getElementById("menu_maniputation_options_img");
					if (display_maniputation_options.style.display == 'block')
						{
							menu_maniputation_options_img.innerHTML = plus_icon;
						}


					var display_maniputation_options3 = document.getElementById("display_maniputation_options3_div");
					var menu_maniputation_options3_img = document.getElementById("menu_maniputation_options3_img");
					if (display_maniputation_options3.style.display == 'block')
						{
							menu_maniputation_options3_img.innerHTML = plus_icon;
							//menu_maniputation_options2_img.innerHTML = plus_icon_white;
						}

					var display_maniputation_options3_type = document.getElementById("display_maniputation_options3_type_div");
					var menu_maniputation_options3_type_img = document.getElementById("menu_maniputation_options3_type_img");
					if (display_maniputation_options3_type.style.display == 'block')
						{
							menu_maniputation_options3_type_img.innerHTML = plus_icon;
							//menu_maniputation_options2_img.innerHTML = plus_icon_white;
						}

					var display_maniputation_options3_category = document.getElementById("display_maniputation_options3_category_div");
					var menu_maniputation_options3_category_img = document.getElementById("menu_maniputation_options3_category_img");
					if (display_maniputation_options3_category.style.display == 'block')
						{
							menu_maniputation_options3_category_img.innerHTML = plus_icon;
							//menu_maniputation_options2_img.innerHTML = plus_icon_white;
						}

					var display_maniputation_options3_server = document.getElementById("display_maniputation_options3_server_div");
					var menu_maniputation_options3_server_img = document.getElementById("menu_maniputation_options3_server_img");
					if (display_maniputation_options3_server.style.display == 'block')
						{
							menu_maniputation_options3_server_img.innerHTML = plus_icon;
							//menu_maniputation_options2_img.innerHTML = plus_icon_white;
						}

					var display_graphic = document.getElementById("display_graphic_div");
					var menu_graphic_img = document.getElementById("menu_graphic_img");
					if (display_graphic.style.display == 'block')
						{
							menu_graphic_img.innerHTML = plus_icon;
						}

					var display_nonpositional = document.getElementById("display_nonpositional_div");
					var menu_nonpositional_img = document.getElementById("menu_nonpositional_img");
					if (display_nonpositional.style.display == 'block')
						{
							menu_nonpositional_img.innerHTML = plus_icon;
						}

					var display_test = document.getElementById("display_test_div");
					var menu_test_img = document.getElementById("menu_test_img");
					if (display_test.style.display == 'block')
						{
							menu_test_img.innerHTML = plus_icon;
						}
		} // function checkIcons()



//------------------------------------------------------------------------------------------
// CHECK AND PUT THE RIGHT +/- ICONS IN THE MANIPULATION OPTIONS
//------------------------------------------------------------------------------------------
	function checkMoColumnIcons()
		{

					var plus_icon = "<img src=\"img/tick01.gif\" border=\"0\" align=\"absbottom\">&nbsp;";
					var minus_icon = "<img src=\"img/notick01.gif\" border=\"0\" align=\"absbottom\">&nbsp;";


					var mo_img_category_column = document.getElementById("menu_mo_img_category_column");
					if (show_col_category == 1)
						{
							mo_img_category_column.innerHTML = plus_icon;
						}
					else
						{
							mo_img_category_column.innerHTML = minus_icon;
						}
					var mo_img_method_column = document.getElementById("menu_mo_img_method_column");
					if (show_col_method == 1)
						{
							mo_img_method_column.innerHTML = plus_icon;
						}
					else
						{
							mo_img_method_column.innerHTML = minus_icon;
						}

					var mo_img_type_column = document.getElementById("menu_mo_img_type_column");
					if (show_col_type == 1)
						{
							mo_img_type_column.innerHTML = plus_icon;
						}
					else
						{
							mo_img_type_column.innerHTML = minus_icon;
						}

					var mo_img_server_column = document.getElementById("menu_mo_img_server_column");
					if (show_col_server == 1)
						{
							mo_img_server_column.innerHTML = plus_icon;
						}
					else
						{
							mo_img_server_column.innerHTML = minus_icon;
						}

					var mo_img_id_column = document.getElementById("menu_mo_img_id_column");
					if (show_col_id == 1)
						{
							mo_img_id_column.innerHTML = plus_icon;
						}
					else
						{
							mo_img_id_column.innerHTML = minus_icon;
						}

					if (show_popup == 2)
						{
							var menu_mo_img_popups = document.getElementById("menu_mo_img_popups");
							menu_mo_img_popups.innerHTML = plus_icon;
						}

					//if(show_popup == 0)
						//{
							//var mo_img_popup_no = document.getElementById("menu_mo_img_popup_no");
							//mo_img_popup_no.innerHTML = radio_icon;
						//}
					//else if(show_popup == 1)
						//{
							//var mo_img_popup_mouseover = document.getElementById("menu_mo_img_popup_mouseover");
							//mo_img_popup_mouseover.innerHTML = radio_icon;
						//}
					//else
						//{
							//var mo_img_popup_mouseclick = document.getElementById("menu_mo_img_popup_mouseclick");
							//mo_img_popup_mouseclick.innerHTML = radio_icon;
						//}

		} // function checkIcons()


//------------------------------------------------------------------------------------------
// FUNCTION TO FIND DOTS
//------------------------------------------------------------------------------------------
	function isDot(s)
	{
		var dot;
		var i;
		var u = String(s);
		loop:
		for (i = 0; i < u.length; i++)
		{
			if (u[i] == ".") {
			  dot = "true";
			  break loop;
			} else {
			  dot = "false";
			}
		}
		if (dot == "false") { return false; } else { return true; }
	}
	//

//------------------------------------------------------------------------------------------
// IS NUMERIC
//------------------------------------------------------------------------------------------
function isNumeric(x)
	{
		var RegExp = /^(-)?(\d*)(\.?)(\d*)$/; // Note: this WILL allow a number that ends in a decimal: -452.
		// compare the argument to the RegEx
		// the 'match' function returns 0 if the value didn't match
		var result = x.match(RegExp);
		if (result==null) result=false;
		return result;
	}


//------------------------------------------------------------------------------------------
// Getting window size and scroll bars position
//------------------------------------------------------------------------------------------
// by http://www.softcomplex.com/docs/get_window_size_and_scrollbar_position.html

function f_clientWidth() {
	return f_filterResults (
		window.innerWidth ? window.innerWidth : 0,
		document.documentElement ? document.documentElement.clientWidth : 0,
		document.body ? document.body.clientWidth : 0
	);
}
function f_clientHeight() {
	return f_filterResults (
		window.innerHeight ? window.innerHeight : 0,
		document.documentElement ? document.documentElement.clientHeight : 0,
		document.body ? document.body.clientHeight : 0
	);
}
function f_scrollLeft() {
	return f_filterResults (
		window.pageXOffset ? window.pageXOffset : 0,
		document.documentElement ? document.documentElement.scrollLeft : 0,
		document.body ? document.body.scrollLeft : 0
	);
}
function f_scrollTop() {
	return f_filterResults (
		window.pageYOffset ? window.pageYOffset : 0,
		document.documentElement ? document.documentElement.scrollTop : 0,
		document.body ? document.body.scrollTop : 0
	);
}
function f_filterResults(n_win, n_docel, n_body) {
	var n_result = n_win ? n_win : 0;
	if (n_docel && (!n_result || (n_result > n_docel)))
		n_result = n_docel;
	return n_body && (!n_result || (n_result > n_body)) ? n_body : n_result;
}

//------------------------------------------------------------------------------------------
// PRELOAD IMAGES
//------------------------------------------------------------------------------------------
// http://elouai.com/javascript-preload-images.php
//------------------------------------------------------------------------------------------

function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

//------------------------------------------------------------------------------------------
// STRUCTURE RELATED FUNCTIONS
//------------------------------------------------------------------------------------------
// adapted from <a href='http://cargo.bioinfo.cnio.es'>Cargo</a> and JMol by Fernando <a href="mailto:fernando@softlech.com">Fernando@softlech.com</a>
//------------------------------------------------------------------------------------------

/**
*  Initialize the Jmol Applet and load (so far hardcoded) a .pdb file
*/
function iniStructPanel(){
	if (alignments==null)
		{
			//document.getElementById("applet").innerHTML="<b>WARNING:</b><i>Dasty2 could not find PDBs associated to this protein ID</i>";
			document.getElementById("display_protstru_div").innerHTML="<br>&nbsp;&nbsp;<span class='title' style='font-style:italic;'>Dasty2 could not find PDBs associated to this protein ID on the 'biojavapdbuniprot' DAS aligment server</span><br>&nbsp;";
			return;
		}
	else
		{
			document.getElementById("display_protstru_div").innerHTML="<div id='applet3d' style='width:100%; height:100%'></div><table width='100%'><tr><td><div id='Applet_Size'></div></td><td rowspan='3' align='right' valign='top'><div id='dropdown'></div></td></tr><tr><td><div id='StruPane_Title'>Structure [Id here]</div></td></tr><tr><td><div id='structureInfo'><p id='structureInfoP'>Additional info here</p></div></td></tr><tr><td colspan='2'><span style='border-top: 1px dotted #999999; display:block; text-align:right;'><a style='text-decoration:none;color:#999999;' href='javascript:changeStructure();'>view/restore image</a></span></td></tr></table>"
		}

	var divDd=document.getElementById("dropdown");
	if (divDd!=null){
		var select=document.createElement("select");
		select.setAttribute("id","PDBlist");
		//select.setAttribute("size","4"); // Rafael
		select.size = 4;
		//select.setAttribute("onchange","changeStructure(this);"); // Rafael
		select.onchange = function() {changeStructure(this);};
		var fileNames= getPDBfileNames();
		for (i=0;i<fileNames.length;i++){
			var idPDB=fileNames[i];
			var punt=idPDB.indexOf(".");
			if (punt!=-1){
				var chain=trim(idPDB.substring(punt+1));
				if (chain=="A"){
					var option2=document.createElement("option");
					option2.innerHTML=idPDB.substring(0,punt+1);
					select.appendChild(option2);
				}
			}
			var option=document.createElement("option");
			if (i==0)
				option.setAttribute("selected","true");
				//option.selected = true;
			option.innerHTML=idPDB;
			select.appendChild(option);
		}
		divDd.appendChild(select);
		segmentUniprot=null;
		segmentPDB=null;

		jmolInitialize("library/jmol");
		//jmolSetDocument(document.getElementById("applet3d").document);
		jmolSetDocument(0);
		jmolSetAppletColor("white");
		//document.getElementById("applet").style.cssText = " z-index:99999;";
		//document.getElementById("applet").innerHTML=jmolApplet("340");
		document.getElementById("applet3d").innerHTML=jmolApplet("100%");
		changeStructure();
	}
}

/**
*  Public function. Called when a new pdb file is selected.
*
*/
function changePDBFile(){
		var script=getJmolLoadScript();
		if (script!=null){
			jmolScriptWait(script);
			loadedPDBMessage();
		}
}


function changeStructure(){
	loadingPDBMessage();
	var PDBList=document.getElementById("PDBlist");
	//var PDBname=PDBList.value; // Rafael
	var PDBname = PDBList.options[PDBList.selectedIndex].text
	//alert(PDBList.selectedIndex);
  	//alert(PDBList.options[PDBList.selectedIndex].text);
	var punto=PDBname.indexOf(".");
	PDBid="";
	if (punto!=-1){
		PDBid=PDBname.substring(0,punto);
	} else {
		PDBid=PDBname;
	}
		structureChain=trim(PDBname.substring(punto+1));
		//var pathPDB="pdb/"+PDBid+".pdb";
		//ajaxCall(proxy_url+"?t=10&s=http://www.rcsb.org/pdb/files&m=pdb&q="+PDBid+".pdb",changePDBFile);
		var pathPDB="pdb/"+PDBid+".pdb";
		makePDBRequest(PDBid);

		//Find the block with the PDB block target
		for(i=0;i<alignments.length;i++){
			if(alignments[i].blocks!=null)
			for(j=0;j<alignments[i].blocks.length;j++){
				if (alignments[i].blocks[j].segments[0].objid.indexOf(PDBid)!=-1){
					segmentPDB=alignments[i].blocks[j].segments[0];
					segmentUniprot=alignments[i].blocks[j].segments[1];
					entro=true;
				}
			}
		}
		//If don't found the block choose the first alignment
		if (entro!=true){
			if (alignments.length>0){
				if(alignments[0].blocks!=null){
					segmentPDB=alignments[0].blocks[0].segments[0];
					segmentUniprot=alignments[0].blocks[0].segments[1];
				}
			}
		}


	//}
}
/**
*  Private function to erase the spaces at the end and at the beggining of a String
*/
function trim(cadena){
    var i=0;
	for(i=0; i<cadena.length; )	{
		if(cadena.charAt(i)==" ")
			cadena=cadena.substring(i+1, cadena.length);
		else
			break;
	}
	for(i=cadena.length-1; i>=0; i=cadena.length-1)	{
		if(cadena.charAt(i)==" ")
			cadena=cadena.substring(0,i);
		else
			break;
	}
	return cadena;
}

/**
*  Private function to get the initial jmol script to load a model
*/
function getJmolLoadScript() {
	if  (PDBid!="") {
        var pathToPdb = proxy_url+"?t=10&s=http://www.ebi.ac.uk/pdbe-srv/view/files&m=pdb&q="+PDBid+".pdb";
		var script ="load "+pathToPdb+"; select all; cartoon on; wireframe off; spacefill off; color chain;";
		return script;
   }
   alert("error in pdb file");
   return null;
}

/**
*  Public function. Call when selecting a range of PDB residues is needed
*  @params: - start: first PDB residue to select, - end: final PDB residue to select
*/
function selectPDBResidues(start, end){
    alert(start + " : " + end);
	var divDd=document.getElementById("dropdown");
	if (divDd!=null){
		var scr='select '+start+'-'+end+'; selectionHalos on;';
		jmolScriptWait(scr);
	}
}

/**
*  Public Function. Call when selecting the region of a anotation is needed
* @params: - feature_row:  , - feature_xmlnumber:
*/
function selectAnotationRegion(feature_row, feature_xmlnumber){
    var objectTest = [];
    objectTest = feature_info[feature_xmlnumber];
	var segUniprot=new segment(sequence_info.sequence_id,objectTest[feature_row]["start_data"],objectTest[feature_row]["end_data"]);
	var segPDB=extrapolateSegment(segUniprot);
	if (segPDB!=null)
    	selectPDBResidues(segPDB.start,segPDB.end);
}

/**
*  ON DEVELOPMENT
*  Public Function. Get the value of some sequence position in the structure
*  @params: - sequencePosition: Integer value for a position in the sequence.
*  @return: Integer value for the position in the structure.
*/
function getValueInStructure(sequencePosition){
    //TODO: call the alignment to get the value of some sequence position in the structure
	var PDBList=document.getElementById("PDBlist");
	if (PDBList!=null){
		var PDBname=PDBList.value;
	    var structurePosition=extrapolatePosition(sequencePosition, PDBname);
		return structurePosition;
	}
}

/*
*  ON DEVELOPMENT
*  Public Function. get the number of the limits of a selection in the sequence DIV
*  @params: - sequenceDiv: The DIV element in dasty, where a sequence is showed
*/
function getSequenceRangeSelected(sequenceDiv){
	var tags=sequenceDiv.getElementsByTagName("*");
	var salida='selecciono secuencia';
	for (i=0; i<sequenceDiv.childNodes.length; i++){
		salida=salida+' - '+sequenceDiv.childNodes[i].nodeValue;
	}
    var txt = '';
	if (window.getSelection){
        txt = window.getSelection();
    } else if (document.getSelection) {
        txt = document.getSelection();
    } else if (document.selection) {
        txt = document.selection.createRange().text;
    } else return;
	alert(salida+' - - - - - - - '+txt);
}

function loadingPDBMessage(){
	var div=document.getElementById("StruPane_Title");
	div.innerHTML="<img src='img/loading.gif' width='20px' /> <i>Loading the PDB file...</i>";
	var div2=document.getElementById("structureInfo");
	div2.innerHTML="";
}
function loadedPDBMessage(){
	var div=document.getElementById("StruPane_Title");
	div.innerHTML="Structure ["+PDBid+"]";
	if ((segmentUniprot!=null) && (segmentPDB!=null)){
		var div2=document.getElementById("structureInfo");
		div2.innerHTML="PDB Region: "+segmentPDB.start+" To:"+segmentPDB.end+"<br/>Uniprot Region: "+segmentUniprot.start+" To:"+segmentUniprot.end;
	}
	var div3=document.getElementById("Applet_Size");
	div3.innerHTML="";
	div3.appendChild(createDivOfSize());

}

function createDivOfSize(){
	var div=document.createElement("div");
	var small=document.createElement("input");
        small.setAttribute("type","button");
        small.setAttribute("value","Small");
        small.setAttribute("onclick","changeAppletSize(330,330); changeStructure()");

        div.appendChild(small);
        var medium=document.createElement("input");
        medium.setAttribute("type","button");
        medium.setAttribute("value","Medium");
        medium.setAttribute("onclick","changeAppletSize(600,600); changeStructure()");
        div.appendChild(medium);
        var large=document.createElement("input");
        large.setAttribute("type","button");
        large.setAttribute("value","Large");
        large.setAttribute("onclick","changeAppletSize(800,800); changeStructure()");
        div.appendChild(large);
	return div;
}






function changeAppletSize(width, height){
	var applet=document.getElementById("jmolApplet0");
	applet.setAttribute("width",width);
	applet.setAttribute("height",height);
	var div=document.getElementById("display_protstru_div");
	//div.setAttribute("width",width+100);
	var div_width = width+20;
	var div_height = height+120;
	//div.style.cssText = "width:" + div_width + "px; height:" + div_height + "px;";
	div.style.cssText = "width:" + div_width + "px;";
}
function printArray(array){
		var texto="L="+array.length+" >";
		var k=0;
		for(k=0;k<array.length;k++){
			texto += "-"+array[k]+"-";
		}
		printOnTest(texto+" <");
}


function printTest()
	{
		printOnTest(PDBid);
	}
