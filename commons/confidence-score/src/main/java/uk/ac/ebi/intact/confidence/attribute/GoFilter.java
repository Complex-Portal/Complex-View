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
package uk.ac.ebi.intact.confidence.attribute;

import uk.ac.ebi.intact.confidence.model.GoIdentifierImpl;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.Attribute;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

/**
 * Filter for the GO terms.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        29-Nov-2007
 *        </pre>
 */
public class GoFilter implements AnnotationConstants{

    //////////////////
    // Public Methods.
    public static void filterForbiddenGos( Collection<Identifier> gos ){
        Set<Identifier> forbiddenGos = getForbidden();
        gos.removeAll( forbiddenGos);
    }

    public static void filterAttributesGos(Collection<Attribute> attributes){
        for ( Iterator<Attribute> iterator = attributes.iterator(); iterator.hasNext(); ) {
            Attribute attr = iterator.next();
            attr.getFirstElement();
            //TODO: finalize this method
        }
    }

    ///////////////////
    // Private Methods.
    private static Set<Identifier> getForbidden() {
        Set<Identifier> gos = new HashSet<Identifier>(forbiddenGoTerms.length);
        for (String goTerm : forbiddenGoTerms){
            gos.add( new GoIdentifierImpl(goTerm));
        }
        return gos;       
    }
}
