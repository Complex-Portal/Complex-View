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

    /*************************/
    /*      Constructor      */
    /*************************/
    // To autowire
    public RestConnection( String url ) { this.WS_URL = url ; }

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
    protected String createQuery( String q,
                                  String first,
                                  String number,
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
        if ( first != null ) query.append("&first=" + first);
        if ( number != null ) query.append("&number=" + number);
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
                                    String first,
                                    String number,
                                    String filter,
                                    String queryType)
    {
        String q = createQuery(query, first, number, filter, queryType);
        JSONObject json = getDataFromWS(q);
        return JSONToComplexRestResult(json);
    }

}
