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
package uk.ac.ebi.intact.editor.controller.participant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.experiment.ExperimentController;
import uk.ac.ebi.intact.editor.controller.interaction.InteractionController;
import uk.ac.ebi.intact.editor.controller.publication.PublicationController;
import uk.ac.ebi.intact.editor.controller.shared.AnnotatedObjectController;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Component;

import javax.faces.event.ComponentSystemEvent;

/**
 * Participant controller.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class ParticipantController extends AnnotatedObjectController {

    private static final Log log = LogFactory.getLog( ParticipantController.class );

    private Component participant;
    private String ac;

    @Autowired
    private PublicationController publicationController;

    @Autowired
    private ExperimentController experimentController;

    @Autowired
    private InteractionController interactionController;

    public ParticipantController() {
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return getParticipant();
    }

    public void loadData( ComponentSystemEvent event ) {
        if ( ac != null ) {
            if ( participant == null || !ac.equals( participant.getAc() ) ) {
                participant = IntactContext.getCurrentInstance().getDaoFactory().getComponentDao().getByAc( ac );
            }
        } else {
            if ( participant != null ) ac = participant.getAc();
        }
    }

    public String getAc() {
        if ( ac == null && participant != null ) {
            return participant.getAc();
        }
        return ac;
    }

    public void setAc( String ac ) {
        this.ac = ac;
    }

    public Component getParticipant() {
        return participant;
    }

    public void setParticipant( Component participant ) {
        this.participant = participant;
    }
}