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
package uk.ac.ebi.intact.confidence.model.parser;

import org.junit.Test;
import org.junit.Assert;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;

/**
 * Test class for BlastInputParserUtils.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        14-Jan-2008
 *        </pre>
 */
public class BlastInputParserUtilsTest {

    @Test
    public void parseUniprot() throws Exception {
        UniprotAc ac = BlastInputParserUtils.parseUniprotInfo( ">P12345|description" );
        Assert.assertEquals( "P12345", ac.getAcNr() );
    }
}
