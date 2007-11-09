/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.attribute.ClassifierInputWriter;
import uk.ac.ebi.intact.confidence.dataRetriever.DataRetrieverException;
import uk.ac.ebi.intact.confidence.dataRetriever.IntactDbRetriever;
import uk.ac.ebi.intact.confidence.dataRetriever.uniprot.UniprotDataRetriever;
import uk.ac.ebi.intact.confidence.expansion.SpokeExpansion;
import uk.ac.ebi.intact.confidence.model.GoId;
import uk.ac.ebi.intact.confidence.model.InterProId;
import uk.ac.ebi.intact.confidence.util.AttributeGetter;
import uk.ac.ebi.intact.confidence.util.DataMethods;

import java.io.*;
import java.util.Collection;
import java.util.Set;

/**
 * Class for scoring the interactions in intact
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @since <pre>
 *               29 Aug 2007
 *               </pre>
 */
public class ConfidenceModel {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( ConfidenceModel.class );
    private String uniprotPath;
    private MaxEntClassifier classifier;
    private File workDir;
    private File blastArchiveDir;
    private File dbFolder;
    private String email;
    private int nrPerSubmission;

    public ConfidenceModel() {
    }

    public ConfidenceModel( String dbFolderPath, String uniprotSwissprotPath, String workDirPath,
                            String blastArchivePath, String email, int nrPerSubmission ) {
        if ( uniprotSwissprotPath == null || dbFolderPath == null ) {
            throw new NullPointerException();
        }
        dbFolder = new File( dbFolderPath );
        dbFolder.mkdir();
        if ( log.isInfoEnabled() ) {
            log.info( "dbFolder: " + dbFolder.getPath() );
        }
        uniprotPath = uniprotSwissprotPath;

        workDir = new File( workDirPath, "ConfidenceModel" );
        workDir.mkdir();

        blastArchiveDir = new File( blastArchivePath );
        blastArchiveDir.mkdir();

        this.email = email;
        testDir( dbFolder );
        testDir( workDir );
        testDir( blastArchiveDir );
        this.nrPerSubmission = nrPerSubmission;
    }

    /**
     * gets data from db, generates LC set, gets the attributes, createsTadm
     * input, runs Tadm, trains the model , classifies the medium confidence set
     */
    public void buildModel() {
        long start = System.currentTimeMillis();
        getConfidenceListsFromDb();
        long aux1 = System.currentTimeMillis();
        long timeDb = aux1 - start;
        log.info( "time for db retrieve (milisec): " + timeDb );

        aux1 = System.currentTimeMillis();
        generateLowconf( 10000 );
        long aux2 = System.currentTimeMillis();
        long timeGenerate = aux2 - aux1;
        if ( log.isInfoEnabled() ) {
            log.info( "time for generating lowconf (milisec): " + timeGenerate );
        }

        getIpGoSeqLowconf();

        aux1 = System.currentTimeMillis();
        getInterProGoAndAlign();
        aux2 = System.currentTimeMillis();
        long timeAttribs = aux2 - aux1;
        if ( log.isInfoEnabled() ) {
            log.info( "time for getting the attributes (milisec): " + timeAttribs );
        }

        aux1 = System.currentTimeMillis();
        createTadmClassifierInput();
        runTadm();
        createModel();
        aux2 = System.currentTimeMillis();
        long timeCreateModel = aux2 - aux1;
        if ( log.isInfoEnabled() ) {
            log.info( "time for training the model (milisec): " + timeCreateModel );
        }

        aux1 = System.currentTimeMillis();
        classifyMedConfSet();
        long stop = System.currentTimeMillis();

        if ( log.isInfoEnabled() ) {
            log.info( "time for db read (milisec): " + timeDb );
            log.info( "time to generate lowconf (milisec): " + timeGenerate );
            log.info( "time for getting the attributes (milisec): " + timeAttribs );
            log.info( "time for training the model (milisec): " + timeCreateModel );
            log.info( "time for classifying the medconf set (milisec): " + ( stop - aux1 ) );
            log.info( "total time in milisec: " + ( stop - start ) );
        }
    }

    public void getConfidenceListsFromDb() {
        IntactDbRetriever intactdb = new IntactDbRetriever( workDir.getPath(), new SpokeExpansion() );
        long start = System.currentTimeMillis();

        try {
            // TODO: replace with a proper way of writing to files
            File file = new File( workDir.getPath(), "medconf_all.txt" );
            // TODO: remove after plugin-debug phase is working
            if ( log.isInfoEnabled() ) {
                log.info( "file MC: " + file.getPath() );
            }
            FileWriter fw = new FileWriter( file );
            intactdb.retrieveMediumConfidenceSet( fw );
            fw.close();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( DataRetrieverException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        if ( log.isInfoEnabled() ) {
            log.info( "time needed : " + ( end - start ) );
        }
        // TODO: remove after it is sure it works the around work
        // List<InteractionSimplified> highconf =
        // intactdb.retrieveHighConfidenceSet();
        //
        // DataMethods dm = new DataMethods();
        // highconf = dm.expand(highconf, new SpokeExpansion());
        // dm.export(highconf, new File(workDir.getPath(), "highconf_all.txt"),
        // true);
    }

    public void generateLowconf( int nr ) {
        DataMethods dm = new DataMethods();
        // TODO: make sure the fasta file is in that directory + create uniprot
        // remote get Proteins ->fasta
        File inFile = new File( workDir.getPath(), "40.S_cerevisiae.fasta" );
        if ( !inFile.exists() ) {
            throw new RuntimeException( inFile.getAbsolutePath() );
        }
        Set<String> yeastProteins = dm.readFastaToProts( inFile, null );
        try {
            BinaryInteractionSet highConfBiSet = new BinaryInteractionSet( workDir.getPath() + "/highconf_all.txt" );
            BinaryInteractionSet medConfBiSet = new BinaryInteractionSet( workDir.getPath() + "/medconf_all.txt" );
            Collection<ProteinPair> all = highConfBiSet.getSet();
            all.addAll( medConfBiSet.getSet() );
            BinaryInteractionSet forbidden = new BinaryInteractionSet( all );
            BinaryInteractionSet lowConf = dm.generateLowConf( yeastProteins, forbidden, nr );

            dm.export( lowConf, new File( workDir.getPath(), "lowconf_all.txt" ) );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void getIpGoSeqLowconf() {
        String lcPath = workDir.getPath() + "/lowconf_all.txt";

        try {
            BinaryInteractionSet lowConf = new BinaryInteractionSet( lcPath );
            // retrieval of InterPro and GO annotation for the low confidence set
            File dirForAttrib = new File( workDir, "DataRetriever" );
            if ( !dirForAttrib.exists() ) {
                dirForAttrib.mkdir();
            }
            getIpGoForLc( lowConf.getAllProtNames(), dirForAttrib );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private void getIpGoForLc( Set<String> lowConfProt, File dirForAttrib ) {
        UniprotDataRetriever uniprot = new UniprotDataRetriever();
        try {
            getGo( uniprot, lowConfProt, new FileWriter( new File( dirForAttrib, "lowconf_uniprot_go.txt" ) ) );
            getIp( uniprot, lowConfProt, new FileWriter( new File( dirForAttrib, "lowconf_uniprot_ip.txt" ) ) );
            getSeq( uniprot, lowConfProt, new FileWriter( new File( dirForAttrib, "lowconf_uniprot_seq.txt" ) ) );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void getIp( UniprotDataRetriever uniprot, Set<String> lowConfProt, Writer fileWriter ) {
        try {
            for ( String ac : lowConfProt ) {
                Set<InterProId> ips = uniprot.getIps( new UniprotAc( ac ) );
                fileWriter.append( ac + "," );
                for ( InterProId ipId : ips ) {
                    fileWriter.append( ipId.getId() + "," );
                }
                fileWriter.append( "\n" );
            }
            fileWriter.close();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void getGo( UniprotDataRetriever uniprot, Set<String> lowConfProt, Writer fileWriter ) {
        try {
            for ( String ac : lowConfProt ) {
                Set<GoId> gos = uniprot.getGos( new UniprotAc( ac ) );
                fileWriter.append( ac + "," );
                for ( GoId goId : gos ) {
                    fileWriter.append( goId.getId() + "," );
                }
                fileWriter.append( "\n" );
            }
            fileWriter.close();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void getSeq( UniprotDataRetriever uniprot, Set<String> lowConfProt, Writer fileWriter ) {
        for ( String ac : lowConfProt ) {
            Sequence seq = uniprot.getSeq( new UniprotAc( ac ) );
            if ( seq != null ) {
                print( ac, seq, fileWriter );
            } else {
                if ( log.isInfoEnabled() ) {
                    log.info( "No Sequence found for ac: " + ac );
                }
            }
        }
        try {
            fileWriter.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private void print( String ac, Sequence seq, Writer fileWriter ) {
        try {
            fileWriter.append( ">" + ac + "|description\n" + seq + "\n" );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void getInterProGoAndAlign() {
        try {
            BinaryInteractionSet biSet = new BinaryInteractionSet( workDir.getPath() + "/highconf_all.txt" );
            AttributeGetter aG = new AttributeGetter( dbFolder, uniprotPath + "/uniprot_sprot.dat", biSet, workDir,
                                                      blastArchiveDir, this.email, nrPerSubmission );
            Set<String> againstProteins = biSet.getAllProtNames();

            File seqFile = new File( workDir, "/DataRetriever/lowconf_db_seq.txt" );
            biSet = new BinaryInteractionSet( workDir.getPath() + "/lowconf_all.txt" );
            aG.getAllAttribs( biSet, againstProteins, workDir.getPath() + "/lowconf_all_attribs.txt", seqFile );

            // TODO: make sure it findes the seqFiles
            seqFile = new File( workDir, "/IntactDbRetriever/highconf_db_seq.txt" );
            aG.getAllAttribs( biSet, againstProteins, workDir.getPath() + "/highconf_all_attribs.txt", seqFile );

            seqFile = new File( workDir, "/IntactDbRetriever/medconf_db_seq.txt" );
            biSet = new BinaryInteractionSet( workDir.getPath() + "/medconf_all.txt" );
            aG.getAllAttribs( biSet, againstProteins, workDir.getPath() + "/medconf_all_attribs.txt", seqFile );

            aG.close();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( BlastServiceException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

//    public void getInterProGoAlignHC(){
//
//    }
//
//    public void getInterProGoAlignLC(){
//
//    }
//
//    public void getInterProGoAlignMC(){
//
//    }

    public void createTadmClassifierInput() {
        try {
            ClassifierInputWriter ciw = new ClassifierInputWriter( workDir.getPath() + "/highconf_all_attribs.txt",
                                                                   workDir.getPath() + "/lowconf_all_attribs.txt", workDir.getPath() + "/tadm.input", "TADM" );
            ciw.writeAttribList( workDir.getPath() + "/all_attribs.txt" );
        } catch ( IllegalArgumentException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void runTadm() {
        String cmd = "tadm -events_in " + workDir.getPath() + "/tadm.input" + " -params_out " + workDir.getPath()
                     + "/weights.txt";
        try {
            Process process = Runtime.getRuntime().exec( cmd );
            process.waitFor();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( InterruptedException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void createModel() {
        try {
            classifier = new MaxEntClassifier( workDir.getPath() + "/all_attribs.txt", workDir.getPath()
                                                                                       + "/weights.txt" );
        } catch ( IllegalArgumentException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void classifyMedConfSet() {
        File file = new File( workDir.getPath(), "medconf_all_attribs.txt" );
        BufferedReader br;
        try {
            FileWriter fw = new FileWriter( new File( workDir.getPath(), "medconf_FINAL_score.txt" ) );

            br = new BufferedReader( new FileReader( file ) );
            String line;

            while ( ( line = br.readLine() ) != null ) {
                double tScore = classifier.trueScoreFromLine( line );
                String[] str = line.split( "," );
                fw.append( str[0] + ": " + tScore + "\n" );
            }
            fw.close();
        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void testDir( File workDir ) {
        if ( !workDir.exists() ) {
            throw new IllegalArgumentException( "WorkDir must exist! " + workDir.getPath() );
        }
        if ( !workDir.isDirectory() ) {
            throw new IllegalArgumentException( "WorkDir must be a directory! " + workDir.getPath() );
        }
        if ( !workDir.canWrite() ) {
            throw new IllegalArgumentException( "WorkDir must be writable! " + workDir.getPath() );
        }
    }
}
