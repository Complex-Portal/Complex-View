/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.hierarchview.highlightment.behaviour;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.EdgeAttributes;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.business.graph.NodeAttributes;
import uk.ac.ebi.intact.application.hierarchview.business.image.Utilities;
import uk.ac.ebi.intact.service.graph.Edge;
import uk.ac.ebi.intact.service.graph.Node;

import java.awt.*;
import java.util.Properties;

/**
 * Behaviour class allowing to change the color of highlighted proteins.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class ColorHighlightmentBehaviour extends HighlightmentBehaviour {

    private static final Log logger = LogFactory.getLog( ColorHighlightmentBehaviour.class );

    /**
     * ******* CONSTANTS ***********
     */
    private final static Color DEFAULT_COLOR_HIGHLIGHTING = new Color( 255, 0, 0 );
    private final static Color COLOR_HIGHLIGHTING;

    static {
        Properties properties = IntactUserI.GRAPH_PROPERTIES;
        String colorString = null;

        if ( null != properties ) {
            // the colorstring given my the properties file is fetched
            colorString = properties.getProperty( "hierarchView.color.highlighting" );

            if ( colorString == null ) {
                logger.warn( "Unable to find the property hierarchview.color.highlighting in "
                             + Constants.PROPERTY_FILE );
            }
        } else {
            logger.warn( "Unable to find " + Constants.PROPERTY_FILE );
        }

        // the highlighting is parsed. if the colorString is not initialised or
        // the parsing failed the DEFAULT_COLOR_HIGHLIGHTING is taken
        COLOR_HIGHLIGHTING = Utilities.parseColor( colorString, DEFAULT_COLOR_HIGHLIGHTING );
    }

    /**
     * Apply the implemented behaviour to the specific Object of the graph. Here,
     * we change the color of the highlighted object.
     *
     * @param aObject the object on which we want to apply the behaviour
     */
    public void applyBehaviour( Object aObject, Network network ) {
        
        if ( Node.class.isInstance( aObject ) ) {
            Node node = (Node) aObject;
            NodeAttributes attributes = network.getNodeAttributes( node.getId()  );
            if (attributes != null){
                attributes.put( Constants.ATTRIBUTE_COLOR_NODE, COLOR_HIGHLIGHTING );
                attributes.put( Constants.ATTRIBUTE_COLOR_LABEL, COLOR_HIGHLIGHTING );
                logger.info( "Protein (" + node.getLabel() + ") receives this behaviour : " + Constants.ATTRIBUTE_COLOR_LABEL + "-" + COLOR_HIGHLIGHTING );
            } else {
                logger.warn("Could not found NodeAttributes for " + node.getId());
            }
        }

        if ( Edge.class.isInstance( aObject ) ) {
            EdgeAttributes attributes = network.getEdgeAttributes( ( Edge ) aObject );
            if (attributes != null){
                attributes.put( Constants.ATTRIBUTE_COLOR_EDGE, COLOR_HIGHLIGHTING );
                logger.info( "Interaction (" + aObject + ") receives this behaviour : " + Constants.ATTRIBUTE_COLOR_EDGE + "-" + COLOR_HIGHLIGHTING );
            } else {
                logger.warn("Could not found EdgeAttributes for " + ((Edge)aObject).getId());
            }
        }
    }
}