<!-- detail page (detail.jsp)-->
<%--
    Page to display a 'detail' view of search results. Current specification is as per the
    mockups August 2004. Details for both Experiment and Interaction  results are displayed using
    the page format defined in this JSP. As such the view beans used may wrap either Experiments
    or Interactions, however the view displayed will be the same. This is because Interaction
    results are always displayed in the context of their Experiments.

    Note that this page will not be accessed directly - an initial search will have the results
    displayed in the new 'simple' JSP view, and so this 'detail' view will be used when a link
    is clicked from that 'simple' results page to view more detail of either an Experiment
    or an Interaction. In the case of Interactions this may mean that a number of tables
    are displayed on this page since (at least in theory) an Interaction may be related to more
    than a single Experiment.

    Furthermore there are a number of Experiments (for example Giot) which have such a large
    number of Interactions that they cannot be displayed on a single page. Thus for such Experiments
    this 'detail' JSP is also responsible for displaying the results in a tabbed view - although
    the actual content for each tab will be provided from the view bean itself.
--%>

<%-- need to provide an error page here to catch unhandled failures --%>
<%@ page language="java" %>
<%@ page buffer="none" %>
<%@ page autoFlush="true" %>

<%-- Intact classes needed --%>
<%@ page import="uk.ac.ebi.intact.model.*,
                 uk.ac.ebi.intact.model.util.CvObjectUtils,
                 uk.ac.ebi.intact.model.util.RoleInfo,
                 uk.ac.ebi.intact.persistence.dao.query.impl.SearchableQuery,
                 uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants,
                 uk.ac.ebi.intact.webapp.search.struts.view.beans.MainDetailView" %>
<%@ page import="uk.ac.ebi.intact.webapp.search.struts.view.beans.MainDetailViewBean" %>
<%@ page import="java.util.*" %>

<%-- Standard Java classes --%>

<%-- may make use of these later to tidy up the JSP a little --%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%
    SearchWebappContext webappContext = SearchWebappContext.getCurrentInstance();

    //build the URL for hierarchView from the absolute path and the relative beans..
    String hvPath = webappContext.getHierarchViewAbsoluteUrl();
    String minePath = webappContext.getMineAbsoluteUrl();

    //the list of shortlabels for the search matches - need to be highlighted
    //NB the SearchAction ensures (in most cases!) this will not be null
    List highlightList = (List) request.getAttribute(SearchConstants.HIGHLIGHT_LABELS_LIST);
    if (highlightList == null)
    {
        highlightList = new ArrayList();  //avoids null checks everywhere!
    }
%>

<%-- The javascript for the button bars.... --%>
<%@ include file="jscript.html" %>

<!-- top line info -->
<%--    <span class="middletext">Search Results for <%=session.getAttribute( SearchConstants.SEARCH_CRITERIA )%> <br></span>--%>

<span class="smalltext">Search Results for

    <%
        SearchableQuery query = SearchWebappContext.getCurrentInstance().getCurrentSearchQuery();
        String params = query.toString();

        if (params.length() > 30)
        {

            // split the params and display 10 per lines.
            StringTokenizer st = new StringTokenizer(params, ",");
            int count = 0;
            while (st.hasMoreTokens())
            {
                out.write(st.nextToken());
                out.write(',');
                count++;
                if ((count % 10) == 0)
                {
                    out.write("<br>");
                }
            }

        }
        else
        {
            out.write(params);
        }

    %>

</span>

<br/>

    <span class="verysmalltext">(short labels of search criteria matches are
        <span style="color: rgb(255, 0, 0);">highlighted</span>
    </span><span class="verysmalltext">)<br></span>
<span class="verysmalltext"><br></span>

<%

    //The View object containing the beans to render
    MainDetailView detailView = (MainDetailView) request.getAttribute(SearchConstants.VIEW_BEAN);

    //first check to see if the bean list is null - if it is then it means that the
    //request is not a new one, but rather a tabbed page request. In this case get the
    //previously saved bean from the session and use that for the rest of this display....
    /*
    if ( viewBeanList == null ) {
        viewBeanList = new ArrayList();
        MainDetailViewBean existingBean = (MainDetailViewBean) session.getAttribute( SearchConstants.LARGE_EXPERIMENT_BEAN );
        viewBeanList.add( existingBean );
        //need to get its shortlabel for highlighting
        //(this comes from the request in all cases EXCEPT a tabbed view)
        highlightList.add( existingBean.getObjIntactName() );
    }
     */
    MainDetailViewBean bean = detailView.getMainDetailViewBean();

    request.setAttribute("currentAc", bean.getObjAc());
%>

<!-- the main form for the page -->
<form name="viewForm">

<%-- button bar for the table --%>
<%@ include file="buttonBar.html" %>


<%
    if (detailView.getTotalItems() > detailView.getItemsPerPage())
    {
%>
<%-- table pagination --%>
<%@include file="tablePagination.jspf"%>

<%
    }
    else
    {
%>
    <br/>
<%
    }
%>

<!-- main results tables -->
<%--<table style="width: 100%; background-color: rgb(241, 245, 248);"--%>
<table style="background-color: rgb(241, 245, 248);"
       border="1" cellpadding="5" cellspacing="0" bordercolor="#4b9996">

<tbody>

<!-- Experiment header row -->
<tr>

    <!-- 'Experiment' title cell plus checkbox -->
    <!-- <td width="10%" rowspan="2" class="headerdark"> -->
    <td rowspan="2" class="headerdark">
        <table width="100%" border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td valign="top">
                    <nobr><span class="whiteheadertext">Experiment</span>
                        <a href="<%= bean.getHelpLink() + "Experiment"%>" target="new" class="whitelink"><sup>
                            ?</sup></a>

                    </nobr>
                </td>
            </tr>
            <tr>
                <td valign="bottom" align="center">
                    <nobr>
                        <% if ( bean.hasPsi1URL() ) { %>
                        <a href="<%= bean.getPsi1Url() %>"><img src="<%= request.getContextPath() %>/images/psi10.png"
                                                                alt="PSI-MI 1.0 Download"
                                                                onmouseover="return overlib('Download data from publication in PSI-MI XML 1.0', DELAY, 150, TEXTCOLOR, '#FFFFFF', FGCOLOR, '#EA8323', BGCOLOR, '#FFFFFF');"
                                                                onmouseout="return nd();"></a>
                        <% } %>
                        <% if ( bean.hasPsi25URL() ) { %>
                        <a href="<%= bean.getPsi25Url() %>"><img src="<%= request.getContextPath() %>/images/psi25.png"
                                                                 alt="PSI-MI 2.5 Download"
                                                                 onmouseover="return overlib('Download data from publication in PSI-MI XML 2.5', DELAY, 150, TEXTCOLOR, '#FFFFFF', FGCOLOR, '#EA8323', BGCOLOR, '#FFFFFF');"
                                                                 onmouseout="return nd();"></a>
                        <% } %>
                    </nobr>
                </td>
            </tr>
        </table>
    </td>

    <!-- 'name' title, linked to help -->
    <%-- <td width="10%" nowrap="nowrap" class="headerdarkmid"> --%>
    <td nowrap="nowrap" class="headerdarkmid">
        <a href="<%= bean.getHelpLink() + "AnnotatedObject.shortLabel"%>" target="new"
           class="tdlink">Name
        </a>
    </td>

    <!-- 'ac' title, linked to help -->
    <%-- <td width="10%" nowrap="nowrap" class="headerdarkmid"> --%>
    <td nowrap="nowrap" class="headerdarkmid">
        <a href="<%= bean.getHelpLink() + "BasicObject.ac"%>" target="new"
           class="tdlink">Ac
        </a>
    </td>

    <!-- 'identification' title, linked to help -->
    <%-- <td width="20%" nowrap="nowrap" class="headerdarkmid"> --%>
    <td nowrap="nowrap" class="headerdarkmid">
        <a href="<%= bean.getHelpLink() + "CVINTERACTION_HELP_SECTION"%>" target="new"
           class="tdlink">Interaction detection
        </a>
    </td>

    <!-- 'participant' title, linked to help -->
    <td rowspan="1" colspan="2" nowrap="nowrap" class="headerdarkmid">
        <a href="<%= bean.getHelpLink() + "CVIDENT_HELP_SECTION"%>" target="new"
           class="tdlink"><nobr>Participant identification</nobr>
        </a>
    </td>

    <!-- 'host' title, linked to help -->
    <td colspan="4" nowrap="nowrap" class="headerdarkmid">
        <a href="<%= bean.getHelpLink() + "Interactor.bioSource"%>" target="new"
           class="tdlink">Host
        </a>
    </td>
</tr>

<!-- Experiment first data row -->
<tr>
    <%-- <td width="10%" class="lefttop"> --%>
    <td nowrap="nowrap" class="lefttop">
        <%
            if ( highlightList.contains( bean.getObjIntactName() ) ) {
        %>
        <b><span style="color: rgb(255, 0, 0);"><%= bean.getObjIntactName()%></span></b>
        <%
        } else {
            //no highlighting
        %>
        <%= bean.getObjIntactName()%>
        <%
            }
        %>
    </td>

    <%-- <td width="10%" class="lefttop"><%= bean.getObjAc()%></td> --%>
    <td nowrap="nowrap" class="lefttop"><%= bean.getObjAc()%></td>

    <!-- linked to the CvInteraction search -->
    <%-- <td width="20%" class="lefttop"> --%>
    <td class="lefttop">
        <a href="<%= bean.getCvInteractionSearchURL() %>">
            <%= bean.getObject().getCvInteraction().getShortLabel() %>
        </a>
    </td>

    <!-- linked to CvIdentification search -->
    <td style="vertical-align: top;" rowspan="1" colspan="2">
        <%
            if (bean.getObject().getCvIdentification() != null) {
        %>
        <a href="<%= bean.getCvIdentificationSearchURL() %>">
            <%= bean.getObject().getCvIdentification().getShortLabel() %>
        </a>
        <%  } else { %>
           -
        <% } %>
    </td>


    <% if ( bean.getExperimentBioSourceName().equalsIgnoreCase( "-" ) ) { %>
    <!-- linked to BioSource search -->
    <td colspan="5" class="lefttop">
        <nobr><%= bean.getExperimentBioSourceName() %></nobr>
    </td>
    <% } else { %>
    <td colspan="4" class="lefttop">
        <a href="<%= bean.getBioSourceSearchURL() %>">
            <nobr><%= bean.getExperimentBioSourceName() %></nobr>
        </a>
    </td>
    <% } %>
</tr>

<!-- Experiment Description -->
<tr>

    <!-- 'Description' title, linked to help -->
    <td class="headerdarkmid" style="font-weight: bold;">
        <a href="<%= bean.getHelpLink() + "AnnotatedObject.fullName" %>" class="tdlink">
            Description
        </a>
    </td>

    <!-- the description itself -->
    <td colspan="9" class="lefttop"><%= bean.getObjDescription() %></td>
</tr>

<!-- Experiment Annotation row  -->
<%
    Collection annotations = bean.getFilteredAnnotations();
    Collection xrefs = bean.getXrefs();
    int rowCount = 0;

    if (annotations.size() > 0)
    {
%>
<tr>

    <!-- 'Annotation' title cell (linked to help) -->
    <%-- <td width="10%" class="headerdarkmid" rowspan="<%= annotations.size() %>" colspan="1"> --%>
    <td class="headerdarkmid" rowspan="<%= annotations.size() %>" colspan="1">
        <a href="<%= bean.getHelpLink() + "AnnotatedObject.Annotation" %>" class="tdlink">
            Annotation<br>
        </a>
    </td>

    <%-- for now do the simplest thing - display all of the Annotation details in turn...
         (NB grouping individuals together requires a lot more logic!!)
         NOTE: First Annotation has to go on the same row as the title cell..
    --%>

    <%

        rowCount = 0;
        for(Iterator iter = annotations.iterator(); iter.hasNext();) {
            Annotation annot = (Annotation)iter.next();
            rowCount++;
            if(rowCount > 1) {
                //need a new <tr> tag for all beans except the first one..

    %>
    <tr>
        <%
            }
        %>

        <!-- annotation 'topic' title cell -->
        <td style="vertical-align: top;">
            <a href="<%= bean.getCvTopicSearchURL(annot)%>" style="font-weight: bold;">
                <%= annot.getCvTopic().getShortLabel()%></a><br>
        </td>

        <!-- annotation text cell -->
        <td class="data" style="vertical-align: top;" rowspan="1" colspan="9">
            <%
                //need to check for a 'url' annotation and hyperlink them if so...
                if ( annot.getCvTopic().getShortLabel().equals( CvTopic.URL ) ) {
            %>
            <a href="<%= annot.getAnnotationText() %>" target="_blank"><%= annot.getAnnotationText() %></a><br>
            <%
            } else {
                if ( annot.getAnnotationText() != null ) { %>
            <%= annot.getAnnotationText() %><br>
            <% } else {
            %>
            - <br>
            <% }
            }

            %>
        </td>

    </tr>
    <%

            }   //end of annotations loop
        }   //end of annotations size check

    %>


    <!-- Xref information -->
    <%

        if(xrefs.size() > 0) {

    %>
<tr>

<!-- 'Xref' title cell - linked to help -->
<%-- <td width="10%" class="headerdarkmid" rowspan="<%= xrefs.size()%>" colspan="1" --%>
<td class="headerdarkmid" rowspan="<%= xrefs.size()%>" colspan="1"
    style="text-align: justify;">
    <a href="<%= bean.getHelpLink() + "AnnotatedObject.Xref"%>" class="tdlink">
        Xref<br>
    </a>
</td>

<%


    rowCount = 0;   //reset rowcount and reuse it
    for(Iterator iter = xrefs.iterator(); iter.hasNext();) {
        Xref xref = (Xref)iter.next();
        rowCount++;
        if(rowCount > 1) {
            //need a new <tr> tag for all beans except the first one..

%>
<tr>
    <%
        }
    %>

    <!-- link to the Xref CvDatabase details -->
    <%-- (I think - example is pubmed) --%>
    <%-- <td width="10%" class="lefttop" colspan="1"> --%>
    <td class="lefttop" colspan="1">
        <a href="<%= bean.getCvDbURL(xref) %>" class="tdlink">
            <%= xref.getCvDatabase().getShortLabel() %>
        </a>
    </td>

    <!-- actual search link to the Xref-->
    <%-- ie the real URL filled with the ID - NB if it is null we can't write a link --%>

    <%-- <td width="10%" class="lefttop"> --%>
    <td class="lefttop">
        <%
            String idUrl = bean.getPrimaryIdURL( xref );
            if ( idUrl != null ) {
        %>
        <a href="<%= idUrl %>"><%= xref.getPrimaryId() %></a>
        <%
            } else {
                out.write( xref.getPrimaryId() );
            }
        %>
    </td>

    <!-- The Xref secondaryID, or a dash if there is none -->
    <td colspan="1" class="lefttop">
        <%= ( xref.getSecondaryId() != null ) ? xref.getSecondaryId() : "-" %>
    </td>

    <%-- CvXrefQualifier, linked to search for CV --%>
    <td style="vertical-align: top;" rowspan="1" colspan="6">
        <%
            CvXrefQualifier cvXrefQualifier = xref.getCvXrefQualifier();
            if (cvXrefQualifier != null)
            {
        %>
        <a href="<%= bean.getHelpLink() + "AnnotatedObject.Xref"%>" target="new">
            Type:</a>
        &nbsp;
        <a href="<%= bean.getCvQualifierURL(cvXrefQualifier)%>">
            <%= xref.getCvXrefQualifier().getShortLabel() %></a><br>

        <%   } else {
                out.write("-");
             } //cvXrefQualifier != null  %>
    </td>
</tr>

<%

        }   //end the Xref loop
    }   //end of xrefs count check

%>


<!-- Interaction details start here... -->
<%

    Collection interactions = bean.getInteractions();
    for(Iterator iter = interactions.iterator(); iter.hasNext();) {
        Interaction interaction = (Interaction)iter.next();

%>

<!-- first row -->
<tr>

    <!-- first cell - title plus checkbox -->
    <%-- <td width="10%" rowspan="2" class="headermid"> --%>
    <td rowspan="2" class="headermid">
        <nobr><input name="<%= interaction.getAc() %>" type="checkbox" class="text">
            <span class="whiteheadertext">Interaction</span>
            <a href="<%= bean.getHelpLink() + "Interaction"%>" target="new" class="whitelink"><sup>?</sup></a>
        </nobr>
    </td>

    <!-- 'name' title cell, linked to help -->
    <%-- <td width="10%" class="headerlight"> --%>
    <td class="headerlight">
        <a href="<%= bean.getHelpLink() + "AnnotatedObject.shortLabel"%>" target="new" class="tdlink">
            Name
        </a>
    </td>

    <!-- 'ac' title cell, linked to help -->
    <%-- <td width="10%" class="headerlight"> --%>
    <td class="headerlight">
        <a href="<%= bean.getHelpLink() + "BasicObject.ac" %>"
           target="new" class="tdlink">
            Ac
        </a>
    </td>

    <!--'interaction type' header cell, linked to help -->
    <%-- <td width="20%" class="headerlight"> --%>
    <td class="headerlight">
        <a href="<%= bean.getHelpLink() + "Interaction.CvInteractionType" %>"
           target="new" class="tdlink">
            Interaction type
        </a>
    </td>

    <!-- 'dissociation constant' header cell, linked to help -->
    <%-- ** NB: make sure the text is 'Kd' and not 'kD' ** --%>
    <%-- <td style="vertical-align: top;" rowspan="1" colspan="4">  --%>
    <td style="vertical-align: top;" class="headerlight" rowspan="1" colspan="6">
        <a href="<%= bean.getHelpLink() + "Interaction.kD"%>"
           target="new" class="tdlink">
            Dissociation constant (Kd) M</a><br>
    </td>

</tr>

<!-- first Interaction data row (relates to the above headers) -->
<tr>

    <!-- shortlabel -->
    <%-- <td width="10%" class="lefttop"> --%>
    <td nowrap="nowrap" class="lefttop">
        <%
            if ( highlightList.contains( interaction.getShortLabel() ) ) {
        %>
        <b><span style="color: rgb(255, 0, 0);"><%= interaction.getShortLabel()%></span></b>
        <%
        } else {
            //no highlighting
        %>
        <%= interaction.getShortLabel() %>
        <%
            }
        %>

    </td>

    <!-- ac -->
    <%-- <td width="10%" class="lefttop"><%= interaction.getAc() %></td> --%>
    <td nowrap="nowrap" class="lefttop"><%= interaction.getAc() %></td>

    <!-- interaction type, linked to CvInteractionType -->
    <%-- <td width="20%" class="lefttop"> --%>
    <td class="lefttop">
        <%
            //the CvInteractionType may be null - check for it...
            if ( interaction.getCvInteractionType() == null ) {
                out.write( "-" );
            } else {
        %>
        <a href="<%= bean.getCvInteractionTypeSearchURL(interaction)%>">
            <%= interaction.getCvInteractionType().getShortLabel() %>
        </a>
        <%
            }
        %>
    </td>

    <!-- dissociation constant -->
    <td class="data" style="vertical-align: top;" rowspan="1" colspan="6">
        <%= ( interaction.getKD() != null && interaction.getKD() != 0F ) ? interaction.getKD().toString() : "-" %>
    </td>
</tr>

<!-- description info row -->
<tr>

    <!-- 'description' title cell -->
    <%-- NB this was LINKED to help in Experiment!! --%>
    <%-- <td width="10%" class="headerlight">Description</td> --%>
    <td class="headerlight">
        <a href="<%= bean.getHelpLink() + "AnnotatedObject.fullName" %>" class="tdlink">
            Description
        </a>
    </td>
    <td colspan="9" class="lefttop">
        <%= ( interaction.getFullName() != null ) ? interaction.getFullName() : "-" %>
    </td>

</tr>

<%-- NB: THERE MAY BE ANNOTATION AND XREF BLOCKS AT THIS POINT AS WELL... --%>
<%

        Collection intAnnots = bean.getFilteredAnnotations(interaction);
        Collection intXrefs = interaction.getXrefs();

%>

<!-- Interaction Annotation row  -->
<%

        if(intAnnots.size() > 0) {

%>
<tr>

<!-- 'Annotation' title cell (linked to help) -->
<%-- <td width="10%" class="headerlight" rowspan="<%= intAnnots.size() %>" colspan="1"> --%>
<td class="headerlight" rowspan="<%= intAnnots.size() %>" colspan="1">
    <a href="<%= bean.getHelpLink() + "AnnotatedObject.shortLabel" %>" class="tdlink">
        Annotation<br>
    </a>
</td>

<%-- for now do the simplest thing - display all of the Annotation details in turn...
     (NB grouping individuals together requires a lot more logic!!)
     NOTE: First Annotation has to go on the same row as the title cell..
--%>

<%

        rowCount = 0;  //reset rowcount and reuse it
        for(Iterator it2 = intAnnots.iterator(); it2.hasNext();) {
            Annotation annot = (Annotation)it2.next();
            rowCount++;
            if(rowCount > 1) {
                //need a new <tr> tag for all beans except the first one..

%>
<tr>
    <%
        }
    %>

    <!-- annotation 'topic' title cell -->
    <td class="data" style="vertical-align: top;">
        <a href="<%= bean.getCvTopicSearchURL(annot)%>" style="font-weight: bold;">
            <%= annot.getCvTopic().getShortLabel()%></a><br>
    </td>

    <!-- annotation text cell -->
    <%
        //need to check for a 'url' annotation and hyperlink them if so...
        if ( annot.getCvTopic().getShortLabel().equals( CvTopic.URL ) ) {
    %>
    <td style="vertical-align: top;" rowspan="1" colspan="8">
        <a href="<%= annot.getAnnotationText() %>" class="tdlink" target="_blank">
            <%
                String ann = annot.getAnnotationText();
                if (ann == null) ann = "-";
                out.write(ann);
            %>
        </a><br>
        <%

                }
                else {

        %>
    <td style="vertical-align: top;" rowspan="1" colspan="8" class="data">
        <%
                String ann = annot.getAnnotationText();
                if (ann == null) ann = "-";
                out.write(ann);
            %><br>
        <%
            }
        %>
    </td>

</tr>
<%

            }   //end of annotations loop
        }   //end of annotations count check

%>


<!-- Interaction Xref information -->
<%

        if(intXrefs.size() > 0) {

%>
<tr>

<!-- 'Xref' title cell - linked to help -->
<%-- <td width="10%" class="headerlight" rowspan="<%= intXrefs.size()%>" colspan="1" --%>
<td class="headerlight" rowspan="<%= intXrefs.size()%>" colspan="1"
    style="text-align: justify;">
    <a href="<%= bean.getHelpLink() + "AnnotatedObject.Xref"%>" class="tdlink">
        Xref<br>
    </a>
</td>

<%
    rowCount = 0;   //reset rowcount and reuse it
    for ( Iterator it3 = intXrefs.iterator(); it3.hasNext(); ) {
        Xref xref = (Xref) it3.next();
        rowCount++;
        if ( rowCount > 1 ) {
            //need a new <tr> tag for all beans except the first one..
%>
<tr>
    <%
        }
    %>

    <!-- link to the Xref CvDatabase details -->
    <%-- (I think - example is pubmed) --%>
    <%-- <td width="10%" class="lefttop" colspan="1"> --%>
    <td class="lefttop" colspan="1">
        <a href="<%= bean.getCvDbURL(xref) %>" class="tdlink">
            <%= xref.getCvDatabase().getShortLabel() %>
        </a>
    </td>

    <!-- actual search link to the Xref-->
    <%-- ie the real URL filled with the ID - NB if it is null we can't write a link --%>

    <%-- <td width="10%" class="lefttop"> --%>
    <td class="lefttop">
        <%
            String idUrl = bean.getPrimaryIdURL( xref );
            if ( idUrl != null ) {
        %>
        <a href="<%= idUrl %>"><%= xref.getPrimaryId() %></a>
        <%
            } else {
                out.write( xref.getPrimaryId() );
            }
        %>
    </td>

    <!-- The Xref secondaryID, or a dash if there is none -->
    <td colspan="1" class="lefttop">
        <%= ( xref.getSecondaryId() != null ) ? xref.getSecondaryId() : "-" %>
    </td>

    <%-- CvXrefQualifier, linked to search for CV --%>
    <td style="vertical-align: top;" rowspan="1" colspan="6">
        <%
            CvXrefQualifier cvXrefQualifier = xref.getCvXrefQualifier();
            if (cvXrefQualifier != null)
            {
        %>
        <a href="<%= bean.getHelpLink() + "Xref.cvXrefType"%>" target="new">
            Type:</a>
        &nbsp;
        <a href="<%= bean.getCvQualifierURL(cvXrefQualifier)%>">
            <%= xref.getCvXrefQualifier().getShortLabel() %>
        </a><br>
        <%   } else {
                out.write("-");
             } //cvXrefQualifier != null  %>
    </td>
</tr>

<%
        }   //end the Xref loop
    }   //end of xref count check
%>


<!-- Interacting molecules detail (ie the Protein information) -->
<tr>

    <!-- title cell -->
    <%-- NB the span should be equal to the number of Proteins, +1 for the sub-headers --%>
    <td class="headerlight"
        rowspan="<%= interaction.getComponents().size() + 1%>" colspan="1">
        Interacting molecules<br>
    </td>

    <!-- 'name' title cell, linked to help -->
    <td class="headerlight">
        <a href="<%= bean.getHelpLink() + "AnnotatedObject.shortLabel"%>"
           target="new" class="tdlink">Name</a>
    </td>

    <!-- 'ac' title cell, linked to help -->
    <td class="headerlight">
        <a href="<%= bean.getHelpLink() + "BasicObject.ac"%>"
           target="new" class="tdlink">Ac</a>
    </td>

    <td class="headerlight">Interactor type<br></td>

    <td class="headerlight">Stoichiometry</td>

    <!-- 'uniprot description' title cell -->
    <%-- *** NOTE ***
    This has been SWAPPED in position compared to the specification as it is not
    possible to format the 'role' cell properly in its current position in the
    specification. This is due to the fact that it appears in a column whose size
    is determined by a wider cell ('interaction identification'). The only other way
    is to split things up into sub-tables - this then gets very messy!
    --%>
    <%-- not linked again --%>
    <td nowrap="nowrap" class="headerlight">Interactor description<br></td>


    <!-- 'expression system' title cell -->
    <%-- NB this seems NOT to be linked --%>
    <td class="headerlight">Expression system<br></td>

    <!-- 'uniprot ac' title cell -->
    <%-- again seems to NOT be linked to help --%>
    <td class="headerlight">Identifier<br></td>

    <!-- 'gene name' title cell -->
    <%-- again NOT linked to help --%>
    <td class="headerlight">Gene name</td>

    <!-- 'role' title cell, linked to help -->
    <%-- *** ROW POSITION SWAPPED - SEE 'UNIPROT DESCRIPTION' COMMENT *** --%>
    <td class="headerlight">
        <a href="<%= bean.getHelpLink() + "Role"%>" class="tdlink">Role</a>
    </td>
</tr>

<!-- Protein data rows, to match the above title cells..... -->
<% /**
 Collection proteins = bean.getProteins(interaction);
 for(Iterator it1 = proteins.iterator(); it1.hasNext();) {
 Protein protein = (Protein)it1.next();

 **/

%>


<!-- Protein data rows, to match the above title cells..... -->
<%

    for ( Iterator iterator = interaction.getComponents().iterator(); iterator.hasNext(); ) {
        Component component = (Component) iterator.next();
        Interactor interactor = component.getInteractor();
//            if (interactor instanceof Protein) {
//                Protein protein = (Protein) interactor;
        BioSource bioSource = component.getExpressedIn();

%>

<%-- data row.... --%>
<tr>

    <!-- shortlabel, linked to protein partners search -->
    <td class="data">
        <nobr>
            <input name="<%=interactor.getAc()%>" type="checkbox" class="text">
            <a href="<%= bean.getInteractorPartnerURL(interactor)%>"><%=interactor.getShortLabel()%></a></nobr>
        <br>
    </td>

    <!-- ac, linked to protein details view -->
    <td class="data">
        <a href="<%= bean.getInteractorSearchURL(interactor)%>"><%=interactor.getAc()%></a>
    </td>

    <!--interator type-->
    <td class="data">
        <%= bean.getInteractorType( interactor )%>
    </td>

    <!-- stoichiometry -->
    <td class="data" style="vertical-align: top;">
        <%= ( component.getStoichiometry() != 0F ) ? component.getStoichiometry() : "-" %>
    </td>

    <!-- uniprot description -->
    <%-- ASSUME this is the same as the Protein fullName --%>
    <%-- *** ROW POSITION SWAPPED - SEE 'UNIPROT DESCRIPTION' COMMENT *** --%>
    <% if ( interactor.getFullName() == null ) { %>
    <td class="data">-</td>
    <% } else { %>
    <td class="data"><%= interactor.getFullName() %></td>
    <% } %>
    <!-- expression system, (ie the BioSource Full Name), with a search link -->
    <% if ( !bean.getBiosourceURL( bioSource ).equalsIgnoreCase( "-" ) ) { %>
    <td class="data">
        <a href="<%= bean.getBiosourceURL(bioSource)%>"><%= bean.getBioSourceName( bioSource )%></a>
    </td>
    <% } else { %>
    <td class="data">
        <%= bean.getBioSourceName( bioSource )%>
    </td>
    <% } %>

    <!-- uniprot ID, linked 'to biosource' (!) (Guess this should be search in uniprot..)-->
    <%-- This is actually an Xref of the Protein, ie its uniprot Xref... --%>
    <td class="data">
        <%
            if ( bean.getPrimaryIdFromXrefIdentity( interactor ) != "-" ) {
                //link it
        %>
        <a href="<%= bean.getIdentityXrefSearchURL(interactor)%>"><%= bean.getPrimaryIdFromXrefIdentity( interactor ) %></a>
        <%
        } else {
            //don't
        %>
        <%= bean.getPrimaryIdFromXrefIdentity( interactor ) %>
        <%
            }
        %>
    </td>

    <!-- gene name(s), not linked -->
    <td class="data"><%// bean.getGeneNames(protein)%>

        <%
            Collection somePartnerGeneNames = bean.getGeneNames( interactor );

            for ( Iterator iteratorGene = somePartnerGeneNames.iterator(); iteratorGene.hasNext(); ) {
                String aGeneName = (String) iteratorGene.next();
                out.write( aGeneName );
                if ( iteratorGene.hasNext() ) {
                    out.write( ", " );
                }
            }
        %>
    </td>

    <!-- role, linked CvComponentRole search -->
    <%-- *** ROW POSITION SWAPPED - SEE 'UNIPROT DESCRIPTION' COMMENT *** --%>
    <%
        //NB this should never be null....

        //   Component comp = bean.getComponent(protein, interaction);
        if ( component != null ) {
    %>

    <td class="data">
        <%
            RoleInfo roleInfo = CvObjectUtils.createRoleInfo(component.getCvExperimentalRole(), component.getCvBiologicalRole());

            if (roleInfo.isBiologicalRoleUnspecified()){
        %>
                <a href="<%= bean.getCvExperimentalRoleSearchURL(component)%>">
                    <%= component.getCvExperimentalRole().getShortLabel()%>
                </a>
        <%
            }
            if (!roleInfo.isBiologicalRoleUnspecified() && !roleInfo.isExperimentalRoleUnspecified()) {
        %>
        /
        <%

            if (roleInfo.isExperimentalRoleUnspecified()) {
        %>
        <a href="<%= bean.getCvBiologicalRoleSearchURL(component)%>">
            <%= component.getCvBiologicalRole().getShortLabel()%>
        </a>
        <%
                }
            }
        %>
    </td>
    <%
    } else {
    %>

    <td class="data">
        -
    </td>
    <% } %>
</tr>
<%
    }   //end of the proteins loop

%>

<!-- 'sequence features' details start here... -->

<%
    Collection featureRows = bean.getFeaturesSummary( interaction );
    if ( ! featureRows.isEmpty() ) {
        Iterator iterator = featureRows.iterator();
%>
<tr>
    <td style="vertical-align: top;" class="headerlight" rowspan="<%= featureRows.size() %>" colspan="1">
        <a href="<%= bean.getHelpLink() + "FEATURES_HELP_SECTION"%>" class="tdlink">Sequence features</a>
    </td>
    <td class="data" style="vertical-align: top;" rowspan="1" colspan="9">
        <%= iterator.next() %>
    </td>
</tr>
<%
    while ( iterator.hasNext() ) {
%>
<tr><td class="data" style="vertical-align: top;" rowspan="1" colspan="9">
    <%= iterator.next() %>
</td></tr>
<%
        } // while

    } // if any feature
%>
<%

     }   //end of interactions loop

%>

</tbody>
</table>

<!-- END of the main display table -->

<%
    if (detailView.getTotalItems() > detailView.getItemsPerPage())
    {
%>
<%-- table pagination --%>
<%@include file="tablePagination.jspf"%>

<%
    }
    else
    {
%>
     <br/>
<%
    }
%>

<%-- button bar for the table --%>
<%@ include file="buttonBar.html" %>


<br>

</form>

