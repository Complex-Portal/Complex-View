/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.dataRetriever;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.intact.confidence.model.InteractionSimplified;
import uk.ac.ebi.intact.confidence.model.ProteinAnnotation;

import java.io.*;
import java.util.Collection;
import java.util.List;

/**
 * DataRetriever strategy out of a PSI- MI TAB file.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version  1.0
 * @since <pre>22 Aug 2007</pre>
 */
public class PsimiTabRetriever implements DataRetrieverStrategy {
	
	/**
	 * Sets up a logger for that class.
	 */
	public static final Log				log	= LogFactory.getLog(PsimiTabRetriever.class);
	private PsimiTabReader psimiTabReader;
	private Collection<BinaryInteraction> binaryInts;
	private List<InteractionSimplified> highConfidence;

	public PsimiTabRetriever(File file, boolean hasHeaderLine) throws DataRetrieverException {
		psimiTabReader = new PsimiTabReader(hasHeaderLine);
		try {
			binaryInts = psimiTabReader.read(file);
		} catch (IOException e) {
			throw new DataRetrieverException( e);
		} catch (ConverterException e) {
			throw new DataRetrieverException( e);
		}
	}

	public int retrieveHighConfidenceSet(File folder) throws DataRetrieverException {
		if(highConfidence == null){
			OutputStream os;
			try {
				os = new FileOutputStream("medConf.txt");
				retrieveMediumConfidenceSet(folder);
			} catch (FileNotFoundException e) {
				throw new DataRetrieverException( e);
			}
		}
        return highConfidence.size();
    }

    public List<InteractionSimplified> retrieveHighConfidenceSet() throws DataRetrieverException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void retrieveMediumConfidenceSet(File folder) {
//			for (BinaryInteraction interaction : binaryInts) {
//				//TODO: ask Sam about the psimi model for the interaction
//			}
	}

    public void retrieveHighConfidenceSet( List<uk.ac.ebi.intact.confidence.model.BinaryInteraction> binaryInts, List<ProteinAnnotation> annotations ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void retrieveHighAndMediumConfidenceSet( File folder ) throws DataRetrieverException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
