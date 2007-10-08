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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.intact.bridges.blast.BlastService;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.EbiWsWUBlast;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
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
	private AnnotationFileMaker	annotationMaker;
	private AlignmentFileMaker	alignmentMaker;
	private FileMaker			fileMaker;
	private FileCombiner		fileCombiner;
	private File				workDir;
	private File				uniprotPath;

	private AttributeGetter() {
		annotationMaker = new AnnotationFileMaker();
		fileCombiner = new FileCombiner();
	}

	public void close(){
		alignmentMaker.close();
	}
	
	public AttributeGetter(File dbFolder, String uniprotPath, BinaryInteractionSet highConfSet, File workDir, File blastArchiveDir, String email, int nrPerSubmission) throws BlastServiceException {
		this();
		annotationMaker.setUniprotFile(new File(uniprotPath));
		String tableName = "job";
		//TODO: remove the path
		HashMap<String, File> paths = GlobalData.getRightPahts();
		File workDirBlast =  paths.get("blastArchive");//new File("E:\\20071016_iarmean");
		if (nrPerSubmission < 1){
			nrPerSubmission = 20;
		}
		BlastService bs = new EbiWsWUBlast(dbFolder, tableName, workDirBlast, email, nrPerSubmission);//new File(workDir.getPath(), "/Blast/"), email);
		alignmentMaker = new AlignmentFileMaker(new Float(0.001), blastArchiveDir, bs);
		fileMaker = new FileMaker(highConfSet);
		this.setWorkDir(workDir.getPath());
		this.uniprotPath = new File(uniprotPath);
		this.uniprotPath.mkdir();
	}

	/**
	 * for a given protein pair it gets the GoPair attributs
	 * 
	 * @param proteinPair
	 * @param outPath
	 */
	public void writeGoAttributes(ProteinPair proteinPair, String outPath) {
		BinaryInteractionSet biSet = new BinaryInteractionSet(Arrays.asList(proteinPair));
		AnnotationFileMaker afm = new AnnotationFileMaker(biSet, uniprotPath.getPath());
		try {
			File goFile = new File(workDir.getPath(), "set_go.txt");
			// get GOs per protein
			afm.writeGoAnnotation(proteinPair, goFile);
			// build GO attributes for pair
			fileMaker.writeAnnotationAttributes(goFile.getPath(), outPath);
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
		AnnotationFileMaker afm = new AnnotationFileMaker(biS, uniprotPath.getPath());
		
		//annotationMaker.setAllProts(biS.getAllProtNames());
		try {
			File goFile = new File(workDir.getPath(), "set_go.txt");
			// get GOs
			afm.writeGoAnnotation(goFile.getPath());
			// build GO attributes
			//TODO: solve once + for all the setting of the biSet for the filemaker
			BinaryInteractionSet auxBiSet = fileMaker.getBiSet();
			fileMaker.setBiSet(biS);
			fileMaker.writeAnnotationAttributes(goFile.getPath(), outPath);
			fileMaker.setBiSet(auxBiSet);
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
		BinaryInteractionSet biSet = new BinaryInteractionSet(Arrays.asList(proteinPair));
		AnnotationFileMaker afm = new AnnotationFileMaker(biSet, uniprotPath.getPath());
		try {
			File ipFile = new File(workDir.getPath(), "set_ip.txt");
			// get Interpro domains per protein
			afm.writeInterproAnnotation(proteinPair, ipFile.getPath());
			// build InterPro attributes for pair
			fileMaker.writeAnnotationAttributes(ipFile.getPath(), outPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeIpAttributes(BinaryInteractionSet biS, String outPath) {
		AnnotationFileMaker afm = new AnnotationFileMaker(biS, uniprotPath.getPath());
		annotationMaker.setAllProts(biS.getAllProtNames());
		try {
			File ipFile = new File(workDir.getPath(), "set_ip.txt");
			// get Interpro domains per protein
			afm.writeInterproAnnotation(ipFile.getPath());
			// build InterPro attributes for pair
			BinaryInteractionSet auxSet = fileMaker.getBiSet();
			//TODO: solve once + for all the setting of the biSet for the filemaker
			fileMaker.setBiSet(biS);
			fileMaker.writeAnnotationAttributes(ipFile.getPath(), outPath);
			fileMaker.setBiSet(auxSet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeAlignmentAttributes(ProteinPair proteinPair, String outPath, Set<UniprotAc> againstProt) throws BlastServiceException {
		Set<UniprotAc> proteins = new HashSet<UniprotAc>();
		proteins.addAll(Arrays.asList(new UniprotAc(proteinPair.getFirstId()), new UniprotAc(proteinPair.getSecondId())));
		try {
			File alignFile = new File(workDir.getPath(), "set_align_pp.txt");
			alignmentMaker.blast(proteins, againstProt, new FileWriter(alignFile));
			fileMaker.writeAnnotationAttributes(alignFile.getPath(), outPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeAlignmentAttributes(BinaryInteractionSet biS, String outPath, Set<String> againstProt) throws BlastServiceException {
		Set<UniprotAc> proteins =  getUniprotAc(biS.getAllProtNames());
		Set<UniprotAc> against = getUniprotAc(againstProt);
		try {
			File alignFile = new File(workDir.getPath(), "set_align_biSet.txt");
			alignmentMaker.blast(proteins, against, new FileWriter(alignFile));
			//TODO: solve once + for all the setting of the biSet for the filemaker
			BinaryInteractionSet auxSet = fileMaker.getBiSet();
			fileMaker.setBiSet(biS);
			fileMaker.writeAnnotationAttributes(alignFile.getPath(), outPath);
			fileMaker.setBiSet(auxSet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Set<UniprotAc> getUniprotAc(Set<String> protStr) {
		Set<UniprotAc> proteins =  new HashSet<UniprotAc>(protStr.size());
		for (String ac : protStr) {
			proteins.add(new UniprotAc(ac));				
		}
		return proteins;
	}

	public void merge(String[] paths, String outPath) {
		try {
			//FIXME: merge -> merge2
			fileCombiner.merge2(paths, outPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getAllAttribs(BinaryInteractionSet biS, Set<String> againstProteins, String outPath) throws BlastServiceException {
		String goPath = workDir.getPath() + "/set_go_attributes.txt";
		writeGoAttributes(biS, goPath);
		String ipPath = workDir.getPath() + "/set_ip_attributes.txt";
		writeIpAttributes(biS, ipPath);
		
		String alignPath = workDir.getPath() + "/set_align_attributes.txt";
		writeAlignmentAttributes(biS, alignPath, againstProteins);
		
		String[] paths = { goPath, ipPath, alignPath };
		merge(paths, outPath);
	}

	public void setWorkDir(String workDirPath) {
		File tmpDir = new File(workDirPath, "AttributeGetter");
		tmpDir.mkdir();
		this.workDir = tmpDir;
	}
}
