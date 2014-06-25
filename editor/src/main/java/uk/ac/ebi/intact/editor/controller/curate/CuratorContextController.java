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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.jami.model.Complex;
import psidev.psi.mi.jami.model.ModelledFeature;
import psidev.psi.mi.jami.model.ModelledParticipant;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.IntactObject;

import java.util.Collection;

/**
 * Keeps the changes for each annotated object by AC.
 * 
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("session")
public class CuratorContextController extends BaseController {

    @Autowired
    private PersistenceController persistenceController;

    public CuratorContextController() {
    }

    public String intactObjectSimpleName(IntactObject io) {
        if (io == null) return null;
        return io.getClass().getSimpleName().replaceAll("Impl", "");
    }

    public String annotatedObjectToString(AnnotatedObject ao) {
        return DebugUtil.annotatedObjectToString(ao, false);
    }

    public String jamiObjectSimpleName(IntactPrimaryObject io) {
        if (io == null) return null;
        if (io instanceof Complex){
            return "Complex";
        }
        else if (io instanceof ModelledParticipant){
            return "Complex Participant";
        }
        else if (io instanceof ModelledFeature){
            return "Complex Feature";
        }
        return null;
    }

    public String acList(Collection<? extends IntactObject> aos) {
        return DebugUtil.acList(aos).toString();
    }
}
