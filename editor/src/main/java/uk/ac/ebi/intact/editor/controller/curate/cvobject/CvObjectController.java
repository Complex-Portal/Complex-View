package uk.ac.ebi.intact.editor.controller.curate.cvobject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.model.DualListModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.controller.curate.cloner.CvObjectIntactCloner;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.CvDagObject;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.CvTopic;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class CvObjectController extends AnnotatedObjectController {

    @Autowired
    private CvObjectService cvObjectService;

    @Autowired
    private EditorCvTermService cvTermService;

    private String ac;
    private CvDagObject cvObject;
    private String cvClassName;

    private String newCvObjectType;

    private boolean isTopic;

    private DualListModel<CvObject> parents;

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return cvObject;
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        this.cvObject = (CvDagObject) annotatedObject;

        if (cvObject != null){
            this.ac = annotatedObject.getAc();
        }
    }

    @Override
    public IntactPrimaryObject getJamiObject() {
        return null;
    }

    @Override
    public void setJamiObject(IntactPrimaryObject annotatedObject) {
         // nothing to do
    }

    @Override
    public String clone() {
        return clone(cvObject, new CvObjectIntactCloner());
    }

    public void loadData(ComponentSystemEvent evt) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            if (ac != null) {
                cvObject = (CvDagObject) loadByAc(IntactContext.getCurrentInstance().getDaoFactory().getCvObjectDao(), ac);
            } else if (cvClassName != null) {
                cvObject = newInstance(cvClassName);
            }

            if (cvObject == null) {
                addErrorMessage("No CvObject with this AC", ac);
                return;
            }

            prepareView();
            refreshTabsAndFocusXref();
        }
        generalLoadChecks();
    }

    private void prepareView() {
        if (cvObject != null) {

            List<CvObject> cvObjectsByClass = new ArrayList<CvObject>(cvObjectService.getCvObjectsByClass(cvObject.getClass()));
            List<CvObject> existingParents = new ArrayList<CvObject>(cvObject.getParents());

            parents = new DualListModel<CvObject>(cvObjectsByClass, existingParents);

            if (cvObject instanceof CvTopic) {
                isTopic = true;
            }
        }
    }

    public String newCvObject() {
        if (newCvObjectType != null) {
            CvObject cvObject = newInstance(newCvObjectType);
            setCvObject(cvObject);
        }

        prepareView();

        return navigateToObject(cvObject);
    }

    private CvDagObject newInstance(String cvClassName) {
        CvDagObject obj = null;

        try {
            Class cvClass = Thread.currentThread().getContextClassLoader().loadClass(cvClassName);

            obj = (CvDagObject) cvClass.newInstance();
        } catch (Exception e) {
            addErrorMessage("Problem creating cvObject", "Class "+cvClassName);
            e.printStackTrace();
        }

        getChangesController().markAsUnsaved(obj);

        return obj;
    }

    @Override
    public boolean doSaveDetails() {
        cvObjectService.refresh(null);
        cvTermService.clearAll();
        
        Collection<CvObject> parentsToRemove = CollectionUtils.subtract(cvObject.getParents(), parents.getTarget());
        Collection<CvObject> parentsToAdd = CollectionUtils.subtract(parents.getTarget(), cvObject.getParents());

        for (CvObject parent : parentsToAdd) {
            CvDagObject refreshedParent = (CvDagObject) getDaoFactory().getCvObjectDao().getByAc(parent.getAc());
            refreshedParent.addChild(cvObject);
            getDaoFactory().getCvObjectDao().update(refreshedParent);
            getDaoFactory().getCvObjectDao().update(cvObject);
        }

        for (CvObject parent : parentsToRemove) {
            CvDagObject refreshedParent = (CvDagObject) getDaoFactory().getCvObjectDao().getByAc(parent.getAc());
            refreshedParent.removeChild(cvObject);
            getDaoFactory().getCvObjectDao().update(refreshedParent);
            getDaoFactory().getCvObjectDao().update(cvObject);
        }

        return super.doSaveDetails();
    }

    @Override
    public void postRevert(){
        prepareView();
    }

    public String[] getUsedIn() {
        String usedInArr = super.findAnnotationText(CvTopic.USED_IN_CLASS);

        if (usedInArr == null) {
            return new String[0];
        }

        String[] rawClasses = usedInArr.split(",");
        String[] classes = new String[rawClasses.length];

        for (int i=0; i<rawClasses.length; i++) {
            classes[i] = rawClasses[i].trim();
        }

        return classes;
    }

    public void setUsedIn(String[] usedIn) {
        String usedInArr = StringUtils.join(usedIn, ",");
        super.setAnnotation(CvTopic.USED_IN_CLASS, usedInArr);
    }

    public String getAc() {
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public CvObject getCvObject() {
        return cvObject;
    }

    public void setCvObject(CvObject cvObject) {
        this.cvObject = (CvDagObject) cvObject;
        this.ac = cvObject.getAc();
    }

    public String getCvClassName() {
        return cvClassName;
    }

    public void setCvClassName(String cvClassName) {
        this.cvClassName = cvClassName;
    }

    public boolean isTopic() {
        return isTopic;
    }

    public CvDagObject getParentCvObjects() {
        if (cvObject.getParents().isEmpty()) {
            return null;
        }

        return cvObject.getParents().iterator().next();
    }

    public void setParentCvObjects(CvDagObject parentCvObjects) {
        if (parentCvObjects != null) {

            // very important : DO NOT use Array.AsList because it will be a problem when persisting the data. We can only do a clear operation on lists
            // and the clear method is always called in the corePersister to refresh collections (instead of using the set )

            cvObject.getParents().clear();
            cvObject.getParents().add(parentCvObjects);
        }
    }

    public DualListModel<CvObject> getParents() {
        return parents;
    }

    public void setParents(DualListModel<CvObject> parents) {
        this.parents = parents;
    }

    public String getNewCvObjectType() {
        return newCvObjectType;
    }

    public void setNewCvObjectType(String newCvObjectType) {
        this.newCvObjectType = newCvObjectType;
    }
}
