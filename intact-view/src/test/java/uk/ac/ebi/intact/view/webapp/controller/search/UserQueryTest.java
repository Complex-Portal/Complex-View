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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class UserQueryTest {

    private UserQuery query;

    @Before
    public void setUp() {
        query = new UserQuery();
        query.reset();
    }

    @After
    public void tearDown() {
        query = null;
    }

    @Test
    public void getInteractorQuery() {
        query.setSearchQuery("test");
        query.addProperty("GO:12345");
        query.setInteractorTypeMi("MI:0123");
        
        Assert.assertEquals("+test +propertiesA:\"GO:12345\" +typeA:\"MI:0123\"", query.getInteractorQuery());
    }

    @Test
    public void getInteractionQuery() {
        query.setSearchQuery("test");
        query.addProperty("GO:12345");
        query.setInteractorTypeMi("MI:0123");

        Assert.assertEquals("+test +properties:\"GO:12345\"", query.getInteractionQuery());
    }

    @Test
    public void getInteractionQuery2() {
        query.setSearchQuery("test");
        query.addProperty("GO:12345");
        query.setDatasets(new String[]{"dataset1"});
        query.setSources(new String[]{"sourceA"});

        Assert.assertEquals("+test +properties:\"GO:12345\" +dataset:dataset1 +source:sourceA", query.getInteractionQuery());
    }

    @Test
    public void getDisplayQuery() {
        query.setSearchQuery("test");
        query.addProperty("GO:12345");
        
        Assert.assertEquals("test AND \"GO:12345\"", query.getDisplayQuery());
    }

    @Test
    public void getDisplayQuery2() {
        query.setSearchQuery("test");
        query.setInteractorTypeMi("MI:0123");
        query.addProperty("GO:12345");

        Assert.assertEquals("test AND \"GO:12345\"", query.getDisplayQuery());
    }

    @Test
    public void removeClearableFilters() {
        query.setSearchQuery("test");
        query.addProperty("GO:12345");

        query.removeClearableFilters();
        
        Assert.assertEquals("test", query.getDisplayQuery());
    }

    @Test
    public void removeClearableFilters2() {
        query.setSearchQuery("test");
        query.addProperty("GO:12345");
        query.setDatasets(new String[]{"dataset1"});
        query.setSources(new String[]{"sourceA"});
        
        query.removeClearableFilters();

        Assert.assertEquals("+test", query.getInteractionQuery());
    }
    
    @Test
    public void reset() {
        query.setSearchQuery("test");
        query.addProperty("GO:12345");

        query.reset();

        Assert.assertEquals("*", query.getDisplayQuery());
    }

    @Test
    public void overridingSearches() {
        query.setSearchQuery("test");

        Assert.assertEquals("test", query.getDisplayQuery());

        query.setSearchQuery("test2");

        Assert.assertEquals("test2", query.getDisplayQuery());
    }

    @Test
    public void overridingOntologySearches() {
        query.setOntologySearchQuery("test");

        Assert.assertEquals("test", query.getDisplayQuery());

        query.setOntologySearchQuery("test2");

        Assert.assertEquals("test2", query.getDisplayQuery());
    }

    @Test
    public void changeSearches() {
        query.setSearchQuery("test");

        Assert.assertEquals("test", query.getDisplayQuery());

        query.setOntologySearchQuery("test2");

        Assert.assertEquals("test2", query.getDisplayQuery());
    }

    @Test
    public void overridingSearches2() {
        query.setInteractorTypeMi("mi1");

        Assert.assertEquals("+typeA:mi1", query.getInteractorQuery());

        query.setInteractorTypeMi("mi2");

        Assert.assertEquals("+typeA:mi2", query.getInteractorQuery());
    }

    @Test
    public void expansionSpoke() {
        query.setSearchQuery("test");
        query.setExpansions(new String[] {"lala"});

        Assert.assertEquals("+test -expansion:Spoke", query.getInteractionQuery());
    }

    @Test
    public void expansionSpoke_not() {
        query.setSearchQuery("test");
        query.setExpansions(new String[] {"lala", FilterPopulatorController.EXPANSION_SPOKE_VALUE});

        Assert.assertEquals("+test", query.getInteractionQuery());
    }
}
