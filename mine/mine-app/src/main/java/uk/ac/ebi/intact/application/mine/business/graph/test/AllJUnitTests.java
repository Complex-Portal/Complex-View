/*
 * Created on 14.07.2004
 */

package uk.ac.ebi.intact.application.mine.business.graph.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Testsuite that is composed of the individual JUnit test suites. Any new test
 * suite should be added here.
 * 
 * @author Sugath Mudali
 * @version $Id$
 */
public class AllJUnitTests extends TestCase {

    /**
     * The constructor with the test name.
     * 
     * @param name the name of the test.
     */
    public AllJUnitTests(final String name) {
        super( name );
    }

    /**
     * Returns a suite containing tests.
     * 
     * @return a suite containing tests. <p/>
     * 
     * <pre>
     * 
     *                                                                                          post: return != null
     *                                                                                          post: return-&gt;forall(obj : Object | obj.oclIsTypeOf(TestSuite))
     *                                                                                          
     * </pre>
     */
    public static Test suite() {
        final TestSuite suite = new TestSuite();
        suite.addTest( StorageTest.suite() );
        suite.addTest( DijkstraTest.suite() );
        return suite;
    }
}