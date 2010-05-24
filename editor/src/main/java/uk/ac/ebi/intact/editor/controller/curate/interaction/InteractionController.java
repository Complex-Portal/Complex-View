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
import org.hibernate.LazyInitializationException;
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

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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

    private String participantToImport;

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

//                participantDataModel = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),
//                                                                                 "select c from Component c where c.interaction.ac = '" + ac + "'",
//                                                                                 "select count(c) from Component c where c.interaction.ac = '" + ac + "'" );
            }
        } else {
            ac = interaction.getAc();

//            if( participantDataModel == null ) {
//                participantDataModel = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),
//                                                                                 "select c from Component c where c.interaction.ac = '" + ac + "'",
//                                                                                 "select count(c) from Component c where c.interaction.ac = '" + ac + "'" );
//            }
        }

        if( interaction.getExperiments().isEmpty() ) {
            addWarningMessage( "This interaction isn't attached to an experiment", "Abort experiment loading." );
            return;
        }

        // check if the publication or experiment are null in their controllers (this happens when the interaction
        // page is loaded directly using a URL)
        if ( publicationController.getPublication() == null ) {
            Publication publication = interaction.getExperiments().iterator().next().getPublication();
            publicationController.setPublication( publication );
        }

        if ( experimentController.getExperiment() == null ) {
            experimentController.setExperiment( interaction.getExperiments().iterator().next() );
        }

        // initialize the experiment lists
        List<String> source  = new ArrayList<String>();
        List<String> target  = new ArrayList<String>();


        if(  interaction.getExperiments().size() > 1 ) {
            addWarningMessage( "There are more than one experiment attached to this interaction", "" );
        }

        for ( Experiment e : interaction.getExperiments() ) {
            source.add( e.getShortLabel() );
        }

        final Publication pub = publicationController.getPublication();
        for ( Experiment e : pub.getExperiments() ) {
            if( ! source.contains( e.getShortLabel() ) ) {
                target.add( e.getShortLabel() );
            }
        }

        experimentLists = new DualListModel<String>( source, target);

        if (interaction != null) {
            refreshParticipants();
        }
    }

    @Override
    public void doSave(ActionEvent evt) {
        super.doSave(evt);

        try {
            for (Component component : interaction.getComponents()) {
                if (component.getAc() == null) {
                    IntactContext.getCurrentInstance().getCorePersister().saveOrUpdate(component);
                }
            }
        } catch (LazyInitializationException e) {
            System.out.println("LAZY B****");
        }
    }

    public void refreshParticipants() {
        final Collection<Component> components = interaction.getComponents();
        participantWrappers = new ArrayList<ParticipantWrapper>( components.size() );

        for ( Component component : components ) {
            participantWrappers.add( new ParticipantWrapper( component ) );
        }
    }

    public void addParticipant(Component component) {
        interaction.addComponent(component);
        participantWrappers.add(new ParticipantWrapper( component ));
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

    public void deleteParticipant(Component participant) {
        System.out.println("DELETE!! "+participant);
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
        return participantWrappers;
    }

    //////////////////////////////////
    // Participant related methods

    public String getInteractorIdentity(Interactor interactor) {
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