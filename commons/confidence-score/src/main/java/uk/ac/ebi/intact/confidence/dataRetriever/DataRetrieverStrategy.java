/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.dataRetriever;

import java.io.Writer;

/**
 * Defines a data retrieving strategy
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since
 * 
 * <pre>
 * 22 Aug 2007
 * </pre>
 */
public interface DataRetrieverStrategy {

	// TODO: ask if there is not an elegant way to implement the rules for
	// high confidence and low confidence sets at a global level
	public void retrieveHighConfidenceSet(Writer w) throws DataRetrieverException;
	
	/*
	 * because it is a very high number of medium confidence interactions,
	 * these will be persisted to a file
	 */
	public void retrieveMediumConfidenceSet(Writer w) throws DataRetrieverException;
}
