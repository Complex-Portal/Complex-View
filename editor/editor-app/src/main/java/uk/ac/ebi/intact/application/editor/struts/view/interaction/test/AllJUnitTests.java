/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE 
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.editor.struts.view.interaction.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Contains all JUnit Tests class for the
 * uk.ac.ebi.intact.application.editor.struts.view.interaction package.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class AllJUnitTests extends TestCase {

    /**
     * Constructs an AllJUnitTests instance with the specified name.
     *
     * @param name the name of the test.
     */
    public AllJUnitTests(String name) {
        super(name);
    }

    /**
     * Returns a suite containing tests.
     *
     * </br><b>OCL:</b>
     * <pre>
     * post: return != null
     * post: return->forall(obj : Object | obj.oclIsTypeOf(Test))
     * </pre>
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(ItemLinkSorterTest.suite());
        return suite;
    }
}
