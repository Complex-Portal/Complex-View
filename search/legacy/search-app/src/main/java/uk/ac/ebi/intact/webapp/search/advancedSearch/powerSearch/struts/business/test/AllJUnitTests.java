/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.struts.business.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * TODO comment that ...
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public class AllJUnitTests extends TestCase {

    /**
     * The constructor with the test name.
     *
     * @param name the name of the test.
     */
    public AllJUnitTests(final String name) {
        super(name);
    }

    /**
     * Returns a suite containing tests.
     *
     * @return a suite containing tests.
     *         <p/>
     *         <pre>
     *                            post: return != null
     *                            post: return->forall(obj : Object | obj.oclIsTypeOf(TestSuite))
     *                         </pre>
     */
    public static Test suite() {
        final TestSuite suite = new TestSuite();

        suite.addTest(QueryBuilderTest.suite());
        return suite;
    }

}
