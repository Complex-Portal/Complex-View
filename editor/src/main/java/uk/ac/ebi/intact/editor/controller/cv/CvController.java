package uk.ac.ebi.intact.editor.controller.cv;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.model.CvDagObject;

import javax.faces.event.ComponentSystemEvent;
import java.util.List;

@Controller
@Scope("conversation.access")
public class CvController extends BaseController {

    private static final Log log = LogFactory.getLog( CvController.class );

    @Autowired
    private DaoFactory daoFactory;	

	private TreeNode root;

    private TreeNode selectedCvObject;

    public TreeNode getRoot() {
		return root;
	}

    @Transactional(readOnly = true)
    public void load(String cvClass, String id)
    {
		Class clazz;
        log.debug("Loading cvClass: " + cvClass);
		try
		{
			clazz = Class.forName(cvClass);

			CvObjectDao<CvDagObject> cvDAO = daoFactory.getCvObjectDao(clazz);
			CvDagObject rootCv = cvDAO.getByPsiMiRef(id);

            if (rootCv == null) {
                throw new IllegalArgumentException("Root does not exist: "+cvClass+" ID: "+id);
            }

			root = buildTreeNode(rootCv, null);

            log.debug("\tLoading completed.");
		}
		catch (ClassNotFoundException e)
		{
			addErrorMessage("Could not load data for class " + cvClass, e.getMessage());
		}
    }
	
	private TreeNode buildTreeNode(CvDagObject cv, TreeNode node) {
		TreeNode childNode = new TreeNode(cv, node);
		
		for(CvDagObject child : cv.getChildren())
		{
            if (cv != null) {
			    buildTreeNode(child, childNode);
            }
		}
		
		return childNode;
	}

    public void onNodeSelect(NodeSelectEvent event) {
        selectedCvObject = event.getTreeNode(); 
    }

    public TreeNode getSelectedCvObject() {
        return selectedCvObject;
    }
}
