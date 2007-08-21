/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.expansion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.intact.confidence.model.ProteinSimplified;
import uk.ac.ebi.intact.confidence.model.InteractionSimplified;


import java.util.*;

/**
 * Process an interaction and expand it using the spoke model. Whenever no bait can be found we select an arbitrary
 * bait (1st one by alphabetical order based on the interactor shortlabel) and build the spoke interactions based on
 * that fake bait.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>13-Oct-2006</pre>
 */
public class SpokeWithoutBaitExpansion extends SpokeExpansion {

    public static final String BAIT_MI_REF = "MI:0496";

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( SpokeWithoutBaitExpansion.class );

    ///////////////////////////////////////////
    // Implements ExpansionStrategy contract

    /**
     * Interaction having more than 2 participants get split following the spoke model expansion. That is, we build
     * pairs of participant following bait-prey and enzyme-target associations.
     *
     * @param interaction a non null interaction.
     * @return a non null collection of interaction, in case the expansion is not possible, we may return an empty
     *         collection.
     */
    public Collection<InteractionSimplified> expand(InteractionSimplified interaction) {

        Collection<InteractionSimplified> interactions = new ArrayList<InteractionSimplified>();

        if ( isBinary( interaction ) ) {

            log.debug( "interaction " + interaction.getAc() + "/" +  " was binary, no further processing involved." );
            interactions.add( interaction );

        } else {

            // split interaction
            Collection<ProteinSimplified> participants = interaction.getInteractors();
            log.debug( participants.size() + " participant(s) found." );

            ProteinSimplified bait = searchBaitParticipant( participants );
            if ( bait != null ) {
                Collection<ProteinSimplified> preys = new ArrayList<ProteinSimplified>( participants.size() - 1 );
                preys.addAll( participants );
                preys.remove( bait );

                for ( ProteinSimplified prey : preys ) {

                    if ( log.isDebugEnabled() ) {
                        String baitStr = displayParticipant( bait );
                        String preyStr = displayParticipant( prey );
                        log.debug( "Build new binary interaction [" + baitStr + "," + preyStr + "]" );
                    }

                    InteractionSimplified i = buildInteraction( interaction, bait, prey );
                    interactions.add( i );
                }
            } else {
                // bait was null
                log.debug( "Could not find a bait participant. Pick a participant arbitrarily: 1st by alphabetical order." );

                // Collect and sort participants by name
                List<ProteinSimplified> sortedParticipants = sortParticipants( participants );

                // Pick the first one
                ProteinSimplified fakeBait = sortedParticipants.get( 0 );

                // Build interactions
                for ( int i = 1; i < sortedParticipants.size(); i++ ) {
                	ProteinSimplified fakePrey = sortedParticipants.get( i );

                    if ( log.isDebugEnabled() ) {
                        String baitStr = displayParticipant( fakeBait );
                        String preyStr = displayParticipant( fakePrey );
                        log.debug( "Build new binary interaction [" + baitStr + "," + preyStr + "]" );
                    }

                    InteractionSimplified spokeInteraction = buildInteraction( interaction, fakeBait, fakePrey );
                    interactions.add( spokeInteraction );
                }
            }

            log.debug( "After expansion: " + interactions.size() + " binary interaction(s) were generated." );
        }

        return interactions;
    }

    ////////////////////////////
    // Private methods

    /**
     * Sort a Collection of Participant based on their uniprotid.
     * @param participants collection to sort.
     * @return a non null List of Participant.
     */
    protected List<ProteinSimplified> sortParticipants(Collection<ProteinSimplified> participants) {

        List<ProteinSimplified> sortedParticipants = new ArrayList<ProteinSimplified>( participants );

        Collections.sort( sortedParticipants, new Comparator<ProteinSimplified>() {
            public int compare(ProteinSimplified p1, ProteinSimplified p2) {
                if( p1 == null ) {
                    throw new IllegalArgumentException( "Both participant should hold a valid interactor." );
                }
                if( p2 == null ) {
                    throw new IllegalArgumentException( "Both participant should hold a valid interactor." );
                }

                String name1 = p1.getUniprotAc();
                String name2 = p2.getUniprotAc();

                int result;
                if ( name1 == null ) {
                    result = -1;
                } else if ( name2 == null ) {
                    result = 1;
                } else {
                    result = name1.compareTo( name2 );
                }

                return result;
            }
        } );

        return sortedParticipants;
    }
}