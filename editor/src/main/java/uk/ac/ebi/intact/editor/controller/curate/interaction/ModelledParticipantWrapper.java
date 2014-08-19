package uk.ac.ebi.intact.editor.controller.curate.interaction;

import psidev.psi.mi.jami.model.ModelledFeature;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.editor.controller.curate.ChangesController;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.IntactModelledFeature;
import uk.ac.ebi.intact.jami.model.extension.IntactModelledParticipant;
import uk.ac.ebi.intact.jami.model.extension.IntactStoichiometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapped modelled participant to allow handling of special fields (eg. author given name) from the interaction view.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
public class ModelledParticipantWrapper {

    private IntactModelledParticipant participant;
    private ChangesController changesController;
    private List<ModelledFeatureWrapper> features;
    private String interactorIdentity;

    private ComplexController interactionController;

    private boolean deleted;

    public ModelledParticipantWrapper(IntactModelledParticipant participant, ChangesController changesController, ComplexController interactionController) {
        this.participant = participant;
        this.changesController = changesController;

        features = new ArrayList<ModelledFeatureWrapper>(participant.getFeatures().size());

        for (ModelledFeature feature : participant.getFeatures()) {
            features.add(new ModelledFeatureWrapper((IntactModelledFeature)feature));
        }

        this.interactionController = interactionController;

        Xref ref = participant.getInteractor().getPreferredIdentifier();
        if (ref == null){
            this.interactorIdentity = participant.getInteractor().getShortName();
        }
        else{
            this.interactorIdentity = ref.getId();
        }
    }

    public IntactModelledParticipant getParticipant() {
        return participant;
    }

    public String getInteractorIdentity() {
        return interactorIdentity;
    }

    public void setParticipant( IntactModelledParticipant participant ) {
        if (participant != null){
            this.participant = participant;
        }
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;

        if (participant.getAc() != null) {
            IntactDao dao = ApplicationContextProvider.getBean("intactDao");

            if (deleted) {

                changesController.markJamiToDelete(participant, (IntactComplex)participant.getInteraction(), dao.getSynchronizerContext().getModelledParticipantSynchronizer());
            } else {
                changesController.removeFromDeleted(participant, (IntactComplex)participant.getInteraction(), dao.getSynchronizerContext().getModelledParticipantSynchronizer());
            }
        }
    }

    public List<ModelledFeatureWrapper> getFeatures() {
        return features;
    }

    public int getMinStoichiometry(){
        return this.participant.getStoichiometry() != null ? this.participant.getStoichiometry().getMinValue() : 0;
    }

    public int getMaxStoichiometry(){
        return this.participant.getStoichiometry() != null ? this.participant.getStoichiometry().getMaxValue() : 0;
    }

    public void setMinStoichiometry(int stc){
        if (this.participant.getStoichiometry() == null){
            this.participant.setStoichiometry(new IntactStoichiometry(stc));
        }
        else {
            IntactStoichiometry stoichiometry = (IntactStoichiometry)participant.getStoichiometry();
            this.participant.setStoichiometry(new IntactStoichiometry(stc, Math.max(stc, stoichiometry.getMaxValue())));
        }
    }

    public void setMaxStoichiometry(int stc){
        if (this.participant.getStoichiometry() == null){
            this.participant.setStoichiometry(new IntactStoichiometry(stc));
        }
        else {
            IntactStoichiometry stoichiometry = (IntactStoichiometry)participant.getStoichiometry();
            this.participant.setStoichiometry(new IntactStoichiometry(Math.min(stc, stoichiometry.getMinValue()), stc));
        }
    }
}
