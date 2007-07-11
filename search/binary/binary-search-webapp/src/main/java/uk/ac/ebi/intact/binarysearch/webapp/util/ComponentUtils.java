package uk.ac.ebi.intact.binarysearch.webapp.util;

import org.apache.myfaces.custom.tree2.HtmlTree;
import org.apache.myfaces.custom.tree2.TreeModel;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ComponentUtils
{

    public static UIComponent newTree(TreeModel treeModel, boolean expanded) {
        HtmlTree tree = (HtmlTree) FacesContext.getCurrentInstance().getApplication().createComponent(HtmlTree.COMPONENT_TYPE);
        tree.setModel(treeModel);

        if (expanded) {
            tree.expandAll();
        }

        return tree;
    }
}
