<%@ page import="uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean,
                 java.util.ArrayList"%>
<%@ page language="java" %>

<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>


<table border="0" cellspacing="0" cellpadding="3" width="100%" style="margin-width:0px;">

    <tr>
        <!-- Displays the available highlightment source -->

        <%
            ArrayList ListSources = (ArrayList) session.getAttribute("sources");
            ArrayList tmpListSources = new ArrayList (ListSources.size());

            int j=0;
            for (int i=0;i<ListSources.size();i++) {
               SourceBean b = (SourceBean) ListSources.get(i);
               if ( b.getType().equals("Go") ) {
                   tmpListSources.add(j,ListSources.get(i));
                   j++;
                }
            }
            session.setAttribute("tmpListSources",tmpListSources);
        %>

            <td valign="top">

                <!-- GO terms -->
                <display:table
                    name="sessionScope.tmpListSources" width="100%" class="tsources"
                    decorator="uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceDecorator">
                    <display:column property="description"        title="Description" width="55%" align="left" />
                    <display:column property="directHighlightUrl" title="Show"        width="8%"  align="center" />
                    <display:column property="graph"              title="Browse"      width="8%"  align="center" />
                    <display:column property="count"              title="Count"       width="9%"  align="center" />
                    <display:column property="id"                 title="ID"          width="20%" align="left" />
                    <display:setProperty name="basic.msg.empty_list"
                                            value="No source available for that interaction network" />
                </display:table>

            </td>
      </tr>
</table>