/*
 Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
 All rights reserved. Please see the file LICENSE
 in the root directory of this distribution.
 */

package uk.ac.ebi.intact.application.editor.struts.view.interaction;

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionErrors;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorActionForm;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureBean;

import java.util.Iterator;
import java.util.List;

/**
 * The Interaction form.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.form name="intForm"
 */
public class InteractionActionForm extends EditorActionForm {

    /**
     * KD
     */
    private Float myKD;

    /**
     * Host organism.
     */
    private String myOragnism;

    /**
     * CV Interaction type.
     */
    private String myInteractionType;

    /**
     * The short label to search an experiment.
     */
    private String myExpSearchValue;

    /**
     * The AC to search an experiment.
     */
//    private String myExpSearchAC;

    /**
     * The short label to search a protein.
     */
    private String myProSearchLabel;

    /**
     * The SP AC to search a protein.
     */
    private String myProtSearchSpAC;

    /**
     * The AC to search a protein.
     */
    private String myProtSearchAC;

    /**
     * The list of experiments in the current interaction.
     */
    private List myExperiments;

    /**
     * The list of proteins in the current interaction.
     */
    private List myComponents;

    /**
     * The list of proteins on hold for the current interaction.
     */
    private List myExpsOnHold;

    // Setter / Getter methods.
    public void setKd(Float kd) {
        myKD = kd;
    }

    public Float getKd() {
        return myKD;
    }

    public void setOrganism(String organism) {
        myOragnism = organism;
    }

    public String getOrganism() {
        return myOragnism;
    }

    /**
     * @struts.validator type="mask" msgkey="error.int.cvtype"
     * @struts.validator-var name="mask" value="${menu-pat}"
     */
    public void setInteractionType(String inter) {
        myInteractionType = inter;
    }

    public String getInteractionType() {
        return myInteractionType;
    }

    public void setExpSearchValue(String value) {
        myExpSearchValue = value;
    }

    public String getExpSearchValue() {
        return myExpSearchValue;
    }
//
//    public void setExpSearchLabel(String label) {
//        myExpSearchLabel = label;
//    }
//
//    public String getExpSearchLabel() {
//        return myExpSearchLabel;
//    }
//
//    public void setExpSearchAC(String ac) {
//        myExpSearchAC = ac;
//    }
//
//    public String getExpSearchAC() {
//        return myExpSearchAC;
//    }

    public void setProtSearchLabel(String label) {
        myProSearchLabel = label.trim();
    }

    public String getProtSearchLabel() {
        return myProSearchLabel;
    }

    public void setProtSearchSpAC(String spac) {
        myProtSearchSpAC = spac.trim();
    }

    public String getProtSearchSpAC() {
        return myProtSearchSpAC;
    }

    public void setProtSearchAC(String ac) {
        myProtSearchAC = ac.trim();
    }

    public String getProtSearchAC() {
        return myProtSearchAC;
    }

    public void setExperiments(List exps) {
        myExperiments = exps;
    }

    public List getExperiments() {
        return myExperiments;
    }

    public void setComponents(List comps) {
        myComponents = comps;
    }

    public List getComponents() {
        return myComponents;
    }

    public void setExpsOnHold(List exps) {
        myExpsOnHold = exps;
    }

    public List getExpsOnHold() {
        return myExpsOnHold;
    }

    public void setExpCmd(int index, String value) {
        setDispatch(index, value);
    }

    public ExperimentRowData getSelectedExperiment() {
        return (ExperimentRowData) myExperiments.get(getDispatchIndex());
    }

    public void setExpOnHoldCmd(int index, String value) {
        setDispatch(index, value);
    }

    public ExperimentRowData getSelectedExpOnHoldCmd() {
        return (ExperimentRowData) myExpsOnHold.get(getDispatchIndex());
    }

    public void setProtCmd(int index, String value) {
        setDispatch(index, value);
    }

    public ComponentBean getSelectedComponent() {
        return (ComponentBean) myComponents.get(getDispatchIndex());
    }

    // There is no need for a method to set protein dispatch because it
    // is already done via protCmd method.

    public void setDispatchFeature(String dispatch) {
        // Only set it if not error (defualt)
        if (!dispatch.equals("error")) {
            setDispatch(dispatch);
        }
    }

    /**
     * Validates the form for when Link Selected Features button was selected.
     *
     * @return errors if two features not selected (exactly). A null is returned
     * if there no errors.
     */
    public ActionErrors validateLinkFeatures() {
        ActionErrors errors = null;
        int count = 0;
        for (Iterator iter0 = getComponents().iterator(); iter0.hasNext()
                && count <= 2;) {
            ComponentBean cb = (ComponentBean) iter0.next();
            for (Iterator iter1 = cb.getFeatures().iterator(); iter1.hasNext()
                    && count <= 2;) {
                FeatureBean fb = (FeatureBean) iter1.next();
                if (fb.isChecked()) {
                    ++count;
                }
            }
        }
        if (count != 2) {
            errors = new ActionErrors();
            errors.add("feature.link", new ActionMessage("error.int.feature.link"));
        }
        return errors;
    }

    /**
     * Validates the form for when Unlink Selected Feature button was selected.
     *
     * @return errors if a single feature wasn't selected. A null is returned
     * if there no errors.
     */
    public ActionErrors validateUnlinkFeatures() {
        ActionErrors errors = null;
        int count = 0;
        for (Iterator iter0 = getComponents().iterator(); iter0.hasNext()
                && count <= 1;) {
            ComponentBean cb = (ComponentBean) iter0.next();
            for (Iterator iter1 = cb.getFeatures().iterator(); iter1.hasNext()
                    && count <= 1;) {
                FeatureBean fb = (FeatureBean) iter1.next();
                if (fb.isChecked()) {
                    ++count;
                }
            }
        }
        if (count != 1) {
            errors = new ActionErrors();
            errors.add("feature.link", new ActionMessage("error.int.feature.unlink"));
        }
        return errors;
    }
}