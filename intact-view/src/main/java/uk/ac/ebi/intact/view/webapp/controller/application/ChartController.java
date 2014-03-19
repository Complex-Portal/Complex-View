package uk.ac.ebi.intact.view.webapp.controller.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.view.webapp.IntactViewException;
import uk.ac.ebi.intact.view.webapp.application.SpringInitializedService;
import javax.faces.bean.ApplicationScoped;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Get statistics per month of:
 *  - Interactions
 *  - Binary interactions
 *  - Interactors
 *  - Complexes
 *  - CVs
 *  - Experiments
 *  - Publications
 *
 * This information is retrieved from the ia_statistics table
 * which is populated in every release.
 *
 *
 * @author Rafael Jimenez (rafael@ebi.ac.uk)
 * @version $Id$
 * @since 4.0.2-SNAPSHOT
 */
@ApplicationScoped
public class ChartController extends SpringInitializedService {

    private static final Log log = LogFactory.getLog(ChartController.class);
    private List<StatsEntry> statsEntryList = new ArrayList<StatsEntry>();
    private int entriesCount = 0;

    /* Titles */
    private final String proteinTitle = "Proteins";
    private final String interactionTitle = "Interactions";
    private final String binaryInteractionTitle = "Binary interactions";
    private final String complexTitle = "n-ary interactions";
    private final String experimentTitle = "Experiments";
    private final String termTitle = "Ontology terms";
    private final String publicationTitle = "Publications";

    /* Series */
    private JSONObject proteinSerie;
    private JSONObject interactionSerie;
    private JSONObject binaryInteractionSerie;
    private JSONObject complexSerie;
    private JSONObject experimentSerie;
    private JSONObject termSerie;
    private JSONObject publicationSerie;

    @Autowired
    protected DaoFactory daoFactory;


    public ChartController() {

    }

    @Override
    public void initialize(){
        if (log.isInfoEnabled()) log.info("Calculating Monthly Chart statistics");
        statsEntryList = getStatEntriesFromDb();
        setSeries();
    }

    public synchronized void reload() {
        if (log.isInfoEnabled()) log.info("Calculating Monthly Chart statistics");
        statsEntryList = getStatEntriesFromDb();
        setSeries();
    }


    public int getEntriesCount(){
        return statsEntryList.size();
    }


    public String getJsonTimeChart01(){
        List<JSONObject> serieList = new ArrayList<JSONObject>();
        serieList.add(proteinSerie);
        serieList.add(interactionSerie);
        serieList.add(binaryInteractionSerie);
        serieList.add(complexSerie);
        JSONObject chart = buildJSONChart("timeChart01", "Proteins, Interactions, Binary interactions and n-ary interactions", serieList);
        String jsonString = chart.toString();
        jsonString = jsonString.replaceAll("\"(Date.UTC)(\\(\\d+,\\d+,\\d+\\))\"", "$1$2");  //["Date.UTC(2012,10,24)",62533] to [Date.UTC(2012,10,24),62533]
        return jsonString;
    }

    public String getJsonTimeChart02(){
        List<JSONObject> serieList = new ArrayList<JSONObject>();
        serieList.add(experimentSerie);
        serieList.add(publicationSerie);
        serieList.add(termSerie);
        JSONObject chart = buildJSONChart("timeChart02", "Experiments, Publications and Ontology terms", serieList);
        String jsonString = chart.toString();
        jsonString = jsonString.replaceAll("\"(Date.UTC)(\\(\\d+,\\d+,\\d+\\))\"", "$1$2");  //["Date.UTC(2012,10,24)",62533] to [Date.UTC(2012,10,24),62533]
        return jsonString;
    }

    private JSONObject buildJSONChart(String chartName, String titleValue, List<JSONObject> serieList){
        JSONObject highchart = new JSONObject();
        /* chart */
        JSONObject chart = new JSONObject();
        chart.put("renderTo", chartName);
        //chart.put("type", "spline");
        highchart.put("chart",chart);
        /* credits */
        JSONObject credits = new JSONObject();
        credits.put("enabled", false);
        highchart.put("credits", credits);
        /* title */
        JSONObject title = new JSONObject();
        title.put("text", titleValue);
        highchart.put("title",title);
        /* xAxis */
        JSONObject dateTimeLabelFormats = new JSONObject();
        dateTimeLabelFormats.put("year", "%Y");
        JSONObject xAxis = new JSONObject();
        xAxis.put("type", "datetime");
        xAxis.put("gridLineWidth", 1);
        xAxis.put("tickInterval", 31536000000L); //365 * 24 * 3600 * 1000, // per year
        xAxis.put("dateTimeLabelFormats", dateTimeLabelFormats);
        highchart.put("xAxis",xAxis);
        /* yAxis */
        JSONObject yAxis_title = new JSONObject();
        yAxis_title.put("text", "");
        JSONObject yAxis = new JSONObject();
        yAxis.put("title", yAxis_title);
        yAxis.put("min", 0);
        highchart.put("yAxis",yAxis);
        /* tooltip */
        JSONObject tooltip = new JSONObject();
        tooltip.put("formatter", "function() {return this.y + ' <b>'+ this.series.name +'</b><br/><i>'+ Highcharts.dateFormat('%e. %b %Y', this.x) + '</i>';}");
        highchart.put("tooltip",tooltip);
        /* plotOptions */
        JSONObject marker = new JSONObject();
        marker.put("symbol", "circle");
        JSONObject seriesOptions = new JSONObject();
        seriesOptions.put("marker", marker);
        JSONObject plotOptions = new JSONObject();
        plotOptions.put("series", seriesOptions);
        highchart.put("plotOptions",plotOptions);

        /* series */
        JSONArray series = new JSONArray();
        for(JSONObject serie:serieList){
            series.add(serie);
        }
        highchart.put("series",series);
        return highchart;
    }


    private void setSeries(){
        /* Data */
        JSONArray proteinData = new JSONArray();
        JSONArray interactionData = new JSONArray();
        JSONArray binaryInteractionData = new JSONArray();
        JSONArray complexData = new JSONArray();
        JSONArray experimentData = new JSONArray();
        JSONArray termData = new JSONArray();
        JSONArray publicationData = new JSONArray();
        for(StatsEntry statsEntry:statsEntryList){
            /* Entries */
            JSONArray proteinEntry = new JSONArray();
            proteinEntry.add(statsEntry.getTimestampJavaScript());
            proteinEntry.add(statsEntry.getProteinNumber());
            proteinData.add(proteinEntry);

            JSONArray interactionEntry = new JSONArray();
            interactionEntry.add(statsEntry.getTimestampJavaScript());
            interactionEntry.add(statsEntry.getInteractionNumber());
            interactionData.add(interactionEntry);

            JSONArray binaryInteractionEntry = new JSONArray();
            binaryInteractionEntry.add(statsEntry.getTimestampJavaScript());
            binaryInteractionEntry.add(statsEntry.getBinaryInteractions());
            binaryInteractionData.add(binaryInteractionEntry);

            JSONArray complexEntry = new JSONArray();
            complexEntry.add(statsEntry.getTimestampJavaScript());
            complexEntry.add(statsEntry.getComplexInteractions());
            complexData.add(complexEntry);

            JSONArray experimentEntry = new JSONArray();
            experimentEntry.add(statsEntry.getTimestampJavaScript());
            experimentEntry.add(statsEntry.getExperimentNumber());
            experimentData.add(experimentEntry);

            JSONArray termEntry = new JSONArray();
            termEntry.add(statsEntry.getTimestampJavaScript());
            termEntry.add(statsEntry.getTermNumber());
            termData.add(termEntry);

            JSONArray publicationEntry = new JSONArray();
            publicationEntry.add(statsEntry.getTimestampJavaScript());
            publicationEntry.add(statsEntry.getPublicationCount());
            publicationData.add(publicationEntry);
        }
        /* Series */
        proteinSerie = new JSONObject();
        proteinSerie.put("name",proteinTitle);
        proteinSerie.put("data", proteinData);
        interactionSerie = new JSONObject();
        interactionSerie.put("name",interactionTitle);
        interactionSerie.put("data", interactionData);
        binaryInteractionSerie = new JSONObject();
        binaryInteractionSerie.put("name",binaryInteractionTitle);
        binaryInteractionSerie.put("data", binaryInteractionData);
        complexSerie = new JSONObject();
        complexSerie.put("name",complexTitle);
        complexSerie.put("data", complexData);
        experimentSerie = new JSONObject();
        experimentSerie.put("name",experimentTitle);
        experimentSerie.put("data", experimentData);
        termSerie = new JSONObject();
        termSerie.put("name",termTitle);
        termSerie.put("data", termData);
        publicationSerie = new JSONObject();
        publicationSerie.put("name",publicationTitle);
        publicationSerie.put("data", publicationData);
    }

    private List<StatsEntry> getStatEntriesFromDb(){
        List<StatsEntry> statsEntryList = new ArrayList<StatsEntry>();
        final List<Object[]> rows;
        try {
            /* Since displaying of more than 300 points in the chart gets a bit messy,
            we select around 150 entries by selecting one entry per month (the last entry of the month) */
            String sql = "SELECT * FROM ia_statistics WHERE timestamp IN (SELECT MAX(timestamp) FROM ia_statistics group by to_char(timestamp, 'Month'), to_char(timestamp, 'YYYY')) order by ac ASC";
            final Query query = daoFactory.getEntityManager().createNativeQuery(sql);
            rows = query.getResultList();
        } catch (IntactViewException e) {
            if (log.isInfoEnabled()) log.error("Error querying database");
            throw new IntactViewException("Error querying database: ", e);
        }
        for(Object[] row:rows){
            Integer AC = ((BigDecimal) row[0]).intValue();
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date TIMESTAMP = null;
            try {
                TIMESTAMP = (Date)formatter.parse(String.valueOf(row[1]));
            } catch (ParseException e) {
                throw new IntactViewException("Date parsing exception: "+TIMESTAMP, e);
            }
            Integer PROTEIN_NUMBER = ((BigDecimal) row[2]).intValue();
            Integer INTERACTION_NUMBER = ((BigDecimal) row[3]).intValue();
            Integer BINARY_INTERACTIONS = ((BigDecimal) row[4]).intValue();
            Integer COMPLEX_INTERACTIONS = ((BigDecimal) row[5]).intValue();
            Integer EXPERIMENT_NUMBER = ((BigDecimal) row[6]).intValue();
            Integer TERM_NUMBER = ((BigDecimal) row[7]).intValue();
            Integer PUBLICATION_COUNT = ((BigDecimal) row[8]).intValue();

            statsEntryList.add(new StatsEntry(AC,TIMESTAMP,PROTEIN_NUMBER,INTERACTION_NUMBER,BINARY_INTERACTIONS,
                    COMPLEX_INTERACTIONS,EXPERIMENT_NUMBER,TERM_NUMBER,PUBLICATION_COUNT));
        }

        return statsEntryList;
    }

}
