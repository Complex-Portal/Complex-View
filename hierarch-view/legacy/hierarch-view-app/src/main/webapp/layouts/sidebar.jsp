<%@ page language="java"%>

<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - The layout for the side bar, which consists of intact logo, input dialog box
   - and menu. Except for the logo, other two components are optional.
   -
   - @author Samuel Kerrien (skerrien@ebi.ac.uk)
   - @version $Id$
-->

<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<table width="100%" border="0" cellspacing="0" cellpadding="0">

    <!-- The input dialog box -->
    <tr>
        <td>
            <tiles:insert attribute="search" ignore="true"/>
        </td>
    </tr>

    <%-- Graph menu --%>
    <tr>
		<td>
            <tiles:insert attribute="graph" ignore="true"/>
        </td>
	</tr>

    <%-- Click behaviour menu --%>
    <tr>
		<td>
            <tiles:insert attribute="click" ignore="true"/>
        </td>
	</tr>

    <%-- PSI section --%>
    <tr>
		<td>
            <tiles:insert attribute="psi" ignore="true"/>
        </td>
	</tr>
	
	<tr>
		<td>
			<tiles:insert attribute="mine" ignore="true"/>
		</td>
	</tr>	

</table>

<%-- Separation line before the links --%>

<hr>