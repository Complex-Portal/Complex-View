/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.webapp.search.struts.view.beans;

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.util.SearchReplace;

import java.util.*;

/**
 * This view bean is used to access the information relating to Features for display by JSPs. For every Component of an
 * Interaction that contains feature information, the will be a feature view bean related to it. TODO: The ranges need
 * handling - a Feature can have more than one...
 *
 * @author Chris Lewington
 * @version $Id$
 */
public class FeatureViewBean extends AbstractViewBean {

    /**
     * The Feature we want the beans for.
     */
    private Feature feature;

    /**
     * Holds the URL to perform subsequent searches from JSPs - used to build 'complete' URLs for use by JSPs
     */
    private String searchURL;

    /**
     * URL for searching for CvFeatureType.
     */
    private String cvFeatureTypeSearchURL = "";

    /**
     * URL for searching for CvFeatureIdentification.
     */
    private String cvFeatureIdentSearchURL = "";

    /**
     * Map of retrieved DB URLs already retrieved from the DB. This is basically a cache to avoid recomputation every
     * time a CvDatabase URL is requested.
     */
    private Map<CvObject,String> dbUrls;


    /**
     * Constructor. Takes a Feature that relates to an Interaction, and wraps the beans for it.
     *
     * @param feature     The Feature we are interested in
     */
    public FeatureViewBean( Feature feature ) {
        super( );
        this.searchURL = searchURL;
        this.feature = feature;
        dbUrls = new HashMap<CvObject, String>();
    }

    /**
     * Adds the shortLabel of the Feature to an internal list used later for highlighting in a display. NOT SURE IF WE
     * STILL NEED THIS!!
     */
    @Override
    public void initHighlightMap() {
        Set<String> set = new HashSet<String>( 1 );
        set.add( feature.getShortLabel() );
        setHighlightMap( set );
    }

    /**
     * Returns the help section.
     */
    @Override
    public String getHelpSection() {
        return "protein.single.view";
    }

    /**
     * Returns the Shortlabel of the given Feature Object
     *
     * @return String contains the shortlabel of the give Feature Object
     */
    public String getFeatureName() {
        return feature.getShortLabel();

    }

    /**
     * Returns the Feature Object itself
     *
     * @return Feature of WrappedFeatureViewBean
     */
    public Feature getFeature() {
        return feature;
    }

    public String getFeatureSummary() {


        return null;
    }

    /**
     * Provides a view bean for any bound Feature.
     *
     * @return featureViewBean a view bean for the Feature bound to this one, or null if there is no bound feature
     */
    public FeatureViewBean getBoundFeatureView() {
        if ( feature.getBoundDomain() != null ) {
            return new FeatureViewBean( feature.getBoundDomain() );
        }
        return null;
    }

    /**
     * Provides the feature type short label.
     *
     * @return String the CvFeatureType shortLabel, or '-' if the feature type itself is null.
     */
    public String getFeatureType( boolean capitalizeFirstLetter ) {

        String type = "-";

        if ( feature.getCvFeatureType() != null ) {
            // get the complete result
            type = feature.getCvFeatureType().getShortLabel();

            if ( capitalizeFirstLetter ) {
                // get the first char
                String begin = type.substring( 0, 1 );
                // put it to uppercase
                begin = begin.toUpperCase();
                // get the rest and add it to the beginning
                String rest = type.substring( 1, type.length() );
                type = begin + rest;
            }
        }
        return type;
    }

    /**
     * Provides the short label of the feature identification.
     *
     * @return String the CvFeatureIdentification shortLabel, or '-' if the identification object itself is null
     */
    public String getFeatureIdentificationName() {

        if ( feature.getCvFeatureIdentification() != null ) {
            return feature.getCvFeatureIdentification().getShortLabel();
        }
        return "-";
    }

    public String getProteinName() {

        return feature.getComponent().getInteractor().getShortLabel();

    }

    /**
     * Provides the full name of the feature identification.
     *
     * @return String the CvFeatureIdentification full name, or '-' if the identification object itself or its full name
     *         are null
     */
    public String getFeatureIdentFullName() {

        if ( ( feature.getCvFeatureIdentification() != null ) &&
             ( feature.getCvFeatureIdentification().getFullName() != null ) ) {
            return feature.getCvFeatureIdentification().getFullName();
        }
        return "-";
    }

    /**
     * Provides a Collection of Strings with shortlabels of the Xref of this specific Feature
     *
     * @return a Collection of String with the shortlabel of the Xref of the wrapped Feature
     */
    public Collection<FeatureXref> getFeatureXrefs() {
        return feature.getXrefs();

    }

    /**
     * Provides a String representation of a URL to perform a search on CvFeatureType
     *
     * @return String a String representation of a search URL link for CvFeatureType.
     */
    public String getCvFeatureTypeSearchURL() {

        if ( ( cvFeatureTypeSearchURL == "" ) && ( feature.getCvFeatureType() != null ) ) {
            //set it on the first call
            //get the CvInteraction object and pull out its AC
            cvFeatureTypeSearchURL = searchURL + feature.getCvFeatureType().getAc()
                                     + "&amp;searchClass=CvFeatureType" + "&filter=ac";
        }
        return cvFeatureTypeSearchURL;
    }

    public boolean hasCvFeatureIdentification() {
        return feature.getCvFeatureIdentification() != null;
    }

    /**
     * Provides a String representation of a URL to perform a search on CvFeatureIdentification
     *
     * @return String a String representation of a search URL link for CvFeatureIdentification.
     */
    public String getCvFeatureIdentSearchURL() {

        if ( ( cvFeatureIdentSearchURL == "" ) && ( feature.getCvFeatureIdentification() != null ) ) {
            //set it on the first call
            //get the CvInteraction object and pull out its AC
            cvFeatureIdentSearchURL = searchURL + feature.getCvFeatureIdentification().getAc()
                                      + "&amp;searchClass=CvFeatureIdentification" + "&filter=ac";
        }
        return cvFeatureIdentSearchURL;
    }

    /**
     * Provides a String representation of a URL to provide acces to an Xrefs' database (curently via AC). The URL is at
     * present stored via an Annotation for the Xref in the Intact DB itself.
     *
     * @param xref The Xref for which the DB URL is required
     *
     * @return String a String representation of a DB URL link for the Xref, or a '-' if there is no stored URL link for
     *         this Xref
     */
    public String getPrimaryIdURL( Xref xref ) {

        // Check if the id can be hyperlinked
        String searchUrl = dbUrls.get( xref.getCvDatabase() );
        if ( searchUrl == null ) {
            //not yet requested - do it now and cache it..
            Collection<Annotation> annotations = xref.getCvDatabase().getAnnotations();
            Annotation annot = null;
            for ( Iterator<Annotation> it = annotations.iterator(); it.hasNext(); ) {
                annot = it.next();
                if ( annot.getCvTopic().getShortLabel().equals( "search-url" ) ) {
                    //found one - we are done
                    searchUrl = annot.getAnnotationText();
                    break;
                }
            }

            //cache it - even if the URL is null, because it may be
            //requested again
            dbUrls.put( xref.getCvDatabase(), searchUrl );
        }

        //if it isn't null, fill it in properly and return
        if ( searchUrl != null ) {
            //An Xref's primary can't be null - the constructor doesn't allow it..
            searchUrl = SearchReplace.replace( searchUrl, "${ac}", xref.getPrimaryId() );

        }
        return searchUrl;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof FeatureViewBean ) ) {
            return false;
        }

        final FeatureViewBean featureViewBean = (FeatureViewBean) o;

        if ( feature != null ? !feature.equals( featureViewBean.feature ) : featureViewBean.feature != null ) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = ( feature != null ? feature.hashCode() : 0 );
        return result;
    }
}

