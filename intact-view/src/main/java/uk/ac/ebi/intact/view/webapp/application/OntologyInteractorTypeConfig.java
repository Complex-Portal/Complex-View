package uk.ac.ebi.intact.view.webapp.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.LazyLoadedOntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.model.CvInteractorType;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;

import javax.faces.bean.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

/**
 * This controller will load ontology terms for different interactor types.
 * It will be used by the searcher to know what are the different types for proteins, nucleic acids, genes
 * and small molecules
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/08/12</pre>
 */
@Controller
@ApplicationScoped
public class OntologyInteractorTypeConfig implements InitializingBean{

    private static final Log log = LogFactory.getLog(OntologyInteractorTypeConfig.class);

    @Autowired
    private IntactViewConfiguration viewConfiguration;

    private String [] proteinTypes;
    private String [] compoundTypes;
    private String [] nucleicAcidTypes;
    private String[] geneTypes;

    private boolean proteinTypesInitialized;
    private boolean compoundTypesInitialized;
    private boolean nucleicAcidTypesInitialized;
    private boolean geneTypesInitialized;

    @Override
    public void afterPropertiesSet() throws Exception {

        final SolrServer ontologySolrServer = viewConfiguration.getOntologySolrServer();
        OntologySearcher ontologySearcher = new OntologySearcher(ontologySolrServer);

        // load proteins
        log.info("Loading protein types...");
        loadProteins(ontologySearcher);

        // load nucleic acids
        log.info("Loading nucleic acid types...");
        loadNucleicAcids(ontologySearcher);

        // load compounds
        log.info("Loading compounds types...");
        loadCompounds(ontologySearcher);

        // load gene
        log.info("Loading gene types...");
        loadGenes(ontologySearcher);
    }

    public void refreshTypes(){
        final SolrServer ontologySolrServer = viewConfiguration.getOntologySolrServer();
        OntologySearcher ontologySearcher = new OntologySearcher(ontologySolrServer);

        // load proteins
        log.info("Loading protein types...");
        loadProteins(ontologySearcher);

        // load nucleic acids
        log.info("Loading nucleic acid types...");
        loadNucleicAcids(ontologySearcher);

        // load compounds
        log.info("Loading compounds types...");
        loadCompounds(ontologySearcher);

        // load gene
        log.info("Loading gene types...");
        loadGenes(ontologySearcher);
    }

    private void loadProteins(OntologySearcher ontologySearcher){
        try{
            OntologyTerm proteinTerm = new LazyLoadedOntologyTerm( ontologySearcher, CvInteractorType.PROTEIN_MI_REF, CvInteractorType.PROTEIN );
            OntologyTerm peptideTerm = new LazyLoadedOntologyTerm( ontologySearcher, CvInteractorType.PEPTIDE_MI_REF, "peptide" );

            List<OntologyTerm> proteinChildren = loadChildrenFor(proteinTerm);
            List<OntologyTerm> peptideChildren = loadChildrenFor(peptideTerm);

            this.proteinTypes = new String[2+peptideChildren.size()+proteinChildren.size()];

            this.proteinTypes[0] = proteinTerm.getId();
            this.proteinTypes[1] = peptideTerm.getId();

            int index = 2;
            for (OntologyTerm term : proteinChildren){
                this.proteinTypes[index] = term.getId();
                index++;
            }
            for (OntologyTerm term : peptideChildren){
                this.proteinTypes[index] = term.getId();
                index++;
            }
            proteinTypesInitialized = true;
        }
        catch (SolrServerException e){
            log.error("Could not load protein types from SOLR index. Use defaults.", e);
            proteinTypesInitialized = false;

            proteinTypes = new String[]{CvInteractorType.PROTEIN_MI_REF,CvInteractorType.PEPTIDE_MI_REF};
        }
    }

    private void loadCompounds(OntologySearcher ontologySearcher){
        try{
            OntologyTerm bioActiveEntity = new LazyLoadedOntologyTerm( ontologySearcher, "MI:1100", "bioactive entity" );

            List<OntologyTerm> compoundChildren = loadChildrenFor(bioActiveEntity);

            this.compoundTypes = new String[1+compoundChildren.size()];

            this.compoundTypes[0] = bioActiveEntity.getId();

            int index = 1;
            for (OntologyTerm term : compoundChildren){
                this.compoundTypes[index] = term.getId();
                index++;
            }
            compoundTypesInitialized = true;
        }
        catch (SolrServerException e){
            log.error("Could not load compound types from SOLR index. Use defaults.", e);
            compoundTypesInitialized = false;
            compoundTypes = new String[]{"MI:1100",CvInteractorType.SMALL_MOLECULE_MI_REF,CvInteractorType.POLYSACCHARIDE_MI_REF};
        }
    }

    private void loadNucleicAcids(OntologySearcher ontologySearcher){
        try{
            OntologyTerm nucleicAcid = new LazyLoadedOntologyTerm( ontologySearcher, "MI:0318", "nucleic acid" );

            List<OntologyTerm> nucleicAcidChildren = loadChildrenFor(nucleicAcid);

            this.nucleicAcidTypes = new String[1+nucleicAcidChildren.size()];

            this.nucleicAcidTypes[0] = nucleicAcid.getId();

            int index = 1;
            for (OntologyTerm term : nucleicAcidChildren){
                this.nucleicAcidTypes[index] = term.getId();
                index++;
            }
            nucleicAcidTypesInitialized = true;
        }
        catch (SolrServerException e){
            log.error("Could not load nucleic acid types from SOLR index. Use defaults.", e);
            nucleicAcidTypesInitialized = false;
            nucleicAcidTypes = new String[]{"MI:0318",CvInteractorType.DNA_MI_REF,CvInteractorType.RNA_MI_REF};
        }
    }

    private void loadGenes(OntologySearcher ontologySearcher){
        try{
            OntologyTerm gene = new LazyLoadedOntologyTerm( ontologySearcher, "MI:0250", "gene" );

            List<OntologyTerm> geneChildren = loadChildrenFor(gene);

            this.geneTypes = new String[1+geneChildren.size()];

            this.geneTypes[0] = gene.getId();

            int index = 1;
            for (OntologyTerm term : geneChildren){
                this.geneTypes[index] = term.getId();
                index++;
            }

            geneTypesInitialized = true;
        }
        catch (SolrServerException e){
            log.error("Could not load gene types from SOLR index. Use defaults.", e);
            geneTypesInitialized = false;
            geneTypes = new String[]{"MI:0250"};
        }
    }

    private List<OntologyTerm> loadChildrenFor(OntologyTerm term){

        List<OntologyTerm> totalChildren = new ArrayList<OntologyTerm>(term.getChildren());

        for (OntologyTerm child : term.getChildren()){
            List<OntologyTerm> children2 = loadChildrenFor(child);

            if (!children2.isEmpty()){
                totalChildren.addAll(children2);
            }
        }

        return totalChildren;
    }

    public String[] getProteinTypes() {
        if (!proteinTypesInitialized){
            final SolrServer ontologySolrServer = viewConfiguration.getOntologySolrServer();
            OntologySearcher ontologySearcher = new OntologySearcher(ontologySolrServer);
            loadProteins(ontologySearcher);
        }
        return proteinTypes;
    }

    public String[] getCompoundTypes() {
        if (!compoundTypesInitialized){
            final SolrServer ontologySolrServer = viewConfiguration.getOntologySolrServer();
            OntologySearcher ontologySearcher = new OntologySearcher(ontologySolrServer);
            loadCompounds(ontologySearcher);
        }
        return compoundTypes;
    }

    public String[] getNucleicAcidTypes() {
        if (!nucleicAcidTypesInitialized){
            final SolrServer ontologySolrServer = viewConfiguration.getOntologySolrServer();
            OntologySearcher ontologySearcher = new OntologySearcher(ontologySolrServer);
            loadNucleicAcids(ontologySearcher);
        }
        return nucleicAcidTypes;
    }

    public String[] getGeneTypes() {
        if (!geneTypesInitialized){
            final SolrServer ontologySolrServer = viewConfiguration.getOntologySolrServer();
            OntologySearcher ontologySearcher = new OntologySearcher(ontologySolrServer);
            loadGenes(ontologySearcher);
        }
        return geneTypes;
    }
}
