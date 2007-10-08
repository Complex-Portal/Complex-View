/**
 * Copyright 2006 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.search.ws;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>05-Oct-2006</pre>
 */
public class SearchTest extends IntactBasicTestCase
{
    private Search search;

    public SearchTest()
    {
    }

    @Before
    protected void setUp() throws Exception
    {
        search = new Search();
    }

    @After
    protected void tearDown() throws Exception
    {
        search = null;
    }

    public void testFindPartnersUsingUniprotIds()
    {
        PartnerResult[] results = search.findPartnersUsingUniprotIds(new String[] {"P29452"});

        Assert.assertEquals(1, results.length);
        Assert.assertEquals(1, results[0].getPartnerUniprotAcs().length);
        Assert.assertEquals("Q56134", results[0].getPartnerUniprotAcs()[0]);
    }

    public void testCountExperimentsUsingIntactQuery()
    {
        Assert.assertEquals(2, search.countExperimentsUsingIntactQuery("*"));
    }

    public void testCountInteractionsUsingIntactQuery()
    {
        Assert.assertEquals(2, search.countInteractionsUsingIntactQuery("*"));
    }

    public void testCountProteinsUsingIntactQuery()
    {
        Assert.assertEquals(2, search.countProteinsUsingIntactQuery("*"));
    }

    public void testCountAllBinaryInteractions()
    {
        Assert.assertEquals(2, search.countAllBinaryInteractions());
    }

    public void testSearchExperimentsUsingIntactQuery()
    {
        Assert.assertEquals(2, search.searchExperimentsUsingQuery("*", null, null).size());
    }

    public void testSearchProteinsUsingIntactQuery()
    {
        for (SimpleResult result : search.searchProteinsUsingQuery("*", null, null))
        {
            System.out.println(result.getAc());
        }
    }
}
