/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.confidence.model.BinaryInteraction;
import uk.ac.ebi.intact.confidence.model.Confidence;
import uk.ac.ebi.intact.confidence.model.Identifier;

import java.util.*;

/**
 * Generates the low confidence set from a given proteom filtering the ones in IntAct.
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version 1.6.0
 * @since
 * 
 * <pre>
 * 14 - Aug - 2007
 * </pre>
 */
public class InteractionGenerator {
	/**
	 * Sets up a logger for that class.
	 */
	public static final Log	log	= LogFactory.getLog(InteractionGenerator.class);

    ///////////////////
    // Constructor(s).
    InteractionGenerator() {}


    /////////////////////
    // Public Method(s).
    public static Set<BinaryInteraction> generate(Set<Identifier> yeastProts, Set<BinaryInteraction> forbidden, int nr){
        if (nr<=0){
            nr = forbidden.size();
        }
        List<Identifier> yeastProtList = new ArrayList<Identifier>(yeastProts);
        List<BinaryInteraction> forbiddenList = new ArrayList<BinaryInteraction>(forbidden);
        List<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>(nr);
        Random random = new Random();

		int i = 0;
		while (i < nr) {
			int index1 = random.nextInt(yeastProtList.size());
			int index2 = random.nextInt(yeastProtList.size());

            if (index1 != index2) {
                // get uniprot ids
                Identifier uniprotId1 = yeastProtList.get( index1 );
                Identifier uniprotId2 = yeastProtList.get( index2 );

                BinaryInteraction auxBin = new BinaryInteraction( uniprotId1, uniprotId2, Confidence.UNKNOWN );
                if ( !forbiddenList.contains( auxBin ) && !interactions.contains( auxBin )) {
                    auxBin.setConfidence( Confidence.LOW );
                    if (log.isTraceEnabled()){
                        log.trace("interaction added: " + auxBin.convertToString());
                    }
                    interactions.add( auxBin );
                    i++;
                }
            }
        }

		return new HashSet<BinaryInteraction>(interactions);
    }
}
