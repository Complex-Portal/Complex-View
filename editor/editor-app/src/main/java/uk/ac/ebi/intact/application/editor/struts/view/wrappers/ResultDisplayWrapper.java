/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.wrappers;

import org.apache.taglibs.display.TableDecorator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.util.LockManager;
import uk.ac.ebi.intact.application.commons.util.DateToolbox;
import uk.ac.ebi.intact.model.Experiment;

import java.text.SimpleDateFormat;

/**
 * This class is the wrapper class for the display library to display
 * results from a search page.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class ResultDisplayWrapper extends TableDecorator {

    private static final Log log = LogFactory.getLog(ResultDisplayWrapper.class);

    /**
     * The formatter for the date.
     */
    private static SimpleDateFormat ourDateFormatter =
            new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    /**
     * @return the link to the search application; clicking on this link, the
     * user will be taken into the search application.
     */
    public String getAc() {
        ResultRowData rowData = (ResultRowData) getObject();

        return "<a href=\"" + "javascript:show('" + rowData.getType() + "', '"
                + rowData.getShortLabel() + "')\">" + rowData.getAc() + "</a>";
    }

    /**
     * Returns the short label.
     * @return short label.
     */
    public String getShortLabel() {
        ResultRowData rowData = (ResultRowData) getObject();

        // The lock manager to check the lock.
        LockManager lmr = getLockManager();

        if (lmr.hasLock(rowData.getAc())) {
            // This object has a lock. Get the current user.
            EditUserI user = (EditUserI) getPageContext().getSession().getAttribute(
                    EditorConstants.INTACT_USER);

            // The current lock associated with the object.
            LockManager.LockObject lock = lmr.getLock(rowData.getAc());

            if (lock != null) {
                // Check the lock owner with the current user.
                if (lock.getOwner().equals(user.getUserName())) {
                    // Locked by the same user.
                    return "<span class=\"owner\">" + getEditorLink(rowData) + "</span>";
                }
                // Locked by someone else.
                return "<span class=\"error\">" + rowData.getShortLabel() + "</span>";
            }
        }
        // Not locked.
        return getEditorLink(rowData);
    }

    /**
     * Returns the full name.
     * @return the full name as a <code>String</code> for the wrapped object.
     */
    public String getFullName() {
        return ((ResultRowData) getObject()).getFullName();
    }

    /**
     * Returns the created information.
     * @return the full name as a <code>String</code> for the wrapped object.
     */
    public String getCreator() {
        return ((ResultRowData) getObject()).getCreator();
    }

    /**
     * Returns the created information.
     * @return the full name as a <code>String</code> for the wrapped object.
     */
    public String getUpdator() {
        return ((ResultRowData) getObject()).getUpdator();
    }

    public String getCreated(){
        String created = new String();
        try {
         created = DateToolbox.formatDate(((ResultRowData) getObject()).getCreated());
        }
        catch(Exception e){
            log.debug(e.getCause().getMessage());
            log.debug(e.getCause().getMessage());
        }
        return created;
    }

    public String getUpdated(){

        return DateToolbox.formatDate(((ResultRowData) getObject()).getUpdated());
    }

    public String getCreationInfo(){
        if((ResultRowData) getObject() != null){
         //Created 2005-JAN-26 by LUISA.
        log.debug("getShortlabel" + ((ResultRowData) getObject()).getShortLabel());
        log.debug("getCreated" + ((ResultRowData) getObject()).getCreated());
        log.debug("getCreator" + ((ResultRowData) getObject()).getCreator());
        }
        String creationInfo = new String();
        try{
            creationInfo = this.getCreated() + " by " + this.getCreator() + ".";
        }catch(Exception e){
            log.debug(e.getMessage());
        }
        return creationInfo;
    }

    public String getUpdateInfo(){
        if((ResultRowData) getObject() != null){
        //Created 2005-JAN-26 by LUISA.
        log.debug("getShortlabel" + ((ResultRowData) getObject()).getShortLabel());
        log.debug("getUpdaed" + ((ResultRowData) getObject()).getUpdated());
        log.debug("getUpdator" + ((ResultRowData) getObject()).getUpdator());
        }
        String updateInfo = new String();
        try{
            updateInfo= this.getUpdated() + " by " + this.getUpdator() + ".";
        }catch(Exception e){
            log.debug(updateInfo);
            log.debug(e.getMessage());
        }
        log.debug(updateInfo);
         
         return updateInfo;
    }

    /**
     * @return the owner for the current bean. "---" is returned if the current
     * bean is not locked.
     */
    public String getLock() {
        ResultRowData rowData = (ResultRowData) getObject();
        LockManager.LockObject lock = getLockManager().getLock(rowData.getAc());
        if (lock == null) {
            // No owner; no need for the title
            return "<input type=\"text\" size=\"7\" value=\"  ---  \" readonly>";
        }
        // Get the owner and the time stamp.
        String owner = lock.getOwner();
        String title = ourDateFormatter.format(lock.getLockDate());
        return "<input type=\"text\" size=\"7\" value=\"" + owner + "\" title=\""
                + title + "\" readonly>";
    }

    // Helper methods.

    /**
     * @return the link for the result page; clicking on this link, the
     * user will be taken into the edit page.
     */
    private String getEditorLink(ResultRowData rowData) {
        return "<a href=secure/edit?ac=" + rowData.getAc() + "&type="
                + rowData.getType() + ">" + rowData.getShortLabel() + "</a>";
    }

    private LockManager getLockManager() {
        return (LockManager) getPageContext().getServletContext().getAttribute(
                EditorConstants.LOCK_MGR);
    }
}
