package uk.ac.ebi.intact.service.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import uk.ac.ebi.intact.confidence.intact.TrainModel;
import uk.ac.ebi.intact.plugin.IntactAbstractMojo;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;

import java.io.*;

/**
 * Mojo confidence score analzer
 *
 * @goal train
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
        try {
            File gisModel = trainModel.generateModel();
            if (!gisModel.exists() && log.isWarnEnabled()){
                log.warn("No classifier file created!");        
            }
        } catch ( Exception e ) {
            throw new MojoExecutionException( e.toString());
       }
        if ( log.isInfoEnabled() ) {
            log.info( "Mojo done." );
        }
    }
}
