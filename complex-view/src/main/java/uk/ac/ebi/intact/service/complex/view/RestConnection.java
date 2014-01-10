package uk.ac.ebi.intact.service.complex.view;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

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
    }

    /*******************************/
    /*      Protected methods      */
    /*******************************/
    protected int getNumberOfComplexes(String q) {
        int result = 0;
        String query = new StringBuilder() .append(this.WS_URL)
                .append(QueryTypes.DEFAULT.value)
                .append("/")
                .append(q.replaceAll(" ", "%20"))
                .append("?format=json")
                .toString();
        Object o = getDataFromWS(query);
        if ( o != null) {
            JSONObject jo = (JSONObject) ((JSONObject) o).get("complexRestResult");
            result = ((Long)jo.get("size")).intValue();
        }
        return result;
    }

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
        query.append( q.replaceAll(" ", "%20") );
        query.append("?format=json");
        query.append("&first=" + pageInfo.getPage() * this.number);
        query.append("&number=" + this.number);
        //We have to do something with the filter
        return query.toString();
    }


    protected String createDetailsQuery(String ac, String queryType) {
        StringBuilder query = new StringBuilder();
        query.append( getBaseURL(queryType) );
        query.append( ac );
        query.append("?format=json");
        return query.toString();
    }

    protected JSONObject getDataFromWS( String query ) {
        JSONObject response = null;
        StringBuilder info = new StringBuilder();
        String aux = null;
        try{
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new URL(query)
                                    .openConnection()
                                    .getInputStream()
                    )
            );
            while ( ( aux = reader.readLine()) != null )
                info.append(aux);
            response = (JSONObject) JSONValue.parse(info.toString());
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return response;
    }

    protected ComplexRestResult JSONToComplexRestResult( JSONObject json ) {
        ComplexRestResult result = null;
        if (json != null) {
            JSONObject complexRestResults = (JSONObject) json.get("complexRestResult");
            JSONArray elements = (JSONArray) complexRestResults.get("elements");
            result = new ComplexRestResult();
            ComplexSearchResults res = null;
            JSONObject ob = null;
            for ( int i = 0; i < elements.size(); ++i ) {
                res = new ComplexSearchResults();
                ob = (JSONObject) elements.get(i);
                res.setComplexAC( (String) ob.get("complexAC") );
                res.setComplexName( (String) ob.get("complexName") );
                res.setCuratedComplex( (String) ob.get("curatedComplex") );
                res.setDescription( (String) ob.get("description") );
                res.setOrganismName( (String) ob.get("organismName") );
                result.add(res);
            }
        }
        return result;
    }

    protected ComplexDetails JSONToComplexDetails(JSONObject json) {
        ComplexDetails details = new ComplexDetails();
        if ( json != null ) {
            JSONObject j = (JSONObject) json.get("complexDetails");
            if ( j != null) {
                details.setSystematicName( (String) j.get("systematicName") );
                details.setFunction( (String) j.get("function") );
                details.setProperties( (String) j.get("properties") );
                details.setAc( (String) j.get("ac") );
                details.setName( (String) j.get("name") );
                details.setSpecie( (String) j.get("specie") );
                JSONArray synonyms = (JSONArray) j.get("synonyms");
                for ( int i = 0; i < synonyms.size(); ++i ) {
                    details.addSynonym((String) synonyms.get(i));
                }
                JSONArray componentsName = (JSONArray) j.get("componentsName");
                JSONArray componentsAC   = (JSONArray) j.get("componentsAC");
                for (  int i = 0; i < componentsName.size(); ++i ) {
                    details.addComponentName((String) componentsName.get(i));
                    details.addComponentAC  ((String) componentsAC  .get(i));
                }
            }
        }
        return details;
    }

    /****************************/
    /*      Public methods      */
    /****************************/

    public Page getPage(String page, String query) {
        int max_elements = getNumberOfComplexes(query);
        return new Page(page, this.number, max_elements);
    }

    public ComplexRestResult query( String query,
                                    Page page,
                                    String filter,
                                    String queryType)
    {
        String q = createQuery(query, page, filter, queryType);
        ComplexRestResult result = JSONToComplexRestResult(getDataFromWS(q));
        if (result != null) {
            result.setOriginalQuery(query);
        }
        return result;
    }

    public ComplexDetails getDetails( String ac, String queryType ) {
        String q = createDetailsQuery(ac, queryType);
        return JSONToComplexDetails(getDataFromWS(q));
    }

}
