/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.sequence;

import org.apache.struts.tiles.ComponentContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorFormI;
import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorMenuFactory;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.CvInteractorType;
import uk.ac.ebi.intact.model.Polymer;
import uk.ac.ebi.intact.model.SequenceChunk;
import uk.ac.ebi.intact.model.util.PolymerFactory;
import uk.ac.ebi.intact.persistence.dao.BioSourceDao;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.InteractorDao;
import uk.ac.ebi.intact.util.Crc64;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sequence edit view bean.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public abstract class SequenceViewBean extends AbstractEditViewBean<Polymer> {

    private static final Log log = LogFactory.getLog(SequenceViewBean.class);

    /**
     * The sequence
     */
    private String mySequence;

    /**
     * The interactor type
     */
    private String myInteractorType;

    /**
     * The organism for the sequence.
     */
    private String myOrganism;

    // Override the super method to initialize this class specific resetting.
    @Override
    public void reset() {
        super.reset();
        // Set fields to null.
        myInteractorType = null;
        myOrganism = null;
        mySequence = null;
    }

    // Override the super method to set the tax id.
    @Override
    public void reset(Polymer polymer) {
        super.reset(polymer);

        // Set the bean data
        myInteractorType = polymer.getCvInteractorType().getShortLabel();
        myOrganism = polymer.getBioSource().getShortLabel();
        mySequence = polymer.getSequence();
    }

    // Override to copy sequence data from the form to the bean.
    @Override
    public void copyPropertiesFrom(EditorFormI editorForm) {
        // Set the common values by calling super first.
        super.copyPropertiesFrom(editorForm);

        // Cast to the sequence form to get sequence data.
        SequenceActionForm seqform = (SequenceActionForm) editorForm;

        myInteractorType = seqform.getInteractorType();
        myOrganism = seqform.getOrganism();
        mySequence = seqform.getSequence();
    }

    // Override to copy sequence data to given form.
    @Override
    public void copyPropertiesTo(EditorFormI form) {
        super.copyPropertiesTo(form);

        // Cast to the sequence form to copy sequence data.
        SequenceActionForm seqform = (SequenceActionForm) form;

        seqform.setInteractorType(myInteractorType);
        seqform.setOrganism(myOrganism);
        seqform.setSequence(mySequence);
    }

    // Override to provide Sequence layout.
    @Override
    public void setLayout(ComponentContext context) {
        context.putAttribute("content", "edit.sequence.layout");
    }

    // Override to provide Sequence help tag.
    @Override
    public String getHelpTag() {
        return "editor.sequence";
    }

    // Getter/Setter methods for attributes.

    public String getSequence() {
        return mySequence;
    }

    /**
     * Nothing need to be saved as Hibernate is saving everything (annotation, xref, sequence...)
     *  thanks to the cascade when you save the Polymer.
     * @param user
     * @throws IntactException
     */
    @Override
    public void persistOthers(EditUserI user) throws IntactException {
    }

    // --------------------- Protected Methods ---------------------------------

    /**
     * Override to provide the menus for this view.
     * @return a map of menus for this view. It consists of common menus for
     * annotation/xref and organism (add or edit).
     */
    @Override
    protected Map<String,List<String>> getMenus(Class editedObject) throws IntactException {
        // The map containing the menus.
        Map<String,List<String>> map = new HashMap<String,List<String>>();

        map.putAll(super.getMenus(editedObject));
//        map.putAll(super.getMenus(Protein.class.getName()));

        String name = EditorMenuFactory.ORGANISM;
        int mode = (myOrganism == null) ? 1 : 0;
        map.put(name, EditorMenuFactory.getInstance().getMenu(name, mode));
        return map;
    }

    // Implements abstract methods
    @Override
    protected void updateAnnotatedObject() throws IntactException {
        // Get the objects using their short label.
        BioSourceDao bioSourceDao = DaoProvider.getDaoFactory().getBioSourceDao();
        BioSource biosrc = bioSourceDao.getByShortLabel(myOrganism);

        CvObjectDao<CvInteractorType> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvInteractorType.class);
        CvInteractorType intType = cvObjectDao.getByShortLabel(myInteractorType);
        // The current polymer
        Polymer polymer = getAnnotatedObject();

        // Have we set the annotated object for the view?
        if (polymer == null) {
            // Not persisted; create a new Polymer using the factory
            polymer = PolymerFactory.factory(IntactContext.getCurrentInstance().getConfig().getInstitution(), biosrc,
                    getShortLabel(), intType);
            setAnnotatedObject(polymer);
        }
        else {
            getAnnotatedObject().setBioSource(biosrc);
            getAnnotatedObject().setCvInteractorType(intType);
        }
        // Set the sequence in the persistOthers method we can safely delete
        // unused sequences.
        
        if (getSequence().length() > 0) {
            getAnnotatedObject().setSequence(getSequence());
            getAnnotatedObject().setCrc64(Crc64.getCrc64(getSequence()));

        }
    }

    protected String getInteractorType() {
        return myInteractorType;
    }
}


