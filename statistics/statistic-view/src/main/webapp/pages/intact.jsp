<%@ page import="uk.ac.ebi.intact.application.statisticView.struts.view.IntactStatisticsBean,
                 uk.ac.ebi.intact.application.statisticView.struts.view.DisplayStatisticsBean,
                 java.util.ArrayList,
                 java.util.List"%>

<%@ page language="java"  %>
<%@ page buffer="none"    %>
<%@ page autoFlush="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/display.tld"     prefix="display" %>

<%@ taglib uri="/WEB-INF/tld/intact.tld"      prefix="intact" %>

<%
    IntactStatisticsBean intactBean = (IntactStatisticsBean) request.getAttribute("intactbean");

    if( intactBean == null ) {
        // TODO forward to an error page
    }

    List stats = intactBean.getDisplayBeans();
    request.setAttribute("statistics", stats);
%>

<a name="top">

<table width="100%" height="100%">
    <tbody>
        <tr>
            <td valign="top" height="*">
            <!-- Displays the available highlightment source -->
                <display:table name="statistics" width="90%"
                               decorator="uk.ac.ebi.intact.application.statisticView.graphic.TableDecorator">
                    <display:column property="statObject" title="Object" width="31%" styleClass="tableCellAlignRight" />
                    <display:column property="count"                     width="7%"  styleClass="tableCellAlignRight" />
                    <display:column property="description"               width="63%" />

                    <display:setProperty name="basic.msg.empty_list"     value="No statistics available..." />
                </display:table>
            </td>
        </tr>
        <tr>
            <td valign="top" height="*">

                <table border="0">
                   <tr>
                      <td>
                            <a name="<%= IntactStatisticsBean.PROTEINS %>">
                            <img src="<%= intactBean.getProteinChartUrl() %>" width=600 height=400 border=0 >
                            <br><br>
                      </td>
                      <td valign="top">
                         <a href="#top"><img src="../images/top.gif" border="0"></a>
                      </td>
                   </tr>
                </table>

                <table border="0"e>
                   <tr>
                      <td>
                          <a name="<%= IntactStatisticsBean.INTERACTIONS %>">
                          <img src="<%= intactBean.getInteractionChartUrl() %>" width=600 height=400 border=0  >
                          <br><br>
                     </td>
                      <td valign="top">
                         <a href="#top"><img src="../images/top.gif" border="0"></a>
                      </td>
                   </tr>
                </table>

                <table border="0">
                   <tr>
                      <td>
                          <a name="<%= IntactStatisticsBean.BINARY_INTERACTIONS %>">
                          <img src="<%= intactBean.getBinaryChartUrl() %>" width=600 height=400 border=0 >
                          <br><br>
                     </td>
                      <td valign="top">
                         <a href="#top"><img src="../images/top.gif" border="0"></a>
                      </td>
                   </tr>
                </table>

                <table border="0">
                   <tr>
                      <td>
                          <a name="<%= IntactStatisticsBean.EXPERIMENTS %>">
                          <img src="<%= intactBean.getExperimentChartUrl() %>" width=600 height=400 border=0>
                          <br><br>
                     </td>
                      <td valign="top">
                         <a href="#top"><img src="../images/top.gif" border="0"></a>
                      </td>
                   </tr>
                </table>

                <table border="0">
                   <tr>
                      <td>
                          <a name="<%= IntactStatisticsBean.CV_TERMS %>">
                          <img src="<%= intactBean.getCvTermChartUrl() %>" width=600 height=400 border=0 >
                          <br><br>
                     </td>
                      <td valign="top">
                         <a href="#top"><img src="../images/top.gif" border="0"></a>
                      </td>
                   </tr>
                </table>

                <table border="0">
                   <tr>
                      <td>
                          <a name="<%= IntactStatisticsBean.INTERACTIONS_PER_BIOSOURCE %>">
                          <img src="<%=intactBean.getBioSourceChartUrl() %>" width=600 height=400 border=0 >
                          <br>
                          <b>Note</b>: This figure is static. It does not change when you select a specific time period.
                          <br><br>
                     </td>
                      <td valign="top">
                         <a href="#top"><img src="../images/top.gif" border="0"></a>
                      </td>
                   </tr>
                </table>

                <table border="0">
                   <tr>
                      <td>
                          <a name="<%= IntactStatisticsBean.INTERACTIONS_PER_IDENTIFICATION %>">
                          <img src="<%= intactBean.getDetectionChartUrl()  %>" width=600 height=400 border=0 >
                          <br>
                          <b>Note</b>: This figure is static. It does not change when you select a specific time period.
                          <br><br>
                     </td>
                      <td valign="top">
                         <a href="#top"><img src="../images/top.gif" border="0"></a>
                      </td>
                   </tr>
                </table>

            </td>
        </tr>
    </tbody>
</table>













