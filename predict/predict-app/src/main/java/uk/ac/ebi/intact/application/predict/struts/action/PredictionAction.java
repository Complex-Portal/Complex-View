package uk.ac.ebi.intact.application.predict.struts.action;

/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import uk.ac.ebi.intact.application.predict.business.PredictUser;
import uk.ac.ebi.intact.application.predict.struts.framework.AbstractPredictAction;
import uk.ac.ebi.intact.application.predict.struts.framework.PredictConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Implementation of <strong>Action</strong> that validates a user access.
 *
 * @author Konrad Paszkiewicz (konrad.paszkiewicz@ic.ac.uk)
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public final class PredictionAction extends AbstractPredictAction {

    /**
     *Collects Top 50 Pay-As-You-Go protein results from database
     *for each species and places them in results bean
     *
     *Species info is collected in species bean
     *
     *These are called from the success jsp page
     *
     *
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // The dyna form.
        DynaActionForm dynaform = (DynaActionForm) form;

        // Retrieve the specie chosen by the user.
        String specie = (String) dynaform.get("specie");

        // Access the predict user; don't create a new session.
        PredictUser user = getPredictUser(request);

        // Set the current specie.
        user.setSpecie(specie);

        // Save the results in the request for the JSP to access.
        List results = user.getDbInfo(specie);
        request.setAttribute(PredictConstants.PREDICTION, results);

        // Forward control to the specified success URI
        return (mapping.findForward(SUCCESS));
    }
}




