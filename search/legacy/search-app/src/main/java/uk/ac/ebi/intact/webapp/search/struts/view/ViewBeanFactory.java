/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
/**
 * This class is a factory to create view bean instances. The beans to generate
 * are stored in a map with the mode class. For example, ExperimentDetailsViewBean is
 * stored under Experiment class.
 * <p>
 * This is a singleton class.
 * </p>
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id:ViewBeanFactory.java 6452 2006-10-16 17:09:42 +0100 (Mon, 16 Oct 2006) baranda $
 */
package uk.ac.ebi.intact.webapp.search.struts.view;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.webapp.search.business.Constants;
import uk.ac.ebi.intact.webapp.search.struts.view.beans.AbstractViewBean;
import uk.ac.ebi.intact.webapp.search.struts.view.beans.BioSourceViewBean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * View Bean Factory.
 *
 * @author Mickael Kleen, Chris L.
 * @since 18.10.2004
 */
public class ViewBeanFactory {

    private transient static final Logger logger = Logger.getLogger( Constants.LOGGER_NAME );

    private static ViewBeanFactory ourInstance;

    ////////////////////////
    // Bean mappings
    ////////////////////////

    /**
     * Mapping related to the detailed view
     */
    private static Map ourBeanToDetailsView = new HashMap();

    /**
     * Mapping related to the single object view
     */
    private static Map<Class,Class<BioSourceViewBean>> ourBeanToSingleItemView = new HashMap<Class, Class<BioSourceViewBean>>();

    /**
     * Maps: Model class -> binary view bean
     */
    private static Map ourBeanToBinaryView = new HashMap();

    /**
     * Maps: Model class -> chunked view bean
     */
    private static Map ourBeanToChunkedView = new HashMap();

    // Stores the beans to create in a map. Make sure to update HtmlBuilder when you
    // add a new view bean.
    static {
        // Details view beans.
//        ourBeanToDetailsView.put ( Experiment.class, ExperimentDetailsViewBean.class );
//        ourBeanToDetailsView.put ( Interaction.class, InteractionDetailsViewBean.class );
//        ourBeanToDetailsView.put ( Protein.class, ProteinDetailsViewBean.class );
//        ourBeanToDetailsView.put ( InteractionProxy.class, InteractionDetailsViewBean.class );
//        ourBeanToDetailsView.put ( ProteinProxy.class, ProteinDetailsViewBean.class );
//        ourBeanToDetailsView.put ( InteractionImpl.class, InteractionDetailsViewBean.class );
//        ourBeanToDetailsView.put ( ProteinImpl.class, ProteinDetailsViewBean.class );

        // Single view bean.
//        ourBeanToSingleItemView.put ( Experiment.class, ExperimentSingleViewBean.class );
//        ourBeanToSingleItemView.put ( Interaction.class, InteractionSingleViewBean.class );
//        ourBeanToSingleItemView.put ( Protein.class, ProteinSingleViewBean.class );
//        ourBeanToSingleItemView.put ( InteractionProxy.class, InteractionSingleViewBean.class );
//        ourBeanToSingleItemView.put ( ProteinProxy.class, ProteinSingleViewBean.class );
//        ourBeanToSingleItemView.put ( InteractionImpl.class, InteractionSingleViewBean.class );
//        ourBeanToSingleItemView.put ( ProteinImpl.class, ProteinSingleViewBean.class );

        ourBeanToSingleItemView.put( CvDatabase.class, BioSourceViewBean.class );
        ourBeanToSingleItemView.put( CvXrefQualifier.class, BioSourceViewBean.class );
        ourBeanToSingleItemView.put( CvTopic.class, BioSourceViewBean.class );
        ourBeanToSingleItemView.put( CvInteraction.class, BioSourceViewBean.class );
        ourBeanToSingleItemView.put( CvInteractionType.class, BioSourceViewBean.class );
        ourBeanToSingleItemView.put( CvExperimentalRole.class, BioSourceViewBean.class );
        ourBeanToSingleItemView.put( CvBiologicalRole.class, BioSourceViewBean.class );
        ourBeanToSingleItemView.put( CvIdentification.class, BioSourceViewBean.class );
        ourBeanToSingleItemView.put( BioSource.class, BioSourceViewBean.class );

        // Binary views.
//        ourBeanToBinaryView.put ( Protein.class, BinaryDetailsViewBean.class );
//        ourBeanToBinaryView.put ( ProteinProxy.class, BinaryDetailsViewBean.class );
//        ourBeanToBinaryView.put ( ProteinImpl.class, BinaryDetailsViewBean.class );

        // chunked single view
//        ourBeanToChunkedView.put( Experiment.class, ExperimentChunkedSingleViewBean.class );
    }

    ///////////////////////////
    // Instanciation methods
    ///////////////////////////

    // Made it private to stop from instantiating this class.
    private ViewBeanFactory() {
    }

    /**
     * Returns the only instance of this class.
     *
     * @return the only instance of this class; always non null value is returned.
     */
    public synchronized static ViewBeanFactory getInstance() {
        if ( ourInstance == null ) {
            ourInstance = new ViewBeanFactory();
        }
        return ourInstance;
    }


    /**
     * Returns the appropriate view bean for given <code>Collection</code> object.
     *
     * @param objects     the <code>Collection</code> of objects to return the view for.
     * @param link        the link to help page.
     * @param contextPath the webapp path.
     *
     * @return the appropriate view for <code>object</code>; null is returned if there is no mapping or an error in
     *         creating an instance of the view.
     */
    public AbstractViewBean getBinaryViewBean( Collection objects, String link, String contextPath ) {

        Object firstItem = objects.iterator().next();
        Class<? extends Object> objsClass = firstItem.getClass();

        logger.info( objsClass );

        Class<BioSourceViewBean> clazz = (Class<BioSourceViewBean>) ourBeanToBinaryView.get( objsClass );
        return getViewBean( clazz, objects, link, contextPath );
    }


    /**
     * Returns the appropriate view bean for given <code>Collection</code> object.
     *
     * @param objects     the <code>Collection</code> of objects to return the view for. Note that for Experiment detail
     *                    views some Experiments may require a tabbed view and so a different bean will be returned (ie
     *                    ExperimentDetailsViewBean).
     * @param link        the link to help page.
     * @param contextPath the webapp path.
     *
     * @return the appropriate view for <code>object</code>; null is returned if there is no mapping, an error in
     *         creating an instance of the view or if the object collection is empty or null.
     */
    public AbstractViewBean getDetailsViewBean( Collection objects, String link,
                                                String contextPath ) {


        if ( ( objects.isEmpty() ) || ( objects == null ) ) {
            logger.info( "ViewBeanFactory: detail view requested for null/empty Collection!" );
            return null;
        }

        Object firstItem = objects.iterator().next();
        Class<? extends Object> objsClass = firstItem.getClass();

        logger.info( objsClass );

        Class<BioSourceViewBean> clazz = (Class<BioSourceViewBean>) ourBeanToDetailsView.get( objsClass );

        return getViewBean( clazz, objects, link, contextPath );
    }

    /**
     * Returns the appropriate view bean for given basic object.
     *
     * @param object the <code>AnnotatedObject</code> to return the view for.
     * @param link   the link to help page.
     *
     * @return the appropriate view for <code>object</code>; null is returned if there is no mapping or an error in
     *         creating an instance of the view.
     */
    public AbstractViewBean getSingleViewBean( AnnotatedObject object, String link, String contextPath ) {

        if ( object == null ) {
            logger.info( "ViewBeanFactory: single view requested for null object!" );
            return null;
        }
        logger.info( object.getClass() );
        Class<BioSourceViewBean> beanClass = ourBeanToSingleItemView.get( object.getClass() );
        return getViewBean( beanClass, object, link, contextPath );
    }


    /**
     * Returns the appropriate view bean for given object. The object can be either a <code>Collection</code> or an
     * <code>AnnotatedObject</code>.
     *
     * @param beanClazz    the type of the bean which will wrap the object to display
     * @param objectToWrap the object to display
     * @param link         the link to help page.
     * @param contextPath  the context path for the help page
     *
     * @return the appropriate view for <code>object</code>; null is returned if there is no mapping or an error in
     *         creating an instance of the view.
     */
    private AbstractViewBean getViewBean( Class<BioSourceViewBean> beanClazz,
                                          Object objectToWrap,
                                          String link,
                                          String contextPath ) {

        if ( beanClazz == null ) {
            return null;
        }
        if ( objectToWrap == null ) {
            logger.info( "ViewBeanFactory: view requested for null object! ViewBean Class " + beanClazz );
            return null;
        }

        try {
            Class<? extends Object> classToWrap = null;

            /* TODO: would be nice to get rid of it
             * If an Experiment (ArrayList) is given, it's not automatically
             * casted to AnnotatedObject (Collection) as required in the constructor
             * of the Bean.
             * So we get a NoSuchMethodException.
             */
            if ( objectToWrap instanceof AnnotatedObject ) {
                classToWrap = AnnotatedObject.class;
            } else {
                classToWrap = Collection.class;
            }

            logger.info( "ClassToWrap affected to: " + classToWrap );

            logger.info( "Ask constructor to: " + beanClazz.getName() );
            logger.info( "Param1: " + classToWrap.getName() + " value: " + objectToWrap );
            logger.info( "Param2: " + String.class.getName() + " value: " + link );
            logger.info( "Param3: " + String.class.getName() + " value: " + contextPath );

            Constructor<BioSourceViewBean> constructor = beanClazz.getConstructor(
                     classToWrap, String.class, String.class );
            return constructor.newInstance(
                    objectToWrap, link, contextPath );

        } catch ( InstantiationException e ) {
            e.printStackTrace();
        } catch ( IllegalAccessException e ) {
            e.printStackTrace();
        } catch ( NoSuchMethodException e ) {
            e.printStackTrace();
        } catch ( InvocationTargetException e ) {
            e.printStackTrace();
        }
        return null;
    }
}