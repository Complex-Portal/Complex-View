/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.interaction.test;

import junit.framework.TestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import uk.ac.ebi.intact.application.editor.struts.view.interaction.ItemLinkSorter;
import org.apache.commons.collections.CollectionUtils;

/**
 * The test class for ItemListSorter.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class ItemLinkSorterTest extends TestCase {

    /**
     * Constructs a ItemLinkSorterTest instance with the specified name.
     * @param name the name of the test.
     */
    public ItemLinkSorterTest(String name) {
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
        return new TestSuite(ItemLinkSorterTest.class);
    }

    public void testDoIt() {
        List linked = new ArrayList();
        List unlinked = new ArrayList();

        linked.add("1");
        linked.add("1");
        linked.add("2");
        linked.add("3");

        unlinked.add("1");
        unlinked.add("3");

        // Item 1 need to be linked twice.
        assertEquals(CollectionUtils.cardinality("1", linked), 2);
        // Item 2 and 3 need to be linked only once.
        assertEquals(CollectionUtils.cardinality("2", linked), 1);
        assertEquals(CollectionUtils.cardinality("3", linked), 1);

        // Item 2 and 3 need to be unlinked only once.
        assertEquals(CollectionUtils.cardinality("1", unlinked), 1);
        assertEquals(CollectionUtils.cardinality("3", unlinked), 1);

        ItemLinkSorter sorter = new ItemLinkSorter();
        sorter.doIt(linked, unlinked);

        // Items 1 & 2 need to be linked.
        Set set = sorter.getItemsToLink();
        assertEquals(set.size(), 2);
        assertTrue(set.contains("1"));
        assertTrue(set.contains("2"));

        // No items to unlink.
        set = sorter.getItemsToUnLink();
        assertTrue(set.isEmpty());

        // Another test.

        linked.clear();
        unlinked.clear();

        linked.add("1");
        linked.add("2");
        linked.add("3");

        unlinked.add("1");
        unlinked.add("1");
        unlinked.add("2");
        unlinked.add("2");
        unlinked.add("3");

        // Item 1,2 and 3 need to be linked once.
        assertEquals(CollectionUtils.cardinality("1", linked), 1);
        assertEquals(CollectionUtils.cardinality("2", linked), 1);
        assertEquals(CollectionUtils.cardinality("3", linked), 1);

        // Item 1 & 2 need to be unlinked twice.
        assertEquals(CollectionUtils.cardinality("1", unlinked), 2);
        assertEquals(CollectionUtils.cardinality("2", unlinked), 2);
        // Item 3 need to be unlinked only once.
        assertEquals(CollectionUtils.cardinality("3", unlinked), 1);

        // Do the sorting.
        sorter.doIt(linked, unlinked);

        // No items to link.
        set = sorter.getItemsToLink();
        assertTrue(set.isEmpty());

        // Items 2 & 3 need to be unlinked.
        set = sorter.getItemsToUnLink();
        assertEquals(set.size(), 2);
        assertTrue(set.contains("1"));
        assertTrue(set.contains("2"));

        // Add one more to the linked list.
        linked.add("4");

        // Do the sorting.
        sorter.doIt(linked, unlinked);

        // One item to link.
        set = sorter.getItemsToLink();
        assertEquals(set.size(), 1);
        assertTrue(set.contains("4"));

        // Items 2 & 3 need to be unlinked.
        set = sorter.getItemsToUnLink();
        assertEquals(set.size(), 2);
        assertTrue(set.contains("1"));
        assertTrue(set.contains("2"));
    }
}
