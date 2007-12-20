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
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.service.graph.Node;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.regex.Pattern;

/**
 * TODO comment that class header
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class RoleHighlightmentSource extends NodeHighlightmentSource {

    private static final Log logger = LogFactory.getLog( RoleHighlightmentSource.class );

    private static final String ATTRIBUTE_OPTION_CHILDREN = "CHILDREN";
    private static final String PROMPT_OPTION_CHILDREN = "With children of the selected Role term";

    private static final Pattern MI_REF_PATTERN = Pattern.compile( "MI:[0-9]{4}" );

    /**
     * The key for this source 'Role'
     */
    public static final String SOURCE_KEY;

    public static final String SOURCE_CLASS;
    private static final String applicationPath;

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

        applicationPath = props.getProperty( "highlightment.source.node.Role.applicationPath" );

        if ( null == applicationPath ) {
            String msg = "Unable to find the Role applicationPath. "
                         + "Check the 'highlightment.source.node.Role.applicationPath' property in the '"
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

    /**
     * Return the html code for specific options of the source to integrate int
     * the highlighting form. if the method return null, the source hasn't
     * options.
     *
     * @return the html code for specific options of the source.
     */
    public String getHtmlCodeOption( HttpSession aSession ) {
        String htmlCode;
        String userKey = uk.ac.ebi.intact.application.hierarchview.business.Constants.USER_KEY;
        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( userKey );
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
     * Returns a collection of proteins to be highlighted in the graph.
     *
     * @param aGraph           the graph
     * @param selectedRoleTerm the selected Role Term
     * @return
     */
    private Collection<Node> proteinToHighlightSourceMap( Network aGraph, String selectedRoleTerm ) {

        Collection<Node> nodeList = new ArrayList( 20 ); // should be enough for 90% cases

        // retrieve the set of proteins to highlight for the source key (e.g. Role) and
        // the selected Role Term
        Set<Node> proteinsToHighlight = aGraph.getNodesForHighlight( SOURCE_KEY, selectedRoleTerm );

        // if we found any proteins we add all of them to the collection
        if ( proteinsToHighlight != null ) {
            nodeList.addAll( proteinsToHighlight );
        }

        return nodeList;
    }


    /**
     * Create a set of protein we must highlight in the graph given in
     * parameter. The protein selection is done according to the source keys
     * stored in the IntactUser. Keys are Role terms, so we select (and highlight)
     * every protein which awned that Role term. If the children option is
     * activated, all proteins which owned a children of the selected Role term
     * are selected.
     *
     * @param aSession the session where to find selected keys.
     * @param aGraph   the graph we want to highlight
     * @return a collection of node to highlight
     */
    public Collection<Node> proteinToHightlight( HttpSession aSession, Network aGraph ) {

        IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( Constants.USER_KEY );
        Collection children = user.getKeys();
        String selectedRole = user.getSelectedKey();

        logger.debug( "getKeys=" + children + " | selectedTerm=" + selectedRole );
        if ( children.remove( selectedRole ) ) {
            if ( logger.isDebugEnabled() ) logger.debug( selectedRole + " removed from children collection" );
        }

        // if the source highlight map of the network is empty
        // it is filled with the source informations from each
        // node of the graph
        if ( aGraph.isNodeHighlightMapEmpty() ) {
            aGraph.initHighlightMap();
        }
        logger.info( "Get Proteins for Hightlight " + proteinToHighlightSourceMap( aGraph, selectedRole ) );

        return proteinToHighlightSourceMap( aGraph, selectedRole );
    }


    public List getSourceUrls( Network network, Collection<String> selectedIDs,
                               String applicationPath, IntactUserI user ) {

        List<SourceBean> urls = new ArrayList();

        // filter to keep only Role terms
        if ( network.isNodeHighlightMapEmpty() ) {
            network.initHighlightMap();
        }

        Map highlightRoleMap = ( Map ) network.getNodeHighlightMap().get( SOURCE_KEY );

        if ( highlightRoleMap != null && !highlightRoleMap.isEmpty() ) {
            Set<String> keySet = highlightRoleMap.keySet();

            if ( keySet != null && !keySet.isEmpty() ) {
                for ( String termInfo : keySet ) {
                    String termType = SOURCE_KEY;
                    CrossReference xref = network.getCrossReferenceById( termInfo );
                    String termId = termInfo;
                    String termDecription = null;
                    if ( termInfo.startsWith( "E" ) ) {
                        termDecription = Constants.EXPERMIENTAL_ROLE + " Role: ";
                    }
                    if ( termInfo.startsWith( "B" ) ) {
                        termDecription = Constants.BIOLOGICAL_ROLE + " Role: ";
                    }

                    if ( xref != null ) {

                        if ( xref.hasText() ) {
                            if ( termDecription == null ) {
                                termDecription = xref.getText();
                            } else {
                                termDecription += xref.getText();
                            }
                        }
                    }
                    int termCount = network.getDatabaseTermCount( termType, termInfo );

                    // to summarize
                    if ( logger.isDebugEnabled() )
                        logger.debug( "TermType=" + termType + " | " +
                                      "TermId=" + termId + " | " +
                                      "TermDescription=" + termDecription + " | " +
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
                        quickUrl = this.applicationPath + "/?termId=" + MI_REF + "&format=contentonly";


                        quickGraphUrl = this.applicationPath + "/?termId="
                                        + MI_REF + "&intact=true&format=contentonly&url="
                                        + hierarchViewURL + "&frame=_top";
                    }

                    boolean selected = false;
                    if ( selectedIDs != null && selectedIDs.contains( termId ) ) {
                        if ( logger.isInfoEnabled() ) logger.info( termId + " SELECTED" );
                        selected = true;
                    }

                    urls.add( new SourceBean( termId, termType, termDecription, termCount,
                                              quickUrl, quickGraphUrl, directHighlightUrl, selected, applicationPath ) );

                }

                // sort the source list by count
                Collections.sort( urls );
            }
        }

        return urls;
    }
}
