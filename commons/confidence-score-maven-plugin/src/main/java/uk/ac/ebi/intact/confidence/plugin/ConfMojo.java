package uk.ac.ebi.intact.confidence.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import uk.ac.ebi.intact.confidence.ConfidenceModelImpl;
import uk.ac.ebi.intact.confidence.utils.Merge;
import uk.ac.ebi.intact.plugin.IntactAbstractMojo;

import java.io.File;
import java.io.IOException;

/**
 * Mojo confidence score
 *
 * @goal confidence
 * @phase install
 */
public class ConfMojo extends IntactAbstractMojo {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( ConfMojo.class );
    /**
     * Project instance
     *
     * @parameter expression="${project}"
     * @readonly
     */
    protected MavenProject project;
    /**
     * The path to the hibernate.cfg.xml file
     *
     * @parameter expression="${dbConfigFilePath}" default-value="${project.build.outputDirectory}"
     */
    private String dbConfigFilePath;

    /**
     * The path to the work directory
     *
     * @parameter expression="${workDirPath}" default-value="${project.build.outputDirectory}"
     */
    private String workDirPath;
    /**
     * The path to the blast archive directory
     *
     * @parameter expression="${blastArchivePath}" default-value="${project.build.outputDirectory}"
     */
    private String blastArchivePath;
    /**
     * The path to the blast database directory
     *
     * @parameter expression="${blastDbPath}" default-value="${project.build.outputDirectory}"
     */
    private String blastDbPath;

    /**
     * The email for the blast web service
     *
     * @parameter expression="${email}" default-value="${x@ebi.ac.uk}"
     */
    private String email;

    /**
     * The path to the gis maximum entropy model file.
     *
     * @parameter expression="${gisModelFilePath}" default-value="${workDirPath/gisModel.txt}"
     */
    private String gisModelFilePath;

    /**
     * The path to the high confidence set file.
     *
     * @parameter expression="${hcFilePath}" default-value="${workDirPath/highconf_set.txt}"
     */
    private String hcFilePath;

    /**
     * The path to the GOA file for intact.
     *
     * @parameter expression="${goaFilePath}" default-value="${workDirPath/gene_association.goa_intact}"
     */
    private String goaFilePath;

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
        if ( log.isDebugEnabled() ) {
            log.debug( "Debug active." );
        }

        if ( log.isInfoEnabled() ) {
            log.info( "Info active." );
        }
        if ( log.isWarnEnabled() ) {
            log.warn( "Warn active." );
        }
        if ( log.isErrorEnabled() ) {
            log.error( "Error active." );
        }
        if ( log.isFatalEnabled() ) {
            log.fatal( "Fatal active." );
        }

        if ( log.isInfoEnabled() ) {
            log.info( "Mojo started ..." );
            log.info( "memory: " + ( Runtime.getRuntime().maxMemory() ) / ( 1024 * 1024 ) );
            log.info( "Mojo started ..." );
            log.info( "memory: " + ( Runtime.getRuntime().maxMemory() ) / ( 1024 * 1024 ) );
            log.info( "db config xml: " + dbConfigFilePath );
            log.info( "workDir: " + workDirPath );
            log.info( "blast archive: " + blastArchivePath );
            log.info( "blast db path: " + blastDbPath );
            log.info( "GIS Model: " + gisModelFilePath );
            log.info( "high confidence set file: " + hcFilePath );
            log.info( "GO annotation file for intact: " + goaFilePath );
        }


        String ipPath = "/homes/iarmean/tmp/ConfidenceModel/McAttribs/set_ip_attributes.txt";
        String goPath = "/homes/iarmean/tmp/ConfidenceModel/McAttribs/set_go_attributes.txt";
        String alignPath = "/homes/iarmean/tmp/ConfidenceModel/McAttribs/set_align_attributes.txt";
        String outPath = "/homes/iarmean/tmp/ConfidenceModel/McAttribs/merged_attributes.txt";
        String[] paths = {ipPath, goPath, alignPath};
        try {
            new Merge().merge( paths, outPath );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        ConfidenceModelImpl cm = new ConfidenceModelImpl( blastDbPath, uniprotDirPath, workDirPath, blastArchivePath, email, nrPerSubmission );
        //	cm.buildModel();
        //or
        classify( cm );
        if ( log.isInfoEnabled() ) {
            log.info( "Mojo done." );
        }
        System.out.println( "Mojo done. :)" );
    }

    private void classify( ConfidenceModelImpl cm ) {
        long start = System.currentTimeMillis();
//        	cm.getConfidenceListsFromDb();
        long aux1 = System.currentTimeMillis();
        long timeDb = aux1 - start;
        if ( log.isInfoEnabled() ) {
            log.info( "time for db retrieve (milisec): " + timeDb );
        }

        aux1 = System.currentTimeMillis();
//        	cm.generateLowconf(9000);
        long aux2 = System.currentTimeMillis();
        long timeGenerate = aux2 - aux1;
        if ( log.isInfoEnabled() ) {
            log.info( "time for generating lowconf (milisec): " + timeGenerate );
        }

        aux1 = System.currentTimeMillis();
        // cm.getInterProGoAndAlign();
        //cm.getInterProGoAlignHC();
        //cm.getInterProGoAlignLC();
        cm.getInterProGoAlignMC();
        aux2 = System.currentTimeMillis();
        long timeAttribs = aux2 - aux1;
        if ( log.isInfoEnabled() ) {
            log.info( "time for getting the attributes (milisec): " + timeAttribs );
        }
        //TODO: deploy conf-score and use this method for attrib gethering
        //cm.getIpGoSeqLowconf();

        aux1 = System.currentTimeMillis();
        cm.createTadmClassifierInput();
        cm.runTadm();
        cm.createModel();
        aux2 = System.currentTimeMillis();
        long timeCreateModel = aux2 - aux1;
        if ( log.isInfoEnabled() ) {
            log.info( "time for training the model (milisec): " + timeCreateModel );
        }

        aux1 = System.currentTimeMillis();
        cm.classifyMedConfSet();
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

    public MavenProject getProject() {
        return project;
    }
}
