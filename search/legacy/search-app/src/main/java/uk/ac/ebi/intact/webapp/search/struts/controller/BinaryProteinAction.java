package uk.ac.ebi.intact.webapp.search.struts.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.webapp.search.struts.util.ProteinUtils;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

/**
 * This Action class performs the calculating and the construction of view beans that will be used for a for an url
 * based search query for 2 protein. It will calculate the interactions and give back the code to forward to the
 * coresponding JSP site for the representation of the results. This method performances the Request
 * from the url-based query with http://www.ebi.ac.uk/intact/search/do/search?binary=protein1,protein2.
 * It calculates all binary and all self interactions between these 2 proteins and returns
 * code to forward to the specific view.
 *
 * @author Michael Kleen
 * @version BinaryProteinAction.java Date: Jan 14, 2005 Time: 12:53:45 PM
 */
public class BinaryProteinAction extends AbstractResultAction {

    private static final Log logger = LogFactory.getLog(BinaryProteinAction.class);

    /**
     * This method overrides the parent one to process the request more effectively. It avoids
     * making any assumptions about the beans or the size of search result list and keep all of the
     * processing in a single place for each Action type.
     *
     * @param request  The request object containing the data we want
     * @return String the return code for forwarding use by the execute method
     */
    protected String processResults(HttpServletRequest request) {

        logger.info("binary protein action");

        final Collection someInteractors = (Collection<AnnotatedObject>) IntactContext.getCurrentInstance()
                .getSession().getRequestAttribute(SearchConstants.SEARCH_RESULTS);

        Collection<? extends AnnotatedObject> results;

        logger.info("interactors in result set: " + someInteractors.size());
        HttpSession session = super.getSession(request);

        // first check for self interactions

        if (someInteractors.size() == 1) {

            final Interactor selfInteractor = (Interactor) someInteractors.iterator().next();

            String protAc = selfInteractor.getAc();

            logger.info("Binary Protein Action: 1 Protein: "+protAc);

            results = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                    .getInteractionDao().getSelfBinaryInteractionsByProtAc(protAc);

            if (logger.isDebugEnabled())
            {
                boolean hasSelfInteraction = results.size() > 0;

                if (hasSelfInteraction)
                {
                    logger.debug("BinaryAction: protein has a self interaction ");
                }
            }
        }
        else if (someInteractors.size() == 2) {
            logger.info("binary interactions");

            results = ProteinUtils.getBinaryInteractions(someInteractors);
            logger.debug("results interactions size : " + results.size());

        }
        else {

            // If we got more than 2 proteins forward to errorpage
            logger.debug("more than 2 Proteins, forward to errorpage");
            return SearchConstants.FORWARD_TOO_MANY_INTERACTORS;
        }

        logger.debug("Interactions found: "+results.size());

        if (!results.isEmpty()) {
            logger.debug("search sucessful");
            //TODO use session here
            request.setAttribute(SearchConstants.SEARCH_RESULTS, results);
            // the simple action handle the prasentation of the interactions
            return SearchConstants.FORWARD_SIMPLE_ACTION;

        }
        else {
            logger.debug("no interactions found between these proteins resultset empty");
            // create statistic
            String info = (String) session.getAttribute("binary");
            StringTokenizer st = new StringTokenizer(info, ",");
            Collection query = new ArrayList(results.size());

            while (st.hasMoreTokens()) {
                String value = st.nextToken();
                query.add(value);
            }

            logger.debug("forward to no interactions view");
            // add the statistics to the request and forward to the no interactions jsp
            request.setAttribute(SearchConstants.RESULT_INFO, query);

            return SearchConstants.FORWARD_NO_INTERACTIONS;
        }
    }
}

