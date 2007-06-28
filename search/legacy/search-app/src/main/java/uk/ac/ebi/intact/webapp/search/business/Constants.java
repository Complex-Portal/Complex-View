/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.  
All rights reserved. Please see the file LICENSE 
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.webapp.search.business;

/**
 * Constants of the business package.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public interface Constants {

    /**
     * Name of the Log4J logger.
     */
    public static final String LOGGER_NAME = "search";

    /**
     * Number of items to display when the display is tabbed. ie. count of interactions per page when an experiment
     * contains too many of them.
     */
    public static final int MAX_PAGE_SIZE = 20;

    /**
     * Default value of a chunk when none is selected.
     */
    public static final int NO_CHUNK_SELECTED = -1;

    ////////////////////////////////////////////////////
    // Configuration keys (web.xml, properties files)
    ////////////////////////////////////////////////////

    /**
     * The name of the hierarchView properties file.
     */
    public static final String HV_PROPS = "HierarchView";

    ///////////////////////
    // Session keys
    ///////////////////////

    /**
     * The key to store an Intact Service object.
     */
    public static final String INTACT_SERVICE = "IntactService";

    /**
     * The key to access a user session object.
     */
    public static final String INTACT_USER = "user";

    /**
     * The search criteria key to display in the results JSP.
     */
    public static final String SEARCH_CRITERIA = "searchCriteria";

    /**
     * The view bean for a single CvObject.
     */
    public static final String VIEW_BEAN = "viewBean";

    /**
     * A Collection of ViewBeans to be processed for display.
     */
    public static final String VIEW_BEAN_LIST = "viewBeanList";

    /**
     * Holds a particular viewBean for a large Experiment. Used in requests for tabbed page beans of the wrapped
     * Experiment.
     */
    public static final String LARGE_EXPERIMENT_BEAN = "largeExp";


    /**
     * The most recent user-defined search (ie not from a CvObject link).
     */
    public static final String LAST_VALID_SEARCH = "lastValidSearch";

    /**
     * The label used to identify the search results Collection (often saved in a request).
     */
    public static final String SEARCH_RESULTS = "searchResults";

    ///////////////////////
    // STRUTS forwards
    ///////////////////////

    /**
     * The key to success action.
     */
    public static final String FORWARD_SUCCESS = "success";

    /**
     * Forward to the results page.
     */
    public static final String FORWARD_SESSION_LOST = "sessionLost";

    /**
     * Forward to the too large page if the dataset is too large.
     */
    public static final String TOO_LARGE_DATASET = "tooLarge";

    /**
     * Key to the too large action.
     */
    public static final String FORWARD_TOO_LARGE = "tooLarge";

    /**
     * Forward to the Action responsible for setting up the 'simple' initial view of simple text search results.
     */
    public static final String FORWARD_SIMPLE_ACTION = "simple";

    /**
     * Forward to the Action responsible for setting up a view when clicking on a link.
     */
    public static final String FORWARD_SINGLE_ACTION = "single";

    /**
     * Forward to the Action responsible for setting up a view when clicking on a link.
     */
    public static final String FORWARD_RESULTS = "results";

    /**
     * Forward to the Action responsible for handling a search giving too many items.
     */
    public static final String FORWARD_TOO_MANY_PROTEINS = "tooManyProteins";

    /**
     * Used as a key to identify a page to display when warning are raised from a search.
     */
    public static final String FORWARD_WARNING = "warning";

    /**
     * Used to identify which page a request came from. Mainly needed by Action classes to decide what to do for a given
     * request when the same request may originate from different sources and then require different processing (eg for
     * Protein it should give a partners view if from the main 'simple' page, or the Protein beans if from another
     * link).
     */
    public static final String PAGE_SOURCE = "view";

    /**
     * Identifies the map (stored in the servlet context) which contains the maximum display size values for various
     * intact types.
     */
    public static final String MAX_ITEMS_MAP = "maxMap";

    /**
     * Indetifies the map (stored in the servlet context) which contains the the count and classname from the initial
     * search request for displaying on the the tooLarge jsp.
     */
    public static final String RESULT_INFO = "resultInfo";
    /**
     * Maximum size for the initial request of objects.
     */
    public static final int MAXIMUM_RESULT_SIZE = 50;

    /**
     * Contains a list of the shortlabels of the items found in a search result. Used by JSPs to decide what shortlabels
     * should be highlighted.
     */
    public static final String HIGHLIGHT_LABELS_LIST = "labelList";

    /**
     * Maximum display sizes allowable for each relevant type. makes sense to keep these in code as a simple config
     * change will not be able to affect these values.
     */
    public static final int MAXIMUM_DISPLAYABLE_INTERACTION = 50;

    //TODO clean this up
//    public static final int MAXIMUM_DISPLAYABLE_PROTEIN = 30;

//    public static final int MAXIMUM_DISPLAYABLE_CVOBJECTS = 30;
//    public static final int MAXIMUM_DISPLAYABLE_EXPERIMENTS = 30;

//    public static final String FORWARD_PROTEIN = "singleProtein";

    /**
     * this value is needed for the result wrapper.
     */
    public static final int MAXIMUM_RESULT_OBJECT = 50;
}