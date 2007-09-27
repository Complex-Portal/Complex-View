/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.editor.struts.action.experiment;

import org.apache.struts.action.ActionServlet;
import servletunit.struts.MockStrutsTestCase;
import uk.ac.ebi.intact.application.editor.LoginPropertiesGetter;
import uk.ac.ebi.intact.application.editor.business.EditorService;
import uk.ac.ebi.intact.application.editor.event.EventListener;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.util.LockManager;
import uk.ac.ebi.intact.context.IntactContext;

/**
 * TODO comment it.
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentDispatchActionTest extends MockStrutsTestCase {

    public void setUp() throws Exception {
        super.setUp();

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
    }

    public void tearDown() throws Exception {
        super.tearDown();
        IntactContext.getCurrentInstance().getDataContext().commitTransaction();
    }

    public ExperimentDispatchActionTest(String testName) {
        super(testName);
    }

    public void testSubmitAction(){
        ActionServlet actionServlet = getActionServletFromCreate("Experiment");
        setActionServlet(actionServlet);

        addRequestParameter("shortLabel", "unit-test");
        addRequestParameter("fullName", "unit test shortlabel");
        addRequestParameter("organism", "arath");
        addRequestParameter("inter","bret");
        addRequestParameter("ident","elisa");
        addRequestParameter("dispatch", "Submit");
        setRequestPathInfo("/expDispatch");
//        setRequestPathInfo("/exp/submit");

        actionPerform();
        // Verifies if the ActionServlet controller used the "result" forward.
        verifyForward("submit");
//        verifyForward("success");
        // Verifies if the ActionServlet controller used the "/do/showResults" forward path.
        verifyForwardPath("/do/exp/submit");
//        verifyTilesForward("success","edit.layout");
        verifyNoActionErrors();
        // Veryfy that no ActionMessage is sent, if any error sent, the test won't pass.
        verifyNoActionMessages();
        setRequestPathInfo("/exp/submit");
        addRequestParameter("dispatch","Submit");

//        setRequestPathInfo("/exp/submit");
        actionPerform();

        verifyForward("result");
//        actionPerform();


    }

    public ActionServlet getActionServletFromCreate(String topic){
        // Login as this is need to do a search.
        setActionServlet(getActionServletFromLogin());
        // get the ActionServlet
        ActionServlet actionServlet = getActionServlet();
        //Set the EditorService as an attribute of the servletContext as this is needed for the search action.
        EditorService service = EditorService.getInstance();
        actionServlet.getServletContext().setAttribute(EditorConstants.EDITOR_SERVICE, service);
        //Set the LockManager as an attribute of the servletContext as this is needed for the search action.
        actionServlet.getServletContext().setAttribute(EditorConstants.LOCK_MGR, LockManager.getInstance());
        // set the action servlet.
        setActionServlet(actionServlet);
        // Set the request path  to /sidebar instructing the ActionServlet to used a particual ActionMapping.
        setRequestPathInfo("/sidebar");
        addRequestParameter("topic",topic);
        // set the topic to CvTopic, the searchString to accepted and the dispatch to search.
        addRequestParameter("dispatch", "Create");
        // Perform the SidebarDispatchAction
        actionPerform();


        return getActionServlet();
    }

    private ActionServlet getActionServletFromLogin() {
        LoginPropertiesGetter loginProperties = new LoginPropertiesGetter();
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
        return getActionServlet();
    }

}
