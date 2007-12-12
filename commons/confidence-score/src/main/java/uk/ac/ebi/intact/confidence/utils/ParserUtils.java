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
package uk.ac.ebi.intact.confidence.utils;

import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        30-Nov-2007
 *        </pre>
 */
public class ParserUtils {

    /**
     *
     * @param attributesInFile : File of the format (uniprotAc;UniprotAc,attr1;attr2 ...)
     * @return : set of uniprotAcs
     */
    public static Set<UniprotAc> parseProteins( File attributesInFile){
        Set<UniprotAc> proteins = new HashSet<UniprotAc>();
        try {
            BufferedReader br =  new BufferedReader(new FileReader(attributesInFile));
            String line ="";
            while((line= br.readLine())!= null){
                String [] aux = line.split(",");
                String[] acs = aux[0].split(";");
                UniprotAc acA = new UniprotAc( acs[0]);
                UniprotAc acB = new UniprotAc(acs[1]);
                proteins.add( acA);
                proteins.add(acB);
            }
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return proteins;
    }
}
