/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.hierarchview.struts.view;

import uk.ac.ebi.intact.application.hierarchview.struts.framework.IntactBaseForm;

/**
 * This form captures the user action of the sideBarClickBehaviour.jsp.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ClickBehaviourForm extends IntactBaseForm {

    /**
     * Identifier for when Expand button is pressed.
     */
    private static final int CENTER = 0;

    /**
     * Identifier for when Contract button is pressed.
     */
    private static final int ADD = 1;


    /**
     * Saves the user action.
     */
    private int myAction;

    private String actionName;

    /**
     * Sets the action.
     * @param action the action for the form.
     */
    public void setAction(String action) {
        actionName = action;

        if (action.equals("center")) {
            myAction = CENTER;
        }
        else if (action.equals("add")) {
            myAction = ADD;
        }
    }

    public String getAction () {
        return actionName;
    }

    /**
     * True if center radio button is selected.
     */
    public boolean centerSelected() {
        return myAction == CENTER;
    }

    /**
     * True if add radio button is selected.
     */
    public boolean addSelected() {
        return myAction == ADD;
    }
}