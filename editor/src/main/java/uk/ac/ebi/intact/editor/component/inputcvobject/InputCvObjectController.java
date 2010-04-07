package uk.ac.ebi.intact.editor.component.inputcvobject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import uk.ac.ebi.intact.model.CvObject;

import java.util.HashMap;
import java.util.Map;

@Controller
@Scope("conversation.access")
public class InputCvObjectController extends BaseController {

    private static final Log log = LogFactory.getLog( InputCvObjectController.class );

    @Autowired
    private DaoFactory daoFactory;	

	private TreeNode root;

    private Map<String, CvObject> cvObjectsByClientId;

    public InputCvObjectController() {
        cvObjectsByClientId = new HashMap<String, CvObject>();
    }

    public TreeNode getRoot() {
		return root;
	}

    @Transactional(readOnly = true)
    public void load(String cvClass, String id)
    {
		Class clazz;
        log.debug("Loading cvClass: " + cvClass);

        try {
			clazz = Class.forName(cvClass);

			CvObjectDao<CvDagObject> cvDAO = daoFactory.getCvObjectDao(clazz);
			CvDagObject rootCv = cvDAO.getByPsiMiRef(id);

            if (rootCv == null) {
                throw new IllegalArgumentException("Root does not exist: "+cvClass+" ID: "+id);
            }

			root = buildTreeNode(rootCv, null);

            log.debug("\tLoading completed.");
		} catch (ClassNotFoundException e) {
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
        TreeNode selectedNode = event.getTreeNode();

        String treeComponentId = event.getComponent().getId();
        String clientId = treeComponentId.replaceAll("__tree", "");
        
        cvObjectsByClientId.put(clientId, (CvObject) selectedNode.getData());
    }

    public CvObject getSelectedCvObject(String clientId) {
        return cvObjectsByClientId.get(clientId);
    }
}
