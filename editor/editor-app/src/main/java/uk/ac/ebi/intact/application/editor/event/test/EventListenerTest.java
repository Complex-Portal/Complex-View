/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE 
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.editor.event.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.editor.event.EventListener;
import uk.ac.ebi.intact.application.editor.event.LoginEvent;
import uk.ac.ebi.intact.application.editor.event.LogoutEvent;

import java.util.Set;

/**
 * The test class for the EventListener class.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class EventListenerTest extends TestCase {

    /**
     * Constructs an instance with the specified name.
     * @param name the name of the test.
     */
    public EventListenerTest(String name) {
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
        return new TestSuite(EventListenerTest.class);
    }

    /**
     * Tests the notifyObservers
     */
    public void testNotifyObservers() {
        EventListener listener = EventListener.getInstance();

        // Login for user abc.
        LoginEvent abcEvent = new LoginEvent("abc");
        listener.notifyObservers(abcEvent);

        Set events = listener.getAuthenticationEvents();
        assertEquals(events.size(), 1);
        assertEquals(events.iterator().next(), abcEvent);

        // Login for user pqr.
        LoginEvent pqrEvent = new LoginEvent("pqr");
        listener.notifyObservers(pqrEvent);
        assertEquals(listener.getAuthenticationEvents().size(), 2);

        // user abc is logging out.
        listener.notifyObservers(new LogoutEvent("abc"));
        assertEquals(listener.getAuthenticationEvents().size(), 1);

        // Only user pqr is still logged in.
        assertEquals(events.size(), 1);
        assertEquals(events.iterator().next(), pqrEvent);

        // user pqr is logging out.
        listener.notifyObservers(new LogoutEvent("pqr"));

        // No events now.
        assertTrue(listener.getAuthenticationEvents().isEmpty());

        // Test for case sensitivity.
        listener.notifyObservers(new LoginEvent("abc"));
        assertEquals(listener.getAuthenticationEvents().size(), 1);

        // Same user but in uppercase
        listener.notifyObservers(new LoginEvent("ABC"));
        // Still only one user.
        assertEquals(listener.getAuthenticationEvents().size(), 1);
    }
}
