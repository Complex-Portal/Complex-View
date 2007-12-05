/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.confidence.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;
import uk.ac.ebi.intact.confidence.model.ConfidenceType;
import uk.ac.ebi.intact.confidence.psimi.PsiMiTabConfidence;
import uk.ac.ebi.intact.confidence.psimi.PsiMiXmlConfidence;
import uk.ac.ebi.intact.plugin.IntactAbstractMojo;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        04-Dec-2007
 *        </pre>
 */

/**
 * Mojo confidence score
 *
 * @goal asign
 * @phase install
 */
public class PsimiXmlMojo extends IntactAbstractMojo {
    /**
         * Sets up a logger for that class.
         */
        public static final Log log = LogFactory.getLog( PsimiXmlMojo.class );
        /**
         * Project instance
         *
         * @parameter expression="${project}"
         * @readonly
         */
        protected MavenProject project;

        /**
         * The path to the PSI-MI XML input File
         *
         * @parameter expression="${psimixmlInputFilePath}" default-value="${project.build.outputDirectory}"
         */
        private String psimixmlInputFilePath;

       
        /**
         * The path to the PSI-MI XML output File
         *
         * @parameter expression="${psimixmlOutputFilePath}" default-value="${project.build.outputDirectory}"
         */
        private String psimixmlOutputFilePath;

        /**
         * The path to the bigh confidence set path for the model
         *
         * @parameter expression="${hcSetPath}" default-value="${project.build.outputDirectory}"
         */
        private String hcSetPath;

        /**
         * The path to the low confidence set path for the model
         *
         * @parameter expression="${blastArchiveDirPath}" default-value="${project.build.outputDirectory}"
         */
        private String lcSetPath;


        /**
         * The path to the email for blast service
         *
         * @parameter expression="${email}" default-value="${project.build.outputDirectory}"
         */
        private String email;

        /**
         * The path to the blastArchiveDirPath for blast service
         *
         * @parameter expression="${blastArchiveDirPath}" default-value="${project.build.outputDirectory}"
         */
        private String blastArchiveDirPath;

        /**
         * The path to the blastDbDirPath for blast service
         *
         * @parameter expression="${blastDbDirPath}" default-value="${project.build.outputDirectory}"
         */
        private String blastDbDirPath;

        /**
         * The path to the nrPerSubmission for blast service
         *
         * @parameter expression="${nrPerSubmission}" default-value="20"
         */
        private int nrPerSubmission;

        /**
         * The path to the tableName for blast service
         *
         * @parameter expression="${tableName}" default-value="${job}"
         */
        private String tableName;

        /**
         * The path to the workingDir for blast service
         *
         * @parameter expression="${workDir}" default-value="${project.build.outputDirectory}"
         */
        private String workDir;

         /**
         * The path to the workingDir for blast service
         *
         * @parameter expression="${confidenceGO}" default-value="${true}"
         */
        private boolean confidenceGO;
        /**
         * The path to the workingDir for blast service
         *
         * @parameter expression="${confidenceInterPro}" default-value="${true}"
         */
        private boolean confidenceInterPro;
        /**
         * The path to the workingDir for blast service
         *
         * @parameter expression="${confidenceAlign}" default-value="${true}"
         */
        private boolean confidenceAlign;


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
            log.info( "PSI-MI XML input File: " + psimixmlInputFilePath );
            log.info( "PSI-MI XML output File: " + psimixmlOutputFilePath );
            log.info( "workDir: " + workDir );
            log.info( "nrPerSubmission: " + nrPerSubmission );
            log.info( "tableName: " + tableName );
            log.info( "blastDbDirPath: " + blastDbDirPath );
            log.info( "blastArchiveDirPath: " + blastArchiveDirPath );
            log.info( "email: " + email );
            log.info( "hcSetPath: " + hcSetPath );
            log.info( "lcSetPath: " + lcSetPath );
        }

        doMojo();

        if ( log.isInfoEnabled() ) {
            log.info( "Mojo finished." );
        }
    }

    private void doMojo() {
        BlastConfig config = new BlastConfig( email );
        if ( blastArchiveDirPath != null ) {
            config.setBlastArchiveDir( new File( blastArchiveDirPath ) );
        }
        if ( blastDbDirPath != null ) {
            config.setDatabaseDir( new File( blastDbDirPath ) );
        }
        if ( nrPerSubmission > 0 ) {
            config.setNrPerSubmission( nrPerSubmission );
        }
        if ( tableName != null ) {
            config.setTableName( tableName );
        }

        if ( workDir == null ) {
            workDir = System.getProperty( "java.io.tmpdir" );
        }

        try {
            PsiMiXmlConfidence conf = new PsiMiXmlConfidence( hcSetPath, lcSetPath, new File( workDir ), config );
            File inFile = new File( psimixmlInputFilePath );
            File outFile = new File( psimixmlOutputFilePath );
            Set<ConfidenceType> confs = new HashSet<ConfidenceType>();
            if (confidenceGO){
                confs.add( ConfidenceType.GO);
            }
            if(confidenceInterPro){
                confs.add( ConfidenceType.InterPRO);
            }
            if(confidenceAlign){
                confs.add( ConfidenceType.Alignment);
            }
            if (inFile.exists()){
                conf.appendConfidence( inFile, outFile, confs);
            } else {
                log.warn("input PsimiTab file not found!");
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }
}
