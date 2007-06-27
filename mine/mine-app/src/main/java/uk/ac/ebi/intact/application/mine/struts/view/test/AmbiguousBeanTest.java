/*
 * Created on 19.07.2004
 */

package uk.ac.ebi.intact.application.mine.struts.view.test;

import java.util.ArrayList;
import java.util.Collection;

import uk.ac.ebi.intact.application.mine.struts.view.AmbiguousBean;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Andreas Groscurth
 */
public class AmbiguousBeanTest extends TestCase {
    public AmbiguousBeanTest(String name) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX()
     * methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( AmbiguousBeanTest.class );
    }

    public void test_normalObject() {
        AmbiguousBean ab = new AmbiguousBean();
        assertNotNull( ab );
        assertNull( ab.getExperiments() );
        assertNull( ab.getProteins() );
        assertNull( ab.getInteractions() );
        assertNull( ab.getSearchAc() );
    }

    public void test_searchAc() {
        AmbiguousBean ab = new AmbiguousBean();
        assertNotNull( ab );
        ab.setSearchAc( "test" );
        assertNotNull( ab.getSearchAc() );
        assertEquals( "test", ab.getSearchAc() );
    }

    public void test_ambiguousResult() {
        AmbiguousBean ab = new AmbiguousBean();
        assertNotNull( ab );
        Collection col = new ArrayList();
        col.add( "1" );
        ab.setProteins( col );
        col = new ArrayList();
        col.add("2");
        ab.setExperiments( col);
        ab.setInteractions( new ArrayList() );
        assertNotNull( ab.getProteins() );
        assertNotNull(ab.getExperiments());
        assertTrue( ab.hasAmbiguousResult() );
    }

    public void test_nonAmbiguousResult() {
        AmbiguousBean ab = new AmbiguousBean();
        assertNotNull( ab );
        Collection col = new ArrayList();
        col.add( "1" );
        ab.setProteins( col );
        ab.setExperiments( new ArrayList() );
        ab.setInteractions( new ArrayList() );
        assertNotNull( ab.getProteins() );
        assertFalse( ab.hasAmbiguousResult() );
    }
}