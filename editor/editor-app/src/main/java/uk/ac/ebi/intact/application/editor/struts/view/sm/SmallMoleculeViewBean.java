/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.editor.struts.view.sm;

import org.apache.log4j.Logger;
import org.apache.struts.tiles.ComponentContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvInteractorType;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.SmallMolecule;
import uk.ac.ebi.intact.model.SmallMoleculeImpl;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Small molecule edit view bean.
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id$
 */
public class SmallMoleculeViewBean extends AbstractEditViewBean<SmallMolecule>  {

    private static final Log log = LogFactory.getLog(SmallMoleculeViewBean.class);

    /**
     * The map of menus for this view.
     */
    private transient Map<String, List<String>> myMenus = new HashMap<String, List<String>>();

    /**
     * Override to provide the menus for this view.
     * @return a map of menus for this view. It consists of common menus for
     * annotation/xref.
     */
    @Override
    public Map<String, List<String>> getMenus() throws IntactException {
        return myMenus;
    }

    public void persistOthers(EditUserI user) throws IntactException {

    }
    // --------------------- Protected Methods ---------------------------------


    // Implements abstract methods
    @Override
    protected void updateAnnotatedObject() throws IntactException {
        // The current small molecule object.
        SmallMolecule sm = getAnnotatedObject();

        // Have we set the annotated object for the view?
        if (sm == null) {
            // Not persisted; create a new small Molecule object.
            try {
                CvInteractorType smInteractorType = getSmallMoleculeInteractorType();
                if( smInteractorType == null){

                    log.error("Could not find the cvInteractorType "
                            + CvInteractorType.SMALL_MOLECULE + " using it's psi-mi id : "
                            + CvInteractorType.SMALL_MOLECULE_MI_REF );
                    throw new IntactException("Could not find the cvInteractorType " + CvInteractorType.SMALL_MOLECULE
                            + " using it's psi-mi id : " + CvInteractorType.SMALL_MOLECULE_MI_REF );

                }
                Constructor ctr = getEditClass().getDeclaredConstructor(String.class, Institution.class, CvInteractorType.class);
                sm = (SmallMoleculeImpl) ctr.newInstance(
                        getShortLabel(),
                        IntactContext.getCurrentInstance().getConfig().getInstitution(),
                        smInteractorType );
                setAnnotatedObject(sm);
            }
            catch (NoSuchMethodException ne) {
                // Shouldn't happen.
                log.error("", new IntactException(ne.getMessage()));
                throw new IntactException(ne.getMessage());
            }
            catch (SecurityException se) {
                log.error("", new IntactException(se.getMessage()) );
                throw new IntactException(se.getMessage());
            }
            catch (InstantiationException ie) {
                log.error("", new IntactException(ie.getMessage()) );
                throw new IntactException(ie.getMessage());
            }
            catch (IllegalAccessException le) {
                log.error("", new IntactException(le.getMessage()) );
                throw new IntactException(le.getMessage());
            }
            catch (InvocationTargetException te) {
                log.error("", new IntactException(te.getMessage()) );
                throw new IntactException(te.getMessage());
            }
        }else{
            CvInteractorType smInteractorType = getSmallMoleculeInteractorType();
            if( smInteractorType != null){
                sm.setCvInteractorType(smInteractorType);
            }else{
                log.error("Could not find the cvInteractorType "
                        + CvInteractorType.SMALL_MOLECULE + " using it's psi-mi id : "
                        + CvInteractorType.SMALL_MOLECULE_MI_REF );
                throw new IntactException("Could not find the cvInteractorType " + CvInteractorType.SMALL_MOLECULE
                        + " using it's psi-mi id : " + CvInteractorType.SMALL_MOLECULE_MI_REF );

            }
        }
    }

    /**
     * Get the small molecule interactorType having the psi-mi id MI:0328.
     * @return the small molecule cvInteractorType.
     */
    private static CvInteractorType getSmallMoleculeInteractorType(){
        CvObjectDao<CvInteractorType> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvInteractorType.class);
        CvInteractorType smallMolecule = cvObjectDao.getByXref(CvInteractorType.SMALL_MOLECULE_MI_REF);
        return smallMolecule;
    }



    /**
     * Override to load the menus for this view.
     */
    @Override
    public void loadMenus() throws IntactException {
        myMenus.clear();

        myMenus.putAll(super.getMenus(SmallMolecule.class));

        //LOGGER.info("help tag : " + this.getHelpTag());
//       myMenus = super.getMenus();
//        myMenus = super.getMenus(CvObject.class.getName());//EditorMenuFactory.TOPIC);
    }

     // Override to provide Experiment layout.
    @Override
    public void setLayout(ComponentContext context) {
        context.putAttribute("content", "edit.sm.layout");
    }
}