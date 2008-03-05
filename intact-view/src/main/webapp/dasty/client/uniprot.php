<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="eng">
<head>

<title>Dasty2, an AJAX protein DAS client</title>

<!-- BEGIN UNIPROT CSS -->
<link href="http://beta.uniprot.org/base.css" rel="stylesheet" type="text/css">
<!-- END UNIPROT CSS -->

<? include("dasty2_css.php"); ?>
<? include("dasty2_javascript.php"); ?>

<style type="text/css">
<!--
.style1 {
	font-family: sans-serif;
	font-size: small;
	font-weight:normal;
}
.maintitle
	{
	background:url(img/uniprot_bg.gif);
	background-repeat:repeat-y;
	}
-->
</style>

</head>

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
Optionally define the URL parametres. Makes Dasty2 independent from the URL.
<body onload="set_query('P05067', 'BioSapiens', 3, ''), start_dasty(0);">
 - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
 
<body onload="start_dasty(1);">

<!-- BEGIN UNIPROT HEADER -->
<table id="header">
  <tbody>
    <tr>
      <td id="logo"><a accesskey="1" href="http://beta.uniprot.org/"><img alt="" src="http://beta.uniprot.org/images/logo_small.gif" /></a></td>
      <td valign="bottom"><table border="0" cellpadding="1" cellspacing="0">
        <tr>
          <td bgcolor="#55729B"><a href="http://www.ebi.ac.uk/dasty/"><img src="img/icon2_dasty.gif" width="14" height="14" /></a></td>
          <td>Dasty2</td>
        </tr>
      </table>
      </td>
      <td id="menu"><a class="style1" href="http://beta.uniprot.org/downloads/">Downloads</a> &middot; <a class="style1" accesskey="9" href="http://beta.uniprot.org/contact/">Contact</a> &middot; <a class="style1" rel="help" href="http://beta.uniprot.org/help/"><strong>Help</strong></a></td>
    </tr>
  </tbody>
</table>
<!-- END UNIPROT HEADER -->

		<? include("uniprot_body.php"); ?>

</body>
</html>
