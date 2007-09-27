/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.framework;

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionErrors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorMenuFactory;
import uk.ac.ebi.intact.application.editor.struts.view.AbstractEditBean;
import uk.ac.ebi.intact.application.editor.struts.view.CommentBean;
import uk.ac.ebi.intact.application.editor.struts.view.XreferenceBean;
import uk.ac.ebi.intact.application.commons.util.DateToolbox;

import java.util.Iterator;
import java.util.List;
import java.util.Date;

/**
 * The form to edit cv data. This form also is the super class for other
 * editor (e.g., Experiment)
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.form name="cvForm"
 */
public class EditorActionForm extends DispatchActionForm implements EditorFormI {

    private static final Log log = LogFactory.getLog(EditorActionForm.class);
    /**
     * The short label.
     */
    private String myShortLabel;

    /**
     * The full name.
     */
    private String myFullName;

    /**
     * The name of the creator curator.
     */
    private String myCreator;

    /**
     * The name of the updator curator.
     */
    private String myUpdator;

    /**
     * The time of creation.
     */
    private Date myCreated;

    /**
     * The time of last update.
     */
    private Date myUpdated;
    /**
     * The accession number.
     */
    private String myAc;

    /**
     * The list of annotations.
     */
    private List myAnnotations;

    /**
     * The list of cross references.
     */
    private List myXrefs;

    /**
     * The page anchor to go when there is an error. Default is none.
     */
    private String myAnchor = "";

    /**
     * The annotation to add.
     */
    private CommentBean myNewAnnotation = new CommentBean();

    /**
     * The cross reference to add.
     */
    private XreferenceBean myNewXref = new XreferenceBean();

    // Getter/Setter methods for form attributes.

    /**
     * @struts.validator type="required"
     *
     * @struts.validator type="mask" msgkey="error.shortlabel.mask"
     * @struts.validator-args arg0resource="label.shortlabel"
     * @struts.validator-var name="mask" value="^[a-z0-9\-:_]+ ?[a-z0-9\-:_]+$"
     *
     * @struts.validator type="maxlength" msgkey="error.shortlabel.maxlength"
     * @struts.validator-args arg1value="${var:maxlength}"
     * @struts.validator-var name="maxlength" value="20"
     */
    public void setShortLabel(String label) {
        myShortLabel = label;
    }

    public String getShortLabel() {
        return myShortLabel;
    }

    public String getCreator() {
        return myCreator;
    }

    public void setCreator(String creator) {
        this.myCreator = creator;
    }

    public String getUpdator() {
        return myUpdator;
    }

    public void setUpdator(String updator) {
        this.myUpdator = updator;
    }

    public String getCreated() {
//        log.debug("The created date is " + myCreated.toString());
        return DateToolbox.formatDate(this.myCreated);
    }

    public void setCreated(Date created) {
        this.myCreated = created;
    }

    public String getUpdated() {
        return DateToolbox.formatDate(this.myUpdated);
    }

    public void setUpdated(Date updated) {
        this.myUpdated = updated;
    }

    public void setFullName(String fullname) {
        myFullName = fullname;
    }

    public String getFullName() {
        return myFullName;
    }

    public void setAc(String ac) {
        myAc = ac;
    }

    public String getAc() {
        return myAc;
    }

    public void setAnnotations(List annotations) {
        myAnnotations = annotations;
    }

    public List getAnnotations() {
        return myAnnotations;
    }

    public void setAnnotCmd(int index, String value) {
        setDispatch(index, value);
    }

    public CommentBean getSelectedAnnotation() {
        return (CommentBean) myAnnotations.get(getDispatchIndex());
    }

    public void setXrefs(List xrefs) {
        myXrefs = xrefs;
    }

    public List getXrefs() {
        return myXrefs;
    }

    public void setXrefCmd(int index, String value) {
        setDispatch(index, value);
    }

    public XreferenceBean getSelectedXref() {
        return (XreferenceBean) myXrefs.get(getDispatchIndex());
    }

    public CommentBean getNewAnnotation() {
        return myNewAnnotation;
    }

    public XreferenceBean getNewXref() {
        return myNewXref;
    }

    public void clearNewBeans() {
        myNewAnnotation.clear();
        myNewXref.clear();
    }

    public String getAnchor() {
        return myAnchor;
    }

    public void setAnchor(String anchor) {
        myAnchor = anchor;
    }

    // Validate methods

    public ActionErrors validateAddAnnotation() {
        ActionErrors errors = null;
        // The bean to extract the values.
        if (getNewAnnotation().getTopic().equals(EditorMenuFactory.SELECT_LIST_ITEM)) {
            // Set the anchor for the page to scroll.
            errors = new ActionErrors();
            errors.add("annotation", new ActionMessage("error.annotation.topic"));
        }
        return errors;
    }

    public ActionErrors validateAddCrossreference() {
        ActionErrors errors = null;
        // The bean to extract the values.
        XreferenceBean xb = getNewXref();
        if (xb.getDatabase().equals(EditorMenuFactory.SELECT_LIST_ITEM)) {
            errors = new ActionErrors();
            errors.add("xref.db", new ActionMessage("error.xref.database"));
        }
        // Primary id is required.
        if (errors == null && AbstractEditorAction.isPropertyEmpty(xb.getPrimaryId())) {
            errors = new ActionErrors();
            errors.add("xref.pid", new ActionMessage("error.xref.pid"));
        }
        return errors;
    }

    public ActionErrors validateSubmit() {
        ActionErrors errors = null;
        // Look for unsaved annotations.
        for (Iterator iter = myAnnotations.iterator(); iter.hasNext(); ) {
            CommentBean cb = (CommentBean) iter.next();
            if (!cb.getEditState().equals(AbstractEditBean.VIEW)) {
                errors = new ActionErrors();
                errors.add("annotation.unsaved",
                           new ActionMessage("error.annotation.unsaved"));
                break;
            }
        }
        // Look for unsaved xrefs.
        if (errors == null) {
            errors = validateUnsavedXref();
        }
        return errors;
    }

    public ActionErrors validateSaveAndContinue() {
        return validateSubmit();
    }

    public ActionErrors validateUnsavedXref() {
        ActionErrors errors = null;
        for (Iterator iter = myXrefs.iterator(); iter.hasNext(); ) {
            XreferenceBean xb = (XreferenceBean) iter.next();
            if (!xb.getEditState().equals(AbstractEditBean.VIEW)) {
                errors = new ActionErrors();
                errors.add("xref.unsaved",
                           new ActionMessage("error.xref.unsaved"));
                break;
            }
        }
        return errors;
    }


}
