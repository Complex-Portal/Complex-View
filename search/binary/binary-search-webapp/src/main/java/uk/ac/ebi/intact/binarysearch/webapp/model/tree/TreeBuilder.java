package uk.ac.ebi.intact.binarysearch.webapp.model.tree;

import org.apache.myfaces.custom.tree2.TreeModel;
import org.apache.myfaces.custom.tree2.TreeModelBase;
import org.apache.myfaces.custom.tree2.TreeNode;
import org.apache.myfaces.custom.tree2.TreeNodeBase;
import uk.ac.ebi.intact.util.ols.Term;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class TreeBuilder
{
    public TreeBuilder() {

    }

    public TreeModel createModel(Term rootTerm) {
        TreeNode rootNode = createNode(rootTerm);
        return new TreeModelBase(rootNode);
    }

    public TreeNode createNode(Term term) {
        if (term == null) {
            return new TreeNodeBase("term", "not available", true);
        }

        boolean isLeaf = term.getChildren() == null || term.getChildren().isEmpty();
        TreeNode baseTerm = new TreeNodeBase("term", term.getName(), isLeaf);

        if (!isLeaf) {
            for (Term childTerm : term.getChildren()) {
                TreeNode childNode = createNode(childTerm);
                baseTerm.getChildren().add(childNode);
            }
        }

        return baseTerm;
    }
}
