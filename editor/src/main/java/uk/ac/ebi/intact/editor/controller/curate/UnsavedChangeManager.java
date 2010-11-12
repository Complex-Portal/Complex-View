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

import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.IntactObject;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class UnsavedChangeManager {

    private Set<UnsavedChange> changes;
    private boolean unsavedChanges;

    public UnsavedChangeManager() {
        changes = new HashSet<UnsavedChange>();
    }

    public void markToDelete(IntactObject object, AnnotatedObject parent) {
        if (object.getAc() != null) {
            unsavedChanges = true;
            changes.add(new UnsavedChange(object, UnsavedChange.DELETED, parent));

            removeFromUnsaved(object);
        } else {
            AnnotatedObjectUtils.removeChild(parent, object);
        }
    }

    public void removeFromDeleted(IntactObject object, AnnotatedObject parent) {
        changes.remove(new UnsavedChange(object, UnsavedChange.DELETED, parent));
    }

    public void markAsUnsaved(IntactObject object) {
        if (object == null) return;
        
        unsavedChanges = true;

        if (object.getAc() != null) {
            changes.add(new UnsavedChange(object, UnsavedChange.UPDATED));
        } else {
            changes.add(new UnsavedChange(object, UnsavedChange.CREATED));
        }
    }

    public void removeFromUnsaved(IntactObject object) {
        changes.remove(new UnsavedChange(object, UnsavedChange.CREATED));
        changes.remove(new UnsavedChange(object, UnsavedChange.UPDATED));
    }

    public void revert(IntactObject object) {
        Iterator<UnsavedChange> iterator = changes.iterator();

        // removed the passed object from the list of unsaved changes
        while (iterator.hasNext()) {
            UnsavedChange unsavedChange = iterator.next();
            if (object.getAc() != null && object.getAc().equals(unsavedChange.getUnsavedObject().getAc())) {
                iterator.remove();
            }
        }
    }

    public boolean isDeletedAc(String ac) {
        return getDeletedAcs(IntactObject.class).contains(ac);
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

        for (UnsavedChange change : changes) {
            if (UnsavedChange.DELETED.equals(change.getAction()) &&
                    type.isAssignableFrom(change.getUnsavedObject().getClass())) {
                IntactObject intactObject = change.getUnsavedObject();
                ios.add(intactObject);
            }
        }

        return ios;
    }

    public List<IntactObject> getAllDeleted() {
        List<IntactObject> ios = new ArrayList<IntactObject>();

        for (UnsavedChange change : changes) {
            if (UnsavedChange.DELETED.equals(change.getAction())) {
                IntactObject intactObject = change.getUnsavedObject();
                ios.add(intactObject);
            }
        }

        return ios;
    }
    
    public List<UnsavedChange> getAllUnsavedDeleted() {
        List<UnsavedChange> unsaved = new ArrayList<UnsavedChange>();

        for (UnsavedChange change : changes) {
            if (UnsavedChange.DELETED.equals(change.getAction())) {
                unsaved.add(change);
            }
        }

        return unsaved;
    }

    public List<IntactObject> getAllUnsaved() {
        List<IntactObject> ios = new ArrayList<IntactObject>();

        for (UnsavedChange change : changes) {
            if (UnsavedChange.UPDATED.equals(change.getAction()) || UnsavedChange.CREATED.equals(change.getAction())) {
                IntactObject intactObject = change.getUnsavedObject();
                ios.add(intactObject);
            }
        }

        return ios;
    }

    public Collection<UnsavedChange> getChanges() {
        return changes;
    }

    public void clearChanges() {
        changes.clear();
    }

    public boolean isUnsavedChanges() {
        return !changes.isEmpty();
    }

    public boolean constainsAc(String ac) {
        for (UnsavedChange change : getChanges()) {
            if (ac.equals(change.getUnsavedObject().getAc())) {
                return true;
            }
        }
        return false;
    }
}
