package uk.ac.ebi.intact.editor.controller.search;

import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.persistence.dao.AnnotatedObjectDao;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Publication;

import javax.faces.event.ActionEvent;
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
public class SearchController extends JpaAwareController {

    private static final Log log = LogFactory.getLog( SearchController.class );

    public String query;

    @Autowired
    private DaoFactory daoFactory;


    private LazyDataModel<Publication> publications;

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

    ///////////////
    // Actions

    public void doSearch( ActionEvent evt) {
        log.info( "Searching for '"+ query +"'..." );

        // TODO implement simple prefix for the search query so that one can aim at an AC, shortlabel, PMID...

        final AnnotatedObjectDao<AnnotatedObject> aodao = daoFactory.getAnnotatedObjectDao( AnnotatedObject.class );
        final AnnotatedObject ao = aodao.getByAc( query );

        final Collection<AnnotatedObject> aos = aodao.getByShortLabelLike( query, true );

        final Publication publication = daoFactory.getPublicationDao().getByPubmedId( query );


    }


    private void publicationSearch( String query ) {
//        final HashMap<String,String> params = Maps.<String, String>newHashMap();
//        params.put(  )
//        publications = LazyDataModelFactory.createLazyDataModel(getCoreEntityManager(),
//
//                                                                "select p " +
//                                                                "from Publication " +
//                                                                "where    p.ac = :query " +
//                                                                "      or p.shortlabel like :query " +
//                                                                "order by p.updated desc",
//
//                                                                "select count(p) from Publication p",
//
//                                                                params );



    }

}
