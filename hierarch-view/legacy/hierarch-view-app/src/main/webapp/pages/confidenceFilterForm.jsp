<%@ page import="uk.ac.ebi.intact.application.hierarchview.business.Constants" %>
<%@ page import="uk.ac.ebi.intact.application.hierarchview.business.IntactUserI" %>
<%@ page import="uk.ac.ebi.intact.application.hierarchview.struts.view.utils.Compare" %>
<%@ page import="uk.ac.ebi.intact.application.hierarchview.struts.view.utils.ConfidenceFilter" %>
<%@ page import="uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean" %>
<%@ page import="uk.ac.ebi.intact.util.SearchReplace" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://ebi.ac.uk/intact/commons" prefix="intact" %>
<%--
  Created by IntelliJ IDEA.
  User: iarmean
  Date: 28-Feb-2008
  Time: 17:10:36
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%

    IntactUserI user = null;

    user = ( IntactUserI ) session.getAttribute( Constants.USER_KEY );

    /**
     * Retreive user's data from the session
     */
    String confRelation = "";
    if ( user != null ) {
        ConfidenceFilter confFilter = user.getConfidenceFilterValues();
        if ( confFilter != null ) {
            confRelation = confFilter.getRelation();
        }
    }
%>
<html:form
        action="/filter"><!-- name="filterForm" type="uk.ac.ebi.intact.application.hierarchview.struts.view.FilterForm" focus="queryString">-->

    <table width="100%" border="1">
        <tr>
            <td width="10%">
                <html:radio property="method" value="FIRST_METHOD"/>
            </td>
            <td width="60%">
                <html:text property="confidenceValue" size="2" maxlength="4"/>

                <html:select property="relation">
                    <%
                        String[] relation = {">=", ">", "<=", "<", "="};
                        for ( int s = 0; s < relation.length; s++ ) {%>
                    <option
                            <%if ( relation[s].equalsIgnoreCase( confRelation ) ) { %>
                            selected>
                        <%} else { %>
                        >
                        <%}%>
                        <%=relation[s]%>
                    </option>
                    <% } %>
                </html:select>
                <html:messages id="message" message="true">
                    <%-- If the filter is false, it prevent bean:write to convert HTML to text --%>
                    <li><bean:write name="message" filter="false"/></li>
                </html:messages>
            </td>
        </tr>
        <tr>
            <td width="10%">
                <html:radio property="method" value="SECOND_METHOD"/>
            </td>
            <td width="60%">
                <table>
                    <tr>
                        <td> between
                            <html:text property="minConfidenceValue" size="2" maxlength="4"/>
                            and
                            <html:text property="maxConfidenceValue" size="2" maxlength="4"/></td>
                    </tr>
                    <tr>
                        <td>
                            <html:radio property="clusivity" value="INCLUSIVE"/>inclusive
                            <html:radio property="clusivity" value="EXCLUSIVE"/>exclusive
                        </td>
                    </tr>
                </table>

            </td>
        </tr>
        <tr>
            <td></td>
            <td>
                <html:submit property="action">
                    <bean:message key="result.highlight.source.submit.button"/>
                </html:submit>
            </td>
        </tr>

    </table>

</html:form>