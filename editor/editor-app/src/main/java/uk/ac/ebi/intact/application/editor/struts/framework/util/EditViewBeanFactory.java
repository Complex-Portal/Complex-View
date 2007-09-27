/*
 Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
 All rights reserved. Please see the file LICENSE
 in the root directory of this distribution.
 */

package uk.ac.ebi.intact.application.editor.struts.framework.util;

import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import uk.ac.ebi.intact.application.editor.struts.view.biosrc.BioSourceViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.cv.CvViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.experiment.ExperimentViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.InteractionViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.sequence.NucleicAcidViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.sequence.ProteinViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.sm.SmallMoleculeViewBean;
import uk.ac.ebi.intact.model.*;

/**
 * The factory class to create edit view beans.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class EditViewBeanFactory implements KeyedPoolableObjectFactory {

    protected static final Log log = LogFactory.getLog(EditViewBeanFactory.class);
    /**
     * Only instance of this class.
     */
    private static final EditViewBeanFactory ourInstance = new EditViewBeanFactory();

    /**
     * Handler to the pool.
     */
    private KeyedObjectPool myPool;

    // No instantiation from outside
    private EditViewBeanFactory() {
        GenericKeyedObjectPool.Config config = new GenericKeyedObjectPool.Config();
        config.maxActive = 10;
        config.whenExhaustedAction = GenericKeyedObjectPool.WHEN_EXHAUSTED_GROW;
        myPool = new GenericKeyedObjectPoolFactory(this, config).createPool();
    }

    /**
     * @return the only instance of this class.
     */
    public static EditViewBeanFactory getInstance() {
        return ourInstance;
    }
    
    /**
     * Gets a view from the pool. The menus are pre-loaded
     * @param key the key to get the view from the pool
     * @return the view for <code>key</code>.
     */
    public AbstractEditViewBean borrowObject(Class key) {
        return borrowObject(key, 1);
    }

    /**
     * Gets a view from the pool
     * @param key the key to get the view from the pool
     * @param mode 1 to load menus now or later for any other value.
     * @return the view for <code>key</code>.
     */
    public AbstractEditViewBean borrowObject(Class key, int mode) {
        try {
            AbstractEditViewBean view = (AbstractEditViewBean) myPool.borrowObject(key);
            if (mode == 1) {
                view.loadMenus();
            }
            return view;
        }
        catch (Exception ex) {
            log.error("", ex);
        }
        return null;
    }

    /**
     * Returns the view back to the pool.
     * @param view the view to return to.
     */
    public void returnObject(AbstractEditViewBean view) {
        try {
            myPool.returnObject(view.getEditClass(), view);
        }
        catch (Exception ex) {
            log.error("", ex);
        }
    }

    // ------------------------------------------------------------------------

    // Implement KeyedPoolableObjectFactory methods

    public Object makeObject(Object key) throws Exception {
        // The bean to return.
        AbstractEditViewBean viewbean;

        // Class is the key.
        Class clazz = (Class) key;

        if (BioSource.class.isAssignableFrom(clazz)) {
            viewbean = new BioSourceViewBean();
        }
        else if (Experiment.class.isAssignableFrom(clazz)) {
            viewbean = new ExperimentViewBean();
        }
        else if (Interaction.class.isAssignableFrom(clazz)) {
            viewbean = new InteractionViewBean();
        }
        else if (Feature.class.isAssignableFrom(clazz)) {
            viewbean = new FeatureViewBean();
        }
        else if (NucleicAcid.class.isAssignableFrom(clazz)) {
            viewbean = new NucleicAcidViewBean();
        }
        else if (Protein.class.isAssignableFrom(clazz)) {
            viewbean = new ProteinViewBean();
        }  else if (SmallMolecule.class.isAssignableFrom(clazz)){
            viewbean = new SmallMoleculeViewBean();
        }
        else {
            // Assume it is an CV object.
            viewbean = new CvViewBean();
        }
        return viewbean;
    }

    public void destroyObject(Object key, Object obj) throws Exception {
    }

    public boolean validateObject(Object key, Object arg1) {
        return true;
    }

    public void activateObject(Object key, Object obj) throws Exception {
    }

    public void passivateObject(Object key, Object obj) throws Exception {
        // Clear the view when passivating.
        ((AbstractEditViewBean) obj).reset();
    }
}