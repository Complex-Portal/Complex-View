/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.dataRetriever;

/**
 * Exception thrown by DataRetrieverStrategy.
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version 1.6.0
 * @since
 * 
 * <pre>
 * 15 Oct 2007
 * </pre>
 */
public class DataRetrieverException extends Exception {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	public DataRetrieverException() {
	}

	public DataRetrieverException(Throwable cause) {
		super(cause);
	}

	public DataRetrieverException(String message) {
		super(message);
	}

	public DataRetrieverException(String message, Throwable cause) {
		super(message, cause);
	}

}
