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
package uk.ac.ebi.intact.confidence.model.io.impl;

import org.junit.Assert;
import uk.ac.ebi.intact.confidence.model.*;
import uk.ac.ebi.intact.confidence.model.io.ProteinAnnotationReader;
import uk.ac.ebi.intact.confidence.model.iterator.ProteinAnnotationIterator;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * ProteinAnnotation File reader.
 * File format >uniprotAc(,annotation)*<
 * annoatation = {uniprotAc|GoId|InterProId}
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        07-Dec-2007
 *        </pre>
 */
public class ProteinAnnotationReaderImpl implements ProteinAnnotationReader {

    /**
     * Reads a file of prtoen annotaions.
     * @param inFile :  File with the protein annotations
     * @return list of ProteinAnnotation objects, not null
     */
    public List<ProteinAnnotation> read( File inFile ) throws IOException {
        List<ProteinAnnotation> proteins = new ArrayList<ProteinAnnotation>();
        BufferedReader br = new BufferedReader( new FileReader( inFile ) );
        String line = "";
        while ( ( line = br.readLine() ) != null ) {
            ProteinAnnotation protAnno = parseProteinAnnotation( line );
            proteins.add( protAnno );
        }
        br.close();
        return proteins;
    }

    private ProteinAnnotation parseProteinAnnotation( String line ) {
        String [] aux = line.split(",");
        Assert.assertTrue( aux.length > 0);
        Identifier id = new UniprotIdentifierImpl(aux[0]);
        List<Identifier> annotations = new ArrayList<Identifier>(aux.length - 1);
        for (int i =1; i< aux.length; i++){
            Identifier anno;
            if (Pattern.matches( GoIdentifierImpl.getRegex(), aux[i])){
                anno = new GoIdentifierImpl( aux[i]);
            } else if (Pattern.matches( InterProIdentifierImpl.getRegex(), aux[i] )){
                anno = new InterProIdentifierImpl( aux[i]);
            }else if (Pattern.matches( UniprotIdentifierImpl.getRegex(), aux[i] )){
                anno = new UniprotIdentifierImpl( aux[i]);
            }else if (Pattern.matches( IntActIdentifierImpl.getRegex(), aux[i] )){
                anno = new IntActIdentifierImpl( aux[i]);
            } else {
                anno = null;
            }
            if (anno != null){
                annotations.add(anno);
            }
        }
        ProteinAnnotation protAnno = new ProteinAnnotation(id, annotations);
        return protAnno;
    }

    public Set<ProteinAnnotation> read2Set (File inFile) throws IOException{
          Set<ProteinAnnotation> proteins = new HashSet<ProteinAnnotation>();
        BufferedReader br = new BufferedReader( new FileReader( inFile ) );
        String line = "";
        while ( ( line = br.readLine() ) != null ) {
            ProteinAnnotation protAnno = parseProteinAnnotation( line );
            proteins.add( protAnno );
        }
        br.close();
        return proteins;
    }

    public Iterator<ProteinAnnotation> iterate( File inFile ) throws FileNotFoundException {
       ProteinAnnotationIterator iterator = new ProteinAnnotationIterator( new FileInputStream( inFile ));
       return iterator;
    }
}
