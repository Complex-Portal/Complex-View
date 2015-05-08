package uk.ac.ebi.intact.service.complex.view;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 03/12/13
 */
public class RestConnection {
    /********************************/
    /*      Private attributes      */
    /********************************/
    private final String ftpUrl;
    private String WS_URL = null;
    private int number;
    private Map<String, String> facetFieldNames;
    private static final Log log = LogFactory.getLog(RestConnection.class);

    /*************************/
    /*      Constructor      */
    /*************************/
    // To autowire
    public RestConnection( String url, Integer elementsPage, Map<String, String> map , String ftp_url) {
        this.WS_URL = url ;
        this.number = elementsPage.intValue() ;
        this.facetFieldNames = map ;
        this.ftpUrl = ftp_url;
    }

    /*******************************/
    /*      Protected methods      */
    /*******************************/
    protected int getNumberOfComplexes(String q, String filters, String facets) throws Exception {
        int result = 0;
        Object o = null;
        StringBuilder queryBuilder = new StringBuilder()
                .append(this.WS_URL)
                .append(QueryTypes.DEFAULT.value)
                .append("/")
                .append(q)
                .append("?format=json");
        if (filters != null) queryBuilder.append("&filters=" + filters );
        if (facets != null) queryBuilder.append("&facets=" + facets );
        try {
            o = getDataFromWS(URIUtil.encodeQuery(queryBuilder.toString()));
        } catch (URIException e) {
            e.printStackTrace();
        }
        if ( o != null) {
            //JSONObject jo = (JSONObject) ((JSONObject) o).get("complexRestResult");
            result = ((Long)((JSONObject)o).get("size")).intValue();
        }
        return result;
    }

    protected String getBaseURL(String queryType) {
        StringBuilder q = new StringBuilder() .append(this.WS_URL);
        queryType = queryType != null ? queryType : QueryTypes.DEFAULT.value;
        switch (QueryTypes.getByValue(queryType)) {
            case EXPORT:
                q.append("export/");
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
                                  String filters,
                                  String facets,
                                  String queryType)
    {
        StringBuilder query = new StringBuilder();
        query.append( getBaseURL(queryType) );
        query.append(q);
        query.append("?format=json");
        query.append("&first=" + pageInfo.getPage() * this.number);
        query.append("&number=" + this.number);
        if (filters != null) query.append("&filters=" + filters );
        if (facets != null) query.append("&facets=" + facets );
        try {
            return URIUtil.encodeQuery(query.toString());
        } catch (URIException e) {
            e.printStackTrace();
        }
        return null;
    }


    protected String createDetailsQuery(String ac, String queryType) {
        StringBuilder query = new StringBuilder();
        query.append( getBaseURL(queryType) );
        query.append( ac );
        query.append("?format=json");
        return query.toString();
    }

    protected JSONObject getDataFromWS( String query ) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(query);
        HttpResponse response = client.execute(request);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) //503
                throw new ComplexPortalException();
            else{ //404
                throw new Exception();
            }
        }
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        return (JSONObject) JSONValue.parse(reader);
    }

    protected ComplexRestResult JSONToComplexRestResult( JSONObject json ) {
        ComplexRestResult result = null;
        if (json != null && json.size() > 0) {
            JSONObject complexRestResults = json;
            JSONArray elements = (JSONArray) complexRestResults.get("elements");
            result = new ComplexRestResult();
            ComplexSearchResults res = null;
            JSONObject ob = null;
            for ( int i = 0; i < elements.size(); ++i ) {
                res = new ComplexSearchResults();
                ob = (JSONObject) elements.get(i);
                res.setComplexAC( (String) ob.get("complexAC") );
                res.setComplexName( (String) ob.get("complexName") );
                res.setDescription( (String) ob.get("description") );
                res.setOrganismName( (String) ob.get("organismName") );
                result.add(res);
            }
            JSONObject facetsOb = (JSONObject) complexRestResults.get("facets");
            List<ComplexFacetResults> list;
            ComplexFacetResults facetResults;
            if ( facetsOb != null ) {
                for(Object facetField : facetsOb.keySet() ) {
                    list = new ArrayList<ComplexFacetResults>();
                    JSONArray facetA = (JSONArray) facetsOb.get(facetField);
                    for ( int i = 0; i < facetA.size(); ++i ) {
                        facetResults = new ComplexFacetResults();
                        ob = (JSONObject) facetA.get(i);
                        facetResults.setName( (String) ob.get("name") );
                        facetResults.setCount( (Long) ob.get("count") );
                        list.add(facetResults);
                    }
                    result.add( String.valueOf( facetField ) , list);
                }
            }
        }
        return result;
    }

    protected ComplexDetails JSONToComplexDetails(JSONObject json) {
        ComplexDetails details = new ComplexDetails();
        if ( json != null ) {
            JSONObject j = json;
            if ( j != null) {
                details.setSystematicName( (String) j.get("systematicName") );
                details.setFunction( (String) j.get("function") );
                details.setProperties( (String) j.get("properties") );
                details.setAc( (String) j.get("ac") );
                details.setName( (String) j.get("name") );
                details.setSpecies( (String) j.get("species") );
                details.setLigand( (String) j.get("ligand") );
                details.setDisease( (String) j.get("disease") );
                details.setComplexAssembly( (String) j.get("complexAssembly") );
                JSONArray synonyms = (JSONArray) j.get("synonyms");
                for ( int i = 0; i < synonyms.size(); ++i ) {
                    details.addSynonym((String) synonyms.get(i));
                }
                // Setting the participants information
                JSONArray partArray = (JSONArray) j.get("participants");
                ComplexDetailsParticipants participant = null;
                JSONArray featuresArray = null;
                ComplexDetailsFeatures features = null;
                JSONArray rangesArray = null;
                Collection<String> ranges = null;
                for ( int i = 0; i < partArray.size(); ++i ){
                    participant = new ComplexDetailsParticipants();
                    JSONObject part = (JSONObject) partArray.get(i);
                    participant.setInteractorAC( (String) part.get("interactorAC") );
                    participant.setIdentifier((String) part.get("identifier"));
                    participant.setIdentifierLink((String) part.get("identifierLink"));
                    participant.setName((String) part.get("name"));
                    participant.setDescription((String) part.get("description"));
                    String stochiometry = (String) part.get("stochiometry");
                    if (stochiometry != null) {
                        String[] stochiometry_values = stochiometry.split(",");
                        String svalues = stochiometry_values[0].split(":")[1].trim();
                        if (svalues.equals("0"))
                            participant.setIdentifier(null);
                        else
                            participant.setStochiometry(svalues);
                    }
                    participant.setBioRole((String) part.get("bioRole"));
                    participant.setBioRoleMI((String) part.get("bioRoleMI"));
                    participant.setBioRoleDefinition((String) part.get("bioRoleDefinition"));
                    participant.setInteractorType((String) part.get("interactorType"));
                    participant.setInteractorTypeMI((String) part.get("interactorTypeMI"));
                    participant.setInteractorTypeDefinition((String) part.get("interactorTypeDefinition"));
                    featuresArray = (JSONArray) part.get("linkedFeatures");
                    for ( int k = 0; k < featuresArray.size(); ++k ){
                        features = new ComplexDetailsFeatures();
                        JSONObject jfeatures = (JSONObject) featuresArray.get(k);
                        features.setParticipantId( (String) jfeatures.get("participantId") );
                        features.setFeatureType((String) jfeatures.get("featureType"));
                        features.setFeatureTypeMI((String) jfeatures.get("featureTypeMI"));
                        features.setFeatureTypeDefinition( (String) jfeatures.get("featureTypeDefinition") );
                        rangesArray = (JSONArray) jfeatures.get("ranges");
                        for ( int l = 0; l < rangesArray.size(); ++l ){
                            ranges = features.getRanges();
                            ranges.add( (String) rangesArray.get(l));
                        }
                        participant.getLinkedFeatures().add(features);
                    }
                    featuresArray = (JSONArray) part.get("otherFeatures");
                    for ( int k = 0; k < featuresArray.size(); ++k ){
                        features = new ComplexDetailsFeatures();
                        JSONObject jfeatures = (JSONObject) featuresArray.get(k);
                        features.setParticipantId( (String) jfeatures.get("participantId") );
                        features.setFeatureType((String) jfeatures.get("featureType"));
                        features.setFeatureTypeMI((String) jfeatures.get("featureTypeMI"));
                        features.setFeatureTypeDefinition( (String) jfeatures.get("featureTypeDefinition") );
                        rangesArray = (JSONArray) jfeatures.get("ranges");
                        for ( int l = 0; l < rangesArray.size(); ++l ){
                            ranges = features.getRanges();
                            ranges.add( (String) rangesArray.get(l));
                        }
                        participant.getOtherFeatures().add(features);
                    }
                    details.getParticipants().add(participant);
                }
                // Setting the cross references information
                JSONArray crossArray = (JSONArray) j.get("crossReferences");
                ComplexDetailsCrossReferences crossReference = null;
                for ( int i = 0; i < crossArray.size() ; ++i) {
                    crossReference = new ComplexDetailsCrossReferences();
                    JSONObject cross = (JSONObject) crossArray.get(i);
                    crossReference.setDatabase( (String) cross.get("database") );
                    crossReference.setQualifier( (String) cross.get("qualifier") );
                    crossReference.setIdentifier( (String) cross.get("identifier") );
                    crossReference.setDescription( (String) cross.get("description") );
                    crossReference.setSearchURL( (String) cross.get("searchURL") );
                    crossReference.setDbMI( (String) cross.get("dbMI") );
                    crossReference.setQualifierMI( (String) cross.get("qualifierMI") );
                    crossReference.setDbdefinition( (String) cross.get("dbdefinition") );
                    crossReference.setQualifierDefinition( (String) cross.get("qualifierDefinition") );
                    details.getCrossReferences().add(crossReference);
                }

            }
        }
        return details;
    }

    /****************************/
    /*      Public methods      */
    /****************************/

    public Page getPage(String page, String query, String filters, String facets) throws Exception {
        int max_elements = getNumberOfComplexes(query, filters, facets);
        return new Page(page, this.number, max_elements);
    }

    public ComplexRestResult query( String query,
                                    Page page,
                                    String filters,
                                    String facets,
                                    String queryType) throws Exception {
        String q = createQuery(query, page, filters, facets, queryType);
        ComplexRestResult result = JSONToComplexRestResult(getDataFromWS(q));
        if (result != null) {
            result.setOriginalQuery(query);
        }
        return result;
    }

    public ComplexDetails getDetails( String ac, String queryType ) throws Exception {
        String q = createDetailsQuery(ac, queryType);
        return JSONToComplexDetails(getDataFromWS(q));
    }

    public String getJsonToVisualize(String ac) throws Exception {
        String url = getBaseURL(QueryTypes.EXPORT.value) + ac;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) //503
                throw new ComplexPortalException();
            else{ //404
                throw new Exception();
            }
        }
        return StringEscapeUtils.escapeJavaScript(EntityUtils.toString(response.getEntity(),"UTF-8"));
    }

    public String getFtpUrl() {
        return ftpUrl;
    }

    public String getWSUrl() {
        return WS_URL;
    }
}
