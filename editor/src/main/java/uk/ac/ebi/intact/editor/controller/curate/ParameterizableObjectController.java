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

import uk.ac.ebi.intact.model.Parameter;
import uk.ac.ebi.intact.model.Parameterizable;

import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class ParameterizableObjectController extends AnnotatedObjectController {

    public ParameterizableObjectHelper getParameterizableObjectHelper() {
        return new ParameterizableObjectHelper((Parameterizable) getAnnotatedObject(), getDaoFactory());
    }

    public void newParameter() {
        getParameterizableObjectHelper().newParameter();
    }

    public List<Parameter> getParameters() {
        return getParameterizableObjectHelper().getParameters();
    }

    public void removeParameter( Parameter parameter ) {
        getParameterizableObjectHelper().removeParameter(parameter);

    }
}
