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
package uk.ac.ebi.intact.editor.controller.curate.experiment;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.InteractionDao;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.controller.curate.publication.PublicationController;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.clone.IntactCloner;
import uk.ac.ebi.intact.model.clone.IntactClonerException;
import uk.ac.ebi.intact.model.util.ExperimentUtils;

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class ExperimentController extends AnnotatedObjectController {

    private Experiment experiment;
    private String ac;
    private LazyDataModel<Interaction> interactionDataModel;

    @Autowired
    private PublicationController publicationController;

    public ExperimentController() {
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return getExperiment();
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        setExperiment((Experiment)annotatedObject);
    }

    @SuppressWarnings("unchecked")
    public void loadData( ComponentSystemEvent event ) {
        if ( ac != null ) {
            if ( experiment == null || !ac.equals( experiment.getAc() ) ) {
                experiment = loadByAc(IntactContext.getCurrentInstance().getDaoFactory().getExperimentDao(), ac);
            }
            interactionDataModel = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),
                                                                                 "select i from InteractionImpl i join i.experiments as exp where exp.ac = '" + ac + "'",
                                                                                 "select count(i) from InteractionImpl i join i.experiments as exp where exp.ac = '" + ac + "'" );
        } else if ( experiment != null ) {
            ac = experiment.getAc();
        }

        if ( experiment != null && publicationController.getPublication() == null ) {
            publicationController.setPublication( experiment.getPublication() );
        }

        generalLoadChecks();
    }

    @Override
    @Transactional(value = "core")
    public boolean doSaveDetails() {
        if (experiment.getAc() == null) {
            experiment.setShortLabel(createExperimentShortLabel());
        }

        InteractionDao interactionDao = getDaoFactory().getInteractionDao();

        boolean saved = false;

        for (String deletedInteractionAc : getUnsavedChangeManager().getDeletedAcs(Interaction.class)) {
            interactionDao.deleteByAc(deletedInteractionAc);
            saved = true;
        }

        return saved;
    }

    @Override
    public void modifyClone(AnnotatedObject clone) {
        clone.setShortLabel(createExperimentShortLabel());
        interactionDataModel = LazyDataModelFactory.createEmptyDataModel();

        final Experiment exp = (Experiment) clone;
        exp.getPublication().addExperiment(exp);
    }

    public void cloneWithInteractions() {
        IntactCloner cloner = new IntactCloner(true) {
           @Override
           public Publication clonePublication( Publication publication ) throws IntactClonerException {
                return publication;
            }

            protected AnnotatedObject cloneAnnotatedObjectCommon( AnnotatedObject<?, ?> ao, AnnotatedObject clone ) throws IntactClonerException {
                if (clone instanceof Publication) {
                    return clone;
                }
                return super.cloneAnnotatedObjectCommon(ao, clone);
            }
        };

        super.clone(cloner);
    }

    public void doPreSave() {
        if (experiment.getPublication() != publicationController.getPublication()) {
            experiment.setPublication(null);
            publicationController.getPublication().addExperiment(experiment);
        }
    }

    public String newExperiment(Publication publication) {
        Experiment experiment = new Experiment(getIntactContext().getInstitution(), createExperimentShortLabel(), null);
        setExperiment(experiment);

        publication.addExperiment(experiment);

        publicationController.setPublication(publication);

        if (publication.getPublicationId() != null) {
            CvDatabase pubmed = getDaoFactory().getCvObjectDao(CvDatabase.class).getByIdentifier(CvDatabase.PUBMED_MI_REF);
            CvXrefQualifier primaryRef = getDaoFactory().getCvObjectDao(CvXrefQualifier.class).getByIdentifier(CvXrefQualifier.PRIMARY_REFERENCE_MI_REF);

            experiment.addXref(new ExperimentXref(pubmed, publication.getShortLabel(), primaryRef));
        }

        interactionDataModel = LazyDataModelFactory.createEmptyDataModel();

        //getUnsavedChangeManager().markAsUnsaved(experiment);

        return navigateToObject(experiment);
    }

    private String createExperimentShortLabel() {
        String author;

        if (publicationController.getFirstAuthor() == null) {
            addWarningMessage("The current publication does not have the authors annotation.","Created anonymous short label.");

            author = "anonymous";

        } else {
            author = publicationController.getFirstAuthor();
        }

        String year;

        if (publicationController.getYear() == null) {
            addWarningMessage("The current publication does not have the year annotation.","Correct the label if necessary and add a year it to the publication.");

            year = new SimpleDateFormat("yyyy").format(new Date());
        } else {
             year = String.valueOf(publicationController.getYear());
        }

        String shortLabel = author+"-"+year;

        String expLabel = shortLabel.toLowerCase();

        if (experiment != null && experiment.getPublication() == null) {
            experiment.setPublication(publicationController.getPublication());
        }

        String pmid = null;

        if (experiment != null) {
            pmid = ExperimentUtils.getPubmedId(experiment);
        } else if (publicationController.getPublication() != null) {
            pmid = publicationController.getPublication().getShortLabel();
        }

        if (pmid != null) {
             return ExperimentUtils.syncShortLabelWithDb(expLabel, pmid);
        } else {
            return expLabel;
        }
    }

    public int countInteractionsByExperimentAc( String ac ) {
        return getDaoFactory().getExperimentDao().countInteractionsForExperimentWithAc( ac );
    }

    public void acceptExperiment(ActionEvent actionEvent) {
           UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");

           setAcceptedMessage("Accepted "+new SimpleDateFormat("yyyy-MMM-dd").format(new Date()).toUpperCase()+" by "+userSessionController.getCurrentUser().getLogin().toUpperCase());
    }

    public boolean isAccepted() {
        return ExperimentUtils.isAccepted(experiment);
    }

    public String getAcceptedMessage() {
        return findAnnotationText( CvTopic.ACCEPTED );
    }

    public void setAcceptedMessage( String message ) {
        setAnnotation( CvTopic.ACCEPTED, message );
    }

    public String getOnHold() {
        return findAnnotationText( CvTopic.ON_HOLD );
    }

    public void setOnHold( String reason ) {
        setAnnotation( CvTopic.ON_HOLD, reason );
    }

    public String getAc() {
        if ( ac == null && experiment != null ) {
            ac = experiment.getAc();
        }
        return ac;
    }

    public void setAc( String ac ) {
        this.ac = ac;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment( Experiment experiment ) {
        this.experiment = experiment;
        this.ac = experiment.getAc();
    }

    public LazyDataModel<Interaction> getInteractionDataModel() {
        return interactionDataModel;
    }

    public void setInteractionDataModel(LazyDataModel<Interaction> interactionDataModel) {
        this.interactionDataModel = interactionDataModel;
    }
}