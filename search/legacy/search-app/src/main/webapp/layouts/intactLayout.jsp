<%@ page language="java"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<%--
    Intact default look & feel layout. It consists of a sidebar and a display
    area. The display area conatns the header, contents and the footer as
    shown below:
    +---------------------+
    | Organization header +
    |---------------------+
    | side | header       +
    | bar  | contents     +
    |      | footer       +
    |---------------------+

    Author: Sugath Mudali (smudali@ebi.ac.uk)
    Version: $Id$
--%>

<html:html>
<head>
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="-1">

    <!-- IntAct dynamic application should not be indexed by search engines -->
    <meta name='ROBOTS' content='NOINDEX'>

    <title><tiles:getAsString name="title"/></title>
    <!-- Don't put the base tag here; let the browser sort out the URLs -->
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/layouts/styles/intact.css"/>
    <link rel="stylesheet" type="text/css" href="http://www.ebi.ac.uk/inc/css/contents.css"/>
    <link rel="stylesheet" type="text/css" href="http://www.ebi.ac.uk/inc/css/userstyles.css"/>
    <link rel="stylesheet" type="text/css" href="http://www.ebi.ac.uk/inc/css/sidebars.css"/>




    <!-- Needed to setup Tooltip for buttonBar.html. -->
    <script type="text/javascript" src="<%= request.getContextPath()%>/layouts/overLIB/overlib.js"></script>

    <SCRIPT LANGUAGE="JavaScript">
    <!-- Original:  Corey (638@gohip.com ) -->
    <!-- Web Site:   http://six38.tripod.com -->
    <!-- This script and many more are available free online at -->
    <!-- The JavaScript Source!! http://javascript.internet.com -->
    <!-- Begin
    var start=new Date();
        start=Date.parse(start)/1000;
        var counts=60;
        function CountDown(){
            var now=new Date();
            now=Date.parse(now)/1000;
            var x=parseInt(counts-(now-start),10);
            if(document.form1){document.form1.clock.value = x;}
            if(x>0){
                timerID=setTimeout("CountDown()", 100)
            }else{
                location.href="http://localhost:8081/intact/search-app"
            }
        }
    //  End -->
    </script>
    <SCRIPT LANGUAGE="JavaScript">
    <!--
    window.setTimeout('CountDown()',100);
    -->
    </script>

</head>

<body bgcolor="#FFFFFF" topmargin="0" leftmargin="0">
<table border="0" height="100%" width="100%" cellspacing="5">

<tr>
    <%-- Sidebar section --%>
    <td bgcolor="#cccccc" width='113' valign='top' align='left'>
        <tiles:insert attribute="sidebar"/>
    </td>

    <td valign="top" height="100%" width="*">
        <table width="100%" height="100%">

                <%-- Application Header section --%>
            <tr>
                <td valign="top" height="5%">
                    <tiles:insert attribute="header"/>
                </td>

            </tr>
                <tr>
                    <td height="3" background="<%=request.getContextPath()%>/images/hor.gif"></td>
                </tr>
                <tr>
                        <%-- Banner that warns users that this site is to be phased out --%>
                    <td height="100" valign="top" align="center">
                        <table height="100">
                            <tr>
                                <td height="100" width="600" align="center">
                                        <%--<a id="important" name="important"/>--%>
                                    <div class="cssinfobox" style="border: 1px dotted rgb(227, 62, 62);">
                                        <span class="red_bold" style="font-size: 14px; text-align: center;">
                                            ** Important Notice About This Web Application **
                                        </span>
                                        <br/>
                                        <br/>
                                        This application is going to be phased out in June 2009.
                                        <br/>
                                        If you need to link to IntAct from your application, please read our
                                        <a href="http://www.ebi.ac.uk/intact">documentation</a>.
                                        <br/>
                                        Please visit our new portal by clicking <a href="http://www.ebi.ac.uk/intact">here</a>.
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </td>
            </tr>
            <%-- Content section --%>
            <tr>
                <td valign="top" height="*">
                    <!-- No errors if none specified -->
                    <tiles:insert attribute="content" ignore="true"/>
                </td>
            </tr>

            <%-- The footer --%>
            <tr>
                <td valign="bottom" height="10%">
                    <tiles:insert attribute="footer"/>
                </td>
            </tr>
        </table>
    </td>
</tr>
</table>
</body>
</html:html>
