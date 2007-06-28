/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.webapp.search.struts.view.beans;

import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.Xref;
import uk.ac.ebi.intact.util.SearchReplace;
import uk.ac.ebi.intact.webapp.search.SearchWebappContext;

import java.util.Collection;
import java.util.Iterator;

/**
 * This class provides JSP view information for a particular Xref. It is used in the CvObject View and in the BioSource
 * View.
 *
 * @author Michael Kleen, Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version XrefViewBean.java Date: Nov 24, 2004 Time: 12:50:50 PM
 */
public class XrefViewBean {

    private Xref obj;


    public XrefViewBean( final Xref obj ) {
        this.obj = obj;
    }

    /**
     * not used ! just here to satified the AbstractViewBean
     */
    public void initHighlightMap() {

    }

    /**
     * Returns the help section. Needs to be reviewed.
     */
    public String getHelpSection() {
        return "protein.single.view";
    }

    /**
     * Provides direct access to the wrapped Xref itself.
     *
     * @return Xref The reference to the wrapped object.
     */
    public Xref getObject() {
        return this.obj;
    }

    /**
     * @return the SearchUrl to the given Xref Object
     */
    public String getSearchUrl() {
        String searchUrl = null;
        String id = this.obj.getPrimaryId();

        if ( id != null ) {

            Collection dbAnnotation = obj.getCvDatabase().getAnnotations();
            if ( null != dbAnnotation ) {
                Iterator i = dbAnnotation.iterator();
                while ( i.hasNext() ) {
                    Annotation annot = (Annotation) i.next();
                    if ( annot.getCvTopic().getShortLabel().equals( "search-url" ) ) {
                        searchUrl = annot.getAnnotationText();
                        // replace it, then its linkable 
                        searchUrl = SearchReplace.replace( searchUrl, "${ac}", id );
                        return searchUrl;
                    }
                }
            }
        }
        return "-";
    }

    public String getName() {
        return this.obj.getCvDatabase().getShortLabel();
    }

    public String getXrefQualifierName() {
        if ( null != this.obj.getCvXrefQualifier() ) {
            return this.obj.getCvXrefQualifier().getShortLabel();
        } else {
            return "-";
        }
    }

    public String getPrimaryId() {

        if ( null != this.obj.getPrimaryId() ) {
            return this.obj.getPrimaryId();
        } else {
            return "-";
        }
    }

    public String getPrimaryIdSearchUrl() {

        if ( null != this.obj.getPrimaryId() ) {

            //TODO
            return null;
        } else {
            return "-";
        }

    }

    public String getSecondaryId() {

        if ( null != this.obj.getSecondaryId() ) {
            return this.obj.getSecondaryId();

        } else {
            return "-";
        }

    }

    public String getSecondaryIdSearchUrl() {

        if ( null != this.obj.getSecondaryId() ) {
            //TODO
            return null;

        } else {
            return "-";
        }

    }

    public String getType() {
        return "Type:";

    }

    public String getTypeUrl() {
        return SearchWebappContext.getCurrentInstance().getHelpLink() + "Xref.cvrefType";

    }
}