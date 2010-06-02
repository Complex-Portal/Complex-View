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
package uk.ac.ebi.intact.editor.controller.curate.feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.controller.curate.experiment.ExperimentController;
import uk.ac.ebi.intact.editor.controller.curate.interaction.InteractionController;
import uk.ac.ebi.intact.editor.controller.curate.participant.ParticipantController;
import uk.ac.ebi.intact.editor.controller.curate.publication.PublicationController;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Publication;

import javax.faces.event.ComponentSystemEvent;

/**
 * Feature controller.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: ParticipantController.java 14281 2010-04-12 21:48:43Z samuel.kerrien $
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class FeatureController extends AnnotatedObjectController {

    private static final Log log = LogFactory.getLog( FeatureController.class );

    private Feature feature;

    /**
     * The AC of the feature to be loaded.
     */
    private String ac;

    @Autowired
    private PublicationController publicationController;

    @Autowired
    private ExperimentController experimentController;

    @Autowired
    private InteractionController interactionController;

    @Autowired
    private ParticipantController participantController;

    public FeatureController() {
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return getFeature();
    }

    public void loadData( ComponentSystemEvent event ) {
        if ( ac != null ) {
            if ( feature == null || !ac.equals( feature.getAc() ) ) {
                feature = IntactContext.getCurrentInstance().getDaoFactory().getFeatureDao().getByAc( ac );
            }
        } else {
            if ( feature != null ) ac = feature.getAc();
        }

        if( interactionController.getInteraction() == null ) {
            final Interaction interaction = feature.getComponent().getInteraction();
            interactionController.setInteraction( interaction );
        }

        if ( publicationController.getPublication() == null ) {
            Publication publication = feature.getComponent().getInteraction().getExperiments().iterator().next().getPublication();
            publicationController.setPublication( publication );
        }

        if ( experimentController.getExperiment() == null ) {
            experimentController.setExperiment( feature.getComponent().getInteraction().getExperiments().iterator().next() );
        }
    }

    public String getAc() {
        if ( ac == null && feature != null ) {
            return feature.getAc();
        }
        return ac;
    }

    public void setAc( String ac ) {
        this.ac = ac;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature( Feature feature ) {
        this.feature = feature;
    }
}