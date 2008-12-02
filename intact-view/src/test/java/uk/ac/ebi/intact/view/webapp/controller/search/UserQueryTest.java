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
    }

    @After
    public void tearDown() {
        query = null;
    }

    @Test
    public void testCreateInteractorQuery() {
        query.setSearchQuery("test");
        query.getProperties().add("GO:12345");
        query.setInteractorTypeMi("MI:0123");
        
        Assert.assertEquals("+test +typeA:\"MI:0123\" +propertiesA:\"GO:12345\"", query.createInteractorQuery());
    }

    @Test
    public void testCreateInteractionQuery() {
        query.setSearchQuery("test");
        query.getProperties().add("GO:12345");
        query.setInteractorTypeMi("MI:0123");

        Assert.assertEquals("+test +properties:\"GO:12345\"", query.createInteractionQuery());
    }

    @Test
    public void testGetDisplayQuery() {
        query.setSearchQuery("test");
        query.getProperties().add("GO:12345");
        
        Assert.assertEquals("test AND \"GO:12345\"", query.getDisplayQuery());
    }
}
