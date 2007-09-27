/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.business.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.editor.business.EditUser;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.Interaction;

/**
 * The test case for EditUser class. Only tests very few methods!!!
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class EditUserTest extends TestCase {

    /**
     * Constructs an instance of this class with the specified name.
     * @param name the name of the test.
     */
    public EditUserTest(String name) {
        super(name);
    }

    /**
     * Sets up the test fixture. Called before every test case method.
     */
    protected void setUp() {
        // Write setting up code for each test.
    }

    /**
     * Tears down the test fixture. Called after every test case method.
     */
    protected void tearDown() {
        // Release resources for after running a test.
    }

    /**
     * Returns this test suite. Reflection is used here to add all
     * the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite(EditUserTest.class);
    }

    public void testShortLabelFormatter() {
        // Holds reference to the formatter.
        EditUser.ShortLabelFormatter formatter;

        formatter = new EditUser.ShortLabelFormatter("abc");
        assertEquals(formatter.getRoot(), "abc");
        // No branch or cloned
        assertFalse(formatter.hasBranch());
        assertFalse(formatter.hasClonedSuffix());
        // Only root entry.
        assertTrue(formatter.isRootOnly());

        formatter = new EditUser.ShortLabelFormatter("abc-xyz");
        assertEquals(formatter.getRoot(), "abc-xyz");
        // No branch or cloned
        assertFalse(formatter.hasBranch());
        assertFalse(formatter.hasClonedSuffix());
        // Only root entry.
        assertTrue(formatter.isRootOnly());

        formatter = new EditUser.ShortLabelFormatter("ho-1");
        assertEquals(formatter.getRoot(), "ho");
        assertTrue(formatter.hasBranch());
        assertEquals(formatter.getBranch(), "1");
        // No cloned name.
        assertFalse(formatter.hasClonedSuffix());
        // No a root entry.
        assertFalse(formatter.isRootOnly());

        formatter = new EditUser.ShortLabelFormatter("ho-1-2");
        assertEquals(formatter.getRoot(), "ho-1");
        assertTrue(formatter.hasBranch());
        assertEquals(formatter.getBranch(), "2");
        // No cloned name.
        assertFalse(formatter.hasClonedSuffix());
        // No a root entry.
        assertFalse(formatter.isRootOnly());

        formatter = new EditUser.ShortLabelFormatter("ho-1-2-3");
        assertEquals(formatter.getRoot(), "ho-1-2");
        assertTrue(formatter.hasBranch());
        assertEquals(formatter.getBranch(), "3");
        // No cloned name.
        assertFalse(formatter.hasClonedSuffix());
        // No a root entry.
        assertFalse(formatter.isRootOnly());

        // Cloned entries.

        formatter = new EditUser.ShortLabelFormatter("ho-x");
        assertEquals(formatter.getRoot(), "ho");
        // No branch.
        assertFalse(formatter.hasBranch());
        assertTrue(formatter.hasClonedSuffix());
        // No a root entry.
        assertFalse(formatter.isRootOnly());

        formatter = new EditUser.ShortLabelFormatter("ho-1-x");
        assertEquals(formatter.getRoot(), "ho");
        assertTrue(formatter.hasBranch());
        assertTrue(formatter.hasClonedSuffix());
        assertEquals(formatter.getBranch(), "1");
        // No a root entry.
        assertFalse(formatter.isRootOnly());

        formatter = new EditUser.ShortLabelFormatter("ho-1-2-x");
        assertEquals(formatter.getRoot(), "ho-1");
        assertTrue(formatter.hasBranch());
        assertTrue(formatter.hasClonedSuffix());
        assertEquals(formatter.getBranch(), "2");
        // No a root entry.
        assertFalse(formatter.isRootOnly());
    }

    public void testGetNextAvailableShortLabel() {
        EditUser user = new EditUser();
        // ga doesn't exist.
        assertEquals(user.getNextAvailableShortLabel(Interaction.class, "ga"),
                "ga");

        // cloning ga interaction.
        assertEquals(user.getNextAvailableShortLabel(Interaction.class, "ga-x"),
                "ga-376");

        // ho-1 exists
        assertEquals(user.getNextAvailableShortLabel(Interaction.class, "ho-1"),
                "ho-2");

        // ho doesn't exist
        assertEquals(user.getNextAvailableShortLabel(Interaction.class, "ho"),
                "ho");

        // cloning ga-2 interaction. ga-3 exists, so it goes for the largest number.
        assertEquals(user.getNextAvailableShortLabel(Interaction.class, "ga-2-x"),
                "ga-376");

        // cloning ga-2 interaction. ga-7 doesn't exist.
        assertEquals(user.getNextAvailableShortLabel(Interaction.class, "ga-6-x"),
                "ga-7");

        // go exists.
        assertEquals(user.getNextAvailableShortLabel(CvDatabase.class, "go"),
                "go-1");
    }
}
