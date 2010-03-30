package uk.ac.ebi.intact.editor.controller.search;

import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Publication;

import java.util.HashMap;

/**
 * Search controller.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
@Controller
public class SearchController extends JpaAwareController {

    private static final Log log = LogFactory.getLog( SearchController.class );

    public String query;

    @Autowired
    private DaoFactory daoFactory;

    private LazyDataModel<Publication> publications;

    private LazyDataModel<Experiment> experiments;

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

    public void setPublications( LazyDataModel<Publication> publications ) {
        this.publications = publications;
    }

    public LazyDataModel<Experiment> getExperiments() {
        return experiments;
    }

    public void setExperiments( LazyDataModel<Experiment> experiments ) {
        this.experiments = experiments;
    }

    ///////////////
    // Actions

    @Transactional(readOnly = true)
    public String doSearch() {
        log.info( "Searching for '"+ query +"'..." );

        // TODO implement simple prefix for the search query so that one can aim at an AC, shortlabel, PMID...

        loadPublication( query );
        loadExperiments( query );

        return "search.results";
    }

    private void loadExperiments( String query ) {

        log.info( "Searching for experiments matching '"+ query +"'..." );

        final HashMap<String,String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );

        experiments = LazyDataModelFactory.createLazyDataModel(getCoreEntityManager(),

                                                                "select distinct e " +
                                                                "from Experiment e inner join e.xrefs as x " +
                                                                "where    e.ac = :query " +
                                                                "      or e.shortLabel like :query " +
                                                                "      or x.primaryId like :query " +
                                                                "order by e.updated desc",

                                                                "select count(distinct e) " +
                                                                "from Experiment e inner join e.xrefs as x " +
                                                                "where    e.ac = :query " +
                                                                "      or e.shortLabel like :query " +
                                                                "      or x.primaryId like :query ",

                                                                params );

        log.info( "Experiment found: " + experiments.getRowCount() );


    }

    private void loadPublication( String query ) {
        log.info( "Searching for publications matching '"+ query +"'..." );

        final HashMap<String,String> params = Maps.<String, String>newHashMap();
        params.put( "query", query );

        // TODO add: publication title, author
        publications = LazyDataModelFactory.createLazyDataModel(getCoreEntityManager(),

                                                                "select distinct p " +
                                                                "from Publication p inner join p.xrefs as x " +
                                                                "where    p.ac = :query " +
                                                                "      or p.shortLabel like :query " +
                                                                "      or x.primaryId like :query " +
                                                                "order by p.updated desc",

                                                                "select count(distinct p) " +
                                                                "from Publication p inner join p.xrefs as x " +
                                                                "where    p.ac = :query " +
                                                                "      or p.shortLabel like :query " +
                                                                "      or x.primaryId like :query ",

                                                                params );

        log.info( "Publications found: " + publications.getRowCount() );
    }
}
