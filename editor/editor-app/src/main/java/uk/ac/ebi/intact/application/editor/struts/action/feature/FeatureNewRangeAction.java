/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.feature;

import org.apache.struts.action.*;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureActionForm;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.feature.RangeBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This action is invoked when the user wants to add a new range to a feature.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/feature/range/new"
 *      name="featureForm"
 *      input="edit.layout"
 *      scope="session"
 */
public class FeatureNewRangeAction extends AbstractEditorAction {

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // The form to extract values.
        FeatureActionForm featureForm = ((FeatureActionForm) form);

        // The current view of the edit session.
        FeatureViewBean view = (FeatureViewBean) getIntactUser(request).getView();

        // The bean to extract values.
        RangeBean rbnew = featureForm.getNewRange();

        // Does the range exist in the current ranges?
        if (view.rangeExists(rbnew)) {
            ActionMessages errors = new ActionMessages();
            errors.add("new.range",
                    new ActionMessage("error.feature.range.exists"));
            saveErrors(request, errors);
            // Incorrect values for ranges. Display the error in the input page.
            return mapping.getInputForward();
        }

        //Make sure that if "toRange" is an int, it does not have a negativ value
        RangeBean newRangeBean = featureForm.getNewRange();
        try{
            int toRange = Integer.parseInt(newRangeBean.getToRange());
            if(toRange<0){
                ActionMessages errors = new ActionMessages();
                errors.add("new.range",new ActionMessage("error.feature.range.toRange.isInt.isNegativ"));
                saveErrors(request, errors);
                return mapping.getInputForward();
             }
        }
        catch(NumberFormatException e){
            //Do nothing because the toRange can be a String as well
        }

        //Make sure that if "fromRange" is an int, it does not have a negativ value
        try{
            int fromRange = Integer.parseInt(newRangeBean.getFromRange());
            if(fromRange<0){
                ActionMessages errors = new ActionMessages();
                errors.add("new.range",new ActionMessage("error.feature.range.toRange.isInt.isNegativ"));
                saveErrors(request, errors);
                return mapping.getInputForward();
             }
        }
        catch(NumberFormatException e){
            //Do nothing because the toRange can be a String as well
        }


        // Add a copy of the new range
        view.addRange(new RangeBean(rbnew.getFromRange(), rbnew.getToRange(),
                rbnew.getLink()));

        // Back to the input form.
        return mapping.getInputForward();
    }
}