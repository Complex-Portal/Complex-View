/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.hierarchview.business.image;

import java.awt.*;
import java.util.StringTokenizer;

/**
 * This class give some usefull methods
 * 
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class Utilities {
    /**
     * Parses the string (a number) and returns the content as an integer.
     * 
     * If the string is not a number or not initialised the default integer is
     * returned.
     * 
     * @param property the string to parse
     * @param defaultProperty the default integer
     * @return an integer given by the string
     */
    public static int parseProperty(String property, int defaultProperty) {
        try {
            return Integer.parseInt( property );
        }
        catch ( NumberFormatException e ) {
            return defaultProperty;
        }
    }
    
    /**
     * Parses the string (a number) and returns the content as a float.
     * 
     * If the string is not a number or not initialised the default float is
     * returned.
     * 
     * @param property the string to parse
     * @param defaultProperty the default float
     * @return a float given by the string
     */
    public static float parseProperty(String property, float defaultProperty) {
        try {
            return Float.parseFloat( property );
        }
        catch ( NumberFormatException e ) {
            return defaultProperty;
        }
    }

    /**
     * Parse a string (number separated by comma) and create a Color Object The
     * string has to be initialized
     * 
     * @param stringColor the string to parse
     * @param defaultRed default RED composite
     * @param defaultGreen default GREEN composite
     * @param defaultBlue default BLUE composite
     * @return the parsed object color
     */
    public static Color parseColor(String stringColor, int defaultRed,
            int defaultGreen, int defaultBlue) {

        StringTokenizer tokens = null;

        int red = defaultRed;
        int green = defaultGreen;
        int blue = defaultBlue;

        tokens = new StringTokenizer( stringColor, "," );

        try {
            if ( tokens.hasMoreTokens() )
                red = new Integer( tokens.nextToken() ).intValue();
            if ( tokens.hasMoreTokens() )
                green = new Integer( tokens.nextToken() ).intValue();
            if ( tokens.hasMoreTokens() )
                blue = new Integer( tokens.nextToken() ).intValue();
        }
        catch ( NumberFormatException e ) {
            // let the default value
        }

        return new Color( red, green, blue );
    }

    /**
     * Parse a string (number separated by comma) and create a color object.
     * 
     * If the string is not initialised or the string cant be parsed the given
     * default color is returned
     * 
     * @param stringColor the string to parse
     * @param defaultColor the default color
     * @return the parsed object color
     */
    public static Color parseColor(String stringColor, final Color defaultColor) {
        if ( stringColor == null ) {
            return defaultColor;
        }
        StringTokenizer tokens = null;

        int red = defaultColor.getRed();
        int green = defaultColor.getGreen();
        int blue = defaultColor.getBlue();

        tokens = new StringTokenizer( stringColor, "," );

        try {
            if ( tokens.hasMoreTokens() )
                red = new Integer( tokens.nextToken() ).intValue();
            if ( tokens.hasMoreTokens() )
                green = new Integer( tokens.nextToken() ).intValue();
            if ( tokens.hasMoreTokens() )
                blue = new Integer( tokens.nextToken() ).intValue();
        }
        catch ( NumberFormatException e ) {
            // let the default value
        }
        
        return new Color( red, green, blue );
    }
}