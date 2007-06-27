/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.graph2MIF;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.graph2MIF.conversion.FusionableGraph;
import uk.ac.ebi.intact.application.graph2MIF.exception.NoGraphRetrievedException;
import uk.ac.ebi.intact.application.graph2MIF.exception.NoInteractorFoundException;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Constants;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.util.simplegraph.Graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Graph producer.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class GraphFactory {

    private static final Log logger = LogFactory.getLog(GraphFactory.class);


    /**
     * getInteractionNetwork retrieves a interactionnetwork (graph) from a given queryString and depth.
     * THe queryString can be a comma-separated list of Interactor's AC.
     *
     * @param queryString String queryString in IntAct
     * @param depth Integer of the depth the graph should be expanded
     * @return graph of the queryString with given depth
     * @exception IntactException thrown if search for interactor failed
     * @exception uk.ac.ebi.intact.application.graph2MIF.exception.NoGraphRetrievedException thrown if DOM-Object could not be serialized
     * @exception uk.ac.ebi.intact.application.graph2MIF.exception.NoInteractorFoundException thrown if no Interactor found for queryString
     */
    public static Graph getGraph( String queryString, Integer depth )
            throws IntactException,
                   NoInteractorFoundException,
                   NoGraphRetrievedException {

        StringTokenizer st = new StringTokenizer (queryString, ",");
        ArrayList queries = new ArrayList();
        String aQuery;
        while (st.hasMoreElements()) {
            aQuery = st.nextToken().trim(); // remove front and back blank space
            if (aQuery.length() > 0)
                queries.add (aQuery);
        }

        //for graph retrieval a interactor is necessary. So get the interactor of given queryString.
        Collection<Interactor> interactors = new ArrayList<Interactor>( queries.size() );
        try {
            logger.info( "Retrieve Interactor from queryString("+ queryString +")" );
            for ( Iterator iterator = queries.iterator (); iterator.hasNext (); ) {
                String query = (String) iterator.next ();
                interactors.add( IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                    .getInteractorDao().getByAc(query) );
            }
        } catch (IntactException e) {
            logger.error( "Could not search for Interactor AC: " + queryString, e );
            throw e;
        }
        logger.info( interactors.size() + " Interactor found." );

        IntactGraphHelper graphHelper = new IntactGraphHelper();

        FusionableGraph interactionNetwork;
        Iterator interactorIterator = interactors.iterator();
        // process the first interactor
        if (interactorIterator.hasNext()) {
            Interactor interactor = (Interactor) interactorIterator.next();
            interactionNetwork = new FusionableGraph();
            try {
                logger.info ( "Start building an Interaction Network from AC: "+ interactor.getAc() +
                        ", depth: "+ depth +"." );
                graphHelper.subGraph( interactor,
                        depth,
                        null,
                        Constants.EXPANSION_BAITPREY,
                        interactionNetwork );
                logger.info ( "Initial graph("+ interactor.getAc() +"):" + interactionNetwork );
            } catch (IntactException e) {
                logger.error("IntActException while subgraph() call " + e.getMessage(), e);
                throw new NoGraphRetrievedException();
            }

            // process eventual further interaction
            while( interactorIterator.hasNext() ) {
                interactor = (Interactor) interactorIterator.next();
                FusionableGraph interactionNetwork2 = new FusionableGraph();
                try {
                    logger.info ( "Start building an Interaction Network from AC: "+ interactor.getAc() +
                            ", depth: "+ depth +"." );
                    graphHelper.subGraph( interactor,
                            depth,
                            null,
                            Constants.EXPANSION_BAITPREY,
                            interactionNetwork2 );
                    logger.info ( "Additional network()" + interactionNetwork2 );
                    interactionNetwork.fusion( interactionNetwork2 );
                    logger.info( "After fusion: " + interactionNetwork );
                } catch (IntactException e) {
                    logger.warn("IntActException while subgraph() call " + e.getMessage(), e);
                    throw new NoGraphRetrievedException();
                }
            }
        } else { //No Interactor found
            logger.warn("No Interactor found for: " + queryString);
            throw new NoInteractorFoundException();
        }

        if (interactionNetwork == null) {
            logger.warn("retrieved graph == null");
            throw new NoGraphRetrievedException();
        }

        logger.info ("Return the graph.");

        //return this graph
        return interactionNetwork;
    }
}
