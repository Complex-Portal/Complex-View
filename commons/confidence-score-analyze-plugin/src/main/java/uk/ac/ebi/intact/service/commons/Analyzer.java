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
package uk.ac.ebi.intact.service.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import uk.ac.ebi.intact.confidence.analyze.AnalyzeMain;
import uk.ac.ebi.intact.plugin.IntactAbstractMojo;

import java.io.File;
import java.io.IOException;

/**
 *   Plugin to analyze the performance of the classifier.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.0-SNAPSHOT
 *        <pre>
 *        13-Nov-2007
 *        </pre>
 */

/**
 * Mojo confidence score analyzer
 *
 * @goal analyze
 * @phase install
 */
public class Analyzer extends IntactAbstractMojo {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( Analyzer.class );

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
     * The path to the work directory
     *
     * @parameter expression="${highconfSetPath}" default-value="${project.build.outputDirectory}"
     */
    private String highconfSetPath;

    /**
     * The path to the work directory
     *
     * @parameter expression="${lowconfSetPath}" default-value="${project.build.outputDirectory}"
     */
    private String lowconfSetPath;

    /**
     * The max nr of running jobs at a time
     *
     * @parameter expression="${threshold}" default-value="${0.5}"
     */
    private double threshold;

    /**
     * The max nr of running jobs at a time
     *
     * @parameter expression="${equality}" default-value="${TRUE}"
     */
    private boolean equality;

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

    public MavenProject getProject() {
        return project;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        enableLogging();
        File workDir = new File( workDirPath );
        if ( !workDir.isDirectory() ) {
            workDir.mkdir();
        }
        if ( log.isInfoEnabled() ) {
            log.info( "Mojo started ..." );
            log.info( "workDirPath: " + workDirPath );
            log.info( "highconfSetPath: " + highconfSetPath );
            log.info( "lowconfSetPath: " + lowconfSetPath );
            log.info( "threshold: " + threshold );
            log.info( "equality: " + equality );
        }
        int nr = 100;
        AnalyzeMain amain = new AnalyzeMain( workDir, nr );
        File hcFile = new File( highconfSetPath );//"~/tmp/confidenceModel/bkup/benchmark/highconf_all_attribs.txt" );
        File lcFile = new File( lowconfSetPath );//"~/tmp/confidenceModel/bkup/benchmark/lowconf_all_attribs.txt" );
        amain.splitToTrainTest( hcFile, lcFile, 20, workDir );
        try {
            amain.generateGisInput( workDir ); //TODO: this was an old version RocAnalyzer should be used, example of XvFoldTest and RocAnalyzerTest
        } catch ( IOException e ) {
            throw new MojoExecutionException( e.toString());
        }
        amain.rocAnalyze( threshold, equality, workDir );
        if ( log.isInfoEnabled() ) {
            log.info( "Mojo done." );
        }
    }
}
