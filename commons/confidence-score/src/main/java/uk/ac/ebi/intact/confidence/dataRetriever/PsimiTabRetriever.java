/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.dataRetriever;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.converter.ConverterException;

import uk.ac.ebi.intact.confidence.model.InteractionSimplified;

/**
 * TODO comment this ... and also implement this class
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
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
	private List<InteractionSimplified> lowConfidence;

	public PsimiTabRetriever(File file, boolean hasHeaderLine){
		psimiTabReader = new PsimiTabReader(hasHeaderLine);
		try {
			binaryInts = psimiTabReader.read(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConverterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<InteractionSimplified> retrieveHighConfidenceSet() {
		if(highConfidence == null){
			OutputStream os;
			try {
				os = new FileOutputStream("medConf.txt");
				retrieveMediumConfidenceSet(new OutputStreamWriter(os));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return highConfidence;
	}

	public List<InteractionSimplified> retrieveLowConfidenceSet() {
		if(lowConfidence == null){
			OutputStream os;
			try {
				os = new FileOutputStream("medConf.txt");
				retrieveMediumConfidenceSet(new OutputStreamWriter(os));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return lowConfidence;
	}

	public void retrieveMediumConfidenceSet(Writer osw) {
//			for (BinaryInteraction interaction : binaryInts) {
//				//TODO: ask Sam about the psimi model for the interaction
//			}
	}
	

}
