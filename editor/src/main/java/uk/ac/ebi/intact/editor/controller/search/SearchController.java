package uk.ac.ebi.intact.editor.controller.search;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import java.util.Collection;
import java.util.HashMap;

/**
 * Search controller.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
@Controller
@Scope( "session" )
public class SearchController extends JpaAwareController {

    private static final Log log = LogFactory.getLog( SearchController.class );

    public String query;

    @Autowired
    private DaoFactory daoFactory;

    private LazyDataModel<Publication> publications;

    private LazyDataModel<Experiment> experiments;

    private LazyDataModel<Interaction> interactions;

    private LazyDataModel<Interactor> molecules;

    private LazyDataModel<CvObject> cvobjects;

    //////////////////
    // Constructors

    public SearchController() {
    }

    ///////////////////////////
    // Getters and Setters

    public String getQuery() {
        return query;
    }

    public void setQuery( String query ) {
        this.query = query;
    }

    public LazyDataModel<Publication> getPublications() {
        return publications;
    }

    public LazyDataModel<Experiment> getExperiments() {
        return experiments;
    }

    public LazyDataModel<Interaction> getInteractions() {
        return interactions;
    }

    public LazyDataModel<Interactor> getMolecules() {
        return molecules;
    }

    public LazyDataModel<CvObject> getCvobjects() {
        return cvobjects;
    }

    ///////////////
    // Actions

    @Transactional( readOnly = true )
    public String doSearch() {

        log.info( "Searching for '" + query + "'..." );

        if ( ! StringUtils.isEmpty(query) ) {
            query = query.toLowerCase().trim();

            String q = query;
            q = q.replaceAll( "\\*", "%" );
            q = q.replaceAll( "\\?", "%" );
            if ( !query.startsWith( "%" ) ) {
                q = "%" + q;
            }
            if ( !query.endsWith( "%" ) ) {
                q = q + "%";
            }

            if ( !query.equals( q ) ) {
                log.info( "Updated query: '" + q +"'" );
            }

            // TODO implement simple prefix for the search query so that one can aim at an AC, shortlabel, PMID...

            // Note: the search is NOT case sensitive !!!
            // Note: the search includes wildcards automatically

            loadPublication( q );
            loadExperiments( q );
            loadInteractions( q );
            loadMolecules( q );
            loadCvObjects( q );
        } else {
            resetSearchResults();
        }

        return "search.results";
    }

    private void resetSearchResults() {
        publications = null;
        experiments = null;
        interactions = null;
        molecules = null;
        cvobjects = null;
    }

    public boolean isEmptyQuery() {
       return StringUtils.isEmpty(query);
    }

    public boolean hasNoResults() {
        return ( publications != null && publications.getRowCount() == 0 )
               && ( experiments != null && experiments.getRowCount() == 0 )
               && ( interactions != null && interactions.getRowCount() == 0 )
               && ( molecules != null && molecules.getRowCount() == 0 )
               && ( cvobjects != null && cvobjects.getRowCount() == 0 );
    }

    public boolean matchesSingleType() {
        int matches = 0;

        if ( publications != null && publications.getRowCount() > 0 ) matches++;
        if ( experiments != null && experiments.getRowCount() > 0 ) matches++;
        if ( interactions != null && interactions.getRowCount() > 0 ) matches++;
        if ( molecules != null && molecules.getRowCount() > 0 ) matches++;
        if ( cvobjects != null && cvobjects.getRowCount() > 0 ) matches++;

        return matches == 1;
    }

    private void loadCvObjects( String query ) {

        log.info( "Searching for CvObject matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );

        // all cvobjects
        cvobjects = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                              "select distinct i " +
                                                              "from CvObject i inner join fetch i.xrefs as x " +
                                                              "where    ( i.ac = :query " +
                                                              "      or lower(i.shortLabel) like :query " +
                                                              "      or lower(i.fullName) like :query " +
                                                              "      or lower(i.identifier) like :query " +
                                                              "      or lower(x.primaryId) like :query ) " +
                                                              "order by i.updated desc",

                                                              "select count(distinct i) " +
                                                              "from CvObject i inner join i.xrefs as x " +
                                                              "where   (i.ac = :query " +
                                                              "      or lower(i.identifier) like :query " +
                                                              "      or lower(i.shortLabel) like :query " +
                                                              "      or lower(i.fullName) like :query " +
                                                              "      or lower(x.primaryId) like :query )",

                                                              params );

        log.info( "CvObject found: " + cvobjects.getRowCount() );
    }

    private void loadMolecules( String query ) {

        log.info( "Searching for Molecules matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );

        // all molecules but interactions
        molecules = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                              "select distinct i " +
                                                              "from InteractorImpl i inner join fetch i.xrefs as x " +
                                                              "where    ( i.ac = :query " +
                                                              "      or lower(i.shortLabel) like :query " +
                                                              "      or lower(x.primaryId) like :query ) " +
                                                              "      and i.cvInteractorType.identifier <> 'MI:0317' " +
                                                              "order by i.updated desc",

                                                              "select count(distinct i) " +
                                                              "from InteractorImpl i inner join i.xrefs as x " +
                                                              "where   (i.ac = :query " +
                                                              "      or lower(i.shortLabel) like :query " +
                                                              "      or lower(x.primaryId) like :query )" +
                                                              "     and i.cvInteractorType.identifier <> 'MI:0317'",

                                                              params );

        log.info( "Molecules found: " + molecules.getRowCount() );
    }

    public int countInteractionsByMoleculeAc( Interactor molecule ) {
        return getDaoFactory().getInteractorDao().countInteractionsForInteractorWithAc( molecule.getAc() );
    }

    public String getIdentityXref( Interactor molecule ) {
        // TODO handle multiple identities (return xref and iterate to display them all)
        final Collection<InteractorXref> xrefs = AnnotatedObjectUtils.searchXrefsByQualifier( molecule, CvXrefQualifier.IDENTITY_MI_REF );
        if ( xrefs.isEmpty() ) {
            return "-";
        }
        return xrefs.iterator().next().getPrimaryId();
    }

    private void loadInteractions( String query ) {

        log.info( "Searching for Interactions matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );

        // Load experiment eagerly to avoid LazyInitializationException when redering the view
        interactions = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                                 "select distinct i " +
                                                                 "from InteractionImpl i inner join i.xrefs as x " +
                                                                 "                       inner join fetch i.experiments as e " +
                                                                 "where    i.ac = :query " +
                                                                 "      or lower(i.shortLabel) like :query " +
                                                                 "      or lower(x.primaryId) like :query " +
                                                                 "order by i.updated desc",

                                                                 "select count(distinct i) " +
                                                                 "from InteractionImpl i inner join i.xrefs as x " +
                                                                 "where    i.ac = :query " +
                                                                 "      or lower(i.shortLabel) like :query " +
                                                                 "      or lower(x.primaryId) like :query ",

                                                                 params );

        log.info( "Interactions found: " + interactions.getRowCount() );
    }

    public Experiment getFirstExperiment( Interaction interaction ) {
        return interaction.getExperiments().iterator().next();
    }

    private void loadExperiments( String query ) {

        log.info( "Searching for experiments matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );

        experiments = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                                "select distinct e " +
                                                                "from Experiment e inner join e.xrefs as x " +
                                                                "where    e.ac = :query " +
                                                                "      or lower(e.shortLabel) like :query " +
                                                                "      or lower(x.primaryId) like :query " +
                                                                "order by e.updated desc",

                                                                "select count(distinct e) " +
                                                                "from Experiment e inner join e.xrefs as x " +
                                                                "where    e.ac = :query " +
                                                                "      or lower(e.shortLabel) like :query " +
                                                                "      or lower(x.primaryId) like :query ",

                                                                params );

        log.info( "Experiment found: " + experiments.getRowCount() );
    }

    private void loadPublication( String query ) {
        log.info( "Searching for publications matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );

        // TODO add: author
        publications = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                                 "select distinct p " +
                                                                 "from Publication p inner join p.xrefs as x " +
                                                                 "where    p.ac = :query " +
                                                                 "      or lower(p.shortLabel) like :query " +
                                                                 "      or lower(p.fullName) like :query " +
                                                                 "      or lower(x.primaryId) like :query " +
                                                                 "order by p.updated desc",

                                                                 "select count(distinct p) " +
                                                                 "from Publication p inner join p.xrefs as x " +
                                                                 "where    p.ac = :query " +
                                                                 "      or lower(p.shortLabel) like :query " +
                                                                 "      or lower(p.fullName) like :query " +
                                                                 "      or lower(x.primaryId) like :query ",

                                                                 params );

        log.info( "Publications found: " + publications.getRowCount() );
    }

    public int countExperimentsForPublication( Publication publication ) {
        return getDaoFactory().getPublicationDao().countExperimentsForPublicationAc( publication.getAc() );
    }

    public int countInteractionsForPublication( Publication publication ) {
        return getDaoFactory().getPublicationDao().countInteractionsForPublicationAc( publication.getAc() );
    }
}
