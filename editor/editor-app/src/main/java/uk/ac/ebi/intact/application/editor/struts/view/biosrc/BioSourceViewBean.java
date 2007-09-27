/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.biosrc;

import org.apache.struts.tiles.ComponentContext;
import uk.ac.ebi.intact.application.editor.exception.validation.BioSourceException;
import uk.ac.ebi.intact.application.editor.exception.validation.ValidationException;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorFormI;
import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorMenuFactory;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.CvCellType;
import uk.ac.ebi.intact.model.CvTissue;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.persistence.dao.BioSourceDao;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BioSource edit view bean.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class BioSourceViewBean extends AbstractEditViewBean<BioSource> {

    //private String myBioSourceXref;

    //public String getBioSourceXref() {
    //    return this.myBioSourceXref;
    //}

    //public void setBioSourceXref() {
    //    CvFilterRessources cvFilterRessources= new CvFilterRessources();

    //    this.myBioSourceXref = cvFilterRessources.getBiosourceXref().toString();
    ///}

    /**
     * The tax id.
     */
    private String myTaxId;

    private String myCellType;
    private String myTissue;

    /**
     * The map of menus for this view.
     */
    private transient Map<String, List<String>> myMenus = new HashMap<String, List<String>>();

    // Override the super method to initialize this class specific resetting.
    public void reset() {
        super.reset();
        // Set fields to null.
        setTaxId(null);
        setCellType(null);
        setTissue(null);
        //setBioSourceXref(null);
    }

    // Override the super method to set the tax id.
    @Override
    public void reset(BioSource bio) {
        super.reset(bio);

        //For javascript
        //setBioSourceXref();

        // Must have a tax id.
        setTaxId(bio.getTaxId());

        CvCellType cellType = bio.getCvCellType();
        setCellType(cellType != null ? cellType.getShortLabel() : null);

        CvTissue tissue = bio.getCvTissue();
        setTissue(tissue != null ? tissue.getShortLabel() : null);
    }

    // Override to copy biosource from the form to the bean.
    @Override
    public void copyPropertiesFrom(EditorFormI editorForm) {
        // Set the common values by calling super first.
        super.copyPropertiesFrom(editorForm);

        // Cast to the biosource form to get biosource data.
        BioSourceActionForm bsform = (BioSourceActionForm) editorForm;

        setTaxId(bsform.getTaxId());
        setTissue(bsform.getTissue());
        setCellType(bsform.getCellType());
        //setBioSourceXref();
    }


    // Override to copy BS data to given form.
    @Override
    public void copyPropertiesTo(EditorFormI form) {
        super.copyPropertiesTo(form);

        // Cast to the biosource form to copy biosource data.
        BioSourceActionForm bsform = (BioSourceActionForm) form;

        bsform.setTaxId(getTaxId());
        bsform.setTissue(getTissue());
        bsform.setCellType(getCellType());
      //  bsform.setBioSourceXref(getBioSourceXref());
    }

    // Override to provide BioSource layout.
    @Override
    public void setLayout(ComponentContext context) {
        context.putAttribute("content", "edit.biosrc.layout");
    }

    // Override to provide BioSource help tag.
    @Override
    public String getHelpTag() {
        return "editor.biosource";
    }

    // Override to provide biosource specific sanity checking.
    @Override
    public void sanityCheck() throws ValidationException, IntactException {
        // There should be one unique bisosurce.
        if ((getCellType() == null) && (getTissue() == null)) {
            BioSourceDao bioSourceDao = DaoProvider.getDaoFactory().getBioSourceDao();
            BioSource bs = bioSourceDao.getByTaxonIdUnique(myTaxId);
            if (bs != null) {
                // A BioSource found.
                if (!bs.getAc().equals(getAc())) {
                    // Different biosources.
                    throw new BioSourceException("bs.sanity.taxid.dup",
                                                 "error.bs.sanity.taxid.dup");
                }
            }
        }
    }

    // Getter/Setter methods for attributes.

    public String getTaxId() {
        return myTaxId;
    }

    public void setTaxId(String taxid) {
        myTaxId = taxid;
    }

    public String getCellType() {
        return myCellType;
    }

    public void setCellType(String cellType) {
        myCellType = EditorMenuFactory.normalizeMenuItem(cellType);
    }

    public String getTissue() {
        return myTissue;
    }

    public void setTissue(String tissue) {
        myTissue = EditorMenuFactory.normalizeMenuItem(tissue);
    }

    /**
     * Override to provide menus for this view.
     * @return a map of menus for this view. It consists of common menus for
     * annotation/xref, cell (add) and tissue (add).
     */
    @Override
    public Map getMenus() throws IntactException {
        return myMenus;
    }

    // Implements abstract methods
    @Override
    protected void updateAnnotatedObject() throws IntactException {
        // The current biosource.
        BioSource bs = getAnnotatedObject();

        // Have we set the annotated object for the view?
        if (bs == null) {
            // Not persisted; create a new biosource object.
            bs = new BioSource(IntactContext.getCurrentInstance().getConfig().getInstitution(), getShortLabel(), getTaxId());
            setAnnotatedObject(bs);
        }
        else {
            bs.setTaxId(getTaxId());
        }
        // Set tissue and cell objects.
        bs.setCvTissue(getCvTissue());
        bs.setCvCellType(getCvCellType());
        //bs.setBioSourceXref();
    }

    /**
     * Override to load the menus for this view.
     */
    @Override
    public void loadMenus() throws IntactException {
        // Handler to the menu factory.
        EditorMenuFactory menuFactory = EditorMenuFactory.getInstance();

        // Clear any existing menus first.
        //myMenus.clear();

        myMenus.putAll(super.getMenus(BioSource.class));

        // The cell type menu
        String name = EditorMenuFactory.CELL;
        myMenus.put(name, menuFactory.getMenu(name, 1));

        // The tissue menu.
        name = EditorMenuFactory.TISSUE;
        myMenus.put(name, menuFactory.getMenu(name, 1));
    }

    /**
     * Returns the CvTissue object using the current tissue.
     * @return CvTissue object or null if the current tissue is null.
     * @throws IntactException for errors in retrieving the CvTisue object.
     */
    private CvTissue getCvTissue() throws IntactException {
        if (myTissue == null) {
            return null;
        }
        CvObjectDao<CvTissue> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvTissue.class);
        CvTissue cvTissue = cvObjectDao.getByShortLabel(myTissue);
        return cvTissue;
    }

    /**
     * Returns the CvCellType object using the current cell type.
     * @return CvCellType object or null if the current cell type is null.
     * @throws IntactException for errors in retrieving the CvCellType object.
     */
    private CvCellType getCvCellType() throws IntactException {
        if (myCellType == null) {
            return null;
        }
        CvObjectDao<CvCellType> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvCellType.class);
        CvCellType cvCellType = cvObjectDao.getByShortLabel(myCellType);
        return cvCellType;
    }
}
