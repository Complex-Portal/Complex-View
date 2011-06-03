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

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class UnsavedChange {

    public static final String DELETED = "deleted";
    public static final String CREATED = "created";
    public static final String UPDATED = "updated";
    public static final String CREATED_TRANSCRIPT = "created-transcript";

    private IntactObject unsavedObject;
    private AnnotatedObject parentObject;
    private String action;
    private Collection<String> acsToDeleteOn = new ArrayList<String>();

    /**
     * This is the global scope of change in case we delete an object from its parent. We know during which update we can save this change
     * This scope is not affecting equals and hashcode. It is just a tag to be able to save deleted event or created transcript events
     */
    private String scope;

    public UnsavedChange(IntactObject unsavedObject, String action, String scope) {
        this.unsavedObject = unsavedObject;
        this.action = action;
        this.scope = scope;
    }

    public UnsavedChange(IntactObject unsavedObject, String action, AnnotatedObject parentObject, String scope) {
        this(unsavedObject, action, scope);
        this.parentObject = parentObject;
        this.scope = scope;
    }

    public String getAction() {
        return action;
    }

    public IntactObject getUnsavedObject() {
        return unsavedObject;
    }

    public AnnotatedObject getParentObject() {
        return parentObject;
    }

    public String getDescription(IntactObject intactObject) {
        if (intactObject instanceof AnnotatedObject) {
            return ((AnnotatedObject)intactObject).getShortLabel();
        }
        
        return DebugUtil.intactObjectToString(intactObject, true);
    }

    public Collection<String> getAcsToDeleteOn() {
        return acsToDeleteOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnsavedChange that = (UnsavedChange) o;

        if (action != null ? !action.equals(that.action) : that.action != null) return false;
        if (unsavedObject != null && unsavedObject.getAc() != null && that.unsavedObject != null && that.unsavedObject.getAc() != null) {
            return unsavedObject.getAc().equals(that.unsavedObject.getAc());
        }

        if (unsavedObject != null ? System.identityHashCode(unsavedObject) != System.identityHashCode(that.unsavedObject) : that.unsavedObject != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = action != null ? action.hashCode() : 0;

        if (unsavedObject != null && unsavedObject.getAc() != null) {
            return 31 * (result + unsavedObject.getAc().hashCode());
        }

        result = 31 * result + (unsavedObject != null ? System.identityHashCode(unsavedObject) : 0);

        return result;
    }

    public String getScope() {
        return scope;
    }
}
