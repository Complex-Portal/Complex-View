/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.confidence.model.io;

import org.junit.Test;
import org.junit.Assert;
import uk.ac.ebi.intact.confidence.model.*;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionAttributesReaderImpl;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.io.File;

/**
 * Test class for BinaryInteractionAttributesReader.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        11-Jan-2008
 *        </pre>
 */
public class BinaryInteractionAttributesReaderTest {

    @Test
    public void read() throws Exception {
        BinaryInteractionAttributesReader biar = new BinaryInteractionAttributesReaderImpl();
        File inFile = new File(BinaryInteractionAttributesReaderTest.class.getResource("BinaryInteractionAttributes.txt").getPath());
        List<BinaryInteractionAttributes> observed = biar.read(inFile);
        List<BinaryInteractionAttributes> expected = expectedInfo();
        Assert.assertEquals( expected.size(), observed.size() );

        // checks if same info, but ignoring the confidence type
        for ( int i=0; i< observed.size(); i++ ) {
            BinaryInteractionAttributes observedAttr = observed.get( i );
            BinaryInteractionAttributes expectedAttr =  expected.get( i );
            Assert.assertTrue(assertSameInfo( expectedAttr, observedAttr ));
        }
    }

    @Test
    public void iterate() throws Exception {
        BinaryInteractionAttributesReader biar = new BinaryInteractionAttributesReaderImpl();
        File inFile = new File(BinaryInteractionAttributesReaderTest.class.getResource("BinaryInteractionAttributes.txt").getPath());
        List<BinaryInteractionAttributes> expected = expectedInfo();

        // checks if same info, but ignoring the confidence type
        int nr =0;
        for ( Iterator<BinaryInteractionAttributes> iter = biar.iterate( inFile ); iter.hasNext(); ) {
            BinaryInteractionAttributes observedAttr =  iter.next();
            BinaryInteractionAttributes expectedAttr =  expected.get( nr );
            Assert.assertTrue(assertSameInfo( expectedAttr, observedAttr ));
            nr ++;

        }
        Assert.assertEquals( expected.size(), nr );
    }

    protected boolean assertSameInfo( BinaryInteractionAttributes expectedAttr, BinaryInteractionAttributes observedAttr ) {
        if (!sameIds(expectedAttr, observedAttr)){
            return false;
        }
        Assert.assertEquals( expectedAttr.getAttributes().size(), observedAttr.getAttributes().size());

        for (int i =0; i < expectedAttr.getAttributes().size(); i++ ){
            Attribute expAttr = expectedAttr.getAttributes().get( i );
            Attribute obsAttr = observedAttr.getAttributes().get( i );
            Assert.assertEquals(expAttr.getFirstElement(), obsAttr.getFirstElement());
            Assert.assertEquals(expAttr.getSecondElement(), obsAttr.getSecondElement());
        }
        return true;
    }

    private boolean sameIds( BinaryInteractionAttributes expectedAttr, BinaryInteractionAttributes observedAttr ) {
        boolean straight = false;
        if (expectedAttr.getFirstId().getId().equalsIgnoreCase( observedAttr.getFirstId().getId()) &&
                expectedAttr.getSecondId().getId().equalsIgnoreCase( observedAttr.getSecondId().getId())){
            straight = true;
        }

        boolean reverse = false;
        if (expectedAttr.getSecondId().getId().equalsIgnoreCase( observedAttr.getFirstId().getId()) &&
                expectedAttr.getSecondId().getId().equalsIgnoreCase( observedAttr.getSecondId().getId())){
            reverse = true;
        }
        return straight || reverse;
    }

    private List<BinaryInteractionAttributes> expectedInfo (){
        Identifier id1 = new UniprotIdentifierImpl("Q9VK47");
        Identifier id2 = new UniprotIdentifierImpl("Q9W528");
        BinaryInteractionAttributes bia = new BinaryInteractionAttributes(id1, id2, Confidence.UNKNOWN);
        Attribute<Identifier> attr = new IdentifierAttributeImpl<UniprotIdentifierImpl>(( UniprotIdentifierImpl)id1,(UniprotIdentifierImpl) id2);
        bia.addAttribute( attr );

        Identifier id11 = new UniprotIdentifierImpl("P31946");
        Identifier id21 = new UniprotIdentifierImpl("P33176");
        BinaryInteractionAttributes bia1 = new BinaryInteractionAttributes(id11, id21, Confidence.UNKNOWN);
        Attribute<Identifier> attr1 =  new IdentifierAttributeImpl<GoIdentifierImpl>(new GoIdentifierImpl( "GO:0005517"), new GoIdentifierImpl("GO:0008017"));
        bia1.addAttribute( attr1 );

        Identifier id12 = new UniprotIdentifierImpl("Q8SX86");
        Identifier id22 = new UniprotIdentifierImpl("Q9W1Q0");
        BinaryInteractionAttributes bia2 = new BinaryInteractionAttributes(id12, id22, Confidence.UNKNOWN);
        Attribute<Identifier> attr11 =  new IdentifierAttributeImpl<InterProIdentifierImpl>(new InterProIdentifierImpl( "IPR001232"), new InterProIdentifierImpl("IPR001810"));
        Attribute<Identifier> attr21 =  new IdentifierAttributeImpl<UniprotIdentifierImpl>(new UniprotIdentifierImpl( "O77430"), new UniprotIdentifierImpl("Q8SX86"));
        Attribute<Identifier> attr31 =  new IdentifierAttributeImpl<UniprotIdentifierImpl>(new UniprotIdentifierImpl( "Q8SX86"), new UniprotIdentifierImpl("Q9VWC4"));
        Attribute<Identifier> attr41 =  new IdentifierAttributeImpl<InterProIdentifierImpl>(new InterProIdentifierImpl( "IPR001810"), new InterProIdentifierImpl("IPR011333"));
        bia2.setAttributes( Arrays.asList((Attribute) attr11, attr21, attr31, attr41 ));


        Identifier id13 = new UniprotIdentifierImpl("P33176");
        Identifier id23 = new UniprotIdentifierImpl("P63104");
        BinaryInteractionAttributes bia3 = new BinaryInteractionAttributes(id13, id23, Confidence.UNKNOWN);
        Attribute<Identifier> attr12 = new IdentifierAttributeImpl<GoIdentifierImpl>( new  GoIdentifierImpl("GO:0005517"), new GoIdentifierImpl("GO:0048471" ));
        Attribute<Identifier> attr22 = new IdentifierAttributeImpl<UniprotIdentifierImpl>(( UniprotIdentifierImpl)id13, (UniprotIdentifierImpl) id23);
        Attribute<Identifier> attr32 = new IdentifierAttributeImpl<GoIdentifierImpl>( new  GoIdentifierImpl("GO:0008017"), new GoIdentifierImpl("GO:0048471" ));
        Attribute<Identifier> attr42 = new IdentifierAttributeImpl<GoIdentifierImpl>( new  GoIdentifierImpl("GO:0003777"), new GoIdentifierImpl("GO:0048471" ));
        Attribute<Identifier> attr52 = new IdentifierAttributeImpl<UniprotIdentifierImpl>( new UniprotIdentifierImpl("O60282"),  new UniprotIdentifierImpl("P29310-2"));
        bia3.setAttributes( Arrays.asList( (Attribute) attr12, attr22, attr32, attr42, attr52 ));
        return Arrays.asList( bia, bia1, bia2, bia3 );
    }
}
