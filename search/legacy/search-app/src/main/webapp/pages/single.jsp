<!--
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
-->

<!-- Page to display a single Object view.This page view will display single CvObjects and BioSource Objects.

    @author Michael Kleen
    @version $Id$
-->

<%@ page language="java" %>

<%-- Intact classes needed --%>
<%@ page import="uk.ac.ebi.intact.model.Xref,
                 uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants,
                 java.util.Iterator"
    %>
<%@ page import="uk.ac.ebi.intact.webapp.search.struts.view.beans.SingleViewBean" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>

<%
    SingleViewBean bean = (SingleViewBean) session.getAttribute(SearchConstants.VIEW_BEAN);
%>

<h3>Search Results for
    <%=session.getAttribute(SearchConstants.SEARCH_CRITERIA) %>
</h3>

<br>
<!-- show the searched object ac -->
<span class="smalltext">(short labels of search criteria matches are
    <span style="color: rgb(255, 0, 0);">highlighted</span>
</span><span class="smalltext">)<br></span></p>

<form name="viewForm">
   <!-- create a table with the object deatils -->
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
                    <a href="<%= bean.getHelpLink() + "search.TableLayout"%>"
                    target="new"><sup><b><font color="white">?</font></b></sup></a></nobr>
                </td>

                <!-- ac -->
                <td class="headerdarkmid">
                    <a href="<%= bean.getHelpLink() + "BasicObject.ac"%>"
                        target="new" class="tdlink">Ac:</a> <%= bean.getObjAc() %>
                </td>

                 <!-- shortlabel -->
                <td class="headerdarkmid">
                    <a href="<%= bean.getHelpLink() + "AnnotatedObject.shortLabel"%>"
                        class="tdlink"
                        target="new"><span style="color: rgb(102, 102, 204);">IntAct </span>name:</a>
                    <a href="<%= bean.getObjSearchURL() %>" class="tdlink" style="font-weight: bold;">
                    <b><span style="color: rgb(255, 0, 0);"><%= bean.getObjIntactName() %></span></b></a>
                </td>


                <td  rowspan="2" class="headernew">
                        <%= bean.getFullname() %>
                    </span>
                </td>
                </tr>
                <tr>
                    <td class="headerdarkmid"  colspan="3">
                         &nbsp;
                    </td>
                </tr>

<!-- list all the anotations -->
<%
    // first get all annotations from the bean
    for(Iterator it = bean.getAnnotations().iterator(); it.hasNext();) {
       SingleViewBean anAnnotation = (SingleViewBean) it.next();
%>

                <tr bgcolor="white">
                      <td class="headerdarkmid">
                        <span style="color: rgb(102, 102, 204);">
                            <a href="<%=anAnnotation.getSearchUrl() %>" class="tdlink" style="font-weight: bold;">
                                <b><%=anAnnotation.getObjIntactName() %>
                                 </span>
                                </b>
                            </a>
                      </td>
                      <td td class="data" colspan="5" >
                         <%=anAnnotation.getFullname()%>
                     </td>
               </tr>
<% } %>

<!-- list all xref -->
<%  // get aall Xrefs from the beans
    // not very nice but its working should be done by the wrapped object not in the jsp
    for(Iterator it = bean.getObject().getXrefs().iterator(); it.hasNext();) {
       Xref aXrefs = (Xref) it.next();
%>
                <tr bgcolor="white">
                    <td class="data">

                            <a href="<%=bean.getSearchUrl(aXrefs.getCvDatabase()) %>">
                            <%=aXrefs.getCvDatabase().getShortLabel()%>

                    </td>

                     <td class="data">
                        <span style="color: rgb(102, 102, 204);">
                            <a href="<%=aXrefs.getPrimaryId().toString() %>">
                            <%=aXrefs.getPrimaryId().toString() %>
                        </span>
                    </td>

                     <td class="data">
                            <%=aXrefs.getSecondaryId().toString() %>
                    </td>


                    <td class="data">
                          <a href="<%=bean.getHelpLink() + "Xref.cvrefType" %>" target="new"/>
                            Type:
                          </a>
                           &nbsp;
                          <a href="<%=bean.getSearchUrl(aXrefs.getCvXrefQualifier())%>">
                              <%=aXrefs.getCvXrefQualifier().getShortLabel() %>
                          </a>

                    </td>

                    <td>
                        <a href="/interpro/internal-tools/intacttest/displayDoc.jsp?section=Xref.cvXrefType" target="new"/>
                        Type:</a>
<a href="/interpro/internal-tools/intacttest/search/do/search?searchString=EBI-300005&amp;searchClass=CvXrefQualifier">
see-also</a>
</td>


               </tr>
    <% } %>

        </tbody>
    </table>
</form>