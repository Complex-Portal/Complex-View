/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.framework.util;

/**
 * Contains constants required for the editor. Constants for forward actions
 * are defined in ForwardConstants interface.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public interface EditorConstants {

    /**
     * The name of the logger.
     */
    public static final String LOGGER = "editor";

    /**
     * The key to store an Intact Service object.
     */
    public static final String EDITOR_SERVICE = "service";

    /**
     * To access intact types.
     */
    public static final String EDITOR_TOPICS = "topics";

    /**
     * The key to access an Intact user.
     */
    public static final String INTACT_USER = "user";

    /**
     * The key to access the server path.
     */
    public static final String SERVER_PATH = "path";

    /**
     * The default help link.
     */
    public static final String HELP_TITLE = "[?]";

    /**
     * The lock manager.
     */
    public static final String LOCK_MGR = "lmr";

    /**
     * The key for the severe warning messages.
     */
    public static final String SEVERE_WARN = "severe-warning";
    
    /**
     * The key to access the anchor map.
     */
    public static final String ANCHOR_MAP = "anchors";

    /**
     * The key to access the login status.
     */
    public static final String LOGGED_IN = "login";

    /**
     * The key to accss the event listener.
     */
    public static final String EVENT_LISTENER = "listener";
}
