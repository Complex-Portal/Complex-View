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

import org.primefaces.event.TabChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.lifecycle.LifecycleManager;
import uk.ac.ebi.intact.core.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.core.persistence.dao.IntactObjectDao;
import uk.ac.ebi.intact.core.persister.Finder;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.curate.cloner.EditorIntactCloner;
import uk.ac.ebi.intact.editor.controller.curate.cvobject.CvObjectService;
import uk.ac.ebi.intact.editor.controller.curate.interaction.ComplexController;
import uk.ac.ebi.intact.editor.controller.curate.publication.PublicationController;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.CvTermDao;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.lifecycle.LifeCycleManager;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleEvent;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleEventType;
import uk.ac.ebi.intact.jami.model.lifecycle.LifeCycleStatus;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Alias;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.Xref;
import uk.ac.ebi.intact.model.clone.IntactCloner;
import uk.ac.ebi.intact.model.clone.IntactClonerException;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.PublicationUtils;
import uk.ac.ebi.intact.util.go.GoServerProxy;
import uk.ac.ebi.intact.util.go.GoTerm;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    private boolean isAnnotationTopicDisabled;
    private boolean isXrefDisabled;
    private boolean isAliasDisabled;

    public AnnotatedObjectController() {
    }

    public abstract AnnotatedObject getAnnotatedObject();

    public abstract void setAnnotatedObject(AnnotatedObject annotatedObject);

    public abstract IntactPrimaryObject getJamiObject();

    public abstract void setJamiObject(IntactPrimaryObject annotatedObject);

    public void refreshTabsAndFocusXref() {
        isXrefDisabled = false;
        isAliasDisabled = true;
        isAnnotationTopicDisabled = true;
    }

    public void refreshTabs() {
        isXrefDisabled = true;
        isAliasDisabled = true;
        isAnnotationTopicDisabled = true;
    }

    public String goToParent() {
        if (isPublicationParent()){
            PublicationController publicationController = (PublicationController) getSpringContext().getBean("publicationController");

            if (publicationController.getPublication() == null) {
                return "/curate/curate?faces-redirect=true";
            }

            return "/curate/publication?faces-redirect=true&includeViewParams=true";
        }
        else{
            ComplexController complexController = (ComplexController) getSpringContext().getBean("complexController");

            if (complexController.getComplex() == null) {
                return "/curate/curate?faces-redirect=true";
            }

            return "/curate/complex?faces-redirect=true&includeViewParams=true";
        }
    }

    protected boolean isPublicationParent() {
        return true;
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

                addWarningMessage("This object is already being edited by: " + who, "Modifications may be lost or affect current work by the other curator");
            }

            PublicationController publicationController = (PublicationController) getSpringContext().getBean("publicationController");

            if (publicationController.getPublication() != null) {
                Publication publication = publicationController.getPublication();

                if (publication.getStatus() == null) {
                    // we assume for now that null status means that the publication has been created using an external process (ie. XML import)
                    LifecycleManager lifecycleManager = getSpringContext().getBean(LifecycleManager.class);
                    lifecycleManager.getStartStatus().create(publication, "Imported from external source");

                    addWarningMessage("Publication without status", "Assuming that it has been imported. Save it if you are happy with this assumption");
                    setUnsavedChanges(true);
                } else if (CvPublicationStatusType.CURATION_IN_PROGRESS.identifier().equals(publication.getStatus().getIdentifier())) {
                    if (!getUserSessionController().isItMe(publication.getCurrentOwner())) {
                        addWarningMessage("Publication being curated by '" + publication.getCurrentOwner().getLogin() + "'", "Please do not modify it without permission");
                    }
                } else if (CvPublicationStatusType.READY_FOR_CHECKING.identifier().equals(publication.getStatus().getIdentifier())) {
                    if (!getUserSessionController().isItMe(publication.getCurrentReviewer())) {
                        addWarningMessage("Publication under review", "This publication is being reviewed by '" + publication.getCurrentReviewer().getLogin() + "'");
                    }
                } else if (CvPublicationStatusType.ACCEPTED_ON_HOLD.identifier().equals(publication.getStatus().getIdentifier())) {
                    addWarningMessage("Publication on-hold", "Reason: " + publicationController.getOnHold());
                } else if (CvPublicationStatusType.RELEASED.identifier().equals(publication.getStatus().getIdentifier())) {
                    LifecycleEvent event = PublicationUtils.getLastEventOfType(publication, CvLifecycleEventType.RELEASED.identifier());

                    SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy");
                    addInfoMessage("Publication already released", "This publication was released on " + sdf.format(event.getWhen()));
                }

            }
        }
        else if (getJamiObject() != null) {
            ChangesController generalChangesController = (ChangesController) getSpringContext().getBean("changesController");
            if (generalChangesController.isObjectBeingEdited(getJamiObject(), false)) {
                String who = generalChangesController.whoIsEditingObject(getJamiObject());

                addWarningMessage("This object is already being edited by: " + who, "Modifications may be lost or affect current work by the other curator");
            }

            ComplexController complexController = (ComplexController) getSpringContext().getBean("complexController");

            if (complexController.getComplex() != null) {
                IntactComplex complex = complexController.getComplex();

                if (complex.getStatus() == null) {
                    // we assume for now that null status means that the publication has been created using an external process (ie. XML import)
                    LifeCycleManager lifecycleManager = ApplicationContextProvider.getBean("jamiLifeCycleManager", LifeCycleManager.class);
                    lifecycleManager.getStartStatus().create(complex, "Imported from external source");

                    addWarningMessage("Complex without status", "Assuming that it has been imported. Save it if you are happy with this assumption");
                    setUnsavedChanges(true);
                } else if (LifeCycleStatus.CURATION_IN_PROGRESS.equals(complex.getStatus())) {
                    if (!getUserSessionController().isJamiUserMe(complex.getCurrentOwner())) {
                        addWarningMessage("Complex being curated by '" + complex.getCurrentOwner().getLogin() + "'", "Please do not modify it without permission");
                    }
                } else if (LifeCycleStatus.READY_FOR_CHECKING.equals(complex.getStatus())) {
                    if (!getUserSessionController().isJamiUserMe(complex.getCurrentReviewer())) {
                        addWarningMessage("Complex under review", "This complex is being reviewed by '" + complex.getCurrentReviewer().getLogin() + "'");
                    }
                } else if (LifeCycleStatus.ACCEPTED_ON_HOLD.equals(complex.getStatus())) {
                    addWarningMessage("Complex on-hold", "Reason: " + complexController.getOnHold());
                } else if (LifeCycleStatus.RELEASED.equals(complex.getStatus())) {

                    SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy");
                    Date releasedDate=null;
                    for (LifeCycleEvent evt : complex.getLifecycleEvents()){
                        if (LifeCycleEventType.RELEASED.equals(evt.getEvent())){
                            releasedDate = evt.getWhen();
                        }
                    }
                    addInfoMessage("Publication already released", "This publication was released on " + (releasedDate != null ? sdf.format(releasedDate) : ""));
                }
            }
        }
    }

    protected <T extends AnnotatedObject> T loadByAc(IntactObjectDao<T> dao, String ac) {
        T ao = (T) changesController.findByAc(ac);

        if (ao == null) {
            ao = dao.getByAc(ac);
        }

        return ao;
    }

    protected <T extends IntactPrimaryObject> T loadJamiByAc(Class<T> cl, String ac) {
        T ao = (T) changesController.findJamiByAc(ac);

        if (ao == null) {
            ao = getJamiEntityManager().find(cl, ac);
        }

        return ao;
    }

    public void unsavedValueChange(ValueChangeEvent evt) {
        if (evt.getOldValue() != null && !evt.getOldValue().equals(evt.getNewValue())) {
            setUnsavedChanges(true);
        } else if (evt.getNewValue() != null && !evt.getNewValue().equals(evt.getOldValue())) {
            setUnsavedChanges(true);
        }
    }

    public String doSave() {
        doSave(true);
        return null;
    }

    /**
     * Executes the deletions and save the current object using the <code>CorePersister</code>. It invokes preSave()
     * before saving just in case a specific controller needs to prepare the object for the save operation. After invoking
     * the CorePersister's save(), it invokes the doSaveDetails() callback that can be used to handle whatever is not handled
     * by the CorePersister (ie. wrapped components). At the end, the current object is refreshed from the database.
     *
     * @param evt the action faces event
     */
    public void doSave(ActionEvent evt) {
        // this method will save and refresh the current view
        doSave(true);
    }

    /**
     * Executes the deletions and save the current object using the <code>CorePersister</code>. It invokes preSave()
     * before saving just in case a specific controller needs to prepare the object for the save operation. After invoking
     * the CorePersister's save(), it invokes the doSaveDetails() callback that can be used to handle whatever is not handled
     * by the CorePersister (ie. wrapped components). At the end, the current object is refreshed from the database.
     */
    public void doSave(boolean refreshCurrentView) {
        ChangesController changesController = (ChangesController) getSpringContext().getBean("changesController");
        PersistenceController persistenceController = getPersistenceController();

        if (getAnnotatedObject() != null){
            // adjust any xref, just if the curator introduced a value in the primaryId of the xref
            // and clicked on save without focusing on another field first (which would trigger
            // a change event and field the values with ajax)
            if (IntactCore.isInitialized(getAnnotatedObject().getXrefs())) {
                xrefChanged(null);
            }

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

            String currentAc = getAnnotatedObject() != null ? getAnnotatedObject().getAc() : null;
            boolean currentAnnotatedObjectDeleted = false;

            // annotated objects specific tasks to prepare the save/delete
            doPreSave();

            boolean saved = false;

            // delete from the unsaved manager
            currentAnnotatedObjectDeleted = processDeleteEvents(currentAc);
            if (currentAnnotatedObjectDeleted){
                saved = true;
            }

            AnnotatedObject annotatedObject = getAnnotatedObject();

            if (!currentAnnotatedObjectDeleted) {
                saved = persistenceController.doSave(annotatedObject);

                if (saved) {
                    // saves specific elements for each annotated object (e.g. components in interactions)
                    boolean detailsSaved = doSaveDetails();

                    if (detailsSaved) saved = true;
                }
            }

            if (saved) {
                lastSaved = new Date();
                changesController.removeFromUnsaved(annotatedObject, collectParentAcsOfCurrentAnnotatedObject());
            }

            // we refresh the object if it has been saved
            if (annotatedObject.getAc() != null && saved) {

                annotatedObject = refresh(annotatedObject);
            }

            setAnnotatedObject(annotatedObject);

            if (annotatedObject != null) {
                addInfoMessage("Saved", DebugUtil.annotatedObjectToString(getAnnotatedObject(), false));
            }

            if (refreshCurrentView) {
                refreshCurrentViewObject();
            }
            doPostSave();
        }
        else{

            String currentAc = getJamiObject() != null ? getJamiObject().getAc() : null;
            boolean currentAnnotatedObjectDeleted = false;

            // annotated objects specific tasks to prepare the save/delete
            doPreSave();

            boolean saved = false;

            // delete from the unsaved manager
            // delete from the unsaved manager
            currentAnnotatedObjectDeleted = processDeleteEvents(currentAc);
            if (currentAnnotatedObjectDeleted){
                saved = true;
            }

            IntactPrimaryObject annotatedObject = getJamiObject();

            if (!currentAnnotatedObjectDeleted) {
                CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

                saved = persistenceController.doSave(annotatedObject, curateController.getJamiMetadata(annotatedObject).getDbSynchronizer());

                if (saved) {
                    // saves specific elements for each annotated object (e.g. components in interactions)
                    boolean detailsSaved = doSaveDetails();

                    if (detailsSaved) saved = true;
                }
            }

            if (saved) {
                lastSaved = new Date();
                changesController.removeFromUnsaved(annotatedObject, collectParentAcsOfCurrentAnnotatedObject());
            }

            // we refresh the object if it has been saved
            if (annotatedObject.getAc() != null && saved) {

                annotatedObject = refresh(annotatedObject);
            }

            setJamiObject(annotatedObject);

            if (annotatedObject != null) {
                addInfoMessage("Saved", annotatedObject.getAc());
            }

            if (refreshCurrentView) {
                refreshCurrentViewObject();
            }
            doPostSave();
        }
    }

    private boolean processDeleteEvents(String currentAc){
        boolean delete = false;
        // delete from the unsaved manager
        final List<UnsavedChange> deletedObjects = new ArrayList(changesController.getAllUnsavedDeleted());
        final List<UnsavedJamiChange> deletedJamiObjects = new ArrayList(changesController.getAllUnsavedJamiDeleted());

        PersistenceController persistenceController = getPersistenceController();

        for (UnsavedChange unsaved : deletedObjects) {

            IntactObject unsavedObject = unsaved.getUnsavedObject();

            // when an object is deleted, other deleted events can become obsolete and could have been removed from the deleted change events
            if (changesController.getAllUnsavedDeleted().contains(unsaved)) {
                // the object to delete is the current object itself. Should delete it now
                if (unsavedObject.getAc() != null && unsavedObject.getAc().equals(currentAc)) {

                    // remove the object to delete from its parent. If it is successful and the current object has been deleted, we can say that the save is successful
                    if (persistenceController.doDelete(unsavedObject)) {
                        delete = true;
                    }

                }
                // the object to delete is different from the current object. Checks that the scope of this object to delete is the ac of the current object being saved
                // if the scope is null or different, the object should not be deleted at this stage because we only save the current object and changes associated with it
                // if current ac is null, no deleted event should be associated with it as this object has not been saved yet
                else if (unsaved.getScope() != null && unsaved.getScope().equals(currentAc)) {
                    // remove the object to delete from its parent
                    persistenceController.doDelete(unsavedObject);
                }
            }
        }

        for (UnsavedJamiChange unsaved : deletedJamiObjects) {

            IntactPrimaryObject unsavedObject = unsaved.getUnsavedObject();

            // when an object is deleted, other deleted events can become obsolete and could have been removed from the deleted change events
            if (changesController.getAllUnsavedDeleted().contains(unsaved)) {
                // the object to delete is the current object itself. Should delete it now
                if (unsavedObject.getAc() != null && unsavedObject.getAc().equals(currentAc)) {

                    // remove the object to delete from its parent. If it is successful and the current object has been deleted, we can say that the save is successful
                    if (persistenceController.doDelete(unsavedObject, unsaved.getDbSynchronizer())) {
                        delete = true;
                    }

                }
                // the object to delete is different from the current object. Checks that the scope of this object to delete is the ac of the current object being saved
                // if the scope is null or different, the object should not be deleted at this stage because we only save the current object and changes associated with it
                // if current ac is null, no deleted event should be associated with it as this object has not been saved yet
                else if (unsaved.getScope() != null && unsaved.getScope().equals(currentAc)) {
                    // remove the object to delete from its parent
                    persistenceController.doDelete(unsavedObject, unsaved.getDbSynchronizer());
                }
            }
        }

        return delete;
    }

    private AnnotatedObject refresh(AnnotatedObject annotatedObject) {

        if (annotatedObject != null) {
            boolean isNew = false;

            if (getAnnotatedObject() != null) {
                isNew = (getAnnotatedObject().getAc() == null);
            }

            if (!isNew && getDaoFactory().getEntityManager().contains(annotatedObject)) {
                // the following line is commented because it seems to cause problems when deleting an xref - it is not deleted.
                // I should write some comments so I could remember why I did add it in the first place...

                getDaoFactory().getEntityManager().refresh(annotatedObject);
            } else {
                annotatedObject = getDaoFactory().getEntityManager().find(annotatedObject.getClass(), annotatedObject.getAc());
            }
        }

        return annotatedObject;
    }

    private IntactPrimaryObject refresh(IntactPrimaryObject annotatedObject) {

        if (annotatedObject != null) {
            boolean isNew = false;

            if (getJamiObject() != null) {
                isNew = (getJamiObject().getAc() == null);
            }

            if (!isNew && getJamiEntityManager().contains(annotatedObject)) {
                // the following line is commented because it seems to cause problems when deleting an xref - it is not deleted.
                // I should write some comments so I could remember why I did add it in the first place...

                getJamiEntityManager().refresh(annotatedObject);
            } else {
                annotatedObject = getJamiEntityManager().find(annotatedObject.getClass(), annotatedObject.getAc());
            }
        }

        return annotatedObject;
    }

    protected void refreshCurrentViewObject() {
        CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

        final AnnotatedObject currentAo = curateController.getCurrentAnnotatedObjectController().getAnnotatedObject();
        final IntactPrimaryObject currentJami = curateController.getCurrentAnnotatedObjectController().getJamiObject();

        if (currentAo != null && currentAo.getAc() != null) {

            // we have to refresh because the current annotated object is different from the annotated object of this controller
            if (getAnnotatedObject() != null && !currentAo.getAc().equals(getAnnotatedObject().getAc())) {
                if (log.isDebugEnabled())
                    log.debug("Refreshing object in view: " + DebugUtil.annotatedObjectToString(currentAo, false));

                AnnotatedObject refreshedAo = refresh(currentAo);
                curateController.getCurrentAnnotatedObjectController().setAnnotatedObject(refreshedAo);
            } else if (getAnnotatedObject() == null && currentAo != null) {
                if (log.isDebugEnabled())
                    log.debug("Refreshing object in view: " + DebugUtil.annotatedObjectToString(currentAo, false));

                AnnotatedObject refreshedAo = refresh(currentAo);
                curateController.getCurrentAnnotatedObjectController().setAnnotatedObject(refreshedAo);
            }
        }
        else if (currentJami != null && currentJami.getAc() != null) {

            // we have to refresh because the current annotated object is different from the annotated object of this controller
            if (getJamiObject() != null && !currentJami.getAc().equals(getJamiObject().getAc())) {
                if (log.isDebugEnabled())
                    log.debug("Refreshing object in view: " + currentJami.getAc());

                IntactPrimaryObject refreshedAo = refresh(currentJami);
                curateController.getCurrentAnnotatedObjectController().setJamiObject(refreshedAo);
            } else if (getJamiObject() == null && currentJami != null) {
                if (log.isDebugEnabled())
                    log.debug("Refreshing object in view: " + currentJami.getAc());

                IntactPrimaryObject refreshedAo = refresh(currentJami);
                curateController.getCurrentAnnotatedObjectController().setJamiObject(refreshedAo);
            }
        }
    }

    public void forceRefreshCurrentViewObject() {
        CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

        final AnnotatedObject currentAo = curateController.getCurrentAnnotatedObjectController().getAnnotatedObject();
        final IntactPrimaryObject currentJami = curateController.getCurrentAnnotatedObjectController().getJamiObject();

        if (currentAo != null && currentAo.getAc() != null) {

            if (log.isDebugEnabled())
                log.debug("Refreshing object in view: " + DebugUtil.annotatedObjectToString(currentAo, false));

            AnnotatedObject refreshedAo = refresh(currentAo);
            curateController.getCurrentAnnotatedObjectController().setAnnotatedObject(refreshedAo);
        }
        else if (currentJami != null && currentJami.getAc() != null) {

            if (log.isDebugEnabled())
                log.debug("Refreshing object in view: " + currentJami.getAc());

            IntactPrimaryObject refreshedAo = refresh(currentJami);
            curateController.getCurrentAnnotatedObjectController().setJamiObject(refreshedAo);
        }
    }

    public void doSaveIfNecessary(ActionEvent evt) {
        if (getAnnotatedObject() != null && getAnnotatedObject().getAc() == null) {
            doSave(null);
        }
        else if (getJamiObject() != null && getJamiObject().getAc() == null){
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

    public void doRevertChanges(ActionEvent evt) {
        if (getAnnotatedObject() != null && getAnnotatedObject().getAc() == null) {
            doCancelEdition();
        }else if (getJamiObject() != null && getJamiObject().getAc() == null) {
            doCancelEdition();
        } else {
            // revert first all unsaved events attached to any children of this object (will avoid to persist new annotations on children eg. copy publication annotations to experiments
            // could not be reverted otherwise)
            refreshUnsavedChangesBeforeRevert();

            PersistenceController persistenceController = getPersistenceController();
            if (getAnnotatedObject() != null){
                setAnnotatedObject((AnnotatedObject) persistenceController.doRevert(getAnnotatedObject()));
            }
            else{
                setJamiObject(persistenceController.doRevert(getJamiObject()));
            }

            postRevert();

            addInfoMessage("Changes reverted", "");
        }
    }

    protected void postRevert() {
        // nothing by default
    }

    public String doCancelEdition() {
        addInfoMessage("Canceled", "");

        refreshUnsavedChangesBeforeRevert();

        return goToParent();

    }

    protected void refreshUnsavedChangesBeforeRevert() {
        if (getAnnotatedObject() != null){
            changesController.revert(getAnnotatedObject());
        }
        else{
            changesController.revert(getJamiObject());
        }
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
        if (getAnnotatedObject() != null){
            return clone(getAnnotatedObject(), newClonerInstance());
        }
        else{
            return clone(getJamiObject());
        }
    }

    protected String clone(AnnotatedObject ao, IntactCloner cloner) {
        AnnotatedObject clone = cloneAnnotatedObject(ao, cloner);

        if (clone == null) return null;

        addInfoMessage("Cloned annotated object", null);

        setAnnotatedObject(clone);

        setUnsavedChanges(true);

        return getCurateController().edit(clone);
    }

    protected String clone(IntactPrimaryObject ao) {
        IntactPrimaryObject clone = cloneAnnotatedObject(ao);

        if (clone == null) return null;

        addInfoMessage("Cloned annotated object", null);
        setJamiObject(clone);
        setUnsavedChanges(true);
        refreshTabsAndFocusXref();
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

    protected <T extends IntactPrimaryObject> T cloneAnnotatedObject(T ao) {
        // to be overrided
        return null;
    }

    public void modifyClone(AnnotatedObject clone) {
        refreshTabsAndFocusXref();
    }

    protected IntactCloner newClonerInstance() {
        return new EditorIntactCloner();
    }

    public String doDelete() {
        PersistenceController persistenceController = getPersistenceController();

        if (getAnnotatedObject() != null && persistenceController.doDelete(getAnnotatedObject())) {
            setAnnotatedObject(null);
            return goToParent();
        }
        else if (getJamiObject() != null && persistenceController.doDelete(getJamiObject(),
                curateController.getJamiMetadata(getJamiObject()).getDbSynchronizer())){
        }
        // if delete not successfull, just display the message and don't go to the parent because the message will be lost
        // keep editing this object
        return curateController.edit(getAnnotatedObject() != null ? getAnnotatedObject() : getJamiObject());
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

        for (Object obj : getXrefs()) {
            Xref xref = (Xref)obj;
            if (xref.getPrimaryId() != null &&
                    (xref.getPrimaryId().startsWith("go:") ||
                            xref.getPrimaryId().startsWith("GO:"))) {

                xref.setPrimaryId(xref.getPrimaryId().toUpperCase());

                try {
                    GoTerm goTerm = goServerProxy.query(xref.getPrimaryId());

                    if (goTerm != null) {
                        if (goDb == null)
                            goDb = getDaoFactory().getCvObjectDao(CvDatabase.class).getByIdentifier(CvDatabase.GO_MI_REF);

                        xref.setCvDatabase(goDb);
                        xref.setSecondaryId(goTerm.getName());

                        GoTerm goCategory = goTerm.getCategory();
                        // we have a root term
                        if (goCategory == null) {
                            goCategory = goTerm;
                        }
                        CvXrefQualifier qualifier = calculateQualifier(goCategory);
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
        } else if ("GO:0003674".equals(goId)) { // GO:0005554 was an alternative id for molecular function
            return cvObjectDao.getByIdentifier(CvXrefQualifier.FUNCTION_MI_REF);
        } else if ("GO:0005575".equals(goId)) {
            return cvObjectDao.getByIdentifier(CvXrefQualifier.COMPONENT_MI_REF);
        }

        if (log.isWarnEnabled()) log.warn("No qualifier found for category: " + goId);

        return null;
    }

    protected CvTerm calculateCvQualifier(GoTerm category) {
        if (category == null) return null;

        String goId = category.getId();

        IntactDao dao = ApplicationContextProvider.getBean("intactDao");
        CvTermDao cvObjectDao = dao.getCvTermDao();
        Collection<IntactCvTerm> terms = Collections.EMPTY_LIST;

        if ("GO:0008150".equals(goId)) {
            terms = cvObjectDao.getByMIIdentifier(CvXrefQualifier.PROCESS_MI_REF);
        } else if ("GO:0003674".equals(goId)) { // GO:0005554 was an alternative id for molecular function
            terms = cvObjectDao.getByMIIdentifier(CvXrefQualifier.FUNCTION_MI_REF);
        } else if ("GO:0005575".equals(goId)) {
            terms = cvObjectDao.getByMIIdentifier(CvXrefQualifier.COMPONENT_MI_REF);
        }
        if (!terms.isEmpty()){
            return terms.iterator().next();
        }

        if (log.isWarnEnabled()) log.warn("No qualifier found for category: " + goId);

        return null;
    }

    public void newXref(ActionEvent evt) {
        getAnnotatedObjectHelper().newXref();
        setUnsavedChanges(true);
    }

    public List getXrefs() {
        return getAnnotatedObjectHelper().getXrefs();
    }

    public void setXref(String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId) {
        getAnnotatedObjectHelper().setXref(databaseIdOrLabel, qualifierIdOrLabel, primaryId, null);
    }

    public void replaceOrCreateXref(String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId) {
        replaceOrCreateXref(databaseIdOrLabel, qualifierIdOrLabel, primaryId, null);
    }

    public void replaceOrCreateXref(String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId, String secondaryId) {
        getAnnotatedObjectHelper().replaceOrCreateXref(databaseIdOrLabel, qualifierIdOrLabel, primaryId, secondaryId);
        setUnsavedChanges(true);
    }

    public void removeXref(String databaseIdOrLabel, String qualifierIdOrLabel) {
        getAnnotatedObjectHelper().removeXref(databaseIdOrLabel, qualifierIdOrLabel);
        setUnsavedChanges(true);
    }

    public void removeXref(Xref xref) {
        getAnnotatedObjectHelper().removeXref(xref);
        setUnsavedChanges(true);
    }

    public void addXref(String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId) {
        getAnnotatedObjectHelper().addXref(databaseIdOrLabel, qualifierIdOrLabel, primaryId, null);
        setUnsavedChanges(true);
    }

    public String findXrefPrimaryId(String databaseId, String qualifierId) {
        return getAnnotatedObjectHelper().findXrefPrimaryId(databaseId, qualifierId);
    }

    public boolean isXrefValid(Object obj) {
        if (obj == null) return false;
        else if (obj instanceof Xref){
            Xref xref = (Xref)obj;
            if (xref.getPrimaryId() == null) return false;
            if (xref.getCvDatabase() == null) return true;

            AnnotatedObject ao = xref.getCvDatabase();

            if (!IntactCore.isInitialized(ao.getAnnotations())) {
                ao = IntactContext.getCurrentInstance().getDaoFactory().getAnnotatedObjectDao(ao.getClass())
                        .getByAc(getAnnotatedObject().getAc());
            }

            if (ao == null) { // this can happen if the object has been removed in the same request just before
                return true;
            }

            final Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel( ao, CvTopic.XREF_VALIDATION_REGEXP_MI_REF);

            if (annotation == null) return true;
            return xref.getPrimaryId().matches(annotation.getAnnotationText());
        }
        else{
            psidev.psi.mi.jami.model.Xref ref = (psidev.psi.mi.jami.model.Xref)obj;
            if (ref.getId() == null) return false;
            if (ref.getDatabase() == null) return true;

            IntactCvTerm ao = (IntactCvTerm)ref.getDatabase();

            if (!ao.areAnnotationsInitialized()) {
                ao = getJamiEntityManager().find(IntactCvTerm.class, ao.getAc());
            }

            if (ao == null) { // this can happen if the object has been removed in the same request just before
                return true;
            }

            final psidev.psi.mi.jami.model.Annotation annotation = AnnotationUtils.collectFirstAnnotationWithTopic(ao.getAnnotations(),
                    psidev.psi.mi.jami.model.Annotation.VALIDATION_REGEXP_MI, psidev.psi.mi.jami.model.Annotation.VALIDATION_REGEXP);

            if (annotation == null) return true;
            return ref.getId().matches(annotation.getValue());
        }
    }

    public String externalLink(Object obj) {
        if (obj == null) return null;
        else if (obj instanceof Xref){
            Xref xref = (Xref)obj;
            if (xref.getPrimaryId() == null) return null;
            if (xref.getCvDatabase() == null) return null;

            AnnotatedObject ao = xref.getCvDatabase();

            if (!IntactCore.isInitialized(ao.getAnnotations())) {
                ao = IntactContext.getCurrentInstance().getDaoFactory().getAnnotatedObjectDao(ao.getClass())
                        .getByAc(getAnnotatedObject().getAc());
            }

            if (ao == null) { // this can happen if the object has been removed in the same request just before
                return null;
            }

            final Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(ao, CvTopic.SEARCH_URL_MI_REF);
            if (annotation == null) return null;


            String extUrl = annotation.getAnnotationText();
            return extUrl.replaceAll("\\$\\{ac\\}", xref.getPrimaryId());
        }
        else{
            psidev.psi.mi.jami.model.Xref ref = (psidev.psi.mi.jami.model.Xref)obj;
            if (ref.getId() == null) return null;
            if (ref.getDatabase() == null) return null;

            IntactCvTerm ao = (IntactCvTerm)ref.getDatabase();

            if (!ao.areAnnotationsInitialized()) {
                ao = getJamiEntityManager().find(IntactCvTerm.class, ao.getAc());
            }

            if (ao == null) { // this can happen if the object has been removed in the same request just before
                return null;
            }

            final psidev.psi.mi.jami.model.Annotation annotation = AnnotationUtils.collectFirstAnnotationWithTopic(ao.getAnnotations(),
                    psidev.psi.mi.jami.model.Annotation.SEARCH_URL_MI, psidev.psi.mi.jami.model.Annotation.SEARCH_URL);
            if (annotation == null) return null;

            String extUrl = annotation.getValue();
            return extUrl.replaceAll("\\$\\{ac\\}", ref.getId());
        }

    }

    protected String navigateToObject(Object annotatedObject) {
        CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

        if (annotatedObject instanceof AnnotatedObject){
            setAnnotatedObject((AnnotatedObject)annotatedObject);
        }
        else{
            setJamiObject((IntactPrimaryObject)annotatedObject);
        }
        return curateController.newIntactObject(annotatedObject);
    }

    // ANNOTATIONS
    ///////////////////////////////////////////////

    public void newAnnotation(ActionEvent evt) {
        getAnnotatedObjectHelper().newAnnotation();
        setUnsavedChanges(true);
    }

    public void addAnnotation(String topicIdOrLabel, String text) {
        getAnnotatedObjectHelper().addAnnotation(topicIdOrLabel, text);
    }

    public void replaceOrCreateAnnotation(String topicOrShortLabel, String text) {
        getAnnotatedObjectHelper().replaceOrCreateAnnotation(topicOrShortLabel, text);
        setUnsavedChanges(true);
    }

    public void removeAnnotation(String topicIdOrLabel) {
        getAnnotatedObjectHelper().removeAnnotation(topicIdOrLabel);
        setUnsavedChanges(true);
    }

    public void removeAnnotation(String topicIdOrLabel, String text) {
        getAnnotatedObjectHelper().removeAnnotation(topicIdOrLabel, text);
        setUnsavedChanges(true);
    }

    public void removeAnnotation(Annotation annotation) {
        getAnnotatedObjectHelper().removeAnnotation(annotation);
        setUnsavedChanges(true);
    }

    public void setAnnotation(String topicIdOrLabel, Object value) {
        getAnnotatedObjectHelper().setAnnotation(topicIdOrLabel, value);
    }

    public String findAnnotationText(String topicId) {
        return getAnnotatedObjectHelper().findAnnotationText(topicId);
    }

    public List getAnnotations() {
        return getAnnotatedObjectHelper().getAnnotations();
    }

    public List<AnnotatedObject> getParentsByAnnotationAc(String annotationAc) {
        return getDaoFactory().getAnnotationDao().getParentsWithAnnotationAc(annotationAc);
    }

    // ALIASES
    ///////////////////////////////////////////////

    public void newAlias(ActionEvent evt) {
        getAnnotatedObjectHelper().newAlias();
        setUnsavedChanges(true);
    }

    public void addAlias(String aliasTypeIdOrLabel, String text) {
        getAnnotatedObjectHelper().addAlias(aliasTypeIdOrLabel, text);
    }

    public void setAlias(String aliasTypeIdOrLabel, Object value) {
        getAnnotatedObjectHelper().setAlias(aliasTypeIdOrLabel, value);
    }

    public void removeAlias(String aliasTypeIdOrLabel, String text) {
        getAnnotatedObjectHelper().removeAlias(aliasTypeIdOrLabel, text);
    }

    public void removeAlias(String aliasTypeIdOrLabel) {
        getAnnotatedObjectHelper().removeAlias(aliasTypeIdOrLabel);
        setUnsavedChanges(true);
    }

    public List getAliases() {
        return getAnnotatedObjectHelper().getAliases();
    }

    public String findAliasName(String aliasTypeId) {
        return getAnnotatedObjectHelper().findAliasName(aliasTypeId);
    }

    /**
     * This method is to be used if only one instance of an aliasType is expected to be stored in a given annotatedObject.
     *
     * @param aliasTypeIdOrLabel
     * @param text
     */
    public void addOrReplace(String aliasTypeIdOrLabel, String text) {
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

    public String getJamiCautionMessage(IntactPrimaryObject ao) {
        return null;
    }

    public String getInternalRemarkMessage() {
        return findAnnotationText(CvTopic.INTERNAL_REMARK);
    }

    public boolean isNoUniprotUpdate(Interactor interactor) {
        if (interactor == null) return false;

        return newAnnotatedObjectHelper(interactor).findAnnotationText(CvTopic.NON_UNIPROT) != null;
    }

    public boolean isNoUniprotUpdate(psidev.psi.mi.jami.model.Interactor interactor) {
        if (interactor == null) return false;

        return AnnotationUtils.collectFirstAnnotationWithTopic(interactor.getAnnotations(), null, CvTopic.NON_UNIPROT) != null;
    }

    protected PersistenceController getPersistenceController() {
        return (PersistenceController) getSpringContext().getBean("persistenceController");
    }

    public boolean isUnsavedChanges() {
        if (getAnnotatedObject() != null){
            return changesController.isUnsaved(getAnnotatedObject());
        }
        else{
            return changesController.isUnsaved(getJamiObject());
        }
    }

    public void setUnsavedChanges(boolean unsavedChanges) {
        Collection<String> parentAcs = collectParentAcsOfCurrentAnnotatedObject();

        // we want to add a new change event for this annotated object
        if (unsavedChanges) {
            if (getAnnotatedObject() != null){
                changesController.markAsUnsaved(getAnnotatedObject(), parentAcs);
            }
            else{
                changesController.markAsUnsaved(getJamiObject(), parentAcs);
            }
        }
        // we want to remove any change event concerning this object (or affecting parent and children)
        else {
            if (getAnnotatedObject() != null){
                changesController.removeFromUnsaved(getAnnotatedObject(), parentAcs);
            }
            else{
                changesController.removeFromUnsaved(getJamiObject(), parentAcs);
            }
        }
    }

    public Collection<String> collectParentAcsOfCurrentAnnotatedObject() {
        return Collections.EMPTY_LIST;
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

    public boolean canIEditIt() {
        if (isPublicationParent()){
            PublicationController publicationController = (PublicationController) getSpringContext().getBean("publicationController");

            if (publicationController.getPublication() == null) {
                return true;
            } else if (publicationController.getPublication().getCurrentOwner() != null) {
                return getUserSessionController().isItMe(publicationController.getPublication().getCurrentOwner());
            }

            return true;
        }
        else{
            ComplexController complexController = (ComplexController) getSpringContext().getBean("complexController");

            if (complexController.getComplex() == null) {
                return true;
            } else if (complexController.getComplex().getCurrentOwner() != null) {
                return getUserSessionController().isJamiUserMe(complexController.getComplex().getCurrentOwner());
            }

            return true;
        }
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
        public void newXref() {
        }

        @Override
        public List<Xref> getXrefs() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public void setXref(String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId, String secondaryId) {
        }

        @Override
        public void replaceOrCreateXref(String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId, String secondaryId) {
        }

        @Override
        public void removeXref(String databaseIdOrLabel, String qualifierIdOrLabel) {
        }

        @Override
        public void removeXref(Xref xref) {
        }

        @Override
        public void addXref(String databaseIdOrLabel, String qualifierIdOrLabel, String primaryId, String secondaryId) {
        }

        @Override
        public String findXrefPrimaryId(String databaseId, String qualifierId) {
            return null;
        }

        @Override
        public void newAnnotation() {
        }

        @Override
        public void addAnnotation(String topicIdOrLabel, String text) {
        }

        @Override
        public void replaceOrCreateAnnotation(String topicOrShortLabel, String text) {
        }

        @Override
        public void removeAnnotation(String topicIdOrLabel) {
        }

        @Override
        public void removeAnnotation(String topicIdOrLabel, String text) {
        }

        @Override
        public void removeAnnotation(Annotation annotation) {
        }

        @Override
        public void setAnnotation(String topicIdOrLabel, Object value) {
        }

        @Override
        public String findAnnotationText(String topicId) {
            return null;
        }

        @Override
        public List<Annotation> getAnnotations() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public void newAlias() {
        }

        @Override
        public void addAlias(String aliasTypeIdOrLabel, String text) {
        }

        @Override
        public void setAlias(String aliasTypeIdOrLabel, Object value) {
        }

        @Override
        public void removeAlias(String aliasTypeIdOrLabel, String text) {
        }

        @Override
        public void removeAlias(String aliasTypeIdOrLabel) {
        }

        @Override
        public List<Alias> getAliases() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public String findAliasName(String aliasTypeId) {
            return null;
        }

        @Override
        public void addOrReplace(String aliasTypeIdOrLabel, String text) {
        }

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
        public int compare(IntactObject o1, IntactObject o2) {
            if (o1.getAc() != null) return 1;
            return 0;
        }
    }

    /**
     * Get the publication ac of this experiment if it exists and add it to the list or parentAcs
     *
     * @param parentAcs
     * @param exp
     */
    public void addPublicationAcToParentAcs(Collection<String> parentAcs, Experiment exp) {
        if (exp.getPublication() != null) {
            Publication pub = exp.getPublication();

            if (pub.getAc() != null) {
                parentAcs.add(pub.getAc());
            }
        }
    }

    /**
     * Get the publication ac of this experiment if it exists, the ac of this experiment if it exists and add it to the list or parentAcs
     *
     * @param parentAcs
     * @param exp
     */
    public void addParentAcsTo(Collection<String> parentAcs, Experiment exp) {
        if (exp.getAc() != null) {
            parentAcs.add(exp.getAc());
        }

        addPublicationAcToParentAcs(parentAcs, exp);
    }

    public boolean isAnnotationTopicDisabled() {
        return isAnnotationTopicDisabled;
    }

    public void setAnnotationTopicDisabled(boolean annotationTopicDisabled) {
        isAnnotationTopicDisabled = annotationTopicDisabled;
    }

    public boolean isXrefDisabled() {
        return isXrefDisabled;
    }

    public void setXrefDisabled(boolean xrefDisabled) {
        isXrefDisabled = xrefDisabled;
    }

    public boolean isAliasDisabled() {
        return isAliasDisabled;
    }

    public void setAliasDisabled(boolean aliasDisabled) {
        isAliasDisabled = aliasDisabled;
    }

    /**
     * Bug jsf : selectOneMenu in a tab returns null if not active tab so we disable the selectOneMenu when it is disabled
     *
     * @param e
     */
    public void onTabChanged(TabChangeEvent e) {

        // the xref tab is active
        if (e.getTab().getId().equals("xrefsTab")) {
            isXrefDisabled = false;
            isAliasDisabled = true;
            isAnnotationTopicDisabled = true;
        } else if (e.getTab().getId().equals("annotationsTab")) {
            isXrefDisabled = true;
            isAliasDisabled = true;
            isAnnotationTopicDisabled = false;
        } else if (e.getTab().getId().equals("aliasesTab")) {
            isXrefDisabled = true;
            isAliasDisabled = false;
            isAnnotationTopicDisabled = true;
        } else {
            isXrefDisabled = true;
            isAliasDisabled = true;
            isAnnotationTopicDisabled = true;
        }
    }
}
