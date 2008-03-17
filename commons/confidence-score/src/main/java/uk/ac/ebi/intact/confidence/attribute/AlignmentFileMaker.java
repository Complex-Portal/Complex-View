/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.attribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.blast.BlastService;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.jdbc.BlastJobEntity;
import uk.ac.ebi.intact.bridges.blast.model.*;
import uk.ac.ebi.intact.confidence.model.InteractionSimplified;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;
import uk.ac.ebi.intact.confidence.util.GlobalData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 09-Aug-2006 <p/> <p/> Input proteins in FASTA format For each protein
 *        in list output significant BLAST hits (sequence alignments) in a
 *        reference file (reference file example -- all IntAct proteins) <p/>
 *        Output format: UniProt IDs delimited by commas Protein,Hit1,Hit2,Hit3
 *        ... <p/> Later use this file to find attributes of protein pairs If
 *        P;Q is a pair, attributes will be: P;Q,HP1;HQ1,HP2;HQ2, ... HP1 is
 *        first hit to P, HQ1 first hit to Q, etc.
 */
public class AlignmentFileMaker {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( AlignmentFileMaker.class );

    private static int count = 0;

    private BlastService blast;
    private File workDir;
    // private File blastArchive;
    private Float threshold;

    // private String fastaRefPath = "/scratch/blast/intact.fasta";
    // private String blastPath = "/scratch/blast/blast-2.2.14/bin/blastall";

    /**
     * Constructor
     */
    public AlignmentFileMaker( BlastService blast ) {
        this( new Float( 0.001 ), null, blast );
    }

    /**
     * Constructor
     *
     * @param threshold
     * @param workingDirectory
     */
    public AlignmentFileMaker( Float threshold, File workingDirectory, BlastService blast ) {

        this.blast = blast;
        this.threshold = threshold;

        if ( workingDirectory == null ) {
            // String workPath =
            // AlignmentFileMaker.class.getResource("doNotRemoveThis.file").getPath();  //folder deleted
            // workDir = new File(getTargetDirectory(), "AlignmentFileMaker");
            // workDir.mkdir();
            //    HashMap<String, File> paths = GlobalData.getRightPahts();// GlobalTestData.getInstance().getRightPahts();//getTargetDirectory();
            // //
            // new
            //   workDir = paths.get( "workDir" );
            // File(workDir.getParent());
        } else {
            this.workDir = new File( workingDirectory, "AlignmentFileMaker" );
        }
        if ( !workDir.isDirectory() ) {
            workDir.mkdir();
        }

        GlobalData.setCount( 0 );
    }

    // //////////////////
    // // Public Methods
    /**
     * Blasts the proteins in the first set against the proteins in the second
     * set. If the writer is not null, the result will be also written on it.
     *
     * @param intToBlast
     * @param againstList
     * @param writer
     * @throws BlastServiceException
     */
    public void blast( List<InteractionSimplified> intToBlast, List<InteractionSimplified> againstList, Writer writer )
            throws BlastServiceException {
        if ( intToBlast == null || againstList == null || writer == null ) {
            throw new NullPointerException( "Params must not be null!" );
        }
        Set<ProteinSimplified> proteins = getProteinList( intToBlast );
        Set<ProteinSimplified> againstProteins = getProteinList( againstList );

        blast( proteins, againstProteins, writer );
    }

    public void blastProt( Set<UniprotAc> proteins, Set<UniprotAc> againstProteins, Writer writer )
            throws BlastServiceException {
        if ( proteins == null || againstProteins == null || writer == null ) {
            throw new NullPointerException( "Params must not be null!" );
        }
        Set<ProteinSimplified> proteinsS = getProteinList( proteins );
        Set<ProteinSimplified> againstProteinsS = getProteinList( againstProteins );

        blast( proteinsS, againstProteinsS, writer );

    }

    public void blastProt( Set<UniprotAc> proteins, Writer writer )
            throws BlastServiceException {
        if ( proteins == null || writer == null ) {
            throw new NullPointerException( "Params must not be null!" );
        }
        Set<ProteinSimplified> proteinsS = getProteinList( proteins );

        blast( proteinsS, new HashSet<ProteinSimplified>( 0 ), writer );

    }

    /**
     * The results will be saved in the proteins objects as alignments.
     *
     * @param proteins        : set of proteins to be blasted
     * @param againstProteins : proteins filtered in the blast result
     * @throws BlastServiceException
     */
    public Set<ProteinSimplified> blast( Set<ProteinSimplified> proteins, Set<UniprotAc> againstProteins ) throws BlastServiceException {
        if ( proteins == null || againstProteins == null ) {
            throw new NullPointerException( "Params must not be null!" );
        }
        GlobalData.totalProts = proteins.size();
        //TODO: smth when list >20 make smth smarter than this
        int nr = 20;
        Set<ProteinSimplified> done = new HashSet<ProteinSimplified>(proteins.size());

        String uniprotAc=null;
        while (proteins.size() != 0){
            for ( Iterator<ProteinSimplified> iter = proteins.iterator(); iter.hasNext(); ) {
                ProteinSimplified proteinSimplified = iter.next();
                if ( log.isDebugEnabled() ) {
                    log.debug( "fetching " + proteinSimplified );
                }
                Sequence seq = proteinSimplified.getSequence();
                BlastInput blastInput;
                if (seq == null){
                    blastInput = new BlastInput(proteinSimplified.getUniprotAc());
                } else {
                    blastInput = new BlastInput( proteinSimplified.getUniprotAc(), proteinSimplified.getSequence() );
                }
                BlastJobEntity blastJobEntity = blast.submitJob( blastInput );
                blast.refreshJob( blastJobEntity );
                if (log.isDebugEnabled()){
                    log.debug( "jobEntity: " + blastJobEntity );
                }
                if ( blastJobEntity.getStatus().equals( BlastJobStatus.DONE )  || blastJobEntity.getStatus().equals( BlastJobStatus.FAILED ) ||
                        blastJobEntity.getStatus().equals(BlastJobStatus.NOT_FOUND)) {
                    BlastResult result = blast.fetchAvailableBlast( blastJobEntity );
                    if ( result == null ) {
                        throw new BlastServiceException( "JOB " + blastJobEntity + " but BlastResult is null!" );
                    } else {
                        processResult( proteinSimplified, result, againstProteins );
                        done.add( proteinSimplified );
                        iter.remove();
                        if (blastJobEntity.getUniprotAc().equalsIgnoreCase( uniprotAc )){
                            uniprotAc =null;
                        }
                    }
                } else {
                    if (uniprotAc == null){
                        uniprotAc = blastJobEntity.getUniprotAc();
                    } else if (uniprotAc.equalsIgnoreCase( blastJobEntity.getUniprotAc() )){
                        try {
                            Thread.sleep( 5000 ); // 5 sec
                        } catch ( InterruptedException e1 ) {
                            throw new BlastServiceException( "thread.sleep interrupted", e1 );
                        }
                    }
                }

            }
        }
        return done;


//        if ( proteins.size() < nr ) {
//            Set<ProteinSimplified> doneProteins = new HashSet<ProteinSimplified>();
//            while ( proteins.size() != 0 ) {
//                for ( Iterator<ProteinSimplified> iterator = proteins.iterator(); iterator.hasNext(); ) {
//                    ProteinSimplified prot = iterator.next();
//                    if ( log.isInfoEnabled() ) {
//                        log.info( "fetching " + prot );
//                    }
//                    BlastResult result = blast.fetchAvailableBlast( new UniprotAc( prot.getUniprotAc().getAcNr() ) );
//                    if ( result != null ) {
//                        processResult( prot, result, againstProteins );
//                        if ( log.isInfoEnabled() ) {
//                            log.info( "processed: " + GlobalData.getCount() + " out of " + GlobalData.totalProts );
//                        }
//                        doneProteins.add( prot);
//                        iterator.remove();
//                    } else {
//                        //TODO: implement a way for the client not to wait
//                        //if (blast.okToSubmit( 1)) {}
//                        BlastInput bi = formatBlastInput( prot );
//                        BlastJobEntity job = blast.submitJob( bi );
//                        if ( log.isInfoEnabled() ) {
//                            log.info( "job submitted: " + job );
//                        }
//                    }
//                }
//            }
//           return doneProteins;
//        }
//        return null;
    }
   

    /**
     * @param proteins
     * @param againstProteins
     * @param writer
     * @throws BlastServiceException
     */
    public void blast( Set<ProteinSimplified> proteins, Set<ProteinSimplified> againstProteins, Writer writer )
            throws BlastServiceException {
        if ( proteins == null || againstProteins == null || writer == null ) {
            throw new NullPointerException( "Params must not be null!" );
        }

        GlobalData.setCount( 0 );
        GlobalData.startTime = -1;
        GlobalData.totalProts = proteins.size();
        if ( log.isInfoEnabled() ) {
            log.info( "total nr of proteins : " + GlobalData.totalProts );
            count++;
            File f = new File( workDir, "listOfProteins" + count + ".txt" );
            printToFile( proteins, f );
        }

        while ( proteins.size() != 0 ) {
            Set<UniprotAc> againstProt = getUniprotAcs( againstProteins );
            for ( Iterator<ProteinSimplified> iterator = proteins.iterator(); iterator.hasNext(); ) {
                ProteinSimplified prot = iterator.next();
                if ( log.isInfoEnabled() ) {
                    log.info( "fetching " + prot );
                }
                BlastResult result = blast.fetchAvailableBlast( new UniprotAc( prot.getUniprotAc().getAcNr() ) );
                if ( result != null ) {
                    processResult( result, againstProt, writer );
                    if ( log.isInfoEnabled() ) {
                        log.info( "processed: " + GlobalData.getCount() + " out of " + GlobalData.totalProts );
                    }
                    iterator.remove();
                } else {
                    //TODO: implement a way for the client not to wait
                    //if (blast.okToSubmit( 1)) {}
                    BlastInput bi = formatBlastInput( prot );
                    BlastJobEntity job = blast.submitJob( bi );
                    if ( log.isInfoEnabled() ) {
                        log.info( "job submitted: " + job );
                    }
                }
            }

        }

        try {
            writer.close();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        GlobalData.endTime = System.currentTimeMillis();
        long time = GlobalData.endTime - GlobalData.startTime;
        if ( log.isInfoEnabled() ) {
            log.info( "for " + GlobalData.getCount() + " prots: " + time );
            log.info( "ETA: " + GlobalData.eta( GlobalData.getCount(), time, GlobalData.totalProts ) + " (min)" );
        }
    }

    private void printToFile( Set<ProteinSimplified> proteins, File f ) {
        try {
            Writer w = new FileWriter( f );
            for ( ProteinSimplified protein : proteins ) {
                w.append( protein.getUniprotAc() + "\n" );
            }

            w.close();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // ///////////////////
    // // Private Methods
    private Set<ProteinSimplified> getProteinList( Set<UniprotAc> proteins ) {
        Set<ProteinSimplified> prots = new HashSet<ProteinSimplified>( proteins.size() );
        for ( UniprotAc uniprotAc : proteins ) {
            prots.add( new ProteinSimplified( uniprotAc ) );
        }
        return prots;
    }

    private BlastInput formatBlastInput( ProteinSimplified prot ) {
        UniprotAc ac = new UniprotAc( prot.getUniprotAc().getAcNr() );
        if ( prot.getSequence() != null ) {
            Sequence seq = new Sequence( prot.getSequence().getSeq() );
            return new BlastInput( ac, seq );
        } else {
            return new BlastInput( ac );
        }
    }

    private Set<UniprotAc> getUniprotAcs( Set<ProteinSimplified> againstProteins ) {
        Set<UniprotAc> againstProts = new HashSet<UniprotAc>( againstProteins.size() );
        for ( ProteinSimplified proteinSimplified : againstProteins ) {
            againstProts.add( new UniprotAc( proteinSimplified.getUniprotAc().getAcNr() ) );
        }
        return againstProts;
    }

    private Set<ProteinSimplified> getProteinList( List<InteractionSimplified> interactions ) {
        Set<ProteinSimplified> proteins = new HashSet<ProteinSimplified>();
        for ( InteractionSimplified intS : interactions ) {
            proteins.addAll( intS.getInteractors() );
        }
        return proteins;
    }

    private Set<UniprotAc> notIncluded( List<BlastResult> results, Set<UniprotAc> proteins ) {
        Set<UniprotAc> protNotIn = new HashSet<UniprotAc>();

        Set<UniprotAc> resultProt = new HashSet<UniprotAc>( results.size() );
        for ( BlastResult blastResult : results ) {
            resultProt.add( new UniprotAc( blastResult.getUniprotAc() ) );
        }

        for ( UniprotAc ac : proteins ) {
            if ( !resultProt.contains( ac ) ) {
                protNotIn.add( ac );
            }
        }
        return protNotIn;
    }

    private void processResult( ProteinSimplified protein, BlastResult result, Set<UniprotAc> againstProteins ) {
        GlobalData.increment( 1 );
        if ( ( GlobalData.getCount() % 20 ) == 0 ) {
            GlobalData.endTime = System.currentTimeMillis();
            long time = GlobalData.endTime - GlobalData.startTime;
            log.info( "for " + GlobalData.getCount() + " prots: " + ( ( time ) / 60000 ) );
            log.info( "ETA: " + GlobalData.eta( GlobalData.getCount(), time, GlobalData.totalProts ) + " (min)" );
        }
        for ( Hit hit : result.getHits() ) {
            Float evalue = hit.getEValue();
            String ac = hit.getUniprotAc();
            if ( ac == null ) {
                log.debug( "Ac is null, in a hit list!" + result.getUniprotAc() + ": " + hit );
            }

            // TODO: remove try/catch block after test
            try {
                UniprotAc uniprotAc = new UniprotAc( ac );
                if ( evalue < threshold && againstProteins.contains( uniprotAc ) ) {
                    protein.addAlignment( uniprotAc );
                }
            } catch ( IllegalArgumentException e ) {
                log.debug( e.toString() + "\n" + " : " + evalue + ": " + ac );
            }
        }
    }

    private void processResult( BlastResult result, Set<UniprotAc> againstProteins, Writer writer ) {
        GlobalData.increment( 1 );
        if ( ( GlobalData.getCount() % 20 ) == 0 ) {
            GlobalData.endTime = System.currentTimeMillis();
            long time = GlobalData.endTime - GlobalData.startTime;
            log.info( "for " + GlobalData.getCount() + " prots: " + ( ( time ) / 60000 ) );
            log.info( "ETA: " + GlobalData.eta( GlobalData.getCount(), time, GlobalData.totalProts ) + " (min)" );
        }
        String alignmentLine = result.getUniprotAc();
        for ( Hit hit : result.getHits() ) {
            Float evalue = hit.getEValue();
            String ac = hit.getUniprotAc();
            if ( ac == null ) {
                log.debug( "Ac is null, in a hit list!" + result.getUniprotAc() + ": " + hit );
            }

            // TODO: remove try/catch block after test
            try {
                UniprotAc uniprotAc = new UniprotAc( ac );
                if ( evalue < threshold && againstProteins.contains( uniprotAc ) ) {
                    alignmentLine += "," + ac;
                }
            } catch ( IllegalArgumentException e ) {
                log.debug( e.toString() + "\n" + alignmentLine + " : " + evalue + ": " + ac );
            }

        }
        try {
            writer.append( alignmentLine + "\n" );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void processResults( List<BlastResult> results, Set<UniprotAc> againstProteins, Writer writer ) {
        // TODO remove this after finalized
        // process the results according to the thresholds : against
        // proteins and eval < 0.001
        // add to the alignmentLine
        // append the alignmentLine to a writer
        if ( GlobalData.startTime == -1 ) {
            GlobalData.startTime = System.currentTimeMillis();
        }
        for ( BlastResult result : results ) {
            GlobalData.increment( 1 );
            if ( ( GlobalData.getCount() % 20 ) == 0 ) {
                GlobalData.endTime = System.currentTimeMillis();
                long time = GlobalData.endTime - GlobalData.startTime;
                log.info( "for " + GlobalData.getCount() + " prots: " + time );
                log.info( "ETA: " + GlobalData.eta( GlobalData.getCount(), time, GlobalData.totalProts ) + " (min)" );
            }
            String alignmentLine = result.getUniprotAc();
            for ( Hit hit : result.getHits() ) {
                Float evalue = hit.getEValue();
                String ac = hit.getUniprotAc();
                if ( ac == null ) {
                    log.debug( "Ac is null, in a hit list!" + result.getUniprotAc() + ": " + hit );
                }

                // TODO: remove try/catch block after test
                try {
                    UniprotAc uniprotAc = new UniprotAc( ac );
                    if ( evalue < threshold && againstProteins.contains( uniprotAc ) ) {
                        alignmentLine += "," + ac;
                    }
                } catch ( IllegalArgumentException e ) {
                    log.debug( e.toString() + "\n" + alignmentLine + " : " + evalue + ": " + ac );
                }

            }
            try {
                writer.append( alignmentLine + "\n" );
            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

//    private File getTargetDirectory() {
//        String outputDirPath = GlobalTestData.class.getResource( "/" ).getFile();
//        System.out.println( "targetDir: " + outputDirPath );
//        Assert.assertNotNull( outputDirPath );
//        File outputDir = new File( outputDirPath );
//        // we are in confidence-score\target\test-classes , move 1 up
//        outputDir = outputDir.getParentFile();
//        Assert.assertNotNull( outputDir );
//        Assert.assertTrue( outputDir.getAbsolutePath(), outputDir.isDirectory() );
//        Assert.assertEquals( "target", outputDir.getName() );
//        return outputDir;
//    }

    // /* (non-Javadoc)
    // * @see java.lang.Object#finalize()
    // */
    // @Override
    // protected void finalize() throws Throwable {
    // blast.close();
    // }
    public void close() {
        try {
            blast.close();
        } catch ( BlastServiceException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
