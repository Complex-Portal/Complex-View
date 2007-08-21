/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.intact.confidence.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.model.util.AliasUtils;

/**
 * TODO comment this ... someday
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since
 * 
 * <pre>
 * 14 - Aug - 2007
 * </pre>
 */
public class DataMethods {

	/**
	 * Sets up a logger for that class.
	 */
	public static final Log log = LogFactory.getLog(AliasUtils.class);

	private ExpansionStrategy expansionStrategy;

	public ExpansionStrategy getExpansionStrategy() {
		return expansionStrategy;
	}

	public void setExpansionStrategy(ExpansionStrategy expansionStrategy) {
		this.expansionStrategy = expansionStrategy;
	}

	DataMethods() {
	}

	DataMethods(ExpansionStrategy expansionStrategy) {
		this.expansionStrategy = expansionStrategy;
	}

	/*
	 * reads a list of EBI-xxxx accession numbers into a collection
	 */
	public Collection<String> exactRead(String filepath) throws IOException {
		if (filepath == null)
			throw new NullPointerException();

		Collection<String> ebiACs = new ArrayList<String>();

		File file = new File(filepath);
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
			System.out.println("file not found : " + filepath);
		}

		BufferedReader br = new BufferedReader(fr);
		String line = "";
		while ((line = br.readLine()) != null) {
			ebiACs.add(line);
		}

		return ebiACs;
	}

	/*
	 * reads  a list of EBI-xxxx accession nr into a collection of interactions
	 */

	public Collection<InteractionSimplified> read(String filepath) throws IOException {
		Collection<String> ebiACs = exactRead(filepath);
		Collection<InteractionSimplified> interactions = getInteractions(ebiACs);
		return interactions;
	}

	/*
	 * gets the information out of the DB
	 * for each EBI-xxxx it retrievs the uniprotAcs
	 */
	private Collection<InteractionSimplified> getInteractions(Collection<String> ebiACs) {
		return (new IntActDb()).read(ebiACs);
	}

	public Collection<InteractionSimplified> expand(Collection<InteractionSimplified> interactions) {
		Collection<InteractionSimplified> expanded = new ArrayList<InteractionSimplified>();
		for (InteractionSimplified interaction : interactions) {
			expanded.addAll(expansionStrategy.expand(interaction));
		}
		return expanded;
	}

	public Collection<InteractionSimplified> generateLCInteractions(Collection<String> yeastProtACs,
			Collection<InteractionSimplified> highconf, Collection<InteractionSimplified> medconf,
			Collection<InteractionSimplified> lowconf, int nr) {

		InteractionGenerator intGen = new InteractionGenerator();
		intGen.setHighconfidence(highconf);
		intGen.setLowconfidence(lowconf);
		intGen.setMediumconfidence(medconf);
		intGen.setProteinACs(yeastProtACs);

		Collection<InteractionSimplified> generatedLC = intGen.generate(nr);

		return generatedLC;
	}
	
	public void export(Collection<InteractionSimplified> interactions, File file, boolean uniprotAcsOnly){
		try {
			FileWriter fw = new FileWriter(file);
	        PrintWriter pw = new PrintWriter(fw);
	       
			for (InteractionSimplified item : interactions) {
				
				if(item.getInteractors().size() != 2) {
					log.debug("interaction : "+item.getAc() + " is not binary!!!");
				}
				
				StringBuilder sb = new StringBuilder();
				if (!uniprotAcsOnly)
					sb.append(item.getAc()+": ");
				ProteinSimplified prot1 = (ProteinSimplified) item.getInteractors().toArray()[0];
				ProteinSimplified prot2 = (ProteinSimplified) item.getInteractors().toArray()[1];
				sb.append(prot1.getUniprotAc() +"," + prot2.getUniprotAc());
				
				
				
				pw.println(sb.toString());
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/*
	 *  reads the uniprotAcs for yeast, out of a fasta file and writes them to an outFile if specified 
	 */
	public Collection<String> readFasta(File inFile, File outFile){
		Collection<String> proteins = new ArrayList<String>();
		
		try {
			FileReader fr = new FileReader(inFile);
			BufferedReader br = new BufferedReader(fr);
			String line ="";
			String uniprotAc = "^>(\\w{6}).*";
			while((line = br.readLine()) != null){
				  if (Pattern.matches(uniprotAc, line)){
					  String[] pices = line.split("\\W+"); // split by non word characters
					  log.info("found uniportAc:	" +pices[1] +"\n");
					  proteins.add(pices[1]);
				  }
				
			}		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//writing it to outFile
		if (outFile !=null && proteins.size() !=0){
				writeToFile(proteins, outFile);
		}
		
		return proteins;
	}
	
	private void writeToFile(Collection<String> proteins, File outFile) {
		try {
			FileWriter fw = new FileWriter(outFile);
	        PrintWriter pw = new PrintWriter(fw);
	       
	        for (String uniprotAc : proteins) {
				pw.println(uniprotAc);
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
	}

	//TODO: remove after test-phase
	public static void main(String[] args) {
		File inFile = new File("E:\\iarmean\\dataHC\\yeastSmall.fasta");
		(new DataMethods()).readFasta(inFile, null);
		System.out.println("done.");
	}
}
