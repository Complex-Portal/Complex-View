/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.predict.struts.framework;

import org.apache.struts.action.ActionServlet;
import uk.ac.ebi.intact.application.predict.business.PredictService;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * The predict action servlet class. This class is
 * responsible for initializing application wide resources.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class PredictActionServlet extends ActionServlet {

    public void init() throws ServletException {
        // Make sure to call super's init().
        super.init();

        // Save the context to avoid repeat calls.
        ServletContext ctx = getServletContext();

        // Create an instance of the service object.
        PredictService service = new PredictService();

        // Make them accessible for any servlets within the server.
        ctx.setAttribute(PredictConstants.SERVICE, service);
    }
}
