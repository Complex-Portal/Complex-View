/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.webapp.search.business;


import uk.ac.ebi.intact.model.IntactObject;

/**
 * This interface represents an Intact user.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk), modified by Chris Lewington
 * @version $Id$
 */
public interface IntactUserIF<T extends IntactObject>
        extends uk.ac.ebi.intact.searchengine.business.IntactUserI
{

    /**
     * Specifies the help link value.
     *
     * @param link a String specifying the help link value
     *
     * @see #getHelpLink
     */
    public void setHelpLink( String link );

    /**
     * Returns the help link value.
     *
     * @return a String representing the help link value
     *
     * @see #setHelpLink
     */
    public String getHelpLink();

    /**
     * Sets this object's search value.
     *
     * @param value a String specifying the search value value
     *
     * @see #getSearchValue
     */
    public void setSearchValue( String value );

    /**
     * Gets this object's search value.
     *
     * @return a String representing the search value value
     *
     * @see #setSearchValue
     */
    public String getSearchValue();

    /**
     * Specifies the search class value.
     *
     * @param searchClass a String specifying the search class value
     *
     * @see #getSearchClass
     */
    public void setSearchClass( Class<T> searchClass );

    /**
     * Returns the search class value.
     *
     * @return a Class representing the search class value
     *
     * @see #setSearchClass
     */
    public Class<T> getSearchClass();

    /**
     * Returns the selected chunk value.
     *
     * @return an int representing the selected chunk value
     *
     * @see #setSelectedChunk
     */
    public int getSelectedChunk();

    /**
     * Specifies the selected chunk value.
     *
     * @param selectedChunk an int specifying the selected chunk value
     *
     * @see #getSelectedChunk
     */
    public void setSelectedChunk( int selectedChunk );

    /**
     * Gets this object's binary value.
     *
     * @return a String representing the binary value value
     *
     * @see #setBinaryValue
     */
    public String getBinaryValue();

    /**
     * Sets this object's binary value.
     *
     * @param binaryValue a String specifying the binary value value
     *
     * @see #getBinaryValue
     */
    public void setBinaryValue( String binaryValue );

    /**
     * Returns the view value.
     *
     * @return a String representing the view value
     *
     * @see #setView
     */
    public String getView();

    /**
     * Specifies the view value.
     *
     * @param viewValue a String specifying the view value
     *
     * @see #getView
     */
    public void setView( String viewValue );

    /**
     * Returns the filter value.
     *
     * @return a String representing the filter value
     *
     * @see #setFilter
     */
    public String getFilter();

    /**
     * Specifies the filter value.
     *
     * @param filterValue a String specifying the filter value
     *
     * @see #getFilter
     */
    public void setFilter( String filterValue );
}