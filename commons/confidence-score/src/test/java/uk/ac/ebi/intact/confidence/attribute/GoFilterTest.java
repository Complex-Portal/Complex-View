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

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.model.GoIdentifierImpl;
import uk.ac.ebi.intact.confidence.model.Identifier;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *  Test class for GO filter.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        29-Nov-2007
 *        </pre>
 */
public class GoFilterTest {
    @Test
    public void testFilter() throws Exception {
        Set<Identifier> gos = new HashSet<Identifier>(3);
        gos.add(new GoIdentifierImpl("GO:0005515"));
        gos.add(new GoIdentifierImpl("GO:0000001"));
        gos.add(new GoIdentifierImpl("GO:0008022"));
        GoFilter.filterForbiddenGos( gos);
        Assert.assertEquals( 1, gos.size());
    }

}
