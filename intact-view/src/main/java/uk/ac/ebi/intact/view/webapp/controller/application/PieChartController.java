package uk.ac.ebi.intact.view.webapp.controller.application;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrException;
import uk.ac.ebi.intact.view.webapp.application.SpringInitializedService;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import javax.faces.bean.ApplicationScoped;
import java.util.*;

/**
 * Get pie chart stats from SOLR
 * So far implemented to get:
 *   - Species
 *   - Interaction types
 *   - Detection methods
 * It relies on the SOLR with binary interactions to bring data (faceting)
 * and on the the SOLR with ontology information to map ontology IDs into names.
 *
 * @author Rafael Jimenez (rafael@ebi.ac.uk)
 * @version $Id$
 * @since 4.0.2-SNAPSHOT
 */
@ApplicationScoped
public class PieChartController extends SpringInitializedService {

    private static final Log log = LogFactory.getLog(PieChartController.class);
    /* TYPE */
    private final String TYPE_FACET_FIELD = "type_s";
    private final String TYPE_TITLE = "Interaction types";
    /* METHOD */
    private final String METHOD_FACET_FIELD = "detmethod_s";
    private final String METHOD_TITLE = "Interaction detection methods";
    /* SPECIE */
    private final String SPECIE_FACET_FIELD = "species_s";
    private final String SPECIE_TITLE = "Species";
    /* PREFIXES */
    private final String[] ontologyPrefixes = {"taxid:", "psi-mi:"};
    private final String solrFiledTermId = "cid";
    private final String solrFiledTermName = "cname";

    /* Series - This is the only input needed to render the Pie Charts */
    private Map<String,JSONObject> series = new HashMap<String,JSONObject>();


    @Autowired
    private IntactViewConfiguration viewConfiguration;


    public PieChartController() {

    }

    @Override
    public void initialize(){
        if (series.size() == 0){
            if (log.isInfoEnabled()) log.info("Calculating Pie Chart statistics");
            QueryResponse interactionsQueryResponse = getFacetInteractionDataFromSolr();
            Set<String> ontologyTermIds = getOntologyTermIds(interactionsQueryResponse.getFacetFields());
            SolrDocumentList ontologyData = getOntologyDataFromSolr(ontologyTermIds);
            Map<String,String> ontologyTerms = mapOntologyTerms(ontologyData);
            setSeries(interactionsQueryResponse, ontologyTerms);
        }
    }

    public synchronized void reload() {
        if (log.isInfoEnabled()) log.info("Calculating Pie Chart statistics");
        QueryResponse interactionsQueryResponse = getFacetInteractionDataFromSolr();
        Set<String> ontologyTermIds = getOntologyTermIds(interactionsQueryResponse.getFacetFields());
        SolrDocumentList ontologyData = getOntologyDataFromSolr(ontologyTermIds);
        Map<String,String> ontologyTerms = mapOntologyTerms(ontologyData);
        setSeries(interactionsQueryResponse, ontologyTerms);
    }


    private QueryResponse getFacetInteractionDataFromSolr() {
        List<FacetField> interactionFacetFields;
        SolrServer interactionSolrServer = viewConfiguration.getInteractionSolrServer();
        /* Prepare QUERY */
        SolrQuery query = new  SolrQuery();
        query.setQuery("*:*");
        query.setRows(0);
        query.setFacet(true);
        // we want all the facet fields with min count = 1
        query.setFacetMinCount(1);
        // Limit the facets returned to a specific number
        query.setFacetLimit(8);
        // we sort the results : the biggest count first
        query.setFacetSort(FacetParams.FACET_SORT_COUNT);
        // Facet fields
        query.addFacetField(METHOD_FACET_FIELD);
        query.addFacetField(TYPE_FACET_FIELD);
        query.addFacetField(SPECIE_FACET_FIELD);


        /* Make QUERY */
        QueryResponse queryResponse;
        try {
            queryResponse = interactionSolrServer.query(query);
        } catch (SolrServerException e) {
            log.error("Problem searching with query in SOLR (interactions): "+query, e);
            throw new IntactSolrException("Problem searching with query in SOLR (interactions): "+query, e);
        }

        return queryResponse;
    }


    private SolrDocumentList getOntologyDataFromSolr(Set<String> ontologyTermsIds) {
        SolrServer ontologySolrServer = viewConfiguration.getOntologySolrServer();
        /* Build lucene query string including all the ontology terms IDs */
        String inputQuery = "";
        for (String ontologyTermId:ontologyTermsIds){
            inputQuery += "\"" + ontologyTermId + "\"" + " OR ";
        }
        inputQuery = inputQuery.substring(0, inputQuery.length() - 4);
        inputQuery = "cid:(" + inputQuery + ")";

        /* Prepare QUERY */
        ModifiableSolrParams queryParameters = new ModifiableSolrParams();
        queryParameters.set("q",inputQuery);
        queryParameters.set("fl", "cname,cid");
        queryParameters.set("rows", 1000);

        /* Make QUERY */
        QueryResponse queryResponse;
        try {
            queryResponse = ontologySolrServer.query(queryParameters);
        } catch (SolrServerException e) {
            log.error("Problem searching with query in SOLR (ontology): "+queryParameters, e);
            throw new IntactSolrException("Problem searching with query in SOLR (ontology): "+queryParameters, e);
        }

        return queryResponse.getResults();
    }


    private Map<String,String> mapOntologyTerms(SolrDocumentList ontologyData){
        Map<String,String> ontologyTerms = new HashMap<String, String>();
        for(SolrDocument doc:ontologyData){
            String termId = (String) doc.getFieldValue(solrFiledTermId);
            String termName = (String) doc.getFieldValue(solrFiledTermName);
            ontologyTerms.put(termId, termName);
        }
        return ontologyTerms;
    }

    private String removeOntologyPrefix(String ontologyTermId){
        /* Filter out prefixes */
        for (int i = 0; i < ontologyPrefixes.length; i++){
            if(ontologyTermId.indexOf(ontologyPrefixes[i]) != -1){
                int start = ontologyTermId.indexOf(ontologyPrefixes[i]) + ontologyPrefixes[i].length();
                ontologyTermId = ontologyTermId.substring(start, ontologyTermId.length());
                break;
            }
        }
        return ontologyTermId;
    }

    /**
     * Set data series taking faceting data
     */
    private Set<String> getOntologyTermIds(List<FacetField> interactionFacetFields){
        Set<String> ontologyTermIds = new HashSet<String>();
        /* Parse QUERY */
        if (interactionFacetFields != null && interactionFacetFields.isEmpty() != true) {
            for (FacetField fieldFacet : interactionFacetFields) {
                if (fieldFacet.getValueCount() > 0) {
                    for (FacetField.Count count : fieldFacet.getValues()) {
                        String ontologyId = removeOntologyPrefix(count.getName());
                        ontologyTermIds.add(ontologyId);
                    }
                }
            }
        }
        return ontologyTermIds;
    }


    /**
     * Set data series taking faceting data
     */
    private void setSeries(QueryResponse queryResponse, Map<String,String> mapOntologyTermIds){
        /* serie example */
        //    {
        //        type: 'pie',
        //        name: 'Browser share',
        //        data: [
        //            ['Firefox',   45.0],
        //            ['IE',       26.8],
        //            {
        //                name: 'Chrome',
        //                y: 12.8,
        //                sliced: true,
        //                selected: true
        //            },
        //            ['Safari',    8.5],
        //            ['Opera',     6.2],
        //            ['Others',   0.7]
        //        ]
        //    }

        List<FacetField> interactionFacetFields = queryResponse.getFacetFields();
        long totalNumberOfInteractions = queryResponse.getResults().getNumFound();

        /* Parse QUERY */
        if (interactionFacetFields != null && interactionFacetFields.isEmpty() != true) {
            for (FacetField fieldFacet : interactionFacetFields) {
                JSONArray data = new JSONArray();
                if (fieldFacet.getValueCount() > 0) {
                    long facetCount = 0;
                    for (FacetField.Count count : fieldFacet.getValues()) {
                        String ontologyName = count.getName();
                        ontologyName = removeOntologyPrefix(ontologyName);
                        if(mapOntologyTermIds.containsKey(ontologyName)){
                            ontologyName = mapOntologyTermIds.get(ontologyName);
                        }
                        JSONObject dataContent = new JSONObject();
                        dataContent.put("name",ontologyName);
                        dataContent.put("y",count.getCount());
                        data.add(dataContent);
                        facetCount += count.getCount();
                    }
                    long othersFieldCount = totalNumberOfInteractions - facetCount;
                    JSONObject dataContent = new JSONObject();
                    dataContent.put("name","others");
                    dataContent.put("y",othersFieldCount);
                    data.add(dataContent);
                }
                JSONObject serie = new JSONObject();
                serie.put("type","pie");
                serie.put("name",fieldFacet.getName());
                serie.put("data", data);
                series.put(fieldFacet.getName(),serie);
            }
        }
    }



    public String getJsonInteractionTypePie(){
        List<JSONObject> serieList = new ArrayList<JSONObject>();
        serieList.add(series.get(TYPE_FACET_FIELD));
        JSONObject pie = buildJSONPie(TYPE_FACET_FIELD, TYPE_TITLE, serieList);
        String jsonString = pie.toString();
        return jsonString;
    }

    public String getJsonInteractionMethodPie(){
        List<JSONObject> serieList = new ArrayList<JSONObject>();
        serieList.add(series.get(METHOD_FACET_FIELD));
        JSONObject pie = buildJSONPie(METHOD_FACET_FIELD, METHOD_TITLE, serieList);
        String jsonString = pie.toString();
        return jsonString;
    }

    public String getJsonInteractionSpeciePie(){
        List<JSONObject> serieList = new ArrayList<JSONObject>();
        serieList.add(series.get(SPECIE_FACET_FIELD));
        JSONObject pie = buildJSONPie(SPECIE_FACET_FIELD, SPECIE_TITLE, serieList);
        String jsonString = pie.toString();
        return jsonString;
    }

    private JSONObject buildJSONPie(String chartName, String titleValue, List<JSONObject> serieList){
        JSONObject highchart = new JSONObject();
        /* chart */
        JSONObject chart = new JSONObject();
        chart.put("renderTo", chartName);
        chart.put("plotBackgroundColor", null);
        chart.put("plotBorderWidth", null);
        chart.put("plotShadow", false);
        highchart.put("chart",chart);
        /* credits */
        JSONObject credits = new JSONObject();
        credits.put("enabled", false);
        highchart.put("credits", credits);
        /* title */
        JSONObject title = new JSONObject();
        title.put("text", titleValue);
        highchart.put("title",title);
        /* tooltip */
        JSONObject tooltip = new JSONObject();
        tooltip.put("pointFormat", "<b>{point.percentage}%</b>");
        tooltip.put("percentageDecimals", 1);
        highchart.put("tooltip",tooltip);
        /* plotOptions */
        JSONObject pieDataLabels = new JSONObject();
        pieDataLabels.put("enabled", true);
        pieDataLabels.put("color", "#000000");
        pieDataLabels.put("connectorColor", "#000000");
        pieDataLabels.put("formatter", "function() {return '<b>'+ this.point.name +'</b>: '+ this.percentage +' %';}");
        JSONObject plotOptions = new JSONObject();
        plotOptions.put("allowPointSelect", true);
        plotOptions.put("cursor", "pointer");
        plotOptions.put("dataLabels", pieDataLabels);
        highchart.put("plotOptions",plotOptions);

        /* series */
        JSONArray series = new JSONArray();
        for(JSONObject serie:serieList){
            series.add(serie);
        }
        highchart.put("series",series);
        return highchart;
    }






}
