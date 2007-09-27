/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.sequence;

import uk.ac.ebi.intact.application.editor.struts.framework.EditorActionForm;

/**
 * The form for the sequence editor.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.form name="seqForm"
 */
public class SequenceActionForm extends EditorActionForm {

    /**
     * The sequence.
     */
    private String mySequence;

    /**
     * The interactor type
     */
    private String myInteractorType;

    /**
     * The host organism
     */
    private String myOrganism;
    
    // Getter / Setter methods

    public String getSequence() {
        return mySequence;
    }

    /**
     * @struts.validator type="mask" msgkey="error.seq.sequence.mask"
     * @struts.validator-args arg0resource="seq.sequence.label"
     * @struts.validator-var name="mask" value="^[A-Z]*$"
     */
    public void setSequence(String seq) {
        mySequence = seq;
    }

    public String getInteractorType() {
        return myInteractorType;
    }

    /**
     * @struts.validator type="mask" msgkey="error.seq.inttype"
     * @struts.validator-args arg0resource="seq.inttype.label"
     * @struts.validator-var name="mask" value="${menu-pat}"
     */
    public void setInteractorType(String interType) {
        myInteractorType = interType;
    }

    public String getOrganism() {
        return myOrganism;
    }

    /**
     * @struts.validator type="mask" msgkey="error.biosrc"
     * @struts.validator-var name="mask" value="${menu-pat}"
     */
    public void setOrganism(String biosrc) {
        myOrganism = biosrc;
    }
}
