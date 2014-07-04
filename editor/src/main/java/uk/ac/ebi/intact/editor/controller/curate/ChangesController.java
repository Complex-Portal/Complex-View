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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.*;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.UserListener;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.IntactModelledFeature;
import uk.ac.ebi.intact.jami.model.extension.IntactModelledParticipant;
import uk.ac.ebi.intact.jami.synchronizer.IntactDbSynchronizer;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.Range;
import uk.ac.ebi.intact.model.user.User;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import java.util.*;

/**
 * Contains the information about current changes.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
public class ChangesController extends JpaAwareController implements UserListener {

    private static final Log log = LogFactory.getLog(ChangesController.class);

    /**
     * Map containing the user name as the key, and a list with his/her changes.
     */
    private Map<String,List<UnsavedChange>> changesPerUser;
    /**
     * Map containing the user name as the key, and a list with his/her changes which are behind the scene and should be hidden from the user.
     */
    private Map<String,List<UnsavedChange>> hiddenChangesPerUser;
    /**
     * Map containing the user name as the key, and a list with his/her changes.
     */
    private Map<String,List<UnsavedJamiChange>> jamiChangesPerUser;
    /**
     * Map containing the user name as the key, and a list with his/her changes which are behind the scene and should be hidden from the user.
     */
    private Map<String,List<UnsavedJamiChange>> hiddenJamiChangesPerUser;

    public ChangesController() {
        changesPerUser = new HashMap<String, List<UnsavedChange>>();
        hiddenChangesPerUser = new HashMap<String, List<UnsavedChange>>();
        jamiChangesPerUser = new HashMap<String, List<UnsavedJamiChange>>();
        hiddenJamiChangesPerUser = new HashMap<String, List<UnsavedJamiChange>>();
    }

    @Override
    public void userLoggedIn(User user) {
        if (user == null) return;

        changesPerUser.put(user.getLogin(), new ArrayList<UnsavedChange>());
        hiddenChangesPerUser.put(user.getLogin(), new ArrayList<UnsavedChange>());
    }

    @Override
    public void userLoggedOut(User user) {
        userLoggedOut(user.getLogin());
    }

    private void userLoggedOut(String user) {
        final List<UnsavedChange> unsavedChanges = getUnsavedChangesForUser(user);
        final List<UnsavedChange> hiddenUnsavedChanges = getHiddenUnsavedChangesForUser(user);
        final List<UnsavedJamiChange> unsavedJamiChanges = getUnsavedJamiChangesForUser(user);
        final List<UnsavedJamiChange> hiddenUnsavedJamiChanges = getHiddenUnsavedJamiChangesForUser(user);

        if (unsavedChanges == null || hiddenUnsavedChanges == null
                || unsavedJamiChanges == null || hiddenUnsavedJamiChanges == null) {
            throw new IllegalStateException("No unsaved changes found for user: "+user);
        }

        if (!unsavedChanges.isEmpty() || !unsavedJamiChanges.isEmpty()) {
            int totalChanges = unsavedChanges.size()+unsavedJamiChanges.size();
            if (log.isInfoEnabled()) log.info("User logged out with "+totalChanges+" pending changes: "+user);

            unsavedChanges.clear();
            unsavedJamiChanges.clear();
        }

        if (!hiddenUnsavedChanges.isEmpty() || !hiddenUnsavedJamiChanges.isEmpty()) {
            int totalChanges = hiddenUnsavedChanges.size()+hiddenUnsavedJamiChanges.size();
            if (log.isInfoEnabled()) log.info("User logged out with "+totalChanges+" hidden pending changes: "+user);

            hiddenUnsavedChanges.clear();
            hiddenUnsavedJamiChanges.clear();
        }

        removeUserFromUnsaved(user);
        removeUserFromHiddenUnsaved(user);
    }

    public void markAsUnsaved(IntactObject io) {
        if (io == null) return;

        CurateController curateController = (CurateController) getSpringContext().getBean("curateController");
        AnnotatedObjectController annotatedObjectController = curateController.getMetadata(io).getAnnotatedObjectController();

        Collection<String> parentAcs = annotatedObjectController.collectParentAcsOfCurrentAnnotatedObject();

        UnsavedChange change;
        if (io.getAc() != null) {
            change = new UnsavedChange(io, UnsavedChange.UPDATED, null);
        } else {
            change = new UnsavedChange(io, UnsavedChange.CREATED, null);
        }

        change.getAcsToDeleteOn().addAll(parentAcs);
        addUnsavedChange(change);
    }

    public void markAsUnsaved(IntactPrimaryObject io) {
        if (io == null) return;

        CurateController curateController = (CurateController) getSpringContext().getBean("curateController");
        CurateController.CurateJamiMetadata meta = curateController.getJamiMetadata(io);
        IntactDbSynchronizer dbSynchronizer = meta.getDbSynchronizer();
        Collection<String> parentAcs = meta.getParents();

        UnsavedJamiChange change;
        if (io.getAc() != null) {
            change = new UnsavedJamiChange(io, UnsavedChange.UPDATED, null, dbSynchronizer);
        } else {
            change = new UnsavedJamiChange(io, UnsavedChange.CREATED, null, dbSynchronizer);
        }
        change.getAcsToDeleteOn().addAll(parentAcs);
        addUnsavedJamiChange(change);
    }

    public void markAsUnsaved(IntactObject io, Collection<String> parentAcs) {
        if (io == null) return;

        UnsavedChange change;

        if (io.getAc() != null) {
            change = new UnsavedChange(io, UnsavedChange.UPDATED, null);
        } else {
            change = new UnsavedChange(io, UnsavedChange.CREATED, null);
        }
        change.getAcsToDeleteOn().addAll(parentAcs);

        addUnsavedChange(change);
    }

    public void markAsUnsaved(IntactPrimaryObject io, Collection<String> parentAcs) {
        if (io == null) return;

        UnsavedJamiChange change;
        CurateController curateController = (CurateController) getSpringContext().getBean("curateController");
        CurateController.CurateJamiMetadata meta = curateController.getJamiMetadata(io);
        IntactDbSynchronizer dbSynchronizer = meta.getDbSynchronizer();

        if (io.getAc() != null) {
            change = new UnsavedJamiChange(io, UnsavedChange.UPDATED, null, dbSynchronizer);
        } else {
            change = new UnsavedJamiChange(io, UnsavedChange.CREATED, null, dbSynchronizer);
        }
        change.getAcsToDeleteOn().addAll(parentAcs);

        addUnsavedJamiChange(change);
    }

    public void markToDelete(IntactObject object, AnnotatedObject parent) {
        if (object.getAc() != null) {

            String scope;

            if (parent != null && parent.getAc() != null){
                scope = parent.getAc();
            }
            else {
                scope = null;
            }

            // very important to delete all changes which can be affected by the delete of this object!!!
            removeObsoleteChangesOnDelete(object);

            // collect parent acs for this intact object if possible
            CurateController curateController = (CurateController) getSpringContext().getBean("curateController");
            AnnotatedObjectController annotatedObjectController = curateController.getMetadata(object).getAnnotatedObjectController();

            Collection<String> parentAcs = annotatedObjectController.collectParentAcsOfCurrentAnnotatedObject();

            UnsavedChange change = new UnsavedChange(object, UnsavedChange.DELETED, parent, scope);
            change.getAcsToDeleteOn().addAll(parentAcs);

            addChange(change);

            // line commented because already done when adding the change
            //removeFromUnsaved(object);
        } else {
            AnnotatedObjectUtils.removeChild(parent, object);
        }
    }

    public void markToDelete(IntactPrimaryObject object, IntactPrimaryObject parent) {
        if (object.getAc() != null) {

            String scope;

            if (parent != null && parent.getAc() != null){
                scope = parent.getAc();
            }
            else {
                scope = null;
            }

            // very important to delete all changes which can be affected by the delete of this object!!!
            removeObsoleteJamiChangesOnDelete(object);

            // collect parent acs for this intact object if possible
            CurateController curateController = (CurateController) getSpringContext().getBean("curateController");
            CurateController.CurateJamiMetadata meta = curateController.getJamiMetadata(object);
            IntactDbSynchronizer dbSynchronizer = meta.getDbSynchronizer();
            Collection<String> parentAcs = meta.getParents();

            UnsavedJamiChange change = new UnsavedJamiChange(object, UnsavedChange.DELETED, scope, dbSynchronizer);
            change.getAcsToDeleteOn().addAll(parentAcs);
            addJamiChange(change);
        }
    }

    public void markToDeleteRange(Range range, Feature parent) {
        if (range.getAc() != null) {

            String scope;
            Collection<String> parentAcs = Collections.EMPTY_LIST;

            if (parent != null && parent.getAc() != null){
                scope = parent.getAc();
                // collect parent acs for this intact object if possible
                CurateController curateController = (CurateController) getSpringContext().getBean("curateController");
                AnnotatedObjectController annotatedObjectController = curateController.getMetadata(parent).getAnnotatedObjectController();

                parentAcs = annotatedObjectController.collectParentAcsOfCurrentAnnotatedObject();
                parentAcs.add(parent.getAc());
            }
            else {
                scope = null;
            }

            UnsavedChange change = new UnsavedChange(range, UnsavedChange.DELETED, parent, scope);
            change.getAcsToDeleteOn().addAll(parentAcs);

            addChange(change);

            // line commented because already done when adding the change
            //removeFromUnsaved(object);
        } else {
            AnnotatedObjectUtils.removeChild(parent, range);
        }
    }

    /**
     * When deleting an object, all save/created/deleted events attached to one of the children of this object became obsolete because will be deleted with the current object
     * @param object
     */
    public void removeObsoleteChangesOnDelete(IntactObject object){
        if (object.getAc() != null){

            List<UnsavedChange> changes = new ArrayList(getUnsavedChangesForCurrentUser());
            for (UnsavedChange change : changes){

                if (change.getAcsToDeleteOn().contains(object.getAc())){
                    getUnsavedChangesForCurrentUser().remove(change);
                }
            }

            List<UnsavedChange> changes2 = new ArrayList(getHiddenUnsavedChangesForCurrentUser());
            for (UnsavedChange change : changes2){

                if (change.getScope().contains(object.getAc())){
                    getHiddenUnsavedChangesForCurrentUser().remove(change);
                }
            }
        }
    }

    /**
     * When deleting an object, all save/created/deleted events attached to one of the children of this object became obsolete because will be deleted with the current object
     * @param object
     */
    public void removeObsoleteJamiChangesOnDelete(IntactPrimaryObject object){
        if (object.getAc() != null){

            List<UnsavedJamiChange> changes = new ArrayList(getUnsavedJamiChangesForCurrentUser());
            for (UnsavedJamiChange change : changes){

                if (change.getAcsToDeleteOn().contains(object.getAc())){
                    getUnsavedJamiChangesForCurrentUser().remove(change);
                }
            }

            List<UnsavedJamiChange> changes2 = new ArrayList(getHiddenUnsavedJamiChangesForCurrentUser());
            for (UnsavedJamiChange change : changes2){

                if (change.getScope().contains(object.getAc())){
                    getHiddenUnsavedJamiChangesForCurrentUser().remove(change);
                }
            }
        }
    }

    /**
     * When saving an object, all save/created/deleted events attached to one of the children (or parent) of this object became obsolete because will be updated with the current object.
     * However, in case of new publication, new experiment, new interaction, new participant, new feature, it is important to keep the change as it will not be created while updating this event.
     * New objects are only attached to their parents if saved. So when saving the parent, it will not create the child because not added yet
     *
     * @param object
     */
    public void removeObsoleteChangesOnSave(IntactObject object, Collection<String> parentAcs){
        if (object.getAc() != null){

            List<UnsavedChange> changes = new ArrayList(getUnsavedChangesForCurrentUser());
            for (UnsavedChange change : changes){

                // very important to check that the ac is not null. Any new children event is not obsolete after saving the parent because not added yet
                // checks the unsaved change is not new and is not a children of the current object to save. If it is a children, the unsaved event of the children becomes obsolete because the parent object will be saved
                if (change.getAcsToDeleteOn().contains(object.getAc()) && change.getUnsavedObject().getAc() != null){
                    getUnsavedChangesForCurrentUser().remove(change);
                }
                // we gave a list of parent acs for this object. It means that we want to remove all unsaved change event which refers to one of the parent acs
                else if (!parentAcs.isEmpty()){
                    // the save event concerns one of the parent of the current object being saved, we can remove this unsaved event as it will be saved with current object
                    if (change.getUnsavedObject().getAc() != null && parentAcs.contains(change.getUnsavedObject().getAc())){
                        getUnsavedChangesForCurrentUser().remove(change);
                    }
                }
            }
        }
    }

    /**
     * When saving an object, all save/created/deleted events attached to one of the children (or parent) of this object became obsolete because will be updated with the current object.
     * However, in case of new publication, new experiment, new interaction, new participant, new feature, it is important to keep the change as it will not be created while updating this event.
     * New objects are only attached to their parents if saved. So when saving the parent, it will not create the child because not added yet
     *
     * @param object
     */
    public void removeObsoleteChangesOnSave(IntactPrimaryObject object, Collection<String> parentAcs){
        if (object.getAc() != null){

            List<UnsavedJamiChange> changes = new ArrayList(getUnsavedJamiChangesForCurrentUser());
            for (UnsavedJamiChange change : changes){

                // very important to check that the ac is not null. Any new children event is not obsolete after saving the parent because not added yet
                // checks the unsaved change is not new and is not a children of the current object to save. If it is a children, the unsaved event of the children becomes obsolete because the parent object will be saved
                if (change.getAcsToDeleteOn().contains(object.getAc()) && change.getUnsavedObject().getAc() != null){
                    getUnsavedJamiChangesForCurrentUser().remove(change);
                }
                // we gave a list of parent acs for this object. It means that we want to remove all unsaved change event which refers to one of the parent acs
                else if (!parentAcs.isEmpty()){
                    // the save event concerns one of the parent of the current object being saved, we can remove this unsaved event as it will be saved with current object
                    if (change.getUnsavedObject().getAc() != null && parentAcs.contains(change.getUnsavedObject().getAc())){
                        getUnsavedJamiChangesForCurrentUser().remove(change);
                    }
                }
            }
        }
    }

    public void markAsHiddenChange(IntactObject object, AnnotatedObject parent, Collection<String> contextAcs) {
        String scope;

        if (parent != null && parent.getAc() != null){
            scope = parent.getAc();
        }
        else {
            scope = null;
        }
        UnsavedChange change = new UnsavedChange(object, UnsavedChange.CREATED_TRANSCRIPT, scope);
        change.getAcsToDeleteOn().addAll(contextAcs);
        addUnsavedHiddenChange(change);
    }

    public void markAsHiddenChange(IntactPrimaryObject object, IntactPrimaryObject parent, Collection<String> contextAcs) {
        String scope;

        if (parent != null && parent.getAc() != null){
            scope = parent.getAc();
        }
        else {
            scope = null;
        }
        CurateController curateController = (CurateController) getSpringContext().getBean("curateController");
        CurateController.CurateJamiMetadata meta = curateController.getJamiMetadata(object);
        IntactDbSynchronizer dbSynchronizer = meta.getDbSynchronizer();
        UnsavedJamiChange change = new UnsavedJamiChange(object, UnsavedChange.CREATED_TRANSCRIPT, scope, dbSynchronizer);
        change.getAcsToDeleteOn().addAll(contextAcs);
        addUnsavedHiddenJamiChange(change);
    }

    @Transactional("transactionManager")
    public void markToDeleteInteraction(Interaction interaction, Collection<Experiment> experiments) {
        Collection<Experiment> parents;

        if (IntactCore.isInitialized(experiments)) {
            parents = experiments;
        } else {
            parents = getDaoFactory().getInteractionDao().getByAc(interaction.getAc()).getExperiments();
        }

        // using an array to avoid a concurrent modification exception, which happens when trying to remove the interaction from its experiments
        final Experiment[] array = parents.toArray(new Experiment[parents.size()]);


        for (int i=0; i<array.length; i++) {
            if (interaction.getAc() != null) {

                String scope;
                // collect parent acs for this intact object if possible
                Collection<String> parentAcs = new ArrayList<String>();
                if (array[i] != null && array[i].getAc() != null){
                    scope = array[i].getAc();
                    parentAcs.add(scope);
                }
                else {
                    scope = null;
                }

                if (array[i].getPublication() != null && array[i].getPublication().getAc() != null){
                    parentAcs.add(array[i].getPublication().getAc());
                }
                // very important to delete all changes which can be affected by the delete of this object!!!
                removeObsoleteChangesOnDelete(interaction);

                UnsavedChange change = new UnsavedChange(interaction, UnsavedChange.DELETED, array[i], scope);
                change.getAcsToDeleteOn().addAll(parentAcs);

                addChange(change);

                // line commented because already done when adding the change
                //removeFromUnsaved(object);
            } else {
                AnnotatedObjectUtils.removeChild(array[i], interaction);
            }
        }
    }

    /**
     * When removing a save event from unsaved events, we have to refresh the unsaved events which have been saved while saving this specific change
     * @param io
     */
    public void removeFromUnsaved(IntactObject io, Collection<String> parentAcs) {
        List<UnsavedChange> changes = getUnsavedChangesForCurrentUser();

        changes.remove(new UnsavedChange(io, UnsavedChange.CREATED, null));
        changes.remove(new UnsavedChange(io, UnsavedChange.UPDATED, null));

        removeObsoleteChangesOnSave(io, parentAcs);
    }

    /**
     * When removing a save event from unsaved events, we have to refresh the unsaved events which have been saved while saving this specific change
     * @param io
     */
    public void removeFromUnsaved(IntactPrimaryObject io, Collection<String> parentAcs) {
        List<UnsavedJamiChange> changes = getUnsavedJamiChangesForCurrentUser();
        CurateController curateController = (CurateController) getSpringContext().getBean("curateController");
        CurateController.CurateJamiMetadata meta = curateController.getJamiMetadata(io);
        IntactDbSynchronizer dbSynchronizer = meta.getDbSynchronizer();

        changes.remove(new UnsavedJamiChange(io, UnsavedChange.CREATED, null, dbSynchronizer));
        changes.remove(new UnsavedJamiChange(io, UnsavedChange.UPDATED, null, dbSynchronizer));

        removeObsoleteChangesOnSave(io, parentAcs);
    }

    public void removeFromHiddenChanges(UnsavedChange unsavedChange) {
        getHiddenUnsavedChangesForCurrentUser().remove(unsavedChange);
    }

    public void removeFromHiddenChanges(UnsavedJamiChange unsavedChange) {
        getHiddenUnsavedJamiChangesForCurrentUser().remove(unsavedChange);
    }

    public void removeFromDeleted(UnsavedChange unsavedChange) {
        getUnsavedChangesForCurrentUser().remove(unsavedChange);
        removeObsoleteChangesOnDelete(unsavedChange.getUnsavedObject());
    }

    public void removeFromDeleted(UnsavedJamiChange unsavedChange) {
        getUnsavedJamiChangesForCurrentUser().remove(unsavedChange);
        removeObsoleteJamiChangesOnDelete(unsavedChange.getUnsavedObject());
    }

    public void removeFromDeleted(IntactObject object, AnnotatedObject parent) {
        String scope;

        if (parent != null && parent.getAc() != null){
            scope = parent.getAc();
        }
        else {
            scope = null;
        }

        getUnsavedChangesForCurrentUser().remove(new UnsavedChange(object, UnsavedChange.DELETED, parent, scope));
        removeObsoleteChangesOnDelete(object);
    }

    public void removeFromDeleted(IntactPrimaryObject object, IntactPrimaryObject parent) {
        String scope;

        if (parent != null && parent.getAc() != null){
            scope = parent.getAc();
        }
        else {
            scope = null;
        }
        CurateController curateController = (CurateController) getSpringContext().getBean("curateController");
        CurateController.CurateJamiMetadata meta = curateController.getJamiMetadata(parent);
        IntactDbSynchronizer dbSynchronizer = meta.getDbSynchronizer();

        getUnsavedJamiChangesForCurrentUser().remove(new UnsavedJamiChange(object, UnsavedChange.DELETED, parent, scope, dbSynchronizer));
        removeObsoleteJamiChangesOnDelete(object);
    }

    public void revert(AnnotatedObject io) {
        Iterator<UnsavedChange> iterator = getUnsavedChangesForCurrentUser().iterator();

        Collection<UnsavedChange> additionnalUnsavedEventToRevert = new ArrayList<UnsavedChange>(getUnsavedChangesForCurrentUser().size());

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedChange unsavedChange = iterator.next();

            // the object has an ac, we can compare using ac
            if (io.getAc() != null){
                // change concerning the object, we can remove it because the revert has been done
                if (io.getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    iterator.remove();
                }
                // change concerning the object but which need to be reverted because don't touch the object itself
                else if (io.getAc().equals(unsavedChange.getScope())){
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null) {

                if (unsavedChange.getUnsavedObject() instanceof AnnotatedObject){
                    AnnotatedObject unsavedAnnObj = (AnnotatedObject) unsavedChange.getUnsavedObject();

                    if (io.getShortLabel() != null && io.getShortLabel().equals(unsavedAnnObj.getShortLabel())){
                        iterator.remove();
                    }
                    else if (io.getShortLabel() == null && unsavedAnnObj.getShortLabel() == null && unsavedChange.getAcsToDeleteOn().isEmpty()){
                        iterator.remove();
                    }
                }
            }
        }

        // now revert additonal changes related to this revert
        if (!additionnalUnsavedEventToRevert.isEmpty()){
            CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

            for (UnsavedChange addChange : additionnalUnsavedEventToRevert){
                AnnotatedObjectController annotatedObjectController = curateController.getMetadata(addChange.getUnsavedObject()).getAnnotatedObjectController();
                annotatedObjectController.doRevertChanges(null);
            }
        }
    }

    public void revert(IntactPrimaryObject io) {
        Iterator<UnsavedJamiChange> iterator = getUnsavedJamiChangesForCurrentUser().iterator();

        Collection<UnsavedJamiChange> additionnalUnsavedEventToRevert = new ArrayList<UnsavedJamiChange>(getUnsavedJamiChangesForCurrentUser().size());

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedJamiChange unsavedChange = iterator.next();

            // the object has an ac, we can compare using ac
            if (io.getAc() != null){
                // change concerning the object, we can remove it because the revert has been done
                if (io.getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    iterator.remove();
                }
                // change concerning the object but which need to be reverted because don't touch the object itself
                else if (io.getAc().equals(unsavedChange.getScope())){
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null && unsavedChange.getUnsavedObject() == io) {

                iterator.remove();
            }
        }

        // now revert additonal changes related to this revert
        if (!additionnalUnsavedEventToRevert.isEmpty()){
            CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

            for (UnsavedJamiChange addChange : additionnalUnsavedEventToRevert){
                AnnotatedObjectController annotatedObjectController = curateController.getJamiMetadata(addChange.getUnsavedObject()).getObjController();
                annotatedObjectController.doRevertChanges(null);
            }
        }
    }

    public void revertInteraction(Interaction interaction, Collection<String> parentAcs) {
        Iterator<UnsavedChange> iterator = getUnsavedChangesForCurrentUser().iterator();
        Collection<UnsavedChange> additionnalUnsavedEventToRevert = new ArrayList<UnsavedChange>(getUnsavedChangesForCurrentUser().size());

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedChange unsavedChange = iterator.next();

            // the object has an ac, we can compare using ac
            if (interaction.getAc() != null){
                if (interaction.getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    iterator.remove();
                }
                // change concerning the object but which need to be reverted because don't touch the object itself
                else if (interaction.getAc().equals(unsavedChange.getScope())){
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
                else if (unsavedChange.getAcsToDeleteOn().contains(interaction.getAc())) {
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null) {
                if (unsavedChange.getUnsavedObject() instanceof Interaction){
                    Interaction unsavedInteraction = (Interaction) unsavedChange.getUnsavedObject();

                    if (interaction.getShortLabel() != null && interaction.getShortLabel().equals(unsavedInteraction.getShortLabel())){
                        iterator.remove();
                    }
                    // if both shortlabels are null, just checks parent acs
                    else if (interaction.getShortLabel() == null && unsavedInteraction.getShortLabel() == null) {
                        checkParentOfUnsavedObject(parentAcs, iterator, unsavedChange);
                    }
                }
            }
        }

        Iterator<UnsavedChange> iterator2 = getHiddenUnsavedChangesForCurrentUser().iterator();

        // removed the passed object from the list of hidden unsaved changes (transcripts created). If this object is the scope of another change as well, delete it
        while (iterator2.hasNext()) {
            UnsavedChange unsavedChange = iterator2.next();
            // the object has an ac, we can compare using ac
            if (interaction.getAc() != null){
                // if the protein transcript has been created when using component scope and the component ac is matching, delete it
                if (interaction.getAc().equals(unsavedChange.getScope())) {
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
                else if (unsavedChange.getAcsToDeleteOn().contains(interaction.getAc())) {
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
            }
        }

        // now revert additonal changes related to this revert
        if (!additionnalUnsavedEventToRevert.isEmpty()){
            CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

            for (UnsavedChange addChange : additionnalUnsavedEventToRevert){
                AnnotatedObjectController annotatedObjectController = curateController.getMetadata(addChange.getUnsavedObject()).getAnnotatedObjectController();
                annotatedObjectController.doRevertChanges(null);
            }
        }
    }

    public void revertComplex(psidev.psi.mi.jami.model.Complex interaction, Collection<String> parentAcs) {
        Iterator<UnsavedJamiChange> iterator = getUnsavedJamiChangesForCurrentUser().iterator();
        Collection<UnsavedJamiChange> additionnalUnsavedEventToRevert = new ArrayList<UnsavedJamiChange>(getUnsavedJamiChangesForCurrentUser().size());

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedJamiChange unsavedChange = iterator.next();

            // the object has an ac, we can compare using ac
            if (interaction instanceof IntactComplex && ((IntactComplex)interaction).getAc() != null){
                if (((IntactComplex)interaction).getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    iterator.remove();
                }
                // change concerning the object but which need to be reverted because don't touch the object itself
                else if (((IntactComplex)interaction).getAc().equals(unsavedChange.getScope())){
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
                else if (unsavedChange.getAcsToDeleteOn().contains(((IntactComplex)interaction).getAc())) {
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null) {
                if (unsavedChange.getUnsavedObject() instanceof IntactComplex){
                    IntactComplex unsavedInteraction = (IntactComplex) unsavedChange.getUnsavedObject();

                    if (interaction.getShortName() != null && interaction.getShortName().equals(unsavedInteraction.getShortName())){
                        iterator.remove();
                    }
                    // if both shortlabels are null, just checks parent acs
                    else if (interaction.getShortName() == null && unsavedInteraction.getShortName() == null) {
                        checkParentOfUnsavedObject(parentAcs, iterator, unsavedChange);
                    }
                }
            }
        }

        Iterator<UnsavedJamiChange> iterator2 = getHiddenUnsavedJamiChangesForCurrentUser().iterator();

        // removed the passed object from the list of hidden unsaved changes (transcripts created). If this object is the scope of another change as well, delete it
        while (iterator2.hasNext()) {
            UnsavedJamiChange unsavedChange = iterator2.next();
            // the object has an ac, we can compare using ac
            if (interaction instanceof IntactComplex && ((IntactComplex)interaction).getAc() != null){
                // if the protein transcript has been created when using component scope and the component ac is matching, delete it
                if (((IntactComplex)interaction).getAc().equals(unsavedChange.getScope())) {
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
                else if (unsavedChange.getAcsToDeleteOn().contains(((IntactComplex)interaction).getAc())) {
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
            }
        }

        // now revert additonal changes related to this revert
        if (!additionnalUnsavedEventToRevert.isEmpty()){
            CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

            for (UnsavedJamiChange addChange : additionnalUnsavedEventToRevert){
                AnnotatedObjectController annotatedObjectController = curateController.getJamiMetadata(addChange.getUnsavedObject()).getObjController();
                annotatedObjectController.doRevertChanges(null);
            }
        }
    }

    public void revertExperiment(Experiment experiment, Collection<String> parentAcs) {
        Iterator<UnsavedChange> iterator = getUnsavedChangesForCurrentUser().iterator();

        Collection<UnsavedChange> additionnalUnsavedEventToRevert = new ArrayList<UnsavedChange>(getUnsavedChangesForCurrentUser().size());

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedChange unsavedChange = iterator.next();

            // the object has an ac, we can compare using ac
            if (experiment.getAc() != null){
                if (experiment.getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    iterator.remove();
                }
                else if (experiment.getAc().equals(unsavedChange.getScope())){
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
                else if (unsavedChange.getAcsToDeleteOn().contains(experiment.getAc())) {
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null) {
                if (unsavedChange.getUnsavedObject() instanceof Experiment){
                    Experiment unsavedExperiment = (Experiment) unsavedChange.getUnsavedObject();

                    if (experiment.getShortLabel() != null && experiment.getShortLabel().equals(unsavedExperiment.getShortLabel())){
                        iterator.remove();
                    }
                    // if both shortlabels are null, just checks parent acs
                    else if (experiment.getShortLabel() == null && unsavedExperiment.getShortLabel() == null) {
                        checkParentOfUnsavedObject(parentAcs, iterator, unsavedChange);
                    }
                }
            }
        }

        // now revert additonal changes related to this revert
        if (!additionnalUnsavedEventToRevert.isEmpty()){
            CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

            for (UnsavedChange addChange : additionnalUnsavedEventToRevert){
                AnnotatedObjectController annotatedObjectController = curateController.getMetadata(addChange.getUnsavedObject()).getAnnotatedObjectController();
                annotatedObjectController.doRevertChanges(null);
            }
        }
    }

    public void revertPublication(Publication publication) {
        Iterator<UnsavedChange> iterator = getUnsavedChangesForCurrentUser().iterator();

        Collection<UnsavedChange> additionnalUnsavedEventToRevert = new ArrayList<UnsavedChange>(getUnsavedChangesForCurrentUser().size());

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedChange unsavedChange = iterator.next();

            // the object has an ac, we can compare using ac
            if (publication.getAc() != null){
                if (publication.getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    iterator.remove();
                }
                else if (publication.getAc().equals(unsavedChange.getScope())){
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
                else if (!unsavedChange.getAcsToDeleteOn().isEmpty()){
                    if (unsavedChange.getAcsToDeleteOn().contains(publication.getAc())) {
                        additionnalUnsavedEventToRevert.add(unsavedChange);
                        iterator.remove();
                    }
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null) {
                if (unsavedChange.getUnsavedObject() instanceof Publication){
                    Publication unsavedPublication = (Publication) unsavedChange.getUnsavedObject();

                    if (publication.getShortLabel() != null && publication.getShortLabel().equals(unsavedPublication.getShortLabel())){
                        iterator.remove();
                    }
                    // if both shortlabels are null, just checks parent acs
                    else if (publication.getShortLabel() == null && unsavedPublication.getShortLabel() == null) {
                        iterator.remove();
                    }
                }
            }
        }

        // now revert additonal changes related to this revert
        if (!additionnalUnsavedEventToRevert.isEmpty()){
            CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

            for (UnsavedChange addChange : additionnalUnsavedEventToRevert){
                AnnotatedObjectController annotatedObjectController = curateController.getMetadata(addChange.getUnsavedObject()).getAnnotatedObjectController();
                annotatedObjectController.doRevertChanges(null);
            }
        }
    }

    public void revertComponent(uk.ac.ebi.intact.model.Component component, Collection<String> parentAcs) {
        Iterator<UnsavedChange> iterator = getUnsavedChangesForCurrentUser().iterator();

        Collection<UnsavedChange> additionnalUnsavedEventToRevert = new ArrayList<UnsavedChange>(getUnsavedChangesForCurrentUser().size());

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedChange unsavedChange = iterator.next();
            // the object has an ac, we can compare using ac
            if (component.getAc() != null){
                if (component.getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    iterator.remove();
                }
                else if (component.getAc().equals(unsavedChange.getScope())){
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
                else if (unsavedChange.getAcsToDeleteOn().contains(component.getAc())) {
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null) {
                if (unsavedChange.getUnsavedObject() instanceof uk.ac.ebi.intact.model.Component){
                    uk.ac.ebi.intact.model.Component unsavedComponent = (uk.ac.ebi.intact.model.Component) unsavedChange.getUnsavedObject();

                    if (component.getShortLabel() != null && component.getShortLabel().equals(unsavedComponent.getShortLabel())){
                        iterator.remove();
                    }
                    // if both shortlabels are null, just checks parent acs
                    else if (component.getShortLabel() == null && unsavedComponent.getShortLabel() == null) {
                        checkParentOfUnsavedObject(parentAcs, iterator, unsavedChange);
                    }
                }
            }
        }

        Iterator<UnsavedChange> iterator2 = getHiddenUnsavedChangesForCurrentUser().iterator();

        // removed the passed object from the list of hidden unsaved changes (transcripts created). If this object is the scope of another change as well, delete it
        while (iterator2.hasNext()) {
            UnsavedChange unsavedChange = iterator2.next();
            // the object has an ac, we can compare using ac
            if (component.getAc() != null){
                // if the protein transcript has been created when using component scope and the component ac is matching, delete it
                if (component.getAc().equals(unsavedChange.getScope())) {
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
                else if (unsavedChange.getAcsToDeleteOn().contains(component.getAc())) {
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
            }
        }

        // now revert additonal changes related to this revert
        if (!additionnalUnsavedEventToRevert.isEmpty()){
            CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

            for (UnsavedChange addChange : additionnalUnsavedEventToRevert){
                AnnotatedObjectController annotatedObjectController = curateController.getMetadata(addChange.getUnsavedObject()).getAnnotatedObjectController();
                annotatedObjectController.doRevertChanges(null);
            }
        }
    }

    public void revertModelledParticipant(ModelledParticipant component, Collection<String> parentAcs) {
        Iterator<UnsavedJamiChange> iterator = getUnsavedJamiChangesForCurrentUser().iterator();

        Collection<UnsavedJamiChange> additionnalUnsavedEventToRevert = new ArrayList<UnsavedJamiChange>(getUnsavedJamiChangesForCurrentUser().size());

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedJamiChange unsavedChange = iterator.next();
            // the object has an ac, we can compare using ac
            if (component instanceof IntactModelledParticipant && ((IntactModelledParticipant)component).getAc() != null){
                if (((IntactModelledParticipant)component).getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    iterator.remove();
                }
                else if (((IntactModelledParticipant)component).getAc().equals(unsavedChange.getScope())){
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
                else if (unsavedChange.getAcsToDeleteOn().contains(((IntactModelledParticipant)component).getAc())) {
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null) {
                checkParentOfUnsavedObject(parentAcs, iterator, unsavedChange);
            }
        }

        Iterator<UnsavedJamiChange> iterator2 = getHiddenUnsavedJamiChangesForCurrentUser().iterator();

        // removed the passed object from the list of hidden unsaved changes (transcripts created). If this object is the scope of another change as well, delete it
        while (iterator2.hasNext()) {
            UnsavedJamiChange unsavedChange = iterator2.next();
            // the object has an ac, we can compare using ac
            if (component instanceof IntactModelledParticipant && ((IntactModelledParticipant)component).getAc() != null){
                // if the protein transcript has been created when using component scope and the component ac is matching, delete it
                if (((IntactModelledParticipant)component).getAc().equals(unsavedChange.getScope())) {
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
                else if (unsavedChange.getAcsToDeleteOn().contains(((IntactModelledParticipant)component).getAc())) {
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
            }
        }

        // now revert additonal changes related to this revert
        if (!additionnalUnsavedEventToRevert.isEmpty()){
            CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

            for (UnsavedJamiChange addChange : additionnalUnsavedEventToRevert){
                AnnotatedObjectController annotatedObjectController = curateController.getJamiMetadata(addChange.getUnsavedObject()).getObjController();
                annotatedObjectController.doRevertChanges(null);
            }
        }
    }

    public void revertFeature(Feature feature, Collection<String> parentAcs) {
        Iterator<UnsavedChange> iterator = getUnsavedChangesForCurrentUser().iterator();

        Collection<UnsavedChange> additionnalUnsavedEventToRevert = new ArrayList<UnsavedChange>(getUnsavedChangesForCurrentUser().size());

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedChange unsavedChange = iterator.next();
            // the object has an ac, we can compare using ac
            if (feature.getAc() != null){
                if (feature.getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    iterator.remove();
                }
                else if (feature.getAc().equals(unsavedChange.getScope())){
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
                else if (unsavedChange.getAcsToDeleteOn().contains(feature.getAc())) {
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null) {
                if (unsavedChange.getUnsavedObject() instanceof Feature){
                    Feature unsavedComponent = (Feature) unsavedChange.getUnsavedObject();

                    if (feature.getShortLabel() != null && feature.getShortLabel().equals(unsavedComponent.getShortLabel())){
                        iterator.remove();
                    }
                    // if both shortlabels are null, just checks parent acs
                    else if (feature.getShortLabel() == null && unsavedComponent.getShortLabel() == null) {
                        checkParentOfUnsavedObject(parentAcs, iterator, unsavedChange);
                    }
                }
            }
        }

        // now revert additonal changes related to this revert
        if (!additionnalUnsavedEventToRevert.isEmpty()){
            CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

            for (UnsavedChange addChange : additionnalUnsavedEventToRevert){
                AnnotatedObjectController annotatedObjectController = curateController.getMetadata(addChange.getUnsavedObject()).getAnnotatedObjectController();
                annotatedObjectController.doRevertChanges(null);
            }
        }
    }

    public void revertFeature(ModelledFeature feature, Collection<String> parentAcs) {
        Iterator<UnsavedJamiChange> iterator = getUnsavedJamiChangesForCurrentUser().iterator();

        Collection<UnsavedJamiChange> additionnalUnsavedEventToRevert = new ArrayList<UnsavedJamiChange>(getUnsavedJamiChangesForCurrentUser().size());

        // removed the passed object from the list of unsaved changes. If this object is the scope of another change as well, delete it
        while (iterator.hasNext()) {
            UnsavedJamiChange unsavedChange = iterator.next();
            // the object has an ac, we can compare using ac
            if (feature instanceof IntactModelledFeature && ((IntactModelledFeature)feature).getAc() != null){
                if (((IntactModelledFeature)feature).getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    iterator.remove();
                }
                else if (((IntactModelledFeature)feature).getAc().equals(unsavedChange.getScope())){
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
                else if (unsavedChange.getAcsToDeleteOn().contains(((IntactModelledFeature)feature).getAc())) {
                    additionnalUnsavedEventToRevert.add(unsavedChange);
                    iterator.remove();
                }
            }
            // the object is new, we can only checks if we have an unchanged event which is new and does not have a collection of parent acs (interactors, organism, cv terms)
            else if (unsavedChange.getUnsavedObject().getAc() == null) {
                checkParentOfUnsavedObject(parentAcs, iterator, unsavedChange);
            }
        }

        // now revert additonal changes related to this revert
        if (!additionnalUnsavedEventToRevert.isEmpty()){
            CurateController curateController = (CurateController) getSpringContext().getBean("curateController");

            for (UnsavedJamiChange addChange : additionnalUnsavedEventToRevert){
                AnnotatedObjectController annotatedObjectController = curateController.getJamiMetadata(addChange.getUnsavedObject()).getObjController();
                annotatedObjectController.doRevertChanges(null);
            }
        }
    }

    private void checkParentOfUnsavedObject(Collection<String> parentAcs, Iterator<UnsavedChange> iterator, UnsavedChange unsavedChange) {

        // both parent acs are not saved, revert it
        if (parentAcs.isEmpty() && unsavedChange.getAcsToDeleteOn().isEmpty()){
            iterator.remove();
        }
        // if one of the parents is saved, checks that the parent acs of the unsaved changes are in common so we don't revert changes not saved which concerns another publication
        else if (!parentAcs.isEmpty()){
            boolean haveSameParents = true;
            for (String parentAc : parentAcs) {

                if (!unsavedChange.getAcsToDeleteOn().contains(parentAc)){
                    haveSameParents = false;
                }
            }

            if (haveSameParents){
                iterator.remove();
            }
        }
    }

    private void checkParentOfUnsavedObject(Collection<String> parentAcs, Iterator<UnsavedJamiChange> iterator, UnsavedJamiChange unsavedChange) {

        // both parent acs are not saved, revert it
        if (parentAcs.isEmpty() && unsavedChange.getAcsToDeleteOn().isEmpty()){
            iterator.remove();
        }
        // if one of the parents is saved, checks that the parent acs of the unsaved changes are in common so we don't revert changes not saved which concerns another publication
        else if (!parentAcs.isEmpty()){
            boolean haveSameParents = true;
            for (String parentAc : parentAcs) {

                if (!unsavedChange.getAcsToDeleteOn().contains(parentAc)){
                    haveSameParents = false;
                }
            }

            if (haveSameParents){
                iterator.remove();
            }
        }
    }

    public boolean isUnsaved(IntactObject io) {
        if (io == null) return false;
        if (io.getAc() == null) return true;

        return isUnsavedAc(io.getAc());
    }

    public boolean isUnsaved(IntactPrimaryObject io) {
        if (io == null) return false;
        if (io.getAc() == null) return true;

        return isUnsavedAc(io.getAc());
    }

    public boolean isUnsavedOrDeleted(IntactObject io) {
        if (isUnsaved(io)) {
            return true;
        } else if (io.getAc() != null && isDeletedAc(io.getAc())) {
            return true;
        }

        return false;
    }

    public boolean isUnsavedOrDeleted(IntactPrimaryObject io) {
        if (isUnsaved(io)) {
            return true;
        } else if (io.getAc() != null && isDeletedAc(io.getAc())) {
            return true;
        }

        return false;
    }

    public boolean isUnsavedAc(String ac) {
        if (ac == null) return true;

        for (UnsavedChange unsavedChange : getUnsavedChangesForCurrentUser()) {
            // if one unsaved event exists and matches the ac of the current object or the scope of the unsaved event matches the current object, this event can be considered as unsaved
            if (ac.equals(unsavedChange.getUnsavedObject().getAc()) || ac.equals(unsavedChange.getScope())) {
                return true;
            }
            // if the current ac is the parent ac of one of the elements to save, mark it as unsaved
            else if (!unsavedChange.getAcsToDeleteOn().isEmpty()){
                if (unsavedChange.getAcsToDeleteOn().contains(ac)){
                    return true;
                }
            }
        }

        for (UnsavedJamiChange unsavedChange : getUnsavedJamiChangesForCurrentUser()) {
            // if one unsaved event exists and matches the ac of the current object or the scope of the unsaved event matches the current object, this event can be considered as unsaved
            if (ac.equals(unsavedChange.getUnsavedObject().getAc()) || ac.equals(unsavedChange.getScope())) {
                return true;
            }
            // if the current ac is the parent ac of one of the elements to save, mark it as unsaved
            else if (!unsavedChange.getAcsToDeleteOn().isEmpty()){
                if (unsavedChange.getAcsToDeleteOn().contains(ac)){
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isDeletedAc(String ac) {
        if (ac == null) return true;

        for (UnsavedChange unsavedChange : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.DELETED.equals(unsavedChange.getAction()) &&
                    ac.equals(unsavedChange.getUnsavedObject().getAc())) {
                return true;
            }
        }

        for (UnsavedJamiChange unsavedChange : getUnsavedJamiChangesForCurrentUser()) {
            if (UnsavedChange.DELETED.equals(unsavedChange.getAction()) &&
                    ac.equals(unsavedChange.getUnsavedObject().getAc())) {
                return true;
            }
        }

        return false;
    }

    public List<String> getDeletedAcs(Class type, String parentAc) {
        return DebugUtil.acList(getDeleted(type, parentAc));
    }

    public List<String> getDeletedAcsByClassName(String className, String parentAc) {
        try {
            return getDeletedAcs(Thread.currentThread().getContextClassLoader().loadClass(className), parentAc);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    public List<IntactObject> getDeleted(Class type, String parentAc) {
        List<IntactObject> ios = new ArrayList<IntactObject>();

        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.DELETED.equals(change.getAction()) &&
                    type.isAssignableFrom(change.getUnsavedObject().getClass())) {

                if (change.getParentObject() != null && change.getParentObject().getAc() != null){
                    if (change.getParentObject().getAc().equals(parentAc)){
                        IntactObject intactObject = change.getUnsavedObject();
                        ios.add(intactObject);
                    }
                }
                else if (change.getScope() != null && change.getScope().equals(parentAc)){
                    IntactObject intactObject = change.getUnsavedObject();
                    ios.add(intactObject);
                }
            }
        }

        return ios;
    }

    public List<IntactObject> getAllUnsaved() {
        List<IntactObject> ios = new ArrayList<IntactObject>();

        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.UPDATED.equals(change.getAction()) || UnsavedChange.CREATED.equals(change.getAction())) {
                IntactObject intactObject = change.getUnsavedObject();
                ios.add(intactObject);
            }
        }

        return ios;
    }

    public List<IntactPrimaryObject> getAllJamiUnsaved() {
        List<IntactPrimaryObject> ios = new ArrayList<IntactPrimaryObject>();

        for (UnsavedJamiChange change : getUnsavedJamiChangesForCurrentUser()) {
            if (UnsavedChange.UPDATED.equals(change.getAction()) || UnsavedChange.CREATED.equals(change.getAction())) {
                IntactPrimaryObject intactObject = change.getUnsavedObject();
                ios.add(intactObject);
            }
        }

        return ios;
    }

    public List<UnsavedChange> getAllUnsavedChanges() {
        List<UnsavedChange> ios = new ArrayList<UnsavedChange>();

        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.UPDATED.equals(change.getAction()) || UnsavedChange.CREATED.equals(change.getAction())) {
                ios.add(change);
            }
        }

        return ios;
    }

    public List<UnsavedJamiChange> getAllUnsavedJamiChanges() {
        List<UnsavedJamiChange> ios = new ArrayList<UnsavedJamiChange>();

        for (UnsavedJamiChange change : getUnsavedJamiChangesForCurrentUser()) {
            if (UnsavedChange.UPDATED.equals(change.getAction()) || UnsavedChange.CREATED.equals(change.getAction())) {
                ios.add(change);
            }
        }

        return ios;
    }

    public List<IntactObject> getAllDeleted() {
        List<IntactObject> ios = new ArrayList<IntactObject>();

        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.DELETED.equals(change.getAction())) {
                IntactObject intactObject = change.getUnsavedObject();
                ios.add(intactObject);
            }
        }

        return ios;
    }

    public List<IntactPrimaryObject> getAllJamiDeleted() {
        List<IntactPrimaryObject> ios = new ArrayList<IntactPrimaryObject>();

        for (UnsavedJamiChange change : getUnsavedJamiChangesForCurrentUser()) {
            if (UnsavedChange.DELETED.equals(change.getAction())) {
                IntactPrimaryObject intactObject = change.getUnsavedObject();
                ios.add(intactObject);
            }
        }

        return ios;
    }

    public List<UnsavedChange> getAllUnsavedDeleted() {
        List<UnsavedChange> unsaved = new ArrayList<UnsavedChange>();

        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.DELETED.equals(change.getAction())) {
                unsaved.add(change);
            }
        }

        return unsaved;
    }

    public List<UnsavedJamiChange> getAllUnsavedJamiDeleted() {
        List<UnsavedJamiChange> unsaved = new ArrayList<UnsavedJamiChange>();

        for (UnsavedJamiChange change : getUnsavedJamiChangesForCurrentUser()) {
            if (UnsavedJamiChange.DELETED.equals(change.getAction())) {
                unsaved.add(change);
            }
        }

        return unsaved;
    }

    public List<UnsavedChange> getAllUnsavedProteinTranscripts() {
        List<UnsavedChange> unsaved = new ArrayList<UnsavedChange>();

        for (UnsavedChange change : getHiddenUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.CREATED_TRANSCRIPT.equals(change.getAction())) {
                unsaved.add(change);
            }
        }

        return unsaved;
    }

    public List<UnsavedJamiChange> getAllUnsavedJamiProteinTranscripts() {
        List<UnsavedJamiChange> unsaved = new ArrayList<UnsavedJamiChange>();

        for (UnsavedJamiChange change : getHiddenUnsavedJamiChangesForCurrentUser()) {
            if (UnsavedChange.CREATED_TRANSCRIPT.equals(change.getAction())) {
                unsaved.add(change);
            }
        }

        return unsaved;
    }

    public List<IntactObject> getAllCreatedProteinTranscripts() {
        List<IntactObject> ios = new ArrayList<IntactObject>();

        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.CREATED_TRANSCRIPT.equals(change.getAction())) {
                IntactObject intactObject = change.getUnsavedObject();
                ios.add(intactObject);
            }
        }

        return ios;
    }

    public IntactObject findByAc(String ac) {
        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (ac.equals(change.getUnsavedObject().getAc())) {
                return change.getUnsavedObject();
            }
        }

        return null;
    }

    public IntactPrimaryObject findJamiByAc(String ac) {
        for (UnsavedJamiChange change : getUnsavedJamiChangesForCurrentUser()) {
            if (ac.equals(change.getUnsavedObject().getAc())) {
                return change.getUnsavedObject();
            }
        }

        return null;
    }

    public List<String> getUsernames() {
        return new ArrayList<String>(changesPerUser.keySet());
    }

    public boolean isObjectBeingEdited(IntactObject io, boolean includeMyself) {
        if (io.getAc() == null) return false;

        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");
        String me = userSessionController.getCurrentUser().getLogin();

        for (String user : getUsernames()) {
            if (!includeMyself && user.equals(me)) continue;

            for (UnsavedChange unsavedChange : getUnsavedChangesForUser(user)) {
                if (io.getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isObjectBeingEdited(IntactPrimaryObject io, boolean includeMyself) {
        if (io.getAc() == null) return false;

        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");
        String me = userSessionController.getCurrentUser().getLogin();

        for (String user : getUsernames()) {
            if (!includeMyself && user.equals(me)) continue;

            for (UnsavedJamiChange unsavedChange : getUnsavedJamiChangesForUser(user)) {
                if (io.getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    return true;
                }
            }
        }

        return false;
    }

    //TODO: probably this should be a list
    public String whoIsEditingObject(IntactObject io) {
        if (io.getAc() == null) return null;

        for (String user : getUsernames()) {
            for (UnsavedChange unsavedChange : getUnsavedChangesForUser(user)) {
                if (io.getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    return user;
                }
            }
        }

        return null;
    }

    public String whoIsEditingObject(IntactPrimaryObject io) {
        if (io.getAc() == null) return null;

        for (String user : getUsernames()) {
            for (UnsavedJamiChange unsavedChange : getUnsavedJamiChangesForUser(user)) {
                if (io.getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                    return user;
                }
            }
        }

        return null;
    }

    public void clearCurrentUserChanges() {
        getUnsavedChangesForCurrentUser().clear();
        getHiddenUnsavedChangesForCurrentUser().clear();
    }

    public int getNumberUnsavedChangedForCurrentUser(){
        return getUnsavedChangesForCurrentUser().size()+getUnsavedJamiChangesForCurrentUser().size();
    }

    public List<UnsavedChange> getUnsavedChangesForCurrentUser() {
        return getUnsavedChangesForUser(getCurrentUser().getLogin());
    }

    public List<UnsavedChange> getHiddenUnsavedChangesForCurrentUser() {
        return getHiddenUnsavedChangesForUser(getCurrentUser().getLogin());
    }

    public List<UnsavedJamiChange> getUnsavedJamiChangesForCurrentUser() {
        return getUnsavedJamiChangesForUser(getCurrentUser().getLogin());
    }

    public List<UnsavedJamiChange> getHiddenUnsavedJamiChangesForCurrentUser() {
        return getHiddenUnsavedJamiChangesForUser(getCurrentUser().getLogin());
    }

    public List<UnsavedChange> getUnsavedChangesForUser(String userId) {
        List<UnsavedChange> unsavedChanges;

        if (changesPerUser.containsKey(userId)) {
            unsavedChanges = changesPerUser.get(userId);
        } else {
            unsavedChanges = new ArrayList<UnsavedChange>();
            changesPerUser.put(userId, unsavedChanges);
        }

        return unsavedChanges;
    }

    public List<UnsavedChange> getHiddenUnsavedChangesForUser(String userId) {
        List<UnsavedChange> unsavedChanges;

        if (hiddenChangesPerUser.containsKey(userId)) {
            unsavedChanges = hiddenChangesPerUser.get(userId);
        } else {
            unsavedChanges = new ArrayList<UnsavedChange>();
            hiddenChangesPerUser.put(userId, unsavedChanges);
        }

        return unsavedChanges;
    }

    public List<UnsavedJamiChange> getUnsavedJamiChangesForUser(String userId) {
        List<UnsavedJamiChange> unsavedChanges;

        if (jamiChangesPerUser.containsKey(userId)) {
            unsavedChanges = jamiChangesPerUser.get(userId);
        } else {
            unsavedChanges = new ArrayList<UnsavedJamiChange>();
            jamiChangesPerUser.put(userId, unsavedChanges);
        }

        return unsavedChanges;
    }

    public List<UnsavedJamiChange> getHiddenUnsavedJamiChangesForUser(String userId) {
        List<UnsavedJamiChange> unsavedChanges;

        if (hiddenJamiChangesPerUser.containsKey(userId)) {
            unsavedChanges = hiddenJamiChangesPerUser.get(userId);
        } else {
            unsavedChanges = new ArrayList<UnsavedJamiChange>();
            hiddenJamiChangesPerUser.put(userId, unsavedChanges);
        }

        return unsavedChanges;
    }

    private void addChange(UnsavedChange unsavedChange) {
        List<UnsavedChange> unsavedChanges = getUnsavedChangesForCurrentUser();

        unsavedChanges.remove(unsavedChange);
        unsavedChanges.add(unsavedChange);
    }

    private void addJamiChange(UnsavedJamiChange unsavedChange) {
        List<UnsavedJamiChange> unsavedChanges = getUnsavedJamiChangesForCurrentUser();

        unsavedChanges.remove(unsavedChange);
        unsavedChanges.add(unsavedChange);
    }

    private boolean addUnsavedChange(UnsavedChange unsavedChange) {

        // check first if the current object will not be deleted
        List<UnsavedChange> deletedChanges = getAllUnsavedDeleted();

        for (UnsavedChange deleteChange : deletedChanges){

            // if one deleted event is in conflict with the current save event (one of the parents of the current object will be deleted), don't add an update event (if experiment is deleted, new changes on the interaction does not make any sense)
            if (unsavedChange.getAcsToDeleteOn().contains(deleteChange.getUnsavedObject().getAc())){
                if (deleteChange.getUnsavedObject() instanceof AnnotatedObject){
                    addWarningMessage("Save not allowed", "This object cannot be updated because it will be deleted when deleting " + DebugUtil.annotatedObjectToString((AnnotatedObject) deleteChange.getUnsavedObject(), false));
                }
                else {
                    addWarningMessage("Save not allowed", "This object will be deleted when deleting the intact object AC = " + deleteChange.getUnsavedObject().getAc());
                }
                return false;
            }
            // the current object will be deleted itself
            else if (unsavedChange.getUnsavedObject().getAc() != null && unsavedChange.getUnsavedObject().getAc().equals(deleteChange.getUnsavedObject().getAc())){
                if (deleteChange.getUnsavedObject() instanceof AnnotatedObject){
                    addWarningMessage("Save not allowed", "This object cannot be updated because it will be deleted when deleting " + DebugUtil.annotatedObjectToString((AnnotatedObject) deleteChange.getUnsavedObject(), false));
                }
                else {
                    addWarningMessage("Save not allowed", "This object will be deleted when deleting the intact object AC = " + deleteChange.getUnsavedObject().getAc());
                }
                return false;
            }
        }

        // the current object will not be deleted (or any of its parents), we can remove safely the current changes if it exists and replace it with the new one
        List<UnsavedChange> unsavedChanges = getUnsavedChangesForCurrentUser();

        unsavedChanges.remove(unsavedChange);

        unsavedChanges.add(unsavedChange);
        return true;
    }

    private boolean addUnsavedJamiChange(UnsavedJamiChange unsavedChange) {

        // check first if the current object will not be deleted
        List<UnsavedJamiChange> deletedChanges = getAllUnsavedJamiDeleted();

        for (UnsavedJamiChange deleteChange : deletedChanges){

            // if one deleted event is in conflict with the current save event (one of the parents of the current object will be deleted), don't add an update event (if experiment is deleted, new changes on the interaction does not make any sense)
            if (unsavedChange.getAcsToDeleteOn().contains(deleteChange.getUnsavedObject().getAc())){
                if (deleteChange.getUnsavedObject() instanceof IntactPrimaryObject){
                    addWarningMessage("Save not allowed", "This object cannot be updated because it will be deleted when deleting " + deleteChange.getUnsavedObject().toString());
                }
                else {
                    addWarningMessage("Save not allowed", "This object will be deleted when deleting the intact object AC = " + deleteChange.getUnsavedObject().getAc());
                }
                return false;
            }
            // the current object will be deleted itself
            if (unsavedChange.getUnsavedObject().getAc() != null && unsavedChange.getUnsavedObject().getAc().equals(deleteChange.getUnsavedObject().getAc())){
                if (deleteChange.getUnsavedObject() instanceof AnnotatedObject){
                    addWarningMessage("Save not allowed", "This object cannot be updated because it will be deleted when deleting " + DebugUtil.annotatedObjectToString((AnnotatedObject) deleteChange.getUnsavedObject(), false));
                }
                else {
                    addWarningMessage("Save not allowed", "This object will be deleted when deleting the intact object AC = " + deleteChange.getUnsavedObject().getAc());
                }
                return false;
            }
        }

        // the current object will not be deleted (or any of its parents), we can remove safely the current changes if it exists and replace it with the new one
        List<UnsavedJamiChange> unsavedChanges = getUnsavedJamiChangesForCurrentUser();
        unsavedChanges.remove(unsavedChange);
        unsavedChanges.add(unsavedChange);
        return true;
    }

    private boolean addUnsavedHiddenChange(UnsavedChange unsavedChange) {

        List<UnsavedChange> deletedChanges = getAllUnsavedDeleted();

        for (UnsavedChange deleteChange : deletedChanges){

            // if one deleted event is in conflict with the current save event, don't add an update event (if experiment is deleted, new changes on the interaction does not make any sense)
            if (unsavedChange.getAcsToDeleteOn().contains(deleteChange.getUnsavedObject().getAc())){
                if (deleteChange.getUnsavedObject() instanceof AnnotatedObject){
                    addWarningMessage("Save not allowed", "This object cannot be updated because it will be deleted when deleting " + DebugUtil.annotatedObjectToString((AnnotatedObject) deleteChange.getUnsavedObject(), false));
                }
                else {
                    addWarningMessage("Save not allowed", "This object will be deleted when deleting the intact object AC = " + deleteChange.getUnsavedObject().getAc());
                }
                return false;
            }
            // the current object will be deleted itself
            else if (unsavedChange.getUnsavedObject().getAc() != null && unsavedChange.getUnsavedObject().getAc().equals(deleteChange.getUnsavedObject().getAc())){
                if (deleteChange.getUnsavedObject() instanceof AnnotatedObject){
                    addWarningMessage("Save not allowed", "This object cannot be updated because it will be deleted when deleting " + DebugUtil.annotatedObjectToString((AnnotatedObject) deleteChange.getUnsavedObject(), false));
                }
                else {
                    addWarningMessage("Save not allowed", "This object will be deleted when deleting the intact object AC = " + deleteChange.getUnsavedObject().getAc());
                }
                return false;
            }
        }

        List<UnsavedChange> unsavedChanges = getHiddenUnsavedChangesForCurrentUser();
        unsavedChanges.remove(unsavedChange);
        unsavedChanges.add(unsavedChange);
        return true;
    }

    private boolean addUnsavedHiddenJamiChange(UnsavedJamiChange unsavedChange) {

        List<UnsavedJamiChange> deletedChanges = getAllUnsavedJamiDeleted();

        for (UnsavedJamiChange deleteChange : deletedChanges){

            // if one deleted event is in conflict with the current save event, don't add an update event (if experiment is deleted, new changes on the interaction does not make any sense)
            if (unsavedChange.getAcsToDeleteOn().contains(deleteChange.getUnsavedObject().getAc())){
                if (deleteChange.getUnsavedObject() instanceof IntactPrimaryObject){
                    addWarningMessage("Save not allowed", "This object cannot be updated because it will be deleted when deleting " +  deleteChange.getUnsavedObject().toString());
                }
                else {
                    addWarningMessage("Save not allowed", "This object will be deleted when deleting the intact object AC = " + deleteChange.getUnsavedObject().getAc());
                }
                return false;
            }
            // the current object will be deleted itself
            else if (unsavedChange.getUnsavedObject().getAc() != null && unsavedChange.getUnsavedObject().getAc().equals(deleteChange.getUnsavedObject().getAc())){
                if (deleteChange.getUnsavedObject() instanceof IntactPrimaryObject){
                    addWarningMessage("Save not allowed", "This object cannot be updated because it will be deleted when deleting " + deleteChange.getUnsavedObject().toString());
                }
                else {
                    addWarningMessage("Save not allowed", "This object will be deleted when deleting the intact object AC = " + deleteChange.getUnsavedObject().getAc());
                }
                return false;
            }
        }

        List<UnsavedJamiChange> unsavedChanges = getHiddenUnsavedJamiChangesForCurrentUser();
        unsavedChanges.remove(unsavedChange);
        unsavedChanges.add(unsavedChange);
        return true;
    }

    private void removeUserFromUnsaved(String user) {
        changesPerUser.remove(user);
        jamiChangesPerUser.remove(user);
    }

    private void removeUserFromHiddenUnsaved(String user) {
        hiddenChangesPerUser.remove(user);
        hiddenJamiChangesPerUser.remove(user);
    }
}
