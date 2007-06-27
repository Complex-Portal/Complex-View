<%@ page language="java" %>

<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>


<table border="0" cellspacing="0" cellpadding="3" width="100%" style="margin-width:0px;">

    <!-- Displays the available highlightment source -->
    <tr>
        <td valign="top">

        <!-- all available source terms -->        
        <display:table
            name="sessionScope.sources" width="100%" class="tsources"
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