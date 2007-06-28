/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.webapp.search.struts.view.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.InteractionImpl;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.DaoUtils;
import uk.ac.ebi.intact.persistence.dao.query.QueryTermConverter;
import uk.ac.ebi.intact.persistence.dao.query.impl.SearchableQuery;
import uk.ac.ebi.intact.persistence.dao.query.impl.StandardQueryTermConverter;
import uk.ac.ebi.intact.webapp.search.SearchWebappContext;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:MainDetailView.java 6452 2006-10-16 17:09:42 +0100 (Mon, 16 Oct 2006) baranda $
 * @since <pre>31-May-2006</pre>
 */
public class MainDetailView  extends AbstractView
{

    /**
     * Log for this class
     */
    public static final Log log = LogFactory.getLog(MainDetailView.class);

    private Experiment experiment;
    private MainDetailViewBean mainDetailViewBean;

    public MainDetailView(HttpServletRequest request, Experiment experiment)
    {
        super(request);
        this.experiment = experiment;

        SearchWebappContext webappContext = SearchWebappContext.getCurrentInstance();

        // pagination preparation here
        // pagination preparation here
        int totalItems = getTotalItems();
        int maxResults = getItemsPerPage();

        if (getCurrentPage() == 0)
        {
            if (totalItems > getItemsPerPage())
            {
                setCurrentPage(1);
            }
        }

        int firstResult = (getCurrentPage() - 1) * getItemsPerPage();

        if (firstResult > totalItems)
        {
            throw new RuntimeException("Page out of bounds: " + getCurrentPage() + " (Item: " + firstResult + " of " + getTotalItems() + ")");
        }

        if (totalItems <= getItemsPerPage()) firstResult = 0;

        // get the interactions to be shown
        // When coming from the partners view, the interactions queried have to be placed in the first position if we are in the first page.
        // We exclude those interactions explicitly from the paginated search, and we will load them directly, and add them to the list
        List<Interaction> interactions = new ArrayList<Interaction>();

        String[] priorInteractionAcs = new String[0];

        // only we will put the searched at the beginning if we are in the first page (or the view is not paginated)
        if (getCurrentPage() <= 1)
        {
            SearchableQuery query = webappContext.getCurrentSearchQuery();

            if (query != null && !query.getAc().getTerms().isEmpty())
            {
                QueryTermConverter converter = new StandardQueryTermConverter();

                String ac = converter.termToString(query.getAc().getTerms().iterator().next());
                if (ac != null && ac.trim().length() > 0)
                {
                    priorInteractionAcs = new String[]  { ac };
                }
            }

            if (log.isDebugEnabled())
            {
                for (String ac : priorInteractionAcs)
                {
                    log.info("Interaction placed prominently: " + ac);
                }
            }


            for (String priorAc : priorInteractionAcs)
            {
                priorAc = DaoUtils.replaceWildcardsByPercent(priorAc);
                Collection<InteractionImpl> inters = getDaoFactory().getInteractionDao().getByAcLike(priorAc, true);
                interactions.addAll(inters);
            }
        }

        // we load the rest of interactions for that experiment
        // if we are in the first page
        if (priorInteractionAcs.length > 0)
        {
            interactions.addAll(getDaoFactory().getExperimentDao()
                .getInteractionsForExperimentWithAcExcludingLike(experiment.getAc(), priorInteractionAcs, firstResult, maxResults - priorInteractionAcs.length));
        }
        else
        {
            interactions.addAll(getDaoFactory().getExperimentDao()
                .getInteractionsForExperimentWithAc(experiment.getAc(), firstResult, maxResults));
        }


        if (log.isDebugEnabled())
        {
            log.debug("Experiment: " + experiment.getAc() + ", showing interactions from " + firstResult + " to " + maxResults);
        }

        // if specific interactions for this experiment are searched, fetch them from the database

        this.mainDetailViewBean = new MainDetailViewBean(experiment, interactions );
    }


    public MainDetailViewBean getMainDetailViewBean()
    {
        return mainDetailViewBean;
    }

    @Override
    public int getTotalItems()
    {
        String prefix = getClass().getName()+"_";

        String attName = prefix+experiment.getAc();

        int totalItems;

        if (getSession().getAttribute(attName) == null)
        {
            totalItems = getDaoFactory().getExperimentDao().countInteractionsForExperimentWithAc(experiment.getAc());

            getSession().setAttribute(attName, totalItems);
        }
        else
        {
            totalItems = (Integer) getSession().getAttribute(attName);
        }

        getRequest().setAttribute(SearchConstants.TOTAL_RESULTS_ATT_NAME, totalItems);

        return totalItems;
    }

    private DaoFactory getDaoFactory()
    {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }
}
