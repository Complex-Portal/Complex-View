/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.dataRetriever;

import java.io.File;

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

	// TODO:  is there not an elegant way to implement the rules for
	// high confidence and low confidence sets at a global level
	public int retrieveHighConfidenceSet(File folder) throws DataRetrieverException;

//    public List<InteractionSimplified> retrieveHighConfidenceSet() throws DataRetrieverException;

    /*
	 * because it is a very high number of medium confidence interactions,
	 * these will be persisted to a file
	 */
	public void retrieveMediumConfidenceSet(File folder) throws DataRetrieverException;

    public void retrieveHighAndMediumConfidenceSet( File folder) throws DataRetrieverException;

}
