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
package uk.ac.ebi.intact.editor.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.model.CvDagObject;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.CvTopic;

import javax.faces.event.ComponentSystemEvent;
import javax.persistence.Query;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
@Scope("conversation.access")
public class InputCvObjectController extends BaseController{

    private static final Log log = LogFactory.getLog( InputCvObjectController.class );

    private TreeNode root;

    private String id;
    private String cvClass;
    private boolean visible;
    private String dialogId;

    private CvObject selected;

    public InputCvObjectController() {
    }

    @Transactional(value = "transactionManager")
    public void load( ComponentSystemEvent evt) {
        log.trace( "Loading CvObject with id '"+id+"'" );

        if (id == null) {
            throw new NullPointerException("id is null");
        }

        //final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        CvObjectDao cvDAO = daoFactory.getCvObjectDao();
        CvDagObject rootCv = (CvDagObject) cvDAO.getByPsiMiRef(id);

        if (rootCv == null) {
            throw new IllegalArgumentException("Root does not exist: " + id);
        }

        root = buildTreeNode(rootCv, null);

        //IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus);

        log.trace( "\tLoading completed. Root: "+root+ "(children="+root.getChildCount()+")" );

    }

    private TreeNode buildTreeNode( CvDagObject cv, TreeNode node ) {
       TreeNode childNode = new DefaultTreeNode(cv, node);

        for ( CvDagObject child : cv.getChildren() ) {
            buildTreeNode( child, childNode );
        }

        return childNode;
    }

    @SuppressWarnings({"JpaQlInspection"})
    @Transactional(value = "transactionManager")
    public String getDescription(CvObject cvObject) {
        if (cvObject == null) return null;
        
        //final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager()
                .createQuery("select a.annotationText from CvObject cv join cv.annotations as a where cv.ac = :cvAc and " +
                        "a.cvTopic.shortLabel = :cvTopicLabel");
        query.setParameter("cvAc", cvObject.getAc());
        query.setParameter("cvTopicLabel", CvTopic.DEFINITION);

        List<String> results = query.getResultList();

        String annot = null;

        if (!results.isEmpty()) {
            annot = results.iterator().next();
        }

        //IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus);

        return annot;
    }

    public TreeNode getRoot() {
        if (root == null) {
            return new DefaultTreeNode(null, null);
        }
        return root;
    }

    //    public TreeModel getRoot() {
////        return root;
//        return (TreeModel) getStateHelper().eval("treeModel");
////
////        if (treeModel != null) {
////            return (TreeNode) treeModel.getWrappedData();
////        }
////
////        return null);
//    }
//
//    public void setRoot(TreeModel root) {
//        getStateHelper().put("treeModel", root);
////        this.root = root;
//    }

    public CvObject getSelected() {
        return selected;
    }

    public void setSelected(CvObject selected) {
        this.selected = selected;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCvClass() {
        return cvClass;
    }

    public void setCvClass(String cvClass) {
        this.cvClass = cvClass;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getDialogId() {
        return dialogId;
    }

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }
}
