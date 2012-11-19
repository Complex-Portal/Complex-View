/**
 * Make a sequence request
 */
function makeSequenceRequest(){
	makeRequest(sequence_url[0][1], 'sequenceXML'); // sequence_url is Global!
}

/**
 * Make a stylesheet request, recovering the uniprot stylesheet
 */
function makeStylesheetRequest() {
	makeRequest(stylesheet_url[0][1], 'styleSheetXML'); // stylesheet_url is Global!
}

/**
 * Make a DAS registry request
 */
function makeDasRegistryRequest() {
	makeRequest(das_registry_url, 'dasRegistryXML'); // stylesheet_url is Global!
}
/**
 * Make a sequence request
 */
function makePDBRequest(PDBid){
	makeRequest(proxy_url+"?t=10&s=http://www.ebi.ac.uk/pdbe-srv/view/files&m=pdb&q="+PDBid+".pdb", 'PDBFile'); // sequence_url is Global!
}

/**
 * Make several feature request doing first the ones on the first_das_source array.
 * The calls for the sources on first_das_source are sequencial i.e. that it process 
 * the source and when it have parsed the file ask for the next source.
 * After process all the first sources makeFeature() is called.
 * @param requestNumber if is the first call is not necessary, afterwards indicate which souce should be query
 */
function makeFeatureRequest(requestNumber) {

	if (typeof requestNumber == "undefined") {
		// RCJ: Make an aligment request. just the first time.
		if (isPDBVisible == true) {
			makeRequest(uniprot_pdb_alignment,'alignmentStructureXML');
		}
	}

	dasty2.validFirstSources = lookForSpecificSources(first_das_source);
	if(dasty2.validFirstSources.length > 0) {	
		if(typeof requestNumber == "undefined") {
			var requestNumberTemp = 0;
		} else {
			var requestNumberTemp = requestNumber;
		} 
		dasty2.firstRequestNumber = requestNumberTemp + 1;
		var featureXML_num = dasty2.validFirstSources[requestNumberTemp];
		var url = feature_url[featureXML_num].url;
		makeRequest(url, 'featureXML', featureXML_num);
	} else {
		dasty2.firstRequestNumber++;
		makeFeatureRequest2();
	} //if(first_das_source.length > 0)
}
	  
/**
 *  Look for specific source names in the Source array file	"feature_url"  
 * @param sources List of sources to search.
 */
function lookForSpecificSources(sources) {
	var sourceFound = [];
	for(var i = 0; i < sources.length; i++) {
		for(var h = 0; h < feature_url.length; h++){
			if(feature_url[h].id.toLowerCase() == sources[i].toLowerCase()) {
					sourceFound.push(h);
			}
		}
	}
	return sourceFound;
}
	

/**
 * Make multiple feature request leaving out sources in "first_das_source"
 */
function makeFeatureRequest2() {
	for(var h = 0; h < feature_url.length; h++) {
		duplicated = 0;
		for(var i = 0; i < first_das_source.length; i++) {
			if(feature_url[h].id.toLowerCase() == first_das_source[i].toLowerCase()) {
					duplicated = 1;
			}
		}  
		if(duplicated == 0) {
			var featureXML_num = h;
			var url = feature_url[featureXML_num].url;
			makeRequest(url, 'featureXML', featureXML_num);
		}
	}
}

/**
 * Request information from different XML Specifiying a local URL
 * @param url Local URL to request
 * @param xml_type type of request ['sequenceXML', 'featureXML', 'styleSheetXML', 'dasRegistryXML']
 * @param featureXML_num just mandatory for requests of the type 'featureXML'. Indicates the number of the  requested query.
 */
function makeRequest(url, xml_type, featureXML_num) {
	if (xml_type=='sequenceXML') {
		new Ajax.Request(url,{
			method: 'get',
            asynchronous: true,
			onSuccess: function(transport){
				parseSequenceXML(transport.responseXML);
			}
		});
	} else if (xml_type=='featureXML') {
		new Ajax.Request(url,{
			method: 'get',
            asynchronous: true,
			onSuccess: function(transport){
				count_displayed_groups++;
				parseFeatureXML(transport.responseXML,featureXML_num);
			},
    		onFailure: function(){
				count_displayed_groups++;
			}
		});
	} else if (xml_type=='styleSheetXML') {
		new Ajax.Request(url,{
			method: 'get',
            asynchronous: true,
			onSuccess: function(transport){
				parseStylesheetXML(transport.responseXML);
			}
		});
	} else if (xml_type=='alignmentStructureXML') {
		new Ajax.Request(url,{
			method: 'get',
            asynchronous: true,
			onSuccess: function(transport){
				doParseAligment(transport.responseXML);
			}
		});
	} else if (xml_type=='dasRegistryXML') {
		new Ajax.Request(url,{
			method: 'get',
            asynchronous: true,
			onSuccess: function(transport){
				parseDasRegistryXML(transport.responseXML);
			}
		});
	} else if (xml_type=='PDBFile') {
		new Ajax.Request(url,{
			method: 'get',
            asynchronous: true,
			onSuccess: function(transport){
				changePDBFile(transport.responseXML);
			}
		});
	}
}	