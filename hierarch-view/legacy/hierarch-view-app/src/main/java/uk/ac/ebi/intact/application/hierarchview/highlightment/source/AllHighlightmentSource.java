/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.hierarchview.highlightment.source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.service.graph.Node;
import uk.ac.ebi.intact.service.graph.binary.InteractorVertex;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Interface allowing to wrap an highlightment source.
 *
 * @author Alexandre Liban (aliban@ebi.ac.uk)
 * @version $Id$
 * @since 24 juni 2005
 */
public class AllHighlightmentSource extends HighlightmentSource {

    static final Log logger = LogFactory.getLog( AllHighlightmentSource.class );

    /**
     * separator of keys, use to create and parse key string.
     */
    private static final String KEY_SEPARATOR = ",";
    private static final String ATTRIBUTE_OPTION_CHILDREN = "CHILDREN";
    private static final String PROMPT_OPTION_CHILDREN = "With children of the selected Interpro term";

    /**
     * The key for this source
     */
    public static final String SOURCE_KEY = "All";

    /**
     * Return the html code for specific options of the source to integrate in the
     * highlighting form. if the method return null, the source hasn't options.
     *
     * @return the html code for specific options of the source.
     */
    public String getHtmlCodeOption( HttpSession aSession ) {
        String htmlCode;
        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( Constants.USER_KEY );
        String check = ( String ) user.getHighlightOption( ATTRIBUTE_OPTION_CHILDREN );

        if ( check == null ) {
            check = "";
        }

        htmlCode = "<input type=\"checkbox\" name=\""
                   + ATTRIBUTE_OPTION_CHILDREN + "\" " + check
                   + " value=\"checked\">" + PROMPT_OPTION_CHILDREN;

        return htmlCode;
    }

    /**
     * Return a collection of keys specific to the selected protein and the
     * current source. e.g. If the source is GO, we will send the collection of
     * GO term owned by the given protein. Those informations are retreived from
     * the Intact database
     *
     * @param node a node of the network
     * @return a set of keys (this keys are a String) (this Keys are a String[]
     *         which contains the GOterm and a description)
     */
    public Collection<String[]> getKeysFromIntAct( Node node, HttpSession aSession ) {

        Collection<String[]> listInterproTerm;
        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession()
                .getAttribute( uk.ac.ebi.intact.application.hierarchview.business.Constants.USER_KEY );

        if ( null == user ) {
            logger.error( "No user found in the session, unable to search for all allowed source terms" );
            return null;
        }

        // getXref collection
        Collection<CrossReference> xRef = ( ( InteractorVertex ) node ).getProperties();

        if ( logger.isDebugEnabled() ) logger.debug( xRef.size() + " Xref found" );

        listInterproTerm = filterInteractorXref( xRef );

        return listInterproTerm;
    }

    /**
     * get a collection of XRef and filter to keep All terms
     *
     * @param xRefs is collection of CrossReference
     * @return a All term collection or an empty collection if none exists.
     */
    private Collection<String[]> filterInteractorXref( Collection<CrossReference> xRefs ) {
        Collection<String[]> listAllSourceTerm = new ArrayList( xRefs.size() ); // size will be >= to needed capacity

        for ( CrossReference xref : xRefs ) {
            String[] allSourceTerm = new String[3];

            // get all the source terms allowed
            Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;
            String sourceList = properties.getProperty( "highlightment.source.allowed" );
            String[] listSource = sourceList.split( "," );

            for ( int i = 1; i < listSource.length; i++ ) {
                if ( ( xref.getDatabase() ).toLowerCase().equals( listSource[i].toLowerCase() ) ) {

                    allSourceTerm[0] = listSource[i];
                    allSourceTerm[1] = xref.getIdentifier();
                    allSourceTerm[2] = xref.getText();
                    listAllSourceTerm.add( allSourceTerm );
                    if ( logger.isDebugEnabled() )
                        logger.debug( "Type: " + listSource[i] + " |Xref: " + xref.getIdentifier() );
                }
            }
        }
        return listAllSourceTerm;
    }

    /**
     * Create a set of protein we must highlight in the graph given in
     * parameter. The protein selection is done according to the source keys
     * stored in the IntactUser. Keys are GO terms, so we select (and highlight)
     * every protein which awned that GO term. If the children option is
     * activated, all proteins which owned a children of the selected GO term
     * are selected.
     *
     * @param aSession the session where to find selected keys.
     * @param aGraph   the graph we want to highlight
     * @return a not null collection of nodes
     */
    public Collection<Node> proteinToHightlight( HttpSession aSession, Network aGraph ) {

        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( Constants.USER_KEY );
        Collection<String> children = user.getKeys();
        String selectedSourceTerm = user.getSelectedKey();
        String selectedSourceTermType = user.getSelectedKeyType();

        if ( logger.isDebugEnabled() )
            logger.debug( "getKeys=" + children + " |selectedSourceTerm=" + selectedSourceTerm + " | selectedSourceTermType=" + selectedSourceTermType );

        if ( children.remove( selectedSourceTerm ) ) {
            if ( logger.isDebugEnabled() ) logger.debug( selectedSourceTerm + " removed from children collection" );
        }

        // get source option
        String check = ( String ) user.getHighlightOption( ATTRIBUTE_OPTION_CHILDREN );
        boolean searchForChildren;
        if ( check != null ) {
            searchForChildren = check.equals( "checked" );
        } else {
            searchForChildren = false;
        }
        if ( logger.isDebugEnabled() ) logger.debug( "Children option activated ? " + searchForChildren );

        logger.debug( "return to: proteinToHighlightInteractor" );
        return proteinToHighlightInteractor( aGraph, selectedSourceTerm,
                                             children, searchForChildren );

    }

    /**
     * Returns a collection of nodes to highlight for the display
     *
     * @param aGraph             the network to display
     * @param selectedSourceTerm the selected source Term
     * @param children           the children source Terms of the selected source Term
     * @param searchForChildren  whether it shall be searched for the children
     * @return a not null collection of nodes
     */
    private Collection<Node> proteinToHighlightInteractor( Network aGraph,
                                                           String selectedSourceTerm,
                                                           Collection<String> children,
                                                           boolean searchForChildren ) {

        // if the source highlight map of the network is empty
        // it is filled with the source informations from each
        // node of the graph
        if ( aGraph.isSourceHighlightMapEmpty() ) {
            aGraph.initSourceHighlightMap();
            //user.getHVNetworkBuilder().initSourceHighlightMap( aGraph );
        }
        // return the set of proteins to highlight based on the source highlighting map of the graph
        return proteinToHighlightSourceMap( aGraph, children, selectedSourceTerm, searchForChildren );
    }

    /**
     * Returns a collection of proteins to be highlighted in the graph.
     * <p/>
     * Method is called when the graph was built by the mine database table.
     *
     * @param aGraph             the graph
     * @param children           the children of the selected source Term
     * @param selectedSourceTerm the selected source Term
     * @param searchForChildren  whether it shall be searched for the children
     * @return a not null collection of nodes
     */
    private Collection proteinToHighlightSourceMap( Network aGraph,
                                                    Collection<String> children,
                                                    String selectedSourceTerm,
                                                    boolean searchForChildren ) {

        Collection<Node> nodeList = new ArrayList( 20 ); // should be enough for 90% cases

        // retrieve the set of proteins to highlight for the source key (e.g. GO)
        // and the source Term
        if ( logger.isDebugEnabled() )
            logger.debug( "Source = " + SOURCE_KEY + " | selectedSourceTerm = " + selectedSourceTerm );

        Set<Node> proteinsToHighlight = aGraph.getProteinsForHighlight( SOURCE_KEY, selectedSourceTerm );

        if ( logger.isInfoEnabled() ) logger.info( "proteinsToHighlight = " + proteinsToHighlight );

        // if we found any proteins we add all of them to the collection
        if ( proteinsToHighlight != null ) {
            nodeList.addAll( proteinsToHighlight );
        }

        /*
        * for every children all proteins related to the current source term are
        * fetched from the source highlighting map of the graph and added to
        * the collection
        */

        if ( searchForChildren ) {
            for ( String aChildren : children ) {
                proteinsToHighlight = aGraph.getProteinsForHighlight( SOURCE_KEY, aChildren );
                if ( proteinsToHighlight != null ) {
                    nodeList.addAll( proteinsToHighlight );
                }
            }
        }
        return nodeList;
    }

    /**
     * Allows to update the session object with parameters' request. These
     * parameters are specific of the implementation.
     *
     * @param aRequest request in which we have to get parameters to save in the
     *                 session
     * @param aSession session in which we have to save the parameter
     */
    public void saveOptions( HttpServletRequest aRequest, HttpSession aSession ) {
        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( Constants.USER_KEY );
        String[] result = aRequest.getParameterValues( ATTRIBUTE_OPTION_CHILDREN );

        if ( result != null )
            user.addHighlightOption( ATTRIBUTE_OPTION_CHILDREN, result[0] );
    } // saveOptions

    /**
     * Return a collection of URL corresponding to the selected protein and source
     * eg. produce a list of GO terms if GO is the source.<br>
     * if the method send back no URL, the given parameter is wrong.
     *
     * @param xRefs           The collection of XRef from which we want to get the list of corresponding URL
     * @param selectedXRefs   The collection of selected XRef
     * @param applicationPath our application path
     * @param user            the current user
     * @return a set of URL pointing on the highlightment source.
     */
    public List getSourceUrls( Collection<CrossReference> xRefs,
                               Collection<String> selectedXRefs,
                               String applicationPath,
                               IntactUserI user ) {

        // filter to keep only the allowed source terms
        logger.debug( xRefs.size() + " Xref before filtering" );
        Collection listAllSourceTerm = null;

        listAllSourceTerm = filterInteractorXref( xRefs );
        logger.debug( listAllSourceTerm.size() + " source term(s) after filtering" );

        // create url collection with exact size
        List urls = new ArrayList( listAllSourceTerm.size() );

        // get the list of all the source terms allowed
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;
        String sourceList = properties.getProperty( "highlightment.source.allowed" );

        // get the delimiter token
        String delimiter = IntactUserI.HIGHLIGHTING_PROPERTIES.getProperty( "highlightment.source.token" );

        // split the list with the delimiter
        String[] listSource = sourceList.split( delimiter );

        // sent the source terms to their implementation classes
        List tmp;
        logger.debug( "sending source terms to their implementation classes..." );

        for ( int i = 1; i < listSource.length; i++ ) {
            HighlightmentSource source = HighlightmentSource.getHighlightmentSource(
                    properties.getProperty( "highlightment.source." + listSource[i] + ".class" ) );
            tmp = source.getSourceUrls( xRefs, selectedXRefs, applicationPath, user );
            urls.addAll( tmp );
            tmp = null;
        }

        logger.debug( "all source terms have been sent successfully" );

        // sort the source list by count
        Collections.sort( urls );

        return urls;
    }

    /**
     * Parse the set of key generate by the source and give back a collection of
     * keys.
     *
     * @param someKeys a string which contains some key separates by a character.
     * @return the splitted version of the key string as a collection of String.
     */
    public Collection<String> parseKeys( String someKeys ) {
        Collection<String> keys = new Vector();

        if ( ( null == someKeys ) || ( someKeys.length() < 1 ) ) {
            return null;
        }

        StringTokenizer st = new StringTokenizer( someKeys, KEY_SEPARATOR );

        while ( st.hasMoreTokens() ) {
            String key = st.nextToken();
            keys.add( key );
        }

        return keys;
    }
}
