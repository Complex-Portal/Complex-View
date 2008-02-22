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
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.highlightment.source.HighlightmentSource;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.LabelValueBean;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.OptionGenerator;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.service.graph.Edge;
import uk.ac.ebi.intact.util.SearchReplace;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Abstract Class which define HighlightmentSource for Edges.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public abstract class EdgeHighlightmentSource implements HighlightmentSource {

    private static final Log logger = LogFactory.getLog( EdgeHighlightmentSource.class );

    private static final String KEY_SEPARATOR = ",";
    private static final String ATTRIBUTE_OPTION_CUMULATIVE = "CUMULATIVE";
    private static final Map<String, String> edgeHighlightmentSources;
    public static final boolean isCumulative;

    static {

        edgeHighlightmentSources = new HashMap<String, String>();
        List<LabelValueBean> sources = OptionGenerator.getHighlightmentSources( "edge" );
        for ( LabelValueBean labelBean : sources ) {
            edgeHighlightmentSources.put( labelBean.getLabel(), labelBean.getDescription() );
        }
        edgeHighlightmentSources.put( ConfidenceHighlightmentSource.SOURCE_KEY, ConfidenceHighlightmentSource.SOURCE_CLASS );
        edgeHighlightmentSources.put( PublicationHighlightmentSource.SOURCE_KEY, PublicationHighlightmentSource.SOURCE_CLASS );

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

    public abstract Map<String, Set<String>> getEdgeMap();

    /**
     * Returns a collection of proteins to be highlighted in the graph.
     * <p/>
     * Method is called when the graph was built by the mine database table.
     *
     * @param user
     *@param network       the network @return
     */
    public Collection<Edge> interactionToHightlight(IntactUserI user, Network network) {

        Collection<String> selectedTerms = user.getSelectedKeys();

        if ( logger.isDebugEnabled() ) {
            logger.debug( "Get Edges for Source(s): " + selectedTerms );
        }

        Collection<Edge> edgeList = new ArrayList( 20 ); // should be enough for 90% cases

        // retrieve the set of proteins to highlight for the source key (e.g. GO) and the selected GO Terms
        Set<Edge> edgesToHighlight = null;
        if ( selectedTerms != null ) {
            for ( String selectedTerm : selectedTerms ) {
                Set<String> edgeIdsToHighlight = getEdgeMap().get( selectedTerm );
                edgesToHighlight = network.getEdgesByIds( edgeIdsToHighlight );
                // if we found any proteins we add all of them to the collection
                if ( edgesToHighlight != null ) {
                    edgeList.addAll( edgesToHighlight );
                }
            }
        }

        return edgeList;
    }

    /**
     * Return a collection of URL corresponding to the selected protein and source
     * eg. produce a list of terms of selected source.<br>
     * if the method send back no URL, the given parameter is wrong.
     *
     * @param network
     * @param selectedXRefs   The collection of selected XRef
     * @param applicationPath our application path
     * @return a set of URL pointing on the highlightment source.
     */
    abstract public List<SourceBean> getSourceUrls( Network network,
                                                    Collection<String> selectedXRefs,
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
} // EdgeHighlightmentSource