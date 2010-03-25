package uk.ac.ebi.intact.editor.controller.cv;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.model.CvDagObject;

import java.util.List;

@Controller
@Scope("conversation.access")
@ConversationName("general")
public class CvController extends BaseController {

    private static final Log log = LogFactory.getLog( CvController.class );

	private String cvClass;

    @Autowired
    private DaoFactory daoFactory;	
	
	private TreeNode root;

    public String getCvClass() {
		return cvClass;
	}

    public void setCvClass(String cvClass) {
		this.cvClass = cvClass;
	}

    public TreeNode getRoot() {
		return root;
	}

    public void setRoot(TreeNode root) {
		this.root = root;
	}

	public void loadData()
	{

        if (cvClass == null) {
            throw new IllegalStateException("cvClass should not be null");
        }

		Class clazz;
        log.debug("Loading cvClass: " + cvClass);
		try 
		{
			clazz = Class.forName(cvClass);
			CvObjectDao<CvDagObject> cvDAO = daoFactory.getCvObjectDao(clazz);
			List<CvDagObject> cvs = cvDAO.getAll();
			CvDagObject rootCv = getRoot( cvs );
			root = buildTreeNode(rootCv, null);			
		} 
		catch (ClassNotFoundException e) 
		{
			addErrorMessage("Could not load data for class " + cvClass, e.getMessage());			
		}		
	}

    public void load(String cvClazz)
    {
        this.cvClass = cvClazz;
        loadData();
    }

	private CvDagObject getRoot(List<CvDagObject> cvs)
	{
		CvDagObject cv = cvs.get(0);
		while( ! cv.getParents().isEmpty() ) {
			cv = cv.getParents().iterator().next();
		}
		return cv;
	}
	
	private TreeNode buildTreeNode(CvDagObject cv, TreeNode node)
	{
		TreeNode childNode = new TreeNode(cv.getShortLabel(), node);
		
		for(CvDagObject child:cv.getChildren())
		{
			buildTreeNode(child, childNode);
		}
		
		return childNode;
	}
}
