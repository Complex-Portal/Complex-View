/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.dataRetriever;

import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * DataRetriever strategy using the web service.
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
	public int retrieveHighConfidenceSet(Writer w) {
		// TODO: implement method
        return 0;
    }

	public void retrieveMediumConfidenceSet(Writer osw) {
		// TODO: implement method
		
	}

}
