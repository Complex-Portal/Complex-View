/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.struts.view.utils;

import java.io.Serializable;

/**
 * Simple JavaBean to represent a source data.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk) & Alexandre Liban (aliban@ebi.ac.uk)
 * @version $Id$
 */
public class SourceBean implements java.lang.Comparable, Serializable
{

    // ----------------------------------------------------------- Instance variables

    private String id;

    private String type;

    private String description;

    private int count;

    private String SourceBrowserUrl;

    private String SourceBrowserGraphUrl;

    private String directHighlightUrl;

    private boolean selected;

    private String applicationPath;

    // ----------------------------------------------------------- Constructors

    public SourceBean (String id,
                       String type,
                       String description,
                       int count,
                       String sourceBrowserUrl,
                       String sourceBrowseGraphUrl,
                       String directHighlightUrl,
                       boolean clickable,
                       String applicationPath) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.count = count;
        this.SourceBrowserUrl = sourceBrowserUrl;
        this.SourceBrowserGraphUrl = sourceBrowseGraphUrl;
        this.directHighlightUrl = directHighlightUrl;
        this.selected = clickable;
        this.applicationPath = applicationPath;
    }

    // ------------------------------------------------------------- Properties

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSourceBrowserUrl() {
        return SourceBrowserUrl;
    }

    public String getSourceBrowserGraphUrl() {
        return SourceBrowserGraphUrl;
    }

    public void setSourceBrowserUrl(String sourceBrowserUrl) {
        this.SourceBrowserUrl = sourceBrowserUrl;
    }

    public void setSourceBrowserGraphUrl(String sourceBrowserGraphUrl) {
        this.SourceBrowserGraphUrl = sourceBrowserGraphUrl;
    }

    public String getDirectHighlightUrl() {
        return directHighlightUrl;
    }

    public void setDirectHighlightUrl(String directHighlightUrl) {
        this.directHighlightUrl = directHighlightUrl;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getApplicationPath() {
        return applicationPath;
    }

    public void setApplicationPath(String applicationPath) {
        this.applicationPath = applicationPath;
    }

    // --------------------------------------------------------- Public Methods


    /**
     * Return a string representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("SourceBean[Id=");
        sb.append(id);
        sb.append(", type=");
        sb.append(type);
        sb.append(", description=");
        sb.append(description);
        sb.append(", count=");
        sb.append(count);
        sb.append(", selected=");
        sb.append(selected);
        sb.append("]");
        return (sb.toString());
    }


    // to allow to sort the list of sources (by count)
    public int compareTo(Object o) {
        int count1 = ( (SourceBean) o ).getCount();
        int count2 = this.getCount();

        if ( count1 > count2 ) return 1;
        else if ( count1 == count2 ) return 0;
        else return -1;
    }
}


