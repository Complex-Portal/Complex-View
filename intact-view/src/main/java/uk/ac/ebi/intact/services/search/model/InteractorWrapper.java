package uk.ac.ebi.intact.services.search.model;

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.ProteinUtils;

import java.util.*;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorWrapper<T extends Interactor> extends AnnotatedObjectWrapper<T> {

    private String geneName;
    private Set<String> interactionsAcs;
    private Set<String> partnerAcs;
    private Object interactions;

    public InteractorWrapper(T data) {
        super(data);

        this.interactionsAcs = new HashSet<String>();
        this.partnerAcs = new HashSet<String>();

        if (!(data instanceof Interaction)) {
            this.geneName = ProteinUtils.getGeneName(data);

            for (Component component : data.getActiveInstances()) {
                this.interactionsAcs.add(component.getInteractionAc());
                this.partnerAcs.add(component.getInteractorAc());
            } 
        }
    }

    public Object getInteractions() {
        if (interactions == null) {

            if (getData().getAc() != null) {
                interactions = new CriteriaDataModel(DetachedCriteria.forClass(Interaction.class)
                        .createAlias("components", "comp")
                        .createAlias("comp.interactor", "interactor")
                        .add(Restrictions.eq("interactor.ac", getData().getAc())));
            } else {

                interactions = new ArrayList<Interaction>();

                for (Component c : getData().getActiveInstances()) {
                    ((List) interactions).add(c.getInteraction());
                }
            }
        }
        return interactions;
    }

    public BioSource getBioSource() {
        return getData().getBioSource();
    }

    public CvInteractorType getCvInteractorType() {
        return getData().getCvInteractorType();
    }

    public Collection<Component> getActiveInstances() {
        return getData().getActiveInstances();
    }

    public String getObjClass() {
        return getData().getObjClass();
    }

    public String getGeneName() {
        return geneName;
    }

    public Set<String> getInteractionsAcs() {
        return interactionsAcs;
    }

    public Set<String> getPartnerAcs() {
        return partnerAcs;
    }

    public int getInteractionsCount() {
        return interactionsAcs.size();
    }

    public int getPartnersCount() {
        return partnerAcs.size();
    }

}