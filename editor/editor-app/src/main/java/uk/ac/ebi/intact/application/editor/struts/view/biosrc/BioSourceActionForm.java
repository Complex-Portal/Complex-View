/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.biosrc;

import uk.ac.ebi.intact.application.editor.struts.framework.EditorActionForm;

/**
 * The form to validate biosource data.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.form name="bsForm"
 */
public class BioSourceActionForm extends EditorActionForm {

    /**
     * The tax id.
     */
    private String myTaxId;

    /**
     * The CV cell type.
     */
    private String myCellType;

    /**
     * The tissue
     */
    private String myTissue;

    // Getter / Setter methods

    public String getTaxId() {
        return myTaxId;
    }

    /**
     * @struts.validator type="required"
     * @struts.validator-args arg0resource="biosource.label.tax"
     *
     * @struts.validator type="mask" msgkey="error.taxid.mask"
     * @struts.validator-var name="mask" value="^[0-9\-]+$"
     */
    public void setTaxId(String taxId) {
        myTaxId = taxId;
    }

    public String getCellType() {
        return myCellType;
    }

    public void setCellType(String cellType) {
        myCellType = cellType;
    }

    public String getTissue() {
        return myTissue;
    }

    public void setTissue(String tissue) {
        myTissue = tissue;
    }
}
