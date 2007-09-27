/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.cv;

import org.apache.log4j.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.BioSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The CV edit view bean. Currently, this class does not provide any additional
 * functionalities (simply extend the super abstract class to allow to create
 * an instance of this class).
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class CvViewBean extends AbstractEditViewBean<CvObject> {

    protected static final Log LOGGER = LogFactory.getLog(CvViewBean.class);

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

    // --------------------- Protected Methods ---------------------------------

    // Implements abstract methods
    @Override
    protected void updateAnnotatedObject() throws IntactException{
        // The current CV object.
        CvObject cvobj = getAnnotatedObject();

        // Have we set the annotated object for the view?
        if (cvobj == null) {
            // Not persisted; create a new cv object.
            try {
                Constructor ctr = getEditClass().getDeclaredConstructor(
                        Institution.class, String.class);
                cvobj = (CvObject) ctr.newInstance(
                        IntactContext.getCurrentInstance().getConfig().getInstitution(), getShortLabel());
            }
            catch (NoSuchMethodException ne) {
                // Shouldn't happen.
                Logger.getLogger(EditorConstants.LOGGER).error("", new IntactException(ne.getMessage()));
                throw new IntactException(ne.getMessage());
            }
            catch (SecurityException se) {
                Logger.getLogger(EditorConstants.LOGGER).error("", new IntactException(se.getMessage()) );
                throw new IntactException(se.getMessage());
            }
            catch (InstantiationException ie) {
                Logger.getLogger(EditorConstants.LOGGER).error("", new IntactException(ie.getMessage()) );
                throw new IntactException(ie.getMessage());
            }
            catch (IllegalAccessException le) {
                Logger.getLogger(EditorConstants.LOGGER).error("", new IntactException(le.getMessage()) );
                throw new IntactException(le.getMessage());
            }
            catch (InvocationTargetException te) {
                Logger.getLogger(EditorConstants.LOGGER).error("", new IntactException(te.getMessage()) );
                throw new IntactException(te.getMessage());
            }
            setAnnotatedObject(cvobj);
        }
    }

    /**
     * Override to load the menus for this view.
     */
    @Override
    public void loadMenus() throws IntactException {
        myMenus.clear();


        //LOGGER.info("help tag : " + this.getHelpTag());
        myMenus.putAll(super.getMenus(CvObject.class));

//        myMenus = super.getMenus();
//        myMenus = super.getMenus(CvObject.class.getName());//EditorMenuFactory.TOPIC);
    }
}
