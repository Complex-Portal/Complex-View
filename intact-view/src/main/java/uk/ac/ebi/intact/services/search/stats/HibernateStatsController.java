package uk.ac.ebi.intact.services.search.stats;

import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import uk.ac.ebi.intact.services.search.SearchBaseController;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@ViewController(viewIds = "/pages/admin/sqlstats.xhtml")
public class HibernateStatsController extends SearchBaseController {

    private Statistics statistics;
    private List<QueryStatsWrapper> queryStatisticsList;

    public HibernateStatsController(){
        HibernateEntityManagerFactory emf = (HibernateEntityManagerFactory) getIntactContext().getConfig().getDefaultDataConfig().getEntityManagerFactory();
        this.statistics = emf.getSessionFactory().getStatistics();

        queryStatisticsList = new ArrayList<QueryStatsWrapper>(statistics.getQueries().length);

        for (String query : statistics.getQueries()) {
            queryStatisticsList.add(new QueryStatsWrapper(query));
        }
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public List<QueryStatsWrapper> getQueryStatisticsList() {
        return queryStatisticsList;
    }

    public class QueryStatsWrapper {

        private String query;

        public QueryStatsWrapper(String query) {
            this.query = query;
        }

        public String getQuery() {
            return query;
        }

        public QueryStatistics getQueryStats() {
            return statistics.getQueryStatistics(query);
        }

        public long getExecutionCount() {
            return getQueryStats().getExecutionCount();
        }

        public long getCacheHitCount() {
            return getQueryStats().getCacheHitCount();
        }

        public long getCachePutCount() {
            return getQueryStats().getCachePutCount();
        }

        public long getCacheMissCount() {
            return getQueryStats().getCacheMissCount();
        }

        public long getExecutionRowCount() {
            return getQueryStats().getExecutionRowCount();
        }

        public long getExecutionAvgTime() {
            return getQueryStats().getExecutionAvgTime();
        }

        public long getExecutionMaxTime() {
            return getQueryStats().getExecutionMaxTime();
        }

        public long getExecutionMinTime() {
            return getQueryStats().getExecutionMinTime();
        }
    }
}
