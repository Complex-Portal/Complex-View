package uk.ac.ebi.intact.webapp.search.struts.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.searchengine.SearchClass;
import uk.ac.ebi.intact.webapp.search.business.IntactUserIF;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;
import uk.ac.ebi.intact.webapp.search.struts.view.beans.SimpleViewBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This Action class performs the construction of view beans that will be used by the simple.jsp
 * page to display initial search results. Note that this Action is different to all others in that
 * it may have to construct more than a single view bean since the results to be displayed may be of
 * multiple types. Therefore it may not be appropriate to extend from AbstractResultAction as in
 * other cases.
 *
 * @author Michael Kleen
 * @version $Id:SimpleResultAction.java 6452 2006-10-16 17:09:42 +0100 (Mon, 16 Oct 2006) baranda $
 */
public class SimpleResultAction extends AbstractResultAction {

    private static final Log logger = LogFactory.getLog(SimpleResultAction.class);

    /**
     * This method overrides the parent one to process the request more effectively. It avoids
     * making any assumptions about the beans or the size of search result list and keep all of the
     * processing in a single place for each Action type.  This method creates WrapperObjects for the
     * Objects Interactions, Proteins,  Experiments, CvObjects and BioSource and returns the the code which
     * is needed to forward to view.
     *
     * @param request  The request object containing the data we want
     * @return String the return code for forwarding use by the execute method
     */
    @Override
    protected String processResults(HttpServletRequest request) {

        logger.debug("enter simple action");

        HttpSession session = super.getSession(request);

        // Handle to the Intact User.
        IntactUserIF user = super.getIntactUser(session);

        //get the search results from the request
        Collection<? extends AnnotatedObject> results = (Collection<? extends AnnotatedObject>) request.getAttribute(SearchConstants.SEARCH_RESULTS);

        logger.debug("SimpleAction: Collection contains " + results.size() + " items.");

        String contextPath = request.getContextPath();
        //build the URL for searches and pass to the view beans
        String searchURL = super.getSearchURL();

        //we can build a List of sublists, partitioned by type, here inseatd of
        //in the JSP. That way the JSP only ever gets things to display, unless
        //there are too many items to display for a particular type...

        List<SimpleViewBean> expList = new ArrayList<SimpleViewBean>(results.size());
        List<SimpleViewBean> interactionList = new ArrayList<SimpleViewBean>(results.size());
        List<SimpleViewBean> proteinList = new ArrayList<SimpleViewBean>(results.size());
        List<SimpleViewBean> nucleicAcidList = new ArrayList<SimpleViewBean>(results.size());
        List<SimpleViewBean> smallMoleculeList = new ArrayList<SimpleViewBean>(results.size());
        List<SimpleViewBean> cvObjectList = new ArrayList<SimpleViewBean>(results.size());

        List<List<SimpleViewBean>> partitionList = new ArrayList<List<SimpleViewBean>>(results.size());   //this will hold the seperate lists as items

        for (AnnotatedObject obj : results)
        {
            SearchClass searchClass = SearchClass.valueOfMappedClass(obj.getClass());

             //now create a relevant view bean for each type in the result set...
            if (searchClass == SearchClass.EXPERIMENT)
            {
                expList.add(new SimpleViewBean(obj));
            }
            else if (searchClass == SearchClass.INTERACTION)
            {
                interactionList.add(new SimpleViewBean(obj));
            }
            else if (searchClass == SearchClass.PROTEIN)
            {
                proteinList.add(new SimpleViewBean(obj));
            }
            else if (searchClass == SearchClass.NUCLEIC_ACID)
            {
                nucleicAcidList.add(new SimpleViewBean(obj));
            }
            else if (searchClass == SearchClass.SMALL_MOLECULE)
            {
                smallMoleculeList.add(new SimpleViewBean(obj));
            }
            else if (searchClass.isCvObjectSubclass())
            {
                cvObjectList.add(new SimpleViewBean(obj));
            }
        } // for

        //now add in the sublists to the in the partitionlist

        if (!expList.isEmpty()) {
            partitionList.add(expList);
        }
        if (!interactionList.isEmpty()) {
            partitionList.add(interactionList);
        }
        if (!proteinList.isEmpty()) {
            partitionList.add(proteinList);
        }
        if (!nucleicAcidList.isEmpty()) {
            partitionList.add(nucleicAcidList);
        }
        if (!smallMoleculeList.isEmpty()) {
            partitionList.add(smallMoleculeList);
        }
        if (!cvObjectList.isEmpty()) {
            partitionList.add(cvObjectList);
        }

//        //get the maximum size beans from the context for later use
//        Map sizeMap = (Map) session.getServletContext().getAttribute(SearchConstants.MAX_ITEMS_MAP);

        //put the viewbeans in the request and send on to the view...
        request.setAttribute(SearchConstants.VIEW_BEAN_LIST, partitionList);
        //TODO use a Constant here
        return "simpleResults";
    }

}
