/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.highlightment.behaviour;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.service.graph.Edge;
import uk.ac.ebi.intact.service.graph.Node;

import java.util.Collection;

/**
 * Behaviour allowing to display only highlighted protein and hide all others.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class VisibleHighlightmentBehaviour extends HighlightmentBehaviour {

    private static final Log logger = LogFactory.getLog( VisibleHighlightmentBehaviour.class );

    /**
     * Select all the graph's protein which are not in the given collection.<br>
     * The aim of that behaviour is to display only se selected protein, so we
     * have to set the VISIBLE flag of all other proteins to false.
     *
     * @param objects the list of protein to highlight
     * @param aGraph  the current interaction network
     * @return the new collection of protein to highlight
     */
    public Collection<?> modifyCollection( Collection objects, Network aGraph ) {

        if ( objects != null && !objects.isEmpty() ) {
            Class objectClass = objects.iterator().next().getClass();
            if ( objectClass.isAssignableFrom( Node.class ) ) {
                /* Get the list of proteins in the current Network */
                Collection<Node> listAllProteins = aGraph.getNodes();
                /* Make a clone of the list */
                Collection newList = listAllProteins;
                /* Remove all proteins of the collection "proteins" */
                newList.removeAll( objects );

                return newList;
            }
            if ( objectClass.isAssignableFrom( Edge.class ) ) {
                /* Get the list of edges in the current Network */
                Collection<Edge> listAllEdges = aGraph.getEdges();
                /* Make a clone of the list */
                Collection newList = listAllEdges;
                /* Remove all proteins of the collection "proteins" */
                newList.removeAll( objects );

                return newList;
            }
        }
        logger.error( "Collection which should be modified is null or empty!" );
        return objects;

    }


    /**
     * Apply the implemented behaviour to the specific Node of the graph.
     * Here, we change the visibility to false for the given node.
     *
     * @param aObject the node on which we want to apply the behaviour
     */
    public void applyBehaviour( Object aObject, Network aGraph ) {

        if ( Node.class.isInstance( aObject ) ) {
            aGraph.getNodeAttributes( (( Node ) aObject).getId() ).put( Constants.ATTRIBUTE_VISIBLE, Boolean.FALSE );
        }
        if ( Edge.class.isInstance( aObject ) ) {
            aGraph.getEdgeAttributes( ( Edge ) aObject ).put( Constants.ATTRIBUTE_VISIBLE, Boolean.FALSE );
        }
    }
}









