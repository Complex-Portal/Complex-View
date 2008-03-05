package uk.ac.ebi.intact.services.search.transintact;

import javax.faces.context.FacesContext;
import java.io.IOException;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BinarySearchController {

    private static final String SEARCH_QUERY_URL = "uk.ac.ebi.intact.SEARCH_QUERY_URL";

    private String searchQuery;

    public BinarySearchController()
    {

    }

    public String doSearch()
    {
        FacesContext context = FacesContext.getCurrentInstance();

        String searchQueryUrl = context.getExternalContext().getInitParameter(SEARCH_QUERY_URL);

        // short-circuit the cycle to redirect to a external page
        try
        {
            context.responseComplete();
            context.getExternalContext().redirect(searchQueryUrl+searchQuery);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public String getSearchQuery()
    {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery)
    {
        this.searchQuery = searchQuery;
    }

}
