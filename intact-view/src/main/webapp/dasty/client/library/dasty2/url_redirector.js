
//-----------------------------------------------------
// Get url parameters
//-----------------------------------------------------
// FROM: http://www.11tmr.com/11tmr.nsf/d6plinks/MWHE-695L9Z
// Modified by Rafael Jimenez to get 2 formats

function getURLParam(strParamName)
{
  var strReturn = "";
  var strHref = document.location.href;
  if ( strHref.indexOf("?") > -1 )
  {
    var strQueryString = strHref.substr(strHref.indexOf("?")).toLowerCase();
      
	if ( strHref.indexOf("&") > -1 )
  		{
			var aQueryString = strQueryString.split("&");	
		}
	else if ( strHref.indexOf(":") > -1 )
		{
			var aQueryString = strQueryString.split(":");	
		}

    for ( var iParam = 0; iParam < aQueryString.length; iParam++ )
	{
      if (
aQueryString[iParam].indexOf(strParamName + "=") > -1 )
	  {
        var aParam = aQueryString[iParam].split("=");
        strReturn = aParam[1];
        break;
      }
    }
  }
  return strReturn;
}


//-----------------------------------------------------
// Get the 3 posible different url parameters for Dasty
//-----------------------------------------------------
function getDastyURLParam(param01, param02, param03)
{
	var param = "";
	var url_param01 = getURLParam(param01); // Dasty2
	var url_param02 = getURLParam(param02); // Dasty1 by Biosapiens
	var url_param03 = getURLParam(param03); // Dasty1 by Dasty1
	
	if(url_param01 == null || url_param01 == "")
		{
			if(url_param02 == null || url_param02 == "")
				{
					if(url_param03 == null || url_param03 == "")
						{
							param = null;
						}
					else
						{
							param = url_param03.toUpperCase();
						}
				}
			else
				{
					param = url_param02.toUpperCase();
				}
		}
	else
		{
			param = url_param01.toUpperCase();
		}
	return param;
}


//-----------------------------------------------------
// If the URL is not right create a right one and
// start Dasty2
//-----------------------------------------------------

function setDastyURLParam(redirection) 
	{
		var count_null = 0;
		
		//var query_id_null_temp = query_id_null;
		if(typeof(query_id_null) == "undefined")
			{
				query_id_null = 0;
			}

			
		
		// label
		var url_label = getDastyURLParam("label", "display", "dis");
		if(url_label == null || url_label == "")
			{
				filterLabel = default_filterLabel;
				count_null++;
			}
		else
			{
				filterLabel = url_label;
			}
			
		// id
		var url_id = getDastyURLParam("q", "id", "ID");
		if( url_id == null || url_id == "")
			{
				query_id = default_query_id;
				if( default_query_id == null || default_query_id == "")
					{	
						query_id_null = 1;
					}
				else
					{
						count_null++;
					}
			}
		else
			{
				query_id = url_id;
			}
			
		// timeout
		var url_timeout = getDastyURLParam("t", "t", "t");
		if(url_timeout == null || url_timeout == "")
			{
				timeout = default_timeout;
				count_null++;
			}
		else
			{
				timeout = url_timeout;
			}
			
		if(query_id_null == 0 || redirection == "redirection")	
			{
				// Correct URL if it is not complete otherwise start Dasty2.
				if(count_null > 0 || redirection == "redirection")
					{
						//alert(createDastyURL() + " / " + count_null);
						document.location.href = createDastyURL()
					}
				else
					{
						start_globals();
					}
			}
		else
			{
				start_globals();
			}
	}


//-----------------------------------------------------
// Create Dasty URL
//-----------------------------------------------------

function createDastyURL(newId)
	{
		if(newId)
			{
				query_id = newId;
			}
		var new_url = dasty_mainpage_name + "?q=" + query_id + "&label=" + filterLabel + "&t=" + timeout;
		return new_url;
	}
	

//-----------------------------------------------------
// Create Dasty URL
//-----------------------------------------------------

function createDastyURLNewID(newId)
	{
			if (dasty_url_control == 0)
				{
					query_id = newId;
					
					var display_query = document.getElementById("display_query");
					display_query.innerHTML = "";
					var display_seque = document.getElementById("display_seque");
					display_seque.innerHTML = "";
					var display_server_checking = document.getElementById("display_server_checking");
					display_server_checking.innerHTML = "";
					var display_feature_details = document.getElementById("display_feature_details");
					display_feature_details.innerHTML = "";
					var display_graphic = document.getElementById("display_graphic");
					display_graphic.innerHTML = "";
					var display_nonpositional = document.getElementById("display_nonpositional");
					display_nonpositional.innerHTML = "";
					
					var display_test = document.getElementById("display_test");
					display_test.innerHTML = "";
					
					start_globals();
				}
			else
				{
					document.location.href = createDastyURL(newId);
				}
	}
	

//-----------------------------------------------------
// Redirect pages with other names to index.html
//-----------------------------------------------------

function DastyRedirector()
	{
		//configuration();
		//default_query_parameters()
		//dasty_mainpage_name = "display.html";
		
		//query_id = getURLParam("ID");
		//filterLabel = getURLParam("dis");
		//timeout = getURLParam("t");
		
		//query_id = "";
		//filterLabel = "";
		//timeout = 3;
		
		//default_filterLabel = "BioSapiens";
		//default_query_id = "P05067";
		//default_timeout = 3;
		
		setDastyURLParam("redirection");
		
		//if(query_id == null || query_id == "") {query_id = default_query_id;}
		//if(filterLabel == null || filterLabel == "") {filterLabel = default_filterLabel;}
		//if(timeout == null || timeout == "") {timeout = default_timeout;}
		
		//document.location.href = createDastyURL();
	}
	
	
function findDastyHtmlPageName(option)
	{
		var strHref = document.location.href;
		var strHref_http = strHref.split("http://");	
		var url_dir = strHref_http[1].split("/");
		var url_dir_count = url_dir.length;
		var url_page = url_dir[url_dir_count - 1];
		var url_page_name = url_page.split(".");
		var dasty_page_name = url_page_name[0];
		
		if(url_page_name[1].indexOf("?") > -1)
			{
				var url_page_ext = url_page_name[1].split("?");
				var dasty_ext_name = url_page_ext[0];
			}
		else if(url_page_name[1].indexOf("&") > -1)
			{
				var url_page_ext = url_page_name[1].split("&");
				var dasty_ext_name = url_page_ext[0];
			}
		else if(url_page_name[1].indexOf("#") > -1)
			{
				var url_page_ext = url_page_name[1].split("#");	
				var dasty_ext_name = url_page_ext[0];
			}
		else
			{
				var dasty_ext_name = url_page_name[1]
			}
		
		if(option == 'ext_name')
			{
				return dasty_ext_name;
			}
		else if(option == 'page_name')
			{
				return dasty_page_name;
			}
		else
			{
				return dasty_page_name;
			}
	}
	
	
//-----------------------------------------------------
// Old function createDastyURL()
//-----------------------------------------------------
/*
function createDastyURL()
	{
		var strHref = document.location.href;
		var strQueryString = strHref.split("?");
    	var url = strQueryString[0];
		if(url[url.length -1] == "/")
			{
				var url_options = dasty_mainpage_name + "?q=" + query_id + "&label=" + filterLabel + "&t=" + timeout;
			}
		else
			{
				var url_options = "?q=" + query_id + "&label=" + filterLabel + "&t=" + timeout;
			}
		var new_url = url + url_options;
		return new_url;
	}
*/
//-----------------------------------------------------


//-----------------------------------------------------
// Old function DastyRedirector(file)
//-----------------------------------------------------
/**
 * Dasty 1.0 redirector.
 *
 * Requires:
 * - DastyConfig.js
 *
 * @author  Antony Quinn
 * @version $Id$
 * Modified by Rafel Jimenez
 * The modified version don't require DastyConfig.js
 */

/**
 * Redirects URLs of the form:
 * content?ID=<id>:dis=<label>
 * to:
 * <file>?q=<id>&label=<label>
 *
 * @param file File to redirect to, eg. index.html
 */
 
 /*
function DastyRedirector(file) {
    var id = "", label = "";
    var query = document.location.search.substring(1);
	alert(query);
    var params = query.split(":");
    if (params.length > 0)  {
        id = params[0].split("=")[1];
    }
    if (params.length > 1)  {
        label = params[1].split("=")[1];
    }
    //var url = file + "?" + QueryParamKeys.id + "=" + id;
	var url = file + "?" + "q" + "=" + id; // Rafael modification
    if (label.length > 0)   {
        //url += "&" + QueryParamKeys.registryLabel + "=" + label;
		url += "&" + "label" + "=" + label; // Rafael modification
    }
    //alert(url);
    document.location.href = url;
}
*/
//-----------------------------------------------------