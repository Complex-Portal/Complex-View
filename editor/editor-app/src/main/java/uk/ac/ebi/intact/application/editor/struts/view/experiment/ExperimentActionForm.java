/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.experiment;

import uk.ac.ebi.intact.application.editor.struts.framework.EditorActionForm;

/**
 * The form to edit bio experiment data.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.form name="expForm"
 */
public class ExperimentActionForm extends EditorActionForm {

    private String myPubmedId;
    private String myInteractionAC;
    private String myOragnism;
    private String myInter;
    private String myIdent;
    private String mySearchValue;

     public void setPubmedId(String pubmedId) {
        myPubmedId = pubmedId;
    }

    public String getPubmedId() {
        return myPubmedId;
    }


    public void setIntac(String intac) {
        myInteractionAC = intac;
    }

    public String getIntac() {
        return myInteractionAC;
    }

    /**
     * @struts.validator type="mask" msgkey="error.biosrc"
     * @struts.validator-var name="mask" value="${menu-pat}"
     */
    public void setOrganism(String organism) {
        myOragnism = organism;
    }

    public String getOrganism() {
        return myOragnism;
    }

    /**
     * @struts.validator type="mask" msgkey="error.exp.inter"
     * @struts.validator-var name="mask" value="${menu-pat}"
     */
    public void setInter(String inter) {
        myInter = inter;
    }

    public String getInter() {
        return myInter;
    }

    /**
     * @struts.validator type="mask" msgkey="error.exp.ident"
     * @struts.validator-var name="mask" value="${menu-pat}"
     */
    public void setIdent(String ident) {
        myIdent = ident;
    }

    public String getIdent() {
        return myIdent;
    }

    public void setSearchValue(String value) {
        mySearchValue = value;
    }

    public String getSearchValue() {
        return mySearchValue;
    }
}
