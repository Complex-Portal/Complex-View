package uk.ac.ebi.intact.view.webapp.controller.admin;

import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.orchestra.viewController.annotations.InitView;
import org.apache.myfaces.orchestra.conversation.ConversationManager;
import org.apache.myfaces.orchestra.conversation.Conversation;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.context.IntactContext;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component("statusBean")
@Scope(value = "request")
@ViewController(viewIds = "/pages/admin/status.xhtml")
public class StatusController extends JpaBaseController {

    private Statistics statistics;
    private List<QueryStatsWrapper> queryStatisticsList;
    private List<Conversation> activeConversations;

    @Autowired
    private EntityManagerFactory emf = null;

    public StatusController(){
        activeConversations = new ArrayList<Conversation>();

        Iterator<Conversation> iterator = ConversationManager.getInstance().iterateConversations();

        while (iterator.hasNext()) {
            Conversation conversation = iterator.next();
            activeConversations.add(conversation);
        }
    }

    @InitView
    public void init() {
        HibernateEntityManagerFactory hemf = (HibernateEntityManagerFactory) emf;
        this.statistics = hemf.getSessionFactory().getStatistics();

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

    public List<Conversation> getActiveConversations() {
        return activeConversations;
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
