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

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class UnsavedChange {

    public static final String DELETED = "deleted";
    public static final String CREATED = "created";
    public static final String UPDATED = "updated";

    private IntactObject unsavedObject;
    private String action;

    public UnsavedChange(IntactObject unsavedObject, String action) {
        this.unsavedObject = unsavedObject;
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public IntactObject getUnsavedObject() {
        return unsavedObject;
    }

    public String getDescription(IntactObject intactObject) {
        if (intactObject instanceof AnnotatedObject) {
            return ((AnnotatedObject)intactObject).getShortLabel();
        }
        
        return DebugUtil.intactObjectToString(intactObject, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnsavedChange that = (UnsavedChange) o;

        if (action != null ? !action.equals(that.action) : that.action != null) return false;
        if (unsavedObject != null ? System.identityHashCode(unsavedObject) != System.identityHashCode(that.unsavedObject) : that.unsavedObject != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = unsavedObject != null ? System.identityHashCode(unsavedObject) : 0;
        result = 31 * result + (action != null ? action.hashCode() : 0);
        return result;
    }
}
