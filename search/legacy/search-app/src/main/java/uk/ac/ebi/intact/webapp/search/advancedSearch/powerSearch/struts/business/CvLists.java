/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.struts.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.util.AnnotationFilter;
import uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.struts.view.bean.CvBean;

import java.util.*;

/**
 * This class provides method to retrieve the list of shortlabel for different CVs.
 *
 * @author Anja Friedrichsen
 * @version $Id$
 */
public class CvLists {

    /**
     * Logger for that class.
     */
    private static final Log logger = LogFactory.getLog(CvLists.class);

    // collection holding per CvDatabase object one CvBean
    private Collection<CvBean> cvDatabase = null;

    // collection holding per CvTopic object one CvBean
    private Collection<CvBean> cvTopic = null;

    // collection holding per CvInteraction object one CvBean
    private Collection<CvBean> cvInteraction = null;

    // collection holding per CvInteractionType object one CvBean
    private Collection<CvBean> cvInteractionType = null;

    // collection holding per CvIdentification object one CvBean
    private Collection<CvBean> cvIdentification = null;

    public static final String NO_CV_INTERACTION_SELECTED = "-no CvInteraction-";
    public static final String NO_CV_INTERACTION_TYPE_SELECTED = "-no CvInteractionType-";
    public static final String ALL_TOPICS_SELECTED = "-all topics-";
    public static final String ALL_DATABASES_SELECTED = "-all databases-";
    public static final String NO_CV_IDENTIFICATION_SELECTED = "-no CvIdentification-";


    public CvLists() {
    }

    /**
     * Collects all objects of the specified type and filter out hidden and obsolete terms.
     *
     * @param clazz the class we want to get all instances of.
     * @param list  the list to populate.
     */
    public void addMenuListItem( Class<? extends CvObject> clazz, Collection<CvBean> list ) {

        List<? extends CvObject> allCvs = IntactContext.getCurrentInstance()
                .getDataContext().getDaoFactory().getCvObjectDao(clazz).getAll(true,true);
        Set<? extends CvObject> cvs = new HashSet<CvObject>(allCvs);

        for (CvObject cv : cvs)
            {
                String ac = cv.getAc();
                String shortlabel = cv.getShortLabel();
                String fullname = cv.getFullName();

                CvBean bean = new CvBean( ac, shortlabel, fullname );
                list.add( bean );
            }
    }

    /**
     * this method retrieves all CvDatabase objects and creates one Bean per object. These beans store information (ac,
     * shortlabel, fullname) about the object and are all added into a collection
     *
     * @return collection containing all CVBeans (one per object)
     *
     * @throws IntactException
     */
    public Collection initCVDatabaseList() throws IntactException {

        if ( this.cvDatabase != null ) {
            // use cache.
            return this.cvDatabase;
        }

        logger.info( "in initCVDatabaseList" );
        this.cvDatabase = new ArrayList<CvBean>();
        // add an empty CV bean for the default case
        CvBean emptyBean = new CvBean( null, ALL_DATABASES_SELECTED, "all databases selected" );
        this.cvDatabase.add( emptyBean );

        addMenuListItem( CvDatabase.class, this.cvDatabase );

        return this.cvDatabase;
    }

    private Collection<CvBean> sortCvBeanAlphabeticaly( Collection<CvBean> list ) {

        if ( list == null ) {
            return Collections.EMPTY_LIST;
        }

        ArrayList toSort = new ArrayList<CvBean>( list );

        // sort alphabetically on shortlabel.
        Collections.sort( toSort, new Comparator() {
            public int compare( Object o1, Object o2 ) {
                CvBean cv1 = (CvBean) o1;
                CvBean cv2 = (CvBean) o2;

                return cv1.getShortlabel().compareTo( cv2.getShortlabel() );
            }
        } );

        return toSort;
    }

    /**
     * this method retrieves all CvTopic objects and creates one Bean per object. These beans store information (ac,
     * shortlabel, fullname) about the object and are all added into a collection
     *
     * @return collection containing all CVBeans (one per object)
     *
     * @throws IntactException
     */
    public Collection<CvBean> initCVTopicList() throws IntactException {

        if ( this.cvTopic != null ) {
            // use cache.
            return this.cvTopic;
        }

        this.cvTopic = new ArrayList<CvBean>();
        // add an empty CV bean for the default case
        CvBean emptyBean = new CvBean( null, ALL_TOPICS_SELECTED, "all topics selected" );
        this.cvTopic.add( emptyBean );

        List<CvTopic> cvTopics = IntactContext.getCurrentInstance()
                .getDataContext().getDaoFactory().getCvObjectDao(CvTopic.class).getAll();

        for (CvTopic cvTo : cvTopics)
        {
            // remove 'no-export' CvTopic
            if ( ! AnnotationFilter.getInstance().isFilteredOut( cvTo ) ) {
                // do not insert the topic 'remark-interal', it should not be seen from outside
                CvBean bean = new CvBean( cvTo.getAc(), cvTo.getShortLabel(), cvTo.getFullName() );
                this.cvTopic.add( bean );
            }
       }


        // sort the list by shortlabel
        this.cvTopic = sortCvBeanAlphabeticaly( this.cvTopic );

        return this.cvTopic;
    }

    /**
     * this method retrieves all CvTopic objects and creates one Bean per object. These beans store information (ac,
     * shortlabel, fullname) about the object and are all added into a collection
     *
     * @return collection containing all CVBeans (one per object)
     *
     * @throws IntactException
     */
    public Collection<CvBean> initCVInteractionList() throws IntactException {

        if ( this.cvInteraction != null ) {
            // use cache.
            return this.cvInteraction;
        }

        this.cvInteraction = new ArrayList<CvBean>();
        // add an empty CV bean for the default case
        CvBean emptyBean = new CvBean( null, NO_CV_INTERACTION_SELECTED, "no interaction selected" );
        this.cvInteraction.add( emptyBean );

        addMenuListItem( CvInteraction.class, this.cvInteraction );

        // sort the list by shortlabel
        this.cvInteraction = sortCvBeanAlphabeticaly( this.cvInteraction );

        return this.cvInteraction;
    }

    /**
     * this method retrieves all CvTopic objects and creates one Bean per object. These beans store information (ac,
     * shortlabel, fullname) about the object and are all added into a collection
     *
     * @return collection containing all CVBeans (one per object)
     *
     * @throws IntactException
     */
    public Collection<CvBean> initCVInteractionTypeList() throws IntactException {

        if ( this.cvInteractionType != null ) {
            // use cache.
            return this.cvInteractionType;
        }

        this.cvInteractionType = new ArrayList<CvBean>();
        // add an empty CV bean for the default case
        CvBean emptyBean = new CvBean( null, NO_CV_INTERACTION_TYPE_SELECTED, "no CvInteractionType selected" );
        this.cvInteractionType.add( emptyBean );

        // sort the list by shortlabel
        addMenuListItem( CvInteractionType.class, this.cvInteractionType );

        return this.cvInteractionType;
    }

    /**
     * this method retrieves all CvIdentification objects and creates one Bean per object. These beans store information
     * (ac, shortlabel, fullname) about the object and are all added into a collection
     *
     * @return collection containing all CVBeans (one per object)
     *
     * @throws IntactException
     */
    public Collection<CvBean> initCVIdentificationList() throws IntactException {

        if ( this.cvIdentification != null ) {
            // use cache.
            return this.cvIdentification;
        }

        this.cvIdentification = new ArrayList<CvBean>();
        // add an empty CV bean for the default case
        CvBean emptyBean = new CvBean( null, NO_CV_IDENTIFICATION_SELECTED, "no identification selected" );
        this.cvIdentification.add( emptyBean );

        addMenuListItem( CvIdentification.class, this.cvIdentification );

        // sort the list by shortlabel
        this.cvIdentification = sortCvBeanAlphabeticaly( this.cvIdentification );

        return this.cvIdentification;
    }
}