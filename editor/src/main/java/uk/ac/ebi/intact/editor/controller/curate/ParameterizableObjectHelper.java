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

import uk.ac.ebi.intact.editor.controller.curate.util.IntactObjectComparator;
import uk.ac.ebi.intact.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ParameterizableObjectHelper {

    private Parameterizable annotatedObject;

    public ParameterizableObjectHelper(Parameterizable annotatedObject) {
        this.annotatedObject = annotatedObject;
    }
    
     // Parameters
    ///////////////////////////////////////////////
    
    public void newParameter() {
         if (annotatedObject instanceof Interaction) {
             InteractionParameter parameter = new InteractionParameter();
             ((Interaction) annotatedObject).addParameter(parameter);
         } else if (annotatedObject instanceof Component) {
             ComponentParameter parameter = new ComponentParameter();
             ((Component) annotatedObject).addParameter(parameter);
         } else {
             throw new IllegalArgumentException("Annotated object is not parameterizable: "+annotatedObject);
         }

    }
    
    public List<Parameter> getParameters() {
        if ( annotatedObject == null ) {
            return Collections.EMPTY_LIST;
        }

        final List<Parameter> parameters = new ArrayList<Parameter>( annotatedObject.getParameters() );
        Collections.sort( parameters, new IntactObjectComparator() );
        return parameters;
    }


    public void removeParameter( Parameter parameter ) {
        annotatedObject.getParameters().remove( parameter );
        
    }

    public Parameterizable getAnnotatedObject() {
        return annotatedObject;
    }
}
