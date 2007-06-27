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

<%@ page import="org.apache.taglibs.standard.lang.jpath.encoding.HtmlEncoder,
                 uk.ac.ebi.intact.application.hierarchview.business.Constants,
                 uk.ac.ebi.intact.application.hierarchview.business.IntactUserI,
                 uk.ac.ebi.intact.context.IntactContext,
                 uk.ac.ebi.intact.util.simplegraph.BasicGraphI,
                 java.util.Iterator"%>

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
%>

<%
   if (user.InteractionNetworkReadyToBeDisplayed()) {
       String graph2mif = user.GRAPH2MIF_PROPERTIES.getProperty( "graph2mif.url" );
       StringBuffer ac = new StringBuffer( 32 );
       Iterator iterator = user.getInteractionNetwork().getCentralProteins().iterator();
       while ( iterator.hasNext() ) {
           BasicGraphI interactor = (BasicGraphI) iterator.next();
           
           ac.append( interactor.getAc() ).append( "%2C" ); // %2C <=> ,
       }
       int l = ac.length();

       if (l > 0)
           ac.delete( l-3, l ); // the 3 last caracters (%2C)
       if (l == 0)
           ac.append(user.getQueryString());

       String url = graph2mif  + "?ac=" +  ac.toString()
                               + "&depth=" + user.getCurrentDepth()
                               + "&strict=false";
       String url10 = url+"&version=1";
       String url25 = url+"&version="+ HtmlEncoder.encode("2.5");
%>

<hr>

    <table width="100%">
        <tr>
          <th colspan="2">
             <div align="left">
                <strong><bean:message key="sidebar.psi.section.title"/></strong>
                <intact:documentation section="hierarchView.PPIN.download" />
             </div>
          </th>
        </tr>

        <tr>
            <td valign="bottom" align="center">
                    <nobr>
                        <img border="0" src="<%= request.getContextPath() %>/images/psi10.png"
                                alt="PSI-MI 1.0 Download"
                                onclick="w=window.open('<%= url10 %>', 'graph2mif');w.focus();">
                    </nobr>
             </td>
        </tr>
        <tr>
            <td valign="bottom" align="center">
                    <nobr>
                        <img border="0" src="<%= request.getContextPath() %>/images/psi25.png"
                             alt="PSI-MI 2.5 Download"
                             onclick="w=window.open('<%= url25 %>', 'graph2mif');w.focus();">
                    </nobr>
             </td>

        </tr>
    </table>

<%
   } // if InteractionNetworkReady
%>