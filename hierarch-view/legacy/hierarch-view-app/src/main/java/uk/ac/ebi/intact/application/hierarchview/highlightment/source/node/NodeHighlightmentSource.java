package uk.ac.ebi.intact.application.hierarchview.highlightment.source.node;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.HighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.LabelValueBean;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.OptionGenerator;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.service.graph.Node;
import uk.ac.ebi.intact.util.SearchReplace;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;


/**
 * Abstract class allowing to wrap an highlightment source.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk) & Alexandre Liban (aliban@ebi.ac.uk)
 */

public abstract class NodeHighlightmentSource implements HighlightmentSource {

    private static final Log logger = LogFactory.getLog( NodeHighlightmentSource.class );

    private static final String KEY_SEPARATOR = ",";
    private static final String ATTRIBUTE_OPTION_CHILDREN = "CHILDREN";
    private static final Map<String, String> nodeHighlightmentSources;

    static {
        nodeHighlightmentSources = new HashMap<String, String>();
        List<LabelValueBean> sources = OptionGenerator.getHighlightmentSources( "node" );
        for ( LabelValueBean labelBean : sources ) {
            nodeHighlightmentSources.put( labelBean.getLabel(), labelBean.getDescription() );
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

    /**
     * Return the html code for specific options of the source to be integrated
     * in the highlighting form.
     * if the method return null, the source hasn't options.
     *
     * @param aSession the current session.
     * @return the html code for specific options of the source.
     */
    abstract public String getHtmlCodeOption( HttpSession aSession );

    /**
     * Create a set of protein we must highlight in the graph given in parameter.
     * The protein selection is done according to the source keys stored in the IntactUser.
     * Some options (specific to each implementation) could have been set and stored in the
     * session, that method has to get them and care about.
     *
     * @param aSession the session where to find selected keys.
     * @param aGraph   the graph we want to highlight.
     * @return a collection of nodes to highlight.
     */
    abstract public Collection<Node> proteinToHightlight( HttpSession aSession, Network aGraph );


    /**
     * Allows to update the session object with options stored in the request.
     * These parameters are specific of the implementation.
     *
     * @param aRequest request in which we have to get parameters to save in the session.
     * @param aSession session in which we have to save the parameter.
     */
    public void saveOptions( HttpServletRequest aRequest, HttpSession aSession ) {
        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession()
                .getAttribute( Constants.USER_KEY );
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
     * @param selectedXRefs   The collection of selected XRef
     * @param applicationPath our application path
     * @param user            the current user
     * @return a set of URL pointing on the highlightment source.
     */
    abstract public List getSourceUrls( Network network,
                                        Collection<String> selectedXRefs,
                                        String applicationPath, IntactUserI user );


    /**
     * Parse the set of key generate by the source and give back a collection of keys.
     *
     * @param someKeys a string which contains some key separates by a character.
     * @return the splitted version of the key string as a collection of String.
     */
    public static Collection<String> parseKeys( String someKeys ) {

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

    public String getDirectHighlightUrl( String applicationPath, String termId, String termType, String randomParam ) {

        String directHighlightUrl = applicationPath
                                    + "/source.do?keys=${selected-children}&clicked=${id}&type=${type}"
                                    + randomParam;

        // replace ${selected-children}, ${id} by the term id and ${type} by term type
        if ( logger.isDebugEnabled() ) logger.debug( "direct highlight URL: " + directHighlightUrl );

        directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${selected-children}", termId );
        directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${id}", termId );
        directHighlightUrl = SearchReplace.replace( directHighlightUrl, "${type}", termType );

        if ( logger.isDebugEnabled() )
            logger.debug( "direct highlight URL (modified): " + directHighlightUrl );

        return directHighlightUrl;
    }

    public String getHierarchViewUrl( String randomParam, String applicationPath ) {

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
