// JavaScript Document
/**
* load the xml alignment file fom the server
*/
function ajaxCall(url,callback){
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
				if(http_request.responseXML && http_request.responseXML.contentType=="text/xml"){
					callback(http_request.responseXML);
				}else
				if(http_request.responseText){
					callback(http_request.responseText);
				}else
					alert("error");	
			}else
				alert("error");
		}
	}
	http_request.send(null);
}
