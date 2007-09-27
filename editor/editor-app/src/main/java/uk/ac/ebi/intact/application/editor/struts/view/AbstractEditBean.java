/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view;

import java.io.Serializable;

/**
 * Generic edit bean.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AbstractEditBean implements Serializable {

    /**
     * The bean is in the editing state.
     */
    public static final String VIEW = "editing";

    /**
     * The bean is in the saving state.
     */
    public static final String SAVE = "saving";

    /**
     * The bean is in the error state.
     */
    public static final String ERROR = "error";

    /**
     * Keeps track of the edit state; default is in view mode.
     */
    private String myEditState = VIEW;

    /**
     * Returns the status of the editing state.
     * @return {@link #VIEW} if this instance is currently in view mode;
     * for all other instances {@link #SAVE} is returned.
     */
    public String getEditState() {
        return myEditState;
    }

    /**
     * Sets this bean's edit state.
     * @param state the bean's edit state.
     *
     * <pre>
     * post: getEditState() = state
     * </pre>
     */
    public void setEditState(String state) {
        myEditState = state;
    }

    /**
     * Returns true if this bean is in error state.
     * @return true if this bean's state equals {@link #ERROR}.
     */
    public boolean isError() {
        return myEditState.equals(ERROR);
    }

    /**
     * Returns a link to display a read only window.
     * @param type the type for search application.
     * @param label the second parameter to the show command; this should
     * be the short label.
     * @return the link to display a read only version of window.
     */
    public static String getLink(String type, String label) {
        String link = "<a href=\"" + "javascript:show('" + type + "', '" + label + "')\""
                + ">" + label + "</a>";
        return link;
    }

    // Helper method for subclasses.

    /**
     * True if both objects are null or object i is equal to object 2.
     * @param obj1 the first object to compare.
     * @param obj2 the second object to compare.
     * @return true only if both objects are null or <code>obj1</code> equals
     * <code>obj2</code>. False is returned for all other instances.
     */
    protected boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 != null) {
            return obj1.equals(obj2);
        }
        return false;
    }
}
