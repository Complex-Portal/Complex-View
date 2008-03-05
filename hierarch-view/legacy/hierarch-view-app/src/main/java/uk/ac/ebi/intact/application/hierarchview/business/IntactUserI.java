/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.business;

import uk.ac.ebi.intact.application.hierarchview.business.data.DataService;
import uk.ac.ebi.intact.application.hierarchview.business.graph.HVNetworkBuilder;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.business.image.ImageBean;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.view.ClickBehaviourForm;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.ConfidenceFilter;
import uk.ac.ebi.intact.util.PropertyLoader;

import javax.servlet.http.HttpSessionBindingListener;
import java.awt.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Properties;

/**
 * This interface stores information about an Intact Web user session. Instead of
 * binding multiple objects, only an object of this class is bound to a session,
 * thus serving a single access point for multiple information.
 * <p/>
 * This class implements the <tt>HttpSessionBindingListener</tt> interface for it
 * can be notified of session time outs.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 *          <p/>
 *          //uk.ac.ebi.intact.searchengine.business.IntactUserI
 */
public interface IntactUserI extends Serializable, HttpSessionBindingListener, uk.ac.ebi.intact.searchengine.business.IntactUserI {

    // All the properties needed by the user (Session scope !)
    public static final Properties GRAPH_PROPERTIES = PropertyLoader.load( StrutsConstants.GRAPH_PROPERTY_FILE );
    public static final Properties SEARCH_PROPERTIES = PropertyLoader.load( StrutsConstants.SEARCH_PROPERTY_FILE );
    public static final Properties WEB_SERVICE_PROPERTIES = PropertyLoader.load( StrutsConstants.WEB_SERVICE_PROPERTY_FILE );
    public static final Properties HIGHLIGHTING_PROPERTIES = PropertyLoader.load( StrutsConstants.HIGHLIGHTING_PROPERTY_FILE );
    public static final Properties GRAPH2MIF_PROPERTIES = PropertyLoader.load( StrutsConstants.GRAPH2MIF_PROPERTY_FILE );    


   public ConfidenceFilter getConfidenceFilterValues();
   public void setConfidenceFilterValues(ConfidenceFilter confidenceFilter);

    public String getQueryString();

    public String getErrorMessage();

    public boolean hasErrorMessage();

    public String getApplicationPath();

    public String getMethodLabel();

    public String getMethodClass();

    public String getBehaviour();

    public ImageBean getImageBean();

    public Collection<String> getSelectedKeys();

    public String getClickedKey();

    public String getSelectedKeyType();

    public void setClickBehaviour( ClickBehaviourForm form );

    public boolean clickBehaviourIsAdd();

    public boolean clickBehaviourIsCenter();

    public Network getInteractionNetwork();

    public boolean InteractionNetworkReadyToBeDisplayed();

    public boolean InteractionNetworkReadyToBeHighlighted();

    public String getSourceURL();

    public boolean hasSourceUrlToDisplay();

    public String getSearchUrl( String query, boolean addFullContext );

    public String getSearchUrl();

    public void setErrorMessage( String errorMessage );

    public void clearErrorMessage();

    public void setQueryString( String aQueryString );

    public void setMethodLabel( String methodLabel );

    public void setMethodClass( String methodClass );

    public void setBehaviour( String behaviour );

    public void setImageBean( ImageBean imageBean );

    public void setNodeCoordinates( String nodeCoordinates );

    public void setSelectedKeys( Collection keys );

    public void setClickedKey( String key );

    public void setSelectedKeyType( String keyType );

    public void setInteractionNetwork( Network in );

    public void setSourceURL( String url );

    public void resetSourceURL();

    public HVNetworkBuilder getHVNetworkBuilder();

    public DataService getDataService();

    public int getNetworkUpdateOption();

    public void setNetworkUpdateOption( int option );

    public void pushNetwork( Network network );

    public Network popNetwork();

    /**
     * Set the default value of user's data
     */
    public void init();

    public void setMinePath( String path );

    public String getMinePath();

    /**
     * Clear all highlight options.
     */
    public void resetHighlightOptions();

    /**
     * Add a new option in the option set.<br>
     * That new option is referenced by its <i>name</i>
     *
     * @param name  name of the option
     * @param value value taken by the option
     */
    public void addHighlightOption( String name, Object value );

    /**
     * Get the value associated to the option name.
     *
     * @param name the name of the option we want the value
     * @return the value associated to the name
     */
    public Object getHighlightOption( String name );

    void setExportUrl( String url25 );
    public String getExportUrl( );

    void setWindowDimension( int width, int height );

    public Dimension getWindowDimension();
}


