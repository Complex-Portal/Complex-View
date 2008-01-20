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
import uk.ac.ebi.intact.confidence.model.io.ProteinAnnotationReader;
import uk.ac.ebi.intact.confidence.model.io.impl.ProteinAnnotationReaderImpl;

import java.util.*;
import java.io.File;
import java.io.IOException;

/**
 * Combines the procesed information into the appropiate input for the model to be trained.
 * Anntations will be in form of attributes.
 * All the attributes will be mearged together.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
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


    public static List<BinaryInteractionAttributes> populateAttributes(List<BinaryInteraction> binaryInteractions, File annotationFile) throws IOException {
        Map<Identifier, ProteinAnnotation > annoMap = createMap(annotationFile);
        List<BinaryInteractionAttributes> populated = new ArrayList<BinaryInteractionAttributes>();
        for ( Iterator<BinaryInteraction> biaIter = binaryInteractions.iterator(); biaIter.hasNext(); )
        {
            BinaryInteraction bi =  biaIter.next();

            ProteinAnnotation protA = annoMap.get( bi.getFirstId()  );
            ProteinAnnotation protB = annoMap.get( bi.getSecondId() );

            BinaryInteractionAttributes bia = new BinaryInteractionAttributes(bi.getFirstId(), bi.getSecondId(), Confidence.UNKNOWN);
            populateAttributes( bia, protA, protB  );

            populated.add( bia );
        }
        return populated;
    }

     /**
     *  Combines the protein annotations an populates the BinaryInteractionAttributes.
     * If other Attributes are present the new ones will be added to them.
     * @param binaryInteractionAttribs
     * @param firstProtein
     * @param secondProtein
     */
    public static void populateAttributes( BinaryInteractionAttributes binaryInteractionAttribs, ProteinAnnotation firstProtein, ProteinAnnotation secondProtein){
        if(binaryInteractionAttribs.getFirstId().equals( firstProtein.getId()) &&  binaryInteractionAttribs.getSecondId().equals( secondProtein.getId())){
            List<Attribute> attribs = combine(firstProtein.getAnnotations(), secondProtein.getAnnotations());
            binaryInteractionAttribs.addAttributes(attribs );
        }   else {
            throw new IllegalArgumentException("Proteins and binary interactin do not match!");
        }
    }

    protected static List<Attribute> combine ( Collection<Identifier> idsA, Collection<Identifier> idsB){
        return combine(new HashSet<Identifier>(idsA), new HashSet<Identifier>(idsB));
    }

    protected static List<Attribute> combine( Set<Identifier> idsA, Set<Identifier> idsB ) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        if (idsA.size() == 0 || idsB.size() == 0){
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
        } else if (idA instanceof UniprotIdentifierImpl && idB instanceof UniprotIdentifierImpl) {
            return new IdentifierAttributeImpl<UniprotIdentifierImpl>( new UniprotIdentifierImpl( idA.getId()), new UniprotIdentifierImpl( idB.getId()));
        }
        return null;
    }

    private static Map<Identifier, ProteinAnnotation> createMap( File annotationFile ) throws IOException {
        ProteinAnnotationReader reader = new ProteinAnnotationReaderImpl();
        List<ProteinAnnotation> annotations = reader.read( annotationFile);
        System.out.println(annotations.size());

        Map<Identifier, ProteinAnnotation> mapped = new HashMap<Identifier, ProteinAnnotation>();
        for (int i =0; i< annotations.size(); i++){
            ProteinAnnotation pa = annotations.get( i );
            mapped.put( pa.getId(), pa );
        }

        return mapped;
    }
   
}
