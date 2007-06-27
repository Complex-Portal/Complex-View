/*
 * Created on 14.07.2004
 *  
 */
package uk.ac.ebi.intact.application.mine.business.graph.model.test;

import uk.ac.ebi.intact.application.mine.business.graph.model.EdgeObject;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Andreas Groscurth
 */
public class EdgeObjectTest extends TestCase {

    public EdgeObjectTest(String name) {
        super(name);
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX()
     * methods to the suite.
     */
    public static Test suite() {
        return new TestSuite(EdgeObjectTest.class);
    }

    public void test_normalObject() {
        EdgeObject eo = new EdgeObject("acc", 4);
        assertNotNull(eo);
        assertEquals("acc", eo.getInteractionAcc());
        assertEquals(4, eo.getWeight(), 0);
    }
    
    public void test_unweightObject() {
        EdgeObject eo = new EdgeObject("acc");
        assertNotNull(eo);
        assertEquals("acc", eo.getInteractionAcc());
        assertEquals(1, eo.getWeight(), 0);
    }
}