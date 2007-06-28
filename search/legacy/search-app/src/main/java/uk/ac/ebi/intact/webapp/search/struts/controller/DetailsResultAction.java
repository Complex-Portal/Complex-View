/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.webapp.search.struts.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.searchengine.SearchClass;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;
import uk.ac.ebi.intact.webapp.search.struts.view.beans.MainDetailView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * Performs the beans view for the Experiement View.
 * <p>
 * This Action can be called in case of a Experiement or an Interaction
 *
 * @author Michael Kleen
 * @version $Id:DetailsResultAction.java 6452 2006-10-16 17:09:42 +0100 (Mon, 16 Oct 2006) baranda $
 */
public class DetailsResultAction extends AbstractResultAction {

    private static final Log logger = LogFactory.getLog(DetailsResultAction.class);

    /**
     * Overrides the parent one to process the request more effectively.
     * <p>
     * It avoids making any assumptions
     * about the beans or the size of search result list and keep all of the processing in a single place for each
     * Action type.
     *
     * @param request  The request to be processed
     *
     * @return String the forward code for the parent execute method to return.
     */
    @Override
    protected String processResults( HttpServletRequest request ) {

        //new info to process, so get the search results from the request
        Collection results = (Collection) request.getAttribute( SearchConstants.SEARCH_RESULTS );
        //initial sanity check - empty results should be just ignored
        if ( ( results == null ) || ( results.isEmpty() ) ) {
            return SearchConstants.FORWARD_NO_MATCHES;
        }

        logger.info( "DetailAction: result Collection contains " + results.size() + " items." );

        //regardless of the result size, just build a viewbean for each result and put into
        //a Collection for use by the JSP - but first check we have the correct type for this
        //Action to process..
        Class<?> resultType = results.iterator().next().getClass();

        Collection<Experiment> experiments = null;
        Interaction interactionResult = null;   //used to tell the viewbean what needs displaying

        if ( ( Interaction.class.isAssignableFrom( resultType ) ) ) {
            interactionResult = (Interaction) results.iterator().next();

            experiments = interactionResult.getExperiments();

        } else if ( Experiment.class.isAssignableFrom( resultType ) ) {
            experiments = results;  //got Experiments in the first place
        }

        if ( experiments != null && !experiments.isEmpty()) {

            if (experiments.size() > 1)
            {
                 logger.warn( "DetailAction: only the first experiment will be shown." );
             }

            Experiment experiment = experiments.iterator().next();
            MainDetailView view = new MainDetailView(request, experiment);

            // We store the MainDetailView in the request, and it will be accessed from the jsp page
            request.setAttribute( SearchConstants.VIEW_BEAN, view );
            request.setAttribute("searchClass", SearchClass.EXPERIMENT.getShortName());

             //send to the detail view JSP
            logger.info( "detailsAction: forwarding to 'details' JSP.." );
            return SearchConstants.FORWARD_DETAIL_PAGE;

        } else {
            //something is wrong here - forward to error page 
            return SearchConstants.FORWARD_FAILURE;
        }
    }
}