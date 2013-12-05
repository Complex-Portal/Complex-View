package uk.ac.ebi.intact.service.complex.view;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 03/12/13
 */
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 03/12/13
 */
public class RestConnection {
    /********************************/
    /*      Private attributes      */
    /********************************/
    String WS_URL = null;
    int number;
    int max_elements;
    /*************************/
    /*      Constructor      */
    /*************************/
    // To autowire
    public RestConnection( String url, Integer elementsPage ) {
        this.WS_URL = url ;
        this.number = elementsPage.intValue() ;
        // We make a first connect to know how many complexes we have
        this.max_elements = getNumberOfComplexes();
    }
    private int getNumberOfComplexes() {
        int result = 0;
        String query = new StringBuilder() .append(this.WS_URL)
                       .append(QueryTypes.DEFAULT.value)
                       .append("/*?format=json")
                       .toString();
        try {
            result = getDataFromWS(query)
                    .getJSONObject("complexRestResult")
                    .getInt("size");
        } catch (JSONException e) {
            //We must log that
            e.printStackTrace();
        }
        return result;
    }

    /*******************************/
    /*      Protected methods      */
    /*******************************/
    protected String getBaseURL(String queryType) {
        StringBuilder q = new StringBuilder() .append(this.WS_URL);
        queryType = queryType != null ? queryType : QueryTypes.DEFAULT.value;
        switch (QueryTypes.getByValue(queryType)) {
            case INTERACTOR:
                q.append("interactor/");
                break;
            case COMPLEX:
                q.append("complex/");
                break;
            case ORGANISM:
                q.append("organism/");
                break;
            default:
            case DEFAULT:
                q.append("search/");
                break;
        }
        return q.toString();
    }

    protected int getPageNumber( String page ) {
        int pageNumber = 0;
        if ( page != null ) {
            try{
                pageNumber = Integer.parseInt(page);
            }
            catch (NumberFormatException e) {
                // We must log that
            }
            if ( pageNumber > 0 ){
                if (pageNumber * this.number >= this.max_elements )
                    pageNumber = (max_elements / this.number);
            }
            else
                pageNumber = 0;
        }
        return pageNumber;
    }

    protected int getPreviousPage ( String page ) {
        int prevPage = getPageNumber(page);
        return --prevPage;
    }

    protected int getNextPage ( String page ) {
        int nextPage = getPageNumber(page);
        return ++nextPage * this.number >= this.max_elements ? -1 : nextPage;
    }

    protected void setPrevAndNextPage ( ComplexRestResult result, String page ) {
        result.setNextPage ( getNextPage ( page ) ) ;
        result.setPage ( getPageNumber ( page ) ) ;
        result.setPrevPage ( getPreviousPage ( page ) ) ;
    }

    protected String createQuery( String q,
                                  String page,
                                  String filter,
                                  String queryType)
    {
        StringBuilder query = new StringBuilder();
        query.append( getBaseURL(queryType) );
        if ( ! q.contains("format=") ) {
            q = new StringBuilder()
                    .append(q)
                    .append("?format=json")
                    .toString();
        }
        query.append( q );
        query.append("&first=" + getPageNumber(page) * this.number);
        query.append("&number=" + this.number);
        //We have to do something with the filter
        return query.toString();
    }
    protected JSONObject getDataFromWS( String query ) {
        JSONObject response = null;
        try{
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new URL(query)
                                    .openConnection()
                                    .getInputStream()
                    )
            );
            response = new JSONObject(reader.readLine());
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

    protected ComplexRestResult JSONToComplexRestResult( JSONObject json ) {
        ComplexRestResult result = null;
        try {
            JSONArray elements = json.getJSONObject("complexRestResult")
                                     .getJSONArray("elements");
            result = new ComplexRestResult();
            ComplexSearchResults res = null;
            JSONObject ob = null;
            for ( int i = 0; i < elements.length(); ++i ) {
                res = new ComplexSearchResults();
                ob = (JSONObject) elements.get(i);
                res.setComplexAC( (String) ob.get("complexAC") );
                res.setComplexName( (String) ob.get("complexName") );
                res.setCuratedComplex( (String) ob.get("curatedComplex") );
                res.setDescription( (String) ob.get("description") );
                res.setOrganismName( (String) ob.get("organismName") );
                result.add(res);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /****************************/
    /*      Public methods      */
    /****************************/

    public ComplexRestResult query( String query,
                                    String page,
                                    String filter,
                                    String queryType)
    {
        String q = createQuery(query, page, filter, queryType);
        JSONObject json = getDataFromWS(q);
        ComplexRestResult result = JSONToComplexRestResult(json);
        setPrevAndNextPage(result, page);
        result.setOriginalQuery(query);
        result.setNumberOfElementsPerPage(this.number);
        return result;
    }

}
