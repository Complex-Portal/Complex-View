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
import org.apache.solr.client.solrj.SolrServerException;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;


/**
 * Base for the controllers used to browse ontologies.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
public abstract class OntologyBrowserController extends BaseController {

    private static final Log log = LogFactory.getLog( OntologyBrowserController.class );

    private UserQuery userQuery;

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    private TreeNode ontologyTreeNode;

    private TreeNode selectedNode;
    private SearchController searchController;

    protected boolean useName = false;

    public OntologyBrowserController() {
    }

    protected abstract OntologyTerm createRootTerm(OntologySearcher ontologySearcher);

    public abstract String getFieldName();

    @PostConstruct
    public void init() {
        final SolrServer ontologySolrServer = intactViewConfiguration.getOntologySolrServer();
        OntologySearcher ontologySearcher = new OntologySearcher(ontologySolrServer);
        ontologyTreeNode = createOntologyTreeModel(createRootTerm(ontologySearcher));
    }

    protected TreeNode createOntologyTreeModel(OntologyTerm rootTerm) {
        userQuery = (UserQuery) getBean("userQuery");

        final SolrQuery query = userQuery.createSolrQuery();
        final String facetField = getFieldName();

        SolrServer solrServer = intactViewConfiguration.getInteractionSolrServer();

        OntologyTermWrapper otwRoot = null;
        try {
            otwRoot = new OntologyTermWrapper(rootTerm, solrServer, query, facetField, false, this.useName);
        } catch (SolrServerException e) {
            addErrorMessage("Problem counting ontology terms: ", e.getMessage());
            e.printStackTrace();
            return null;
        }

        TreeNode treeNode = createRootTreeNode(otwRoot);

        return treeNode;
    }

    protected void resetTreeNode(){
        OntologyTermWrapper otwRoot = (OntologyTermWrapper) ontologyTreeNode.getData();
        ontologyTreeNode = createRootTreeNode(otwRoot);
    }

    public void onNodeSelect(NodeSelectEvent evt) {
        searchController = (SearchController) getBean("searchBean");

        final OntologyTermWrapper otw = (OntologyTermWrapper) evt.getTreeNode().getData();

        userQuery = (UserQuery) getBean("userQuery");

        if (otw.isUseName()){
            userQuery.doAddParamToQuery("AND", getFieldName(), otw.getTerm().getName());
        }
        else {
            userQuery.doAddParamToQuery("AND", getFieldName(), otw.getTerm().getId());
        }
        // reset tree node
        resetTreeNode();

        FacesContext.getCurrentInstance().getApplication().getNavigationHandler()
                .handleNavigation(FacesContext.getCurrentInstance(), null, searchController.doBinarySearchAction());
    }

    public void doSelectCvTerm(NodeSelectEvent evt) {
        userQuery = (UserQuery) getBean("userQuery");

        // update user query with selected term
        userQuery.doSelectCvTerm(evt);
        // reset tree node
        resetTreeNode();
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