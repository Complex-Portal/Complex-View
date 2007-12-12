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
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;
import uk.ac.ebi.intact.confidence.model.UniprotIdentifierImpl;

import java.util.HashSet;
import java.util.Iterator;
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
public class ConversionUtils {
    private ConversionUtils(){};

     public static Set<ProteinSimplified> getProteinList( Set<UniprotAc> proteins ) {
        Set<ProteinSimplified> prots = new HashSet<ProteinSimplified>( proteins.size() );
        for ( UniprotAc uniprotAc : proteins ) {
            prots.add( new ProteinSimplified( uniprotAc ) );
        }
        return prots;
    }

    public static Set<Identifier> convert2Id(Set<UniprotAc> uniprotAcs){
        if (uniprotAcs == null){
            return new HashSet<Identifier>(0);
        }
        Set<Identifier> ids = new HashSet<Identifier>();
        for ( Iterator<UniprotAc> acIter = uniprotAcs.iterator(); acIter.hasNext(); ) {
            UniprotAc ac =  acIter.next();
            Identifier id = new UniprotIdentifierImpl(ac);
            ids.add(id);
        }
        return ids;        
    }
}
