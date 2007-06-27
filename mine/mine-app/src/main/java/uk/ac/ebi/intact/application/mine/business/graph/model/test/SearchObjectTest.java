/*
 * Created on 14.07.2004
 *
 */
package uk.ac.ebi.intact.application.mine.business.graph.model.test;

import jdsl.core.ref.NodeSequence;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.mine.business.graph.model.SearchObject;

/**
 * @author Andreas Groscurth
 */
public class SearchObjectTest extends TestCase {
    private static int SIZE = 8;

    public SearchObjectTest(String name) {
        super(name);
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX()
     * methods to the suite.
     */
    public static Test suite() {
        return new TestSuite(SearchObjectTest.class);
    }

    public void test_objectWithPath() {
        SearchObject so = new SearchObject(1, SIZE);
        assertNotNull(so);
        so.setPath(new NodeSequence());
        assertNotNull(so.getPath());
    }

    public void test_bitOperations() {
        SearchObject so1 = new SearchObject(0, SIZE);
        SearchObject so2 = new SearchObject(1, SIZE);
        SearchObject so3 = new SearchObject(2, SIZE);
        assertFalse(so1.hasPathAlreadyFound(so2));
        so1.pathWasFound(so2);
        assertTrue(so1.hasPathAlreadyFound(so2));
        so2.pathWasFound(so3);
        assertFalse(so1.hasPathAlreadyFound(so3));
        assertTrue(so2.hasPathAlreadyFound(so3));
    }

    public void test_normalObject() {
        SearchObject so = new SearchObject(1, SIZE);
        assertNotNull(so);
        assertNull(so.getPath());
        assertEquals(Integer.MAX_VALUE, so.getPathLength());
    }

    public void test_negativeObject() {
        try {
            new SearchObject(-1, SIZE);
            fail("no negative index should be allowed !");
        }
        catch (Exception e) {
            // ok
        }
    }

    public void test_outOfBounds() {
        try {
            new SearchObject(10, SIZE);
            fail("the index should not be greater than the allowed size");
        }
        catch (Exception e) {
            //ok
        }
    }
}