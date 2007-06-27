/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.business;


/**
 * Manifest constants for the interaction network components.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public final class Constants {

    /**
     * The name of the logger specific of hierarchview
     */
    public static final String LOGGER_NAME = "hierarchview";

    /**
     * The name of the benchmark logger
     */
    public static final String BENCHMARK_LOGGER_NAME = "benchmark";



    /**
     * Where to find the property file
     */
    public static final String PROPERTY_FILE = "/config/Graph.properties";

    /**
     * The attribute which allows to access the width of the image graph.
     */
    public static final String ATTRIBUTE_LENGTH = "length";

    /**
     * The attribute which allows to access the height of the image graph.
     */
    public static final String ATTRIBUTE_HEIGHT = "height";

    /**
     * The X coordinate of a Node int the image.
     */
    public static final String ATTRIBUTE_COORDINATE_X = "x";

    /**
     * The Y coordinate of a Node int the image.
     */
    public static final String ATTRIBUTE_COORDINATE_Y = "y";

    /**
     * The attribute which allows to access the label of a Node.
     */
    public static final String ATTRIBUTE_LABEL = "label";

    /**
     * The attribute which allows to access the color of a Node.
     */
    public static final String ATTRIBUTE_COLOR_NODE = "colorNode";

    /**
     * The attribute which allows to access the color of a Label.
     */
    public static final String ATTRIBUTE_COLOR_LABEL = "colorLabel";

    /**
     * The attribute which allows to access the visibility of a Node
     */
    public static final String ATTRIBUTE_VISIBLE = "visible";

    /**
     * Key name of the user in the session
     */
    public static final String USER_KEY = "user";

    /**
     * Used as a key to identify a mapping filename (for Castor).
     * the value is defined in the web.xml file
     */
    public static final String MAPPING_FILE = "mappingfile";

    /**
     * Used as a key to identify a datasource class - its value
     * is deifned in the web.xml file as a servlet context parameter
     */
    public static final String DATA_SOURCE = "datasource";
}
