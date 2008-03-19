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
package uk.ac.ebi.intact.confidence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.confidence.dataRetriever.DataRetrieverException;
import uk.ac.ebi.intact.confidence.dataRetriever.DataRetrieverStrategy;
import uk.ac.ebi.intact.confidence.dataRetriever.IntactDbRetriever;
import uk.ac.ebi.intact.confidence.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.confidence.expansion.SpokeExpansion;
import uk.ac.ebi.intact.confidence.model.BinaryInteraction;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionReader;
import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionWriter;
import uk.ac.ebi.intact.confidence.model.io.FastaReader;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionReaderImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionWriterImpl;
import uk.ac.ebi.intact.confidence.model.io.impl.FastaReaderImpl;
import uk.ac.ebi.intact.confidence.util.InteractionGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of the ConfidenceModel.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0 - SNAPSHOT
 *        <pre>
 *               03-Dec-2007
 *               </pre>
 */
public class ConfidenceGISModel implements ConfidenceModel {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( ConfidenceGISModel.class );
    private File workDir;
    private int highConfNr = 10000;

    public void setExpansion( ExpansionStrategy expansion ) {
        this.expansion = expansion;
    }

    private ExpansionStrategy expansion;

    public ConfidenceGISModel( File workDir ) {
        this.workDir = workDir;
        expansion = new SpokeExpansion();
    }


    public void retireveHighConfidenceSet() {
        DataRetrieverStrategy dataR = new IntactDbRetriever( workDir, expansion );
        try {
            Writer writer = new FileWriter( new File( workDir, "hc_all.txt" ) );
            highConfNr = dataR.retrieveHighConfidenceSet( workDir );
        } catch ( DataRetrieverException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Before performing this step, the high confidence and medium confidence sets from teh db schould be in the workDir.
     * Generates random binary interactions. Default is 10000 if it was not replaced by the size of the high confidence list.
     *
     * @param fastaFile
     */
    public void retireveLowConfidenceSet( File fastaFile ) throws IOException {
        if ( log.isTraceEnabled() ) {
            log.trace( "generating low confidence interactions ..." );
        }
        generateLowconf( fastaFile, highConfNr );
        if ( log.isTraceEnabled() ) {
            log.trace( "finished generating low confidence interactions." );
        }
    }

    public void retrieveHighConfidenceSetWithAnnotations( File workDir ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void retrieveHighConfidenceSetAndMediumConfidenceSetWithAnnotations( File workDir ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void retrieveLowConfidenceSet( File workDir, File fastaFile, int nr ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void retrieveLowConfidenceSetAnnotations( File workDir, File lowconfFile ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void processAnnotations( File workDir ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void buildAttributes() {
       
    }

    public File trainModel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    ////////   -----------------------------------------------------------------------
    // protected

     protected void generateLowconf( File inFile, int nr ) throws IOException {
        if ( !inFile.exists() ) {
            throw new RuntimeException( inFile.getAbsolutePath() );
        }

         try {
             FastaReader fr = new FastaReaderImpl();

             Set<Identifier> yeastProteins = fr.readProteins( inFile );
             BinaryInteractionReader bir = new BinaryInteractionReaderImpl();
             Set<BinaryInteraction> forbidden = new HashSet<BinaryInteraction>();
             forbidden.addAll( bir.read2Set( new File(workDir, "highconf_set.txt")) );
             forbidden.addAll( bir.read2Set(new File(workDir, "medconf_set.txt")) );

             Set<BinaryInteraction> generated = InteractionGenerator.generate( yeastProteins, forbidden, nr );
             BinaryInteractionWriter biw = new BinaryInteractionWriterImpl();
             File outFile = new File (workDir, "lowconf_set.txt");
             biw.write( generated, outFile );
         } catch ( IOException e ) {
             throw  e ;
         }         
    }

}
