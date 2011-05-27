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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.core.persistence.dao.IntactObjectDao;
import uk.ac.ebi.intact.core.persister.Finder;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.controller.curate.cloner.EditorIntactCloner;
import uk.ac.ebi.intact.editor.controller.curate.cvobject.CvObjectService;
import uk.ac.ebi.intact.editor.controller.curate.publication.PublicationController;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.clone.IntactCloner;
import uk.ac.ebi.intact.model.clone.IntactClonerException;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.util.go.GoServerProxy;
import uk.ac.ebi.intact.util.go.GoTerm;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.validator.ValidatorException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class AnnotatedObjectController extends JpaAwareController implements ValueChangeAware {

    private final Logger log = LoggerFactory.getLogger(AnnotatedObjectController.class);

    private Date lastSaved;

    @Autowired
    private CuratorContextController curatorContextController;

    @Autowired
    private CurateController curateController;

    @Autowired
    private ChangesController changesController;

    public AnnotatedObjectController() {
    }

    public abstract AnnotatedObject getAnnotatedObject();
    public abstract void setAnnotatedObject(AnnotatedObject annotatedObject);

    public String goToParent() {
        PublicationController publicationController = (PublicationController) getSpringContext().getBean("publicationController");

        if (publicationController.getPublication() == null) {
            return "/curate/curate?faces-redirect=true";
        }

        return "/curate/publication?faces-redirect=true&includeViewParams=true";
    }

    public AnnotatedObjectWrapper getAnnotatedObjectWrapper() {
        return new AnnotatedObjectWrapper(changesController, getAnnotatedObject());
    }

    public AnnotatedObjectHelper getAnnotatedObjectHelper() {
        AnnotatedObject ao = getAnnotatedObject();

        if (ao == null) {
            return new EmptyAnnotatedObjectHelper();
        }

        if (!IntactCore.isInitialized(ao.getAnnotations())) {
            ao = getDaoFactory().getEntityManager().find(ao.getClass(), ao.getAc());
            setAnnotatedObject(ao);
        }

        return newAnnotatedObjectHelper(ao);
    }

    protected void generalLoadChecks() {
        if (getAnnotatedObject() != null) {
            ChangesController generalChangesController = (ChangesController) getSpringContext().getBean("changesController");
            if (generalChangesController.isObjectBeingEdited(getAnnotatedObject(), false)) {
                String who = generalChangesController.whoIsEditingObject(getAnnotatedObject());

                addWarningMessage("This object is already being edited by: "+who, "Modifications may be lost or affect current work by the other curator");
            }
        }
    }

    protected <T extends AnnotatedObject> T loadByAc(IntactObjectDao<T> dao, String ac) {
        T ao = (T) changesController.findByAc(ac);

        if (ao == null) {
            ao = dao.getByAc( ac );
        }

        return ao;
    }

    /**
     * Executes the deletions and save the current object using the <code>CorePersister</code>. It invokes preSave()
     * before saving just in case a specific controller needs to prepare the object for the save operation. After invoking
     * the CorePersister's save(), it invokes the doSaveDetails() callback that can be used to handle whatever is not handled
     * by the CorePersister (ie. wrapped components). At the end, the current object is refreshed from the database.
     *
     * @param evt the action faces event
     */
    public void doSave( ActionEvent evt ) {
        ChangesController changesController = (ChangesController) getSpringContext().getBean("changesController");

        // adjust any xref, just if the curator introduced a value in the primaryId of the xref
        // and clicked on save without focusing on another field first (which would trigger
        // a change event and field the values with ajax)
        //xrefChanged(null);

        // check if object already exists in the database before creating a new one
        try {
            // if the annotated object does not have an ac, check if another one similar exists in the db
            if (getAnnotatedObject().getAc() == null) {
                Finder finder = (Finder) getSpringContext().getBean("finder");

                final String ac = finder.findAc(getAnnotatedObject());

                if (ac != null) {
                    AnnotatedObject existingAo = getDaoFactory().getEntityManager().find(getAnnotatedObject().getClass(), ac);
                    addErrorMessage("An identical object exists: " + DebugUtil.annotatedObjectToString(existingAo, false), "Cannot save identical objects");
                    FacesContext.getCurrentInstance().renderResponse();
                    return;
                }
            }
        } catch (Throwable t) {
            handleException(t);
        }

        // delete from the unsaved manager
        final List<UnsavedChange> deletedObjects = changesController.getAllUnsavedDeleted();

        PersistenceController persistenceController = getPersistenceController();

        for (UnsavedChange unsaved : deletedObjects) {
            // remove the object to delete from its parent
            persistenceController.doDelete(unsaved.getUnsavedObject());

            changesController.removeFromDeleted(unsaved);
        }

        // annotated objects specific tasks to prepare the save
        doPreSave();

        AnnotatedObject annotatedObject = getAnnotatedObject();

        boolean saved = persistenceController.doSave(annotatedObject);

        // saves specific elements for each annotated object (e.g. components in interactions)
        boolean detailsSaved = doSaveDetails();

        if (detailsSaved) saved = true;

        if (saved) {
            lastSaved = new Date();
            setUnsavedChanges(false);
        }

        if (annotatedObject.getAc() != null) {
            changesController.removeFromUnsaved(annotatedObject);

            annotatedObject = refresh(annotatedObject);
        }

        setAnnotatedObject(annotatedObject);

        addInfoMessage("Saved", DebugUtil.annotatedObjectToString(getAnnotatedObject(), false));

        refreshCurrentViewObject();

        doPostSave();
    }

    private AnnotatedObject refresh(AnnotatedObject annotatedObject) {
        final TransactionStatus transactionStatus2 = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        boolean isNew = (getAnnotatedObject().getAc() == null);

        if (!isNew && getDaoFactory().getEntityManager().contains(annotatedObject)) {
            // the following line is commented because it seems to cause problems when deleting an xref - it is not deleted.
            // I should write some comments so I could remember why I did add it in the first place...

            //getDaoFactory().getEntityManager().refresh(annotatedObject);
        } else {
            annotatedObject = getDaoFactory().getEntityManager().find(annotatedObject.getClass(), annotatedObject.getAc());
        }
        IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus2);
        return annotatedObject;
    }

    protected void refreshCurrentViewObject() {
        CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

        final AnnotatedObject currentAo = curateController.getCurrentAnnotatedObjectController().getAnnotatedObject();

        if (currentAo != null && currentAo.getAc() != null && getAnnotatedObject() != null && !currentAo.getAc().equals(getAnnotatedObject().getAc())) {
            if (log.isDebugEnabled()) log.debug("Refreshing object in view: "+DebugUtil.annotatedObjectToString(currentAo, false));

            refresh(currentAo);
        }
    }

    public void doSaveIfNecessary(ActionEvent evt) {
        if (getAnnotatedObject().getAc() == null) {
            doSave(null);
        }
    }

    public void doPreSave() {
    }

    public void doPostSave() {
    }

    public boolean doSaveDetails() {
        return false;
    }

    public void validateAnnotatedObject(FacesContext context, UIComponent component, Object value) throws ValidatorException {

    }

    @Transactional(value = "transactionManager", propagation = Propagation.NEVER)
    public void doRevertChanges( ActionEvent evt ) {
        PersistenceController persistenceController = getPersistenceController();
        setAnnotatedObject((AnnotatedObject) persistenceController.doRevert(getAnnotatedObject()));

        changesController.clearCurrentUserChanges();

        postRevert();

        addInfoMessage("Changes reverted", "");
    }

    protected void postRevert(){
        // nothing by default
    }

    public String doCancelEdition() {
        addInfoMessage("Canceled", "");

        changesController.clearCurrentUserChanges();

        return goToParent();

    }

    public void changed() {
        setUnsavedChanges(true);
    }

    public void changed(ActionEvent evt) {
        setUnsavedChanges(true);
    }

    @Override
    public void changed(AjaxBehaviorEvent evt) {
        setUnsavedChanges(true);
    }

    public String clone() {
        return clone(getAnnotatedObject(), newClonerInstance());
    }

    protected String clone(AnnotatedObject ao, IntactCloner cloner) {
        AnnotatedObject clone = cloneAnnotatedObject(ao, cloner);

        if (clone == null) return null;

        addInfoMessage("Cloned annotated object", null);

        setAnnotatedObject(clone);

        changesController.markAsUnsaved(clone);

        return getCurateController().edit(clone);
    }

    protected <T extends AnnotatedObject> T cloneAnnotatedObject(T ao, IntactCloner cloner) {
        T clone = null;

        try {
            clone = cloner.clone(ao);
        } catch (IntactClonerException e) {
            addErrorMessage("Could not clone object", e.getMessage());
            handleException(e);
            return null;
        }

        modifyClone(clone);

        return clone;
    }

    public void modifyClone(AnnotatedObject clone) {
        // nothing by default
    }

    protected IntactCloner newClonerInstance() {
        return new EditorIntactCloner();
    }

    @Transactional(value = "transactionManager", propagation = Propagation.NEVER)
    public String doDelete() {
        PersistenceController persistenceController = getPersistenceController();

        if (persistenceController.doDelete(getAnnotatedObject())){
            setAnnotatedObject(null);
            return goToParent();
        }

        // if delete not successfull, just display the message and don't go to the parent because the message will be lost
        return "/curate/curate";
    }

    // XREFS
    ///////////////////////////////////////////////
    public void xrefChanged() {
        xrefChanged(null);
    }

    public void xrefChanged(AjaxBehaviorEvent evt) {
        changed(evt);

        GoServerProxy goServerProxy = new GoServerProxy();

        CvDatabase goDb = null;

        for (Xref xref : getXrefs()) {
            if (xref.getPrimaryId() != null &&
                    (xref.getPrimaryId().startsWith("go:") ||
                            xref.getPrimaryId().startsWith("GO:"))) {

                xref.setPrimaryId(xref.getPrimaryId().toUpperCase());

                try {
                    GoTerm goTerm = goServerProxy.query(xref.getPrimaryId());

                    if (goTerm != null) {
                        if (goDb == null) goDb = getDaoFactory().getCvObjectDao(CvDatabase.class).getByIdentifier(CvDatabase.GO_MI_REF);

                        xref.setCvDatabase(goDb);
                        xref.setSecondaryId(goTerm.getName());

                        CvXrefQualifier qualifier = calculateQualifier(goTerm.getCategory());
                        xref.setCvXrefQualifier(qualifier);
                    }
                } catch (GoServerProxy.GoIdNotFoundException notFoundExc) {
                    continue;
                } catch (Throwable e) {
                    handleException(e);
                    return;
                }
            }
        }
    }

    private CvXrefQualifier calculateQualifier(GoTerm category) {
        if (category == null) return null;

        String goId = category.getId();

        CvObjectDao<CvXrefQualifier> cvObjectDao = getDaoFactory().getCvObjectDao(CvXrefQualifier.class);

        if ("GO:0008150".equals(goId)) {
            return cvObjectDao.getByIdentifier(CvXrefQualifier.PROCESS_MI_REF);
        } else if ("GO:0005554".equals(goId)) {
            return cvObjectDao.getByIdentifier(CvXrefQualifier.FUNCTION_MI_REF);
        } else if ("GO:0005575".equals(goId)) {
            return cvObjectDao.getByIdentifier(CvXrefQualifier.COMPONENT_MI_REF);
        }

        if (log.isWarnEnabled()) log.warn("No qualifier found for category: "+goId);

        return null;
    }

    public void newXref( ActionEvent evt ) {
        getAnnotatedObjectHelper().newXref();
        setUnsavedChanges(true);
    }

    public List<Xref> getXrefs() {
        return getAnnotatedObjectHelper().getXrefs();
    }

    public void setXref( String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId ) {
        getAnnotatedObjectHelper().setXref(databaseIdOrLabel, qualifierIdOrLabel, primaryId, null);
    }

    public void replaceOrCreateXref( String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId ) {
        replaceOrCreateXref(databaseIdOrLabel, qualifierIdOrLabel, primaryId, null);
    }

    public void replaceOrCreateXref( String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId, String secondaryId) {
        getAnnotatedObjectHelper().replaceOrCreateXref(databaseIdOrLabel, qualifierIdOrLabel, primaryId, secondaryId);
        setUnsavedChanges(true);
    }

    public void removeXref( String databaseIdOrLabel, String qualifierIdOrLabel ) {
        getAnnotatedObjectHelper().removeXref(databaseIdOrLabel, qualifierIdOrLabel);
        setUnsavedChanges( true );
    }

    public void removeXref( Xref xref ) {
        getAnnotatedObjectHelper().removeXref(xref);
        setUnsavedChanges( true );
    }

    public void addXref( String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId ) {
        getAnnotatedObjectHelper().addXref(databaseIdOrLabel, qualifierIdOrLabel, primaryId, null);
        setUnsavedChanges(true);
    }

    public String findXrefPrimaryId( String databaseId, String qualifierId ) {
        return getAnnotatedObjectHelper().findXrefPrimaryId(databaseId, qualifierId);
    }

    public boolean isXrefValid(Xref xref) {
        if (xref == null) return false;
        if (xref.getPrimaryId() == null) return false;
        if (xref.getCvDatabase() == null) return true;

        final Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(xref.getCvDatabase(), CvTopic.XREF_VALIDATION_REGEXP_MI_REF);
        if (annotation == null) return true;

        return xref.getPrimaryId().matches(annotation.getAnnotationText());
    }

    public String externalLink( Xref xref ) {
        if (xref == null) return null;
        if (xref.getPrimaryId() == null) return null;
        if (xref.getCvDatabase() == null) return null;

        final Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(xref.getCvDatabase(), CvTopic.SEARCH_URL_MI_REF);
        if (annotation == null) return null;

        String extUrl = annotation.getAnnotationText();
        return extUrl.replaceAll("\\$\\{ac\\}", xref.getPrimaryId());

    }

    protected String navigateToObject(AnnotatedObject annotatedObject) {
        CurateController curateController = (CurateController) getSpringContext().getBean("curateController");
        setAnnotatedObject(annotatedObject);
        return curateController.newIntactObject(annotatedObject);
    }

    // ANNOTATIONS
    ///////////////////////////////////////////////

    public void newAnnotation( ActionEvent evt ) {
        getAnnotatedObjectHelper().newAnnotation();
        setUnsavedChanges(true);
    }

    public void addAnnotation( String topicIdOrLabel, String text ) {
        getAnnotatedObjectHelper().addAnnotation(topicIdOrLabel, text);
    }

    public void replaceOrCreateAnnotation( String topicOrShortLabel, String text ) {
        getAnnotatedObjectHelper().replaceOrCreateAnnotation(topicOrShortLabel, text);
        setUnsavedChanges(true);
    }

    public void removeAnnotation( String topicIdOrLabel ) {
        getAnnotatedObjectHelper().removeAnnotation(topicIdOrLabel);
        setUnsavedChanges( true );
    }

    public void removeAnnotation( String topicIdOrLabel, String text ) {
        getAnnotatedObjectHelper().removeAnnotation(topicIdOrLabel, text);
        setUnsavedChanges(true);
    }

    public void removeAnnotation( Annotation annotation ) {
        getAnnotatedObjectHelper().removeAnnotation( annotation );
        setUnsavedChanges(true);
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

    public List<AnnotatedObject> getParentsByAnnotationAc(String annotationAc) {
        return getDaoFactory().getAnnotationDao().getParentsWithAnnotationAc(annotationAc);
    }

    // ALIASES
    ///////////////////////////////////////////////

    public void newAlias( ActionEvent evt ) {
        getAnnotatedObjectHelper().newAlias();
        setUnsavedChanges(true);
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
        setUnsavedChanges(true);
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

    public String getCautionMessage() {
        return findAnnotationText(CvTopic.CAUTION_MI_REF);
    }

    public String getCautionMessage(AnnotatedObject ao) {
        if (ao == null) return null;
        return newAnnotatedObjectHelper(ao).findAnnotationText(CvTopic.CAUTION_MI_REF);
    }

    public String getInternalRemarkMessage() {
        return findAnnotationText(CvTopic.INTERNAL_REMARK);
    }

    public boolean isNoUniprotUpdate(Interactor interactor) {
        if (interactor == null) return false;

        return newAnnotatedObjectHelper(interactor).findAnnotationText(CvTopic.NON_UNIPROT) != null;
    }

    protected PersistenceController getPersistenceController() {
        return (PersistenceController)getSpringContext().getBean("persistenceController");
    }

    public boolean isUnsavedChanges() {
        return changesController.isUnsaved(getAnnotatedObject());
    }

    public void setUnsavedChanges(boolean unsavedChanges) {
        if (unsavedChanges) {
            changesController.markAsUnsaved(getAnnotatedObject());
        } else {
            changesController.removeFromUnsaved(getAnnotatedObject());
        }
    }

    public Date getLastSaved() {
        return lastSaved;
    }

    public void setLastSaved(Date lastSaved) {
        this.lastSaved = lastSaved;
    }

    public CuratorContextController getCuratorContextController() {
        return curatorContextController;
    }

    public CurateController getCurateController() {
        return curateController;
    }

    public User getCurrentUser() {
        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");
        return userSessionController.getCurrentUser();
    }

    public ChangesController getChangesController() {
        return changesController;
    }

    protected AnnotatedObjectHelper newAnnotatedObjectHelper(AnnotatedObject annotatedObject) {
        AnnotatedObjectHelper helper = (AnnotatedObjectHelper) getSpringContext().getBean("annotatedObjectHelper");
        helper.setAnnotatedObject(annotatedObject);

        return helper;
    }

    private static class EmptyAnnotatedObjectHelper extends AnnotatedObjectHelper {
        public EmptyAnnotatedObjectHelper() {
            super();
        }

        @Override
        public void newXref() {}

        @Override
        public List<Xref> getXrefs() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public void setXref(String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId, String secondaryId) {}

        @Override
        public void replaceOrCreateXref(String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId, String secondaryId) {}

        @Override
        public void removeXref(String databaseIdOrLabel, String qualifierIdOrLabel) {}

        @Override
        public void removeXref(Xref xref) {}

        @Override
        public void addXref(String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId, String secondaryId) {}

        @Override
        public String findXrefPrimaryId(String databaseId, String qualifierId) {
            return null;
        }

        @Override
        public void newAnnotation() {}

        @Override
        public void addAnnotation(String topicIdOrLabel, String text) {}

        @Override
        public void replaceOrCreateAnnotation(String topicOrShortLabel, String text) {}

        @Override
        public void removeAnnotation(String topicIdOrLabel) {}

        @Override
        public void removeAnnotation(String topicIdOrLabel, String text) {}

        @Override
        public void removeAnnotation(Annotation annotation) {}

        @Override
        public void setAnnotation(String topicIdOrLabel, Object value) {}

        @Override
        public String findAnnotationText(String topicId) {
            return null;
        }

        @Override
        public List<Annotation> getAnnotations() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public void newAlias() {}

        @Override
        public void addAlias(String aliasTypeIdOrLabel, String text) {}

        @Override
        public void setAlias(String aliasTypeIdOrLabel, Object value) {}

        @Override
        public void removeAlias(String aliasTypeIdOrLabel, String text) {}

        @Override
        public void removeAlias(String aliasTypeIdOrLabel) {}

        @Override
        public List<Alias> getAliases() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public String findAliasName(String aliasTypeId) {
            return null;
        }

        @Override
        public void addOrReplace(String aliasTypeIdOrLabel, String text) {}

        @Override
        protected PersistenceController getPersistenceController() {
            return super.getPersistenceController();
        }

        @Override
        protected IntactContext getIntactContext() {
            return super.getIntactContext();
        }

        @Override
        protected CvObjectService getCvObjectService() {
            return super.getCvObjectService();
        }

        @Override
        public AnnotatedObject getAnnotatedObject() {
            return null;
        }
    }

    private class IntactObjectComparator implements Comparator<IntactObject> {
        @Override
        public int compare( IntactObject o1, IntactObject o2 ) {
            if ( o1.getAc() != null ) return 1;
            return 0;
        }
    }
}
