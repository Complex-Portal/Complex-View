<?
/** DAS proxy. PHP version.
*   Proxies requests to DAS Registry and DAS servers. Query parameters:
*   s   Server URL
*   m   Method (one of: sequence, features, types, stylesheet, registry)
*   q   DAS segment (only required for sequence and features methods)
*   t   HTTP timeout (default is 5 seconds)
*
* Author    Fernando Martinez <fernando@softlech.com>
*/
session_start();
include_once("DasProxy.php");
set_time_limit(300); // modify script execution time (in secs) as needed. Set to 0 for limitless
// All responses, including error messages, must be valid XML
header('Content-type: text/xml');
$url = DasProxy::getUrl();
$timeout = DasProxy::getTimeout();
echo(DasProxy::getResponse($url, $timeout));
?>