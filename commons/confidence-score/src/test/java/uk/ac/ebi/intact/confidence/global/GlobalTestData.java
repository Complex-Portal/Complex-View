/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.global;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import uk.ac.ebi.intact.confidence.model.InteractionSimplified;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;

/**
 * TODO comment this
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since
 * 
 * <pre>
 * 22 Aug 2007
 * </pre>
 */
public class GlobalTestData {

	private List<InteractionSimplified>	binaryInteractions;
	private List<InteractionSimplified>	complexInteractions;
	private List<InteractionSimplified>	compelxWithoutBaitInteractions;
	private List<String>				binaryProteins;
	private String						tmpDir	="E:\\tmp\\";//"~/tmp/";
	private String						dataDir	= "E:\\iarmean\\data\\";

	private GlobalTestData() {
		initBinary();
		initComplex();
		initComplexWithoutBait();
	}

	private static class SingletonHolder {
		private final static GlobalTestData	INSTANCE	= new GlobalTestData();
	}

	public static GlobalTestData getInstance() {
		try {
			SingletonHolder.INSTANCE.tmpDir = File.createTempFile("test", "tmp").getPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return SingletonHolder.INSTANCE;
	}

	private void initBinary() {
		ProteinSimplified comp1 = new ProteinSimplified("P12345", "neutral");
		ProteinSimplified comp2 = new ProteinSimplified("Q12345", "neutral");
		ProteinSimplified comp3 = new ProteinSimplified("Q12345", "neutral");
		ProteinSimplified comp4 = new ProteinSimplified("S12345", "neutral");
		InteractionSimplified intS1 = new InteractionSimplified("EBI-1234", Arrays.asList(comp1, comp2));
		InteractionSimplified intS2 = new InteractionSimplified("EBI-2345", Arrays.asList(comp3, comp4));
		binaryProteins = Arrays.asList(comp1.getUniprotAc(), comp3.getUniprotAc(), 
				comp4.getUniprotAc());
		binaryInteractions = Arrays.asList(intS1, intS2);
	}

	private void initComplex() {
		ProteinSimplified comp11 = new ProteinSimplified("P12345", "bait");
		ProteinSimplified comp12 = new ProteinSimplified("Q12345", "neutral");
		ProteinSimplified comp13 = new ProteinSimplified("R12345", "neutral");
		ProteinSimplified comp14 = new ProteinSimplified("S12345", "neutral");

		InteractionSimplified intS11 = new InteractionSimplified("EBI-3456", Arrays.asList(comp11, comp12, comp13,
				comp14));
		comp11.setRole("neutral");
		comp14.setRole("bait");
		InteractionSimplified intS12 = new InteractionSimplified("EBI-4567", Arrays.asList(comp11, comp12, comp13,
				comp14));

		complexInteractions = Arrays.asList(intS11, intS12);
	}

	private void initComplexWithoutBait() {
		ProteinSimplified comp12 = new ProteinSimplified("Q12345", "neutral");
		ProteinSimplified comp13 = new ProteinSimplified("R12345", "neutral");
		ProteinSimplified comp14 = new ProteinSimplified("S12345", "neutral");
		ProteinSimplified comp11 = new ProteinSimplified("P12345", "neutral");
		ProteinSimplified comp111 = new ProteinSimplified("P12345", "neutral");
		ProteinSimplified comp112 = new ProteinSimplified("Q12345", "neutral");
		ProteinSimplified comp113 = new ProteinSimplified("R12345", "neutral");
		InteractionSimplified intS111 = new InteractionSimplified("EBI-5678", Arrays.asList(comp12, comp14, comp11,
				comp113));
		InteractionSimplified intS112 = new InteractionSimplified("EBI-6789", Arrays.asList(comp13, comp11, comp111,
				comp112));

		compelxWithoutBaitInteractions = Arrays.asList(intS111, intS112);
	}

	public List<InteractionSimplified> getBinaryInteractions() {
		return binaryInteractions;
	}

	public List<InteractionSimplified> getComplexInteractions() {
		return complexInteractions;
	}

	public List<InteractionSimplified> getCompelxWithoutBaitInteractions() {
		return compelxWithoutBaitInteractions;
	}

	public String getTmpDir() {
		return tmpDir;
	}

	public String getDataDir() {
		return dataDir;
	}

	public List<String> getBinaryProteins() {
		return binaryProteins;
	}

}
