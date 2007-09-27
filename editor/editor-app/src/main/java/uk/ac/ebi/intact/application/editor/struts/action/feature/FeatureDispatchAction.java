/*
 Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
 All rights reserved. Please see the file LICENSE
 in the root directory of this distribution.
 */

package uk.ac.ebi.intact.application.editor.struts.action.feature;

import org.apache.struts.action.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.business.EditorService;
import uk.ac.ebi.intact.application.editor.struts.action.CommonDispatchAction;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureActionForm;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.InteractionViewBean;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.FeatureDao;
import uk.ac.ebi.intact.persistence.dao.RangeDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

/**
 * This action class extends from common dispatch action class to override
 * submit action (Submit button) for Feature editor. Other submit actions such
 * as Save & Continue are not affected.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/feature/submit"
 *      name="featureForm"
 *      input="edit.layout"
 *      scope="session"
 *      parameter="dispatch"
 *
 * @struts.action-forward
 *      name="success"
 *      path="edit.layout"
 *
 * @struts.action-forward
 *      name="interaction"
 *      path="/do/int/fill/form"
 */
public class FeatureDispatchAction extends CommonDispatchAction {

    protected static Log log = LogFactory.getLog(FeatureDispatchAction.class);

    /**
     * Overrides the super's submit action to handle Feature editor specific
     * behaviour.
     *
     * @param mapping the <code>ActionMapping</code> used to select this
     * instance
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
    public ActionForward submit(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        log.debug("\n\n\n\n\nFeatureDispatchAction.submit");
        EditUserI user = getIntactUser(request);

        // The current view.
        FeatureViewBean view = ((FeatureViewBean) user.getView());

        // Stores mapping forwards.
        ActionForward forward;

        // The list of features (only valid in mutation mode).
        List features = null;

        // Check to see if this sumbitted Feature has mutations.
        if (view.isInMutationMode()) {
            try {
                // Persist mutations
                features = persistMutations(user);
                // It was a success.
                forward = mapping.findForward(SUCCESS);
            }
            catch (IntactException ie) {
                // Log the stack trace.
                LOGGER.error("Problem in persisting mutations : ",ie);
                // Error with updating.
                ActionMessages errors = new ActionMessages();
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.update",
                        ie.getRootCause().getMessage()));
                saveErrors(request, errors);
                forward = mapping.findForward(FAILURE);
            }
        }
        else {
            // Submit the form. Analyze the forward path.
            forward = submitForm(mapping, form, request, true);
        }
        // Return the forward for any non success.
        if (!forward.equals(mapping.findForward(SUCCESS))) {
            return forward;
        }
        // Set the topic.
        user.setSelectedTopic(EditorService.getTopic(Interaction.class));

        // The parent view of the current view.
        InteractionViewBean intView = (InteractionViewBean) user.popPreviousView();

        // Update individual Features if in mutation mode.
        if (view.isInMutationMode()) {
            // features variable can never be null when in mutation mode. At least
            // it should contain a single entry
            for (Iterator iter = features.iterator(); iter.hasNext(); ) {
                intView.saveFeature((Feature) iter.next());
            }
        }
        else {
            // Update the feature in the interaction view (only for non mutation mode)
            intView.saveFeature(view.getAnnotatedObject());
        }
        // Turn off mutation mode (or else you will get mutation screen again)
        view.turnOffMutationMode();

        // The interaction we are going back to.
        user.setView(intView);

        // Return to the interaction editor.
        return mapping.findForward(INT);
    }

    // Override the super method to allow duplicate short labels for feature.
    protected String getShortLabel(EditUserI user, Class editClass,
                                   String formlabel) throws IntactException {
        return formlabel;
    }

    private List persistMutations(EditUserI user) throws IntactException {
        FeatureDao featureDao = DaoProvider.getDaoFactory().getFeatureDao();
        RangeDao rangeDao = DaoProvider.getDaoFactory().getRangeDao();

        // The list of features to return.
        List features = new ArrayList();

        // The current view.
        FeatureViewBean view = ((FeatureViewBean) user.getView());

        // The owner for new Features.
        Institution owner = IntactContext.getCurrentInstance().getConfig().getInstitution();

        // CvFeature types.
        CvObjectDao<CvFeatureType> cvFeatureDao = DaoProvider.getDaoFactory().getCvObjectDao(CvFeatureType.class) ;
        CvFeatureType featureType = cvFeatureDao.getByShortLabel(view.getCvFeatureType());

        // CvFeatureIdent is optional.
        CvObjectDao<CvFeatureIdentification> cvFeatureIdentificationDao = DaoProvider.getDaoFactory().getCvObjectDao(CvFeatureIdentification.class);
        CvFeatureIdentification featureIdent = null;
        if (view.getCvFeatureIdentification() != null) {
            featureIdent = cvFeatureIdentificationDao.getByShortLabel(view.getCvFeatureIdentification());
        }
        StringTokenizer stk = new StringTokenizer(view.getFullName(),
                getService().getResource("mutation.feature.sep"));

        // The mutation Feature to create.
        Feature feature;

        // The sequence to set in Ranges.
        String sequence = ((Protein) view.getComponent().getInteractor()).getSequence();

        do {
            // Contains info for a single feature mutation
            String token = stk.nextToken();

            // The next possible label for the new Feature.
            String nextSL = computeFeatureShortLabel(token);
            feature = new Feature(owner, nextSL, view.getComponent(), featureType);
            if (featureIdent != null) {
                feature.setCvFeatureIdentification(featureIdent);
            }
            // Create a Feature in a separate transaction.
            try {

                featureDao.persist(feature);
            }
            catch (IntactException ie) {
                // Log the stack trace.
                LOGGER.error("", ie);
                throw ie;
            }
            // Feature is persisted, add it to the list.
            features.add(feature);

            try {
                for (Iterator iter = rangesToCreate(token, owner, sequence); iter.hasNext(); ) {
                    Range range = (Range) iter.next();
                    rangeDao.persist(range);
                    feature.addRange(range);
                }
                featureDao.saveOrUpdate(feature);
            }
            catch (IntactException ie) {
                // Log the stack trace.
                LOGGER.error("", ie);
//                // Rethrow it again for logging the exception.
                throw ie;
            }
        }
        while (stk.hasMoreTokens());
        return features;
    }

    /**
     * Computes the short label for a Feature
     * @param token this token may consist of multiple ranges
     * @return the computed short label. Each range is joined with '-' as long
     * as the string of the text is less than or equals tm max chars allowed for
     * a short label.
     */
    private String computeFeatureShortLabel(String token) {
        // The buffer to construct the name
        StringBuffer sb = new StringBuffer();

        // The range separator.
        String sep = getService().getResource("mutation.range.sep");

        StringTokenizer stk1 = new StringTokenizer(token, sep);
        while (stk1.hasMoreTokens()) {
            String range = stk1.nextToken().trim();
            if (sb.length() == 0) {
                sb.append(range);
                continue;
            }
            if ((sb.length() + range.length() + 1) <= AnnotatedObjectImpl.MAX_SHORT_LABEL_LEN) {
                sb.append("-");
                sb.append(range);
            }
            else {
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Returns an iterator which contains Ranges to create.
     * @param str the string consists of ranges
     * @param owner the owner for a Range
     * @param sequence the sequence for the range
     * @return an iterator consists of Ranges to create
     */
    private Iterator rangesToCreate(String str, Institution owner, String sequence) {
        // The ranges to return.
        List ranges = new ArrayList();

        // The range separator.
        String sep = getService().getResource("mutation.range.sep");

        // Break into tokens.
        StringTokenizer stk = new StringTokenizer(str, sep);

        do {
            // Extract the range value to construct a range object.
            int rangeValue = extractRange(stk.nextToken());
            ranges.add(new Range(owner, rangeValue, rangeValue, sequence));
        }
        while (stk.hasMoreTokens());
        return ranges.iterator();
    }

    /**
     * Extracts a range value from a string.
     * @param element the element to extract the range
     * @return an int value extracted from <code>element</code>
     */
    private int extractRange(String element) {
        Matcher matcher = FeatureActionForm.MUT_ITEM_REGX.matcher(element.trim());
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(2));
        }
        return -1;
    }
}