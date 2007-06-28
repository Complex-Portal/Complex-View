/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.webapp.search.advancedSearch.cvIntegration.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.util.CvDagObjectUtils;

import java.util.Collection;
import java.util.Iterator;

/**
 * TODO comment that ...
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public class CvDagObjectToolTest extends TestCase {

    /**
     * Constructs a NewtServerProxyTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public CvDagObjectToolTest(final String name) {
        super(name);
    }

    Institution owner;
    CvDagObjectUtils dagUtils;
    /**
     * Returns this test suite. Reflection is used here to add all
     * the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite(CvDagObjectToolTest.class);
    }

    /**
     * Sets up the test fixture. Called before every test case method.
     */
    protected void setUp() throws IntactException {
        dagUtils = new CvDagObjectUtils();
        owner = new Institution("test");
    }


     protected void tearDown() throws IntactException {
     }
    /**
     * This method will test the creation of a linear model (Collection)
     * from a hierarchical (tree) model. It will validate the left and
     * right bound assignments.
     */
    public void testLinearModelCreationFromTree() {
        CvDagObject first = new CvInteraction(owner, "first");
        dagUtils.buildBounds(first, 1);
        assertEquals(1, first.getLeftBound());
        assertEquals(2, first.getRightBound());

        dagUtils.buildBounds(first, 99);
        assertEquals(99, first.getLeftBound());
        assertEquals(100, first.getRightBound());

        CvDagObject second = new  CvInteraction(owner, "second");
        first.addChild(second);
        dagUtils.buildBounds(first,1);
        assertEquals(1, first.getLeftBound());
        assertEquals(4, first.getRightBound());
        assertEquals(2, second.getLeftBound());
        assertEquals(3, second.getRightBound());

        CvDagObject third = new  CvInteraction(owner, "third");
        CvDagObject fourth = new  CvInteraction(owner, "fourth");
        CvDagObject fifth = new  CvInteraction(owner, "fifth");
        first.addChild(fifth);
        second.addChild(third);
        second.addChild(fourth);
        dagUtils.buildBounds(first, 1);
        assertEquals(1, first.getLeftBound());
        assertEquals(10, first.getRightBound());
        assertEquals(2, second.getLeftBound());
        assertEquals(7, second.getRightBound());
        assertEquals(3, third.getLeftBound());
        assertEquals(4, third.getRightBound());
        assertEquals(5, fourth.getLeftBound());
        assertEquals(6, fourth.getRightBound());
        assertEquals(8, fifth.getLeftBound());
        assertEquals(9, fifth.getRightBound());

        CvDagObject sixth = new CvInteraction(owner, "sixth");
        CvDagObject seventh = new CvInteraction(owner, "seventh");
        CvDagObject eighth = new CvInteraction(owner, "eighth");
        fourth.addChild(sixth);
        fourth.addChild(seventh);
        fifth.addChild(eighth);
        dagUtils.buildBounds(first, 1);
        assertEquals(1, first.getLeftBound());
        assertEquals(16, first.getRightBound());
        assertEquals(2, second.getLeftBound());
        assertEquals(11, second.getRightBound());
        assertEquals(3, third.getLeftBound());
        assertEquals(4, third.getRightBound());
        assertEquals(5, fourth.getLeftBound());
        assertEquals(10, fourth.getRightBound());
        assertEquals(12, fifth.getLeftBound());
        assertEquals(15, fifth.getRightBound());
        assertEquals(6, sixth.getLeftBound());
        assertEquals(7, sixth.getRightBound());
        assertEquals(8, seventh.getLeftBound());
        assertEquals(9, seventh.getRightBound());
        assertEquals(13, eighth.getLeftBound());
        assertEquals(14, eighth.getRightBound());

    }

    public void testInsert() throws IntactException {
        dagUtils.insertCVs(CvInteraction.class);
        CvObjectDao<CvInteraction> dao = getDaoFactory().getCvObjectDao(CvInteraction.class);
        CvDagObject aDagObject = dao.getByShortLabel("anti tag coimmunopre");
        CvDagObject aChild1 = dao.getByShortLabel("flag tag");
        CvDagObject aChild2 = dao.getByShortLabel("his tag");
        CvDagObject aChild3 = dao.getByShortLabel("myc tag");
        CvDagObject aChild4 = dao.getByShortLabel("ha tag");
        CvDagObject aChild5 = dao.getByShortLabel("tandem affinity puri");

        Collection allChildren = dagUtils.getCvWithChildren(aDagObject);
        for (Iterator iterator = allChildren.iterator(); iterator.hasNext();) {
            String s = (String) iterator.next();
            System.out.println("Found: " + s);

        }
        assertTrue(allChildren.contains(aChild1.getAc()));
        assertTrue(allChildren.contains(aChild2.getAc()));
        assertTrue(allChildren.contains(aChild3.getAc()));
        assertTrue(allChildren.contains(aChild4.getAc()));
        assertTrue(allChildren.contains(aChild5.getAc()));
        assertEquals(5, allChildren.size());

        dagUtils.insertCVs(CvIdentification.class);
        CvObjectDao<CvIdentification> dao2 = getDaoFactory().getCvObjectDao(CvIdentification.class);
        aDagObject = dao2.getByShortLabel("nucleotide sequence");
        aChild1 = dao2.getByShortLabel("partial dna sequence");
        aChild2 = dao2.getByShortLabel("full identification");
        aChild3 = dao2.getByShortLabel("southern blot");
        aChild4 = dao2.getByShortLabel("primer specific pcr");

        allChildren = dagUtils.getCvWithChildren(aDagObject);
        assertTrue(allChildren.contains(aChild1.getAc()));
        assertTrue(allChildren.contains(aChild2.getAc()));
        assertTrue(allChildren.contains(aChild3.getAc()));
        assertTrue(allChildren.contains(aChild4.getAc()));
        assertEquals(4, allChildren.size());

        dagUtils.insertCVs(CvInteractionType.class);
        CvObjectDao<CvInteractionType> dao3 = getDaoFactory().getCvObjectDao(CvInteractionType.class);
        aDagObject = dao3.getByShortLabel("lipid cleavage");
        aChild1 = dao3.getByShortLabel("degeranylation");
        aChild2 = dao3.getByShortLabel("defarnesylation reac");
        aChild3 = dao3.getByShortLabel("demyristoylation");
        aChild4 = dao3.getByShortLabel("depalmitoylation");

        allChildren = dagUtils.getCvWithChildren(aDagObject);
        assertTrue(allChildren.contains(aChild1.getAc()));
        assertTrue(allChildren.contains(aChild2.getAc()));
        assertTrue(allChildren.contains(aChild3.getAc()));
        assertTrue(allChildren.contains(aChild4.getAc()));
        assertEquals(4, allChildren.size());


    }

    private DaoFactory getDaoFactory()
    {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }
}
