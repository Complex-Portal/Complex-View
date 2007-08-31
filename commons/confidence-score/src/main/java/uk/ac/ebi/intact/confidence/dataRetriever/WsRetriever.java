/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.dataRetriever;

import java.io.Writer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.intact.confidence.model.InteractionSimplified;

/**
 * TODO comment this ... and also implement this class
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>22 Aug 2007</pre>
 */
public class WsRetriever implements DataRetrieverStrategy {

	/**
	 * Sets up a logger for that class.
	 */
	public static final Log				log	= LogFactory.getLog(WsRetriever.class);	
	public List<InteractionSimplified> retrieveHighConfidenceSet() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<InteractionSimplified> retrieveLowConfidenceSet() {
		// TODO Auto-generated method stub
		return null;
	}

	public void retrieveMediumConfidenceSet(Writer osw) {
		// TODO Auto-generated method stub
		
	}

}
