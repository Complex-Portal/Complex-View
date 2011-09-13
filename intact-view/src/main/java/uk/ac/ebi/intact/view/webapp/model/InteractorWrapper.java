/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.model;

import uk.ac.ebi.intact.model.Interactor;

import java.io.Serializable;

/**
 * Wrapper of the interactor, in order to provide convenient methods when used as data results.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorWrapper implements Serializable {

    private Interactor interactor;
    private long count;

    public InteractorWrapper(Interactor interactor) {
        this.interactor = interactor;
    }

    public InteractorWrapper(Interactor interactor, long count) {
        this.interactor = interactor;
        this.count = count;
    }

    public Interactor getInteractor() {
        return interactor;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
