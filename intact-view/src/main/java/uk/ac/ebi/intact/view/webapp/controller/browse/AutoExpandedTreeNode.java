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
package uk.ac.ebi.intact.view.webapp.controller.browse;

import org.primefaces.model.TreeNode;

import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AutoExpandedTreeNode extends OntologyTermNode {

    private List<TreeNode> childrenNode = null;
    
    public AutoExpandedTreeNode(OntologyTermWrapper data) {
        this(data, null);
    }

    public AutoExpandedTreeNode(OntologyTermWrapper data, TreeNode parent) {
        super(data, parent);
    }

    @Override
    public List<TreeNode> getChildren() {
        if (childrenNode != null){
            return new ArrayList<TreeNode>(childrenNode);
        }
                
        OntologyTermWrapper otw = getOntologyTermWrapper();
        final List<OntologyTermWrapper> children = getChildrenWithMoreThanOneMember(otw);

        if (children.isEmpty()) {
            return createTreeNodes(otw.getChildren());
        }

        return createTreeNodes(children);
    }

    private List<TreeNode> createTreeNodes(List<OntologyTermWrapper> otwChildren) {
         childrenNode = new ArrayList<TreeNode>();

        for (OntologyTermWrapper otwChild : otwChildren) {
            childrenNode.add(new AutoExpandedTreeNode(otwChild, this));
        }

        List<TreeNode> childrenList = new ArrayList<TreeNode>(childrenNode);
        setChildren(childrenList);

        return childrenList;
    }

    private List<OntologyTermWrapper> getChildrenWithMoreThanOneMember(OntologyTermWrapper ontologyTermWrapper) {
        final List<OntologyTermWrapper> children = ontologyTermWrapper.getChildren();

        if (children.size() == 1) {
            OntologyTermWrapper child = children.iterator().next();
            return getChildrenWithMoreThanOneMember(child);
        }

        return children;
    }


}
