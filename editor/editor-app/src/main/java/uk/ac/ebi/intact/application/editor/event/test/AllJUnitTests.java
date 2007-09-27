/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.event.test;

// JUnit classes

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Testsuite that is composed of the individual test classes. Any new test
 * class should be added to this class.
 *
 * @author Sugath Mudali
 * @version $Id$
 */
public class AllJUnitTests extends TestCase {

    /**
     * The constructor with the test name.
     *
     * @param name the name of the test.
     */
    public AllJUnitTests(String name) {
        super(name);
    }

    /**
     * Returns a suite containing tests.
     * @return a suite containing tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(EventListenerTest.suite());
        return suite;
    }
}
