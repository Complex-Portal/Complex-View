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
package uk.ac.ebi.intact.confidence.model.io.impl;

import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;
import uk.ac.ebi.intact.confidence.model.UniprotIdentifierImpl;
import uk.ac.ebi.intact.confidence.model.io.FastaReader;
import uk.ac.ebi.intact.confidence.model.parser.BlastInputParserUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Implementation of the FastaReader interface.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0 - SNAPSHOT
 */
public class FastaReaderImpl implements FastaReader {

   public Set<Identifier> readProteins( File inFile ) throws IOException {
        Set<Identifier> ids = new HashSet<Identifier>();
        Set<ProteinSimplified> data = read2Set( inFile );
        for ( Iterator<ProteinSimplified> iter = data.iterator(); iter.hasNext(); ) {
            ProteinSimplified protS = iter.next();
            ids.add( new UniprotIdentifierImpl(protS.getUniprotAc()) );
        }
        return ids;
    }

     public Set<ProteinSimplified> read2Set( File inFile) throws IOException {
         Set<ProteinSimplified> proteins = new HashSet<ProteinSimplified>();
         BufferedReader br = new BufferedReader( new FileReader( inFile ) );
         String line = "";
         String regex = "^>.*";
         UniprotAc ac = null;
         String seqStr ="";
         while ( ( line = br.readLine() ) != null ) {
            if ( Pattern.matches( regex, line )){
                if (ac != null){
                    //save current ac
                    if (!seqStr.equals( "" )) {
                        Sequence seq = new Sequence(seqStr);
                        ProteinSimplified protS = new ProteinSimplified( ac, seq);
                        proteins.add( protS );
                        seqStr = "";
                    }
                }
                ac = BlastInputParserUtils.parseUniprotInfo( line );
            } else {
                seqStr += line.trim();
                //seq = BlastInputParserUtils.parseSequence( line );
            }
         }
         if (ac != null && !seqStr.equals( "" )){
             Sequence seq = new Sequence(seqStr);
             ProteinSimplified protS = new ProteinSimplified( ac, seq);
             proteins.add( protS );
         }
         br.close();
         return proteins;
     }

}
