/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.hierarchview.struts.view;

import uk.ac.ebi.intact.application.hierarchview.struts.framework.IntactBaseForm;

/**
 * This form captures the user action of the sideBarGraph.jsp.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionNetworkForm extends IntactBaseForm {

    // Class Data

    /**
     * Identifier for when Expand button is pressed.
     */
    private static final int EXPAND = 0;

    /**
     * Identifier for when Contract button is pressed.
     */
    private static final int CONTRACT = 1;

    // Instance Data

    /**
     * Saves the user action.
     */
    private int myAction;

    private String actionName;

    /**
     * Sets the action.
     * @param action the action for the form. If this contains the word
     * 'AC' then the search is by AC otherwise the search is by label.
     */
    public void setAction(String action) {
        actionName = action;

        if (action.equals("Expand")) {
            myAction = EXPAND;
        }
        else if (action.equals("Contract")) {
            myAction = CONTRACT;
        }
    }

    public String getAction () {
        return actionName;
    }

    /**
     * True if Expand/Contract button is pressed.
     */
    public boolean expandSelected() {
        return myAction == EXPAND;
    }

    /**
     * True if Contract All button is pressed.
     */
    public boolean contractSelected() {
        return myAction == CONTRACT;
    }
}