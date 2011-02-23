package uk.ac.ebi.intact.editor.controller.curate.organism;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.CvXrefQualifier;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class BioSourceController extends AnnotatedObjectController {

    private String ac;
    private BioSource bioSource;

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return bioSource;
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
       this.bioSource = (BioSource) annotatedObject;
    }

    public void loadData(ComponentSystemEvent evt) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            if (ac != null) {
                bioSource = loadByAc(IntactContext.getCurrentInstance().getDaoFactory().getBioSourceDao(), ac);
            } else {
                bioSource = new BioSource();
            }

        }

        generalLoadChecks();
    }

    public String newOrganism() {
        BioSource bioSource = new BioSource();
        setBioSource(bioSource);

        //getUnsavedChangeManager().markAsUnsaved(bioSource);

        return navigateToObject(bioSource);
    }

    public String getAc() {
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public BioSource getBioSource() {
        return bioSource;
    }

    public void setBioSource(BioSource bioSource) {
        this.bioSource = bioSource;
        this.ac = bioSource.getAc();
    }

    public void setTaxId(String taxId) {
        bioSource.setTaxId(taxId);
        replaceOrCreateXref("MI:0942", CvXrefQualifier.IDENTITY, taxId);
    }

    public String getTaxId() {
        return bioSource.getTaxId();
    }


}
