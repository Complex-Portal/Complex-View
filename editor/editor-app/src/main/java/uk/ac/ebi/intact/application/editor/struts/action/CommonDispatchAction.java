/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action;

import org.apache.struts.action.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.commons.util.DateToolbox;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.exception.SessionExpiredException;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorDispatchAction;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorFormI;
import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.CommentBean;
import uk.ac.ebi.intact.application.editor.struts.view.XreferenceBean;
import uk.ac.ebi.intact.application.editor.struts.view.biosrc.BioSourceViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.cv.CvViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.experiment.ExperimentActionForm;
import uk.ac.ebi.intact.application.editor.struts.view.experiment.ExperimentViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.InteractionViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.sequence.NucleicAcidViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.sequence.ProteinViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.sequence.SequenceViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.sm.SmallMoleculeViewBean;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.ExperimentDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * This dispatcher class contains common dispath events for all the forms.
 * The common dispatch events are the actions related to action buttons plus
 * adding new annotations and xrefs.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class CommonDispatchAction extends AbstractEditorDispatchAction {

    /**
     * Commons Logging instance.
     */
    protected static Log log = LogFactory.getLog(CommonDispatchAction.class);

    public static String allowedChar = "A to Z, a to z, 0 to 9 and : !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";//new String();

    /**
     * Provides the mapping from resource key to method name.
     *
     * @return Resource key / method name map.
     */
    protected Map getKeyMethodMap() {
        Map map = new HashMap();
        map.put("button.submit", "submit");
        map.put("button.save.continue", "save");
        map.put("button.clone", "clone");
        map.put("annotations.button.add", "addAnnot");

        map.put("exp.button.review","addAnnot");
        map.put("exp.button.accept","addAnnot");

        map.put("xrefs.button.add", "addXref");
        return map;
    }

    /**
     * Action for submitting the edit form.
     *
     * @param mapping the <code>ActionMapping</code> used to select this instance
     * @param form the optional <code>ActionForm</code> bean for this request
     * (if any).
     * @param request the HTTP request we are processing
     * @param response the HTTP response we are creating
     * @return failure mapping for any errors in updating the CV object; search
     *         mapping if the update is successful and the previous search has only one
     *         result; results mapping if the update is successful and the previous
     *         search has produced multiple results.
     * @throws Exception for any uncaught errors.
     */
    public ActionForward submit(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
            throws Exception {
        ActionForward forward = submitForm(mapping, form, request, true);

        // Only return to the result page for a successful submission.
        if (forward.getPath().equals(mapping.findForward(SUCCESS).getPath())) {
            // Handler to the Intact User.
            EditUserI user = getIntactUser(request);

            // The current view.
            AbstractEditViewBean view = user.getView();

            // Update the search cache.
            user.updateSearchCache(view.getAnnotatedObject());

            // Add the current edited object to the recent list.
            view.addToRecentList(user);

            // Only show the submitted record.
            forward = mapping.findForward(RESULT);
        }
        return forward;
    }

    /**
     * Action for saving the edit form.
     *
     * @param mapping the <code>ActionMapping</code> used to select this instance
     * @param form the optional <code>ActionForm</code> bean for this request
     * (if any).
     * @param request the HTTP request we are processing
     * @param response the HTTP response we are creating
     * @return failure mapping for any errors in updating the CV object; search
     *         mapping if the update is successful and the previous search has only one
     *         result; results mapping if the update is successful and the previous
     *         search has produced multiple results.
     * @throws Exception for any uncaught errors.
     */
    public ActionForward save(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
            throws Exception {
        ActionForward forward = submitForm(mapping, form, request, false);
        // Turn editing mode on as it was switched off upon a successfull comitt
        if (forward.equals(mapping.findForward(SUCCESS))) {
            getIntactUser(request).startEditing();
        }
        // Handler to the Intact User.
        EditUserI user = getIntactUser(request);

        // The current view.
        AbstractEditViewBean view = user.getView();

        getLockManager().acquire(view.getAc(),user.getUserName());

        return forward;
    }

    /**
     * Action for cloning the edit form. The current object is saved before
     * cloning it.
     *
     * @param mapping the <code>ActionMapping</code> used to select this instance
     * @param form the optional <code>ActionForm</code> bean for this request
     * (if any).
     * @param request the HTTP request we are processing
     * @param response the HTTP response we are creating
     * @return failure mapping for any errors in updating the CV object; search
     *         mapping if the update is successful and the previous search has only one
     *         result; results mapping if the update is successful and the previous
     *         search has produced multiple results.
     * @throws Exception for any uncaught errors.
     */
    public ActionForward clone(ActionMapping mapping,
                               ActionForm form,
                               HttpServletRequest request,
                               HttpServletResponse response)
            throws Exception {
        // Save the form first. Analyze the forward path.
        ActionForward forward = save(mapping, form, request, response);

        // Return the forward if it isn't a success.
        if (!forward.equals(mapping.findForward(SUCCESS))) {
            return forward;
        }
        // Handler to the Intact User.
        EditUserI user = getIntactUser(request);

        // The current view.
        AbstractEditViewBean view = user.getView();

        // Get the original object for clone.
        AnnotatedObjectImpl orig = (AnnotatedObjectImpl) view.getAnnotatedObject();
        // Clone it.
        AnnotatedObjectImpl copy = (AnnotatedObjectImpl) orig.clone();

        // Release the lock first.
        getLockManager().release(view.getAc());

        // Now, set the view as the cloned object.
        user.setClonedView(copy, orig.getAc());

        // Redisplay the cloned object.
        return mapping.findForward(RELOAD);
    }

    /**
     * Action for adding a new annotation.
     *
     * @param mapping the <code>ActionMapping</code> used to select this instance
     * @param form the optional <code>ActionForm</code> bean for this request
     * (if any).
     * @param request the HTTP request we are processing
     * @param response the HTTP response we are creating
     * @return failure mapping for any errors in cancelling the CV object; search
     *         mapping if the cancel is successful and the previous search has only one
     *         result; results mapping if the cancel is successful and the previous
     *         search has produced multiple results.
     * @throws Exception for any uncaught errors.
     */
    public ActionForward addAnnot(ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response)
            throws Exception {

        // Handler to the Intact User.
        EditUserI user = getIntactUser(request);

        ResourceBundle rb = ResourceBundle.getBundle("uk.ac.ebi.intact.application.editor.MessageResources");

        // The current form.
        EditorFormI editorForm = (EditorFormI) form;

        // The dispatch value holds the button label.
        String dispatch = editorForm.getDispatch();
        String acceptButtonLabel = rb.getString("exp.button.accept");
        String reviewButtonLabel = rb.getString("exp.button.review");
        if(dispatch.equals(reviewButtonLabel) || dispatch.equals(acceptButtonLabel)) {
            acceptOrReview(mapping, form, request, response, dispatch, acceptButtonLabel);
        } else{

            // The bean to extract the values.
            CommentBean cb = editorForm.getNewAnnotation();

            // The current view.
            AbstractEditViewBean view = user.getView();

            String description = cb.getDescription();
            log.debug("The description is " + cb.getDescription());

            for (int i=0; i<description.length(); i++ ){
                char c = description.charAt(i);
               
                if (!(c >= 32  && c <= 126)){

//                if(!Character.isUnicodeIdentifierPart(c)){
                    ActionMessages errors = new ActionMessages();
                    errors.add("char.not.allowed", new ActionMessage("error.annotation.char.not.allowed", formatErrorMessage(description,i)));
                    saveErrors(request, errors);

                    // Set the anchor
                    setAnchor(request, editorForm);
                    // Display the error in the edit page.
                    return mapping.getInputForward();
                }
            }

            // Does this bean exist in the current view?
            if (view.annotationExists(cb)) {
                // The errors to display.
                ActionMessages errors = new ActionMessages();
                errors.add("new.annotation", new ActionMessage("error.annotation.exists"));
                saveErrors(request, errors);

                // Set the anchor
                setAnchor(request, editorForm);
                // Display the error in the edit page.
                return mapping.getInputForward();
            }

            // Add the bean to the view.
            view.addAnnotation( (CommentBean) cb.clone() );

            // Set anchor if necessary.
            setAnchor(request, editorForm);

            return mapping.getInputForward();
        }
        //Annotation BUG
        //editorForm.resetDispatch();

        setAnchor(request, editorForm);
        return mapping.getInputForward();
    }

    String formatErrorMessage(String msg, int char2hilightPosition){
    // Implements super's abstract methods.

        String firstPart = msg.substring(0,char2hilightPosition);
        String char2hilight = " ["+ Character.toString(msg.charAt(char2hilightPosition)) +  "] ";
        String secondPart = msg.substring(char2hilightPosition+1, msg.length());
        return " The annotation decription : " + firstPart + char2hilight + secondPart + " Allowed characters :"+ allowedChar;
    }

    /**
     * Action for adding a new xref.
     *
     * @param mapping the <code>ActionMapping</code> used to select this instance
     * @param form the optional <code>ActionForm</code> bean for this request
     * (if any).
     * @param request the HTTP request we are processing
     * @param response the HTTP response we are creating
     * @return failure mapping for any errors in cancelling the CV object; search
     *         mapping if the cancel is successful and the previous search has only one
     *         result; results mapping if the cancel is successful and the previous
     *         search has produced multiple results.
     * @throws Exception for any uncaught errors.
     */
    public ActionForward addXref(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // Handler to the EditUserI.
        EditUserI user = getIntactUser(request);

        // The current form.
        EditorFormI editorForm = (EditorFormI) form;

        // The bean to extract the values.
        XreferenceBean xb = editorForm.getNewXref();

        // The current view.
        AbstractEditViewBean view = user.getView();

        // We test that the xref has a valid primaryId, has the hasValidPrimaryId is already implemented in
        // uk.ac.ebi.intact.model.Xref, out of the XreferenceBean we create an xref and use its method hasValidPrimaryId
        // If if return false we display the error.
        Xref xref = createXref(xb, view);
        if(!xref.hasValidPrimaryId()){
            String regExp = getIdRegularExpression(xref.getCvDatabase());
            ActionMessages errors = new ActionMessages();
            errors.add("new.xref", new ActionMessage("error.xref.pid.not.valid",regExp));
            saveErrors(request, errors);

            // Set the anchor
            setAnchor(request, editorForm);
            // Display the error in the edit page.
            return mapping.getInputForward();
        }

        // Does this bean exist in the current view?
        if (view.xrefExists(xb)) {
            ActionMessages errors = new ActionMessages();
            errors.add("new.xref", new ActionMessage("error.xref.exists"));
            saveErrors(request, errors);

            // Set the anchor
            setAnchor(request, editorForm);
            // Display the error in the edit page.
            return mapping.getInputForward();
        }
        // For Go database, set values from the Go server.
        if (xb.getDatabase().equals("go")) {
            ActionMessages errors = xb.setFromGoServer(user.getGoProxy());
            // Non null error indicates errors.
            if (errors != null) {
                saveErrors(request, errors);
                // Set the anchor
                setAnchor(request, editorForm);
                // Display the errors in the input page.
                return mapping.getInputForward();
            }
            // reset the xref value as the xb value have changed.  
            xref.setSecondaryId(xb.getSecondaryId());
            xref.setDbRelease(xb.getReleaseNumber());
        }


        // Add the bean to the view.
        view.addXref((XreferenceBean) xb.clone());

        // Set anchor if necessary.
        setAnchor(request, editorForm);

        return mapping.getInputForward();
    }

    private String getIdRegularExpression(CvDatabase cv)  {
        Collection<Annotation> annotations = cv.getAnnotations();
        for(Annotation annotation : annotations){
            CvObjectXref cvTopicXref = CvObjectUtils.getPsiMiIdentityXref(annotation.getCvTopic());
            if(cvTopicXref != null && CvTopic.XREF_VALIDATION_REGEXP_MI_REF.equals(cvTopicXref.getPrimaryId())){
                return annotation.getAnnotationText();
            }
        }
        return null;
    }

    /**
     * Given a XreferenceBean it creates a Xref and returns it
     * @param xb
     * @return
     * @throws IntactException
     */

    public Xref createXref(XreferenceBean xb, AbstractEditViewBean view) throws IntactException {
        Institution institution = IntactContext.getCurrentInstance().getInstitution();//new Institution("ebi");
        CvObjectDao cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao();
        CvDatabase cvDatabase = (CvDatabase) cvObjectDao.getByShortLabel(xb.getDatabase());//CvDatabase) helper.getObjectByLabel(CvDatabase.class , xb.getDatabase()));
        CvXrefQualifier cvXrefQualifier = (CvXrefQualifier) cvObjectDao.getByShortLabel(xb.getQualifier());//new CvXrefQualifier(institution, xb.getQualifier());
        Xref xref;
        if( view instanceof BioSourceViewBean ){
            xref = new BioSourceXref(institution,cvDatabase,xb.getPrimaryId(),xb.getSecondaryId(),xb.getReleaseNumber(),cvXrefQualifier);
        } else if ( view instanceof CvViewBean ){
            xref = new CvObjectXref(institution,cvDatabase,xb.getPrimaryId(),xb.getSecondaryId(),xb.getReleaseNumber(),cvXrefQualifier);
        } else if ( view instanceof ExperimentViewBean ){
            xref = new ExperimentXref(institution,cvDatabase,xb.getPrimaryId(),xb.getSecondaryId(),xb.getReleaseNumber(),cvXrefQualifier);
        } else if ( view instanceof FeatureViewBean ){
            xref = new FeatureXref(institution,cvDatabase,xb.getPrimaryId(),xb.getSecondaryId(),xb.getReleaseNumber(),cvXrefQualifier);
        } else if ( view instanceof InteractionViewBean ||
                    view instanceof NucleicAcidViewBean ||
                    view instanceof ProteinViewBean ||
                    view instanceof SequenceViewBean ||
                    view instanceof SmallMoleculeViewBean ){
            xref = new InteractorXref(institution,cvDatabase,xb.getPrimaryId(),xb.getSecondaryId(),xb.getReleaseNumber(),cvXrefQualifier);
        }
        else{
            throw new IntactException("Not known AbstractEditViewBean sub-classes : " + view.getClass().getName());
        }
        return xref;
    }

    /**
     * Handles both submit/save actions.
     *
     * @param mapping the <code>ActionMapping</code> used to select this instance
     * @param form the optional <code>ActionForm</code> bean for this request
     * (if any).
     * @param request the HTTP request we are processing
     * @param submit true for submit action.
     * @return failure mapping for any errors in updating the CV object; search
     *         mapping if the update is successful and the previous search has only one
     *         result; results mapping if the update is successful and the previous
     *         search has produced multiple results.
     * @throws Exception for any uncaught errors.
     */
    protected ActionForward submitForm(ActionMapping mapping,
                                       ActionForm form,
                                       HttpServletRequest request,
                                       boolean submit)
            throws Exception {
        // Handler to the Intact User.
        EditUserI user = getIntactUser(request);

        // The current view.
        AbstractEditViewBean view = user.getView();

        // The current form.
        EditorFormI editForm = (EditorFormI) form;

        // The new short label.
        String newFormLabel = getShortLabel(user, view.getEditClass(),
                editForm.getShortLabel());

        // Update the view and the form.
        if (!view.getShortLabel().equals(newFormLabel)) {
            view.setShortLabel(newFormLabel);
            editForm.setShortLabel(newFormLabel);
        }
        // Runs the editor sanity checking
        view.sanityCheck();

        try {
            // Persist my current state (this takes care of updating the wrapped
            // object with values from the form).
            view.persist(user);

            // Any other objects to persist in their own transaction.
            view.persistOthers(user);
            // We reset the view with the saved interaction so that the ac are reset as well.
            // !!!!BE CAREFULL !!!! when you reset the view all the isSelected boolean are reset to false, so you won't know anymore
            // if something has been selected.
            AnnotatedObject annotatedObject = view.getAnnotatedObject();
            view.reset(annotatedObject);
        }
        catch (IntactException ie) {
            // Log the stack trace.
            log.error("Exception trying to persist the view ", ie);
            // Error with updating.
            ActionMessages errors = new ActionMessages();
            // The error message.
            String msg = ie.getRootCause() != null ? ie.getRootCause().getMessage()
                    : "Update failure, root cause is not availabe";
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.update", msg));
            saveErrors(request, errors);
            return mapping.findForward(FAILURE);
        }
        finally {
            // Release the lock only for submit.
            if (submit) {
                getLockManager().release(view.getAc());
            }
            // Clear any left overs from previous transaction.
            view.clearTransactions();
        }
        // All are up todate except for AC which still can be null for a new object.
        editForm.setAc(view.getAcLink());

        // We can't use mapping.getInputForward here as this return value
        // is used by subclasses (they need to distinguish between a success or
        // a failure such as duplicate short labels).
        return mapping.findForward(SUCCESS);
    }

    /**
     * Returns a unique short label.
     * @param user to get the next shortlabel available and to check whether given
     * short label is unique or not.
     * @param editClass the current editing class.
     * @param formlabel the short label from the form.
     * @return the new short label if <code>formlabel</code> is not unique or else
     * it is as same as <code>formlabel</code>
     * @throws IntactException for errors in searching the database.
     */
    protected String getShortLabel(EditUserI user, Class editClass,
                                   String formlabel) throws IntactException {
        // No need to get the next available short label if it is unique.
        if (!user.shortLabelExists(formlabel)) {
            return formlabel;
        }
        // Try to get the next available short label by adding -x if it doesn't
        // end with -
//        formlabel += formlabel.endsWith("-") ? "x" : "-x";

        return user.getNextAvailableShortLabel(editClass, formlabel);
    }

    public ActionForward acceptOrReview(ActionMapping mapping,
                                        ActionForm form,
                                        HttpServletRequest request,
                                        HttpServletResponse response,
                                        String dispatch,
                                        String acceptButtonLabel) throws SessionExpiredException, IntactException {

        EditUserI user = getIntactUser(request);
        EditorFormI editorForm = (EditorFormI)form;
        ExperimentActionForm expForm=(ExperimentActionForm)form;
        String userName = user.getUserName();

        // Search for the userstamp corresponding to the experiment (name of the curator who has
        // curated the experiment)
        String shortLabel = expForm.getShortLabel();

        CommentBean cb1 = expForm.getNewAnnotation();


        ExperimentDao experimentDao = DaoProvider.getDaoFactory().getExperimentDao();
        Experiment experiment = experimentDao.getByShortLabel(shortLabel);
        if (experiment == null){
            log.error("Experiment is null,  we won't be abble to get the creator.");
        }

        String creator = experiment.getCreator();

        // If the user who is trying to Accept or Review the experiment is the user who has curated the experiment
        // display the error : "You can not Accept or Review your own curated experiment"d
        if(userName.toUpperCase().trim().equals(creator.toUpperCase())){
            ActionMessages errors = new ActionMessages();
            errors.add("new.annotation", new ActionMessage("error.curator.accepter"));
            saveErrors(request, errors);
            // Set the anchor
            setAnchor(request, editorForm);
            // Display the error in the edit page.
            return mapping.getInputForward();
        }else{
            Calendar cal = new GregorianCalendar();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH)+1;
            int day = cal.get(Calendar.DAY_OF_MONTH);

            CvTopic cvTopic;
            String description = new String();
            String date = year + "-" + DateToolbox.getMonth(month) + "-" + day;
            CvObjectDao<CvTopic> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvTopic.class);

            if(dispatch.equals(acceptButtonLabel)){ // if the button press is "Accept"
                description = description + "Accepted " + date + " by " + userName.toUpperCase() + ".";
                // The topic for new annotation.
                cvTopic = cvObjectDao.getByShortLabel(CvTopic.ACCEPTED);
            }else{ // if the button press is "Review"
                description = description + "Rejected " + date + " by " + userName.toUpperCase() + ".";

                // The topic for new annotation.
                cvTopic = cvObjectDao.getByShortLabel(CvTopic.TO_BE_REVIEWED);
            }
            if (cb1 != null){
                description = description + " " + cb1.getDescription();
            }
            Annotation annotation=new Annotation(IntactContext.getCurrentInstance().getConfig().getInstitution(), cvTopic, description);
            CommentBean cb = new CommentBean(annotation);
            AbstractEditViewBean view = getIntactUser(request).getView();
            view.addAnnotation(cb);
            view.copyPropertiesTo(editorForm);
            expForm.clearNewBeans();
            expForm.resetDispatch();
        }
        setAnchor(request, editorForm);

        return mapping.getInputForward();
    }


}