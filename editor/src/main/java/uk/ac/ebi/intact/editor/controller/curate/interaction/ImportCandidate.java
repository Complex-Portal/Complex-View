package uk.ac.ebi.intact.editor.controller.curate.interaction;

import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.uniprot.model.UniprotProtein;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ImportCandidate {

    private boolean selected = true;
    private String query;
    private String organism;
    private List<String> primaryAcs;
    private List<String> secondaryAcs;
    private String source;
    private Interactor interactor;
    private UniprotProtein uniprotProtein;

    public ImportCandidate(String query, Interactor interactor) {
        this.query = query;
        this.interactor = interactor;

        if (interactor.getBioSource() != null) {
            this.organism = interactor.getBioSource().getFullName();
        }
    }

    public ImportCandidate(String query, UniprotProtein uniprotProtein) {
        this.query = query;
        this.uniprotProtein = uniprotProtein;

        primaryAcs = new ArrayList<String>(1);

        primaryAcs.add(uniprotProtein.getPrimaryAc());
        secondaryAcs = uniprotProtein.getSecondaryAcs();
        organism = uniprotProtein.getOrganism().getName();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Interactor getInteractor() {
        return interactor;
    }

    public void setInteractor(Interactor interactor) {
        this.interactor = interactor;
    }

    public List<String> getPrimaryAcs() {
        return primaryAcs;
    }

    public void setPrimaryAcs(List<String> primaryAcs) {
        this.primaryAcs = primaryAcs;
    }

    public List<String> getSecondaryAcs() {
        return secondaryAcs;
    }

    public void setSecondaryAcs(List<String> secondaryAcs) {
        this.secondaryAcs = secondaryAcs;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public UniprotProtein getUniprotProtein() {
        return uniprotProtein;
    }

    public void setUniprotProtein(UniprotProtein uniprotProtein) {
        this.uniprotProtein = uniprotProtein;
    }
}
