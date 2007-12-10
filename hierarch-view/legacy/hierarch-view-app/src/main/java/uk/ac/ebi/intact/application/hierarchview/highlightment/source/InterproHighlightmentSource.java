package uk.ac.ebi.intact.application.hierarchview.highlightment.source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.service.graph.Node;
import uk.ac.ebi.intact.util.SearchReplace;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.*;

/**
 * Interface allowing to wrap an highlightment source.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 */

public class InterproHighlightmentSource extends HighlightmentSource {

    public static final Log logger = LogFactory.getLog( InterproHighlightmentSource.class );

    /**
     * separator of keys, use to create and parse key string.
     */
    private static final String KEY_SEPARATOR = ",";
    private static final String ATTRIBUTE_OPTION_CHILDREN = "CHILDREN";
    private static final String PROMPT_OPTION_CHILDREN = "With children of the selected Interpro term";

    /**
     * The key for this source
     */
    private static final String SOURCE_KEY = "interpro";

    /**
     * Return the html code for specific options of the source to integrate int
     * the highlighting form. if the method return null, the source hasn't
     * options.
     *
     * @return the html code for specific options of the source.
     */
    public String getHtmlCodeOption( HttpSession aSession ) {
        String htmlCode;
        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession()
                .getAttribute( uk.ac.ebi.intact.application.hierarchview.business.Constants.USER_KEY );
        String check = ( String ) user
                .getHighlightOption( ATTRIBUTE_OPTION_CHILDREN );

        if ( check == null ) {
            check = "";
        }

        htmlCode = "<input type=\"checkbox\" name=\""
                   + ATTRIBUTE_OPTION_CHILDREN + "\" " + check
                   + " value=\"checked\">" + PROMPT_OPTION_CHILDREN;

        return htmlCode;
    }

    /**
     * get a collection of XRef and filter to keep only GO terms
     *
     * @param xRef the CrossReference collection
     * @return a GO term collection or an empty collection if none exists.
     */
    private Collection<String[]> filterInteractorCrossReference( Collection<CrossReference> xRef ) {
        Collection<String[]> listInterproTerm = new ArrayList( xRef.size() ); // size will be >= to needed capacity

        for ( CrossReference aXRef : xRef ) {
            String[] goTerm = new String[3];

            if ( ( aXRef.getDatabase() ).toLowerCase().equals( SOURCE_KEY ) ) {
                goTerm[0] = "Interpro";
                goTerm[1] = aXRef.getIdentifier();
                goTerm[2] = aXRef.getText();
                listInterproTerm.add( goTerm );
                if ( logger.isDebugEnabled() ) logger.debug( aXRef.getIdentifier() );
            }
        }

        return listInterproTerm;
    }

    /**
     * Returns a collection of nodes to highlight for the display
     *
     * @param aGraph               the network to display
     * @param selectedInterproTerm the selected Interpro Term
     * @param children             the children Interpro Terms of the selected Interpro Term
     * @param searchForChildren    whether it shall be searched for the children
     * @return
     * @throws IntactException
     * @throws SQLException
     */
    private Collection proteinToHighlightInteractor( Network aGraph,
                                                     String selectedInterproTerm, Collection children,
                                                     boolean searchForChildren ) {

        // if the source highlight map of the network is empty
        // it is filled with the source informations from each
        // node of the graph
        if ( aGraph.isSourceHighlightMapEmpty() ) {
            aGraph.initSourceHighlightMap();
            //user.getHVNetworkBuilder().initSourceHighlightMap( aGraph );
        }
        // return the set of proteins to highlight based on the source
        // highlighting map of the graph
        return proteinToHighlightSourceMap( aGraph, children, selectedInterproTerm,
                                            searchForChildren );
    }

    /**
     * Returns a collection of proteins to be highlighted in the graph.
     * <p/>
     * Method is called when the graph was built by the mine database table.
     *
     * @param aGraph               the graph
     * @param children             the children of the selected Interpro Term
     * @param selectedInterproTerm the selected Interpro Term
     * @param searchForChildren    whether it shall be searched for the children
     * @return
     */
    private Collection proteinToHighlightSourceMap( Network aGraph, Collection children,
                                                    String selectedInterproTerm, boolean searchForChildren ) {

        Collection nodeList = new ArrayList( 20 ); // should be enough for 90% cases
        // cases

        // retrieve the set of proteins to highlight for the source key (e.g.
        // GO) and the selected Interpro Term
        Set proteinsToHighlight = aGraph.getProteinsForHighlight( SOURCE_KEY, selectedInterproTerm );

        // if we found any proteins we add all of them to the collection
        if ( proteinsToHighlight != null ) {
            nodeList.addAll( proteinsToHighlight );
        }

        /*
         * for every children all proteins related to the current GO Term are
         * fetched from the source highlighting map of the graph and added to
         * the collection
         */
        if ( searchForChildren ) {
            for ( Object aChildren : children ) {
                proteinsToHighlight = aGraph.getProteinsForHighlight( SOURCE_KEY, ( String ) aChildren );
                if ( proteinsToHighlight != null ) {
                    nodeList.addAll( proteinsToHighlight );
                }
            }
        }
        return nodeList;
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
     * @return a collection of node to highlight
     */
    public Collection<Node> proteinToHightlight( HttpSession aSession, Network aGraph ) {

        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( Constants.USER_KEY );
        Collection children = user.getKeys();
        String selectedInterproTerm = user.getSelectedKey();

        if ( children.remove( selectedInterproTerm ) ) {
            if ( logger.isInfoEnabled() ) logger.info( selectedInterproTerm + " removed from children collection" );
        }

        // get source option
        String check = ( String ) user.getHighlightOption( ATTRIBUTE_OPTION_CHILDREN );
        boolean searchForChildren;
        if ( check != null ) {
            searchForChildren = check.equals( "checked" );
        } else {
            searchForChildren = false;
        }
        if ( logger.isInfoEnabled() ) logger.info( "Children option activated ? " + searchForChildren );

        searchForChildren = true;

        return proteinToHighlightInteractor( aGraph, selectedInterproTerm, children, searchForChildren );

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

    public List getSourceUrls( Collection<CrossReference> xRefs, Collection<String> selectedXRefs,
                               String applicationPath, IntactUserI user ) {

        // get in the Highlightment properties file where is interpro
        Properties props = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null == props ) {
            String msg = "Unable to find the Interpro hostname. "
                         + "The properties file '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' couldn't be loaded.";
            logger.error( msg );
            throw new IntactException( msg );
        }

        String interproPath = props.getProperty( "highlightment.source.Interpro.applicationPath" );

        if ( null == interproPath ) {
            String msg = "Unable to find the Interpro hostname. "
                         + "Check the 'highlightment.source.Interpro.applicationPath' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }

        // filter to keep only Interpro terms
        if ( logger.isInfoEnabled() ) logger.info( xRefs.size() + " Xref before filtering" );

        Collection<String[]> listInterproTerm;

        listInterproTerm = filterInteractorCrossReference( xRefs );

        if ( logger.isInfoEnabled() ) logger.info( listInterproTerm.size() + " Interpro term(s) after filtering" );

        // create url collection with exact size
        List urls = new ArrayList( listInterproTerm.size() );

        // list of Nodes of the Graph
        Network aGraph = user.getInteractionNetwork();

        // Create a collection of label-value object (Interpro term, URL to access a
        // nice display in interpro)
        String[] interproTermInfo;
        String interproTermId, interproTermType, interproTermDescription;

        /*
         * In order to avoid the browser to cache the response to that request
         * we stick at its end of the generated URL.
         */
        String randomParam = "&now=" + System.currentTimeMillis();

        if ( listInterproTerm != null && !listInterproTerm.isEmpty() ) {
            for ( String[] aListInterproTerm : listInterproTerm ) {
                interproTermInfo = aListInterproTerm;
                interproTermId = interproTermInfo[1];
                interproTermType = interproTermInfo[0];
                interproTermDescription = interproTermInfo[2];

                int interproTermCount = aGraph.getDatabaseTermCount( interproTermId );

                if ( logger.isInfoEnabled() ) logger.info( "goTermCount: " + interproTermCount );

                // to summarize
                if ( logger.isInfoEnabled() )
                    logger.info( "interproTermType=" + interproTermType + " | interproTermId=" + interproTermId
                                 + " | interproTermDescription=" + interproTermDescription + " | interproTermCount="
                                 + interproTermCount );


                String directHighlightUrl = applicationPath +
                                            "/source.do?keys=${selected-children}&clicked=${id}&type=${type}"
                                            + randomParam;
                String hierarchViewURL = null;

                try {
                    hierarchViewURL = URLEncoder.encode( directHighlightUrl, "UTF-8" );
                }
                catch ( UnsupportedEncodingException e ) {
                    logger.error( "Error duing UTF-8 encoding of url: " + directHighlightUrl, e );
                }

                // replace ${selected-children}, ${id} by the Interpro id and ${type} by Interpro
                if ( logger.isInfoEnabled() ) logger.info( "direct highlight URL: " + directHighlightUrl );

                directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${selected-children}", interproTermId );
                directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${id}", interproTermId );
                directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${type}", interproTermType );

                if ( logger.isInfoEnabled() ) logger.info( "direct highlight URL (modified): " + directHighlightUrl );

                String quickInterproUrl = interproPath + "/DisplayIproEntry?ac=" + interproTermId + "&format=normal";

                String quickInterproGraphUrl = interproPath + "/DisplayInterproTerm?selected="
                                               + interproTermId + "&intact=true&format=contentonly&url="
                                               + hierarchViewURL + "&frame=_top";
                logger.info( "Xref: " + interproTermId );

                boolean selected = false;
                if ( selectedXRefs != null && selectedXRefs.contains( interproTermId ) ) {
                    if ( logger.isInfoEnabled() ) logger.info( interproTermId + " SELECTED" );
                    selected = true;
                }

                urls.add( new SourceBean( interproTermId, interproTermType, interproTermDescription,
                                          interproTermCount, quickInterproUrl, quickInterproGraphUrl, directHighlightUrl, selected,
                                          applicationPath ) );
            }
        }

        // sort the source list by count
        Collections.sort( urls );

        return urls;
    } // getSourceUrls

    /**
     * Parse the set of key generate by the source and give back a collection of
     * keys.
     *
     * @param someKeys a string which contains some key separates by a character.
     * @return the splitted version of the key string as a collection of String.
     */
    public Collection<String> parseKeys( String someKeys ) {
        Collection keys = new Vector();

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