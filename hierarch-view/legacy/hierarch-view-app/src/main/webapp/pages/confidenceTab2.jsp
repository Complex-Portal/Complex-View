<%@ page import="uk.ac.ebi.intact.application.hierarchview.struts.view.utils.Compare" %>
<%@ page import="uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean" %>
<%@ page import="uk.ac.ebi.intact.util.SearchReplace" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%--
  Created by IntelliJ IDEA.
  User: iarmean
  Date: 18-Feb-2008
  Time: 10:01:41
  To change this template use File | Settings | File Templates.
--%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<table border="0" cellspacing="0" cellpadding="3" width="100%" style="margin-width:0;">
<tr>
    <!-- Displays the available highlightment source -->

    <%
        List<SourceBean> ListSources = ( ArrayList ) session.getAttribute( "sources" );
//        System.out.println( "size sources: " + ListSources.size() );
        List<SourceBean> groups = null;
        SourceBean sb = ListSources.get( 0 );
        String termType = "Confidence";

        String applicationPath = sb.getApplicationPath();
        String sourceBrowseGraphUrl = sb.getSourceBrowserGraphUrl();
        String directHighlightUrl = applicationPath
                                    + "/source.do?keys=${selected-terms}&clicked=${id}&type=${type}";

        directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${type}", termType );

        String id1 = "[0.0 - 0.25";
        String id2 = "[0.25 - 0.50";
        String id3 = "[0.50 - 0.75";
        String id4 = "[0.75 - 1";

        String directHighlightUrl1 = SearchReplace.replace( directHighlightUrl, "${id}", id1 );
        String directHighlightUrl2 = SearchReplace.replace( directHighlightUrl, "${id}", id2 );
        String directHighlightUrl3 = SearchReplace.replace( directHighlightUrl, "${id}", id3 );
        String directHighlightUrl4 = SearchReplace.replace( directHighlightUrl, "${id}", id4 );
        SourceBean sb1 = new SourceBean( id1, "Confidence", "very low confidence", 0, null, sourceBrowseGraphUrl, directHighlightUrl1, false, applicationPath );
        SourceBean sb2 = new SourceBean( id2, "Confidence", "low confidence", 0, null, sourceBrowseGraphUrl, directHighlightUrl2, false, applicationPath );
        SourceBean sb3 = new SourceBean( id3, "Confidence", "high confidence", 0, null, sourceBrowseGraphUrl, directHighlightUrl3, false, applicationPath );
        SourceBean sb4 = new SourceBean( id4, "Confidence", "very high confidence", 0, null, sourceBrowseGraphUrl, directHighlightUrl4, false, applicationPath );
        groups = Arrays.asList( sb1, sb2, sb3, sb4 );

        directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${selected-terms}", "null" );
        String selectedKeys = "";
        for ( SourceBean sourceBean : ListSources ) {
            if ( sourceBean.getType().equals( "Confidence" ) ) {
                Double d = Double.valueOf( sourceBean.getId() );
                int classIndex = -1;
                if ( d < 0.25 ) {
                    classIndex = 0;
                } else if ( d >= 0.25 && d < 0.50 ) {
                    classIndex = 1;
                } else if ( d >= 0.50 && d < 0.75 ) {
                    classIndex = 2;
                } else if ( d >= 0.75 ) {
                    classIndex = 3;
                }

                if ( classIndex != -1 ) {
                    if ( sourceBean.isSelected() ) {
                        groups.get( classIndex ).setSelected( true );
                        String id = groups.get( classIndex ).getId();
                        if ( !selectedKeys.contains( id ) ) {
                            selectedKeys += "," + id;
                        }
                    }
                    groups.get( classIndex ).setCount( groups.get( classIndex ).getCount() + sourceBean.getCount() );
                }
            }
        }

        for ( int i = 0; i < groups.size(); i++ ) {
            String directUrl = "null";
            if ( selectedKeys != "" ) {
                directUrl = SearchReplace.replace( groups.get( i ).getDirectHighlightUrl(), "${selected-terms}", selectedKeys.substring( 1 ) );
            } else {
                directUrl = SearchReplace.replace( groups.get( i ).getDirectHighlightUrl(), "${selected-terms}", "null" );
            }
            groups.get( i ).setDirectHighlightUrl( directUrl );
        }
        session.setAttribute( "confidenceGroupsSources", groups );
    %>

    <jsp:include page="confidenceFilterForm.jsp"/>
</tr>
<tr>
    <td valign="top">

        <!-- Confidence terms -->
        <display:table
                name="sessionScope.confidenceGroupsSources" width="100%" class="tsources"
                decorator="uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceDecorator">
            <display:column property="id" title="Category" width="63%" align="left"/>
            <display:column property="directHighlightUrl" title="Show" width="8%" align="center"/>
            <display:column property="count" title="Count" width="9%" align="center"/>
            <display:setProperty name="basic.msg.empty_list" value="No source available for that interaction network"/>
        </display:table>

    </td>
</tr>
</table>