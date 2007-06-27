/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.business.image;

import java.io.Serializable;

/**
 * Storage of the image dimension, allow to keep the height, width, border size ...
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ImageDimension implements Serializable {
    // ---------------------------------------------------------------- Constants
    public static float DEFAULT_BORDER = 5f;

    // ---------------------------------------------------------------- Instance Variables
    private float xmin;
    private float xmax;
    private float ymin;
    private float ymax;

    // ---------------------------------------------------------------- Constructors
    /**
     * initialize all coordinate to zero
     */
    public ImageDimension () {
        xmin = xmax = ymin = ymax = 0;
    }

    // ---------------------------------------------------------------- Accessors
    public float length () { return xmax - xmin; }

    public float height () { return ymax - ymin; }

    public float xmin ()   { return xmin; }

    public float ymin ()   { return ymin; }

    // ---------------------------------------------------------------- public methods
    /**
     * Widen the size if the new coordinate is out of the usable space.
     * After adding a set of points we should have obtain something like below.<br>
     *     +-----------------------4-----+<br>
     *     +                             +<br>
     *     +        1 (x1,y1)            +<br>
     *     +                             +<br>
     *     +                             +<br>
     *     +                             +<br>
     *     +                             +<br>
     *     5                             +<br>
     *     +              6              2 (x2,y2)<br>
     *     +                             +<br>
     *     +                             +<br>
     *     +                             +<br>
     *     +------3----------------------+<br>
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     */
    public void adjust (float x, float y) {
        if (x < xmin) xmin = x;
        if (y < ymin) ymin = y;
        if (x > xmax) xmax = x;
        if (y > ymax) ymax = y;
    }


    /**
     * Adjust width and height according to components size.
     * This is efficient only if node have already been set.<br>
     * <br>
     *     +------------------+    ^ <br>
     *     +                  +    | <br>
     *     +        * (x,y)   +    | height <br>
     *     +                  +    | <br>
     *     +------------------+    - <br>
     *  <br>
     *     <----- width ------> <br>
     *
     * @param width width of the conponent
     * @param height height of the component
     * @param x the X coordinate
     * @param y the Y coordinate
     */
    public void adjustCadre (float width, float height, float x, float y) {
        float tmp = 0;
        if ((tmp = x  - width/2) < xmin)
            xmin = tmp;
        if ((tmp = x  + width/2) > xmax)
            xmax = tmp;
        if ((tmp = y  - height/2) < ymin)
            ymin = tmp;
        if ((tmp = y  + height/2) > ymax)
            ymax = tmp;
    }
}
