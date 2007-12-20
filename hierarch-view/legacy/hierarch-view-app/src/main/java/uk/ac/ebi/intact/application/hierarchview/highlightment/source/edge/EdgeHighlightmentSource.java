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
package uk.ac.ebi.intact.application.hierarchview.highlightment.source.edge;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.HighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.LabelValueBean;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.OptionGenerator;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.service.graph.Edge;
import uk.ac.ebi.intact.util.SearchReplace;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * TODO comment that class header
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public abstract class EdgeHighlightmentSource implements HighlightmentSource {

    private static Log logger = LogFactory.getLog( EdgeHighlightmentSource.class );

    private static final String KEY_SEPARATOR = ",";
    private static final String ATTRIBUTE_OPTION_CHILDREN = "CHILDREN";
    private static final Map<String, String> edgeHighlightmentSources;

    static {

        edgeHighlightmentSources = new HashMap<String, String>();
        List<LabelValueBean> sources = OptionGenerator.getHighlightmentSources( "edge" );
        for ( LabelValueBean labelBean : sources ) {
            edgeHighlightmentSources.put( labelBean.getLabel(), labelBean.getDescription() );
        }
        edgeHighlightmentSources.put( ConfidenceHighlightmentSource.SOURCE_KEY, ConfidenceHighlightmentSource.SOURCE_CLASS );
        edgeHighlightmentSources.put( PMIDHighlightmentSource.SOURCE_KEY, PMIDHighlightmentSource.SOURCE_CLASS );
    }

    public static EdgeHighlightmentSource getHighlightmentSourceBySourceKey( String sourceKey ) {
        return getHighlightmentSource( edgeHighlightmentSources.get( sourceKey ) );
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
    public static EdgeHighlightmentSource getHighlightmentSource( String aClassName ) {

        Object object = null;

        try {

            // create a class by its name
            Class cls = Class.forName( aClassName );

            // Create an instance of the class invoked
            object = cls.newInstance();

            if ( !( object instanceof EdgeHighlightmentSource ) ) {
                // my object is not from the proper type
                logger.error( aClassName + " is not a EdgeHighlightmentSource" );
                return null;
            }

        } catch ( Exception e ) {
            logger.error( "Unable to instanciate object:" + aClassName );
            // nothing to do, object is already setted to null
        }

        return ( EdgeHighlightmentSource ) object;

    }

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
    abstract public Collection<Edge> interactionToHightlight( HttpSession aSession, Network aGraph );


    /**
     * Allows to update the session object with options stored in the request.
     * These parameters are specific of the implementation.
     *
     * @param aRequest request in which we have to get parameters to save in the session.
     * @param aSession session in which we have to save the parameter.
     */
    public void saveOptions( HttpServletRequest aRequest, HttpSession aSession ) {
        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( Constants.USER_KEY );
        String[] result = aRequest.getParameterValues( ATTRIBUTE_OPTION_CHILDREN );

        if ( result != null )
            user.addHighlightOption( ATTRIBUTE_OPTION_CHILDREN, result[0] );
    }


    /**
     * Return a collection of URL corresponding to the selected protein and source
     * eg. produce a list of terms of selected source.<br>
     * if the method send back no URL, the given parameter is wrong.
     *
     * @param network
     * @param selectedXRefs   The collection of selected XRef
     * @param applicationPath our application path
     * @param user            the current user
     * @return a set of URL pointing on the highlightment source.
     */
    abstract public List<SourceBean> getSourceUrls( Network network,
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
} // EdgeHighlightmentSource