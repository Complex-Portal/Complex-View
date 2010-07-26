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
package uk.ac.ebi.intact.editor.controller.curate.interaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.model.DualListModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.controller.curate.experiment.ExperimentController;
import uk.ac.ebi.intact.editor.controller.curate.publication.PublicationController;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.uniprot.service.UniprotRemoteService;

import javax.faces.event.ComponentSystemEvent;
import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class InteractionController extends AnnotatedObjectController {

    private static final Log log = LogFactory.getLog( InteractionController.class );

    @Autowired
    private UniprotRemoteService uniprotRemoteService;

    private Interaction interaction;
    private String ac;
    private DualListModel<String> experimentLists;

    private List<ParticipantWrapper> participantWrappers;

    @Autowired
    private PublicationController publicationController;

    @Autowired
    private ExperimentController experimentController;

    public InteractionController() {
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return getInteraction();
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        setInteraction((Interaction)annotatedObject);
    }

    public DualListModel<String> getExperimentLists() {
        return experimentLists;
    }

    public void setExperimentLists( DualListModel<String> experimentLists ) {
        this.experimentLists = experimentLists;
    }

    public void loadData( ComponentSystemEvent event ) {
        if ( ac != null ) {
            if ( interaction == null || !ac.equals( interaction.getAc() ) ) {
                interaction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc( ac );
            }
        } else {
            ac = interaction.getAc();
        }

        if (interaction == null) {
            addErrorMessage("No interaction with this AC", ac);
            return;
        }

        if( interaction.getExperiments().isEmpty() ) {
            addWarningMessage( "This interaction isn't attached to an experiment", "Plase add one or delete it" );
        } else {

            // check if the publication or experiment are null in their controllers (this happens when the interaction
            // page is loaded directly using a URL)
            if ( publicationController.getPublication() == null ) {
                Publication publication = interaction.getExperiments().iterator().next().getPublication();
                publicationController.setPublication( publication );
            }

            if ( experimentController.getExperiment() == null ) {
                experimentController.setExperiment( interaction.getExperiments().iterator().next() );
            }
        }

        refreshExperimentLists();


        if (interaction != null) {
            refreshParticipants();
        }
    }

    private void refreshExperimentLists() {
        // initialize the experiment lists
        List<String> source  = new ArrayList<String>();
        List<String> target  = new ArrayList<String>();


        if(  interaction.getExperiments().size() > 1 ) {
            addWarningMessage( "There are more than one experiment attached to this interaction", "" );
        }

        for ( Experiment e : interaction.getExperiments() ) {
            source.add( e.getShortLabel() );
        }

        if (publicationController.getPublication() != null) {
            final Publication pub = publicationController.getPublication();
            for ( Experiment e : pub.getExperiments() ) {
                if( ! source.contains( e.getShortLabel() ) ) {
                    target.add( e.getShortLabel() );
                }
            }
        }

        experimentLists = new DualListModel<String>( source, target);
    }

    @Override
    public boolean doSaveDetails() {
        boolean saved = false;

        for (ParticipantWrapper pw : participantWrappers) {
            Component component = pw.getParticipant();

            if (pw.isDeleted() && component.getAc() != null) {
                interaction.removeComponent(component);
                IntactContext.getCurrentInstance().getDaoFactory().getComponentDao().delete(component);
            }
            if (component.getAc() == null) {
                IntactContext.getCurrentInstance().getCorePersister().saveOrUpdate(component);
            }

            saved = true;
        }

        refreshParticipants();

        return saved;
    }

    public void newInteraction(Experiment experiment) {
        Collection<Experiment> experiments = Collections.singletonList(experiment);
        interaction = new InteractionImpl(experiments, new ArrayList<Component>(), null, null, "new-interaction-1", IntactContext.getCurrentInstance().getInstitution());
        interaction.setShortLabel(null);
        
        experiment.addInteraction(interaction);

        refreshExperimentLists();

        getUnsavedChangeManager().markAsUnsaved(interaction);
    }

    public void refreshParticipants() {
        final Collection<Component> components = interaction.getComponents();
        participantWrappers = new ArrayList<ParticipantWrapper>( components.size() );

        for ( Component component : components ) {
            participantWrappers.add( new ParticipantWrapper( component, getUnsavedChangeManager() ) );
        }
    }

    public void addParticipant(Component component) {
        interaction.addComponent(component);
        participantWrappers.add(new ParticipantWrapper( component, getUnsavedChangeManager() ));
    }

    public String getAc() {
        if ( ac == null && interaction != null ) {
            return interaction.getAc();
        }
        return ac;
    }

    public int countParticipantsByInteractionAc( String ac ) {
        return getDaoFactory().getInteractionDao().countInteractorsByInteractionAc( ac );
    }

    public Experiment getFirstExperiment( Interaction interaction ) {
        return interaction.getExperiments().iterator().next();
    }

    public void deleteParticipant(ParticipantWrapper participantWrapper) {
        participantWrapper.setDeleted(true);

        Component participant = participantWrapper.getParticipant();
        //interaction.removeComponent(participant);
        //refreshParticipants();
        setUnsavedChanges(true);

        StringBuilder participantInfo = new StringBuilder();

        if (participant.getInteractor() != null) {
            participantInfo.append(participant.getInteractor().getShortLabel());
            participantInfo.append(" ");
        }

        if (participant.getAc() != null) {
            participantInfo.append("(").append(participant.getAc()+")");
        }

        addInfoMessage("Removed participant", participantInfo.toString());
    }

    public void setAc( String ac ) {
        this.ac = ac;
    }

    public Interaction getInteraction() {
        return interaction;
    }

    public void setInteraction( Interaction interaction ) {
        this.interaction = interaction;
    }

    public Collection<ParticipantWrapper> getParticipants() {
        if (participantWrappers == null && ac != null) {
            loadData(null);
        } else if (interaction == null || (ac != null && !ac.equals(interaction.getAc()))) {
            loadData(null);
        } else if (participantWrappers == null) {
            participantWrappers = new ArrayList<ParticipantWrapper>();
        }
        return participantWrappers;
    }

    //////////////////////////////////
    // Participant related methods

    public String getInteractorIdentity(Interactor interactor) {
        if (interactor == null) return null;
        
        final Collection<InteractorXref> identities =
                AnnotatedObjectUtils.searchXrefsByQualifier( interactor, CvXrefQualifier.IDENTITY_MI_REF );
        StringBuilder sb = new StringBuilder(64);
        for ( Iterator<InteractorXref> iterator = identities.iterator(); iterator.hasNext(); ) {
            InteractorXref xref = iterator.next();
            sb.append( xref.getPrimaryId() );
            if( iterator.hasNext() ) {
                sb.append( "|" );
            }
        }
        return sb.toString();
    }

}