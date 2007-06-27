<%@ page language="java" %>

<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - hierarchView graph title page
   - This should be displayed in the content part of the IntAct layout,
   - it displays the interaction network title.
   -
   - @author Samuel Kerrien (skerrien@ebi.ac.uk)
   - @version $Id$
-->

<%@ page import="uk.ac.ebi.intact.application.hierarchview.business.Constants,
                 uk.ac.ebi.intact.application.hierarchview.business.IntactUserI,
                 uk.ac.ebi.intact.application.hierarchview.business.graph.InteractionNetwork,
                 uk.ac.ebi.intact.context.IntactContext,
                 uk.ac.ebi.intact.searchengine.CriteriaBean,
                 java.util.ArrayList,
                 java.util.Iterator" %>
<%@ page import="java.util.StringTokenizer" %>

<%
    /**
     * Retreive user's data from the session
     */
    IntactUserI user = (IntactUserI) IntactContext.getCurrentInstance().getSession().getAttribute(Constants.USER_KEY);
    if (user.getSearchUrl() == null) return;

    InteractionNetwork in = user.getInteractionNetwork();
    if (in == null) return;

    String prefix = "<b>";
    String suffix = "</b>";

    ArrayList criterias = in.getCriteria();
    StringBuffer context = new StringBuffer(512);
    for (Iterator iterator = criterias.iterator(); iterator.hasNext();)
    {
        CriteriaBean aCriteria = (CriteriaBean) iterator.next();

        context.append(prefix + aCriteria.getTarget() + ": ");
        context.append("<a href=\"" + user.getSearchUrl(aCriteria.getQuery(), false) + "\" target=\"_blank\">" +
                aCriteria.getQuery() + "</a>");
        context.append(suffix + ", ");
    }

    int max = criterias.size();
    // remove the last comma and white space
    StringBuffer contextToDisplay = new StringBuffer(256);
    if ((max = context.length()) > 0)
    {
        String network = (String) session.getAttribute("network");
        String singletons = (String) session.getAttribute("singletons");

        String tmp = context.substring(0, max - 2);
        // if no MiNe parameters are given the default HV display is used
        if (null == network && null == singletons)
        {
            // the normal hv display
            contextToDisplay.append("Interaction network for ");
            contextToDisplay.append(tmp).append("<br>");
        }
        else
        {
            String net = "This is the minimal connecting network for ";
            // the string containing the borders of the networks is split
            StringTokenizer tokens = new StringTokenizer(network, ",");
            // the int array stores the length of the different
            // connecting networks
            int[] borders = new int[tokens.countTokens()];
            int i = 0;
            // the borders are parsed as an int and stored in the array
            while (tokens.hasMoreTokens())
            {
                borders[i++] = Integer.parseInt(tokens.nextToken());
            }
            // the string containing the shortlabels of the interactors
            // of the minimal connecting networks are split
            tokens = new StringTokenizer(tmp, ",");
            contextToDisplay.append(net);
            i = 1;
            int j = 0;
            while (tokens.hasMoreTokens())
            {
                // the current interactor is added
                contextToDisplay.append(tokens.nextToken());
                // if the boundary of the current interaction network is reached
                if (i == borders[j])
                {
                    // if there are more interactors are available
                    if (tokens.hasMoreTokens())
                    {
                        // because there are more interactors available
                        // a newline is created and the beginning text
                        // for each network is added
                        contextToDisplay.append("<br>").append(net);
                    }
                    j++;
                }
                i++;
            }

            // if there are also singletons available
            // they are added to the comment line with a link
            // to the search application
            if (null != singletons)
            {
                String tok;
                contextToDisplay.append("<br>The following proteins are not in " +
                        "a connecting network: ");
                tokens = new StringTokenizer(singletons, ",");
                while (tokens.hasMoreTokens())
                {
                    // the current singleton is fetched
                    tok = tokens.nextToken().trim();
                    contextToDisplay.append(prefix + "shortLabel: ");
                    // the search string for the current singleton is fetched
                    contextToDisplay.append("<a href=\"" + user.getSearchUrl(tok, false) + "\"");
                    contextToDisplay.append(" target=\"_blank\">" + tok + "</a>");
                    contextToDisplay.append(suffix + " ");
                }
                session.setAttribute("singletons", null);
            }
            // the attribute is erazed to allow the user to work
            // without problems without the mine informations
            session.setAttribute("network", null);
        }
    }
    String selectedKey = user.getSelectedKey();
    if (selectedKey == null) selectedKey = "";
    else
    {
        selectedKey = "<br>Highlight by " + prefix + selectedKey + suffix;
    }

%>

<table border="0" cellspacing="3" cellpadding="3" width="100%" height="100%">

      <tr>
             <td>
			 <%= contextToDisplay.toString() %>
             </td>
      </tr>

</table>