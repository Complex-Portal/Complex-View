<%--
  Created by IntelliJ IDEA.
  User: maitesin
  Date: 05/12/13
  Time: 11:11
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- paulirish.com/2008/conditional-stylesheets-vs-css-hacks-answer-neither/ -->
<!--[if lt IE 7]> <html class="no-js ie6 oldie" lang="en"> <![endif]-->
<!--[if IE 7]>    <html class="no-js ie7 oldie" lang="en"> <![endif]-->
<!--[if IE 8]>    <html class="no-js ie8 oldie" lang="en"> <![endif]-->
<!-- Consider adding an manifest.appcache: h5bp.com/d/Offline -->
<!--[if gt IE 8]><!--> <html class="no-js" lang="en"> <!--<![endif]-->
<head>
    <meta charset="utf-8">

    <!-- Use the .htaccess and remove these lines to avoid edge case issues.
         More info: h5bp.com/b/378 -->
    <!-- <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"> --> <!-- Not yet implemented -->

    <%--<title>[page-title] &lt; [service-name] &lt; EMBL-EBI</title>--%>
    <title>${page_title} &lt; ${complex_portal_name} &lt; EMBL-EBI</title> <%--Added by Oscar--%>
    <meta name="description" content="EMBL-EBI"><!-- Describe what this page is about -->
    <meta name="keywords" content="bioinformatics, europe, institute"><!-- A few keywords that relate to the content of THIS PAGE (not the whol project) -->
    <meta name="author" content="EMBL-EBI"><!-- Your [project-name] here -->

    <!-- Mobile viewport optimized: j.mp/bplateviewport -->
    <meta name="viewport" content="width=device-width,initial-scale=1">

    <!-- Place favicon.ico and apple-touch-icon.png in the root directory: mathiasbynens.be/notes/touch-icons -->

    <!-- CSS: implied media=all -->
    <!-- CSS concatenated and minified via ant build script-->
    <link rel="stylesheet" href="//www.ebi.ac.uk/web_guidelines/css/compliance/develop/boilerplate-style.css">
    <link rel="stylesheet" href="//www.ebi.ac.uk/web_guidelines/css/compliance/develop/ebi-global.css" type="text/css" media="screen">
    <link rel="stylesheet" href="//www.ebi.ac.uk/web_guidelines/css/compliance/develop/ebi-visual.css" type="text/css" media="screen">
    <link rel="stylesheet" href="//www.ebi.ac.uk/web_guidelines/css/compliance/develop/984-24-col-fluid.css" type="text/css" media="screen">

    <!-- you can replace this with [projectname]-colours.css. See http://www.ebi.ac.uk/web/style/colour for details of how to do this -->
    <!-- also inform ES so we can host your colour palette file -->
    <link rel="stylesheet" href="//www.ebi.ac.uk/web_guidelines/css/compliance/develop/embl-petrol-colours.css" type="text/css" media="screen">

    <!-- for production the above can be replaced with -->
    <!--
    <link rel="stylesheet" href="//www.ebi.ac.uk/web_guidelines/css/compliance/mini/ebi-fluid-embl.css">
    -->

    <style type="text/css">
            /* You have the option of setting a maximum width for your page, and making sure everything is centered */
            /* body { max-width: 1600px; margin: 0 auto; } */
    </style>
    <link href="<%=request.getContextPath()%>/resources/css/main.css" rel="stylesheet"> <%--Added by Oscar--%>
    <!-- end CSS-->
    <%--Begin JS--%>
    <script type="text/javascript" charset="utf-8" src="//code.jquery.com/jquery-1.10.2.js"></script> <%--Added by Oscar--%>
    <script type="text/javascript" charset="utf-8" src="<%=request.getContextPath()%>/resources/js/main.js"></script> <%--Added by Oscar--%>

    <!-- All JavaScript at the bottom, except for Modernizr / Respond.
         Modernizr enables HTML5 elements & feature detects; Respond is a polyfill for min/max-width CSS3 Media Queries
         For optimal performance, use a custom Modernizr build: www.modernizr.com/download/ -->

    <!-- Full build -->
    <!-- <script src="//www.ebi.ac.uk/web_guidelines/js/libs/modernizr.minified.2.1.6.js"></script> -->

    <!-- custom build (lacks most of the "advanced" HTML5 support -->
    <script src="//www.ebi.ac.uk/web_guidelines/js/libs/modernizr.custom.49274.js"></script>
</head>
<body class="level2"><!-- add any of your classes or IDs -->
<div id="skip-to">
    <ul>
        <li><a href="#content">Skip to main content</a></li>
        <li><a href="#local-nav">Skip to local navigation</a></li>
        <li><a href="#global-nav">Skip to EBI global navigation menu</a></li>
        <li><a href="#global-nav-expanded">Skip to expanded EBI global navigation menu (includes all sub-sections)</a></li>
    </ul>
</div>

<div id="wrapper" class="container_24">
    <header>
        <div id="global-masthead" class="masthead grid_24">
            <!--This has to be one line and no newline characters-->
            <a href="//www.ebi.ac.uk/" title="Go to the EMBL-EBI homepage"><img src="//www.ebi.ac.uk/web_guidelines/images/logos/EMBL-EBI/EMBL_EBI_Logo_white.png" alt="EMBL European Bioinformatics Institute"></a>

            <nav>
                <ul id="global-nav">
                    <!-- set active class as appropriate -->
                    <li class="first active" id="services"><a href="//www.ebi.ac.uk/services">Services</a></li>
                    <li id="research"><a href="//www.ebi.ac.uk/research">Research</a></li>
                    <li id="training"><a href="//www.ebi.ac.uk/training">Training</a></li>
                    <li id="industry"><a href="//www.ebi.ac.uk/industry">Industry</a></li>
                    <li id="about" class="last"><a href="//www.ebi.ac.uk/about">About us</a></li>
                </ul>
            </nav>

        </div>
