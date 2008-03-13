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

import uk.ac.ebi.intact.bridges.blast.model.BlastInput;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.model.io.BlastInputReader;
import uk.ac.ebi.intact.confidence.model.iterator.BlastInputIterator;
import uk.ac.ebi.intact.confidence.model.parser.BlastInputParserUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Implementation of the reader for BlastInput.
 * It parses a fasta file reading the information in BlastInput objects.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        14-Jan-2008
 *        </pre>
 */
public class BlastInputReaderImpl implements BlastInputReader {

    public List<BlastInput> read( File inFile ) throws IOException {
        List<BlastInput> bis = new ArrayList<BlastInput>();
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        String line ="";
        String regex = "^>.*";
        UniprotAc ac = null;
        Sequence seq = null;
        while ((line = br.readLine() ) != null){
            if ( Pattern.matches( regex, line )){
                ac = BlastInputParserUtils.parseUniprotInfo( line );  
            } else {
                seq = BlastInputParserUtils.parseSequence( line );
            }

            if (ac != null && seq != null){
                BlastInput bi = new BlastInput(ac, seq);
                bis.add( bi );
                ac = null;
                seq = null;
            }

        }
        return bis;
    }

    public Set<BlastInput> read2Set (File inFile) throws IOException{
        Set<BlastInput> bis = new HashSet<BlastInput>();
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        String line ="";
        String regex = "^>.*";
        UniprotAc ac = null;
        Sequence seq = null;
        while ((line = br.readLine() ) != null){
            if ( Pattern.matches( regex, line )){
                ac = BlastInputParserUtils.parseUniprotInfo( line );
            } else {
                seq = BlastInputParserUtils.parseSequence( line );
            }

            if (ac != null && seq != null){
                BlastInput bi = new BlastInput(ac, seq);
                bis.add( bi );
                ac = null;
                seq = null;
            }
        }
        return bis;
    }

    public Iterator<BlastInput> iterate( File inFile ) throws FileNotFoundException{
        BlastInputIterator iterator = new BlastInputIterator( new FileInputStream( inFile ));
        return iterator;
    }
}
