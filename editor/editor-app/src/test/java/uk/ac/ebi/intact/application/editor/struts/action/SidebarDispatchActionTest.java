/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.editor.struts.action;

import org.apache.struts.action.ActionServlet;
import servletunit.struts.MockStrutsTestCase;
import uk.ac.ebi.intact.application.editor.LoginPropertiesGetter;
import uk.ac.ebi.intact.application.editor.business.EditorService;
import uk.ac.ebi.intact.application.editor.event.EventListener;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.util.LockManager;
import uk.ac.ebi.intact.context.IntactContext;

/**
 * This class permit to test the SidebarDispatchAction.
 * This SidebarDispatch action handles the actions occuring on page which appears just after the login. On this page,
 * you can decide to search or to create an object.
 *
 * S E A R C H
 * ------------
 * Giving a type of Object and a search String and pressing the "search" button this action searches for it and :
 *      edit the object if only 1 is found
 *      display an error message if nothing is found
 *      display an error if too many potential corresponding objects are found (more then 50 "search-max" value in the
 *          EditorRessources.properties file.
 *      display a list of all the potential corresponding object if not too many, then the user can choose the one he
 *          wants to edit.
 * C R E A T E
 * -----------
 * Giving a type of Object and pressing the "Create" button this action will :
 *      Open a blank Editor page corresponding to this object.
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id$
 */
public class SidebarDispatchActionTest extends MockStrutsTestCase {

    public void setUp() throws Exception {
        super.setUp();

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
    }

    public void tearDown() throws Exception {
        super.tearDown();

        IntactContext.getCurrentInstance().getDataContext().commitTransaction();

    }

    public SidebarDispatchActionTest(String testName) {
        super(testName);

    }

    /**
     * Given a topic (ex : CvTopic) and a searchString (ex : accepted), it check that the SidebarDispatchAction find it,
     * and then displays it. This will, work only if the database contains -in the exemple- one and only one CvTopic
     * having "accepted" as shortlabel or ac (obviously here there's more chance that "accpeted" will be a shortlabel ;-).
     *
     */
    public void searchAndEdit(String topic, String searchString){
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
        // set the topic to CvTopic, the searchString to accepted and the dispatch to search.
        addRequestParameter("topic", topic);
        addRequestParameter("searchString",searchString);
        addRequestParameter("dispatch", "Search");
        // Perform the SidebarDispatchAction
        actionPerform();
        // Veryfy that no Error is sent, if any error sent, the test won't pass.
        verifyNoActionErrors();
        // Veryfy that no ActionMessage is sent, if any error sent, the test won't pass.
        verifyNoActionMessages();
        // Verifies if the ActionServlet controller used the "secure" forward.
        verifyForward("secure");
        // Verifies if the ActionServlet controller used the "/do/secire/edit" forward path.
        verifyForwardPath("/do/secure/edit");
    }

    /**
     * Check that the experiment EBI-476945 is found and displayed. (Work only if  EBI-476945 experiment is present in
     * the database)
     */
    public void testSearchAndDisplayExperiment(){
        searchAndEdit("Experiment", "EBI-476945");
    }
    /**
     * Check that the Interaction EBI-754756 is found and displayed. (Work only if  EBI-476945 Interaction is present in
     * the database)
     */
    public void testSearchAndDisplayInteraction(){
        searchAndEdit("Interaction", "EBI-754756");
    }
    /**
     * Check that the BioSource EBI-140 is found and displayed. (Work only if  EBI-140 BioSource is present in
     * the database)
     */
    public void testSearchAndDisplayBioSource(){
        searchAndEdit("BioSource", "EBI-140");
    }
    /**
     * Check that the CvAliasType EBI-49936 is found and displayed. (Work only if  EBI-140 BioSource is present in
     * the database)
     */
    public void testSearchAndDisplayCvAliasType(){
        searchAndEdit("CvAliasType","EBI-49936");
    }
    /**
     * Check that the CvCellType EBI-300790 is found and displayed. (Work only if  EBI-300790 CvCellType is present in
     * the database)
     */
    public void testSearchAndDisplayCvCellType(){
        searchAndEdit("CvCellType","EBI-300790");
    }
    /**
     * Check that the CvComponentRole EBI-49 is found and displayed. (Work only if  EBI-49 CvComponentRole is present in
     * the database)
     */
    public void testSearchAndDisplayCvComponentRole(){
        searchAndEdit("CvComponentRole","EBI-49");
    }
    /**
     * Check that the CvDatabase EBI-73984 is found and displayed. (Work only if  9 CvDatabase EBI-73984 is present in
     * the database)
     */
    public void testSearchAndDisplayCvDatabase(){
        searchAndEdit("CvDatabase", "EBI-73984");
    }
    /**
     * Check that the CvFuzzyType EBI-448301 is found and displayed. (Work only if  EBI-448301 CvComponentRole is present in
     * the database)
     */
    public void testSearchAndDisplayCvFuzzyType(){
        searchAndEdit("CvFuzzyType","EBI-448301");
    }
    /**
     * Check that the CvTissue EBI-300796 is found and displayed. (Work only if  EBI-300796 CvTissue is present in
     * the database)
     */
    public void testSearchAndDisplayCvTissue(){
        searchAndEdit("CvTissue", "EBI-300796");
    }
    /**
     * Check that the CvTopic accepted is found and displayed. (Work only if  CvTopic accepted is present in
     * the database)
     */
    public void testSearchAndDisplayCvTopic(){
        searchAndEdit("CvTopic", "accepted");
    }
    /**
     * Check that the CvXrefQualifier EBI-28 is found and displayed. (Work only if  CvXrefQualifier EBI-28 is present in
     * the database)
     */
    public void testSearchAndDisplayCvQualifier(){
        searchAndEdit("CvXrefQualifier", "EBI-28");
    }
    /**
     * Check that the NucleicAcid EBI-936571 is found and displayed. (Work only if  NucleicAcid EBI-936571 is present in
     * the database)
     */
    public void testSearchAndDisplayNucleicAcid(){
        searchAndEdit("NucleicAcid","EBI-936571");
    }
    /**
     * Check that the Protein EBI-81752 is found and displayed. (Work only if  Protein EBI-81752 is present in
     * the database)
     */
    public void testSearchAndDisplayProtein(){
        searchAndEdit("Protein", "EBI-81752");
    }


    /**
     * This test, that if one search returns too many rows (more then 50), it does display
     * the error message : error.search.large ( see MessageRessources.properties)
     */
    public void testTooManyRowsSearch(){
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
        // set the topic to CvTopic, the searchString to accepted and the dispatch to search.
        addRequestParameter("topic", "CvTopic");
        addRequestParameter("searchString", "*");
        addRequestParameter("dispatch", "Search");
        // Perform the SidebarDispatchAction
        actionPerform();
        verifyTilesForward("failure","error.layout");
        // Veryfy that no Error is sent, if any error sent, the test won't pass.
        String[] errors = {"error.search.large"};
        verifyActionErrors(errors);

    }

    /**
     * Test that if the object is not in the database it displays the error.search.nomatch error (see MessageRessources.
     * properties).
     */
    public void testSearchNoMatchSearch(){
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
        // set the topic to CvTopic, the searchString to accepted and the dispatch to search.
        addRequestParameter("topic", "CvTopic");
        addRequestParameter("searchString", "duck");
        addRequestParameter("dispatch", "Search");
        // Perform the SidebarDispatchAction
        actionPerform();
        verifyTilesForward("failure","error.layout");
        // Veryfy that no Error is sent, if any error sent, the test won't pass.
        String[] errors = {"error.search.nomatch"};
        verifyActionErrors(errors);

    }

    /**
     * Test that if there's more than 1 result but less then 50, it displays all the results, so that the user
     * can choose which one he wants to display.
     */
    public void testSearchAndShowResults(){
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
        // set the topic to CvTopic, the searchString to accepted and the dispatch to search.
        addRequestParameter("topic", "Experiment");
        addRequestParameter("searchString", "butkevich-2004-*");
        addRequestParameter("dispatch", "Search");
        // Perform the SidebarDispatchAction
        actionPerform();
        // Verifies if the ActionServlet controller used the "result" forward.
        verifyForward("result");
        // Verifies if the ActionServlet controller used the "/do/showResults" forward path.
        verifyForwardPath("/do/showResults");
        // Veryfy that no Error is sent, if any error sent, the test won't pass.
        verifyNoActionErrors();
        // Veryfy that no ActionMessage is sent, if any error sent, the test won't pass.
        verifyNoActionMessages();
    }

    public void testCreateExperiment() {
        create("Experiment");
    }

    public void testCreateProtein() {
        create("Protein");
    }
    public void testCreateBioSource() {
        create("BioSource");
    }
    public void testCreateCvXrefQualifier() {
        create("CvXrefQualifier");
    }
    public void testCreateNucleicAcid() {
        create("NucleicAcid");
    }
    public void testCreateCvCellType() {
        create("CvCellType");
    }
    public void testCreateInteraction() {
        create("Interaction");
    }
    public void testCreateCvDatabase() {
        create("CvDatabase");
    }
    public void testCreateCvAliasType() {
        create("CvAliasType");
    }
    public void testCreateSmallMolecule() {
        create("SmallMolecule");
    }
    public void testCreateCvFuzzyType() {
        create("CvFuzzyType");
    }
    public void testCreateCvTissue() {
        create("CvTissue");
    }
    public void testCreateCvTopic() {
        create("CvTopic");
    }
    public void testCreateCvComponentRole() {
        create("CvComponentRole");
    }

    public void create(String topic){
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
        // set the topic to CvTopic, the searchString to accepted and the dispatch to search.
        addRequestParameter("topic",topic);
        addRequestParameter("dispatch", "Create");
        // Perform the SidebarDispatchAction
        actionPerform();
        // Verifies if the ActionServlet controller used the "result" forward.
        verifyForward("create");
        // Verifies if the ActionServlet controller used the "/do/showResults" forward path.
        verifyForwardPath("/do/choose");
        verifyNoActionErrors();
        // Veryfy that no ActionMessage is sent, if any error sent, the test won't pass.
        verifyNoActionMessages();

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
