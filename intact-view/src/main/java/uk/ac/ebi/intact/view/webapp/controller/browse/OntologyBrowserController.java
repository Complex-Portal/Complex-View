/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.controller.browse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.FacetParams;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.ContextController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import java.util.HashMap;
import java.util.Map;


/**
 * Base for the controllers used to browse ontologies.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
public abstract class OntologyBrowserController extends BaseController {

    private static final Log log = LogFactory.getLog( OntologyBrowserController.class );

    @Autowired
    private UserQuery userQuery;

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    @Autowired
    private BrowserCache browserCache;

    private TreeNode ontologyTreeNode;

    private TreeNode selectedNode;

    public OntologyBrowserController() {
    }

    protected abstract OntologyTerm createRootTerm(OntologySearcher ontologySearcher);

    public abstract String getFieldName();

    @PostConstruct
    public void init() {
        OntologySearcher ontologySearcher = new OntologySearcher(intactViewConfiguration.getOntologySolrServer());
        ontologyTreeNode = createOntologyTreeModel(createRootTerm(ontologySearcher));
    }

    protected TreeNode createOntologyTreeModel(OntologyTerm rootTerm) {
        final SolrQuery query = userQuery.createSolrQuery();
        final String facetField = getFieldName();

//        if (browserCache.containsKey(facetField, query)) {
//            return browserCache.get(facetField, query);
//        }

        SolrServer solrServer = intactViewConfiguration.getInteractionSolrServer();

        // we copy the query, because we don't want to modify the current query instance.
        // otherwise the cache would not have the same key.
        SolrQuery queryCopy = query.getCopy();
        queryCopy.setRows(0);
        queryCopy.setFacet(true);
        queryCopy.setFacetLimit(Integer.MAX_VALUE);
        queryCopy.setFacetMinCount(1);
        queryCopy.setFacetSort(FacetParams.FACET_SORT_COUNT);
        queryCopy.addFacetField(facetField);

        final QueryResponse queryResponse;

        try {
             if (log.isDebugEnabled()) log.debug("Loading ontology counts: "+queryCopy);

             queryResponse = solrServer.query(queryCopy);
         } catch (Throwable e) {
             addErrorMessage("Problem counting ontology terms: ", e.getMessage());
             e.printStackTrace();
             return null;
         }

        final FacetField field = queryResponse.getFacetField(facetField);

        Map<String,Long> termsCountMap = new HashMap<String,Long>(1024);

        if (field != null && field.getValues() != null) {
            for (FacetField.Count c : field.getValues()) {
                termsCountMap.put(c.getName(), c.getCount());
            }
        }

        OntologyTermWrapper otwRoot = new OntologyTermWrapper(rootTerm, termsCountMap, false);

        TreeNode treeNode = createRootTreeNode(otwRoot);

        //browserCache.put(facetField, query, treeModel);

        return treeNode;
    }

    public void onNodeSelect(NodeSelectEvent evt) {
        SearchController searchController = (SearchController) getBean("searchBean");

        final OntologyTermWrapper otw = (OntologyTermWrapper) evt.getTreeNode().getData();

        userQuery.doAddParamToQuery("AND", getFieldName(), otw.getTerm().getId());

        FacesContext.getCurrentInstance().getApplication().getNavigationHandler()
                .handleNavigation(FacesContext.getCurrentInstance(), null, searchController.doBinarySearchAction());
    }

    protected TreeNode createRootTreeNode(OntologyTermWrapper otwRoot) {
        return new OntologyTermNode(otwRoot, null);
    }

    public TreeNode getOntologyTreeNode() {
        return ontologyTreeNode;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }
}