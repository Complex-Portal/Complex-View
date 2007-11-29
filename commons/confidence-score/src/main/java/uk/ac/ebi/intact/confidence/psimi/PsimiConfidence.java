/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.psimi;

import opennlp.maxent.GISModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.tab.model.ConfidenceImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntActColumnHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * writes to the psimi-tab file the score values
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @since <pre>28 Aug 2007</pre>
 */
public class PsimiConfidence {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( PsimiConfidence.class );

    private File workDir;

    //	private Collection<BinaryInteraction> psimiInts;
    private GISModel classifier;
    private HashSet<String> againstProteins; //for blast
//	//TODO: remove this tmpDir member
//	private static String tmpDir = "E:\\tmp\\";

    //TODO:remove after test
     public PsimiConfidence(){
         
     }

    public PsimiConfidence(String hcSetPath, String lcSetPath, File workDir) {
        this.workDir = workDir;

    }

    public void appendConfidence( String hcSetPath, String lcSetPath, File inPsiMiFile, boolean hasHeaderLine, File outPsiMiFile ) {
  

        PsimiTabReader reader = new PsimiTabReader( hasHeaderLine );
        reader.setBinaryInteractionClass( IntActBinaryInteraction.class );
        reader.setColumnHandler( new IntActColumnHandler() );
        try {
            Collection<BinaryInteraction> psiMiInts = reader.read( inPsiMiFile );
            saveScores( psiMiInts );
            writeScores( psiMiInts, hasHeaderLine, outPsiMiFile );
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( ConverterException e ) {
            e.printStackTrace();
        }
    }

    protected void saveScores( Collection<BinaryInteraction> psiMiInts ) {
        for ( Iterator<BinaryInteraction> iter = psiMiInts.iterator(); iter.hasNext(); ) {
            BinaryInteraction interaction = iter.next();
            String [] attribs = getAttributes(interaction);
            double [] scores = classifier.eval( attribs);
            save(interaction, classifier.eval( attribs));

        }
    }

    private String[] getAttributes( BinaryInteraction interaction ) {
       String [] acs = getUniprotAcs(interaction);
//        try {
//         //  AttributeGetterFastaFile ag = new AttributeGetterFastaFile( );
//        } catch ( BlastServiceException e ) {
//            e.printStackTrace();
//        }

        return new String[0];
    }

    private String[] getUniprotAcs( BinaryInteraction interaction ) {
        String uniprotA ="";
        Collection<CrossReference> xrefsA= interaction.getInteractorA().getIdentifiers();
        for ( Iterator<CrossReference> crossReferenceIterator = xrefsA.iterator(); crossReferenceIterator.hasNext(); ) {
            CrossReference crossReference =  crossReferenceIterator.next();
            if (crossReference.getDatabase().equalsIgnoreCase( "uniprotkb")){
                uniprotA = crossReference.getIdentifier();
            }
        }
        String uniprotB ="";
        Collection<CrossReference> xrefsB= interaction.getInteractorB().getIdentifiers();
        for ( Iterator<CrossReference> crossReferenceIterator = xrefsB.iterator(); crossReferenceIterator.hasNext(); ) {
            CrossReference crossReference =  crossReferenceIterator.next();
            if (crossReference.getDatabase().equalsIgnoreCase( "uniprotkb")){
                uniprotB = crossReference.getIdentifier();
            }
        }
        if (uniprotA.equalsIgnoreCase( "") || uniprotB.equalsIgnoreCase( "")){
            return new String[0];
        } else {
            String [] result = {uniprotA, uniprotB};
            return result;
        }



//            if (xrefsA.size() != 1){
//				log.debug("xrefs contains more than 1 CrossReference");
//			}
//			CrosssReference xrefA = (CrossReference)xrefsA.toArray()[0];
//			String ac1 = xrefA.getIdentifier();
//
//			//get interactorB ac
//			Collection<CrossReference> xrefsB= binaryInteraction.getInteractorB().getIdentifiers();
//			if (xrefsB.size() != 1){
//				log.debug("xrefs contains more than 1 CrossReference");
//			}
//			CrossReference xrefB = (CrossReference)xrefsB.toArray()[0];
//			String ac2 = xrefB.getIdentifier();
//
//			if (uniprot1.equals(ac1) && uniprot2.equals(ac2)){
    }

    private void save( BinaryInteraction interaction, double[] scores ) {
        List<Confidence> confVals= new ArrayList<Confidence>(scores.length);
        Confidence conf1 = new ConfidenceImpl("high", Double.toString( scores [classifier.getIndex( "high")]));
        Confidence conf2 = new ConfidenceImpl("low", Double.toString( scores [classifier.getIndex( "low")]));
        confVals.add(conf1); confVals.add(conf2);
        interaction.setConfidenceValues(confVals);
    }


    private void writeScores( Collection<BinaryInteraction> psiMiInts, boolean hasHeaderLine, File outPsiMiFile ) {
        PsimiTabWriter writer = new PsimiTabWriter();
        writer.setHeaderEnabled( hasHeaderLine );
        writer.setBinaryInteractionClass( IntActBinaryInteraction.class );
        writer.setColumnHandler( new IntActColumnHandler() );
        try {
            writer.write( psiMiInts, outPsiMiFile );
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( ConverterException e ) {
            e.printStackTrace();
        }
    }

    //	private void save(String uniprot1, String uniprot2, Double score) {
//		for (BinaryInteraction binaryInteraction : psimiInts) {
//			Collection<CrossReference> xrefsA= binaryInteraction.getInteractorA().getIdentifiers();
//			if (xrefsA.size() != 1){
//				log.debug("xrefs contains more than 1 CrossReference");
//			}
//			CrosssReference xrefA = (CrossReference)xrefsA.toArray()[0];
//			String ac1 = xrefA.getIdentifier();
//
//			//get interactorB ac
//			Collection<CrossReference> xrefsB= binaryInteraction.getInteractorB().getIdentifiers();
//			if (xrefsB.size() != 1){
//				log.debug("xrefs contains more than 1 CrossReference");
//			}
//			CrossReference xrefB = (CrossReference)xrefsB.toArray()[0];
//			String ac2 = xrefB.getIdentifier();
//
//			if (uniprot1.equals(ac1) && uniprot2.equals(ac2)){
//				Confidence conf = new ConfidenceImpl("%", score.toString());
//
//				log.info("confValue.size: "+ binaryInteraction.getConfidenceValues().size());
//				List<Confidence> confs = new ArrayList<Confidence>();
//				confs = Arrays.asList(conf);
//				binaryInteraction.setConfidenceValues(confs);
//			}
//		}
//
//	}


    public static void main( String[] args ) {
        PsimiConfidence psimi = new PsimiConfidence();
    }

    //////TODO: remove all if i do not need them
//    public PsimiConfidence( GISModel maxEntClassifier, HashSet<String> hcProteins){
//		classifier = maxEntClassifier;
//		againstProteins = hcProteins;
//	}
//
//
//
//	private void computeScore(Collection<BinaryInteraction> psimiInts) throws BlastServiceException, IOException {
//		BinaryInteractionSet biS = getBiSet(psimiInts);
//		int nr = -2;
//		File dbFolder = new File(tmpDir,"dbFolder");
//		dbFolder.mkdir();
//		AttributeGetterFastaFile  aG = new AttributeGetterFastaFile(dbFolder,tmpDir + "uniprot_sprot.dat", null, null, null, null, nr);
//		String outPath = tmpDir + "psimi_all_attributes.txt";
//		aG.getAllAttribs(biS, againstProteins, outPath, null);
//		//TODO: read the all attribs, and for each do a getAttribs per line => score
//		FileReader fr;
//		try {
//			fr = new FileReader(new File(outPath));
//			BufferedReader br =  new BufferedReader(fr);
//			String line = "";
//			while((line = br.readLine())!= null){
//				HashSet<Attribute> attributs = FileMethods.parseAttributeLine(line);
//				 Double[] probs = classifier.probs(attributs);
//				 Double tScore = probs[0];
//
//				 String ac1 ="";
//				 String ac2="";
//				 save(ac1, ac2, tScore);
//				 // save this score to the psi- mi- format
//			}
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	private void save(String uniprot1, String uniprot2, Double score) {
//		for (BinaryInteraction binaryInteraction : psimiInts) {
//			Collection<CrossReference> xrefsA= binaryInteraction.getInteractorA().getIdentifiers();
//			if (xrefsA.size() != 1){
//				log.debug("xrefs contains more than 1 CrossReference");
//			}
//			CrossReference xrefA = (CrossReference)xrefsA.toArray()[0];
//			String ac1 = xrefA.getIdentifier();
//
//			//get interactorB ac
//			Collection<CrossReference> xrefsB= binaryInteraction.getInteractorB().getIdentifiers();
//			if (xrefsB.size() != 1){
//				log.debug("xrefs contains more than 1 CrossReference");
//			}
//			CrossReference xrefB = (CrossReference)xrefsB.toArray()[0];
//			String ac2 = xrefB.getIdentifier();
//
//			if (uniprot1.equals(ac1) && uniprot2.equals(ac2)){
//				Confidence conf = new ConfidenceImpl("%", score.toString());
//
//				log.info("confValue.size: "+ binaryInteraction.getConfidenceValues().size());
//				List<Confidence> confs = new ArrayList<Confidence>();
//				confs = Arrays.asList(conf);
//				binaryInteraction.setConfidenceValues(confs);
//			}
//		}
//
//	}
//
//	private BinaryInteractionSet getBiSet(Collection<BinaryInteraction> interactions) {
//		Collection<ProteinPair> proteinPairs = new ArrayList<ProteinPair>();
//		for (BinaryInteraction binaryInteraction : interactions) {
//
//			// get interactioA ac
//			Collection<CrossReference> xrefsA= binaryInteraction.getInteractorA().getIdentifiers();
//			if (xrefsA.size() != 1){
//				log.debug("xrefs contains more than 1 CrossReference");
//			}
//			CrossReference xrefA = (CrossReference)xrefsA.toArray()[0];
//			String ac1 = xrefA.getIdentifier();
//
//			//get interactorB ac
//			Collection<CrossReference> xrefsB= binaryInteraction.getInteractorB().getIdentifiers();
//			if (xrefsB.size() != 1){
//				log.debug("xrefs contains more than 1 CrossReference");
//			}
//			CrossReference xrefB = (CrossReference)xrefsB.toArray()[0];
//			String ac2 = xrefB.getIdentifier();
//
//			ProteinPair pp = new ProteinPair(ac1, ac2);
//			proteinPairs.add(pp);
//		}
//
//		BinaryInteractionSet biS = new BinaryInteractionSet(proteinPairs);
//
//		return biS;
//	}
}
