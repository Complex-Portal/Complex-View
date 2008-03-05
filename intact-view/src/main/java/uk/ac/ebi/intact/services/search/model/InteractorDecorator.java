package uk.ac.ebi.intact.services.search.model;

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.ProteinUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorDecorator<T extends Interactor> extends AnnotatedObjectDecorator<T>{

    private String geneName;
    private Set<String> interactionsAcs;
    private Set<String> partnerAcs;

    public InteractorDecorator(T data) {
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
