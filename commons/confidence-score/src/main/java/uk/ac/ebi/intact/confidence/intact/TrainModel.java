/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.confidence.intact;

import opennlp.maxent.GISModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.confidence.attribute.SeqAlignFilter;
import uk.ac.ebi.intact.confidence.main.InfoFiltering;
import uk.ac.ebi.intact.confidence.main.InfoGathering;
import uk.ac.ebi.intact.confidence.main.InfoModelInput;
import uk.ac.ebi.intact.confidence.main.InfoProcessing;
import uk.ac.ebi.intact.confidence.maxent.MaxentUtils;
import uk.ac.ebi.intact.confidence.maxent.OpenNLPMaxEntClassifier;
import uk.ac.ebi.intact.confidence.model.*;
import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionAttributesWriter;
import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionReader;
import uk.ac.ebi.intact.confidence.model.io.ProteinAnnotationReader;
import uk.ac.ebi.intact.confidence.model.io.ProteinAnnotationWriter;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionAttributesWriterImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionReaderImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationReaderImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationWriterImpl;
import uk.ac.ebi.intact.confidence.utils.Merge;
import uk.ac.ebi.intact.context.IntactContext;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Class to train a model without having any preious computed data.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *                      17-Jan-2008
 *                      </pre>
 */
public class TrainModel {
    private static final Log log = LogFactory.getLog( TrainModel.class );

    public static IntactConfidenceCalculator generateModel() throws Exception {
        /**
         * File that must be specified: the yeast fasta file.
         * When creating the low confidence interactions the proteins in the interaction are
         * choosen out of hte yeast proteins. THIS MUST BE SET.
         */
        // put a proper yeast fasta file, or any fasta file containing the proteins for the low confidence generating step
        //TODO: make sure the fasta file is there, if it does not exist the low confidence set will not be created
        File fastaFile = new File( "/net/nfs6/vol1/homes/iarmean/tmp/40.S_cerevisiae.fasta" );

        /**
         * Files that can be specified, if some data has been preeprocessed.
         */
        //TODO: specifiy the working directory, if not specified than the tmp dir will be choosen
        // File workDir = new File(System.getProperty( "java.io.tmpdir" ), "TrainModel");
        File workDir = new File("/net/nfs7/vol22/sp-pro5/20080201_iarmean/TrainModel");

        /**
         * Information for the blast configuration, if there is prerun blast results available:
         * - dbFolder: the folder where the h2 db is   : optional
         * - blastArchiveDir: folder containing the blast result xml files (ex for a file : "P12345.xml") : optional
         * - email: the email for the blast service  : compulsory
         */
         //TODO: specifiy dbFolder and blastArchiveDir and email
//        File dbFolder = new File( "/net/nfs6/vol1/homes/iarmean/tmp\\blastDb" );
//        File blastArchiveDir =  new File ("/net/nfs7/vol22/sp-pro5/20080216_iarmean");
//        String email = "myName@yahuo.com";
        File dbFolder = new File(workDir.getParentFile(), "blastDb");
        dbFolder.mkdir();
        File blastArchiveDir = new File(workDir.getParentFile(), "archive");
        blastArchiveDir.mkdir();
        String email = "iarmean@ebi.ac.uk";
        

        if(log.isInfoEnabled()){
            log.info("workDir: "+ workDir.getPath());
            log.info("yeastFastaFile: "+ fastaFile.getPath());
            log.info("blastDbFolder: "+ dbFolder.getPath());
            log.info("blastArchive: " + blastArchiveDir.getPath());
            log.info("email: "+ email);
        }

        /**
         * Retreive the high confidence data set from IntAct.
         * In the same time the medium confidence set will be also retrieved.
         * For each confidence set additional 3 files will be created:
         * ex. for high confidence: highconf_set.txt, highconf_set_ip.txt
         *  highconf_set_go.txt, highconf_set_seq.txt
         */
        long start = ( System.currentTimeMillis() / 1000 );
        File pgConfigFile = new File(TrainModel.class.getResource("/hibernate.iweb2.cfg.xml").getFile());
        IntactContext.initStandaloneContext(pgConfigFile);


        InfoGathering infoG = new InfoGathering();
        Report report = infoG.retrieveHighConfidenceAndMediumConfidenceSetWithAnnotations( workDir );
        long time = ( System.currentTimeMillis() / 1000 );
//        if ( log.isInfoEnabled() ) {
//            long total = time - start;
//            log.info( "Retrieval of high confidence and medium confidence with annotations: " + total );
//        }


        /**
         *    Generate the same amount as high confidences low confidence:
         * - read the high confidence set
         * - generate low confidence set
         * - get the annotation for the low confidence set
         */

        BinaryInteractionReader bir = new BinaryInteractionReaderImpl();
        bir.setConfidence( Confidence.HIGH );
     //   File hcFile = new File( workDir, "highconf_set.txt" );
        Set<BinaryInteraction> birs = bir.read2Set( report.getHighconfFile() );


       //infoG.retrieveLowConfidenceSet( workDir, fastaFile, birs.size() );
       infoG.retrieveLowConfidenceSet( report, fastaFile, birs.size() );
        //File lowconfFile = new File( workDir, "lowconf_set.txt" );
        infoG.retrieveLowConfidenceSetAnnotations( report );
        long time2 = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time2 - time;
            log.info( "Generating low confidence set with annotation: " + total );
        }

        /**
         * Generate the blast annotation files (blast results). The blast results are filtered only by a threshold (0.001)
         *
         */

        InfoProcessing ip = new InfoProcessing( dbFolder, blastArchiveDir, email );
      //  File seqFile = new File( workDir, "highconf_set_seq.txt" );
        File outFile = new File( workDir, "highconf_set_seq_anno.txt" );
        ip.process( report.getHighconfSeqFile(), outFile );
        time = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time - time2;
            log.info( "Blasting high confidence set : " + total );
        }
      //  File inFile = new File( workDir, "lowconf_set_seq.txt" );
        File outFile2 = new File( workDir, "lowconf_set_seq_anno.txt" );
        ip.process( report.getLowconfSeqFile(), outFile2 );
        time2 = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time2 - time;
            log.info( "Blasting low confidence set : " + total );
        }

        /**
         * Filter the GOs and the blast uniprotAcs according to the high confidence proteins.
         *
         */
      //  File goAnnoFile = new File( workDir, "highconf_set_go.txt" );
        ProteinAnnotationReader bar = new ProteinAnnotationReaderImpl();
        outFile = new File( workDir, "highconf_set_go_filter.txt" );
        Set<ProteinAnnotation> pas = bar.read2Set( report.getHighconfGOFile() );
        InfoFiltering.filterGo( pas );
        ProteinAnnotationWriter paw = new ProteinAnnotationWriterImpl();
        paw.write( pas, outFile );
        time = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time - time2;
            log.info( "Filter GOs for high confidence set : " + total );
        }

//        goAnnoFile = new File( workDir, "lowconf_set_go.txt" );
        outFile = new File( workDir, "lowconf_set_go_filter.txt" );
        pas = bar.read2Set( report.getLowconfGOFile());
        InfoFiltering.filterGo( pas );
        paw.write( pas, outFile );
        time2 = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time2 - time;
            log.info( "Filtering GOs for low confidence set : " + total );
        }

        File seqAnnoFile = new File( workDir, "highconf_set_seq_anno.txt" );
        Set<ProteinAnnotation> proteinAnnotation = bar.read2Set( seqAnnoFile );
        SeqAlignFilter.filter( proteinAnnotation );
        outFile = new File( workDir, "highconf_set_seq_anno_filter.txt" );
        paw.write( proteinAnnotation, outFile );
        time = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time - time2;
            log.info( "Filter Blasts for high confidence set : " + total );
        }

        seqAnnoFile = new File( workDir, "lowconf_set_seq_anno.txt" );
        proteinAnnotation = bar.read2Set( seqAnnoFile );
        SeqAlignFilter.filter( proteinAnnotation );
        outFile = new File( workDir, "lowconf_set_seq_anno_filter.txt" );
        paw.write( proteinAnnotation, outFile );
        time2 = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time2 - time;
            log.info( "Filtering Blasts for low confidence set : " + total );
        }

        /**
         * Combine the annotations to attributes.
         */
        //File biFile = new File( workDir, "highconf_set.txt" );
        List<BinaryInteraction> binaryInteractions = bir.read( report.getHighconfFile() );
        File annoFile = new File( workDir, "highconf_set_seq_anno_filter.txt" );
        List<BinaryInteractionAttributes> attribs = InfoModelInput.populateAttributes( binaryInteractions, annoFile );
        BinaryInteractionAttributesWriter biaw = new BinaryInteractionAttributesWriterImpl();
        outFile = new File( workDir, "highconf_set_seq_attribs.txt" );
        biaw.write( attribs, outFile );
        time = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time - time2;
            log.info( "Creating  blast attributes for high confidence set : " + total );
        }
        annoFile = new File( workDir, "highconf_set_go_filter.txt" );
        attribs = InfoModelInput.populateAttributes( binaryInteractions, annoFile );
        outFile = new File( workDir, "highconf_set_go_attribs.txt" );
        biaw.write( attribs, outFile );
        time2 = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time2 - time;
            log.info( "Creating GO attributes for high confidence set : " + total );
        }
        annoFile = new File( workDir, "highconf_set_ip.txt" );
        attribs = InfoModelInput.populateAttributes( binaryInteractions, annoFile );
        outFile = new File( workDir, "highconf_set_ip_attribs.txt" );
        biaw.write( attribs, outFile );
        time = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time - time2;
            log.info( "Creating IP attributes for high confidence set : " + total );
        }

       // biFile = new File( workDir, "lowconf_set.txt" );
        binaryInteractions = bir.read( report.getLowconfFile() );
        annoFile = new File( workDir, "lowconf_set_seq_anno_filter.txt" );
        attribs = InfoModelInput.populateAttributes( binaryInteractions, annoFile );
        outFile = new File( workDir, "lowconf_set_seq_attribs.txt" );
        biaw.write( attribs, outFile );
        time2 = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time2 - time;
            log.info( "Creating blast attributes for low confidence set : " + total );
        }
        annoFile = new File( workDir, "lowconf_set_go_filter.txt" );
        attribs = InfoModelInput.populateAttributes( binaryInteractions, annoFile );
        outFile = new File( workDir, "lowconf_set_go_attribs.txt" );
        biaw.write( attribs, outFile );
        time = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time - time2;
            log.info( "Creating GO attributes for low confidence set : " + total );
        }
        annoFile = new File( workDir, "lowconf_set_ip.txt" );
        attribs = InfoModelInput.populateAttributes( binaryInteractions, annoFile );
        outFile = new File( workDir, "lowconf_set_ip_attribs.txt" );
        biaw.write( attribs, outFile );
        time2 = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time2 - time;
            log.info( "Creating IP attributes for low confidence set : " + total );
        }

        /**
         * Merge the attributes.
         *
         */

        File attribsSeq = new File( workDir, "highconf_set_seq_attribs.txt" );
        File attribsIp = new File( workDir, "highconf_set_ip_attribs.txt" );
        File attribsGo = new File( workDir, "highconf_set_go_attribs.txt" );
        String[] paths = {attribsSeq.getPath(), attribsIp.getPath(), attribsGo.getPath()};
        File hcSet = new File( workDir, "highconf_set_attribs.txt" );
        ( new Merge() ).merge( paths, hcSet.getPath() );
        time = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time - time2;
            log.info( "Merged attributes for high confidence set : " + total );
        }

        attribsSeq = new File( workDir, "lowconf_set_seq_attribs.txt" );
        attribsIp = new File( workDir, "lowconf_set_ip_attribs.txt" );
        attribsGo = new File( workDir, "lowconf_set_go_attribs.txt" );
        String[] paths2 = {attribsSeq.getPath(), attribsIp.getPath(), attribsGo.getPath()};
        File lcSet = new File( workDir, "lowconf_set_attribs.txt" );
        ( new Merge() ).merge( paths2, lcSet.getPath() );
        time2 = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time2 - time;
            log.info( "Merged attributes for low confidence set : " + total );
        }

        /**
         *  Generate the model.
         */

        OpenNLPMaxEntClassifier classifier = new OpenNLPMaxEntClassifier( hcSet.getPath(), lcSet.getPath(), workDir );
        GISModel model = classifier.getModel();
        outFile = new File( workDir, "gisModel.txt" );
        MaxentUtils.writeModelToFile( model, outFile );
        time = ( System.currentTimeMillis() / 1000 );
        if ( log.isInfoEnabled() ) {
            long total = time - time2;
            log.info( "Model trained and printed out : " + total );
        }

        return null;
    }

}
