/*
 Copyright (c) 2002-2005 The European Bioinformatics Institute, and others.
 All rights reserved. Please see the file LICENSE
 in the root directory of this distribution.
 */

package uk.ac.ebi.intact.application.editor.struts.view.experiment;

import java.util.ResourceBundle;
import java.util.Date;

import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.application.editor.struts.view.wrappers.ResultRowData;

/**
 * This class contains data for an Interaction row in the experiment editor.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionRowData extends ResultRowData {

    /**
     * The underlying Interaction. Could be null if none assigned to it (e.g. search).
     */
    private Interaction myInteraction;
    
    /**
     * This contains HTML script for action button.
     */
    private String myAction;

    // Static methods

    /**
     * Creates a Row with action set to Add/Hide Interaction.
     * @param rowData the row
     * @return a table row with action set to Add/Hide Interaction.
     */
    public static InteractionRowData makeSearchRow(ResultRowData rowData) {
        InteractionRowData row = new InteractionRowData(rowData.getAc(),
               rowData.getShortLabel(), rowData.getFullName(), rowData.getCreator(),
               rowData.getUpdator(), rowData.getCreated(), rowData.getUpdated());
        row.setSearchActionString();
        return row;
    }

    /**
     * This constructor is mainly used for creating an instance to find it in a
     * collection.
     * @param ac the ac is required as it is used for equals method.
     */
    public InteractionRowData(String ac) {
        this(ac, null, null, null, null, null, null);
    }

    /**
     * Creates an instance of this class using given Interaction.
     * @param inter the interaction to wrap this instance around.
     */
    public InteractionRowData(Interaction inter) {
        this(inter.getAc(), inter.getShortLabel(), inter.getFullName(), inter.getCreator(),
             inter.getUpdator(), inter.getCreated(), inter.getUpdated());
        myInteraction = inter;
        setActionString();
    }

    /**
     * Creates an instance of this class using ac, shortlabel and fullname.
     * @param ac
     * @param shortlabel
     * @param fullname
     */

    private InteractionRowData(String ac, String shortlabel, String fullname, String updator, String creator, Date created, Date updated) {
        super(ac, shortlabel, fullname, updator, creator, created, updated);
    }

    public Interaction getInteraction() {
        return myInteraction;
    }
    
    public String getAction() {
        return myAction;
    }

    // Setter methods.

    /**
     * Sets the default string for action buttons. This string is for Edit/Delete
     * Interactions.
     */
    public void setActionString() {
        // Resource bundle to access the message resources to set keys.
        ResourceBundle msgres = ResourceBundle
                .getBundle("uk.ac.ebi.intact.application.editor.MessageResources");
        myAction = "<input type=\"submit\" name=\"dispatch\" value=\""
                + msgres.getString("exp.int.button.edit") + "\""
                + " onclick=\"setIntAc('" + getAc() + "');\">"
                + "<input type=\"submit\" name=\"dispatch\" value=\""
                + msgres.getString("exp.int.button.del") + "\""
                + " onclick=\"setIntAc('" + getAc() + "');\">";
    }

    /**
     * Sets the action string for Interaction search.
     */
    private void setSearchActionString() {
        // Resource bundle to access the message resources to set keys.
        ResourceBundle msgres = ResourceBundle
                .getBundle("uk.ac.ebi.intact.application.editor.MessageResources");

        myAction = "<input type=\"submit\" name=\"dispatch\" value=\""
                + msgres.getString("exp.int.button.add") + "\""
                + " onclick=\"setIntAc('" + getAc() + "');\">"
                + "<input type=\"submit\" name=\"dispatch\" value=\""
                + msgres.getString("exp.int.button.hide") + "\""
                + " onclick=\"setIntAc('" + getAc() + "');\">";
    }
}