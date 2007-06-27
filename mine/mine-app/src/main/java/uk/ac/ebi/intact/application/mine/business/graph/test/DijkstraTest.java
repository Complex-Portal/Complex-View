/*
 * Created on 14.07.2004
 */

package uk.ac.ebi.intact.application.mine.business.graph.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jdsl.core.api.ObjectIterator;
import jdsl.core.api.Sequence;
import jdsl.graph.api.Edge;
import jdsl.graph.api.Graph;
import jdsl.graph.api.Vertex;
import jdsl.graph.ref.IncidenceListGraph;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.mine.business.graph.Dijkstra;
import uk.ac.ebi.intact.application.mine.business.graph.MineStorage;
import uk.ac.ebi.intact.application.mine.business.graph.Storage;
import uk.ac.ebi.intact.application.mine.business.graph.model.EdgeObject;
import uk.ac.ebi.intact.application.mine.business.graph.model.GraphData;
import uk.ac.ebi.intact.application.mine.business.graph.model.SearchObject;

/**
 * @author Andreas Groscurth
 */
public class DijkstraTest extends TestCase {
    private GraphData md = getMineTestData();

    public DijkstraTest(String name) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX()
     * methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( DijkstraTest.class );
    }

    private Collection getMine(Graph graph, Map search) {
        Storage storage = new MineStorage( graph.numVertices() );
        Dijkstra d = new Dijkstra( storage, search );
        Vertex[] nodes = (Vertex[]) search.keySet().toArray( new Vertex[0] );
        SearchObject[] se = (SearchObject[]) search.values().toArray(
                new SearchObject[0] );

        for (int i = 0; i < nodes.length; i++) {
            for (int j = i + 1; j < nodes.length; j++) {
                if ( !se[i].hasPathAlreadyFound( se[j] ) ) {
                    d.execute( graph, nodes[i], nodes[j] );
                    storage.cleanup();
                }
            }
        }
        List mine = new ArrayList();
        Object n1, n2;
        for (int i = 0; i < se.length; i++) {
            Sequence seq = se[i].getPath();

            if ( seq == null ) {
                continue;
            }
            for (ObjectIterator iter = seq.elements(); iter.hasNext();) {
                Edge element = (Edge) iter.nextObject();
                Vertex[] v = graph.endVertices( element );
                n1 = v[0].element();
                n2 = v[1].element();

                if ( !mine.contains( n1 ) ) {
                    mine.add( n1 );
                }
                if ( !mine.contains( n2 ) ) {
                    mine.add( n2 );
                }
            }
        }
        return mine;

    }

    public void test_dijkstraSimpleRun() {
        assertNotNull( md );
        assertNotNull( md.getAccMap() );
        assertNotNull( md.getGraph() );
        Collection node = new ArrayList();
        node.add( "acc 0" );
        node.add( "acc 7" );
        Map search = getSearchNodes( md.getAccMap(), node );
        Collection path = getMine( md.getGraph(), search );
        assertEquals( path.size() == 0, false );
        assertEquals( path.size() == 2, true );
        assertEquals( path.contains( "acc 7" ), true );
        assertEquals( path.contains( "acc 3" ), false );
    }

    public void test_dijkstraNoPath() {
        assertNotNull( md );
        assertNotNull( md.getAccMap() );
        assertNotNull( md.getGraph() );
        Collection node = new ArrayList();
        node.add( "acc 0" );
        node.add( "acc 18" );
        Map map = getSearchNodes( md.getAccMap(), node );
        Collection path = getMine( md.getGraph(), map );
        assertEquals( path.size() == 0, true );
    }

    public void test_dijkstraLongPath() {
        assertNotNull( md );
        assertNotNull( md.getAccMap() );
        assertNotNull( md.getGraph() );
        Collection node = new ArrayList();
        node.add( "acc 0" );
        node.add( "acc 3" );
        node.add( "acc 15" );
        Map map = getSearchNodes( md.getAccMap(), node );
        Collection path = getMine( md.getGraph(), map );
        assertEquals( path.size() == 0, false );
        assertEquals( path.size() == 4, true );
        assertEquals( path.contains( "acc 7" ), true );
        assertEquals( path.contains( "acc 4" ), false );
    }

    private Map getSearchNodes(Map accMap, Collection nodes) {
        Map map = new Hashtable();
        Object key;
        Vertex vertex;
        int i = 0;
        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            key = (Object) iter.next();
            vertex = (Vertex) accMap.get( key );
            if ( vertex == null ) {
                throw new NullPointerException( "No node found " + "for " + key );
            }
            map.put( vertex, new SearchObject( i++, nodes.size() ) );
        }
        return map;
    }

    public void test_normalObject() {
        Storage st = new MineStorage( 100 );
        Map map = new Hashtable();
        Dijkstra d = new Dijkstra( st, map );
        assertNotNull( d );
    }

    private GraphData getMineTestData() {
        Map map = new Hashtable();
        List acc = new ArrayList();
        List interAcc = new ArrayList();
        for (int i = 0; i < 21; i++) {
            acc.add( "acc " + i );
        }
        for (int i = 0; i < 24; i++) {
            interAcc.add( "intAcc " + i );
        }
        Graph g = new IncidenceListGraph();
        Vertex[] v = new Vertex[acc.size()];
        String accS;
        for (int i = 0; i < acc.size(); i++) {
            accS = acc.get( i ).toString();
            v[i] = g.insertVertex( accS );
            map.put( accS, v[i] );
        }
        EdgeObject ed = new EdgeObject( "0-7", 1 );
        g.insertEdge( v[0], v[7], ed );
        ed = new EdgeObject( "3-7", 1 );
        g.insertEdge( v[3], v[7], ed );
        ed = new EdgeObject( "15-7", 1 );
        g.insertEdge( v[15], v[7], ed );
        ed = new EdgeObject( "3-4", 1 );
        g.insertEdge( v[3], v[14], ed );
        ed = new EdgeObject( "1-3", 1 );
        g.insertEdge( v[1], v[3], ed );
        ed = new EdgeObject( "1-8", 1 );
        g.insertEdge( v[1], v[8], ed );
        ed = new EdgeObject( "1-2", 1 );
        g.insertEdge( v[1], v[2], ed );
        ed = new EdgeObject( "2-13", 1 );
        g.insertEdge( v[2], v[13], ed );
        ed = new EdgeObject( "8-2", 1 );
        g.insertEdge( v[8], v[2], ed );
        ed = new EdgeObject( "8-9", 1 );
        g.insertEdge( v[8], v[9], ed );
        ed = new EdgeObject( "2-4", 1 );
        g.insertEdge( v[2], v[4], ed );
        ed = new EdgeObject( "2-6", 1 );
        g.insertEdge( v[2], v[6], ed );
        ed = new EdgeObject( "9-4", 1 );
        g.insertEdge( v[9], v[4], ed );
        ed = new EdgeObject( "4-10", 1 );
        g.insertEdge( v[4], v[10], ed );
        ed = new EdgeObject( "4-20", 1 );
        g.insertEdge( v[4], v[20], ed );
        ed = new EdgeObject( "10-16", 1 );
        g.insertEdge( v[10], v[16], ed );
        ed = new EdgeObject( "10-17", 1 );
        g.insertEdge( v[10], v[17], ed );
        ed = new EdgeObject( "19-17", 1 );
        g.insertEdge( v[19], v[17], ed );
        ed = new EdgeObject( "6-12", 1 );
        g.insertEdge( v[6], v[12], ed );
        ed = new EdgeObject( "16-15", 1 );
        g.insertEdge( v[16], v[15], ed );
        ed = new EdgeObject( "6-11", 1 );
        g.insertEdge( v[6], v[11], ed );
        ed = new EdgeObject( "12-11", 1 );
        g.insertEdge( v[12], v[11], ed );
        ed = new EdgeObject( "5-11", 1 );
        g.insertEdge( v[5], v[11], ed );
        ed = new EdgeObject( "5-18", 1 );
        g.insertEdge( v[5], v[18], ed );
        return new GraphData( g, map );
    }
}