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
package uk.ac.ebi.intact.application.hierarchview.highlightment.source.node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.service.graph.Node;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Interface allowing to wrap an highlightment source.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class RoleHighlightmentSource extends NodeHighlightmentSource {

    private static final Log logger = LogFactory.getLog( RoleHighlightmentSource.class );

    private static final Pattern MI_REF_PATTERN = Pattern.compile( "MI:[0-9]{4}" );

    private static final String EXPERMIENTAL_ROLE = "Experimental";

    private static final String BIOLOGICAL_ROLE = "Biological";

    /**
     * The key for this source 'Role'
     */
    public static final String SOURCE_KEY;

    /**
     * The class for this source 'role'
     */
    static final String SOURCE_CLASS;

    private static final String MIRefPath;

    private HashMap<String, CrossReference> roleRefMap;

    private Map<String, Set<String>> roleNodeMap;


    static {

        // get in the Highlightment properties file where is interpro
        Properties props = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null == props ) {
            String msg = "Unable to find the Role hostname. "
                         + "The properties file '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' couldn't be loaded.";
            logger.error( msg );
            throw new IntactException( msg );
        }
        SOURCE_KEY = props.getProperty( "highlightment.source.node.Role.label" );

        if ( null == SOURCE_KEY ) {
            String msg = "Unable to find the Role Label. "
                         + "Check the 'highlightment.source.node.Role.label' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }

        MIRefPath = props.getProperty( "highlightment.source.node.Role.applicationPath" );

        if ( null == MIRefPath ) {
            String msg = "Unable to find the Role MIRefPath. "
                         + "Check the 'highlightment.source.node.Role.MIRefPath' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }

        SOURCE_CLASS = props.getProperty( "highlightment.source.node.Role.class" );

        if ( null == SOURCE_CLASS ) {
            String msg = "Unable to find the Role Class. "
                         + "Check the 'highlightment.source.node.Role.class' property in the '"
                         + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                         + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }
    }

    public RoleHighlightmentSource() {
         roleRefMap = new HashMap<String, CrossReference>();
        roleNodeMap = new Hashtable<String, Set<String>>();
    }

    public void addToSourceMap( String termId, CrossReference termObject ) {
        roleRefMap.put( termId,  termObject );
    }

    public void addToNodeMap( String termId, Node node ) {
        // the nodes realted to the given sourceID are fetched
        Set<String> sourceNodes = roleNodeMap.get( termId );

        // if no set exists a new one is created and put into the sourceMap
        if ( sourceNodes == null ) {
            // a hashset is used to avoid duplicate entries
            sourceNodes = new HashSet<String>();
            roleNodeMap.put( termId, sourceNodes );
        }
        sourceNodes.add( node.getId() );
    }

    public Map<String, Set<String>> getNodeMap() {
        return roleNodeMap;
    }

    public List getSourceUrls(Network network,
                              Collection<String> selectedSourceTerms,
                              HttpServletRequest request, String applicationPath) {

        if ( roleNodeMap == null || roleNodeMap.isEmpty() ) {
            network.initHighlightMap(request);
        }

        List<SourceBean> urls = new ArrayList<SourceBean>();

        if ( roleNodeMap != null && !roleNodeMap.isEmpty() ) {
            Set<String> keySet = roleNodeMap.keySet();

            if ( keySet != null && !keySet.isEmpty() ) {
                Set<String> cloneKeySet = new HashSet<String>();
                cloneKeySet.addAll( keySet );
                keySet = cloneKeySet;

                for ( String termId : keySet ) {
                    String termType = SOURCE_KEY;
                    String termDescription = null;

                    if ( termId.startsWith( "E" ) ) {
                        termDescription = EXPERMIENTAL_ROLE + " Role: ";
                    }
                    if ( termId.startsWith( "B" ) ) {
                        termDescription = BIOLOGICAL_ROLE + " Role: ";
                    }
                    if ( roleRefMap != null ) {
                        CrossReference xref = roleRefMap.get( termId );

                        if ( xref != null && xref.hasText() ) {
                            if ( termDescription == null ) {
                                termDescription = xref.getText();
                            } else {
                                termDescription += xref.getText();

                            }
                        }
                    }

                    int termCount = roleNodeMap.get(termId ).size();

                    // to summarize
                    if ( logger.isDebugEnabled() )
                        logger.debug( "TermType=" + termType + " | " +
                                      "TermId=" + termId + " | " +
                                      "TermDescription=" + termDescription + " | " +
                                      "TermCount=" + termCount );

                    /*
                    * In order to avoid the browser to cache the response to that request
                    * we stick at its end of the generated URL.
                    */
                    String randomParam = "&now=" + System.currentTimeMillis();


                    String directHighlightUrl = getDirectHighlightUrl( applicationPath, termId, termType, randomParam );
                    String hierarchViewURL = getHierarchViewUrl( randomParam, applicationPath );

                    String quickUrl = null;
                    String quickGraphUrl = null;

                    if ( MI_REF_PATTERN.matcher( termId ).find() ) {
                        String MI_REF = termId.split( "\\|" )[1];
                        quickUrl = MIRefPath + "/?termId=" + MI_REF + "&format=contentonly";


                        quickGraphUrl = MIRefPath + "/?termId="
                                        + MI_REF + "&intact=true&format=contentonly&url="
                                        + hierarchViewURL + "&frame=_top";
                    }

                    boolean selected = false;
                    if ( selectedSourceTerms != null ) {
                        if ( selectedSourceTerms.contains( termId ) ) {
                            if ( logger.isInfoEnabled() ) logger.info( termId + " SELECTED" );
                            selected = true;
                        }
                        directHighlightUrl = getDirectHighlightUrl( applicationPath, termId, selectedSourceTerms, termType, randomParam );
                    } else {
                        directHighlightUrl = getDirectHighlightUrl( applicationPath, termId, termType, randomParam );
                    }

                    urls.add( new SourceBean( termId, termType, termDescription, termCount,
                                              quickUrl, quickGraphUrl, directHighlightUrl, selected, applicationPath ) );

                }

                // sort the source list by count
                Collections.sort( urls );
            }
        }

        return urls;
    }

    public void prepare() {
        roleNodeMap.clear();
        roleRefMap.clear();
    }

    public static RoleHighlightmentSource getInstance(HttpServletRequest request) {
        String attName = RoleHighlightmentSource.class.getName();

        final RoleHighlightmentSource source;

        if (request.getSession().getAttribute(attName) != null) {
            source = (RoleHighlightmentSource) request.getSession().getAttribute(attName);
        } else {
            source = new RoleHighlightmentSource();
            request.getSession().setAttribute(attName, source);
        }

        return source;
    }
}
