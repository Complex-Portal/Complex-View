/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.attribute.AlignmentFileMaker;
import uk.ac.ebi.intact.confidence.attribute.AnnotationFileMaker;
import uk.ac.ebi.intact.confidence.attribute.FileCombiner;
import uk.ac.ebi.intact.confidence.attribute.FileMaker;

/**
 * TODO comment this
 * 
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since
 * 
 * <pre>
 * 28 Aug 2007
 * </pre>
 */
public class AttributeGetter {
	private AnnotationFileMaker		annotationMaker;
	private AlignmentFileMaker		alignmentMaker;
	private FileMaker				fileMaker;
	private FileCombiner			fileCombiner;
	private String					tmpDir	= "E:\\tmp\\";

	private AttributeGetter() {
		annotationMaker = new AnnotationFileMaker();
		alignmentMaker = new AlignmentFileMaker();
		fileCombiner = new FileCombiner();
	}

	public AttributeGetter(String path, BinaryInteractionSet highConfSet){
		this();
		annotationMaker.setUniprotFile(new File(path));
		fileMaker = new FileMaker(highConfSet);
	}
	
	/**
	 * for a given protein pair it gets the GoPair attributs
	 * 
	 * @param proteinPair
	 * @param outPath
	 */
	public void writeGoAttributes(ProteinPair proteinPair, String outPath) {
		try {
			String tmpOut = tmpDir + "set_go.txt";
			// get GOs per protein
			annotationMaker.writeGoAnnotation(proteinPair, tmpOut);
			// build GO attributes for pair
			fileMaker.writeAnnotationAttributes(tmpOut, outPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * for a given binaryInteractionSet it gets the GoPair attributs
	 * 
	 * @param biS
	 * @param outPath
	 */
	public void writeGoAttributes(BinaryInteractionSet biS, String outPath) {
		annotationMaker.setAllProts(biS.getAllProtNames());
		try {
			String tmpOut = tmpDir + "set_go.txt";
			// get GOs
			annotationMaker.writeGoAnnotation(outPath);
			// build GO attributes
			fileMaker.writeAnnotationAttributes(tmpOut, outPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * for a given protein pair it gets the IpAttributes
	 * 
	 * @param proteinPair
	 * @param outPath
	 */
	public void writeIpAttributes(ProteinPair proteinPair, String outPath) {
		try {
			String tmpOut = tmpDir + "set_ip.txt";
			// get Interpro domains per protein
			annotationMaker.writeInterproAnnotation(proteinPair, tmpOut);
			// build InterPro attributes for pair
			fileMaker.writeAnnotationAttributes(tmpOut, outPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeIpAttributes(BinaryInteractionSet biS, String outPath){
		annotationMaker.setAllProts(biS.getAllProtNames());
		try {
			String tmpOut = tmpDir + "set_ip.txt";
			// get Interpro domains per protein
			annotationMaker.writeInterproAnnotation(tmpOut);
			// build InterPro attributes for pair
			fileMaker.writeAnnotationAttributes(tmpOut, outPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeAlignmentAttributes(ProteinPair proteinPair, String outPath, HashSet<String> againstProt) {
		HashSet<String> proteins =  new HashSet<String>();
		proteins.addAll(Arrays.asList(proteinPair.getFirstId(), proteinPair.getSecondId()));
		try {
			alignmentMaker.blast(proteins, againstProt, new FileWriter(new File(outPath)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeAlignmentAttributes(BinaryInteractionSet biS, String outPath, HashSet<String> againstProt){
		HashSet<String> proteins =  biS.getAllProtNames();
		try {
			alignmentMaker.blast(proteins, againstProt, new FileWriter(new File(outPath)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void merge(String[] paths, String outPath) {
		try {
			fileCombiner.merge(paths, outPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getAllAttribs(BinaryInteractionSet biS,HashSet<String> againstProteins, String outPath){
		String goPath = tmpDir + "set_go_attributes.txt";
		writeGoAttributes(biS, goPath);
		String ipPath = tmpDir + "set_ip_attributes.txt";
		writeIpAttributes(biS, ipPath);
		String alignPath = tmpDir + "set_align_attributes.txt";
		writeAlignmentAttributes(biS, alignPath, againstProteins);
		String[] paths = {goPath, ipPath, alignPath};
		merge(paths, outPath);
	}

	public void setTmpDir(String tmpDir) {
		this.tmpDir = tmpDir;
	}
}
