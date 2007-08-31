/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.expansion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;
import uk.ac.ebi.intact.confidence.model.InteractionSimplified;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Process an interaction and expand it using the spoke model.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>13-Oct-2006</pre>
 */
public class SpokeExpansion extends BinaryExpansionStrategy {

    public static final String BAIT_MI_REF = "MI:0496";

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( SpokeExpansion.class );

    ///////////////////////////////////////////
    // Implements ExpansionStrategy contract

    /**
     * Interaction having more than 2 participants get split following the spoke model expansion. That is, we build
     * pairs of participant following bait-prey and enzyme-target associations.
     *
     * @param interaction a non null interaction.
     *
     * @return a non null collection of interaction, in case the expansion is not possible, we may return an empty
     *         collection.
     */
    public Collection<InteractionSimplified> expand( InteractionSimplified interaction ) {

        Collection<InteractionSimplified> interactions = new ArrayList<InteractionSimplified>();

        if ( isBinary( interaction ) ) {

            log.debug( "interaction " + interaction.getAc() + "/"  + " was binary, no further processing involved." );
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
                log.debug( "Could not find a bait participant. No further processing involved." );
            }

            log.debug( "After expansion: " + interactions.size() + " binary interaction(s) were generated." );
        }

        return interactions;
    }

    ////////////////////////////
    // Private methods

    protected String displayParticipant( ProteinSimplified p ) {
        // fetch role
        String name = p.getRole();      
        // fetch interactor
        String interactor = p.getUniprotAc();

        return interactor + ":" + name;
    }

    protected boolean isBait( ProteinSimplified participant ) {
        if ( participant == null ) {
            throw new IllegalArgumentException( "Participant must not be null." );
        }

        log.debug( "Checking if participant (ac:" + participant.getUniprotAc() + ") is a bait." );
        if ( participant.getRole().equals("bait")) { 
        	log.debug( "Yes it is." );
            return true;
         }
        else
        	log.debug( "No it is not." );

        return false;
    }

    protected ProteinSimplified searchBaitParticipant( Collection<ProteinSimplified> participants ) {
        if ( participants == null ) {
            throw new IllegalArgumentException( "Participants must not be null." );
        }

        for ( ProteinSimplified participant : participants ) {
            if ( isBait( participant ) ) {
                return participant;
            }
        }

        return null;
    }
    
    public BinaryInteractionSet expand2(InteractionSimplified interaction) {
		Collection<InteractionSimplified> interactions =  expand(interaction);
		Collection<ProteinPair> proteinPairs = new HashSet<ProteinPair>();
		for (InteractionSimplified intS : interactions) {
			if (intS.getInteractors().size() != 2){
				log.debug("binary interaction expected!!");
			}
			ProteinSimplified prot1 = (ProteinSimplified)intS.getInteractors().toArray()[0];
			ProteinSimplified prot2 = (ProteinSimplified)intS.getInteractors().toArray()[1];
			ProteinPair pp = new ProteinPair(prot1.getUniprotAc(), prot2.getUniprotAc());
			proteinPairs.add(pp);
		}
		return new BinaryInteractionSet(proteinPairs);
	}
}