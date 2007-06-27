<%@ page language="java" %>

<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - This displays a bunch of radio button which allows the user to select the behaviour
   - of its click on the interaction network.
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

    if (user.InteractionNetworkReadyToBeDisplayed())
    {

        String applicationContext = request.getContextPath();
        String centerItem = "";
        String addItem = "";
        String radio = "<img src=\"" + applicationContext + "/images/select.gif\" border=\"0\">";
        String radioChk = "<img src=\"" + applicationContext + "/images/select-chk.gif\" border=\"0\">";

        long timestamp = System.currentTimeMillis();

        if (user.clickBehaviourIsAdd())
        {
            addItem = radioChk;
            centerItem = "<a href=\"" + applicationContext + "/clickBehaviour.do?action=center&timestamp=" + timestamp + "\" target=\"sidebarFrame\">" + radio + "</a>";
        }
        else
        {
            // default
            addItem = "<a href=\"" + applicationContext + "/clickBehaviour.do?action=add&timestamp=" + timestamp + "\" target=\"sidebarFrame\">" + radio + "</a>";
            centerItem = radioChk;
        }
%>

<hr>

<!-- click behaviour section -->

    <table width="100%">
        <tr>
            <td>
                <strong><bean:message key="sidebar.click.section.title"/><strong>
            </td>
        </tr>

        <tr>
            <td>
                <%= centerItem %> <bean:message key="sidebar.click.center.title"/>
                <intact:documentation section="hierarchView.PPIN.center" />
            </td>
        </tr>

        <tr>
            <td>
                <%= addItem %> <bean:message key="sidebar.click.add.title"/>
                <intact:documentation section="hierarchView.PPIN.add" />
            </td>
        </tr>
    </table>

<%
   } // if InteractionNetworkReady
%>