<?
/** DAS proxy. PHP version.
* Proxies requests to DAS Registry and DAS servers. Query parameters:
*   s   Server URL
*   m   Method (one of: sequence, features, types, stylesheet, registry, or pdb)
*   q   DAS segment (only required for sequence and features methods)
*   t   HTTP timeout (default is 5 seconds)
*   Call statically (see proxy.php)
*
* Author    Fernando Martinez <fernando@softlech.com>
*/
class DasProxy{

// Default proxy server
// const DEFAULT_PROXY = 'http://wwwcache.ebi.ac.uk:3128/';
 const DEFAULT_PROXY = '';

// Path in the server for the cache dir to the PDB files
 const CACHE_PATH='pdb/';

// DAS error codes
// See http://biodas.org/documents/spec.html
private $dasErrorCodes = array(
		       200=>'OK, data follows',
		       400=>'Bad command',
		       401=>'Bad data source',
		       402=>'Bad command arguments',
		       403=>'Bad reference object',
		       404=>'Bad stylesheet',
		       405=>'Coordinate error',
		       500=>'Server error',
		       501=>'Unimplemented feature',
		      );

    /** 
    * Get URL of network proxy server, if required
    * See http://cpan.uwinnipeg.ca/htdocs/libwww-perl/LWP/UserAgent.html*ua_gt_env_proxy
    */
    function getProxyServer()    {
        $proxy = '';
        if (isset($_ENV['CGI_HTTP_PROXY'])) {
            $proxy = $_ENV['CGI_HTTP_PROXY'];
        }
        else    {
            // Use default
            $proxy = DasProxy::DEFAULT_PROXY;
        }
        return $proxy;
    }
    
    /** 
    * Returns response from DAS server or registry, or exception if error.
    */
    function getResponse($url, $timeout){
        $proxy = DasProxy::getProxyServer();
        $ctx = null;
        if(strlen($proxy)>0){
            $aContext = array(
                'http' => array(
                'proxy' => $proxy, 
                'request_fulluri' => True,
                ),
            );
            $ctx = stream_context_create($aContext);
        }
        $url=str_replace(" ","%20",$url);
        if($_REQUEST['m']=='pdb' && file_exists(DasProxy::CACHE_PATH.$_REQUEST['q'])){
            $_SESSION['pdb'] = DasProxy::CACHE_PATH.$_REQUEST['q'];
            return "<response>ok</response>";
        }else if($_REQUEST['m']=='ontology' && file_exists(DasProxy::CACHE_PATH.$_REQUEST['q'])){
            $_SESSION['onto'] = DasProxy::CACHE_PATH.$_REQUEST['q'];
            return file_get_contents(DasProxy::CACHE_PATH.$_REQUEST['q']);
        }else{  
			if(strlen($proxy)>0)
				$handle = fopen($url, "rb",false,$ctx);
			else
				$handle = fopen($url, "rb");	
			if($handle === false){
				error_log("error reading from ".$url);
				DasProxy::throwException("error reading from ".$url);
				return;
			}
			stream_set_timeout($handle, $timeout);
			stream_set_blocking($handle,0);
			$content = DasProxy::readUrlContent($handle);
	        if (isset($content) && strlen($content)>0) {
	            if($_REQUEST['m']=='pdb'){
					fclose($handle);
	                file_put_contents(DasProxy::CACHE_PATH.$_REQUEST['q'],$content);
	                $_SESSION['pdb'] = DasProxy::CACHE_PATH.$_REQUEST['q'];
	                return "<response>ok</response>";
	            }
				$dasStatus = DasProxy::getHeader($handle,"x-das-status");
				fclose($handle);
				if(isset($dasStatus) && ($stval=intval($dasStatus))!==200)
					DasProxy::throwException($dasStatus." ".$dasErrorCodes[$stval]);
				else	
	            	return $content;
	        }
	        else {
	            // HTTP error
				fclose($handle);
	            error_log("Failed to retrieve ".$url);
				DasProxy::throwException("Failed to retrieve ".$url);
	        }
        }
    }
    
	function readUrlContent($handle){
		$content = '';
		while (!feof($handle)) {
		 	$content .= fread($handle, 8192);
		 	$info = stream_get_meta_data($handle);
		 	if ($info['timed_out']) {
				DasProxy::throwException("Timeout error");
				return null;
			}
		}
		return $content;
	}
	
    /**
    * Get HTTP timeout
    */
    function getTimeout(){
        // Get query parameters
        $timeout = 5;
        if (isset($_REQUEST['t'])){
            $timeout = $_REQUEST['t'];
        }
        else  {
        }
        return $timeout;
    }
    
    /** 
    * Get URL of DAS server or DAS registry
    */
    function getUrl()    {
    
        // Get query parameters
        $server = "";
        $method = "";
        $id     = "";
        $reg_authority	= "";
        $reg_label		= "";
        $reg_type		= "";
        
        if (isset($_REQUEST['s'])) {
            $server = $_REQUEST['s'];
            $method = $_REQUEST['m'];
            $id     = $_REQUEST['q'];
            $reg_authority	= $_REQUEST['a'];
            $reg_label		= $_REQUEST['l'];
            $reg_type		= $_REQUEST['c'];
        }
        // Command line (useful for testing)
        elseif (isset($ARGV[0])){
            $server = $ARGV[0];
            $method = $ARGV[1];
            $id     = $ARGV[2];
            $reg_authority	=  $ARGV[3];
            $reg_label		= $ARGV[4];
            $reg_type		= $ARGV[5];
        }
    
        // Check
        DasProxy::checkParam("server", $server);
        DasProxy::checkParam("method", $method);
    
        // Check server has trailing slash
        $last_char = substr($server,-1, 1);
        if (!($last_char=="/"))   {
            $server .= "/";
        }
    
        // Build URL
        $url = "";
        if ($method=="sequence" || $method=="features")	{
           DasProxy::checkParam("id", $id);
           $url = $server.$method."?segment=".$id;
        }
        // PDB URL example: http://www.pdb.org/pdb/files/1aal.pdb
        elseif ($method=="pdb")	{
           DasProxy::checkParam("id", $id);
           $url = $server.$id;
        }
        elseif ($method=="alignment")	{
           DasProxy::checkParam("id", $id);
           $url = $server.$method."?query=".$id;
        }elseif ($method=="ontology")	{
           DasProxy::checkParam("id", $id);
           $url = $server.$method."?query=".$id;error_log("here and url=".$url." and id=".$id);
        }elseif ($method=="types" || $method=="stylesheet")	{
           $url = $server.$method;
        }
        elseif ($method=="registry"){
        	$url = $server;
        	if (strlen($reg_authority)>0){
        		$url .= "?authority=".$reg_authority;
        	} 
        	if (strlen($reg_label)>0){
        		if (strlen($reg_authority)>0){
        			$url .= "&label=".$reg_label;
        		}
        		else{
        			$url .= "?label=".$reg_label;
        		}
        	}
        	if (strlen($reg_type)>0){
        		if (strlen($reg_authority)>0 || strlen($reg_label)>0)	{
        			$url .= "&type=".$reg_type;
        		}
        		else{
        			$url .= "?type=".$reg_type;
        		}
        	}    		
        		
        }
        else	{
            error_log("Method not allowed: ".$method);
        }
        return $url;
    
    }
    
    /** 
    * Check parameter with given name (key) has a value other than ""
    */
    function checkParam($key, $value){
        if (!isset( $value) || strlen($value)==0){
            error_log("'" .$key. "' parameter not specified.");
        }
    }
	
	/**
	* sends error message to browser
	*/
	function throwException($exception) {
		echo("<exception>".$exception."</exception>\n");
	}

	/**
	* gets requested header, if available
	*/
	function getHeader($fp,$header){
		$meta_data = stream_get_meta_data($fp);
		foreach($meta_data['wrapper_data'] as $response){
			if(strpos(strtolower($response),$header)!==false){
				return substr($response,strpos($response,":")+1);
			}
		}
		return null;
	}
}
?>
