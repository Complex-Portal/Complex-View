/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.commons.util.XrefHelper;
import uk.ac.ebi.intact.application.editor.business.EditorService;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.util.go.GoServerProxy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Bean to store data for x'references.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class XreferenceBean extends AbstractEditKeyBean {

    protected static final Log LOGGER = LogFactory.getLog(XreferenceBean.class);

    /**
     * Reference to the Xref object this instance is created with. Transient as
     * it can be created using values in the bean.
     */
    private transient Xref myXref;

    /**
     * The database name.
     */
    private String myDatabaseName;

    /**
     * The database link.
     */
    private String myDatabaseLink;

    /**
     * The primary id.
     */
    private String myPrimaryId;

    /**
     * The primary id as a link.
     */
    private String myPrimaryIdLink;

    /**
     * The secondary id.
     */
    private String mySecondaryId;

    /**
     * The release number.
     */
    private String myReleaseNumber;

    /**
     * The reference qualifier.
     */
    private String myReferenceQualifer;

    /**
     * Reference qualifier as a link.
     */
    private String myRefQualifierLink;

    /**
     * Default constructor.
     */
    public XreferenceBean() {
    }

    /**
     * Instantiate an object of this class from a Xref object.
     *
     * @param xref the <code>Xref</code> object to construct an
     *             instance of this class.
     */
    public XreferenceBean(Xref xref) {
        initialize(xref);
    }

    /**
     * Instantiates with given xref and key.
     *
     * @param xref the underlying <code>Xref</code> object.
     * @param key  the key to assigned to this bean.
     */
    public XreferenceBean(Xref xref, long key) {
        super(key);
        initialize(xref);
    }

    /**
     * Override to make a clone of this object.
     *
     * @return a cloned version of the current instance.
     * @throws CloneNotSupportedException for errors in cloning.
     */
    public Object clone() throws CloneNotSupportedException {
        XreferenceBean copy = (XreferenceBean) super.clone();
        copy.myXref = null;
        return copy;
    }

    /**
     * Updates the internal xref with the new values from the form
     * @throws IntactException for errors in searching the database.
     */
    public Xref getXref(AnnotatedObject annotatedObject) throws IntactException {
        // The CV objects to set.
        CvDatabase db = DaoProvider.getDaoFactory().getCvObjectDao(CvDatabase.class).getByShortLabel(myDatabaseName);
        CvXrefQualifier xqual = DaoProvider.getDaoFactory().getCvObjectDao(CvXrefQualifier.class).getByShortLabel(myReferenceQualifer);

        // Create a new xref (true if this object was cloned).
        if (myXref == null) {
            if(annotatedObject instanceof BioSource ){

               myXref = new BioSourceXref(IntactContext.getCurrentInstance().getConfig().getInstitution(), db, myPrimaryId, mySecondaryId, myReleaseNumber, xqual);
               myXref.setParent(annotatedObject);
            }else if (annotatedObject instanceof CvObject ){
                myXref = new CvObjectXref(IntactContext.getCurrentInstance().getConfig().getInstitution(), db, myPrimaryId, mySecondaryId, myReleaseNumber, xqual);
                myXref.setParent(annotatedObject);
            }else if (annotatedObject instanceof Experiment ){
                myXref = new ExperimentXref(IntactContext.getCurrentInstance().getConfig().getInstitution(), db, myPrimaryId, mySecondaryId, myReleaseNumber, xqual);
                myXref.setParent(annotatedObject);
            }else if (annotatedObject instanceof Feature ){
                myXref = new FeatureXref(IntactContext.getCurrentInstance().getConfig().getInstitution(), db, myPrimaryId, mySecondaryId, myReleaseNumber, xqual);
                myXref.setParent(annotatedObject);
            }else if (annotatedObject instanceof Interactor ){
                myXref = new InteractorXref(IntactContext.getCurrentInstance().getConfig().getInstitution(), db, myPrimaryId, mySecondaryId, myReleaseNumber, xqual);
                myXref.setParent(annotatedObject);
            }
            else{
                throw new IntactException("Unknown type of AnnotatedObject " + annotatedObject.getClass().getName() +
                " , can not create proper Xref");
            }
        } else {

        // Update the existing xref with new values.
            myXref.setCvDatabase(db);
            myXref.setPrimaryId(myPrimaryId);
            myXref.setSecondaryId(mySecondaryId);
            myXref.setDbRelease(myReleaseNumber);
            myXref.setCvXrefQualifier(xqual);
            myXref.setParent(annotatedObject);
        }
        return myXref;
    }

    /**
     * Return the database name.
     */
    public String getDatabase() {
        return myDatabaseName;
    }

    /**
     * Returns the database with a link to show its contents in a window.
     *
     * @return the database as a browsable link.
     */
    public String getDatabaseLink() {
        if (myDatabaseLink == null) {
            setDatabaseLink();
        }
        return myDatabaseLink;
    }

    /**
     * Sets the database name.
     *
     * @param dbname the name of the database.
     */
    public void setDatabase(String dbname) {
        if (!dbname.equals(myDatabaseName)) {
            myDatabaseName = dbname;
            setDatabaseLink();
        }
    }

    /**
     * Return the primary id as a link. Only used when viewing a xref.
     */
    public String getPrimaryIdLink() {
        if (myPrimaryIdLink == null) {
            setPrimaryIdLink();
        }
        return myPrimaryIdLink;
    }

    /**
     * Return the primary id. Used for editing the data (not the link).
     */
    public String getPrimaryId() {
        return myPrimaryId;
    }

    /**
     * Sets the primary id.
     *
     * @param primaryId the primary id as a <code>String</code>; any excess
     *                  blanks are trimmed as this is set from values entered into a free input
     *                  text box.
     */
    public void setPrimaryId(String primaryId) {
        String pid = primaryId.trim();
        if (!pid.equals(myPrimaryId)) {
            myPrimaryId = pid;
            setPrimaryIdLink();
        }
    }

    /**
     * Return the secondary id.
     */
    public String getSecondaryId() {
        return mySecondaryId;
    }

    /**
     * Sets the secondary id.
     *
     * @param secondaryId the primary id as a <code>String</code>.
     */
    public void setSecondaryId(String secondaryId) {
        mySecondaryId = secondaryId;
    }

    /**
     * Return the release number.
     */
    public String getReleaseNumber() {
        return myReleaseNumber;
    }

    /**
     * Sets the release number.
     *
     * @param releaseNumber the release number as a <code>String</code>.
     */
    public void setReleaseNumber(String releaseNumber) {
        myReleaseNumber = releaseNumber;
    }

    /**
     * Return the reference qualifier.
     */
    public String getQualifier() {
        return myReferenceQualifer;
    }

    /**
     * Returns the qualifier with a link to show its contents in a window.
     *
     * @return the qualifier as a browsable link.
     */
    public String getQualifierLink() {
        if (myRefQualifierLink == null) {
            setRefQualifierLink();
        }
        return myRefQualifierLink;
    }

    /**
     * Sets the reference qualifier.
     *
     * @param refQualifier the reference qaulifier as a <code>String</code>.
     */
    public void setQualifier(String refQualifier) {
        if (!refQualifier.equals(myReferenceQualifer)) {
            myReferenceQualifer = refQualifier;
            setRefQualifierLink();
        }
    }

    /**
     * Sets Secondary id and Qualifier values from the Go Server using PrimaryId.
     *
     * @param proxy the GO Proxy to get the GO response
     * @return non null for errors in accessing the Go server.
     */
    public ActionMessages setFromGoServer(GoServerProxy proxy) {
        // Get the response from the Go server.
        GoResult result = getGoResponse(proxy);

        if (result.myGoErrors != null) {
            // Found errors; return them.
            return result.myGoErrors;
        }
        // Only set the secondary id and the qualifier if there are no errors.
        setSecondaryId(result.myGoResponse.getName());
        setQualifier(result.myGoResponse.getCategory());

        // No errors; all set.
        return null;
    }

    /**
     * Resets fields to blanks, so the addXref form doesn't display
     * previous values.
     */
    public void clear() {
        myDatabaseName = null;
        myPrimaryId = null;
        myPrimaryIdLink = null;
        mySecondaryId = null;
        myReleaseNumber = null;
        myReferenceQualifer = null;
    }

    /**
     * Returns true if given bean is equivalent to the current bean.
     * @param xb the bean to compare.
     * @return true database, primary, secondary, release number and
     * reference qualifier are equivalent; otherwise false is returned.
     */
    public boolean isEquivalent(XreferenceBean xb) {
        // Check attributes.
        return xb.getDatabase().equals(getDatabase())
                && xb.getPrimaryId().equals(getPrimaryId())
                && xb.getSecondaryId().equals(getSecondaryId())
                && xb.getReleaseNumber().equals(getReleaseNumber())
                && xb.getQualifier().equals(getQualifier());
    }

    // Helper methods

    /**
     * Intialize the member variables using the given Xref object.
     *
     * @param xref <code>Xref</code> object to populate this bean.
     */
    private void initialize(Xref xref) {
        myXref = xref;

        myDatabaseName = xref.getCvDatabase().getShortLabel();
        setDatabaseLink();

        myPrimaryId = xref.getPrimaryId();
        mySecondaryId = xref.getSecondaryId();
        myReleaseNumber = xref.getDbRelease();

        CvXrefQualifier qualifier = xref.getCvXrefQualifier();
        if (qualifier != null) {
            myReferenceQualifer = qualifier.getShortLabel();
        }
        setRefQualifierLink();
        setPrimaryIdLink();
    }

    private GoResult getGoResponse(GoServerProxy proxy) {
        GoResult result = new GoResult();
        try {
            result.myGoResponse = proxy.query(getPrimaryId());
        }
        catch (GoServerProxy.GoIdNotFoundException ex) {

            LOGGER.info("GO Proxy", ex);
            // GO id not found.
            result.addErrors("error.xref.go.search",new ActionMessage("error.xref.go.search",
                    getPrimaryId()));
            return result;
        }
        catch (IOException ioe) {
            LOGGER.info("GO Proxy", ioe);
            // Error in communcating with the server.
            result.addErrors("error.xref.go.connection",new ActionMessage("error.xref.go.connection",
                    ioe.getMessage()));
            return result;
        }

        return result;
    }

    private void setDatabaseLink() {
        myDatabaseLink = getLink(EditorService.getTopic(CvDatabase.class),
                myDatabaseName);
    }

    private void setPrimaryIdLink() {
        // When no Xref is wrapped (for instance adding a new xref).
        if (myXref == null) {
            myPrimaryIdLink = myPrimaryId;
            return;
        }
        // The primary id link.
        String link = XrefHelper.getPrimaryIdLink(myXref);

        // javascipt to display the link.
        if (link.startsWith("http://")) {
            try {
                link = "<a href=\"" + "javascript:showXrefPId('"
                        + URLEncoder.encode(link, "UTF-8") + "')\"" + ">"
                        + myPrimaryId + "</a>";
            }
            catch (UnsupportedEncodingException uee) {
                // This shouldn't happen as we know the encoding.
                LOGGER.info("PID Link", uee);
            }
        }
        myPrimaryIdLink = link;
    }

    private void setRefQualifierLink() {
        myRefQualifierLink = getLink(EditorService.getTopic(CvXrefQualifier.class),
                myReferenceQualifer);
    }

    // ------------------------------------------------------------------------

    // Static class to encapsulate GO response and action errors.

    private static class GoResult {
        private ActionMessages myGoErrors;
        private GoServerProxy.GoResponse myGoResponse;

        private void addErrors(String property, ActionMessage error) {
            if (myGoErrors == null) {
                myGoErrors = new ActionMessages();
            }
            myGoErrors.add(property, error);
        }
    }
}
