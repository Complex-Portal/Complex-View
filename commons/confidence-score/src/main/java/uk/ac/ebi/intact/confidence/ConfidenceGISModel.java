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
import uk.ac.ebi.intact.confidence.util.DataMethods;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
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
            highConfNr = dataR.retrieveHighConfidenceSet( writer );
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
    public void retireveLowConfidenceSet( File fastaFile ) {
        if ( log.isInfoEnabled() ) {
            log.info( "generating low confidence interactions ..." );
        }
        generateLowconf( fastaFile, highConfNr );
        if ( log.isInfoEnabled() ) {
            log.info( "finished generating low confidence interactions." );
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

     protected void generateLowconf( File inFile, int nr ) {
        DataMethods dm = new DataMethods();
        // TODO: make sure the fasta file is in that directory + create uniprot
        // remote get Proteins ->fasta
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

}
