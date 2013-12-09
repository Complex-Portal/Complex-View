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
    /*************************/
    /*      Constructor      */
    /*************************/
    // To autowire
    public RestConnection( String url, Integer elementsPage ) {
        this.WS_URL = url ;
        this.number = elementsPage.intValue() ;
        // We make a first connect to know how many complexes we have
    }
    private int getNumberOfComplexes(String q) {
        int result = 0;
        String query = new StringBuilder() .append(this.WS_URL)
                       .append(QueryTypes.DEFAULT.value)
                       .append("/")
                       .append(q)
                       .append("?format=json")
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
            case DETAILS:
                q.append("details/");
                break;
            default:
            case DEFAULT:
                q.append("search/");
                break;
        }
        return q.toString();
    }

    protected String createQuery( String q,
                                  Page pageInfo,
                                  String filter,
                                  String queryType)
    {
        StringBuilder query = new StringBuilder();
        query.append( getBaseURL(queryType) );
        query.append( q );
        query.append("?format=json");
        query.append("&first=" + pageInfo.getPage() * this.number);
        query.append("&number=" + this.number);
        //We have to do something with the filter
        return query.toString();
    }


    private String createDetailsQuery(String ac, String queryType) {
        StringBuilder query = new StringBuilder();
        query.append( getBaseURL(queryType) );
        query.append( ac );
        query.append("?format=json");
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

    private ComplexDetails JSONToComplexDetails(JSONObject json) {
        ComplexDetails details = null;
        // We must do something here
        return details;
    }

    /****************************/
    /*      Public methods      */
    /****************************/

    public ComplexRestResult query( String query,
                                    String page,
                                    String filter,
                                    String queryType)
    {
        int max_elements = getNumberOfComplexes(query);
        Page pageInfo = new Page(page, this.number, max_elements);
        String q = createQuery(query, pageInfo, filter, queryType);
        ComplexRestResult result = JSONToComplexRestResult(getDataFromWS(q));
        result.setOriginalQuery(query);
        result.setPageInfo(pageInfo);
        return result;
    }

    public ComplexDetails getDetails( String ac, String queryType ) {
        String q = createDetailsQuery(ac, queryType);
        return JSONToComplexDetails(getDataFromWS(q));
    }

}
