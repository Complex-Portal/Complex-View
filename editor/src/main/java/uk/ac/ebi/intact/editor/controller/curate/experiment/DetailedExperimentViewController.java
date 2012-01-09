/**
 * Copyright 2012 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.controller.curate.experiment;

import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.Experiment;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DetailedExperimentViewController extends JpaAwareController {
    
    private String ac;
    private ExperimentWrapper experimentWrapper;

    public DetailedExperimentViewController() {
    }

    public void loadData( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            if (ac != null) {
                Experiment experiment = getDaoFactory().getExperimentDao().getByAc(ac);
                this.experimentWrapper = new ExperimentWrapper(experiment, getDaoFactory().getEntityManager());
            }
        }
        
    }

    public String getAc() {
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public ExperimentWrapper getExperimentWrapper() {
        return experimentWrapper;
    }
}
