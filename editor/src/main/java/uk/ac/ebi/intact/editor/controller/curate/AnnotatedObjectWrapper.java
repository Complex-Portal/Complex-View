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

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.AnnotatedObject;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotatedObjectWrapper {

    private UnsavedChangeManager unsavedChangeManager;
    private AnnotatedObject annotatedObject;

    protected AnnotatedObjectWrapper(AnnotatedObject annotatedObject) {
        this.annotatedObject = annotatedObject;
    }

    public AnnotatedObject getAnnotatedObject() {
        return annotatedObject;
    }

    public UnsavedChangeManager getUnsavedChangeManager() {
        if (unsavedChangeManager == null) {
            UnsavedChangeManagerController unsavedChangeManagerController = (UnsavedChangeManagerController)
                    IntactContext.getCurrentInstance().getSpringContext().getBean("unsavedChangeManagerController");

            if (getAnnotatedObject() != null) {
                unsavedChangeManager = unsavedChangeManagerController.getUnsavedChangeManager(System.identityHashCode(getAnnotatedObject()));
            } else {
                unsavedChangeManager = new UnsavedChangeManager();
            }
        }

        return unsavedChangeManager;
    }
}
