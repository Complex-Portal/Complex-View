/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.mine.struts.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import uk.ac.ebi.intact.application.mine.business.Constants;
import uk.ac.ebi.intact.application.mine.business.IntactUserI;
import uk.ac.ebi.intact.application.mine.business.MineException;
import uk.ac.ebi.intact.application.mine.business.graph.GraphManager;
import uk.ac.ebi.intact.application.mine.business.graph.MineHelper;
import uk.ac.ebi.intact.application.mine.business.graph.model.GraphData;
import uk.ac.ebi.intact.application.mine.struts.view.ErrorBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * This class provides the action to start the Dijkstra algorithm to find the
 * shortest path between all search nodes.
 *
 * @author Andreas Groscurth (groscurt@ebi.ac.uk)
 */
public class DisplayAction extends Action {

    private static final Log logger = LogFactory.getLog(DisplayAction.class);

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an ActionForward instance describing where and how control should
     * be forwarded, or null if the response has already been completed.
     *
     * @param mapping  - The <code>ActionMapping</code> used to select this
     *                 instance
     * @param form     - The optional <code>ActionForm</code> bean for this
     *                 request (if any)
     * @param request  - The HTTP request we are processing
     * @param response - The HTTP response we are creating
     * @return - represents a destination to which the controller servlet,
     *         <code>ActionServlet</code>, might be directed to perform a
     *         RequestDispatcher.forward() or HttpServletResponse.sendRedirect()
     *         to, as a result of processing activities of an
     *         <code>Action</code> class
     */
    public ActionForward execute( ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response ) {
        HttpSession session = request.getSession( true );
        IntactUserI user = (IntactUserI) session.getAttribute( Constants.USER );
        // the accession numbers for the minimal connecting network are fetched
        Collection searchFor =
                (Collection) request.getAttribute( Constants.SEARCH );

        logger.info( "to search for " + searchFor );

        MessageResources mr = getResources( request );

        // if no user is in the current session an excepion is thrown
        // because up to now a user should have been created and e.g.
        // the search ac nr. should have been set.
        if( user == null || searchFor == null ) {
            throw new NullPointerException( "No user could be found in the " + "current session" );
        }

        logger.info( "start minehelper" );

        MineHelper helper = null;
        try {
            helper = new MineHelper( user );
        } catch ( MineException e3 ) {
            request.setAttribute( Constants.ERROR, new ErrorBean( mr.getMessage( "displayAction.noData" ) ) );
            return mapping.findForward( Constants.ERROR );
        }
        // the network map maps the unique graphid to the accession numbers
        // which are in the graph represented by the graphid
        // Integer -> Collection (related protein ac)
        Map networks = null;
        try {
            networks = helper.getNetworkMap( searchFor );
        } catch ( SQLException e1 ) {
            request.setAttribute( Constants.ERROR, new ErrorBean( mr.getMessage( "displayAction.noNetworkMap" ) ) );
            return mapping.findForward( Constants.ERROR );
        }
        Integer graphid;

        Collection search = null;
        for ( Iterator iter = networks.keySet().iterator(); iter.hasNext(); ) {
            // the key stores the taxid and graphid for the current search
            graphid = (Integer) iter.next();
            search = (Collection) networks.get( graphid );

            // if the current search ac are in a graph in the database
            if( graphid != Constants.SINGLETON_GRAPHID && search.size() > 1 ) {
                // the shortest path is computed
                logger.debug( "searching for MiNe with " + search );
                GraphData graphData;
                // the graphManager is responsible for building and storing the
                // graphs it is implemented as a singleton so just one instance
                // is allowed during runtime.
                GraphManager graphManager = GraphManager.getInstance();
                // the graph is fetched for the given graphid. As long as there
                // is no graph given for the graphid the action waits 25ms and
                // then tries again to retrieve the graph
                logger.debug( "starting it with " );
                while ( ( graphData = graphManager.getGraphData( graphid, user ) )
                        == null ) {
                    try {
                        Thread.sleep( 25 );
                    } catch ( InterruptedException e ) {
                        request.setAttribute( Constants.ERROR, new ErrorBean( mr.getMessage( "displayAction.noGraph", Integer.toString( graphid.intValue() ) ) ) );
                        return mapping.findForward( Constants.ERROR );
                    }
                }
                logger.warn( "done" );
                try {
                    // the minimal network is computed
                    // the found network is then updated in the MiNeHelper class
                    helper.computeMiNe( graphData, search );
                } catch ( MineException e2 ) {
                    String searchString = search.toString();
                    searchString =
                    searchString.substring( 1, searchString.length() - 1 );
                    request.setAttribute( Constants.ERROR, new ErrorBean( mr.getMessage( "displayAction.noMiNe", searchString ) ) );
                    return mapping.findForward( Constants.ERROR );
                }
            } else {
                logger.info( search + " is added to the singletons" );
                // the search accession numbers are not in a graph
                // therefore they are added to the singletons
                try {
                    user.addToSingletons( helper.getShortLabels( search ) );
                } catch ( SQLException e ) {
                    user.addToSingletons( search );
                }
            }
        }

        // if no paths could been found the application is forwarded
        // to the error page
        if( user.getPaths().isEmpty() ) {
            logger.warn( "no connecting network found" );
            // the singletons are wrapped into a string
            // [1,2,3] -> 1,2,3
            String singletons = user.getSingletons().toString();
            singletons = singletons.substring( 1, singletons.length() - 1 );

            request.setAttribute( Constants.ERROR, new ErrorBean( mr.getMessage( "displayAction.noNetwork", singletons ) ) );
            return mapping.findForward( Constants.ERROR );
        }
        // forward to the result page
        return mapping.findForward( Constants.SUCCESS );
    }
}