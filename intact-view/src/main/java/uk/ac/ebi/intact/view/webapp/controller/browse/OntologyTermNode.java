/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
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

import org.apache.solr.client.solrj.SolrServerException;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyTermNode extends DefaultTreeNode {

    private Set<TreeNode> childrenNode = null;

    public OntologyTermNode(OntologyTermWrapper ontologyTermWrapper) {
        this(ontologyTermWrapper, null);
    }

    public OntologyTermNode(OntologyTermWrapper ontologyTermWrapper, TreeNode parent) {
        super(ontologyTermWrapper, null);
        setParent(parent);
    }

    @Override
    public List<TreeNode> getChildren() {
        if (childrenNode != null) {
            return new ArrayList<TreeNode>(childrenNode);
        }

        childrenNode = new LinkedHashSet<TreeNode>();

        OntologyTermWrapper otw = getOntologyTermWrapper();
        List<OntologyTermWrapper> ontologyTermWrappers;
        try {
            ontologyTermWrappers = otw.getChildren();
        } catch (SolrServerException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problem getting term children interaction count", e.getMessage());
            context.addMessage(null, facesMessage);

            ontologyTermWrappers = Collections.EMPTY_LIST;
        }

        for (OntologyTermWrapper otwChild : ontologyTermWrappers) {
            childrenNode.add(new OntologyTermNode(otwChild, this));
        }

        final List<TreeNode> childrenList = new LinkedList<TreeNode>(childrenNode);
        setChildren(childrenList);

        return childrenList;
    }

    @Override
    public boolean isLeaf() {
        OntologyTermWrapper otw = (OntologyTermWrapper) getData();
        try {
            return otw.isLeaf();
        } catch (SolrServerException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problem getting term children interaction count", e.getMessage());
            context.addMessage(null, facesMessage);
        }

        return true;
    }

    protected OntologyTermWrapper getOntologyTermWrapper() {
        return (OntologyTermWrapper) getData();
    }
}
