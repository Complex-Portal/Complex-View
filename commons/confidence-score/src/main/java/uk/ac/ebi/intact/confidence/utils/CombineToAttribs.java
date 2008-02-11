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

import uk.ac.ebi.intact.confidence.model.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Combines 2 list of annotations to a list of attributes.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        12-Dec-2007
 *        </pre>
 */
public class CombineToAttribs {

    public static List<Attribute> combine( Set<Identifier> idsA, Set<Identifier> idsB ) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        if(idsA == null || idsB == null){
            return attributes;
        }
        for ( Iterator<Identifier> idItA = idsA.iterator(); idItA.hasNext(); ) {
            Identifier idA = idItA.next();
            for ( Iterator<Identifier> idItB = idsB.iterator(); idItB.hasNext(); ) {
                Identifier idB = idItB.next();
                if ( !idA.equals( idB ) ) {
                    Attribute attr = properAttribute( idA, idB ); //new GoPairAttribute( new GoTermPair( goIdA.getId(), goIdB.getId() ) );
                    // for the reverse part the GoTermPair comes in action, because it sorts the names
                    if ( !attributes.contains( attr ) ) {
                        attributes.add( attr );
                    }
                }
            }
        }
        return attributes;
    }

     private static Attribute properAttribute( Identifier idA, Identifier idB ) {
        if ( idA instanceof GoIdentifierImpl && idB instanceof GoIdentifierImpl ) {
            return new IdentifierAttributeImpl<GoIdentifierImpl>( new GoIdentifierImpl(idA.getId()), new GoIdentifierImpl(idB.getId() ) );
        } else if ( idA instanceof InterProIdentifierImpl && idB instanceof InterProIdentifierImpl ) {
            return new IdentifierAttributeImpl<InterProIdentifierImpl>( new InterProIdentifierImpl( idA.getId()), new InterProIdentifierImpl(idB.getId() ) );
        }
        return null;
    }
}
