/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.struts.business.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.struts.action.DynaActionForm;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.struts.business.QueryBuilder;

/**
 * TODO comment that ...
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public class QueryBuilderTest extends TestCase {


    /**
     * Constructs a FirstTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public QueryBuilderTest(String name) {
        super(name);
    }

    /**
     * Sets up the test fixture. Called before every test case method.
     */
    protected void setUp() {
        // Write setting up code for each test.
    }

    public void testGetSqlLikeStatement() throws IntactException {

        DynaActionForm dyForm = new DynaActionForm();
        dyForm.set("searchObject", "protein");
        dyForm.set("acNumber", "EBI-90");
        dyForm.set("shortlabel", "yeast");
        dyForm.set("description", "blabla");
        dyForm.set("connection", "and");

        QueryBuilder qb = new QueryBuilder(dyForm);

        String statement1 = "SELECT protein FROM intact WHERE (ac = 'EBI-90' and fullname = 'blabla' and shortlabel = 'yeast');";

        assertEquals(statement1, qb.getSqlLikeStatement());

    }

    /**
     * Returns this test suite. Reflection is used here to add all
     * the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite(QueryBuilderTest.class);
    }

}



