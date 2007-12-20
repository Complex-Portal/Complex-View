/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.hierarchview.highlightment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.business.image.DrawGraph;
import uk.ac.ebi.intact.application.hierarchview.business.image.ImageBean;
import uk.ac.ebi.intact.application.hierarchview.highlightment.behaviour.HighlightmentBehaviour;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.edge.EdgeHighlightmentSource;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.service.graph.Edge;

import javax.servlet.http.HttpSession;
import java.util.Collection;

/**
 * TODO comment that class header
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class HighlightInteractions {

    private static final Log logger = LogFactory.getLog( HighlightInteractions.class );

    public static void perform( EdgeHighlightmentSource edgeHighlightmentSource, String behaviourClass, HttpSession session, Network in ) {
        /*
         * Put the default color and default visibility in the interaction network before to highlight this one.
         */
        in.initEdges();
        in.initNodes();

        // Search the interactions to highlight
        Collection<Edge> interactionsToHighlight = edgeHighlightmentSource.interactionToHightlight( session, in );

        // Check if the interaction selected is in the selected tab
        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( Constants.USER_KEY );

        // Interaction network's modification
        HighlightmentBehaviour highlightmentBehaviour = HighlightmentBehaviour.getHighlightmentBehaviour( behaviourClass );

        // apply the highlight to the selected set of protein
        logger.info( "Interactions collection to be highlighted : " + interactionsToHighlight );
        highlightmentBehaviour.apply( interactionsToHighlight, in );

        // store data in the session
        if ( user == null ) {
            logger.info( "USER is null, exit the highlight process" );
            return;
        }

        String applicationPath = user.getApplicationPath();

        // Rebuild Image data
        //        GraphToSVG svgProducer = new GraphToSVG (in);
        DrawGraph imageProducer = new DrawGraph( in, applicationPath, user.getMinePath() );
        imageProducer.draw();
        ImageBean ib = imageProducer.getImageBean();

        // TODO : test is user OK
        user.setImageBean( null );
        user.setImageBean( ib );

        // TODO: needed ?! have to be tested !
        user.setInteractionNetwork( in );
    }

    /**
     * Constructor Allow to modify the current graph to highlight a part of this.
     *
     * @param source         The highlighting source
     * @param behaviourClass The highlighting behaviour class name
     * @param session        The current session
     * @param in             The interaction network
     */
    public static void perform( String source, String behaviourClass, HttpSession session, Network in ) {

        // Search the highlight source implementation
        EdgeHighlightmentSource edgeHighlightmentSource = EdgeHighlightmentSource.getHighlightmentSource( source );

        perform( edgeHighlightmentSource, behaviourClass, session, in );
    }
}
