<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="eng">
<head>

<!-- BEGIN EBI CODE -->
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />  
<meta name="author" content="EBI Web Team" />
<meta http-equiv="Content-Language" content="en-GB" />
<meta http-equiv="Window-target" content="_top" />
<meta name="no-email-collection" content="http://www.unspam.com/noemailcollection/" />
<meta name="generator" content="Dreamweaver 8" />
<!-- END EBI CODE -->

<title>Dasty2, an AJAX protein DAS client</title>

<!-- BEGIN EBI CODE -->
<link rel="stylesheet"  href="http://www.ebi.ac.uk/inc/css/contents.css"     type="text/css" />
<link rel="stylesheet"  href="http://www.ebi.ac.uk/inc/css/userstyles.css"   type="text/css" />
<script  src="http://www.ebi.ac.uk/inc/js/contents.js" type="text/javascript"></script>
<link rel="SHORTCUT ICON" href="http://www.ebi.ac.uk/bookmark.ico" />
<style type="text/css">
@media print { 
	body, .contents, .header, .contentsarea, .head { 
		position: relative;
	}  
} 
</style>
<!-- END EBI CODE -->

<? include("dasty2_css.php"); ?>
<? include("dasty2_javascript.php"); ?>

<style type="text/css">
<!--
.maintitle
	{
	background:url(img/ebi_bg.gif);
	background-repeat:repeat-y;
	}
.gr_row_02 { background:#f2f7f7; }

.feature_table_row_decor1{ background:#f2f7f7; vertical-align:top;}

-->
</style>


</head>

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
Optionally define the URL parametres. Makes Dasty2 independent from the URL.
<body onload="set_query('P05067', 'BioSapiens', 3, ''), start_dasty(0);">
 - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
 
<body onload="start_dasty(1);">

<!-- BEGIN EBI CODE -->
	<div class="headerdiv" id="headerdiv" style="position:absolute; z-index: 1;">
		<iframe src="/inc/head.html" name="head" id="head" frameborder="0" marginwidth="0px" marginheight="0px" scrolling="no"  width="100%" style="position:absolute; z-index: 1; height: 57px;">Your browser does not support inline frames or is currently configured not to display inline frames. Content can be viewed at actual source page: http://www.ebi.ac.uk/inc/head.html</iframe>
	</div>
    
	<div class="general">
<!-- END EBI CODE -->

		<? include("ebi_body.php"); ?>

<!-- BEGIN EBI CODE -->
	</div>

  <table class="footerpane" id="footerpane" summary="The main footer pane of the page">
				<tr>
				  <td colspan ="4" class="footerrow">
					<div class="footerdiv" id="footerdiv"  style="z-index:2;">

						<iframe src="/inc/foot.html" name="foot" frameborder="0" marginwidth="0px" marginheight="0px" scrolling="no"  height="22px" width="100%"  style="z-index:2;">Your browser does not support inline frames or is currently configured not to display inline frames. Content can be viewed at actual source page: http://www.ebi.ac.uk/inc/foot.html</iframe>
					</div>
				  </td>
				</tr>
  </table>
	  <script  src="http://www.ebi.ac.uk/inc/js/footer.js" type="text/javascript"></script>
<!-- END EBI CODE -->

</body>
</html>
