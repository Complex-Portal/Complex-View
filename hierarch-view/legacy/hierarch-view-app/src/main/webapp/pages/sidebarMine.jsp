<%@ page language="java" %>

<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - This layout displays the PSI download button.
   - This is displayed only if the user has already requested the display
   - of an interaction network.
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

    // if a path was provided by MiNe a button is shown
    // to enable to clear the path highlighting of the edges
    if (user.getMinePath() != null)
    {
%>
<hr>
<form action="<%= request.getContextPath() %>/clearMinePath.do">
    <table width="100%">
        <tr>
          <th colspan="2">
             <div align="left">
                <strong><bean:message key="sidebar.mine.section.title"/></strong>
                <intact:documentation section="mine" />
             </div>
          </th>
        </tr>

        </tr>
            <td>
                <html:submit property="action" titleKey="sidebar.mine.button.submit.title">
                    <bean:message key="sidebar.mine.button.submit"/>
                </html:submit>
            </td>
        </tr>
    </table>
</form>
<%
    }
%>