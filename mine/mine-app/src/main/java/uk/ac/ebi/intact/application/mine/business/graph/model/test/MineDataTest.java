/*
 * Created on 14.07.2004
 *
 */
package uk.ac.ebi.intact.application.mine.business.graph.model.test;

import java.util.Hashtable;
import java.util.Map;

import jdsl.graph.api.Graph;
import jdsl.graph.ref.IncidenceListGraph;
import uk.ac.ebi.intact.application.mine.business.graph.model.GraphData;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Andreas Groscurth
 */
//TODO: RENAME IT !!!
public class MineDataTest extends TestCase {

    public MineDataTest(String name) {
        super(name);
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX()
     * methods to the suite.
     */
    public static Test suite() {
        return new TestSuite(MineDataTest.class);
    }

    public void test_normalObject() {
        Graph g = new IncidenceListGraph();
        Map m = new Hashtable();
        GraphData md = new GraphData(g, m);
        assertNotNull(md);
        assertEquals(g, md.getGraph());
        assertEquals(m, md.getAccMap());
    }

    public void test_nullGraph() {
        try {
            new GraphData(null, new Hashtable());
            fail("graph should actually not be null !");
        }
        catch (Exception e) {
            //ok
        }
    }

    public void test_nullMap() {
        try {
            new GraphData(new IncidenceListGraph(), null);
            fail("map should actually not be null !");
        }
        catch (Exception e) {
            //ok
        }
    }
}