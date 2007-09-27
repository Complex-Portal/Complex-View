/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.feature;

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionErrors;
import uk.ac.ebi.intact.application.commons.util.DateToolbox;
import uk.ac.ebi.intact.application.editor.business.EditorService;
import uk.ac.ebi.intact.application.editor.struts.framework.DispatchActionForm;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorActionForm;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorFormI;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorMenuFactory;
import uk.ac.ebi.intact.application.editor.struts.view.AbstractEditBean;
import uk.ac.ebi.intact.application.editor.struts.view.CommentBean;
import uk.ac.ebi.intact.application.editor.struts.view.XreferenceBean;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The action form for the Feature editor. This form wraps around an EditorActionForm
 * so it can provide its own setShortLabel() method with struts tags.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.form name="featureForm"
 */
public class    FeatureActionForm extends DispatchActionForm implements EditorFormI {

    /**
     * The pattern to match for a mutation entry.
     * Patern: starts with an alpha character, followed by digits and an alpha char.
     */
    public static final Pattern MUT_ITEM_REGX = Pattern.compile("^([a-z]+)(\\d+)([a-z]+)$");

    /**
     * Delegator for EditorFormI methods.
     */
    private EditorActionForm myDelegate = new EditorActionForm();

    /**
     * The creator name.
     */
    private String myCreator;

    /**
     * The updator name.
     */
    private String myUpdator;

    /**
     * The time of creation.
     */
    private Date myCreated;

    /**
     * The time of last update
     */
    private Date myUpdated;

    /**
     * The parent ac
     */
    private String myParentAc;

    /**
     * The parent short label.
     */
    private String myParentShortLabel;

    /**
     * The parent fullname.
     */
    private String myParentFullName;

    /**
     * The feature type.
     */
    private String myFeatureType;

    /**
     * The feature identification.
     */
    private String myFeatureIdent;

    /**
     * List of ranges for the feature.
     */
    private List myRanges;

    /**
     * The new range as a bean.
     */
    private RangeBean myNewRange = new RangeBean();

    /**
     * Keeps track of mutation state.
     */
    private boolean myMutationState;

    // Implementation of EditorFormI methods
    /**
     * Need to override the super method write these tags to the validation file.
     *
     * @struts.validator type="required"
     *
     * @struts.validator type="mask" msgkey="error.shortlabel.mask"
     * @struts.validator-args arg0resource="label.shortlabel"
     * @struts.validator-var name="mask" value="^[a-z0-9][a-z0-9\-:_\s]*[a-z0-9]$"
     *
     * @struts.validator type="maxlength" msgkey="error.shortlabel.maxlength"
     * @struts.validator-args arg1value="${var:maxlength}"
     * @struts.validator-var name="maxlength" value="20"
     */
    public void setShortLabel(String label) {
        myDelegate.setShortLabel(label);
    }

    public String getShortLabel() {
        return myDelegate.getShortLabel();
    }

    public void setFullName(String fullname) {
        myDelegate.setFullName(fullname);
    }

    public String getFullName() {
        return myDelegate.getFullName();
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

    public void setAc(String ac) {
        myDelegate.setAc(ac);
    }

    public String getAc() {
        return myDelegate.getAc();
    }

    public void setAnnotations(List annotations) {
        // No annotations for Feature
    }

    public List getAnnotations() {
        // No annotations for Feature
        return null;
    }

    public void setAnnotCmd(int index, String value) {
        // No annotations for Feature
    }

    public CommentBean getSelectedAnnotation() {
        // No annotations for Feature
        return null;
    }

    public void setXrefs(List xrefs) {
        myDelegate.setXrefs(xrefs);
    }

    public List getXrefs() {
        return myDelegate.getXrefs();
    }

    public void setXrefCmd(int index, String value) {
        setDispatch(index, value);
    }

    public XreferenceBean getSelectedXref() {
        return (XreferenceBean) myDelegate.getXrefs().get(getDispatchIndex());
    }

    public CommentBean getNewAnnotation() {
        return myDelegate.getNewAnnotation();
    }

    public XreferenceBean getNewXref() {
        return myDelegate.getNewXref();
    }

    public void clearNewBeans() {
        myDelegate.clearNewBeans();
    }

    public String getAnchor() {
        return myDelegate.getAnchor();
    }

    public void setAnchor(String anchor) {
        myDelegate.setAnchor(anchor);
    }

    // Getter/Setter methods.

    public String getParentAc() {
        return myParentAc;
    }

    public void setParentAc(String ac) {
        myParentAc = ac;
    }

    public String getParentShortLabel() {
        return myParentShortLabel;
    }

    public void setParentShortLabel(String label) {
        myParentShortLabel = label;
    }

    public String getParentFullName() {
        return myParentFullName;
    }

    public void setParentFullName(String fullname) {
        myParentFullName = fullname;
    }

    public String getFeatureType() {
        return myFeatureType;
    }

    /**
     * @struts.validator type="mask" msgkey="error.feature.cvtype"
     * @struts.validator-var name="mask" value="${menu-pat}"
     */
    public void setFeatureType(String type) {
        myFeatureType = type;
    }

    public String getFeatureIdent() {
        return myFeatureIdent;
    }

    public void setFeatureIdent(String ident) {
        myFeatureIdent = ident;
    }

    /**
     * Sets the dispatch with index of the button. This was called for selecting
     * Edit/Delete range buttons.
     *
     * @param index index of the button.
     * @param value the button label.
     */
    public void setRangeCmd(int index, String value) {
        setDispatch(index, value);
    }

    /**
     * Sets ranges for the feature.
     *
     * @param ranges a list of ranges for a <code>Feature</code>.
     * <p/>
     * <pre>
     * pre:  forall(obj : Object | obj.oclIsTypeOf(RangeBean))
     * </pre>
     */
    public void setRanges(List ranges) {
        myRanges = ranges;
    }

    public List getRanges() {
        return myRanges;
    }

    public void setNewRange(RangeBean rb) {
        myNewRange = rb;
    }

    public RangeBean getNewRange() {
        return myNewRange;
    }

    /**
     * Override the super method to reset the new range bean.
     */
//    public void resetNewBeans() {
//        super.resetNewBeans();
//        myNewRange.reset();
//    }

    /**
     * Returns the selected range.
     *
     * @return the selected range as indicated by the dispatch index.
     */
    public RangeBean getSelectedRange() {
        return (RangeBean) myRanges.get(getDispatchIndex());
    }

    public void setMutationState(boolean state) {
        myMutationState = state;
    }

    // Validate Methods

    public ActionErrors validateAddRange() {
        return getNewRange().validate("new");
    }

    public ActionErrors validateSaveRange() {
        return getSelectedRange().validate("edit");
    }

    public ActionErrors validateSubmit() {
        // Look for unsaved xrefs.
        ActionErrors errors = myDelegate.validateUnsavedXref();
        if (errors != null) {
            return errors;
        }
        // Do the mutation specific validation in mutation mode.
        if (myMutationState) {
            // Check the full name for Feature mutations.
            errors = validateMutations();
        }
        else {
            // Must have ranges.
            if (myRanges.isEmpty()) {
                errors = new ActionErrors();
                errors.add("feature.range.empty",
                           new ActionMessage("error.feature.range.empty"));
            }
            else {
                // Check for unsaved ranges.
                errors = checkUnsavedRanges();
            }
        }
        return errors;
    }

    // Override the super method
    public ActionErrors validateSaveAndContinue() {
        return validateSubmit();
    }

    /**
     * This method is purely for testing the validation of mutations.
     * @param fullname
     * @param featureSep
     * @param rangeSep
     * @return and ActionMessage
     */
    public ActionErrors testValidateMutations(String fullname, String featureSep,
                                              String rangeSep) {
        return doValidateMutations(fullname, featureSep, rangeSep);
    }

    /**
     * Checks for unsaved ranges. A range not in a view state is flagged as an error.
     *
     * @return null if no errors found.
     */
    private ActionErrors checkUnsavedRanges() {
        // The errors to return.
        ActionErrors errors = null;

        // Look for any unsaved or error proteins.
        for (Iterator iter = myRanges.iterator(); iter.hasNext();) {
            RangeBean rb = (RangeBean) iter.next();
            // They all must be in view mode. Flag an error if not.
            if (!rb.getEditState().equals(AbstractEditBean.VIEW)) {
                errors = new ActionErrors();
                errors.add("feature.range.unsaved",
                           new ActionMessage("error.feature.range.unsaved"));
                break;
            }
        }
        return errors;
    }

    private ActionErrors validateMutations() {
        EditorService service = (EditorService)
                super.getServlet().getServletContext().getAttribute(EditorConstants.EDITOR_SERVICE);

        String featureSep = service.getResource("mutation.feature.sep");
        String rangeSep = service.getResource("mutation.range.sep");
        return doValidateMutations(getFullName(), featureSep, rangeSep);
    }

    /**
     * Does the mutation validation  here. Need this method for testing.
     * @param text the text to do the validation
     * @param featureSep the separator char for a feature
     * @param rangeSep the seprator char for a range
     * @return may be null if there no errors.
     */
    private static ActionErrors doValidateMutations(String text, String featureSep,
                                                    String rangeSep) {
        // The errors to return.
        ActionErrors errors = null;

        StringTokenizer stk1 = new StringTokenizer(text, featureSep);
        if (!stk1.hasMoreTokens()) {
            // No Features given in the full name.
            errors = new ActionErrors();
            errors.add("feature.mutation.empty",
                       new ActionMessage("error.feature.mutation.empty"));
            return errors;
        }
        // Found some features.

        // Collector for range values.
        List rangeValues = new ArrayList();
        do {
            rangeValues.clear();
            String feature = stk1.nextToken();
            StringTokenizer stk2 = new StringTokenizer(feature, rangeSep);
            if (!stk2.hasMoreTokens()) {
                // Only a single range specified
                errors = validateMutationElement(feature.trim(), rangeValues);
                continue;
            }
            do {
                // Ranges specified
                String range = stk2.nextToken().trim();
                errors = validateMutationElement(range, rangeValues);
            }
            while (stk2.hasMoreTokens() && (errors == null));
        }
        while (stk1.hasMoreTokens() && (errors == null));
        return errors;
    }

    /**
     * Performs the validation on an individual element.
     * @param element the element to perform the validation.
     * @param seen a list of seen ranges for a Feature
     * @return null if there are no errors.
     */
    private static ActionErrors validateMutationElement(String element, List seen) {
        // The action errors to return.
        ActionErrors errors = null;
        Matcher matcher = MUT_ITEM_REGX.matcher(element);
        if (matcher.matches()) {
            // The element is in correct format.
            if (matcher.group(1).equals(matcher.group(3))) {
                errors = new ActionErrors();
                errors.add("feature.mutation.invalid",
                           new ActionMessage("error.feature.mutation.same", matcher.group(1),
                                           matcher.group(3)));
            }
            // A valid element. Check if the range value was seen before or not.
            if (seen.contains(matcher.group(2))) {
                errors = new ActionErrors();
                errors.add("feature.mutation.invalid",
                           new ActionMessage("error.feature.mutation.range", element,
                                           matcher.group(2)));
            }
            else {
                // Not seen. Add it to the seen collection.
                seen.add(matcher.group(2));
            }
        }
        else {
            // Invalid entry.
            errors = new ActionErrors();
            errors.add("feature.mutation.invalid",
                       new ActionMessage("error.feature.mutation.format", element));
        }
        return errors;
    }

    /**
     * Make sure that the databalse is not still set to EditorMenuFactory.SELECT_LIST_ITEM and that the primaryId is not
     * empty.
     * @return null if there are no errors.
     */
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


}
