/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.editor.struts.security;

import org.apache.struts.action.ActionServlet;
import servletunit.struts.MockStrutsTestCase;
import uk.ac.ebi.intact.application.editor.LoginPropertiesGetter;
import uk.ac.ebi.intact.application.editor.event.EventListener;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.context.IntactContext;


/**
 * This struts unit test case is done to test the login page of the Editor.
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id$
 */
public class LoginActionTest extends MockStrutsTestCase {

    public void setUp() throws Exception {
        super.setUp();

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
    }

    public void tearDown() throws Exception {
        super.tearDown();
        IntactContext.getCurrentInstance().getDataContext().commitTransaction();
    }

    public LoginActionTest(String testName) {
        super(testName); }

    /**
     * This method test that using a valid username and a valid password, you can login.
     */
    public void testSuccessfulLogin() {
        LoginPropertiesGetter loginProperties = new LoginPropertiesGetter();

        // Set the request path  to /login instructing the ActionServlet to used a particual ActionMapping.
        setRequestPathInfo("/login");
        // set the username property to x
        addRequestParameter("username", loginProperties.getName());
        // set the username property to y
        addRequestParameter("password", loginProperties.getPassword());
        // In order to login you need to access the event listener, so we add it to the ServletContext so that the login
        // system can access it later on.
        ActionServlet actionServlet =  getActionServlet();
        actionServlet.getServletContext().setAttribute(EditorConstants.EVENT_LISTENER, EventListener.getInstance() );
        setActionServlet(actionServlet);
        // Perform the LoginAction
        actionPerform();
        // Veryfy that no Error is sent, if any error sent, the test won't pass.
        verifyNoActionErrors();
        // Veryfy that no ActionMessage is sent, if any error sent, the test won't pass.
         verifyNoActionMessages();
        // Veryfy that the foward name is "success" and that the tiles-definition name is search.layout.
        verifyTilesForward("success","search.layout");
    }

    /**
     * This method test that using an invalid username and an invalid password, you can't connect.
     */
     public void testFailingLogin() {
        // Set the request path  to /login instructing the ActionServlet to used a particual ActionMapping.
        setRequestPathInfo("/login");
        // set the username property to "guinea" and the password to "papouasie". Having a user called guinea with a
        // password beeing papouasie, would be terribly unlucky. :-)
        addRequestParameter("username", "guinea");
        addRequestParameter("password", "papouasie");
        // In order to login you need to access the event listener, so we add it to the ServletContext so that the login
        // system can access it later on.
        ActionServlet actionServlet =  getActionServlet();
        actionServlet.getServletContext().setAttribute(EditorConstants.EVENT_LISTENER, EventListener.getInstance() );
        setActionServlet(actionServlet);
        // Perform the LoginAction
        actionPerform();
        // Verifies that the ActionServlet controller forwarded to the defined input Tiles "login.error.layout"
        verifyInputTilesForward("login.error.layout");
        // Verifies if the ActionServlet controller sent the error message "error.invalid.user"
        String[] errors = {"error.invalid.user"};
        verifyActionErrors(errors);
    }

}
