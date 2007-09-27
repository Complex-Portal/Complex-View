<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - Assembles various JSPs to present the Feature editor.
  --%>

<%@ page language="java"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>

<jsp:include page="../js.jsp" />

<%-- Include javascript for show user functionality --%>
<jsp:include page="../misc/user_js.jsp"/>

<html:form action="/featureDispatch" onsubmit="return validateFeatureForm(this)">
    <html:hidden property="anchor" />

    <jsp:include page="creation_info.jsp"/>
    </p>
    <jsp:include page="parent_protein.jsp" />
    <jsp:include page="defined_features.jsp" />
    </p>
    <jsp:include page="new_feature.jsp" />
    </p>
    <jsp:include page="ranges.jsp" />
    </p>
    <jsp:include page="add_range.jsp" />
    <jsp:include page="../xrefs.jsp" />
    <jsp:include page="../addXrefs.jsp" />

    <p></p>
    <jsp:include page="../action.jsp" />

</html:form>
<html:javascript formName="featureForm"/>