<%@ page language="java" %>

<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - This layout displays the search box for hierarchView.
   -
   - @author Samuel Kerrien (skerrien@ebi.ac.uk)
   - @version $Id$
-->

<%@ page import="uk.ac.ebi.intact.application.hierarchview.business.Constants,
                 uk.ac.ebi.intact.application.hierarchview.business.IntactUserI,
                 uk.ac.ebi.intact.application.hierarchview.struts.view.utils.LabelValueBean,
                 uk.ac.ebi.intact.application.hierarchview.struts.view.utils.OptionGenerator,
                 uk.ac.ebi.intact.context.IntactContext"%>
<%@ page import="javax.servlet.jsp.PageContext" %>
<%@ page import="java.util.ArrayList" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://ebi.ac.uk/intact/commons"      prefix="intact"%>

<%

    IntactUserI user = null;

    user = (IntactUserI) IntactContext.getCurrentInstance().getSession().getAttribute(Constants.USER_KEY);

    /**
     * Retreive user's data from the session
     */
    String queryString = null;
    String methodLabel = null;
    String fieldMethod = null;

    if (user != null)
    {
        queryString = user.getQueryString();
        methodLabel = user.getMethodLabel();
        fieldMethod = (null == methodLabel ? "" : methodLabel);
    }
    else
    {
        queryString = "";
        fieldMethod = "";
    }

%>

<!-- Search section -->
<html:form action="/search" focus="queryString">

    <table>
        <tr>
          <th>
             <div align="left">
                <strong><bean:message key="sidebar.search.section.title"/></strong>
                <intact:documentation section="hierarchView.display" />
             </div>
          </th>
        </tr>

        <tr>
            <td>
                   <html:text property="queryString" value="<%= queryString %>" size="12"/>
            </td>

        </tr>

		    <%
                /**
                 * get a collection of highlightment sources.
                 */
                ArrayList sources = OptionGenerator.getHighlightmentSources ();

                // by default, all available source are selected ("All")
                if (sources.size() > 1) {
                    // set the item collection and display it
                    pageContext.setAttribute("sources", sources, PageContext.PAGE_SCOPE);

               %>

                    <html:hidden property="method" value="All" />

               <%
                    /*

                    <table border=0>
                      <tr>
                         <td align="right">
                           <h4> highlight sources </h4>
                         </td>
                      </tr>
                      <tr>
                         <td>
                            <html:select property="method" size="1" value="= fieldMethod">
                              <html:options collection="sources" property="value" labelProperty="< % =label % >" />
                            </html:select>
                         </td>
                      </tr>
                    </table>

                    */
                }
                else if (sources.size() == 1) {
                    LabelValueBean lvb = (LabelValueBean) sources.get(0);
                    String methodClassName = lvb.getValue();
            %>
                    <!-- dont't display the method if no choice available -->
                    <html:hidden property="method" value="<%= methodClassName %>" />
            <%
                }
            %>

        <tr>
            <td>
                <html:submit property="action" titleKey="sidebar.search.button.submit.title">
                    <bean:message key="sidebar.search.button.submit" />
                </html:submit>
            <%
                if (user != null && user.InteractionNetworkReadyToBeDisplayed()) {
                    // display the Add button only if the user as already a network
            %>
                <html:submit property="action" titleKey="sidebar.search.button.add.title">
                    <bean:message key="sidebar.search.button.add" />
                </html:submit>
            <%
                }
            %>
            </td>
        </tr>
    </table>

</html:form>
