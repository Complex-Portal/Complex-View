package uk.ac.ebi.intact.webapp.search.struts.controller;

import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

import javax.servlet.http.HttpServletRequest;
import java.util.StringTokenizer;


/**
 * This <code>Action</code> class sums up the selected Protein ACs of the IntAct Search webapp
 * and sets up a waiting message in Html format to display the selected IntAct ACs.
 * Furthermore, this class is then forwarding to the actual <code>InterProSearchAction</code> class by passing
 * its IntAct protein ACs.
 *
 * @author Christian Kohler (ckohler@ebi.ac.uk)
 * @version $Id$
 * @see InterProSearchAction
 */
public class WaitingInterProSearchAction extends AbstractResultAction {
    protected String processResults(HttpServletRequest request) {

        // the IntAct ACs of the selected Proteins
        String ac = request.getParameter("ac");

        // stores all IntAct ACs as a String (e.g. EBI-1, EBI-2,...)
        StringBuffer acBuffer = new StringBuffer(512);

        // builds Html Message, where only the selected IntAct ACs will be displayed
        setupAcHtmlMessage(acBuffer, ac);

        // forwarding Attribute WAITING_MSG to waiting.jsp
        request.setAttribute(SearchConstants.WAITING_MSG, acBuffer.toString());

        StringTokenizer st = new StringTokenizer(ac, ",");
        StringBuffer urlBuffer = new StringBuffer(512);

        // builds URL, forwarding to InterProSearchAction
        urlBuffer.append("../do/interProSearch?ac=");

        while (st.hasMoreTokens()) {
            String intactAC = st.nextToken();
            urlBuffer.append(intactAC);
            if (st.hasMoreTokens()) {
                urlBuffer.append(',');
            }
        }

        // Attribute defined in SearchConstants.java
        request.setAttribute(SearchConstants.WAITING_URL, urlBuffer.toString());

        // forwarding to the waiting page
        return SearchConstants.FORWARD_WAITING_PAGE;
    }


    /**
     * Builds Html Waiting-Message, shown before the <code>Action</code> is performed.
     *
     * @param acBuffer used to append all the IntAct AC(s).
     * @param ac       contains the IntAct AC(s) as String (eg. EBI-1,EBI-2,EBI-3...).
     */
    public void setupAcHtmlMessage(StringBuffer acBuffer, String ac) {
        StringTokenizer st = new StringTokenizer(ac, ",");

        // set to 1 in order not to break the line after the first IntAct AC.
        int count = 1;
        int numberOfACs = st.countTokens();
        acBuffer.append("<table border=\"0\">");
        acBuffer.append("<font size=\"3\"><u>Step 1:</u></font>").append("      Converting ").
                append(numberOfACs).append(" IntAct AC(s) to UniProtKB ID(s): ").append("<p>").append("<br>");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            
            // breaks html-Line in order to display only 10 ACs per row
            boolean breakLine = (count % 10) == 0;

            acBuffer.append("<b><big>").append(s).append("</big></b>");
            if (st.hasMoreTokens()) {
                acBuffer.append(", ");

                if (breakLine) {
                    acBuffer.append("<br>");
                }
            }
            count++;
        }

        acBuffer.append("<br>");
        acBuffer.append("<font color=\"#999999\" size =\"3\">");
        acBuffer.append("<br>").append("<p>");
        acBuffer.append("<u>Step 2:</u>").append("       Searching InterPro ID by UniProtKB ID: ").append("<br>");
        acBuffer.append("</font>");
        acBuffer.append("</table>");
    }
}
