package uk.ac.ebi.intact.editor.controller.curate.cvobject;

import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.editor.controller.curate.cloner.CvObjectIntactCloner;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.CvDagObject;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.CvTopic;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.SelectItem;
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

    private String ac;
    private CvDagObject cvObject;
    private String cvClassName;
    private List<SelectItem> cvObjectSelectItems;

    private String newCvObjectType;

    private boolean isTopic;

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
        }
        generalLoadChecks();
    }

    private void prepareView() {
        if (cvObject != null) {
            cvObjectSelectItems = new ArrayList<SelectItem>(256);

            final Collection<CvObject> cvObjectsByClass = cvObjectService.getCvObjectsByClass(cvObject.getClass());
            cvObjectSelectItems = cvObjectService.createSelectItems(cvObjectsByClass, "-- Select parent --");

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

        for (CvDagObject parent : cvObject.getParents()) {
            CvDagObject refreshedParent = (CvDagObject) getDaoFactory().getCvObjectDao().getByAc(parent.getAc());
            refreshedParent.addChild(cvObject);
            getDaoFactory().getCvObjectDao().update(refreshedParent);
            getDaoFactory().getCvObjectDao().update(cvObject);
        }

        return super.doSaveDetails();
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

    public List<SelectItem> getCvObjectSelectItems() {
        return cvObjectSelectItems;
    }

    public void setCvObjectSelectItems(List<SelectItem> cvObjectSelectItems) {
        this.cvObjectSelectItems = cvObjectSelectItems;
    }

    public String getNewCvObjectType() {
        return newCvObjectType;
    }

    public void setNewCvObjectType(String newCvObjectType) {
        this.newCvObjectType = newCvObjectType;
    }
}
