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
package uk.ac.ebi.intact.confidence.model.parser;

import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.ProteinAnnotation;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser util class for ProteinAnnotation files.
 * line: <uniprotAc>,<uniprotAc|GOIdentifier|InterProIdentifier>*
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *               12-Dec-2007
 *               </pre>
 */
public class ProteinAnnotationParserUtils {

    public static ProteinAnnotation parseProteinAnnotation( String line ) {
        String[] aux = line.split( "," );
        Identifier idA = BinaryInteractionParserUtils.parseIdentifier( aux[0] );
        if ( idA != null && aux.length > 1) {
            List<Identifier> annotations = new ArrayList<Identifier>( aux.length - 1 );
            for (int i =1; i< aux.length; i++){
                Identifier id = BinaryInteractionParserUtils.parseIdentifier( aux[i]);
                if (id != null){
                    annotations.add( id);
                }
            }
            return new ProteinAnnotation(idA, annotations);
        }
        return new ProteinAnnotation(idA);
    }
}
