<%@ page import="uk.ac.ebi.intact.application.hierarchview.highlightment.source.node.MoleculeTypeHighlightmentSource,
                 uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page language="java" %>

<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>


<%--
  Created by IntelliJ IDEA.
  User: nneuhaus
  Date: 17-Dec-2007
  Time: 16:53:14
--%>

<table border="0" cellspacing="0" cellpadding="3" width="100%">

    <tr>
        <!-- Displays the available highlightment source -->

        <%
            List<SourceBean> ListSources = ( ArrayList ) session.getAttribute( "sources" );
            List<SourceBean> tmpListSources = new ArrayList( ListSources.size() );

            int j = 0;
            for ( SourceBean source : ListSources ) {
                if ( source.getType().equalsIgnoreCase( MoleculeTypeHighlightmentSource.SOURCE_KEY ) ) {
                    tmpListSources.add( j, source );
                    j++;
                }
            }
            session.setAttribute( "tmpListSources", tmpListSources );
        %>

        <td valign="top">

            <!-- Type terms -->
            <display:table
                    name="sessionScope.tmpListSources" width="100%" class="tsources"
                    decorator="uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceDecorator">
                <display:column property="description" title="Molecule Type" width="63%" align="left"/>
                <display:column property="directHighlightUrl" title="Show" width="8%" align="center"/>
                <display:column property="count" title="Count" width="9%" align="center"/>
                <display:column property="id" title="MI-Ref" width="20%" align="left"/>
                <display:setProperty name="basic.msg.empty_list"
                                     value="No source available for that interaction network"/>
            </display:table>

        </td>
    </tr>
</table>