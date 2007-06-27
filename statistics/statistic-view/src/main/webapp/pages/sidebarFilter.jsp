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

<%@ page import="java.text.SimpleDateFormat,
                 java.util.GregorianCalendar,
                 java.util.Calendar,
                 java.util.ArrayList,
                 uk.ac.ebi.intact.application.statisticView.struts.view.FilterBean,
                 uk.ac.ebi.intact.application.statisticView.business.util.Constants,
                 uk.ac.ebi.intact.application.statisticView.business.model.IntactStatistics,
                 java.sql.Timestamp,
                 java.sql.Date,
                 org.apache.log4j.Logger,
                 uk.ac.ebi.intact.application.statisticView.business.data.StatisticHelper"%>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>

<%
    Log logger = LogFactory.getLog("uk.ac.ebi.intact.statisticView.page.sidebarFilter.jsp");
    StatisticHelper helper = null;
    Date eldest = null;
    try
    {
        helper = new StatisticHelper();
        Timestamp timestamp = helper.getFirstTimestamp();
        eldest = new Date(timestamp.getTime());
        logger.info("Oldest date found: " + eldest);
    }
    catch (Exception nde)
    {
        logger.error("Error while trying to get the first timestamp.", nde);
    }

    /**
     * create the links
     */
    SimpleDateFormat dateFormater = helper.dateFormater;
    Calendar calendar = GregorianCalendar.getInstance();
    ArrayList filters = new ArrayList();
    filters.add(new FilterBean("-", ""));

    int i = 1;
    while (calendar.getTime().after(eldest))
    {
        calendar.add(Calendar.MONTH, -1); // previous month
        String iMonthAgo = dateFormater.format(calendar.getTime());
        filters.add(new FilterBean("" + i, iMonthAgo));
        i++;
    }


    pageContext.setAttribute("filters", filters);
%>

    <table width="100%">
        <tr>
          <th colspan="2">
             <div align="left">
                <strong><bean:message  key="sidebar.filter.section.title1"/></strong>
             </div>
          </th>
        </tr>

        <tr>
            <td>

            <html:form action="/statistics">
               <table>
                  <tr>
                     <td>
                       Last
                       <html:select property="start">
                          <html:options collection="filters" property="value" labelProperty="label"/>
                       </html:select>
                       month.
                    </td>
                 </tr>
                 <tr>
                   <td align="right">

                       <html:submit property="action" titleKey="filter.button.submit.title">
                            <bean:message  key="filter.button.submit.title"/>
                       </html:submit>
                 </td>
               </tr>
               </table>
            </html:form>

              <hr>

                <b><bean:message  key="sidebar.filter.section.title2"/></b><br>

                   <%
                      String start = request.getParameter( "start" );
                      String stop  = request.getParameter( "stop" );

                      String startValue = (start == null ? "" : start);
                      String stopValue  = (stop  == null ? "" : stop);
                   %>


               <html:form action="/statistics">

               <table>
                  <tr>
                     <td valign="top" align="right">
                        <bean:message  key="filter.label.from"/>
                     </td>
                     <td>
                        <html:text property="start" value="<%= startValue %>" size="12" maxlength="11" />
                        <br>
                        <small><font color="#898989">(02-Feb-2004)</font></small>
                     </td>
                  </tr>

                  <tr>
                    <td valign="top" align="right">
                       <bean:message  key="filter.label.to"/>
                    </td>
                    <td>
                       <html:text property="stop" value="<%= stopValue %>" size="12" maxlength="11" />
                       <br>
                       <small><font color="#898989">(02-Feb-2004)</font></small>
                    </td>
                  </tr>
                  <tr>
                    <td colspan="2" align="right">
                       <html:submit property="action" titleKey="filter.button.submit.title">
                            <bean:message  key="filter.button.submit.title"/>
                       </html:submit>
                    </td>
                  </tr>
                </table>
               </html:form>

            </td>
        </tr>
    </table>

<hr>