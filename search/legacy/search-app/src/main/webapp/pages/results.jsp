 <%--
    Search results page.

    @author Chris Lewington and Sugath Mudali
    @version $Id$
--%>

<%@ page language="java"  %>
<%@ page buffer="none"    %>
<%@ page autoFlush="true" %>

<%@ page import="uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants,
                 java.util.StringTokenizer"%>
 <%@ page import="uk.ac.ebi.intact.webapp.search.SearchWebappContext" %>
 <%@ page import="uk.ac.ebi.intact.webapp.search.struts.view.beans.AbstractViewBean" %>

 <!-- Import util classes -->

 <%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>


<%
    SearchWebappContext webappContext = SearchWebappContext.getCurrentInstance();

    //build the URL for hierarchView from the absolute path and the relative beans..
    String hvPath = webappContext.getHierarchViewAbsoluteUrl();
    String minePath = webappContext.getMineAbsoluteUrl();
%>

<script language="JavaScript" type="text/javascript">

    // Returns true if a checkbox has been checked.
    function checkAC(form, msg) {
        for (var i = 0; i < form.elements.length; i++) {
            // Only interested in 'checkbox' fields.
            if (form.elements[i].type == "checkbox") {
                // Only porcess if they are checked.
                if (form.elements[i].checked) {
                    return true;
                }
            }
        }
        window.alert(msg);
        return false;
    }

    // This is a global variable to setup a window.
    var newWindow;

    // Create a new window if it hasnt' created before and bring it to the
    // front if it is focusable.
    function makeNewWindow(link) {
        if (!newWindow || newWindow.closed) {
            newWindow = window.open(link, "hvWindow");
        }
        else if (newWindow.focus) {
            newWindow.location.href = link;
            newWindow.focus();
        }
    }

    // Will be invoked when user selects graph button. An AC must be selected.
    // This in trun will create a new widow and invoke hierarchView application
    // in the new window.
    function writeToWindow(form, msg) {
        // An AC must have been selected.
        if (!checkAC(form, msg)) {
            return;
        }
        var ac;
        for (var i = 0; i < form.elements.length; i++) {
            // Only interested in 'checkbox' fields.
            if (form.elements[i].type == "checkbox") {
                // Only process if they are checked.
                //TODO: This should handle multiple checkboxes....
                if (form.elements[i].checked) {
                    var name = form.elements[i].name;
                    // Remove the table identifer from name to get ac.
                    if(ac == null) {
                        ac = name;
                    }
                    else {
                        ac = ac + "," + name;
                    }
                }
            }
        }
        var link = "<%=hvPath%>"
            + "?AC=" + ac + "&depth=" + <%=webappContext.getHierarchViewDepth()%>
            + "&method=" + "<%=webappContext.getHierarchViewMethod()%>";
        //window.alert(link);
        makeNewWindow(link);
    }

    // Checks all the check boxes.
    function checkAll() {
        var form = document.viewForm;
        for (var i = 0; i < form.elements.length; i++) {
            // Only interested in 'checkbox' fields.
            if (form.elements[i].type == "checkbox") {
                // Only check it if it isn't already checked.
                if (!form.elements[i].checked) {
                    form.elements[i].checked = true;
                }
            }
        }
    }

    // Clears all the check boxes.
    function clearAll(form) {
        var form = document.viewForm;
        for (var i = 0; i < form.elements.length; i++) {
            // Only interested in 'checkbox' fields.
            if (form.elements[i].type == "checkbox") {
                // Only clear it if it is checked.
                if (form.elements[i].checked) {
                    form.elements[i].checked = false;
                }
            }
        }
    }
</script>

<span class="smalltext">Search Results for

    <%
        String params = (String) session.getAttribute(SearchConstants.SEARCH_CRITERIA);

        if( params.length() > 30 ) {

            // split the params and display 10 per lines.
            StringTokenizer st = new StringTokenizer( params, "," );
            int count = 0;
            while( st.hasMoreTokens() ) {
                out.write( st.nextToken() );
                out.write( ',' );
                count++;
                if( (count % 10) == 0 ) {
                    out.write( "<br>" );
                }
            }

        } else {
            out.write( params );
        }

    %>

</span>

    <br>
<span class="verysmalltext">(short labels of search criteria matches are
    <span style="color: rgb(255, 0, 0);">highlighted</span>
</span><span class="verysmalltext">)<br></span></p>

<!-- a line to separate the header -->
<hr size=2>

<form name="viewForm">

<!-- Get the view Bean and dump its data to the web page-->
<%
    AbstractViewBean bean = (AbstractViewBean) session.getAttribute(SearchConstants.VIEW_BEAN);

    if (bean == null)
    {
        out.write("nothing found in the session.");
    }
    else
    {

        if (bean.showGraphButtons())
        {
            //display links - none to display for single object views.
%>
                <html:link href='javascript:checkAll()'>
                    Check All
                </html:link>
                <html:link href='javascript:clearAll()'>
                    Clear All
                </html:link>
<%
         }

        String helpLink = webappContext.getHelpLink();

%>
        <html:link href="<%=helpLink %>" target="new">
            Help
        </html:link>
        <hr size=2>

<%
        // Displays the content of the view.
        bean.getHTML( out );
%>

    <hr size=2>

        <!-- The footer table. -->
        <table cellpadding="1" cellspacing="0" border="1" width="100%">
            <tr>

            <%
                if(bean.showGraphButtons()) {
                    //display buttons - none to display for single object views..
            %>
                <td align="center">
                    <input type="button" name="action" value="Graph"
                        onclick="writeToWindow( this.form, 'Please select an AC to display the graph')">
                    <input type="reset" value="Reset">
                </td>
           <%
              }
           %>
            </tr>
        </table>
<%
     } // end if bean != null
%>
</form>
