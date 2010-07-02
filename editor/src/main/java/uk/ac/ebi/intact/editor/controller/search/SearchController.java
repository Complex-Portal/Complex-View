package uk.ac.ebi.intact.editor.controller.search;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Search controller.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
@Controller
@Scope( "conversation.access" )
@ConversationName ("search")
public class SearchController extends JpaAwareController {

    private static final Log log = LogFactory.getLog( SearchController.class );

    private String query;

    @Autowired
    private DaoFactory daoFactory;

    private LazyDataModel<Publication> publications;

    private LazyDataModel<Experiment> experiments;

    private LazyDataModel<Interaction> interactions;

    private LazyDataModel<Interactor> molecules;

    private LazyDataModel<CvObject> cvobjects;

    private LazyDataModel<Feature> features;

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

     public LazyDataModel<Feature> getFeatures() {
        return features;
    }

    ///////////////
    // Actions

    @Transactional( readOnly = true )
    public String doSearch() {

        log.info( "Searching for '" + query + "'..." );

        if ( !StringUtils.isEmpty( query ) ) {
            final String originalQuery = query;
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
                log.info( "Updated query: '" + q + "'" );
            }

            // TODO implement simple prefix for the search query so that one can aim at an AC, shortlabel, PMID...

            // Note: the search is NOT case sensitive !!!
            // Note: the search includes wildcards automatically
            final String finalQuery = q;

            ExecutorService executorService = Executors.newCachedThreadPool();

            Runnable runnablePub = new Runnable() {
                @Override
                public void run() {
                   loadPublication( finalQuery, originalQuery );
                }
            };

            Runnable runnableExp = new Runnable() {
                @Override
                public void run() {
                   loadExperiments( finalQuery, originalQuery );
                }
            };

            Runnable runnableInt = new Runnable() {
                @Override
                public void run() {
                   loadInteractions( finalQuery, originalQuery );
                }
            };

            Runnable runnableMol = new Runnable() {
                @Override
                public void run() {
                   loadMolecules( finalQuery, originalQuery );
                }
            };

            Runnable runnableCvs = new Runnable() {
                @Override
                public void run() {
                   loadCvObjects( finalQuery, originalQuery );
                }
            };

            Runnable runnableFeatures = new Runnable() {
                @Override
                public void run() {
                   loadFeatures( finalQuery, originalQuery );
                }
            };

            executorService.submit(runnablePub);
            executorService.submit(runnableExp);
            executorService.submit(runnableInt);
            executorService.submit(runnableMol);
            executorService.submit(runnableCvs);
            executorService.submit(runnableFeatures);

            executorService.shutdown();

            try {
                executorService.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
        return StringUtils.isEmpty( query );
    }

    public boolean hasNoResults() {
        return ( publications != null && publications.getRowCount() == 0 )
               && ( experiments != null && experiments.getRowCount() == 0 )
               && ( interactions != null && interactions.getRowCount() == 0 )
               && ( molecules != null && molecules.getRowCount() == 0 )
               && ( cvobjects != null && cvobjects.getRowCount() == 0 )
               && ( features != null && features.getRowCount() == 0 );
    }

    public boolean matchesSingleType() {
        int matches = 0;

        if ( publications != null && publications.getRowCount() > 0 ) matches++;
        if ( experiments != null && experiments.getRowCount() > 0 ) matches++;
        if ( interactions != null && interactions.getRowCount() > 0 ) matches++;
        if ( molecules != null && molecules.getRowCount() > 0 ) matches++;
        if ( cvobjects != null && cvobjects.getRowCount() > 0 ) matches++;
        if ( features != null && features.getRowCount() > 0 ) matches++;

        return matches == 1;
    }

    private void loadCvObjects( String query, String originalQuery ) {

        log.info( "Searching for CvObject matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );
        params.put( "ac", originalQuery );

        // all cvobjects
        cvobjects = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                              "select distinct i " +
                                                              "from CvObject i inner join fetch i.xrefs as x " +
                                                              "where    ( i.ac = :ac " +
                                                              "      or lower(i.shortLabel) like :query " +
                                                              "      or lower(i.fullName) like :query " +
                                                              "      or lower(i.identifier) like :query " +
                                                              "      or lower(x.primaryId) like :query ) " +
                                                              "order by i.updated desc",

                                                              "select count(distinct i) " +
                                                              "from CvObject i inner join i.xrefs as x " +
                                                              "where   (i.ac = :ac " +
                                                              "      or lower(i.identifier) like :query " +
                                                              "      or lower(i.shortLabel) like :query " +
                                                              "      or lower(i.fullName) like :query " +
                                                              "      or lower(x.primaryId) like :query )",

                                                              params );

        log.info( "CvObject found: " + cvobjects.getRowCount() );
    }

    private void loadMolecules( String query, String originalQuery ) {

        log.info( "Searching for Molecules matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );
        params.put( "ac", originalQuery );

        // all molecules but interactions
        molecules = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                              "select distinct i " +
                                                              "from InteractorImpl i inner join fetch i.xrefs as x " +
                                                              "where    ( i.ac = :ac " +
                                                              "      or lower(i.shortLabel) like :query " +
                                                              "      or lower(x.primaryId) like :query ) " +
                                                              "      and i.cvInteractorType.identifier <> 'MI:0317' " +
                                                              "order by i.updated desc",

                                                              "select count(distinct i) " +
                                                              "from InteractorImpl i inner join i.xrefs as x " +
                                                              "where   (i.ac = :ac " +
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

    private void loadInteractions( String query, String originalQuery ) {

        log.info( "Searching for Interactions matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );
        params.put( "ac", originalQuery );

        // Load experiment eagerly to avoid LazyInitializationException when redering the view
        interactions = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                                 "select distinct i " +
                                                                 "from InteractionImpl i inner join i.xrefs as x " +
                                                                 "                       inner join fetch i.experiments as e " +
                                                                 "where    i.ac = :ac " +
                                                                 "      or lower(i.shortLabel) like :query " +
                                                                 "      or lower(x.primaryId) like :query " +
                                                                 "order by i.updated desc",

                                                                 "select count(distinct i) " +
                                                                 "from InteractionImpl i inner join i.xrefs as x " +
                                                                 "where    i.ac = :ac " +
                                                                 "      or lower(i.shortLabel) like :query " +
                                                                 "      or lower(x.primaryId) like :query ",

                                                                 params );

        log.info( "Interactions found: " + interactions.getRowCount() );
    }

    public Experiment getFirstExperiment( Interaction interaction ) {
        return interaction.getExperiments().iterator().next();
    }

    private void loadExperiments( String query, String originalQuery ) {

        log.info( "Searching for experiments matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );
        params.put( "ac", originalQuery );

        experiments = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                                "select distinct e " +
                                                                "from Experiment e inner join e.xrefs as x " +
                                                                "where    e.ac = :ac " +
                                                                "      or lower(e.shortLabel) like :query " +
                                                                "      or lower(x.primaryId) like :query " +
                                                                "order by e.updated desc",

                                                                "select count(distinct e) " +
                                                                "from Experiment e inner join e.xrefs as x " +
                                                                "where    e.ac = :ac " +
                                                                "      or lower(e.shortLabel) like :query " +
                                                                "      or lower(x.primaryId) like :query ",

                                                                params );

        log.info( "Experiment found: " + experiments.getRowCount() );
    }

    private void loadPublication( String query, String originalQuery ) {
        log.info( "Searching for publications matching '" + query + "'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );
        params.put( "ac", originalQuery );

        // TODO add: author
        publications = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                                 "select distinct p " +
                                                                 "from Publication p inner join p.xrefs as x " +
                                                                 "where    p.ac = :ac " +
                                                                 "      or lower(p.shortLabel) like :query " +
                                                                 "      or lower(p.fullName) like :query " +
                                                                 "      or lower(x.primaryId) like :query " +
                                                                 "order by p.updated desc",

                                                                 "select count(distinct p) " +
                                                                 "from Publication p inner join p.xrefs as x " +
                                                                 "where    p.ac = :ac " +
                                                                 "      or lower(p.shortLabel) like :query " +
                                                                 "      or lower(p.fullName) like :query " +
                                                                 "      or lower(x.primaryId) like :query ",

                                                                 params );

        log.info( "Publications found: " + publications.getRowCount() );
    }

    private void loadFeatures( String query, String originalQuery ) {
        log.info( "Searching for features matching '" + query + "' or AC '"+originalQuery+"'..." );

        final HashMap<String, String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );
        params.put( "ac", originalQuery );

        features = LazyDataModelFactory.createLazyDataModel( getCoreEntityManager(),

                                                                 "select distinct p " +
                                                                 "from Feature p inner join p.xrefs as x " +
                                                                 "where    p.ac = :ac " +
                                                                 "      or lower(p.shortLabel) like :query " +
                                                                 "      or lower(p.fullName) like :query " +
                                                                 "      or lower(x.primaryId) like :query " +
                                                                 "order by p.updated desc",

                                                                 "select count(distinct p) " +
                                                                 "from Feature p inner join p.xrefs as x " +
                                                                 "where    p.ac = :ac " +
                                                                 "      or lower(p.shortLabel) like :query " +
                                                                 "      or lower(p.fullName) like :query " +
                                                                 "      or lower(x.primaryId) like :query ",

                                                                 params );

        log.info( "Features found: " + features.getRowCount() );
    }

    public int countExperimentsForPublication( Publication publication ) {
        return getDaoFactory().getPublicationDao().countExperimentsForPublicationAc( publication.getAc() );
    }

    public int countInteractionsForPublication( Publication publication ) {
        return getDaoFactory().getPublicationDao().countInteractionsForPublicationAc( publication.getAc() );
    }
}
