/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.business.image;

/**
 * Allows to store the image and the associated data :
 * - Image data and infos,
 * - HTML MAP.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

import java.awt.image.BufferedImage;
import java.io.Serializable;

public class ImageBean implements Serializable {

    /**
     * generated image data in order to display the picture
     */
    private transient BufferedImage imageData;

    public BufferedImage getImageData() {
        return imageData;
    }

    public void setImageData(BufferedImage imageData) {
        this.imageData = imageData;
    }



    /**
     * HTML MAP code.
     */
    private String mapCode;

    public void setMapCode (String aMapCode) {
        mapCode = aMapCode;
    }

    public String getMapCode () {
        return mapCode;
    }



    /**
     * Size of the image
     */
    private int imageHeight;
    private int imageWidth;

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }
}

