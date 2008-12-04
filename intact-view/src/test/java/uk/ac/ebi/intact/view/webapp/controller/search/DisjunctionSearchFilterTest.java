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
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.controller.search;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DisjunctionSearchFilterTest {

    @Test
    public void testToLuceneSyntax() {
        SearchFilter filter1 = new SimpleFilter("filter1");
        SearchFilter filter2 = new SimpleFilter("filter2");

        SearchFilter disjFilter = new DisjunctionSearchFilter(Arrays.asList(filter1, filter2));

        Assert.assertEquals("(filter1 filter2)", disjFilter.toLuceneSyntax());
    }
    
    @Test
    public void testToLuceneSyntax2() {
        SearchFilter filter1 = new SimpleFilter("filter1");

        SearchFilter disjFilter = new DisjunctionSearchFilter(Arrays.asList(filter1));

        Assert.assertEquals("filter1", disjFilter.toLuceneSyntax());
    }

    @Test
    public void testToLuceneSyntax_field() {
        SearchFilter filter1 = new SimpleFilter("filter1");
        SearchFilter filter2 = new SimpleFilter("filter2");

        SearchFilter disjFilter = new DisjunctionSearchFilter(Arrays.asList(filter1, filter2), "source");

        Assert.assertEquals("(source:filter1 source:filter2)", disjFilter.toLuceneSyntax());
    }

    @Test
    public void testToDisplay() {
        SearchFilter filter1 = new SimpleFilter("filter1");
        SearchFilter filter2 = new SimpleFilter("filter2");

        SearchFilter disjFilter = new DisjunctionSearchFilter(Arrays.asList(filter1, filter2));

        Assert.assertEquals("(filter1 OR filter2)", disjFilter.toDisplay());
    }
}
