<%@ page language="java" %>

<!--
- Copyright (c) 2002 The European Bioinformatics Institute, and others.
- All rights reserved. Please see the file LICENSE
- in the root directory of this distribution.
-
- Allows to forward to the main page of the application.
-
- @author Samuel Kerrien (skerrien@ebi.ac.uk)
- @version $Id$
-->

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>

<html:html>

    <head>
        <base target="_top"/>
    </head>

    <body bgcolor="white">
        <html:form action="/init">
            <html:hidden property="width"/>
            <html:hidden property="height"/>
            <html:hidden property="host"/>
            <html:hidden property="protocol"/>
        </html:form>
    </body>

    <script language="JavaScript" type="text/javascript">
        <!--
          
          var myWidth = 0, myHeight = 0;
          if( typeof( window.innerWidth ) == 'number' ) {
            //Non-IE
            myWidth = window.innerWidth;
            myHeight = window.innerHeight;
          } else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
            //IE 6+ in 'standards compliant mode'
            myWidth = document.documentElement.clientWidth;
            myHeight = document.documentElement.clientHeight;
          } else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
            //IE 4 compatible
            myWidth = document.body.clientWidth;
            myHeight = document.body.clientHeight;
          }

        document.forms[0].width.value = myWidth;
        document.forms[0].height.value = myHeight;

        // gives the current host and protocol from the browser URL.
        document.forms[0].host.value = window.location.host;
        document.forms[0].protocol.value = window.location.protocol;

        // submit the form automatically toward the entry page
        document.forms[0].submit();
        //-->
    </script>



</html:html>
