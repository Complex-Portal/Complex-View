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
 * Combines the procesed information into the appropiate input for the model to be trained.
 * Anntations will be in form of attributes.
 * All the attributes will be mearged together.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        10-Dec-2007
 *        </pre>
 */
public class InfoModelInput {

//
//    /**
//     *  Combines the protein annotations an populates the BinaryInteractionAttributes.
//     * If other Attributes are present the new ones will be added to them.
//     * @param binaryInteractionAttribs
//     * @param firstProtein
//     * @param secondProtein
//     */
//    public void populateAttributes( List<BinaryInteractionAttributes> binaryInteractionAttribs, List<ProteinAnnotation> firstProtein, ProteinAnnotation secondProtein){
//        for ( Iterator<BinaryInteractionAttributes> interactionAttributesIterator = binaryInteractionAttribs.iterator(); interactionAttributesIterator.hasNext(); )
//        {
//            BinaryInteractionAttributes interactionAttributes = interactionAttributesIterator.next();
//            firstProtein.indexOf(new ProteinAnnotaion(interactionAttributes.getFirstId()) );
//        }
//
//        if(binaryInteractionAttribs.getFirstId().equals( firstProtein.getId()) &&  binaryInteractionAttribs.getSecondId().equals( secondProtein.getId())){
//            List<Attribute> attribs = combine(firstProtein.getAnnotations(), secondProtein.getAnnotations());
//            binaryInteractionAttribs.addAttributes(attribs );
//        }   else {
//            throw new IllegalArgumentException("Proteins and binary interactin do not match!");
//        }
//    }

     /**
     *  Combines the protein annotations an populates the BinaryInteractionAttributes.
     * If other Attributes are present the new ones will be added to them.
     * @param binaryInteractionAttribs
     * @param firstProtein
     * @param secondProtein
     */
    public void populateAttributes( BinaryInteractionAttributes binaryInteractionAttribs, ProteinAnnotation firstProtein, ProteinAnnotation secondProtein){
        if(binaryInteractionAttribs.getFirstId().equals( firstProtein.getId()) &&  binaryInteractionAttribs.getSecondId().equals( secondProtein.getId())){
            List<Attribute> attribs = combine(firstProtein.getAnnotations(), secondProtein.getAnnotations());
            binaryInteractionAttribs.addAttributes(attribs );
        }   else {
            throw new IllegalArgumentException("Proteins and binary interactin do not match!");
        }
    }

    protected List<Attribute> combine ( Collection<Identifier> idsA, Collection<Identifier> idsB){
        return combine(new HashSet<Identifier>(idsA), new HashSet<Identifier>(idsB));
    }

    protected List<Attribute> combine( Set<Identifier> idsA, Set<Identifier> idsB ) {
        List<Attribute> attributes = new ArrayList<Attribute>();
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

     private Attribute properAttribute( Identifier idA, Identifier idB ) {
        if ( idA instanceof GoIdentifierImpl && idB instanceof GoIdentifierImpl ) {
            return new IdentifierAttributeImpl<GoIdentifierImpl>( new GoIdentifierImpl(idA.getId()), new GoIdentifierImpl(idB.getId() ) );
        } else if ( idA instanceof InterProIdentifierImpl && idB instanceof InterProIdentifierImpl ) {
            return new IdentifierAttributeImpl<InterProIdentifierImpl>( new InterProIdentifierImpl( idA.getId()), new InterProIdentifierImpl(idB.getId() ) );
        }
        return null;
    }
   
}
