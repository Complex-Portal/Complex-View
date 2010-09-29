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

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyTermNode extends DefaultTreeNode {

    public OntologyTermNode(OntologyTermWrapper ontologyTermWrapper) {
        this(ontologyTermWrapper, null);
    }

    public OntologyTermNode(OntologyTermWrapper ontologyTermWrapper, TreeNode parent) {
        super(ontologyTermWrapper, null);
        setParent(parent);
    }

    @Override
    public List<TreeNode> getChildren() {
        OntologyTermWrapper otw = getOntologyTermWrapper();
        final List<OntologyTermWrapper> ontologyTermWrappers = otw.getChildren();

        List<TreeNode> treeNodes = new ArrayList<TreeNode>();

        for (OntologyTermWrapper otwChild : ontologyTermWrappers) {
            treeNodes.add(new OntologyTermNode(otwChild, this));
        }

        setChildren(treeNodes);

        return treeNodes;
    }

    @Override
    public boolean isLeaf() {
        OntologyTermWrapper otw = (OntologyTermWrapper) getData();
        return otw.isLeaf();
    }

    protected OntologyTermWrapper getOntologyTermWrapper() {
        return (OntologyTermWrapper) getData();
    }
}
