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
package uk.ac.ebi.intact.confidence.factory;

import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Mock factory for the needed objects when testing.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *               12-Dec-2007
 *               </pre>
 */
public class MockFactory {

    public static BinaryInteractionAttributes createBinaryIntWithAttribs() {
        Identifier idA = createDeterministicUniprotId( 1 );
        Identifier idB = createDeterministicUniprotId( 2 );
        List<Attribute> attribs = createDeterministicGoAttribs( 3 );
        BinaryInteractionAttributes bia = new BinaryInteractionAttributes( idA, idB, attribs, Confidence.UNKNOWN );
        return bia;
    }

    private static List<Attribute> createDeterministicGoAttribs( int nr ) {
        List<Attribute> attribs = new ArrayList<Attribute>();
        for ( int i = 0; i < nr; i++ ) {
            Attribute<Identifier> attr = new IdentifierAttributeImpl<GoIdentifierImpl>( new GoIdentifierImpl( "GO:000005" + i ), new GoIdentifierImpl( "GO:000005" + ( i + 1 ) ) );
            attribs.add( attr );
        }
        return attribs;
    }

    public static UniprotIdentifierImpl createDeterministicUniprotId( int i ) {
        if ( i == 1 ) {
            return new UniprotIdentifierImpl( "P12345" );
        } else if ( i == 2 ) {
            return new UniprotIdentifierImpl( "Q12345" );
        } else {
            return null;
        }
    }

    public static ProteinSimplified createDeterministicProteinSimplified( int nr ) {
        if ( nr == 1 ) {
            return new ProteinSimplified( new UniprotAc( "P12345" ) );
        } else if ( nr == 2 ) {
            return new ProteinSimplified( new UniprotAc( "Q12345" ) );
        } else {
            return new ProteinSimplified( new UniprotAc( "P123457" ) );
        }
    }

    public static ProteinSimplified createRandomProteinSimplified() {
        ProteinSimplified ps = new ProteinSimplified( new UniprotAc(createRandomUniprotId().getId() ));
        return ps;
    }

    public static GoIdentifierImpl createDeterministicGoId(int nr) {
        switch(nr){
            case 1: return new GoIdentifierImpl( "GO:0000001");
            case 2: return new GoIdentifierImpl( "GO:0000002");
            case 3: return new GoIdentifierImpl( "GO:0000003");
            default:
                return new GoIdentifierImpl( "GO:1000000");
        }
    }

     public static InterProIdentifierImpl createDeterministicInterProId(int nr) {
        switch(nr){
            case 1: return new InterProIdentifierImpl( "IPR000001");
            case 2: return new InterProIdentifierImpl( "IPR000002");
            case 3: return new InterProIdentifierImpl( "IPR000003");
            default:
                return new InterProIdentifierImpl( "IPR100000");
        }
    }

    public static UniprotIdentifierImpl createRandomUniprotId(){
        Random r = new Random();
        int aux = r.nextInt( 10 );
        if ( aux == 0 ) {
            aux += 1;
        }
        int i = r.nextInt( 10000 ) + aux * 10000; // always with 5 digits
        return new UniprotIdentifierImpl( "P" + i);
    }

    public static GoIdentifierImpl createRandomGoId() {
        Random r = new Random();
        int aux = r.nextInt( 10 );
        if ( aux == 0 ) {
            aux += 1;
        }
        int i = r.nextInt( 1000000 ) + aux * 1000000; // always with 7 digits
        return new GoIdentifierImpl( "GO:" + i );
    }

    public static InterProIdentifierImpl createRandomInterProId(){
        Random r = new Random();
        int aux = r.nextInt(10);
        if (aux ==0){
            aux +=1;
        }
        int i = r.nextInt( 100000 ) + aux * 1000000; // always with 6 digits
        return new InterProIdentifierImpl( "IPR"+i);
    }



    public static InteractionSimplified createDeterministicInteractionSimplified() {
        List<ProteinSimplified> interactors = Arrays.asList( createDeterministicProteinSimplified( 1 ), createDeterministicProteinSimplified( 2 ) );
        InteractionSimplified is = new InteractionSimplified( "test-int", interactors );
        return is;
    }

    public static InteractionSimplified createRandomInteractionSimplified() {
        List<ProteinSimplified> interactors = Arrays.asList( createRandomProteinSimplified(), createDeterministicProteinSimplified( 2 ) );
        InteractionSimplified is = new InteractionSimplified( "test-int", interactors );
        return is;
    }

    public static BinaryInteractionAttributes createDeterministicBinaryInteractionAttributes(){
        BinaryInteractionAttributes bia = new BinaryInteractionAttributes(createDeterministicUniprotId( 1 ), createDeterministicUniprotId( 2 ), Confidence.UNKNOWN);
        bia.addAttribute( createDeterministicAttribute(1));
        bia.addAttribute( createDeterministicAttribute(2));
        bia.addAttribute( createDeterministicAttribute(3));
        return bia;
    }

    public static BinaryInteractionAttributes createRandomBinaryInteractionAttributes(){
        BinaryInteractionAttributes bia = new BinaryInteractionAttributes(createRandomUniprotId(),createRandomUniprotId(),Confidence.UNKNOWN);               
        bia.addAttribute( createRandomAttribute() );
        bia.addAttribute( createRandomAttribute() );
        bia.addAttribute( createRandomAttribute() );
        return bia;
    }

    private static Attribute createDeterministicAttribute(int nr) {
        if (nr == 1){
            return new IdentifierAttributeImpl<GoIdentifierImpl>(createDeterministicGoId(1),createDeterministicGoId(2) );
        } else if (nr == 2){
            return new IdentifierAttributeImpl<InterProIdentifierImpl>(createDeterministicInterProId(1),createDeterministicInterProId(2) );
        } else{
            return new IdentifierAttributeImpl<UniprotIdentifierImpl>(createDeterministicUniprotId(1),createDeterministicUniprotId(2) );
        }
    }

     public static Attribute createRandomAttribute(){
         Random r = new Random();
         int  i = r.nextInt(3); // 0. uniprot; 1. go; 2. interpro
         return createRandomAttribute(i);
    }

    public static Attribute createRandomAttribute(int nr){
        if (nr == 1){
            return new IdentifierAttributeImpl<GoIdentifierImpl>(createRandomGoId(), createRandomGoId() );
        } else if (nr == 2){
            return new IdentifierAttributeImpl<InterProIdentifierImpl>(createRandomInterProId(),createRandomInterProId() );
        } else{
            return new IdentifierAttributeImpl<UniprotIdentifierImpl>(createRandomUniprotId(),createRandomUniprotId() );
        }
    }
}
