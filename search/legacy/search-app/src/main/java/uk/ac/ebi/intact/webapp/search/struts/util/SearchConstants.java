/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.webapp.search.struts.util;

/**
 * Contains constants required for the Search webapp.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id:SearchConstants.java 6452 2006-10-16 17:09:42 +0100 (Mon, 16 Oct 2006) baranda $
 */
public interface SearchConstants {

    ////////////////////////////////////////////////////
    // Configuration keys (web.xml, properties files)

    /**
     * The key to access configuration directory.
     */
    String CONFIG_DIR = "configdir";

    /**
     * Used as a key to identify a datasource class - its value is deifned in the web.xml file as a
     * servlet context parameter
     */
    String DATA_SOURCE = "datasource";

    //////////////////////////
    // Session/Request keys

    /**
     * URL to which the waiting page should redirect.
     */
    public static final String WAITING_URL = "waiting_url";

    /**
     * Message to display on the waiting page.
     */
    public static final String WAITING_MSG = "waiting_message";

    /**
     * This value indicates how long should the waiting page wait before to fire redirect.
     */
    public static final Integer DEFAULT_WAITING_TIME = 1;

    /**
     * This value indicates how long should the waiting page wait before to fire redirect.
     */
    public static final String WAITING_TIME = "waiting_time";

    /**
     * When using the waiting page, if that key is bound in the session and the values if Boolean.TRUE,
     * then there is no forward.
     */
    public static final String DO_NOT_FORWARD = "waiting_page_do_not_forward";

    /**
     * The key to store an Intact Service object.
     */
    public static final String INTACT_SERVICE = "IntactService";

    /**
     * The key to access a user session object.
     */
    String INTACT_USER = "user";

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
     * Holds a particular viewBean for a large Experiment. Used in requests for tabbed page beans of
     * the wrapped Experiment
     */
    public static final String LARGE_EXPERIMENT_BEAN = "largeExp";

    /**
     * The most recent user-defined search (ie not from a CvObject link).
     */
    public static final String LAST_VALID_SEARCH = "lastValidSearch";

    /**
     * The label used to identify the search results Collection (often saved in a request)
     */
    public static final String SEARCH_RESULTS = "searchResults";

    /**
     * The label used to identify the search results Map (often saved in a request)
     */
    public static final String SEARCH_RESULTS_MAP = "searchResultsMap";

    /**
     * class to be searched for, needed for the advanced search
     */
    public static final String SEARCH_CLASS = "search_class";

    /**
     * Indetifies the map which contains the the count and classname
     * from the initial search request for displaying on the the tooLarge jsp
     */
    public static final String RESULT_INFO = "uk.ac.ebi.intact.search.internal.RESULTS_INFO";

    /**
     * Maximum size for the initial request of objects
     */
    public static final int MAXIMUM_RESULT_SIZE = 60;

    /////////////////////
    // Misc constants
    /////////////////////

    /**
     * Forward to the too large page if the dataset is too large
     */
    public static final String TOO_LARGE_DATASET = "tooLarge";

    /**
     * Contains a list of the shortlabels of the items found in a search result. Used by JSPs to
     * decide what shortlabels should be highlighted.
     */
    public static final String HIGHLIGHT_LABELS_LIST = "labelList";

    ///////////////////////
    // STRUTS forwards
    ///////////////////////

    /**
     * Used to identify which page a request came from. Mainly needed by Action classes to decide
     * what to do for a given request when the same request may originate from different sources and
     * then require different processing (eg for Protein it should give a partners view if from the
     * main 'simple' page, or the Protein beans if from another link).
     */
    public static final String PAGE_SOURCE = "view";

    public static final String ERROR_MESSAGE = "error_message";

    public static final String IMAGE_BEAN = "imageBean";

    public static final String SEARCH_OBJECT = "searchObject";

    /**
     * The key to success action.
     */
    public static final String FORWARD_SUCCESS = "success";

    /**
     * Used in various action classes to define where to forward to on different conditions.  See
     * the struts-config.xml file to see where the page that is using this forwards to.
     */
    public static final String FORWARD_FAILURE = "failure";

    /**
     * Key to the too large action
     */
    public static final String FORWARD_TOO_LARGE = "tooLarge";

    public static final String FORWARD_NO_INTERACTOR_FOUND = "noInteractor";

    /**
     * Key to the dispatcher action.
     */
    public static final String FORWARD_DISPATCHER_ACTION = "dispatcher";

    /**
     * Key to the advanced dispatcher action.
     */
    public static final String FORWARD_ADV_DISPATCHER_ACTION = "advDispatcher";

    /**
     * Forward to the Action responsible for setting up a view when using the search box
     */
    public static final String FORWARD_DETAILS_ACTION = "details";

    /**
     * Forward to the Action responsible for setting up a view when using the search box
     */
    public static final String FORWARD_BINARY_ACTION = "binary";

    /**
     * Forward to the Partner view
     */
    public static final String FORWARD_PARTNER_VIEW = "partner";

    /**
     * Forward to the Action responsible for setting up the 'simple' initial view of simple text
     * search results
     */
    public static final String FORWARD_SIMPLE_ACTION = "simple";

    /**
     * Forward to the Action responsible for setting up the 'simple' initial view of simple text
     * search results
     */
    public static final String FORWARD_SIMPLE_RESULTS = "simpleResults";

    /**
     * Forward to the Action responsible for setting up a view when clicking on a link
     */
    public static final String FORWARD_SINGLE_ACTION = "single";

    /**
     * Forward to the Action responsible for setting up a view when clicking on a link
     */
    public static final String FORWARD_RESULTS = "results";

    public static final String FORWARD_DETAIL_PAGE = "detailPage";

    /**
     * Used as a key to identify a page to display when no matches are found from a search.
     */
    public static final String FORWARD_NO_MATCHES = "noMatch";

    /**
     * Used as a key to identify the binary protein action
     */
    public static final String FORWARD_BINARYINTERACTOR_ACTION = "binaryInteractor";


    /**
     * If we got more than 2 proteins forward to errorpage.
     */
    public static final String FORWARD_TOO_MANY_INTERACTORS = "tooManyInteractors";

    /**
     * forward when no interaction found in BinaryProteinAction.
     */
    public static final String FORWARD_NO_INTERACTIONS = "noInteractions";

    /**
     * forward to a graph representation of IntAct CVs.
     */
    public static final String FORWARD_SHOW_CV_DAG = "showCvDag";

    /**
     * forward to a waiting page.
     */
    public static final String FORWARD_WAITING_PAGE = "waiting_page";


    /**
     * Used to forward to a syntax error page, if the search string has an incorrect syntax
     */
    public static final String FORWARD_ERROR = "error";
    public static final String FORWARD_BIOSOURCE = "bioSource";
    public static final String FORWARD_CVOBJECT = "cvObject";
    public static final String FORWARD_INTERACTOR = "singleInteractor";
    public static final String FORWARD_NO_RESOURCE = "noResource";
    public static final String FORWARD_INDEX_ERROR = "indexError";

    /**
     * Used as a key to identify a page to display when matches are found from a search.
     */
    String FORWARD_MATCHES = "match";

    //Maximum display sizes allowable for each relevant type. makes sense to
    //keep these in code as a simple config change will not be able to affect these values
    int MAXIMUM_DISPLAYABLE_INTERACTION = 50;
    int MAXIMUM_DISPLAYABLE_PROTEIN = 30;
    int MAXIMUM_DISPLAYABLE_CVOBJECTS = 30;
    int MAXIMUM_DISPLAYABLE_EXPERIMENTS = 30;

    public static final int RESULTS_PER_PAGE = 30;
    public static final String DATA_PAGE_INDEX_ATT_NAME = "uk.ac.ebi.intact.PAGE_INDEX";
    public static final String TOTAL_RESULTS_ATT_NAME = "uk.ac.ebi.intact.search.internal.TOTAL_RESULTS";
}