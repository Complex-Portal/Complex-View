/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.business.graphdraw;

import java.awt.image.BufferedImage;

/**
 * A bean that holds the imagedata and the corresponding map.
 *
 * @since 27.04.2005
 * @author Samuel Kerrien (Samuel kerrien), Anja Friedrichsen
 * @version $Id$
 */
public class ImageBean {

    /**
     * generated image data in order to display the picture.
     */
    private transient BufferedImage imageData;

    /**
     * HTML map code
     */
    private String imageMap;

    /**
     * name of the CV to generate the image.
     */
    private String cvName;

    //////////////////////////
    // Getters and Setters

    /**
     * Returns the image data value.
     *
     * @return a BufferedImage object representing the image data value.
     *
     * @see #setImageData
     */
    public BufferedImage getImageData() {
        return imageData;
    }

    /**
     * Specifies the image data value.
     *
     * @param imageData a BufferedImage object specifying the image data value.
     *
     * @see #getImageData
     */
    public void setImageData( BufferedImage imageData ) {
        this.imageData = imageData;
    }

    /**
     * Returns the image map value.
     *
     * @return a String representing the image map value.
     *
     * @see #setImageMap
     */
    public String getImageMap() {
        return imageMap;
    }

    /**
     * Specifies the image map value.
     *
     * @param imageMap a String specifying the image map value.
     *
     * @see #getImageMap
     */
    public void setImageMap( String imageMap ) {
        this.imageMap = imageMap;
    }

    /**
     * Gets this object's cv name.
     *
     * @return a String representing the cv name value.
     *
     * @see #setCvName
     */
    public String getCvName() {
        return cvName;
    }

    /**
     * Sets this object's cv name.
     *
     * @param cvName a String specifying the cv name value.
     *
     * @see #getCvName
     */
    public void setCvName( String cvName ) {
        this.cvName = cvName;
    }
}