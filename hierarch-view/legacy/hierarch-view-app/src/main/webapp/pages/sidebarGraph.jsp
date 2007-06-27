<%@ page language="java" %>

<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - This layout displays graph management components for hierarchView.
   - According to the current state of the displayed graph : its depth,
   - we display button in order to get expanded or contracted.
   - We show only available options (e.g. if the depth can be desacrease
   - we don't show the contract button).
   -
   - @author Samuel Kerrien (skerrien@ebi.ac.uk)
   - @version $Id$
-->

<%@ page import="uk.ac.ebi.intact.application.hierarchview.business.Constants,
                 uk.ac.ebi.intact.application.hierarchview.business.IntactUserI"%>
<%@ page import="uk.ac.ebi.intact.context.IntactContext" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://ebi.ac.uk/intact/commons"      prefix="intact"%>

<%
    /**
     * Retreive user's data from the session
     */
    IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession().getAttribute(Constants.USER_KEY);

    if (user == null)
    {
        // no user in the session, don't display anything
        return;
    }
%>

<%
   if (user.InteractionNetworkReadyToBeDisplayed()) {
%>

<hr>

<!-- Graph section -->
<html:form action="/interactionNetwork">

    <table width="100%">
        <tr>
          <th colspan="2">
             <div align="left">
                <strong><bean:message key="sidebar.graph.section.title"/></strong>
                <intact:documentation section="hierarchView.PPIN.expand" />
             </div>
          </th>
        </tr>

        <tr>
            <td width="50%">
                <%
                    if (user.maximalDepthReached() == false) {
                %>
                    <html:submit property="action" titleKey="sidebar.graph.button.expand.title">
                        <bean:message key="sidebar.graph.button.expand"/>
                    </html:submit>
                <%
                    }
                %>
            </td>

            <td width="50%">
                <%
                    if (user.minimalDepthReached() == false) {
                %>
                    <html:submit property="action" titleKey="sidebar.graph.button.contract.title">
                        <bean:message key="sidebar.graph.button.contract"/>
                    </html:submit>
                <%
                    }
                %>

            </td>
        </tr>
    </table>

</html:form>

<%
   } // if InteractionNetworkReady
%>