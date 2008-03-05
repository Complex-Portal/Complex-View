<?
/**
 * pdb loader: load  pdb file from disk and send it to the client
 * author: Fernando Martinez <fernando@softlech.com>
 */
session_start();
// send a pdb file to the client
//error_log("in pdbloader:".$_SESSION['pdb']);
if(isset($_SESSION['pdb']))
{
	$url = $_SESSION['pdb'];
}else
	$url = "pdb/1cmw.pdb";
if(file_exists($url)){
	$contents = @file_get_contents($url);
	if(isset($contents))
		echo($contents);
}
?>