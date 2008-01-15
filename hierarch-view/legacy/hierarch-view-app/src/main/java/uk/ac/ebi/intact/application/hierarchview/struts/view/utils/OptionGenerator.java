/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.struts.view.utils;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.HVNetworkBuilder;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * Allows to create some collection to populate option list in HTML form.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class OptionGenerator {

    private static Logger logger = Logger.getLogger( Constants.LOGGER_NAME );

    /**
     * create a collection of LabelValueBean object from a properties file
     *
     * @return a collection of LabelValueBean object
     */
    public static List<LabelValueBean> getHighlightmentSources( String option ) {

        List<LabelValueBean> sources = new ArrayList<LabelValueBean>();

        // read the Highlighting.proterties file
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null != properties ) {
            if ( !option.equals( "all" ) ) {
                String highlightmentPath = null;
                if ( option.equals( "node" ) ) {
                    highlightmentPath = "highlightment.source.node.";
                }
                if ( option.equals( "edge" ) ) {
                    highlightmentPath = "highlightment.source.edge.";
                }

                String sourceList = properties.getProperty( highlightmentPath + "allowed" );

                if ( ( null == sourceList ) || ( sourceList.length() < 1 ) ) {
                    logger.warn( "Unable to find the property: +" + highlightmentPath + "allowed (" +
                                 StrutsConstants.HIGHLIGHTING_PROPERTY_FILE + ")" );
                    return null;
                }

                // parse source list
                String token = properties.getProperty( highlightmentPath + "token" );

                if ( ( null == token ) || ( token.length() < 1 ) ) {
                    logger.warn( "Unable to find the property: " + highlightmentPath + "token (" +
                                 StrutsConstants.HIGHLIGHTING_PROPERTY_FILE + ")" );
                    return null;
                }

                StringTokenizer st = new StringTokenizer( sourceList, token );

                while ( st.hasMoreTokens() ) {
                    String sourceKey = st.nextToken();
                    String propName = highlightmentPath + sourceKey + ".label";
                    String label = properties.getProperty( propName );

                    if ( ( null == label ) || ( label.length() < 1 ) ) {
                        logger.warn( "Unable to find the property: " + propName + " (" +
                                     StrutsConstants.HIGHLIGHTING_PROPERTY_FILE + ")" );
                        continue;
                    }
                    String clazz = properties.getProperty( highlightmentPath + sourceKey + ".class" );

                    sources.add( new LabelValueBean( label, sourceKey, clazz ) );
                } // while
            } else {

                List<LabelValueBean> values = getHighlightmentSources( "node" );
                if ( values != null && !values.isEmpty() ) {
                    sources.addAll( values );
                }

                values = getHighlightmentSources( "edge" );
                if ( values != null && !values.isEmpty() ) {
                    sources.addAll( values );
                }
            }
        } else {
            logger.warn( "Unable to load the properties file: " + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE );
        }

        return sources;
    } // getHighlightmentSources


    /**
     * create a LabelValueBean object corresponding to the default source if it exists
     *
     * @return a LabelValueBean object or null is no default.
     */
    public static LabelValueBean getDefaultSource() {

        // read the Highlighting.proterties file
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null != properties ) {

            String sourceList = properties.getProperty( "highlightment.source.node.allowed" );

            if ( ( null == sourceList ) || ( sourceList.length() < 1 ) ) {
                logger.warn( "Unable to find the property: highlightment.source.node.allowed (" +
                             StrutsConstants.HIGHLIGHTING_PROPERTY_FILE + ")" );
                return null;
            }

            // parse source list
            String token = properties.getProperty( "highlightment.source.node.token" );

            if ( ( null == token ) || ( token.length() < 1 ) ) {
                logger.warn( "Unable to find the property: highlightment.source.node.token (" +
                             StrutsConstants.HIGHLIGHTING_PROPERTY_FILE + ")" );
                return null;
            }

            StringTokenizer st = new StringTokenizer( sourceList, token );

            if ( st.hasMoreTokens() ) {
                String sourceKey = st.nextToken();
                String propName = "highlightment.source.node." + sourceKey + ".label";
                String label = properties.getProperty( propName );

                if ( ( null == label ) || ( label.length() < 1 ) ) {
                    logger.warn( "Unable to find the property: " + propName + " (" +
                                 StrutsConstants.HIGHLIGHTING_PROPERTY_FILE + ")" );
                }

                return new LabelValueBean( label, sourceKey, "" );
            }
        } else {
            logger.warn( "Unable to load the properties file: " + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE );
        }

        return null;
    } // getDefaultSource

    /**
     * create a LabelValueBean object corresponding to the default source if it exists
     *
     * @return a LabelValueBean object or null if it doesn't exists.
     */
    public static LabelValueBean getSource( String sourceName ) {
        String sourceListPath = null;
        String highlightmentPath = null;
        String tokenPath = null;

        if ( HVNetworkBuilder.NODE_SOURCES.contains( sourceName ) ) {
            sourceListPath = "highlightment.source.node.allowed";
            highlightmentPath = "highlightment.source.node.";
            tokenPath = "highlightment.source.node.token";
        }
        if ( HVNetworkBuilder.EDGE_SOURCES.contains( sourceName ) ) {
            sourceListPath = "highlightment.source.edge.allowed";
            highlightmentPath = "highlightment.source.edge.";
            tokenPath = "highlightment.source.edge.token";
        }

        // read the Highlighting.proterties file
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null != properties ) {

            String sourceList = properties.getProperty( sourceListPath );

            if ( ( null == sourceList ) || ( sourceList.length() < 1 ) ) {
                logger.warn( "Unable to find the property: " + sourceListPath + " (" +
                             StrutsConstants.HIGHLIGHTING_PROPERTY_FILE + ")" );
                return null;
            }

            // parse source list
            String token = properties.getProperty( tokenPath );

            if ( ( null == token ) || ( token.length() < 1 ) ) {
                logger.warn( "Unable to find the property: " + tokenPath + " (" +
                             StrutsConstants.HIGHLIGHTING_PROPERTY_FILE + ")" );
                return null;
            }

            StringTokenizer st = new StringTokenizer( sourceList, token );

            while ( st.hasMoreTokens() ) {
                String sourceKey = st.nextToken();
                if ( sourceKey.equals( sourceName ) ) {
                    String propName = highlightmentPath + sourceKey + ".label";
                    String label = properties.getProperty( propName );

                    if ( ( null == label ) || ( label.length() < 1 ) ) {
                        logger.warn( "Unable to find the property: " + propName + " (" +
                                     StrutsConstants.HIGHLIGHTING_PROPERTY_FILE + ")" );
                        return null;
                    }

                    return new LabelValueBean( label, sourceKey, "" );
                }
            }
        } else {
            logger.warn( "Unable to load the properties file: " + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE );
        }
        return null;
    } // getSource


    /**
     * Create a collection of LabelValueBean object specific of an highlightment method
     * from a properties file
     *
     * @param anHighlightmentMethod
     * @return a collection of LabelValueBean object specific of an highlightment method
     */
    public static List<LabelValueBean> getAuthorizedBehaviour( String anHighlightmentMethod ) {

        List<LabelValueBean> behaviours = new ArrayList<LabelValueBean>();

        // read the Highlighting.proterties file
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null != properties ) {

            String behaviourList = properties.getProperty( "highlightment.behaviour." + anHighlightmentMethod + ".allowed" );

            if ( ( null == behaviourList ) || ( behaviourList.length() < 1 ) ) {
                logger.info( "No behaviour defined for the source called:" + anHighlightmentMethod );

                // As there are no specified allowed list of behaviour for this method,
                // we try to load the global definition of defined behaviour.
                behaviourList = properties.getProperty( "highlightment.behaviour.existing" );

                if ( ( null == behaviourList ) || ( behaviourList.length() < 1 ) ) {
                    logger.warn( "Unable to find the property: highlightment.behaviour.existing (" +
                                 StrutsConstants.HIGHLIGHTING_PROPERTY_FILE + ")" );
                    return null;
                }
            }

            // parse behaviour list
            String token = properties.getProperty( "highlightment.behaviour.token" );

            if ( ( null == token ) || ( token.length() < 1 ) ) {
                logger.warn( "Unable to find the property: highlightment.behaviour.token (" +
                             StrutsConstants.HIGHLIGHTING_PROPERTY_FILE + ")" );
                return null;
            }

            StringTokenizer st = new StringTokenizer( behaviourList, token );

            while ( st.hasMoreTokens() ) {
                String sourceKey = st.nextToken();
                String labelProp = "highlightment.behaviour." + sourceKey + ".label";
                String classProp = "highlightment.behaviour." + sourceKey + ".class";
                String label = properties.getProperty( labelProp );
                String value = properties.getProperty( classProp );

                if ( ( null == label ) || ( label.length() < 1 ) || ( null == value ) || ( value.length() < 1 ) ) {
                    logger.warn( "Unable to find either properties:  (" + labelProp + ", " + classProp + " (" +
                                 StrutsConstants.HIGHLIGHTING_PROPERTY_FILE + ")" );
                    continue; // don't add this element
                }

                behaviours.add( new LabelValueBean( label, value, "" ) );
            } // while

        } // if

        return behaviours;

    } // getAuthorizedBehaviour
}
