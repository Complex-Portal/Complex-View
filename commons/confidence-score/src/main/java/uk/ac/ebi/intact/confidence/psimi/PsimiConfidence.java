/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.psimi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.ConfidenceImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.FileMethods;
import uk.ac.ebi.intact.confidence.MaxEntClassifier;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.attribute.Attribute;
import uk.ac.ebi.intact.confidence.util.AttributeGetter;

/**
 * writes to the psimi-tab file the score values
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>28 Aug 2007</pre>
 */
public class PsimiConfidence {
	/**
	 * Sets up a logger for that class.
	 */
	public static final Log				log	= LogFactory.getLog(PsimiConfidence.class);
	private PsimiTabReader reader;
	private Collection<BinaryInteraction> psimiInts;
	private MaxEntClassifier classifier;
	private HashSet<String> againstProteins; //for blast
	//TODO: remove this tmpDir member
	private static String tmpDir = "E:\\tmp\\";
	
	public PsimiConfidence(MaxEntClassifier  maxEntClassifier, HashSet<String> hcProteins){
		classifier = maxEntClassifier;
		againstProteins = hcProteins;
	}
	
	public void appendConfidencee(File inFile, boolean hasHeaderLine, File outFile){
		
		try {
			psimiInts= reader.read(inFile);		
			computeScore(psimiInts);
			//saveToPsimi();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConverterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void computeScore(Collection<BinaryInteraction> psimiInts) {
		BinaryInteractionSet biS = getBISet(psimiInts);
		AttributeGetter  aG = new AttributeGetter(tmpDir + "uniprot_sprot.dat", null);
		String outPath = tmpDir + "psimi_all_attributes.txt";
		aG.getAllAttribs(biS, againstProteins, outPath);
		//TODO: read the all attribs, and for each do a getAttribs per line => score
		FileReader fr;
		try {
			fr = new FileReader(new File(outPath));
			BufferedReader br =  new BufferedReader(fr);
			String line = "";
			while((line = br.readLine())!= null){
				HashSet<Attribute> attributs = FileMethods.parseAttributeLine(line);
				 Double[] probs = classifier.probs(attributs);
				 Double tScore = probs[0];
				 
				 String ac1 ="";
				 String ac2="";
				 save(ac1, ac2, tScore);
				 // save this score to the psi- mi- format
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void save(String uniprot1, String uniprot2, Double score) {
		for (BinaryInteraction binaryInteraction : psimiInts) {
			Collection<CrossReference> xrefsA= binaryInteraction.getInteractorA().getIdentifiers();
			if (xrefsA.size() != 1){
				log.debug("xrefs contains more than 1 CrossReference");
			}
			CrossReference xrefA = (CrossReference)xrefsA.toArray()[0];
			String ac1 = xrefA.getIdentifier();
			
			//get interactorB ac
			Collection<CrossReference> xrefsB= binaryInteraction.getInteractorB().getIdentifiers();
			if (xrefsB.size() != 1){
				log.debug("xrefs contains more than 1 CrossReference");
			}
			CrossReference xrefB = (CrossReference)xrefsB.toArray()[0];
			String ac2 = xrefB.getIdentifier();
			
			if (uniprot1.equals(ac1) && uniprot2.equals(ac2)){
				Confidence conf = new ConfidenceImpl("%", score.toString());
	
				log.info("confValue.size: "+ binaryInteraction.getConfidenceValues().size());
				List<Confidence> confs = new ArrayList<Confidence>();
				confs = Arrays.asList(conf);
				binaryInteraction.setConfidenceValues(confs);
			}
		}
		
	}

	private BinaryInteractionSet getBISet(Collection<BinaryInteraction> interactions) {
		Collection<ProteinPair> proteinPairs = new ArrayList<ProteinPair>();
		for (BinaryInteraction binaryInteraction : interactions) {
		
			// get interactioA ac
			Collection<CrossReference> xrefsA= binaryInteraction.getInteractorA().getIdentifiers();
			if (xrefsA.size() != 1){
				log.debug("xrefs contains more than 1 CrossReference");
			}
			CrossReference xrefA = (CrossReference)xrefsA.toArray()[0];
			String ac1 = xrefA.getIdentifier();
			
			//get interactorB ac
			Collection<CrossReference> xrefsB= binaryInteraction.getInteractorB().getIdentifiers();
			if (xrefsB.size() != 1){
				log.debug("xrefs contains more than 1 CrossReference");
			}
			CrossReference xrefB = (CrossReference)xrefsB.toArray()[0];
			String ac2 = xrefB.getIdentifier();
			
			ProteinPair pp = new ProteinPair(ac1, ac2);
			proteinPairs.add(pp);
		}
		
		BinaryInteractionSet biS = new BinaryInteractionSet(proteinPairs);
		
		return biS;
	}
}
