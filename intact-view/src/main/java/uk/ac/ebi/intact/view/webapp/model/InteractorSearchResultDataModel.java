/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.model;

import com.google.common.collect.Multimap;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearcher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.InteractorIdCount;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.InteractorXref;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;

import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;
import javax.persistence.Query;
import java.util.*;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractorSearchResultDataModel extends LazyDataModel<InteractorWrapper> {

    private static final Log log = LogFactory.getLog(InteractorSearchResultDataModel.class);

    private static String DEFAULT_SORT_COLUMN = "relevancescore";

    private SolrQuery solrQuery;
    private SolrServer solrServer;

    private String[] interactorTypeMis;

    private List<InteractorWrapper> idCounts;
    private int totalCount;

    private int rowIndex = -1;
    private int firstResult = 0;

    private String sortColumn = DEFAULT_SORT_COLUMN;
    private SolrQuery.ORDER sortOrder = SolrQuery.ORDER.asc;

    private Map<String,List<InteractorIdCount>> cache = new LRUMap(5);
    private Map<String,InteractorWrapper> interactorCache = new LRUMap(200);

    public InteractorSearchResultDataModel(SolrServer solrServer, SolrQuery solrQuery, String interactorTypeMi)  {
        this(solrServer, solrQuery, new String[] {interactorTypeMi});
    }

    public InteractorSearchResultDataModel(SolrServer solrServer, SolrQuery solrQuery, String[] interactorTypeMis)  {
        if (solrQuery == null) {
            throw new IllegalArgumentException("Trying to create data model with a null SolrQuery");
        }

        this.solrServer = solrServer;
        this.solrQuery = solrQuery.getCopy();
        this.interactorTypeMis = interactorTypeMis;
    }


    @Override
    public List<InteractorWrapper> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        if (pageSize == 0) pageSize = 20;

        if (solrQuery == null) {
            throw new IllegalStateException("Trying to fetch results for a null SolrQuery");
        }

        String cacheKey = solrQuery+"_"+ Arrays.toString(interactorTypeMis);

        List<InteractorIdCount> allIdCounts;

        if (cache.containsKey(cacheKey)) {
            if (log.isDebugEnabled()) log.debug("Fetching interactors for query (cache hit): "+solrQuery);

            allIdCounts = cache.get(cacheKey);
        } else {
            if (log.isDebugEnabled()) log.debug("Fetching interactors for query: "+solrQuery);

            IntactSolrSearcher searcher = new IntactSolrSearcher(solrServer);
            final Multimap<String,InteractorIdCount> idCountMultimap = searcher.searchInteractors(solrQuery, interactorTypeMis);

            idCounts = new ArrayList<InteractorWrapper>();

            allIdCounts = new ArrayList<InteractorIdCount>(idCountMultimap.values());

            cache.put(cacheKey, allIdCounts);
        }

        totalCount = allIdCounts.size();

        idCounts.clear();

        for (InteractorIdCount iic : allIdCounts.subList(first, Math.min(totalCount, first + pageSize))) {
            idCounts.add(wrap(iic));
        }

        return idCounts;
    }

    public int getRowCount() {
        if (idCounts == null) {
            this.load(0,0, null, SortOrder.ASCENDING, null);
        }
        return totalCount;
    }

    public InteractorWrapper getRowData() {
        if (idCounts == null) {
            return null;
        }

        if (!isRowAvailable()) {
            throw new IllegalArgumentException("row is unavailable");
        }

        InteractorWrapper idCount = idCounts.get(getRowIndex());
        
        return idCount;
    }

    private InteractorWrapper wrap(InteractorIdCount idCount) {
        if (interactorCache.containsKey(idCount.getAc())) {
            return interactorCache.get(idCount.getAc());
        }

        DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Query query = dataContext.getDaoFactory()
                .getEntityManager().createQuery("select i from InteractorImpl i where i.ac = :ac");
        query.setParameter("ac", idCount.getAc());

        Interactor interactor;

        List<Interactor> results = query.getResultList();

        if (results.isEmpty()) {
            Query xrefQuery = dataContext.getDaoFactory()
                    .getEntityManager().createQuery("select i from InteractorImpl i inner join i.xrefs as xref " +
                            "where xref.primaryId = :ac and xref.cvXrefQualifier.identifier = :qualifier");
            xrefQuery.setParameter("ac", idCount.getAc());
            xrefQuery.setParameter("qualifier", CvXrefQualifier.IDENTITY_MI_REF);

            results = xrefQuery.getResultList();
        }

        interactor = results.iterator().next();

        IntactCore.ensureInitializedXrefs(interactor);

        InteractorWrapper wrapper = new InteractorWrapper(interactor, idCount.getCount());

        dataContext.commitTransaction(transactionStatus);

        interactorCache.put(idCount.getAc(), wrapper);

        return wrapper;
    }

    public Object getWrappedData() {
        return idCounts;
    }

    @Override
    public InteractorWrapper getRowData(String rowKey) {
        for (InteractorWrapper iic : idCounts) {
            if (iic.getInteractor().getAc().equals(rowKey)) {
                return iic;
            }
        }
        return null;
    }

    @Override
    public Object getRowKey(InteractorWrapper object) {
        if (object.getInteractor() != null) {
            return object.getInteractor().getAc();
        }

        return null;
    }

    public List<InteractorWrapper> getInteractorIdCounts() {
        return idCounts;
    }

    public SolrQuery getSearchQuery() {
        return solrQuery;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public boolean isAscending() {
        return (sortOrder == SolrQuery.ORDER.asc);
    }

}