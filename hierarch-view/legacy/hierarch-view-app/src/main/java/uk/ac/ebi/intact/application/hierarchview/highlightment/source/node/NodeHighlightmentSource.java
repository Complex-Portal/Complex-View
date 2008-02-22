package uk.ac.ebi.intact.application.hierarchview.highlightment.source.node;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUser;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.HighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.LabelValueBean;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.OptionGenerator;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.service.graph.Node;
import uk.ac.ebi.intact.util.SearchReplace;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;


/**
 * Abstract Class which define HighlightmentSource for Nodes.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */

public abstract class NodeHighlightmentSource implements HighlightmentSource {

    private static final Log logger = LogFactory.getLog( NodeHighlightmentSource.class );

    private static final String KEY_SEPARATOR = ", ";
    private static final String ATTRIBUTE_OPTION_CUMULATIVE = "CUMULATIVE";
    private static final String ATTRIBUTE_OPTION_CHILDREN = "CHILDREN";
    private static final Map<String, String> nodeHighlightmentSources;
    public static final boolean isCumulative;

    static {
        nodeHighlightmentSources = new HashMap<String, String>();
        List<LabelValueBean> sources = OptionGenerator.getHighlightmentSources( "node" );
        for ( LabelValueBean labelBean : sources ) {
            nodeHighlightmentSources.put( labelBean.getLabel(), labelBean.getDescription() );
        }

        // get in the Highlightment properties file where is go
        Properties props = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null == props ) {
            String msg = "Unable to find the go hostname. The properties file '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' couldn't be loaded.";
            logger.error( msg );
            throw new IntactException( msg );
        }

        String highlightOption = props.getProperty( "highlightment.option" );
        isCumulative = highlightOption.equals( ATTRIBUTE_OPTION_CUMULATIVE );

        if ( null == highlightOption ) {
            String msg = "Unable to find the highlightOption. "
                         + "Check the 'highlightment.option' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }
    }

    public static NodeHighlightmentSource getHighlightmentSourceBySourceKey( String sourceKey ) {
        return getHighlightmentSource( nodeHighlightmentSources.get( sourceKey ) );
    }

    /**
     * Provides a implementation of NodeHighlightmentSource by its name.
     * for example you have an implementation of this abstract class called : <b>GoHighlightmentSource</b>.
     * so, you could call the following method to get an instance of this class :
     * <br>
     * <b>NodeHighlightmentSource.getHighlightmentSource ("mypackage.GoHighlightmentSource");</b>
     * <br>
     * then you're able to use methods provided by this abstract class without to know
     * what implementation you are using.
     *
     * @param aClassName the name of the implementation class you want to get
     * @return an NodeHighlightmentSource object, or null if an error occurs.
     */
    public static NodeHighlightmentSource getHighlightmentSource( String aClassName ) {

        Object object = null;

        try {

            // create a class by its name
            Class cls = Class.forName( aClassName );

            // Create an instance of the class invoked
            object = cls.newInstance();

            if ( !( object instanceof NodeHighlightmentSource ) ) {
                // my object is not from the proper type
                logger.error( aClassName + " is not a NodeHighlightmentSource" );
                return null;
            }
        } catch ( Exception e ) {
            logger.error( "Unable to instanciate object:" + aClassName );
            // nothing to do, object is already setted to null
        }

        return ( NodeHighlightmentSource ) object;

    } // NodeHighlightmentSource

//    /**
//     * Return the html code for specific options of the source to be integrated
//     * in the highlighting form.
//     * if the method return null, the source hasn't options.
//     *
//     * @param aSession the current session.
//     * @return the html code for specific options of the source.
//     */
//    abstract public String getHtmlCodeOption( HttpSession aSession );

    public abstract Map<String, Set<String>> getNodeMap();

    /**
     * Create a set of protein we must highlight in the graph given in
     * parameter. The protein selection is done according to the source keys
     * stored in the IntactUser. Keys are for example GO terms, so we select (and highlight)
     * every protein which awned that Role term.
     *
     * @param network the graph we want to highlight
     * @return a collection of node to highlight
     */
    public Collection<Node> proteinToHightlight( IntactUserI user, Network network ) {

        Collection<String> selectedTerms = user.getSelectedKeys();

        if ( logger.isDebugEnabled() ) {
            logger.debug( "Get Nodes for Source(s): " + selectedTerms );
        }

        Collection<Node> nodeList = new ArrayList( 20 ); // should be enough for 90% cases

        // retrieve the set of proteins to highlight for the source key (e.g. GO) and the selected GO Terms
        Set<Node> proteinsToHighlight = null;
        if ( selectedTerms != null ) {
            for ( String selectedGOTerm : selectedTerms ) {
                Set<String> nodeIdsToHighlight = getNodeMap().get( selectedGOTerm );
                proteinsToHighlight = network.getNodesByIds( nodeIdsToHighlight );
                // if we found any proteins we add all of them to the collection
                if ( proteinsToHighlight != null ) {
                    nodeList.addAll( proteinsToHighlight );
                }
            }
        }
        return nodeList;
    }

    /**
     * Allows to update the session object with options stored in the request.
     * These parameters are specific of the implementation.
     *
     * @param aRequest request in which we have to get parameters to save in the session.
     * @param aSession session in which we have to save the parameter.
     */
    public void saveOptions( HttpServletRequest aRequest, HttpSession aSession ) {
        IntactUserI user = IntactUser.getCurrentInstance(aSession);
        String[] result = aRequest.getParameterValues( ATTRIBUTE_OPTION_CHILDREN );

        if ( result != null )
            user.addHighlightOption( ATTRIBUTE_OPTION_CHILDREN, result[0] );
    }

    /**
     * Return a collection of URL corresponding to the selected protein and source
     * eg. produce a list of MoleculeType terms if MoleculeType is the source.<br>
     * if the method send back no URL, the given parameter is wrong.
     *
     * @param network
     * @param selectedTerms   The collection of selected XRef
     * @param applicationPath our application path
     * @return a set of URL pointing on the highlightment source.
     */
    abstract public List getSourceUrls( Network network,
                                        Collection<String> selectedTerms,
                                        String applicationPath );

    /**
     * Parse the set of key generate by the source and give back a collection of keys.
     *
     * @param someKeys a string which contains some key separates by a character.
     * @return the splitted version of the key string as a collection of String.
     */
    public static Set<String> parseKeys( String someKeys ) {

        if ( ( null == someKeys ) || ( someKeys.length() < 1 ) || "null".equals( someKeys ) ) {
            return null;
        }

        Set<String> keys = new HashSet<String>();

        if ( isCumulative ) {
            StringTokenizer st = new StringTokenizer( someKeys, KEY_SEPARATOR );

            while ( st.hasMoreTokens() ) {
                String key = st.nextToken();
                if ( !"null".equals( key ) ) {
                    keys.add( key );
                }
            }
        }
        return keys;
    }

    String getDirectHighlightUrl( String applicationPath, String termId, String termType, String randomParam ) {

        String directHighlightUrl = applicationPath
                                    + "/source.do?keys=${selected-terms}&clicked=${id}&type=${type}"
                                    + randomParam;

        // replace ${selected-children}, ${id} by the term id and ${type} by term type
        if ( logger.isDebugEnabled() ) logger.debug( "direct highlight URL: " + directHighlightUrl );

        directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${selected-terms}", "null" );
        directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${id}", termId );
        directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${type}", termType );

        if ( logger.isDebugEnabled() )
            logger.debug( "direct highlight URL (modified): " + directHighlightUrl );

        return directHighlightUrl;
    }

    String getDirectHighlightUrl( String applicationPath, String termId, Collection<String> selectedTermIds, String termType, String randomParam ) {

        String directHighlightUrl = applicationPath
                                    + "/source.do?keys=${selected-terms}&clicked=${id}&type=${type}"
                                    + randomParam;

        // replace ${selected-children}, ${id} by the term id and ${type} by term type
        if ( logger.isDebugEnabled() ) logger.debug( "direct highlight URL: " + directHighlightUrl );

        String selectedTerms = "null";
        if ( selectedTermIds != null && !selectedTermIds.isEmpty() ) {
            StringBuffer buffer = new StringBuffer();
            Iterator<String> iterator = selectedTermIds.iterator();

            while ( iterator.hasNext() ) {
                String id = iterator.next();
                buffer.append( id );
                if ( iterator.hasNext() ) {
                    buffer.append( KEY_SEPARATOR );
                }
            }
            selectedTerms = buffer.toString();
        }
        directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${selected-terms}", selectedTerms );
        directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${id}", termId );
        directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${type}", termType );

        if ( logger.isDebugEnabled() )
            logger.debug( "direct highlight URL (modified): " + directHighlightUrl );

        return directHighlightUrl;
    }

    String getHierarchViewUrl( String randomParam, String applicationPath ) {

        String directHighlightUrl = applicationPath
                                    + "/source.do?keys=${selected-children}&clicked=${id}&type=${type}"
                                    + randomParam;
        String hierarchViewURL = null;

        try {
            hierarchViewURL = URLEncoder.encode( directHighlightUrl, "UTF-8" );
        }
        catch ( UnsupportedEncodingException e ) {
            logger.error( e );
        }
        return hierarchViewURL;
    }

} // NodeHighlightmentSource
