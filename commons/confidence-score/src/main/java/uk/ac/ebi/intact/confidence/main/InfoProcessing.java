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

import java.util.*;

/**
 * It processes the annotations into the desired form.
 * For the Confidence Score it is mean the blast.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        07-Dec-2007
 *        </pre>
 */
public class InfoProcessing {

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
