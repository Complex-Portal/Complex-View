package uk.ac.ebi.intact.editor.controller.curate.interaction;

import psidev.psi.mi.jami.model.Organism;
import psidev.psi.mi.jami.model.Protein;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 */
public class ImportJamiCandidate {

    private boolean selected = true;
    private String query;
    private String organism;
    private List<String> primaryAcs;
    private List<String> secondaryAcs;
    private String source;
    private psidev.psi.mi.jami.model.Interactor interactor;
    private Protein uniprotProtein;

    public ImportJamiCandidate(String query, psidev.psi.mi.jami.model.Interactor interactor) {
        this.query = query;
        this.interactor = interactor;

        if (interactor.getOrganism() != null) {
            Organism biosource = interactor.getOrganism();

            if (biosource.getScientificName() != null){
                this.organism = biosource.getScientificName();
            }
            else {
                this.organism = biosource.getCommonName();
            }
        }
    }

    public ImportJamiCandidate(String query, Protein uniprotProteinLike) {
        this.query = query;
        this.uniprotProtein = uniprotProteinLike;

        primaryAcs = new ArrayList<String>(1);

        primaryAcs.add(uniprotProtein.getUniprotkb());
        secondaryAcs = new ArrayList<String>(uniprotProtein.getIdentifiers().size());
        for (Xref ref : uniprotProtein.getIdentifiers()){
            if (!ref.getId().equals(uniprotProtein.getUniprotkb())){
                secondaryAcs.add(ref.getId());
            }
        }
        organism = uniprotProtein.getOrganism().getCommonName();
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

    public psidev.psi.mi.jami.model.Interactor getInteractor() {
        return interactor;
    }

    public void setInteractor(psidev.psi.mi.jami.model.Interactor interactor) {
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

    public Protein getUniprotProtein() {
        return uniprotProtein;
    }

    public void setUniprotProtein(Protein uniprotProtein) {
        this.uniprotProtein = uniprotProtein;
    }

    public boolean isIsoform() {
        return uniprotProtein != null && (uniprotProtein.getUniprotkb().contains("-"));
    }

    public boolean isChain() {
        return uniprotProtein != null && (uniprotProtein.getUniprotkb().contains("-PRO"));
    }

    public boolean isPersistentInteractor(){
        return this.interactor instanceof IntactInteractor;
    }
}
