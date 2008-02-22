/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */

package uk.ac.ebi.intact.application.hierarchview.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.hierarchview.business.data.DataService;
import uk.ac.ebi.intact.application.hierarchview.business.data.DataServiceFactory;
import uk.ac.ebi.intact.application.hierarchview.business.data.DatabaseService;
import uk.ac.ebi.intact.application.hierarchview.business.graph.HVNetworkBuilder;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.business.image.ImageBean;
import uk.ac.ebi.intact.application.hierarchview.exception.HierarchViewDataException;
import uk.ac.ebi.intact.application.hierarchview.struts.view.ClickBehaviourForm;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.IntactObject;
import uk.ac.ebi.intact.service.graph.Node;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import java.awt.*;
import java.util.*;

/**
 * This class stores information about an Intact Web user session. <br>
 * Instead of binding multiple objects, only an object of this class is bound to
 * a session, thus serving a single access point for multiple information.
 * <p/>
 * This class implements the <tt>HttpSessionBindingListener</tt> interface for
 * it can be notified of session time outs.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class IntactUser implements IntactUserI {

    private static final Log logger = LogFactory.getLog( IntactUser.class );

    private String minePath;

    /**
     * The current network with which the user is working
     */
    private Network network;

    /**
     * The current click behaviour
     */
    private ClickBehaviourForm clickBehaviour;

    /**
     * Set of highlight options (key/value) given by the user via the web
     * interface
     */
    private Map highlightOptions;

    /**
     * data relatives to the image producing (Image byte code, SVG DOM, HTML
     * MAP)
     */
    private ImageBean imageBean;

    /**
     * the Javascript Array containing all the nodes real coordinates
     */
    private String nodeCoordinates;

    /**
     * Collection of keys received from the current source. The highlightment of
     * the current interaction network is done according to these keys. <b>keys
     * </b> contains a set of keys to take into account in the highlight process
     * <b>selectedKey </b> is the one which has been selected by the user
     */
    private Collection keys;
    private String selectedKey;
    private String selectedKeyType;

    /**
     * URL describes the link to the highlight source in the case the user has
     * selected one of those available for the current protein. The URL is
     * encoded in the UTF-8 format.
     */
    private String sourceURL;

    // User's form fields
    private String queryString;

    private String errorMessage;
    private int networkUpdateOption;

    private String methodLabel;
    private String methodClass;
    private String behaviour;
    private String applicationPath;


    private DataService dataservice;
    private HVNetworkBuilder networkBuilder;

    private Stack<Network> userQueryHistory;
    private String exportUrl;
    private Dimension windowDimension = null;

    /**
     * Constructs an instance of this class with given mapping file and the name
     * of the data source class.
     *
     * @param applicationPath the current application path
     * @throws IntactException thrown for any error in creating lists such as
     *                         topics, database names etc.
     */
    public IntactUser( String applicationPath ) throws IntactException {

        init();

        this.applicationPath = applicationPath;
    }

    public static IntactUser getCurrentInstance(HttpSession session) {
        return (IntactUser) session.getAttribute(Constants.USER_KEY);
    }

    public static boolean currentInstanceExists(HttpSession session) {
        return (null != session.getAttribute(Constants.USER_KEY));
    }

    public String getQueryString() {
        return queryString;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasErrorMessage() {
        return errorMessage != null;
    }

    public String getApplicationPath() {
        return applicationPath;
    }

    public String getMethodLabel() {
        return methodLabel;
    }

    public String getMethodClass() {
        return methodClass;
    }

    public String getBehaviour() {
        return behaviour;
    }

    public Network popNetwork() {
        return userQueryHistory.pop();
    }

    public void pushNetwork( Network network ) {
        userQueryHistory.push( network );
    }

    /**
     * Allows the user to know if an interaction network will be displayed
     *
     * @return
     */
    public boolean InteractionNetworkReadyToBeDisplayed() {
        return ( ( null != queryString ) && ( null != imageBean ) );
    }

    /**
     * Allows the user to know if an interaction network is ready to be
     * highlighted. i.e. all data needed to highlight the current interaction
     * network are available.
     *
     * @return boolean true if the interaction network can be highlighted, esle
     *         false.
     */
    public boolean InteractionNetworkReadyToBeHighlighted() {
        return ( null != queryString ) && ( null != keys )
               && ( behaviour != null ) && ( null != network );
    }

    public void setClickBehaviour( ClickBehaviourForm form ) {
        clickBehaviour = form;
    }

    /**
     * is the current behaviour is to Add when the user click on the image map
     *
     * @return true if Add the view is the current behaviour
     */
    public boolean clickBehaviourIsAdd() {
        return clickBehaviour != null && clickBehaviour.addSelected();
    }

    /**
     * is the current behaviour is to Center the view when the user click on the
     * image map
     *
     * @return true if Center the view is the current behaviour
     */
    public boolean clickBehaviourIsCenter() {
        return clickBehaviour == null || clickBehaviour.centerSelected();
    }

    public Network getInteractionNetwork() {
        return network;
    }

    public ImageBean getImageBean() {
        return imageBean;
    }

    public String getNodeCoordinates() {
        return nodeCoordinates;
    }

    public Collection<String> getSelectedKeys() {
        return keys;
    }

    public String getClickedKey() {
        return selectedKey;
    }

    public String getSelectedKeyType() {
        return selectedKeyType;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public boolean hasSourceUrlToDisplay() {
        return ( sourceURL != null );
    }

    public void setQueryString( String queryString ) {
        this.queryString = queryString;
    }

    public void setMethodLabel( String methodLabel ) {
        this.methodLabel = methodLabel;
    }

    public void setMethodClass( String methodClass ) {
        this.methodClass = methodClass;
    }

    public void setBehaviour( String behaviour ) {
        this.behaviour = behaviour;
    }

    public void setInteractionNetwork( Network in ) {
        this.network = in;
    }

    public void setImageBean( ImageBean imageBean ) {
        this.imageBean = imageBean;
    }

    public void setNodeCoordinates( String nodeCoordinates ) {
        this.nodeCoordinates = nodeCoordinates;
    }

    public void setSelectedKeys( Collection keys ) {
        this.keys = keys;
    }

    public void setClickedKey( String key ) {
        selectedKey = key;
    }

    public void setSelectedKeyType( String keyType ) {
        selectedKeyType = keyType;
    }

    public void setSourceURL( String aSourceURL ) {
        sourceURL = aSourceURL;
    }

    public void resetSourceURL() {
        sourceURL = null;
    }

    public HVNetworkBuilder getHVNetworkBuilder() {
        return networkBuilder;
    }


    /**
     * Set the default value of user's data
     */
    public void init() {
        this.queryString = null;

        methodLabel = null;
        methodClass = null;
        behaviour = null;

        network = null;
        imageBean = null;
        keys = null;
        selectedKey = null;
        highlightOptions = new HashMap();
        sourceURL = null;
        logger.info( "User's data set to default" );
        
        dataservice = DataServiceFactory.buildDataService( SEARCH_PROPERTIES.getProperty( "search.source.name" ) );
        if ( dataservice != null ) {
            networkBuilder = new HVNetworkBuilder( this );
            userQueryHistory = new Stack<Network>();
        }
    }

    public void resetHighlightOptions() {
        highlightOptions.clear();
    }

    public void addHighlightOption( String name, Object value ) {
        highlightOptions.put( name, value );
    }

    public Object getHighlightOption( String name ) {
        return highlightOptions.get( name );
    }

    public void setExportUrl( String url25 ) {
        this.exportUrl = url25;
    }

    public String getExportUrl() {
        return exportUrl;
    }

    public void setWindowDimension( int width, int height ) {
        windowDimension = new Dimension(width, height);
    }

    public Dimension getWindowDimension() {
        return windowDimension;
    }

    public void setErrorMessage( String errorMessage ) {
        this.errorMessage = errorMessage;
    }

    public void clearErrorMessage() {
        this.errorMessage = null;
    }

    public String getSearchUrl( String query, boolean addFullContext ) {
        String searchURL = null;

        // read the Search.properties file
        Properties properties = SEARCH_PROPERTIES;

        if ( null != properties ) {
            String url = properties.getProperty( "search.url" );
            String queryParameter = properties.getProperty( "search.parameter.query.name" );
            if ( addFullContext && ( network != null ) ) {
                StringBuffer buffer = new StringBuffer( 64 );

                Collection<Node> interactors = network.getNodes();

                for ( Node interactor : interactors ) {
                    String interactorAc = interactor.getId();
                    buffer.append( ',' ).append( interactorAc );
                }

                // forward to search giving the spec of the current interaction
                // network plus the current query.
                searchURL = url + "?" + queryParameter + "=" + query + buffer.toString() + "&filter=ac";
                // classValue
            } else {
                // forward to search giving the spec of the current interaction network plus the current query.
                searchURL = url + "?" + queryParameter + "=" + query + "&filter=ac"; //  + "&" + // classParameter
                // + "=" +
                // classValue
            }
        }

        logger.info( "search URL = " + searchURL );
        return searchURL;
    }

    public String getSearchUrl() {
        return getSearchUrl( queryString, true );
    }

    // Implements HttpSessionBindingListener

    /**
     * Will call this method when an object is bound to a session. Not doing
     * anything.
     */
    public void valueBound( HttpSessionBindingEvent event ) {
    }

    /**
     * Will call this method when an object is unbound from a session.
     */
    public void valueUnbound( HttpSessionBindingEvent event ) {
        // nothing here
    }

    // Implementation of IntactUserI interface
    public <T extends IntactObject> Collection<T> search( Class<T> objectType,
                                                          String searchParam,
                                                          String searchValue ) throws IntactException {
        return DatabaseService.getColByPropertyName( objectType, searchParam, searchValue );
    }

    public String getUserName() {
        return "default";
    }

    public String getDatabaseName() {
        try {
            return getDataService().getDbName();
        }
        catch ( HierarchViewDataException e ) {
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see uk.ac.ebi.intact.application.hierarchview.business.IntactUserI#setMinePath(java.lang.String)
     */
    public void setMinePath( String path ) {
        minePath = path;
    }

    /* (non-Javadoc)
     * @see uk.ac.ebi.intact.application.hierarchview.business.IntactUserI#getMinePath()
     */
    public String getMinePath() {
        return minePath;
    }

    public DataService getDataService() {
        return this.dataservice;
    }

    public int getNetworkUpdateOption() {
        return networkUpdateOption;
    }

    public void setNetworkUpdateOption( int option ) {
        networkUpdateOption = option;
    }
}