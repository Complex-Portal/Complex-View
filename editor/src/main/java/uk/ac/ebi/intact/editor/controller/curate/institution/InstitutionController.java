package uk.ac.ebi.intact.editor.controller.curate.institution;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.hibernate.Hibernate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Institution;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class InstitutionController extends AnnotatedObjectController {

    private String ac;
    private Institution institution;

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return institution;
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        this.institution = (Institution) annotatedObject;

        if (institution != null){
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

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void loadData(ComponentSystemEvent evt) {
        if (!FacesContext.getCurrentInstance().isPostback()) {

            if (ac != null) {
                institution = loadByAc(getDaoFactory().getInstitutionDao(), ac);
                Hibernate.initialize(institution.getAnnotations());
                Hibernate.initialize(institution.getXrefs());
                Hibernate.initialize(institution.getAliases());
            } else {
                institution = new Institution();
            }

            if (institution == null) {
                super.addErrorMessage("Institution does not exist", ac);
                return;
            }

            refreshTabsAndFocusXref();

            if (!Hibernate.isInitialized(institution.getAnnotations())
                    || !Hibernate.isInitialized(institution.getXrefs())
                    || !Hibernate.isInitialized(institution.getAliases())){
                institution = loadByAc(getDaoFactory().getInstitutionDao(), institution.getAc());

                // initialise xrefs
                Hibernate.initialize(institution.getXrefs());
                // initialise aliases
                Hibernate.initialize(institution.getAliases());
                // initialise annotations
                Hibernate.initialize(institution.getAnnotations());
            }
        }

        generalLoadChecks();
    }

    @Override
    public void doPostSave() {
        InstitutionService iac = (InstitutionService) getSpringContext().getBean("institutionService");
        iac.refresh(null);
    }

    public String newInstitution() {
        Institution institution = new Institution();

        //getUnsavedChangeManager().markAsUnsaved(Institution);
        changed();
        return navigateToObject(institution);
    }

    public String getAc() {
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
        this.ac = institution.getAc();
    }

    public String getPostalAddress() {
        return findAnnotationText("postaladdress");
    }

    public void setPostalAddress(String address) {
        updateAnnotation("postaladdress", address);
    }

    public String getUrl() {
        return findAnnotationText(CvTopic.URL_MI_REF);
    }

    public void setUrl(String address) {
        updateAnnotation(CvTopic.URL_MI_REF, address);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String getCautionMessage() {
        if (institution == null){
            return null;
        }
        if (!Hibernate.isInitialized(institution.getAnnotations())){
            return getAnnotatedObjectHelper().findAnnotationText(getDaoFactory().getInstitutionDao().getByAc(institution.getAc()),
                    CvTopic.CAUTION_MI_REF, getDaoFactory());
        }
        return findAnnotationText(CvTopic.CAUTION_MI_REF);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public String getInternalRemarkMessage() {
        if (institution == null){
            return null;
        }
        if (!Hibernate.isInitialized(institution.getAnnotations())){
            return getAnnotatedObjectHelper().findAnnotationText(getDaoFactory().getInstitutionDao().getByAc(institution.getAc()),
                    CvTopic.INTERNAL_REMARK, getDaoFactory());
        }
        return findAnnotationText(CvTopic.INTERNAL_REMARK);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public List collectAnnotations() {
        return super.collectAnnotations();
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public List collectAliases() {
        return super.collectAliases();
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public List collectXrefs() {
        return super.collectXrefs();
    }
}
