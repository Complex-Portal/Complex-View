/*
 * Created on 14.07.2004
 */

package uk.ac.ebi.intact.application.mine.business.graph.test;

import jdsl.core.api.InspectableDictionary;
import jdsl.graph.api.Edge;
import jdsl.graph.api.Vertex;
import jdsl.graph.ref.IncidenceListGraph;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.mine.business.graph.MineStorage;
import uk.ac.ebi.intact.application.mine.business.graph.Storage;

/**
 * @author Andreas Groscurth
 */
public class StorageTest extends TestCase {
    private Vertex v;
    private Edge e;

    public StorageTest(String name) {
        super( name );
        init();
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX()
     * methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( StorageTest.class );
    }

    public void test_normalObject() {
        Storage st = new MineStorage( 10 );
        assertNotNull( st );
        assertFalse( st.hasDistance( v ) );
        assertFalse( st.hasEdgeToParent( v ) );
        assertFalse( st.hasLocator( v ) );
    }

    public void test_changeStorage() {
        Storage st = new MineStorage( 10 );
        assertNotNull( st );
        st.setDistance( v, 2 );
        assertTrue( st.hasDistance( v ) );
        assertEquals( st.getDistance( v ), 2 );

        InspectableDictionary.InvalidLocator l = new InspectableDictionary.InvalidLocator();
        st.setLocator( v, l );
        assertTrue( st.hasLocator( v ) );
        assertEquals( st.getLocator( v ), l );

        st.setEdgeToParent( v, e );
        assertTrue( st.hasEdgeToParent( v ) );
        assertEquals( st.getEdgeToParent( v ), e );

        st.cleanup();
        assertFalse( st.hasDistance( v ) );
        assertFalse( st.hasEdgeToParent( v ) );
        assertFalse( st.hasLocator( v ) );
    }

    private void init() {
        IncidenceListGraph g = new IncidenceListGraph();
        Vertex v2 = null;
        for (int i = 0; i < 1; i++) {
            v = g.insertVertex( i + "" );
            v2 = g.insertVertex( ( i + 1 ) + "" );
        }
        e = g.insertEdge( v, v2, "4" );
    }
}