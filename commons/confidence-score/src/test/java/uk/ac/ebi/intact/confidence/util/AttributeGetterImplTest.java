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
package uk.ac.ebi.intact.confidence.util;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.attribute.Attribute;
import uk.ac.ebi.intact.confidence.model.GoId;
import uk.ac.ebi.intact.confidence.model.Id;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        29-Nov-2007
 *        </pre>
 */
public class AttributeGetterImplTest {
    @Test
    public void testCombine() throws Exception {
        AttributeGetterImpl ag = new AttributeGetterImpl();
        Set<Id> goA = new HashSet<Id>(3);
        goA.add( new GoId("GO:0000001"));
        goA.add( new GoId("GO:0000002"));
        goA.add( new GoId("GO:0000003"));
        Set<Id> goB = new HashSet<Id>(5);
        goB.add( new GoId("GO:0000001"));
        goB.add( new GoId("GO:0000002"));
        goB.add( new GoId("GO:0000004"));
        goB.add( new GoId("GO:0000005"));
        List<Attribute> attrs = ag.combine(goA, goB);
        Assert.assertEquals( 9, attrs.size());
    }

}
