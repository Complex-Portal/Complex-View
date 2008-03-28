package uk.ac.ebi.intact.service.commons;

import opennlp.maxent.GISModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import uk.ac.ebi.intact.confidence.maxent.AbstractMaxEnt;
import uk.ac.ebi.intact.confidence.maxent.MaxEntClassifierException;
import uk.ac.ebi.intact.confidence.maxent.MaxentUtils;
import uk.ac.ebi.intact.confidence.maxent.WekaMaxEntClassifier;
import uk.ac.ebi.intact.confidence.weights.WeightsStrategy;
import uk.ac.ebi.intact.confidence.weights.WeightsStrategyException;
import uk.ac.ebi.intact.confidence.weights.WekaWeightsImpl;
import uk.ac.ebi.intact.confidence.weights.inputs.OpenNLP;
import uk.ac.ebi.intact.confidence.intact.TrainModel;
import uk.ac.ebi.intact.plugin.IntactAbstractMojo;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;

import java.io.*;

/**
 * Mojo confidence score analzer
 *
 * @goal analyze
 * @phase install
 */
public class Classify extends IntactAbstractMojo {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( Classify.class );

    /**
     * Project instance
     *
     * @parameter expression="${project}"
     * @readonly
     */
    protected MavenProject project;

    /**
     * The path to the work directory
     *
     * @parameter expression="${workDirPath}" default-value="${project.build.outputDirectory}"
     */
    private String workDirPath;

    /**
     * The path to the hibernate configuraation file
     *
     * @parameter expression="${pgConfigPath}" default-value="${project.build.outputDirectory/hibernate.cfg.xml}"
     */
    private String pgConfigPath;

    /**
     * The path to yeast proteome fasta file
     *
     * @parameter expression="${yeastFastaPath}" default-value="${project.build.outputDirectory}"
     */
    private String yeastFastaPath;

//    File goaIntact, BlastConfig blastConfig

    /**
     * The path to the GOA annotation file trimmed for IntAct
     *
     * @parameter expression="${goaIntactPath}" default-value="${project.build.outputDirectory}"
     */
    private String goaIntactPath;

   /**
     * The email address for the Blast service
     *
     * @parameter expression="${email}" default-value="x@ebi.ac.uk"
     */
    private String email;

   /**
     * The path to the blast archive containing blast outputs
     *
     * @parameter expression="${blastArchivePath}" default-value="${project.build.outputDirectory}"
     */
    private String blastArchivePath;

    /**
     * The path to the blast database containing preprocessed blast jobs
     *
     * @parameter expression="${blastDbDirPath}" default-value="${project.build.outputDirectory}"
     */
    private String blastDbDirPath;

    public MavenProject getProject() {
        return project;
    }

    protected Appender getLogAppender() throws IOException {
        File logFile = new File( getDirectory(), "log.out" );
        getLog().info( "Setting log4j in output: " + logFile.getAbsolutePath() );

        Layout layout = getLogLayout();
        FileAppender appender = new FileAppender( layout, logFile.getAbsolutePath(), false );
        appender.setThreshold( Priority.INFO );

        return appender;
    }

    protected Layout getLogLayout() {
        String pattern = "%d [%t] %-5p (%C{1},%L) - %m%n";
        PatternLayout layout = new PatternLayout( pattern );
        return layout;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        enableLogging();
        if ( log.isInfoEnabled() ) {
            log.info( "Mojo started ..." );
            log.info( "hibernate config file: " + pgConfigPath );
            log.info( "yeast proteome file: " + yeastFastaPath );
            log.info( "GOA file for IntAct: " + goaIntactPath );
            log.info( "workDirPath: " + workDirPath );
            log.info( "blast service: email: " + email );

        }

        BlastConfig blastConfig = new BlastConfig(email);
        blastConfig.setBlastArchiveDir( new File( blastArchivePath ) );
        blastConfig.setDatabaseDir( new File(blastDbDirPath)  );

        TrainModel trainModel = new TrainModel(new File(workDirPath), new File(pgConfigPath),
                                               new File(yeastFastaPath), new File(goaIntactPath),
                                               blastConfig);
        File gisModel = trainModel.generateModel();
          //File workDir, File pgConfigFile, File yeastFasta, File goaIntact, BlastConfig blastConfig
//        //TODO: remove initialization after test-phase
//         workDirPath = "H:\\tmp";
//         highconfAttributesPath = "H:\\tmp\\ConfidenceModel\\HcAttribs\\set_go_attributes.txt";
//         lowconfAttributesPath = "H:\\tmp\\ConfidenceModel\\LcAttribs\\set_go_attributes.txt";
//         medconfAttributesPath = "H:\\tmp\\ConfidenceModel\\McAttribs\\set_go_attributes.txt";

//        if ( log.isInfoEnabled() ) {
//            log.info( "writing weights and attribs ..." );
//        }
//        long start = System.currentTimeMillis();
//        String[] aux = medconfAttributesPath.split( "_" );
//        String type = "";
//        if ( aux.length == 3 ) {
//            type = aux[1] + "_";
//        }
//
//
//        if ( log.isInfoEnabled() ) {
//            long end = System.currentTimeMillis();
//            long total = ( end - start ) / 60000;
//            log.info( "weights computed: " + total + "(min)" );
//        }


//        if ( log.isInfoEnabled() ) {
//            log.info( "creating gisModel input ..." );
//        }
//        File outGisInput = new File( workDirPath, "gisModel.input" );
//        OpenNLP.createInput( highconfAttributesPath, lowconfAttributesPath, outGisInput.getPath() );
//        if ( log.isInfoEnabled() ) {
//            log.info( "finished creating gisModel input." );
//            log.info( "writing model to file ..." );
//        }
//
//        File outputModelFile = new File( workDirPath, "gisModel.output" );
//        try {
//            GISModel model = MaxentUtils.createModel( new FileInputStream( outGisInput ) );
//
//            MaxentUtils.writeModelToFile( model, outputModelFile );
//            if ( log.isInfoEnabled() ) {
//                log.info( "finished  writing model to file." );
//                log.info( "scoring mc ..." );
//            }
//            File outScore = new File( workDirPath, "mc_score_gis.txt" );
//            score( model, medconfAttributesPath, outScore.getPath() );
//            if ( log.isInfoEnabled() ) {
//                log.info( "finished scoring mc." );
//            }
//
//        } catch ( FileNotFoundException e ) {
//            e.printStackTrace();
//        } catch ( IOException e ) {
//            e.printStackTrace();
//        }

        if ( log.isInfoEnabled() ) {
            log.info( "Mojo done." );
        }
    }

//    private void score( GISModel model, String mcPath, String outPath ) {
//        try {
//            Writer w = new FileWriter( outPath );
//            BufferedReader br = new BufferedReader( new FileReader( mcPath ) );
//            String line = "";
//            int  count =0;
//            while ( ( line = br.readLine() ) != null ) {
//                String[] aux = line.split( "," );
//                String[] attribs = OpenNLP.getAttribsFromLine( line );
//                double[] eval = model.eval( attribs );
//                w.append( aux[0] + ": " + eval[0] + "\n");
//                count ++;
//                if ((count %20)==0 ){
//                    log.info("scored: " + count);
//                }
//            }
//            if(log.isInfoEnabled()){
//                log.info("scored total: " + count);
//            }
//            br.close();
//            w.close();
//        } catch ( FileNotFoundException e ) {
//            e.printStackTrace();
//        } catch ( IOException e ) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void main( String[] args ) {
//        File outAttribs = new File( "H:\\tmp\\ConfidenceModel\\components\\all_attribs_new.txt" );
//        File outWeight = new File( "H:\\tmp\\ConfidenceModel\\components\\all_weights_new.txt" );
//        try {
//            AbstractMaxEnt maxent = new WekaMaxEntClassifier( outAttribs.getPath(), outWeight.getPath() );
//            File outScoreFile = new File( "H:\\tmp\\ConfidenceModel\\components", "medconfidence_" + "all_new_" + "FINAL_SCORE.txt" );
//            String medconfAttributesPath = "H:\\tmp\\ConfidenceModel\\components\\mc_all_attributes.txt";
//            maxent.writeInteractionScores( medconfAttributesPath, outScoreFile.getPath() );
//        } catch ( MaxEntClassifierException e ) {
//            e.printStackTrace();
//        }
//
////        Classify cl = new Classify();
////
////        try {
////            cl.execute();
////        } catch ( MojoExecutionException e ) {
////            e.printStackTrace();
////        } catch ( MojoFailureException e ) {
////            e.printStackTrace();
////        }
//    }
//
//
//    private void WekaClassify() {
//        String[] aux = medconfAttributesPath.split( "_" );
//        String type = "";
//        if ( aux.length == 3 ) {
//            type = aux[1] + "_";
//        }
//        File outWeight = new File( workDirPath, type + "weights.txt" );
//        File outAttribs = new File( workDirPath, type + "attribs.txt" );
//
//        WeightsStrategy weka = new WekaWeightsImpl( new File( workDirPath ) );
//        try {
//            weka.computeWeights( highconfAttributesPath, lowconfAttributesPath, outAttribs.getPath(), outWeight.getPath() );
//            AbstractMaxEnt maxent = new WekaMaxEntClassifier( outAttribs.getPath(), outWeight.getPath() );
//            File outScoreFile = new File( workDirPath, "medconfidence_" + type + "FINAL_SCORE.txt" );
//            maxent.writeInteractionScores( medconfAttributesPath, outScoreFile.getPath() );
//
//        } catch ( WeightsStrategyException e ) {
//            e.printStackTrace();
//        } catch ( MaxEntClassifierException e ) {
//            e.printStackTrace();
//        }
//    }

}
