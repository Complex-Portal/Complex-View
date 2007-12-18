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
package uk.ac.ebi.intact.confidence.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.attribute.GoFilter;
import uk.ac.ebi.intact.confidence.dataRetriever.IntactDbRetriever;
import uk.ac.ebi.intact.confidence.dataRetriever.uniprot.UniprotDataRetriever;
import uk.ac.ebi.intact.confidence.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.confidence.expansion.SpokeExpansion;
import uk.ac.ebi.intact.confidence.main.exception.InfoGatheringException;
import uk.ac.ebi.intact.confidence.model.BinaryInteraction;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionReader;
import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionReaderImpl;
import uk.ac.ebi.intact.confidence.util.DataMethods;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        07-Dec-2007
 *        </pre>
 */
public class InfoGathering {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( InfoGathering.class );
    private ExpansionStrategy expansion;

    public InfoGathering( ExpansionStrategy expansion){
        this.expansion = expansion;

    }

   public void retrieveHighConfidenceAndMediumConfidenceSetWithAnnotations( File workDir) throws InfoGatheringException, IOException {
       IntactDbRetriever intactdb = new IntactDbRetriever( workDir, expansion);
       File hcFile = new File(workDir, "hc_set.txt");
       File mcFile = new File(workDir, "lc_set.txt");
       intactdb.readConfidences(hcFile, mcFile );
   }
    
    /**
     *
     * @param workDir : must contain a highconf_set.txt and a medconf_set.txt file
     * @param fastaFile : for the set of proteins the low confidences will be generated form.
     * @param nr : optional, if n >0 n low confidence interactions will be generated, if n<=0 will generate as many low confidences as high confidences found
     */
    public File retrieveLowConfidenceSet(File workDir, File fastaFile, int nr){
          return generateLowconf( workDir, fastaFile, nr);
    }

    public void retrieveLowConfidenceSetAnnotations(File workDir, File lowconfFile){
        try {
            BinaryInteractionSet lowConf = new BinaryInteractionSet( lowconfFile.getPath() );
            File dirForAttrib = new File( workDir, "DataRetriever" );
            writeIpGoForLc( lowConf.getAllProtNames(), dirForAttrib );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

     protected File generateLowconf( File workDir, File inFile, int nr ) {
        DataMethods dm = new DataMethods();
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
            File lcFile = new File( workDir.getPath(), "lowconf_all.txt" );
            dm.export( lowConf, lcFile );
            return lcFile;
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
         return null;
    }


    private void writeIpGoForLc( Set<String> lowConfProt, File dirForAttrib ) {
           UniprotDataRetriever uniprot = new UniprotDataRetriever();
           try {
               writeGo( uniprot, lowConfProt, new FileWriter( new File( dirForAttrib, "lowconf_uniprot_go.txt" ) ) );
               writeIp( uniprot, lowConfProt, new FileWriter( new File( dirForAttrib, "lowconf_uniprot_ip.txt" ) ) );
               writeSeq( uniprot, lowConfProt, new FileWriter( new File( dirForAttrib, "lowconf_uniprot_seq.txt" ) ) );
           } catch ( IOException e ) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }

       }

       private void writeIp( UniprotDataRetriever uniprot, Set<String> lowConfProt, Writer fileWriter ) {
           try {
               for ( String ac : lowConfProt ) {
                   Set<Identifier> ips = uniprot.getIps( new UniprotAc( ac ) );
                   fileWriter.append( ac + "," );
                   for ( Identifier ipId : ips ) {
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

       private void writeGo( UniprotDataRetriever uniprot, Set<String> lowConfProt, Writer fileWriter ) {
           try {
               for ( String ac : lowConfProt ) {
                   Set<Identifier> gos = uniprot.getGos( new UniprotAc( ac ) );
                   GoFilter.filterForbiddenGos( gos);
                   fileWriter.append( ac + "," );
                   for ( Identifier goId : gos ) {
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

       private void writeSeq( UniprotDataRetriever uniprot, Set<String> lowConfProt, Writer fileWriter ) {
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
    
       public static void main(String[] args) {
            // 1. InfoGathering
           InfoGathering infoG = new InfoGathering( new SpokeExpansion() );
           File workDir = new File( "E:\\tmp", "ConfMain" );
           workDir.mkdir();
           try {
               infoG.retrieveHighConfidenceAndMediumConfidenceSetWithAnnotations( workDir );
           } catch ( InfoGatheringException e ) {
               e.printStackTrace();
           } catch ( IOException e ) {
               e.printStackTrace();
           }

//           BinaryInteractionReader bir = new BinaryInteractionReaderImpl();
//           File inFile = new File( workDir, "hc_set.txt" );
//           //File inFile = new File("H:\\projects\\intact-current\\service\\commons\\confidence-score\\target\\test.txt");
//           Set<BinaryInteraction> setInts = new HashSet<BinaryInteraction>(bir.read( inFile ));
//           int lcNr = setInts.size();
//
//           File fastaFile = new File( "yeast fasta path in here" );
//           File lcFile = infoG.retrieveLowConfidenceSet( workDir, fastaFile, lcNr );
//           if ( lcFile != null ) {
//               infoG.retrieveLowConfidenceSetAnnotations( workDir, lcFile );
//           }
       }

}
