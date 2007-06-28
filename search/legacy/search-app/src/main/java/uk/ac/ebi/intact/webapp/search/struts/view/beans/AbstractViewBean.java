/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.webapp.search.struts.view.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.context.impl.WebappSession;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.searchengine.SearchClass;
import uk.ac.ebi.intact.webapp.search.SearchWebappContext;

import java.io.Serializable;
import java.util.Set;

/**
 * Abstract class containing some basic operations useful to display beans for Intact. Subclasses might for example be
 * based around requirements for particular Intact types (eg BasicObjects) or perhaps concrete type requiring specific
 * functionality (eg Proteins).
 *
 * @author Chris Lewington
 * @version $Id$
 */
public abstract class AbstractViewBean implements Serializable {

    /**
     * Logger for that class.
     */
    private static final Log logger = LogFactory.getLog(AbstractViewBean.class);

    /**
     * A collection of short labels to highlight.
     */
    private Set highlightMap;

    /**
     * Construst an instance of this class.
     */
    public AbstractViewBean() {
    }

    /**
     * Returns the higlight map.
     *
     * @return map consists of short labels for the current bean.
     *
     * @see #setHighlightMap
     */
    public Set getHighlightMap() {
        if ( highlightMap == null ) {
            initHighlightMap();
        }
        return highlightMap;
    }

    /**
     * Specifies the highlight map value.
     *
     * @param highlightMap set the value of the highlight map.
     *
     * @see #getHighlightMap
     */
    public void setHighlightMap( Set highlightMap ) {
        this.highlightMap = highlightMap;
    }

    /**
     * Returns the url based link to the help section based on the servlet context path.
     *
     * @return String which represents the url based link to the intact help section
     */
    public String getHelpLink() {
        return SearchWebappContext.getCurrentInstance().getHelpLink();
    }

    public String getSearchLink()
    {
        return SearchWebappContext.getCurrentInstance().getSearchUrl();
    }

    /**
     * Returns the context path as string based on the servlet context path.
     *
     * @return String which represents the context path
     */
    public String getContextPath() {
        return ((WebappSession) IntactContext.getCurrentInstance().getSession()).getRequest().getContextPath();
    }

    /**
     * The graph buttons are not displayed by default. Subclasses needs to overwrite it to change that behaviour.
     *
     * @return whether or not the graph buttons are displayed
     */
    public boolean showGraphButtons() {
        return false;
    }

    /**
     * Performs the initialisation of HighlightMap.
     */
    public abstract void initHighlightMap();

    /**
     * Returns the help section value.
     *
     * @return a String representing the help section value
     */
    public abstract String getHelpSection();

    /**
     * String representation of the type of an AnnotatedObject.
     *
     * @param anAnnotatedObject
     * @return String  the intact type of  the annotedObject
     */
    protected String getIntactType( final AnnotatedObject anAnnotatedObject ) {
         return SearchClass.valueOfMappedClass(anAnnotatedObject.getClass()).getShortName();
    }
}