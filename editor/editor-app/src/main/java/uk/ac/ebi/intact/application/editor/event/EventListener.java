/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.event;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * The event listener.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class EventListener extends Observable {

    // -------------------------------------------------------------------------

    private class AuthenticateObserver implements Observer {

        /**
         * Maintains a list of authenticate events
         */
        private Set<AuthenticationEvent> myAuthenticateEvents = new HashSet();

        // Implement the Observer interface

        public void update(Observable observable, Object arg) {
            // Only handle login or logout related events.
            if (!(arg instanceof AuthenticationEvent)) {
                return;
            }
            if (arg instanceof LoginEvent) {
                myAuthenticateEvents.add((AuthenticationEvent)arg);
            }
            else {
                // Must be a logout event.
                myAuthenticateEvents.remove(arg);
            }
        }

        /**
         * Returns the current list of authenticate events.
         *
         * @return returns the current list of authenticate events (cloning is
         *         not necessary as set items are immutable).
         */
        private Set<AuthenticationEvent> getAuthenticationEvents() {
            return myAuthenticateEvents;
        }
    }

    // -- End of Inner class --------------------------------------------------

    /**
     * The only instance of this class.
     */
    private static final EventListener ourInstance = new EventListener();

    /**
     * @return the only instance of this class.
     */
    public static EventListener getInstance() {
        return ourInstance;
    }

    /**
     * Reference to the user observer
     */
    private AuthenticateObserver myAuthenticateObserver = new AuthenticateObserver();

    /**
     * Default constructor. Private to disable instantiate it from outside.
     * Adds default listeners.
     */
    private EventListener() {
        addObserver(myAuthenticateObserver);
    }

    /**
     * Notifies registered observers about the change.
     * @param event the event to notify the observers with.
     */
    @Override
    public void notifyObservers(Object event) {
        // Otherwise it won't propagate changes:
        setChanged();
        super.notifyObservers(event);
    }

    /**
     * Returns the current set of authenticate events via the authenticate observer.
     *
     * @return returns the current set of authenticate events (cloning is
     *         not necessary as set items are immutable).
     */
    public Set<AuthenticationEvent> getAuthenticationEvents() {
        return myAuthenticateObserver.getAuthenticationEvents();
    }
}
