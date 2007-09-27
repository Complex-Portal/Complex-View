/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.editor.event.EventListener;
import uk.ac.ebi.intact.application.editor.event.LogoutEvent;
import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditViewBeanFactory;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.struts.view.experiment.InteractionRowData;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.ExperimentRowData;
import uk.ac.ebi.intact.application.editor.struts.view.wrappers.ResultRowData;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.application.editor.util.LockManager;
import uk.ac.ebi.intact.application.editor.exception.AuthenticateException;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.*;
import uk.ac.ebi.intact.persistence.util.CgLibUtil;
import uk.ac.ebi.intact.searchengine.ResultWrapper;
import uk.ac.ebi.intact.searchengine.SearchHelper;
import uk.ac.ebi.intact.searchengine.SearchHelperI;
import uk.ac.ebi.intact.util.NewtServerProxy;
import uk.ac.ebi.intact.util.protein.UpdateProteinsI;
import uk.ac.ebi.intact.util.protein.UpdateProteins;
import uk.ac.ebi.intact.util.go.GoServerProxy;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.SQLException;

/**
 * This class stores information about an Intact Web user session. Instead of
 * binding multiple objects, only an object of this class is bound to a session,
 * thus serving a single access point for multiple information.
 * <p>
 * This class also implements the <tt>HttpSessionBindingListener</tt> interface
 * for it can be notified of session time outs.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class EditUser implements EditUserI, HttpSessionBindingListener {

    private static final Log log = LogFactory.getLog(EditUser.class);

    // -------------------------------------------------------------------------

    /**
     * The formatter for a short label.
     */
    public static class ShortLabelFormatter {

        /**
         * The pattern to split the short label.
         */
        private static final Pattern ourSLSplitPattern = Pattern.compile("-");

        /**
         * An array of strings after applying the split pattern.
         */
        private String[] myLabelComps;

        /**
         * Constructs an instance with given short label.
         * @param shortLabel the short label to do the formatting.
         */
        public ShortLabelFormatter(String shortLabel) {
            myLabelComps = ourSLSplitPattern.split(shortLabel);
        }

        /**
         * @return true if this short label has a branch (i.e., ends with a digit).
         */
        public boolean hasBranch() {
            return myLabelComps[getBranchPos()].matches("\\d+");
        }

        /**
         * @return true if this short label ends with x (e.g., abc-x).
         */
        public boolean hasClonedSuffix() {
            return myLabelComps[myLabelComps.length - 1].equals("x");
        }

        /**
         * @return true only if the short label hasn't got a branch or cloned suffix.
         */
        public boolean isRootOnly() {
            return !hasBranch() && !hasClonedSuffix();
        }

        /**
         * @return the branch number as an int or -1 if there is no branch.
         */
        public int getBranchNumber() {
            if (!hasBranch()) {
                // No branch.
                return -1;
            }
            return Integer.parseInt(getBranch());
        }

        /**
         * @return the root element without the branch (if it exists).
         */
        public String getRoot() {
            // Initialize with the first item.
            StringBuffer sb = new StringBuffer(myLabelComps[0]);

            // Decide where to stop.
            int stop = myLabelComps.length;
            if (hasClonedSuffix()) {
                --stop;
            }
            if (hasBranch()) {
                --stop;
            }
            // Loop from the second item.
            for (int i = 1; i < stop; i++) {
                sb.append("-").append(myLabelComps[i]);
            }
            return sb.toString();
        }

        /**
         * @return the branch component ot null if none exists.
         */
        public String getBranch() {
            if (!hasBranch()) {
                return null;
            }
            return myLabelComps[getBranchPos()];
        }

        private int getBranchPos() {
            int pos = myLabelComps.length - 1;
            if (hasClonedSuffix()) {
                --pos;
            }
            return pos;
        }
    }

    // -- End of Inner class --------------------------------------------------

    /**
     * The pattern to parse an existing cloned short label.
     * pattern: any number of characters followed by -, and digits.
     */
    private static final Pattern ourClonedSLPattern = Pattern.compile("^(.+)?\\-(\\d+)$");

    /**
     * Maintains the edit state; it used when inserting the header dynamically.
     * For example, if in the edit state, then append the current object's class
     * type to the header or else display the standard header.
     */
    private boolean myEditState;

    /**
     * The topic selected by the user.
     */
    private String mySelectedTopic;

    /**
     * The current view of the user.
     */
    private AbstractEditViewBean myEditView;

    /**
     * Holds the last search results.
     */
    private Collection mySearchCache = new ArrayList();

    /**
     * The name of the current user.
     */
    private String myUserName;

    /**
     * The password for the current user.
     */
    private String myPassword;

    /**
     * The name of the current database.
     */
    private transient String myDatabaseName;

    /**
     * The search helper. This is recreated if necessary.
     */
    private transient SearchHelperI mySearchHelper;

    /**
     * Reference to Newt proxy server instance; transient as it is created
     * if required.
     */
    private transient NewtServerProxy myNewtServer;

    /**
     * Reference to Go proxy server instance; transient as it is created
     * if required.
     */
    private transient GoServerProxy myGoServer;

    /**
     * Reference to the Protein factory.
     */
    private transient UpdateProteinsI myProteinFactory;

    /**
     * A set of currently edited/added experiments.
     */
    private transient Set<ExperimentRowData> myCurrentExperiments = new HashSet<ExperimentRowData>();

    /**
     * A set of currently edited/added interactions.
     */
    private transient Set<InteractionRowData> myCurrentInteractions = new HashSet<InteractionRowData>();

    /**
     * Contains views when the user navigates from one editor to another.
     */
    private transient Stack<AbstractEditViewBean> myViewStack = new Stack<AbstractEditViewBean>();

    // ------------------------------------------------------------------------

    // Constructors.

    /**
     * The default constructor. This constructor uses the default values for
     * user and password (mainly for testing).
     */
    public EditUser() {
    }

    /**
     * Constructs an instance of this class with given user and the password.
     *
     * @param user the user
     * @param password the password of <code>user</code>.
     * @exception IntactException
     * due to an invalid user.
     */
    public EditUser(String user, String password, String databaseName) throws AuthenticateException {
        myUserName = user;
        myPassword = password;
        myDatabaseName = databaseName;
    }

    // Methods to handle special serialization issues.

    private void readObject(ObjectInputStream in) throws IOException,
                                                         ClassNotFoundException {
        in.defaultReadObject();
        try {
            myViewStack = new Stack();
        }
        catch (IntactException ie) {
            log.error("", ie);
            throw new IOException(ie.getMessage());
        }
    }

    // Implements HttpSessionBindingListener

    /**
     * Will call this method when an object is bound to a session.
     * Starts the user view.
     */
    public void valueBound(HttpSessionBindingEvent event) {}

    /**
     * Will call this method when an object is unbound from a session. This
     * method sets the logout time.
     */
    public void valueUnbound(HttpSessionBindingEvent event) {
        // This method is also called when logging in (invalidate the session).
        if (myUserName == null) {
            // Called by session invalidate when logging in.
            return;
        }
        ServletContext ctx = event.getSession().getServletContext();
        LockManager lm = (LockManager) ctx.getAttribute(EditorConstants.LOCK_MGR);

        // Notify the event listener.
        EventListener listener = (EventListener) ctx.getAttribute(
                EditorConstants.EVENT_LISTENER);
        listener.notifyObservers(new LogoutEvent(myUserName));

        // Release all the locks held by this user.
        lm.releaseAllLocks(myUserName);

        // Release the current view back to the pool.
        releaseView();

        // Release any views stored on the view stack back to the pool.
        EditViewBeanFactory factory = EditViewBeanFactory.getInstance();
        while (!myViewStack.empty()) {
            factory.returnObject((AbstractEditViewBean) myViewStack.pop());
        }
    }

    // Override Objects's equal method.

    /**
     * Compares <code>obj</code> with this object according to
     * Java's equals() contract.
     * @param obj the object to compare.
     * @return true only if <code>obj</code> is an instance of this class
     * and the user name is same. For all other instances, false is returned.
     */
    @Override
    public boolean equals(Object obj) {
        // Identical to this?
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EditUser)) {
            return false;
        }
        // Can cast it safely.
        EditUser other = (EditUser) obj;
        // User name must equal
        return other.myUserName.equals(other.myUserName);
    }

    // Implementation of IntactUserI interface.

    public String getUserName() {
        return myUserName;
    }

    public String getDatabaseName() {
        return myDatabaseName;
    }

    public <T extends IntactObject> Collection<T> search(Class<T> objectType, String searchParam,
                                                         String searchValue) throws IntactException {
        throw new UnsupportedOperationException();
    }

    // Implementation of EditUserI interface.

    public AbstractEditViewBean getView() {
        return myEditView;
    }

    public void releaseView() {
        if (myEditView != null) {
            EditViewBeanFactory.getInstance().returnObject(myEditView);
        }
        myEditView = null;
    }

    public void setView(AbstractEditViewBean view) {
        // Return the view back to the pool.
        releaseView();
        myEditView = view;
    }

    public void setView(Class clazz) {
        setView(clazz, true);
    }

    public void setView(Class clazz, boolean release) {
        if (release) {
            // Return the view back to the pool.
            releaseView();
        }
        else {
            myViewStack.push(myEditView);
        }
        // Start editing the object.
        startEditing();
        // The new view based on the class type.
        myEditView = EditViewBeanFactory.getInstance().borrowObject(clazz);
        myEditView.reset(clazz);
    }

    public void setView(AnnotatedObject annobj) throws IntactException {
        setView(annobj, true);
    }

    public void setView(AnnotatedObject annobj, boolean release) throws IntactException {
        if (release) {
            // Return the view back to the pool.
            releaseView();
        }
        else {
            myViewStack.push(myEditView);
        }
        // Start editing the object.
        startEditing();
        // View based on the class for given edit object.
        myEditView = EditViewBeanFactory.getInstance().borrowObject(
                CgLibUtil.getRealClassName(annobj), 0);
        // Resets the view with the new annotated object.
        myEditView.reset(annobj);
        // Load menus after setting the annotated object.
        myEditView.loadMenus();
    }

    public void setClonedView(AnnotatedObject obj, String originalAc) throws IntactException {
        startEditing();
        // Return the view back to the pool.
        releaseView();
        myEditView = EditViewBeanFactory.getInstance().borrowObject(
                CgLibUtil.getRealClassName(obj), 0);
        myEditView.resetClonedObject(obj, this);
        myEditView.setOriginalAc(originalAc);
        // Load menus after setting the annotated object.
        myEditView.loadMenus();
    }

    public boolean restorePreviousView() {
        if (hasPreviousView()) {
            setView(popPreviousView());
            return true;
        }
        return false;
    }

    public AbstractEditViewBean popPreviousView() {
        if (hasPreviousView()) {
            return myViewStack.pop();
        }
        return null;
    }

    public AbstractEditViewBean peekPreviousView() {
        if (hasPreviousView()) {
            return myViewStack.peek();
        }
        return null;
    }

    public boolean hasPreviousView() {
        return !myViewStack.isEmpty();
    }

    public String getSelectedTopic() {
        return mySelectedTopic;
    }

    public void setSelectedTopic(String topic) {
        mySelectedTopic = topic;
    }

    public boolean isEditing() {
        return myEditState;
    }

    public void startEditing() {
        myEditState = true;
    }


    public void rollback() throws IntactException {
        endEditing();
    }

    public void delete() throws IntactException {
        AnnotatedObject annobj = myEditView.getAnnotatedObject();
        if (annobj.getAc() == null) {
            return;
        }

        delete(annobj);
    }

    private void delete(AnnotatedObject annobj){
        if(annobj.getAc() == null){
            return;
        }

        if(annobj instanceof Experiment){
            log.debug("myAnnotObject is instanceof Experiment");
            ExperimentDao experimentDao = DaoProvider.getDaoFactory().getExperimentDao();
            experimentDao.delete((Experiment) annobj);
        }else if(annobj instanceof Interactor){
            log.debug("myAnnotObject is instanceof Interactor");
            InteractorDao interactorDao = DaoProvider.getDaoFactory().getInteractorDao();
            interactorDao.delete((Interactor)annobj);
        }else if(annobj instanceof Feature){
            log.debug("myAnnotObject is instanceof Feature");
            FeatureDao featureDao = DaoProvider.getDaoFactory().getFeatureDao();
            featureDao.delete((Feature) annobj);
        }else if(annobj instanceof CvObject){
            log.debug("myAnnotObject is instanceof CvObject");
            CvObjectDao<CvObject> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvObject.class);
            cvObjectDao.delete((CvObject) annobj);
        }else if(annobj instanceof BioSource){
            log.debug("myAnnotObject is instanceof BioSource");
            BioSourceDao bioSourceDao = DaoProvider.getDaoFactory().getBioSourceDao();
            bioSourceDao.delete((BioSource) annobj);
        }
    }

    public void cancelEdit() {
        endEditing();
        releaseView();
        // Can't release the view vbecause it is required to display the cancelled
        // view in the search page.
    }

//    public ResultWrapper getSPTRProteins(String pid, int max) throws IntactException {
//        // The result wrapper to return.
//        ResultWrapper rw = null;
//
//        // Set the helper as it has already been closed.
//        Collection prots;
//
//            prots = getMyProteinFactory().insertSPTrProteins(pid);
//
//        if (prots.size() > max) {
//            // Exceeds the maximum size.
//            rw = new ResultWrapper(prots.size(), max);
//        }
//        else if (prots.isEmpty()) {
//            rw = new ResultWrapper(0, max);
//        }
//        else {
//            // Within allowed range
//            rw = new ResultWrapper(prots, max);
//        }
//        return rw;
//    }

//    public Exception getProteinParseException() {
//        // Map of exceptions.
//        Map map = getMyProteinFactory().getParsingExceptions();
//        // Only interested in the first entry as the parsing is limited to a
//        // single entry. Guard against empty exceptions
//        if (!map.values().isEmpty()) {
//            return (Exception) map.values().iterator().next();
//        }
//        return null;
//    }

    public UpdateProteinsI getMyProteinFactory(){
        if(IntactContext.getCurrentInstance().getDataContext().isTransactionActive()){
            log.debug("Transaction is active");
        }
        if(myProteinFactory == null){
            myProteinFactory = new UpdateProteins();
        }
        return myProteinFactory;
    }

    public void addToSearchCache(Collection<ResultRowData> results) {
        // Clear previous results.
        mySearchCache.clear();
        mySearchCache.addAll(results);
    }

    public void updateSearchCache(AnnotatedObject annotobj) {
        // Clear previous results.
        mySearchCache.clear();
        mySearchCache.add(new ResultRowData(annotobj));
    }

    public ResultWrapper lookup(Class clazz, String param, String value, int max)
            throws IntactException {

        // The result to return.
        try {
            InteractorDao interactorDao = DaoProvider.getDaoFactory().getInteractorDao();
            Collection<Interactor> interactors= interactorDao.getColByPropertyName(param, value);
            if (interactors.isEmpty()) {
                return new ResultWrapper(0, max);
            }
            else {
                return new ResultWrapper(interactors, max);
            }
        }
        catch (IntactException e) {
            // This is an internal error. Log it.
            log.error("", e);
            // Rethrow it again for the presentaion layer to handle it
            throw e;
        }
    }

    public boolean shortLabelExists(String label) throws IntactException {
        // Holds the result from the search.
        log.debug("Searching for an annotated object having shortlabel " + label + "and class " + myEditView.getEditClass());
        AnnotatedObject annotObj = getAnnotatedObjectWithIdenticalShortlabel(label, myEditView.getEditClass());
        if(annotObj == null){
            log.debug("No annotated object found with shortlabel " + label);
            // Don't have this short label on the database.
            return false;
        } else {
            log.debug("One annotated object found with shortlabel " + label + "it's ac is " + annotObj.getAc());
            String resultAc = annotObj.getAc();
            if (resultAc.equals(myEditView.getAc())) {
                log.debug("Acs  are equal : " + annotObj.getAc());
                // We have retrieved the same record from the DB.
                return false;
            }
        }
        // There is another record exists with the same short label.
        return true;
    }

    public AnnotatedObject getAnnotatedObjectWithIdenticalShortlabel (String shortlabel, Class editClass){
        AnnotatedObjectDao annotatedObjectDao = DaoProvider.getDaoFactory(editClass);

        return annotatedObjectDao.getByShortLabel(shortlabel);
    }

    public Collection<AnnotatedObject> getAnnotatedObjectsByShortlabelLike (String shortlabel, Class editClass){
        AnnotatedObjectDao annotatedObjectDao = DaoProvider.getDaoFactory(editClass);

        return annotatedObjectDao.getByShortLabelLike(shortlabel);
    }

    /**
     * Returns the next available short label from the persistent system.
     * @param clazz the calss or the type for the search.
     * @param label the starting short label; this must contain the suffix '-x'
     * @return the next available short label from the persistent system. Basically,
     * this takes the form of <code>label</code> with -x substituted with the last
     * persistent number. For example, it could be abc-2 provided that <code>label</code>
     * is abc-x and abc-1 is the last persistent suffix. This could be as same
     * as <code>label</code> if there is an error in accessing the database to
     * access other similar objects. Null is returned if <code>label</code> has
     * invalid format.
     */
    public String getNextAvailableShortLabel(Class<? extends AnnotatedObject> clazz, String label) {
        try {
            return doGetNextAvailableShortLabel(clazz, label);
        }
        catch (IntactException ie) {
            log.error("", ie);
            // Error in searching, just return the original name for user to
            // decide.
        }
        return label;
    }

    public List getSearchResult() {
        return (List) mySearchCache;
    }

    public NewtServerProxy getNewtProxy() {
        if (myNewtServer == null) {
            try {
                myNewtServer = new NewtServerProxy();
            }
            catch (MalformedURLException murle) {
                // This should never happen because default constructor has a
                // valid URL.
                log.info("Problem trying to create a NewtServerProxy, this shouldn't have happen has the " + "" +
                        "default constructor has a valid url (http://www.ebi.ac.uk/newt/display) :", murle);
            }
        }
        return myNewtServer;
    }

    public GoServerProxy getGoProxy() {
        if (myGoServer == null) {
            myGoServer = new GoServerProxy();
        }
        return myGoServer;
    }

    public String getHelpTag() {
        if (myEditView != null) {
            return myEditView.getHelpTag();
        }
        return null;
    }

    public void addToCurrentExperiment(ExperimentRowData row) {
        myCurrentExperiments.add(row);
    }

    public void removeFromCurrentExperiment(ExperimentRowData row) {
        myCurrentExperiments.remove(row);
    }

    public Set<ExperimentRowData> getCurrentExperiments() {
        return myCurrentExperiments;
    }

    public void addToCurrentInteraction(InteractionRowData row) {
        myCurrentInteractions.add(row);
    }

    public void removeFromCurrentInteraction(InteractionRowData row) {
        myCurrentInteractions.remove(row);
    }

    public Set<InteractionRowData> getCurrentInteractions() {
        return myCurrentInteractions;
    }

    /**
     * @return returns an instance of search helper. A new object is created
     * if the current search helper is null.
     */
    private SearchHelperI getSearchHelper() {
        if (mySearchHelper == null) {
            mySearchHelper = new SearchHelper();
        }
        return mySearchHelper;
    }

    /**
     * Finishes the editing session.
     */
    private void endEditing() {
        myEditState = false;
    }

    private String doGetNextAvailableShortLabel(Class<? extends AnnotatedObject> clazz, String label)
            throws IntactException {
        // The formatter to analyse the short label.
        ShortLabelFormatter formatter = new ShortLabelFormatter(label);

        // Holds the result from the search.
        Collection<? extends AnnotatedObject> results = null;

        // The prefix for the proposed short label;
        String prefix = null;

        // Try to guess the next new label.
        String nextLabel;

        if (formatter.isRootOnly()) {
            // case: abc
            AnnotatedObject annobj = getAnnotatedObjectWithIdenticalShortlabel(label,clazz);
            if(annobj == null){
                log.debug("No object found in db with shortlabel : " + label + " and class " + clazz.getName());
                return label;
            }
            // Label already exists.
            prefix = label;
            nextLabel = prefix + "-1";
        }
        else {
            // case: abc-x or abc-1
            prefix = formatter.getRoot();
            // Trying to guess the new label by increment the branch number by
            // one. This avoids loading all the cloned names with prefix-*.
            nextLabel = formatter.hasBranch()
                    ? prefix + "-" + (formatter.getBranchNumber() + 1)
                    : prefix + "-1";
        }

        AnnotatedObject annobj = getAnnotatedObjectWithIdenticalShortlabel(nextLabel,clazz);

        if (annobj == null) {
            log.debug("No object found in db with shortlabel corresponding to next label : " + nextLabel + " and class " + clazz.getName());

            return nextLabel;
        }
        // AT this point: no success with incrementing the branch number.

        // Get all the short labels with the prefix.
        results = getAnnotatedObjectsByShortlabelLike(prefix + "-"+"%",clazz);
        if (results.isEmpty()) {
            // No matches found for the longest match. The first clone entry.
            return prefix + "-1";
        }
        // Found at least one entry. Need to find out the largest number.
        int number = 2;

        for (AnnotatedObject result : results)
        {
            String shortLabel = result.getShortLabel();
            // Need to split the short label to extract the number.
            Matcher matcher1 = ourClonedSLPattern.matcher(shortLabel);
            if (matcher1.matches())
            {
                // Only consider a short label matching the prefix.
                if (matcher1.group(1).equals(prefix))
                {
                    // They should match; the search returns only the matching one.
                    int digit = Integer.parseInt(matcher1.group(2));
                    if (digit >= number)
                    {
                        number = digit + 1;
                    }
                }
            }
        }
        return prefix + "-" + number;
    }
}
