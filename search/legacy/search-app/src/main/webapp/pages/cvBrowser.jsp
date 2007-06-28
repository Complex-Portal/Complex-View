<%@ page import="uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.Constants"%>
<%@ page import="uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.business.graphdraw.ImageBean" %>
<html><head>
<%@ taglib uri="http://ebi.ac.uk/intact/advanced-search" prefix="advancedSearch" %>

<html:html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

    <%
        // retreive the Bean that contains the CV name.
        ImageBean imageBean = (ImageBean) session.getServletContext().getAttribute(Constants.IMAGE_BEAN);

        // get the given field name from the request
        String fieldName = (String) request.getParameter("field");
    %>

    <script language="javascript" type="text/javascript">
     // Function used to update the calling form.
     // Once a term has been clicked, the field is filled with the shortlabel.
     //
     // param: the shortlabel of the selected term.
     function SendInfo( selectedTerm ){

         var options = window.opener.document.advancedForm["<%= fieldName %>"];

         // Loop over the list of term and select the right one.
         for(var i = 0; i < options.length; i++) {
            var option = options[i];
            if(option.value == selectedTerm) {
                option.selected = true;
                break;
            }
         }
     } // sendInfo
    </script>

    <link href="/intact/search/layouts/styles/intact.css" rel="stylesheet" type="text/css">

    <title><%= imageBean.getCvName() %></title>

</head>
<body>
    <table width="100%">
        <tr>
             <td> <span class="header">IntAct - CvBrowser</span> </td>
        </tr>
        <tr>
            <td height="3" background="/intact/search/images/hor.gif"></td>
        </tr>
    </table>

    <form name="formPop">
    <input type='button' onclick='javascript:window.close();' name="button" value="CLOSE" />

        <!--
          --  Displays the cvDag graph if the picture
          --  has been generated and stored in the response.
          -->
        <advancedSearch:displayCvDag/>

    <br><input type='button' onclick='javascript:window.close();' name="button" value="CLOSE" />
    </form>
</body>
</html:html>