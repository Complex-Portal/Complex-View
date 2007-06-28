/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.webapp.search.struts.view.beans;

import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.Xref;
import uk.ac.ebi.intact.searchengine.SearchClass;
import uk.ac.ebi.intact.util.AnnotationFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * Used to provide the information for JSP display relating to a particular BioSource Object.
 * <p/>
 * Its main purpose is to provide very simple beans for display in an initial search result page.
 *
 * @author Michael Kleen, Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version BioSourceViewBean.java Date: Nov 14, 2004 Time: 9:09:19 PM
 */
public class BioSourceViewBean extends AbstractViewBean {

    /**
     * The BioSource Object which is wrapped
     */
    private final BioSource obj;

    /**
     * Cached search URL, set up on first request for it.
     */
    private String objSearchURL;

    /**
     * The intact type of the wrapped BioSource Object. Note that only the interface types are relevant for display
     * purposes - thus any concrete 'Impl' types will be considered to be their interface types.
     */
    private String intactType;


    /**
     * The bean constructor requires an BioSource Object to wrap, plus beans on the context path to the search
     * webapp and the help link.
     *
     * @param obj         The BioSource whose beans are to be displayed
     */
    public BioSourceViewBean( final BioSource obj ) {
        super( );
        this.obj = obj;
    }


    /**
     * not used ! just here to satified the AbstractViewBean
     */
    public void initHighlightMap() {
    }


    /**
     * Returns the help section. Needs to be reviewed.
     *
     * @return a string representation of the help section
     */
    public String getHelpSection() {
        return "protein.single.view";
    }


    /**
     * The intact name for an object is its shortLabel. Required in all view types.
     *
     * @return String the object's Intact name.
     */
    public String getObjIntactName() {
        if ( this.obj.getShortLabel() != null ) {
            return this.obj.getShortLabel();
        }
        return "-";
    }

    /**
     * The BioSource Object's AC. Required in all view types.
     *
     * @return String the AC of the wrapped object.
     */
    public String getObjAc() {
        if ( this.obj.getAc() != null ) {
            return this.obj.getAc();
        }
        return "-";
    }

    /**
     * This is currently assumed to be the BioSource's full name. Required by all view types.
     *
     * @return String a description of the BioSource, or a "-" if there is none.
     */
    public String getObjDescription() {
        if ( this.obj.getFullName() != null ) {
            return this.obj.getFullName();
        }
        return "-";
    }


    /**
     * Provides a String representation of a URL to perform a search on this AnnotatedObject's beans (curently via AC)
     *
     * @return String a String representation of a search URL link for the wrapped AnnotatedObject
     */
    public String getObjSearchURL() {

        if ( objSearchURL == null ) {
            //set it on the first call
            //NB need to get the correct intact type of the wrapped object
            objSearchURL = this.getSearchLink() + this.obj.getAc() + "&amp;searchClass=" + getIntactType();
        }
        return objSearchURL;
    }


    /**
     * Provides direct access to the wrapped AnnotatedObject itself.
     *
     * @return BioSource The reference to the wrapped object.
     */
    public AnnotatedObject getObject() {
        return this.obj;
    }


    /**
     * Convenience method to provide a filtered list of Annotations for a given BioSource Object. Useful in JSP display
     * to apply the same filters of the wrapped BioSource Object.
     *
     * @return Collection the filtered List of Annotations (empty if there are none)
     */
    public Collection getFilteredAnnotations() {
        final ArrayList result = new ArrayList();
        // get all Annotations
        Collection someAnnotations = this.obj.getAnnotations();
        // looks what is in it
        for ( Iterator it = someAnnotations.iterator(); it.hasNext(); ) {
            Annotation annotation = (Annotation) it.next();
            //run through the filter
            if ( false == AnnotationFilter.getInstance().isFilteredOut( annotation ) ) {
                // if it's not in the filter get them
                AnnotationViewBean anAnnotationViewBean = new AnnotationViewBean( annotation );
                result.add( anAnnotationViewBean );
            }
        }

        return result;
    }


    /**
     * Provides access to Annotations of the CVTopics of the  wrraped BioSource stored in SingleViewBeans for the
     * prasentation in the jsp
     *
     * @return Collection with all XrefsViewBeans which  wrapped all Xrefs from the given Object
     */
    public Collection getXrefs() {
        final ArrayList result = new ArrayList();
        // first get all Xrefs
        final Collection someXrefs = this.obj.getXrefs();
        // then create a collection of XrefViewBean
        for ( Iterator iterator = someXrefs.iterator(); iterator.hasNext(); ) {
            final Xref aXref = ( (Xref) iterator.next() );
            result.add( new XrefViewBean( aXref ) );

        }
        return result;
    }

    /**
     * Provides the basic Intact type of the wrapped BioSource (ie no java package beans). NOTE: only the INTERFACE
     * types are provided as these are the only ones of interest in the model - display pages are not interested in
     * objects of type XXXImpl.
     *
     * @return String The intact type of the wrapped object
     */
    /**
     * Provides the basic Intact type of the wrapped AnnotatedObject (ie no java package beans). NOTE: only the
     * INTERFACE types are provided as these are the only ones of interest in the model - display pages are not
     * interested in objects of type XXXImpl. For subclasses of CvObject we only need 'CvObject' for display purposes.
     *
     * @return String The intact type of the wrapped object (eg 'Experiment')
     */
    public String getIntactType() {

        if ( intactType == null ) {
            intactType = SearchClass.valueOfMappedClass(obj.getClass()).getShortName();
        }
        return intactType;

    }

    /**
     * Get the Search URL for the given object.
     *
     * @param anAnnotatedObject the object for which we want the search URL
     *
     * @return the SearchUrl to the given AnnotatadObject
     */
    public String getSearchUrl( final AnnotatedObject anAnnotatedObject ) {

        final String aSearchURL = super.getSearchLink() + anAnnotatedObject.getAc() + "&amp;searchClass=" + getIntactType( anAnnotatedObject );
        return aSearchURL;

    }

    /**
     * Returns a Url based Search Query to the givevn BioSource Object.
     *
     * @return the SearchUrl to the given BioSource Object
     */
    public String getSearchUrl() {

        final String aSearchURL = super.getSearchLink() + this.obj.getAc() + "&amp;searchClass=" + getIntactType( this.obj );
        return aSearchURL;

    }

    /**
     * Returns the Fullname to the givevn BioSource Object.
     *
     * @return the FullName to the given BioSource Object
     */
    public String getFullname() {
        if ( this.obj.getFullName() != null ) {
            return this.obj.getFullName();
        }
        return "-";
    }
}