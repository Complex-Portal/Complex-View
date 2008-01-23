<%@ page import="uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<%--
  @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
  @version $Id
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<table border="0" cellspacing="0" cellpadding="3" width="100%" style="margin-width:0;">

    <tr>
        <!-- Displays the available highlightment source -->

        <%

            List<SourceBean> ListSources = ( ArrayList ) session.getAttribute( "sources" );
            List<SourceBean> tmpListSources = new ArrayList( ListSources.size() );

            int j = 0;
            for ( SourceBean sourceBean : ListSources ) {
                if ( sourceBean.getType().equalsIgnoreCase( "Species" ) ) {
                    tmpListSources.add( j, sourceBean );
                    j++;
                }
            }
            session.setAttribute( "tmpListSources", tmpListSources );
        %>


        <td valign="top">

            <!-- Species terms -->
            <display:table
                    name="sessionScope.tmpListSources" width="100%" class="tsources"
                    decorator="uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceDecorator">
                <display:column property="description" title="Organism" width="63%" align="left"/>
                <display:column property="directHighlightUrl" title="Show" width="8%" align="center"/>
                <display:column property="count" title="Count" width="9%" align="center"/>
                <display:column property="id" title="Taxid" width="20%" align="left"/>
                <display:setProperty name="basic.msg.empty_list"
                                     value="No source available for that interaction network"/>
            </display:table>

        </td>
    </tr>
</table>