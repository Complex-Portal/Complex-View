/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.framework.util;

/**
 * Forward constants for the editor.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public interface ForwardConstants {

    /**
     * The key to success action.
     */
    public static final String SUCCESS = "success";

    /**
     * Used in various action classes to define where to forward
     * to on different conditions.  See the struts-config.xml file
     * to see where the page that is using this forwards to.
     */
    public static final String FAILURE = "failure";

    /**
     * Forward to the search page.
     */
    public static final String SEARCH = "search";

    /**
     * Forward to the result page.
     */
    public static final String RESULT = "result";

    /**
     * Forward to the reload action.
     */
    public static final String RELOAD = "reload";
    
    /**
     * Used as a key to identify a page to display when matches are found
     * from a search.
     */
    public static final String MATCH = "match";

    /**
     * Used as a key to identify a page to display when no matches are found
     * from a search.
     */
    public static final String NO_MATCH = "noMatch";

    /**
     * Forward to the experiment editor from the interaction editor.
     */
    public static final String EXP = "experiment";

    /**
     * Forward to the interaction editor from the feature editor.
     */
    public static final String INT = "interaction";

    /**
     * Forward to the feature editor from the interaction editor.
     */
    public static final String FEATURE = "feature";
}
