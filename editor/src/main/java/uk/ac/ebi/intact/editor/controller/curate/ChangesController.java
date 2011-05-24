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
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.UserListener;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.IntactObject;
import uk.ac.ebi.intact.model.Interaction;
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

    public ChangesController() {
        changesPerUser = new HashMap<String, List<UnsavedChange>>();
    }

    @Override
    public void userLoggedIn(User user) {
        if (user == null) return;

        changesPerUser.put(user.getLogin(), new ArrayList<UnsavedChange>());
    }

    @Override
    public void userLoggedOut(User user) {
        userLoggedOut(user.getLogin());
    }

    private void userLoggedOut(String user) {
        final List<UnsavedChange> unsavedChanges = getUnsavedChangesForUser(user);

        if (unsavedChanges == null) {
            throw new IllegalStateException("No unsaved changes found for user: "+user);
        }

        if (!unsavedChanges.isEmpty()) {
            if (log.isInfoEnabled()) log.info("User logged out with "+unsavedChanges.size()+" pending changes: "+user);

            unsavedChanges.clear();
        }

        removeUserFromUnsaved(user);
    }

    public void markAsUnsaved(IntactObject io) {
        if (io == null) return;

        if (io.getAc() != null) {
            addChange(new UnsavedChange(io, UnsavedChange.UPDATED));
        } else {
            addChange(new UnsavedChange(io, UnsavedChange.CREATED));
        }
    }



    public void markToDelete(IntactObject object, AnnotatedObject parent) {
        if (object.getAc() != null) {
            addChange(new UnsavedChange(object, UnsavedChange.DELETED, parent));
            removeFromUnsaved(object);
        } else {
            AnnotatedObjectUtils.removeChild(parent, object);
        }

        if (parent != null && parent.getAc() != null) {
            addChange(new UnsavedChange(parent, UnsavedChange.UPDATED));
        }
    }

    public void markToCreatedTranscriptWithoutMaster(IntactObject object) {
        addChange(new UnsavedChange(object, UnsavedChange.CREATED_TRANSCRIPT));
    }

    @Transactional
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
            markToDelete(interaction, array[i]);
        }
    }

    public void removeFromUnsaved(IntactObject io) {
        List<UnsavedChange> changes = getUnsavedChangesForCurrentUser();

        changes.remove(new UnsavedChange(io, UnsavedChange.CREATED));
        changes.remove(new UnsavedChange(io, UnsavedChange.UPDATED));
    }

    public void removeFromCreatedTranscriptWithoutProtein(UnsavedChange unsavedChange) {
        getUnsavedChangesForCurrentUser().remove(unsavedChange);
    }

    public void removeFromDeleted(UnsavedChange unsavedChange) {
        getUnsavedChangesForCurrentUser().remove(unsavedChange);
    }

    public void removeFromDeleted(IntactObject object, AnnotatedObject parent) {
        getUnsavedChangesForCurrentUser().remove(new UnsavedChange(object, UnsavedChange.DELETED, parent));
    }

    public void revert(IntactObject io) {
        Iterator<UnsavedChange> iterator = getUnsavedChangesForCurrentUser().iterator();

        // removed the passed object from the list of unsaved changes
        while (iterator.hasNext()) {
            UnsavedChange unsavedChange = iterator.next();
            if (io.getAc() != null && io.getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                iterator.remove();
            }
        }
    }

    public boolean isUnsaved(IntactObject io) {
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

    public boolean isUnsavedAc(String ac) {
        if (ac == null) return true;

        for (UnsavedChange unsavedChange : getUnsavedChangesForCurrentUser()) {
            if (ac.equals(unsavedChange.getUnsavedObject().getAc())) {
                return true;
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

        return false;
    }

    public List<String> getDeletedAcs(Class type) {
        return DebugUtil.acList(getDeleted(type));
    }

    public List<String> getDeletedAcsByClassName(String className) {
        try {
            return getDeletedAcs(Thread.currentThread().getContextClassLoader().loadClass(className));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    public List<IntactObject> getDeleted(Class type) {
        List<IntactObject> ios = new ArrayList<IntactObject>();

        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.DELETED.equals(change.getAction()) &&
                    type.isAssignableFrom(change.getUnsavedObject().getClass())) {
                IntactObject intactObject = change.getUnsavedObject();
                ios.add(intactObject);
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

    public List<UnsavedChange> getAllUnsavedDeleted() {
        List<UnsavedChange> unsaved = new ArrayList<UnsavedChange>();

        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
            if (UnsavedChange.DELETED.equals(change.getAction())) {
                unsaved.add(change);
            }
        }

        return unsaved;
    }

    public List<UnsavedChange> getAllUnsavedProteinTranscripts() {
        List<UnsavedChange> unsaved = new ArrayList<UnsavedChange>();

        for (UnsavedChange change : getUnsavedChangesForCurrentUser()) {
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

    public void clearCurrentUserChanges() {
        getUnsavedChangesForCurrentUser().clear();
    }

    private User getCurrentUser() {
        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");
        return userSessionController.getCurrentUser();
    }

    public List<UnsavedChange> getUnsavedChangesForCurrentUser() {
        return getUnsavedChangesForUser(getCurrentUser().getLogin());
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

    private void addChange(UnsavedChange unsavedChange) {
        List<UnsavedChange> unsavedChanges = getUnsavedChangesForCurrentUser();

        unsavedChanges.remove(unsavedChange);
        unsavedChanges.add(unsavedChange);
    }

    private void removeUserFromUnsaved(String user) {
        changesPerUser.remove(user);
    }
}
