<!--
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
-->

<!-- Page to display a single Object view.This page view will display single CvObjects and BioSource Objects.

    @author Chris Lewington
    @version $Id$
-->

<%@ page language="java" %>

<%-- Intact classes needed --%>
<%@ page import="uk.ac.ebi.intact.model.CvTopic,
                 uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants,
                 java.util.Collection"
    %>
<%@ page import="uk.ac.ebi.intact.webapp.search.struts.view.beans.CvObjectViewBean" %>
<%@ page import="uk.ac.ebi.intact.webapp.search.struts.view.beans.AnnotationViewBean" %>
<%@ page import="uk.ac.ebi.intact.webapp.search.struts.view.beans.XrefViewBean" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>

<%
    CvObjectViewBean bean = (CvObjectViewBean) session.getAttribute(SearchConstants.VIEW_BEAN);
%>
<span class="smalltext"> </span>

<span class="smalltext">Search Results for <%= session.getAttribute(SearchConstants.SEARCH_CRITERIA) %> </span>
<br/>
<span class="verysmalltext">(short labels of search criteria matches are
    <span style="color: rgb(255, 0, 0);">highlighted</span>
</span><span class="verysmalltext">)<br></span></p>

<form name="viewForm">
   <!-- create a table with the  Cvobject deatils -->
   <table style="width: 100%; background-color: rgb(51, 102, 102);" cellpadding="2">
        <tbody>

            <!-- header row -->
             <tr bgcolor="white">

                <!-- main label --->
                <td class="headerdark">
                    <nobr>
                    <!-- set Help Link -->
                    <span class = "whiteheadertext">
                      <%=bean.getIntactType() %>
                    </span>
                    <a href="<%= bean.getHelpLink() + "cvs"%>"
                    target="new"><sup><b><font color="white">?</font></b></sup></a></nobr>
                </td>

                  <!-- ac -->
                <td class="headerdarkmid">
                 <nobr>   <a href="<%= bean.getHelpLink() + "BasicObject.ac"%>"
                        target="new" class="tdlink">Ac:</a> <%= bean.getObjAc() %> &nbsp; </nobr> 
                </td>

                 <!-- shortlabel -->
                <td class="headerdarkmid">
                    <a href="<%= bean.getHelpLink() + "AnnotatedObject.shortLabel"%>"
                        class="tdlink"
                        target="new">IntAct name:</a>
                    <b><span style="color: rgb(255, 0, 0);"><%= bean.getObjIntactName() %></span></b>
                </td>

<% // get all Xrefs
    // find out which size the table have to be
    Collection<XrefViewBean> someXrefBeans = bean.getXrefs();
    Collection<AnnotationViewBean> someAnnotations = bean.getFilteredAnnotations();
    // put in a extra field in the table
    if (someXrefBeans.size() != 0 || someAnnotations.size() != 0)
    { %>
                    <td class="headerdarkmid" colspan="2">
                         &nbsp;
                     </td>
   <% } %>
                </tr>
                 <tr bgcolor="white">
                    <!-- name label + help link   -->
                    <td class="headerdarkmid">
                            <a href="<%=bean.getHelpLink() + "AnnotatedObject.fullName" %>"class="tdlink">
                               Description
                            </a>
                   </td>
                    <!-- name of the xref -->
                    <td colspan="5"  class="data" >
                        <%= bean.getFullname() %>
                    </td>
                 </tr>


<!-- list all the anotations -->
<%
    // first get all annotations from the bean
    if( false == someAnnotations.isEmpty() )  {
    //get the first one and process it on its own - it seems we need it to put a search
    //link for it into the first cell of the row, then process the others as per
    //'usual'
    AnnotationViewBean firstAnnotation = someAnnotations.iterator().next();
%>
     <tr bgcolor="white">

         <td class="headerdarkmid" rowspan="<%= someAnnotations.size() %>">
                    <a href="<%= bean.getHelpLink() + "AnnotatedObject.shortLabel" %>" class="tdlink">
                    Annotation<br>
                    </a>
                </td>

        <%
        for(AnnotationViewBean anAnnotation : someAnnotations) {

            if( ! anAnnotation.equals( firstAnnotation ) ) {
            //we need to have new rows for each Annotations OTHER THAN the first..
        %>
               <tr bgcolor="white">
         <% } %>

                <td td class="data">
                    <a href="<%=bean.getSearchUrl(anAnnotation.getObject().getCvTopic()) %>" >
                    <%=anAnnotation.getName()%>
                 </td>
                      <!-- todo -->
                      <td td class="data" colspan="4" >

                        <%
                            //need to check for a 'url' annotation and hyperlink them if so...
                            if( anAnnotation.getObject().getCvTopic().getShortLabel().equals( CvTopic.URL ) ) {
                        %>
                              <a href="<%= anAnnotation.getText() %>" target="_blank"><%= anAnnotation.getText() %></a><br>
                        <%
                            } else {
                                    if( anAnnotation.getText() != null) {  %>
                                        <%= anAnnotation.getText() %><br>
                                <%   }
                                    else {
                                    %>
                                  - <br>
                                    <% }
                            }

                        %>

                     </td>
               </tr>
        <% } %>
  <% } %>

<!-- list all xref -->
<%
    if(!someXrefBeans.isEmpty())  {
        //get the first one and process it on its own - it seems we need it to put a search
        //link for it into the first cell of the row, then process the others as per
        //'usual'
        XrefViewBean firstXref = someXrefBeans.iterator().next();
%>

     <tr bgcolor="white">

           <td class="headerdarkmid" rowspan="<%= someXrefBeans.size() %>">
                <a href="<%= bean.getHelpLink() + "Xref.cvrefType" %>" class="tdlink">
                Xref
                </a>
            </td>
    <%  // now go on with all the other xrefs
        for(XrefViewBean aXref : someXrefBeans) {
%>

<%
       if(!aXref.equals(firstXref)) {
            //we need to have new rows for each Xref OTHER THAN the first..
%>
            <tr bgcolor="white">
<%
       }
%>
                  <!-- name of the xref -->
                  <td class="data">
                           <a href="<%= bean.getSearchUrl(aXref.getObject().getCvDatabase()) %>">
                           <%=aXref.getName()%>
                           </a>
                   </td>

                  <!-- primary id -->
                  <td class="data">
                        <% if(!aXref.getSearchUrl().equalsIgnoreCase("-")) { %>
                                <a href="<%=aXref.getSearchUrl() %>">
                           <% } %>
                            <%=aXref.getPrimaryId()%>
                     </td>

            <!-- ignore the secondary id if it we got no secondary id --> 
            <% if(!aXref.getSecondaryId().equalsIgnoreCase("-")) { %>
                     <td class="data">
                          <%=aXref.getSecondaryId()%>
                     </td>
            <% }    %>

                      <!-- type -->
                     <td class="data">
                        <a href="<%=bean.getHelpLink() + "Xref.cvrefType" %>" target="new">
                        <%=aXref.getType()%></a>
                        &nbsp;
                        <!-- if we got no XrefQualifierName it's not "linkable" -->
                        <% if(!aXref.getXrefQualifierName().equalsIgnoreCase("-")) { %>
                                <a href="<%=bean.getSearchUrl(aXref.getObject().getCvXrefQualifier())%>">
                         <% } %>
                        <%=aXref.getXrefQualifierName() %>
                        </a>
                     </td>
               </tr>

    <% } // for all Xrefs %>

  <% } // if any Xrefs %>

        </tbody>
    </table>
</form>