package uk.ac.ebi.intact.application.hierarchview.struts;


/**
 * Manifest constants for the hierarchview application.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public final class StrutsConstants {

    // ******************************************************* Properties files

    /**
     * Where to find the properties file
     */
    public static final String WEB_SERVICE_PROPERTY_FILE = "/config/WebService.properties";

    public static final String HIGHLIGHTING_PROPERTY_FILE = "/config/Highlighting.properties";

    public static final String GRAPH_PROPERTY_FILE = "/config/Graph.properties";

    public static final String SEARCH_PROPERTY_FILE = "/config/Search.properties";

    public static final String GRAPH2MIF_PROPERTY_FILE = "/config/graph2mif.properties";

    // ********************************************************* session keys

    public static final String HOST     = "intact.hierarchview.host";
    public static final String PROTOCOL = "intact.hierarchview.protocol";


    // ********************************************************* Request parameters

    /**
     * The name of the attribute in the session which represents
     * the list of elements selected component in the external
     * application which allows to highlight.
     */
    public static final String ATTRIBUTE_KEYS_LIST = "keys";

    /**
     * The name of the attribute in the session which represents
     * the element selected in the external application which
     * allows to highlight.
     */
    public static final String ATTRIBUTE_KEY_CLICKED = "clicked";

/**
     * The name of the attribute in the session which represents
     * the type of the element selected in the external application which
     * allows to highlight.
     */
    public static final String ATTRIBUTE_KEY_TYPE = "type";

    /**
     * The name of the HTTP attribute to describe the URL of the highlight source.
     */
    public static final String ATTRIBUTE_SOURCE_URL = "url";

    /**
     * Action allowed to create an interaction network
     */
    public static final int CREATE_INTERACTION_NETWORK = 0;

    public static final int ADD_INTERACTION_NETWORK = 1;

    public static final int UPDATE_INTERACTION_NETWORK = 2;
}

























