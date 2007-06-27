/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.business;

import uk.ac.ebi.intact.application.hierarchview.business.graph.InteractionNetwork;
import uk.ac.ebi.intact.application.hierarchview.business.image.ImageBean;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.view.ClickBehaviourForm;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.util.PropertyLoader;

import javax.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;
import java.util.Collection;
import java.util.Properties;

/**
 * This interface stores information about an Intact Web user session. Instead of
 * binding multiple objects, only an object of this class is bound to a session,
 * thus serving a single access point for multiple information.
 * <p>
 * This class implements the <tt>HttpSessionBindingListener</tt> interface for it
 * can be notified of session time outs.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public interface IntactUserI
        extends Serializable,
                HttpSessionBindingListener,
        uk.ac.ebi.intact.searchengine.business.IntactUserI
{

    // All the properties needed by the user (Session scope !)
    public static final Properties GRAPH_PROPERTIES        = PropertyLoader.load (StrutsConstants.GRAPH_PROPERTY_FILE);
    public static final Properties SEARCH_PROPERTIES       = PropertyLoader.load (StrutsConstants.SEARCH_PROPERTY_FILE);
    public static final Properties WEB_SERVICE_PROPERTIES  = PropertyLoader.load (StrutsConstants.WEB_SERVICE_PROPERTY_FILE);
    public static final Properties HIGHLIGHTING_PROPERTIES = PropertyLoader.load (StrutsConstants.HIGHLIGHTING_PROPERTY_FILE);
    public static final Properties GRAPH2MIF_PROPERTIES    = PropertyLoader.load (StrutsConstants.GRAPH2MIF_PROPERTY_FILE);


    public String  getQueryString();
    public String  getApplicationPath();
    public int     getCurrentDepth();
    public boolean minimalDepthReached();
    public boolean maximalDepthReached();

    public String  getMethodLabel();
    public String  getMethodClass();
    public String  getBehaviour();
    public ImageBean getImageBean();
    public String getNodeCoordinates();
    public Collection getKeys();
    public String getSelectedKey();
    public String getSelectedKeyType();

    public void setClickBehaviour (ClickBehaviourForm form);
    public boolean clickBehaviourIsAdd ();
    public boolean clickBehaviourIsCenter ();

    public InteractionNetwork getInteractionNetwork();
    public boolean InteractionNetworkReadyToBeDisplayed();
    public boolean InteractionNetworkReadyToBeHighlighted();
    public String getSourceURL ();
    public boolean hasSourceUrlToDisplay();
    public String getSearchUrl (String query, boolean addFullContext);
    public String getSearchUrl ();

    public void setQueryString (String aQueryString);
    public void increaseDepth();
    public void desacreaseDepth();
    public void setDepthToDefault();
    public void setMethodLabel (String methodLabel);
    public void setMethodClass (String methodClass);
    public void setBehaviour (String behaviour);
    public void setImageBean (ImageBean imageBean);
    public void setNodeCoordinates (String nodeCoordinates);
    public void setKeys (Collection keys);
    public void setSelectedKey (String key);
    public void setSelectedKeyType (String keyType);

    public void setInteractionNetwork (InteractionNetwork in);

    public void setSourceURL(String url);
    public void resetSourceURL();


    /**
     * Set the default value of user's data
     */
    public void init ();
    
    public void setMinePath(String path);
    public String getMinePath();


    /**
     * Returns a subgraph centered on startNode.
     * The subgraph will contain all nodes which are up to graphDepth interactions away from startNode.
     * Only Interactions which belong to one of the Experiments in experiments will be taken into account.
     * If experiments is empty, all Interactions are taken into account.
     *
     * Graph depth:
     * This parameter limits the size of the returned interaction graph. All baits are shown with all
     * the interacting preys, even if they would normally be on the "rim" of the graph.
     * Therefore the actual diameter of the graph may be 2*(graphDepth+1).
     *
     * Expansion:
     * If an Interaction has more than two interactors, it has to be defined how pairwise interactions
     * are generated from the complex data. The possible values are defined in the beginning of this file.
     *
     * @param in - the interaction network.
     * @param graphDepth - depth of the graph
     * @param experiments - Experiments which should be taken into account
     * @param complexExpansion - Mode of expansion of complexes into pairwise interactions
     *
     * @return a InteractionNetwork object.
     *
     * @exception IntactException - thrown if problems are encountered
     */
    public InteractionNetwork subGraph (InteractionNetwork in,
                                        int graphDepth,
                                        Collection experiments,
                                        int complexExpansion) throws IntactException ;
    
    /**
     * Clear all highlight options.
     */
    public void resetHighlightOptions () ;

    /**
     * Add a new option in the option set.<br>
     * That new option is referenced by its <i>name</i>
     * @param name name of the option
     * @param value value taken by the option
     */
    public void addHighlightOption (String name, Object value);

    /**
     * Get the value associated to the option name.
     * @param name the name of the option we want the value
     * @return the value associated to the name
     */
    public Object getHighlightOption (String name);

    public void setCurrentDepth(int i);
}
