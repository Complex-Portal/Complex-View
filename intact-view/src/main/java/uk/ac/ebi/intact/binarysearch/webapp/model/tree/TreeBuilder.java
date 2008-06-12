package uk.ac.ebi.intact.binarysearch.webapp.model.tree;

import org.apache.myfaces.trinidad.model.TreeModel;
import org.apache.myfaces.trinidad.model.ChildPropertyTreeModel;
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
        TreeModel model = new ChildPropertyTreeModel(rootTerm, "children");
        return model;
    }

   
}
