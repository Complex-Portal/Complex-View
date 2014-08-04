package uk.ac.ebi.intact.editor.controller.curate.interaction;

import psidev.psi.mi.jami.model.ModelledFeature;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.editor.controller.curate.ChangesController;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.*;
import uk.ac.ebi.intact.jami.utils.IntactUtils;

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

    public String getAuthorGivenName() {
        psidev.psi.mi.jami.model.Alias alias = psidev.psi.mi.jami.utils.AliasUtils.collectFirstAliasWithType(this.participant.getAliases(), psidev.psi.mi.jami.model.Alias.AUTHOR_ASSIGNED_NAME_MI,
                psidev.psi.mi.jami.model.Alias.AUTHOR_ASSIGNED_NAME);
        return alias != null ? alias.getName() : null;
    }

    public void setAuthorGivenName( String name ) {
        if (name == null){
            psidev.psi.mi.jami.utils.AliasUtils.removeAllAliasesWithType(this.participant.getAliases(), psidev.psi.mi.jami.model.Alias.AUTHOR_ASSIGNED_NAME_MI,
                    psidev.psi.mi.jami.model.Alias.AUTHOR_ASSIGNED_NAME);
        }
        else{
            IntactDao dao = ApplicationContextProvider.getBean("intactDao");
            psidev.psi.mi.jami.model.Alias alias = psidev.psi.mi.jami.utils.AliasUtils.collectFirstAliasWithType(this.participant.getAliases(), psidev.psi.mi.jami.model.Alias.AUTHOR_ASSIGNED_NAME_MI,
                    psidev.psi.mi.jami.model.Alias.AUTHOR_ASSIGNED_NAME);
            if (alias != null && alias.getName().equals(name)){
                ((AbstractIntactAlias)alias).setName(name);
                changesController.markAsJamiUnsaved(participant, dao.getSynchronizerContext().getModelledParticipantSynchronizer());
            }
            else {
                this.participant.getAliases().add(new ModelledFeatureAlias(IntactUtils.createMIAliasType(psidev.psi.mi.jami.model.Alias.AUTHOR_ASSIGNED_NAME,
                        psidev.psi.mi.jami.model.Alias.AUTHOR_ASSIGNED_NAME_MI), name));
                changesController.markAsJamiUnsaved(participant, dao.getSynchronizerContext().getModelledParticipantSynchronizer());
            }
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
            this.participant.setStoichiometry(new IntactStoichiometry(stc, stoichiometry.getMaxValue()));
        }
    }

    public void setMaxStoichiometry(int stc){
        if (this.participant.getStoichiometry() == null){
            this.participant.setStoichiometry(new IntactStoichiometry(stc));
        }
        else {
            IntactStoichiometry stoichiometry = (IntactStoichiometry)participant.getStoichiometry();
            this.participant.setStoichiometry(new IntactStoichiometry(stoichiometry.getMinValue(), stc));
        }
    }
}
