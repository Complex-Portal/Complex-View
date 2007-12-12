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
package uk.ac.ebi.intact.confidence.test;

import static org.junit.Assert.assertTrue;
import uk.ac.ebi.intact.confidence.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        12-Dec-2007
 *        </pre>
 */
public class MockFactory {

    public static BinaryInteractionAttributes createBinaryIntWithAttribs(){
        Identifier idA = createDeterministicUniprotId(1);
        Identifier idB = createDeterministicUniprotId(2);
        List<Attribute> attribs = createDeterministicGoAttribs(3);
        BinaryInteractionAttributes bia = new BinaryInteractionAttributes(idA, idB, attribs, Confidence.UNKNOWN );
        return bia;
    }

    private static List<Attribute> createDeterministicGoAttribs( int nr ) {
        List<Attribute> attribs = new ArrayList<Attribute>();
        for (int i =0; i< nr; i++ ){
            Attribute<Identifier> attr = new IdentifierAttributeImpl<GoIdentifierImpl>(new GoIdentifierImpl( "GO:000005" + i), new GoIdentifierImpl( "GO:000005" +(i+1)));
            attribs.add( attr);
        }
        return attribs;
    }

    public static Identifier createDeterministicUniprotId(int i) {
      if (i == 1){
          return new UniprotIdentifierImpl("P12345");
      } else if (i == 2){
          return new UniprotIdentifierImpl("Q12345");
      } else {
          return null;
      }
    }
}
