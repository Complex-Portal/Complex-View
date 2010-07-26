/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.editor.controller.curate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.*;

import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AnnotatedObjectController extends JpaAwareController implements ValueChangeAware {

    private static final Log log = LogFactory.getLog( AnnotatedObjectController.class );

    private Date lastSaved;

    public AnnotatedObjectController() {
    }

    public abstract AnnotatedObject getAnnotatedObject();
    public abstract void setAnnotatedObject(AnnotatedObject annotatedObject);

    public AnnotatedObjectWrapper getAnnotatedObjectWrapper() {
        return new AnnotatedObjectWrapper(getAnnotatedObject());
    }

    public AnnotatedObjectHelper getAnnotatedObjectHelper() {
        return new AnnotatedObjectHelper(getAnnotatedObject());
    }

    @Transactional(propagation = Propagation.NEVER)
    public void doSave( ActionEvent evt ) {
        PersistenceController persistenceController = getPersistenceController();
        boolean saved = persistenceController.doSave(getAnnotatedObject());

        // saves specific elements for each annotated object (e.g. components in interactions)
        boolean detailsSaved = doSaveDetails();

        if (detailsSaved) saved = true;

        // delete from the unsaved manager
        final List<IntactObject> deletedObjects = getAnnotatedObjectWrapper().getUnsavedChangeManager().getAllDeleted();

        for (IntactObject intactObject : deletedObjects) {
            if (intactObject.getAc() != null) {
                if (log.isDebugEnabled()) log.debug("Deleting: " + DebugUtil.intactObjectToString(intactObject, false));

                getDaoFactory().getIntactObjectDao().deleteByAc(intactObject.getAc());
            }
        }

        if (saved) {
            lastSaved = new Date();
            setUnsavedChanges(false);
        }

        if (getAnnotatedObject().getAc() != null) {
            final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();
            
            getDaoFactory().getEntityManager().refresh(getAnnotatedObject());
            IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus);
        }
        
        getUnsavedChangeManager().clearChanges();
    }

    public boolean doSaveDetails() {
        return false;
    }

    @Transactional(propagation = Propagation.NEVER)
    public void doRevertChanges( ActionEvent evt ) {
//        getCoreEntityManager().clear();
//        getCoreEntityManager().refresh(getAnnotatedObject());
        if (getAnnotatedObject().getAc() != null) {
            final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

            getDaoFactory().getEntityManager().refresh(getAnnotatedObject());
            IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus);
        } else {
            setAnnotatedObject(null);
        }

        getUnsavedChangeManager().clearChanges();

        addInfoMessage("Changes reverted", "");
    }

    @Override
    public void changed(AjaxBehaviorEvent evt) {
        setUnsavedChanges(true);
    }

    // XREFS
    ///////////////////////////////////////////////

    public void newXref( ActionEvent evt ) {
        getAnnotatedObjectHelper().newXref();
        setUnsavedChanges( true );
    }

    public List<Xref> getXrefs() {
        return getAnnotatedObjectHelper().getXrefs();
    }

    public void setXref( String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId ) {
        getAnnotatedObjectHelper().setXref(databaseIdOrLabel, qualifierIdOrLabel, primaryId);
    }

    public void replaceOrCreateXref( String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId ) {
        getAnnotatedObjectHelper().replaceOrCreateXref(databaseIdOrLabel, qualifierIdOrLabel, primaryId);
    }

    public void removeXref( String databaseIdOrLabel, String qualifierIdOrLabel ) {
        getAnnotatedObjectHelper().removeXref(databaseIdOrLabel, qualifierIdOrLabel);
    }

    public void removeXref( Xref xref ) {
        getAnnotatedObjectHelper().removeXref( xref );
        setUnsavedChanges( true );
    }

    public void addXref( String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId ) {
        getAnnotatedObjectHelper().addXref(databaseIdOrLabel, qualifierIdOrLabel, primaryId);
    }

    public String findXrefPrimaryId( String databaseId, String qualifierId ) {
        return getAnnotatedObjectHelper().findXrefPrimaryId(databaseId, qualifierId);
    }

    // ANNOTATIONS
    ///////////////////////////////////////////////

    public void newAnnotation( ActionEvent evt ) {
        getAnnotatedObjectHelper().newAnnotation();
        setUnsavedChanges( true );
    }

    public void addAnnotation( String topicIdOrLabel, String text ) {
        getAnnotatedObjectHelper().addAnnotation(topicIdOrLabel, text);
    }

    public void replaceOrCreateAnnotation( String topicOrShortLabel, String text ) {
        getAnnotatedObjectHelper().replaceOrCreateAnnotation(topicOrShortLabel, text);
    }

    public void removeAnnotation( String topicIdOrLabel ) {
        getAnnotatedObjectHelper().removeAnnotation(topicIdOrLabel);
    }

    public void removeAnnotation( String topicIdOrLabel, String text ) {
        getAnnotatedObjectHelper().removeAnnotation(topicIdOrLabel, text);
    }

    public void removeAnnotation( Annotation annotation ) {
        getAnnotatedObjectHelper().removeAnnotation( annotation );
        setUnsavedChanges( true );
    }

    public void setAnnotation( String topicIdOrLabel, Object value ) {
        getAnnotatedObjectHelper().setAnnotation(topicIdOrLabel, value);
    }

    public String findAnnotationText( String topicId ) {
        return getAnnotatedObjectHelper().findAnnotationText(topicId);
    }

    public List<Annotation> getAnnotations() {
        return getAnnotatedObjectHelper().getAnnotations();
    }

    // ALIASES
    ///////////////////////////////////////////////

    public void newAlias( ActionEvent evt ) {
        getAnnotatedObjectHelper().newAlias();
        setUnsavedChanges( true );
    }

    public void addAlias( String aliasTypeIdOrLabel, String text ) {
        getAnnotatedObjectHelper().addAlias(aliasTypeIdOrLabel, text);
    }

    public void setAlias( String aliasTypeIdOrLabel, Object value ) {
        getAnnotatedObjectHelper().setAlias(aliasTypeIdOrLabel, value);
    }

    public void removeAlias( String aliasTypeIdOrLabel, String text ) {
        getAnnotatedObjectHelper().removeAlias(aliasTypeIdOrLabel, text);
    }

    public void removeAlias( String aliasTypeIdOrLabel ) {
        getAnnotatedObjectHelper().removeAlias(aliasTypeIdOrLabel);
    }

    public List<Alias> getAliases() {
        return getAnnotatedObjectHelper().getAliases();
    }
    
    public String findAliasName( String aliasTypeId ) {
       return getAnnotatedObjectHelper().findAliasName(aliasTypeId);
    }

    /**
     * This method is to be used if only one instance of an aliasType is expected to be stored in a given annotatedObject.
     * @param aliasTypeIdOrLabel
     * @param text
     */
    public void addOrReplace( String aliasTypeIdOrLabel, String text ) {
        getAnnotatedObjectHelper().addOrReplace(aliasTypeIdOrLabel, text);

    }

    // OTHER
    ////////////////////////////////////////////////////

    protected PersistenceController getPersistenceController() {
        return (PersistenceController)getSpringContext().getBean("persistenceController");
    }

    public boolean isUnsavedChanges() {
        return getUnsavedChangeManager().isUnsavedChanges();
    }

    public void setUnsavedChanges(boolean unsavedChanges) {
        if (unsavedChanges) {
            getUnsavedChangeManager().markAsUnsaved(getAnnotatedObject());
        } else {
            getUnsavedChangeManager().removeFromUnsaved(getAnnotatedObject());
        }
    }

    public Date getLastSaved() {
        return lastSaved;
    }

    public void setLastSaved(Date lastSaved) {
        this.lastSaved = lastSaved;
    }

    public UnsavedChangeManager getUnsavedChangeManager() {
        return getAnnotatedObjectWrapper().getUnsavedChangeManager();
    }

    private class IntactObjectComparator implements Comparator<IntactObject> {
        @Override
        public int compare( IntactObject o1, IntactObject o2 ) {
            if ( o1.getAc() != null ) return 1;
            return 0;
        }
    }
}
