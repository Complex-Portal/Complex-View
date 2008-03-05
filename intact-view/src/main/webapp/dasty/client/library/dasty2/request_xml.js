//------------------------------------------------------------------------------------------	
// MAKE A SEQUENCE REQUESTS
//------------------------------------------------------------------------------------------
	function makeSequenceRequest()
		{		
			makeRequest(sequence_url[0][1], 'sequenceXML'); // sequence_url is Global!
		}

//

//------------------------------------------------------------------------------------------	
// MAKE A STYLESHEET REQUESTS
//------------------------------------------------------------------------------------------
// I Should retrieve one stylesheet by Annotation server. By now I just will recover the uniprot one.

	function makeStylesheetRequest()
		{
			makeRequest(stylesheet_url[0][1], 'styleSheetXML'); // stylesheet_url is Global!
		}
//

//------------------------------------------------------------------------------------------	
// MAKE A DAS REGISTRY REQUESTS
//------------------------------------------------------------------------------------------


	function makeDasRegistryRequest()
		{
			makeRequest(das_registry_url, 'dasRegistryXML'); // stylesheet_url is Global!
		}
//

//------------------------------------------------------------------------------------------	
// MAKE SEVERAL FEATURE REQUESTS
//------------------------------------------------------------------------------------------
	  
function makeFeatureRequest(requestNumber)
	  {	
	    dasty2.validFirstSources = lookForSpecificSources(first_das_source);
		//printOnTest(dasty2.validFirstSources);
		
		if(dasty2.validFirstSources.length > 0)
			{	
				if(typeof requestNumber == "undefined")
					{
						var requestNumberTemp = 0;
					}
				else
					{
						var requestNumberTemp = requestNumber;
					} //if(requestNumber == null || requestNumber = '')	
					
					dasty2.firstRequestNumber = requestNumberTemp + 1;
					var featureXML_num = dasty2.validFirstSources[requestNumberTemp];
					var url = feature_url[featureXML_num].url;
					makeRequest(url, 'featureXML', featureXML_num);

			}
		 else
		 	{	dasty2.firstRequestNumber++;
				makeFeatureRequest2();
			} //if(first_das_source.length > 0)
	  }	  	  
	  
// Look for specific source names in the Source array file	"feature_url"  
function lookForSpecificSources(sources)
	{
		var sourceFound = [];
		for(var i = 0; i < sources.length; i++)
			{
				for(var h = 0; h < feature_url.length; h++)
					{
						//printOnTest(feature_url[h].id.toLowerCase() + " " + sources[i].toLowerCase());
						if(feature_url[h].id.toLowerCase() == sources[i].toLowerCase())
							{
								sourceFound.push(h);
							}
					}
			}
		return sourceFound;
	}
	


	  
				
function makeFeatureRequest2()
	  {				
	  			// RCJ: Make a aligment request
	  			
				if (isPDBVisible==true){
							parseAlignment(uniprot_pdb_alignment);
				}
				
	  			// RCJ: Make multiple feature request leaving out sources in "first_das_source"
				
				for(var h = 0; h < feature_url.length; h++)
					{
						duplicated = 0;
						
						for(var i = 0; i < first_das_source.length; i++)
				  		  {
							if(feature_url[h].id.toLowerCase() == first_das_source[i].toLowerCase())
								{
									duplicated = 1;
								}
						  }
						  
						if(duplicated == 0)
							{
								var featureXML_num = h;
								var url = feature_url[featureXML_num].url;
								makeRequest(url, 'featureXML', featureXML_num);
							}
					}
	  }


//------------------------------------------------------------------------------------------	
// REQUEST INFORMATION FROM DIFFERENT XML SPECIFIYING A LOCAL URL.
//------------------------------------------------------------------------------------------		
			
	  function makeRequest(url, xml_type, featureXML_num)
			{
				//alert("fn:" + featureXML_num);
				if (xml_type=='sequenceXML')
					{
						http_request.open('GET', url, true);
						http_request.onreadystatechange = parseSequenceXML;
                		http_request.send(null);
					}
				else if (xml_type=='featureXML')
					{
						ajax_feature(featureXML_num);
						//feature_http_request[featureXML_num] =  http_request;
						//feature_http_request[featureXML_num].onreadystatechange = parseFeatureXML;
						feature_http_request[featureXML_num].onreadystatechange = function()
							{
									parseFeatureXML(featureXML_num);
							};
						feature_http_request[featureXML_num].open('GET', url, true);
                		feature_http_request[featureXML_num].send(null);
						
						//alert("featureXML_num: " + featureXML_num);
					}
				else if (xml_type=='serverListXML')
					{
						http_request.open('GET', url, true);
						http_request.onreadystatechange = parseServerListXML;
                		http_request.send(null);
					}
				else if (xml_type=='styleSheetXML')
					{
						http_request.open('GET', url, true);
						http_request.onreadystatechange = parseStylesheetXML;
                		http_request.send(null);
					}
				else if (xml_type=='dasRegistryXML')
					{
						
						http_request.open('GET', url, true);
						http_request.onreadystatechange = parseDasRegistryXML;
                		http_request.send(null);						
						//ajax_elements("das_registry");
						//elements_http_request["das_registry"].open('GET', url, true);
						//elements_http_request["das_registry"].onreadystatechange = parseDasRegistryXML;
                		//elements_http_request["das_registry"].send(null);
					};
                
            }	