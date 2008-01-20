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

import uk.ac.ebi.intact.confidence.model.*;
import uk.ac.ebi.intact.confidence.model.io.ProteinAnnotationWriter;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationWriterImpl;
import uk.ac.ebi.intact.confidence.model.io.BlastInputReader;
import uk.ac.ebi.intact.confidence.model.io.impl.BlastInputReaderImpl;
import uk.ac.ebi.intact.confidence.util.GlobalData;
import uk.ac.ebi.intact.bridges.blast.BlastService;
import uk.ac.ebi.intact.bridges.blast.EbiWsWUBlast;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.jdbc.BlastJobEntity;
import uk.ac.ebi.intact.bridges.blast.model.*;

import java.util.*;
import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * It processes the annotations into the desired form.
 * For the Confidence Score it is meant the blast.
 * (Step 1a)
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        07-Dec-2007
 *        </pre>
 */
public class InfoProcessing {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( InfoProcessing.class );

    private BlastService bs;

    public InfoProcessing( File dbFolder, File blastArchiveDir, String email) throws BlastServiceException {
        this(dbFolder, blastArchiveDir,  email, 25);     
    }

    public InfoProcessing( File dbFolder, File blastArchiveDir, String email, int nrPerSubmission ) throws BlastServiceException {
        String tableName="job";
        bs = new EbiWsWUBlast(dbFolder, tableName, blastArchiveDir, email, nrPerSubmission);
        GlobalData.setCount( 0 );
    }


    public void process (File inFile, File outFile) throws IOException, BlastServiceException {
        BlastInputReader biReader = new BlastInputReaderImpl();
        Set<BlastInput> blastInputs = biReader.read2Set( inFile );
        process( blastInputs, outFile, new Float(0.001) );
    }

    public void process (Set<BlastInput> blastInputs, File outFile, Float threshold) throws BlastServiceException, IOException {
        ProteinAnnotationWriter paw = new ProteinAnnotationWriterImpl();
        Iterator<BlastInput> iterator = blastInputs.iterator();
        while(iterator.hasNext()){
            BlastInput bi = iterator.next();
            ProteinAnnotation pa = blast(bi, threshold);
            paw.append( pa, outFile );
        }       
    }

    

    public ProteinAnnotation blast( BlastInput blastInput, Float threshold) throws BlastServiceException {
        BlastResult br = bs.fetchAvailableBlast( blastInput.getUniprotAc() );
        if (br == null){
            BlastJobEntity jobEntity = bs.submitJob( blastInput );
            if ( log.isInfoEnabled() ) {
                log.info( "job: " + jobEntity );
            }
            while ( !jobEntity.getStatus().equals( BlastJobStatus.DONE )  && !jobEntity.getStatus().equals( BlastJobStatus.FAILED ) && !jobEntity.getStatus().equals( BlastJobStatus.NOT_FOUND )) {
                try {
                    Thread.sleep( 10000 );
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
                bs.refreshJob(jobEntity); 
            }
            br = bs.fetchAvailableBlast( blastInput.getUniprotAc() );
        }
        if (br != null){
            return processResult(br, threshold);
        }   else {
            return null;
        }
    }

    private ProteinAnnotation processResult( BlastResult result, Float threshold ) {
        GlobalData.increment( 1 );
        if ( ( GlobalData.getCount() % 20 ) == 0 ) {
            GlobalData.endTime = System.currentTimeMillis();
            long time = GlobalData.endTime - GlobalData.startTime;
            log.info( "for " + GlobalData.getCount() + " prots: " + ( ( time ) / 60000 ) );
            log.info( "ETA: " + GlobalData.eta( GlobalData.getCount(), time, GlobalData.totalProts ) + " (min)" );
        }
        ProteinAnnotation  pa = new ProteinAnnotation(new UniprotIdentifierImpl( result.getUniprotAc()));
        int nr =0;
        for ( Hit hit : result.getHits() ) {
            nr++;
            Float evalue = hit.getEValue();
            String ac = hit.getUniprotAc();
            if ( ac == null ) {
                if (log.isDebugEnabled()){
                    log.debug( "Ac is null, in a hit list! " + result.getUniprotAc() + ": hit nr " + nr  +" eValue: " + evalue);
                }
            } else {
                try{
                    Identifier id = new UniprotIdentifierImpl( ac);
                    if ( evalue < threshold ) {
                        pa.addAnnotation( id );
                    }
                } catch (IllegalArgumentException e){
                    if (log.isWarnEnabled()){
                        log.warn( "Ac is not proper formatted, in the hit list! " + result.getUniprotAc() +"(hit nr: " + nr + ")" + e.getMessage() );
                    }
                }
            }
        }
        return pa;
    }

    public void close() throws BlastServiceException {
        bs.close();
    }

//    public void writeBlastHits( BinaryInteractionSet biS, String outPath, Set<String> againstProteins,
//			File seqFile) throws BlastServiceException {
//		Set<UniprotAc> proteins = getUniprotAc(biS.getAllProtNames());
//		Set<UniprotAc> against = getUniprotAc(againstProteins);
//		try {
//			File alignFile = new File(workDir.getPath(), "set_align_biSet.txt");
//
//			Set<ProteinSimplified> prots = null;
//			Set<ProteinSimplified> againstProt = getProteinSimplified(against);
//			if (seqFile != null) {
//				DataMethods d = new DataMethods();
//				prots = d.readExactFasta(seqFile);
//                prots = retainProteins(prots, proteins);
//
//            } else {
//				prots = getProteinSimplified(proteins);
//			}
//
//			alignmentMaker.blast(prots, againstProt, new FileWriter(alignFile));
//			// TODO: solve once + for all the setting of the biSet for the
//			// filemaker
//			BinaryInteractionSet auxSet = fileMaker.getBiSet();
//			fileMaker.setBiSet(biS);
//			fileMaker.writeAnnotationAttributes(alignFile.getPath(), outPath);
//			fileMaker.setBiSet(auxSet);
//		} catch ( IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
