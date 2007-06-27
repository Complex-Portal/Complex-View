<%@ page language="java" %>

<%@ taglib uri="http://ebi.ac.uk/intact/commons" prefix="intact"%>

<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - hierarchView highlight title page
   - This should be displayed in the content part of the IntAct layout,
   - it displays the highlightment sources title.
   -
   - @author Samuel Kerrien (skerrien@ebi.ac.uk)
   - @version $Id$
-->

<table border="0" cellspacing="3" cellpadding="3" width="100%">

      <tr>
             <td>
                   <!-- displays the interaction network title -->
                   Existing highlight sources for the central protein(s).
                   <intact:documentation section="hierarchView.PPIN.highlight" />
             </td>
      </tr>

</table>