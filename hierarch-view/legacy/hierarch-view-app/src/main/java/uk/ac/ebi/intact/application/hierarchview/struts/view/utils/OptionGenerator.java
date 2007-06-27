/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.struts.view.utils;

import org.apache.log4j.Logger;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;

import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * Allows to create some collection to populate option list in HTML form.
 * 
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */

public class OptionGenerator {

    private static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    /**
     * create a collection of LabelValueBean object from a properties file
     *
     * @return a collection of LabelValueBean object
     */
    public static ArrayList getHighlightmentSources () {

        ArrayList sources = new ArrayList ();

        // read the Highlighting.proterties file
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if (null != properties) {

            String sourceList = properties.getProperty ("highlightment.source.allowed");

            if ((null == sourceList) || (sourceList.length() < 1)) {
                logger.warn ("Unable to find the property: highlightment.source.allowed (" +
                             StrutsConstants.HIGHLIGHTING_PROPERTY_FILE +")");
                return null;
            }

            // parse source list
            String token = properties.getProperty ("highlightment.source.token");

            if ((null == token) || (token.length() < 1)) {
                logger.warn ("Unable to find the property: highlightment.source.token (" +
                             StrutsConstants.HIGHLIGHTING_PROPERTY_FILE +")");
                return null;
            }

            StringTokenizer st = new StringTokenizer (sourceList, token);

            while (st.hasMoreTokens()) {
                String sourceKey = st.nextToken();
                String propName = "highlightment.source." + sourceKey + ".label";
                String label = properties.getProperty (propName);

                if ((null == label) || (label.length() < 1)) {
                    logger.warn ("Unable to find the property: "+ propName +" ("+
                                 StrutsConstants.HIGHLIGHTING_PROPERTY_FILE +")");
                    continue;
                }

                sources.add (new LabelValueBean(label, sourceKey, ""));
            } // while
        } else {
            logger.warn("Unable to load the properties file: " + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE);
        }

        return sources;
    } // getHighlightmentSources


    /**
     * create a LabelValueBean object corresponding to the default source if it exists
     *
     * @return a LabelValueBean object or null is no default.
     */
    public static LabelValueBean getDefaultSource () {

        // read the Highlighting.proterties file
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if (null != properties) {

            String sourceList = properties.getProperty ("highlightment.source.allowed");

            if ((null == sourceList) || (sourceList.length() < 1)) {
                logger.warn ("Unable to find the property: highlightment.source.allowed (" +
                             StrutsConstants.HIGHLIGHTING_PROPERTY_FILE +")");
                return null;
            }

            // parse source list
            String token = properties.getProperty ("highlightment.source.token");

            if ((null == token) || (token.length() < 1)) {
                logger.warn ("Unable to find the property: highlightment.source.token (" +
                             StrutsConstants.HIGHLIGHTING_PROPERTY_FILE +")");
                return null;
            }

            StringTokenizer st = new StringTokenizer (sourceList, token);

            if (st.hasMoreTokens()) {
                String sourceKey = st.nextToken();
                String propName = "highlightment.source." + sourceKey + ".label";
                String label = properties.getProperty (propName);

                if ((null == label) || (label.length() < 1)) {
                    logger.warn ("Unable to find the property: "+ propName +" ("+
                                 StrutsConstants.HIGHLIGHTING_PROPERTY_FILE +")");
                }

                return new LabelValueBean (label, sourceKey, "");
            }
        } else {
            logger.warn("Unable to load the properties file: " + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE);
        }

        return null;
    } // getDefaultSource

    /**
     * create a LabelValueBean object corresponding to the default source if it exists
     *
     * @return a LabelValueBean object or null if it doesn't exists.
     */
    public static LabelValueBean getSource (String sourceName) {

        // read the Highlighting.proterties file
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;;

        if (null != properties) {

            String sourceList = properties.getProperty ("highlightment.source.allowed");

            if ((null == sourceList) || (sourceList.length() < 1)) {
                logger.warn ("Unable to find the property: highlightment.source.allowed (" +
                             StrutsConstants.HIGHLIGHTING_PROPERTY_FILE +")");
                return null;
            }

            // parse source list
            String token = properties.getProperty ("highlightment.source.token");

            if ((null == token) || (token.length() < 1)) {
                logger.warn ("Unable to find the property: highlightment.source.token (" +
                             StrutsConstants.HIGHLIGHTING_PROPERTY_FILE +")");
                return null;
            }

            StringTokenizer st = new StringTokenizer (sourceList, token);

            while (st.hasMoreTokens()) {
                String sourceKey = st.nextToken();
                if (sourceKey.equals(sourceName)) {
                    String propName = "highlightment.source." + sourceKey + ".label";
                    String label = properties.getProperty (propName);

                    if ((null == label) || (label.length() < 1)) {
                        logger.warn ("Unable to find the property: "+ propName +" ("+
                                StrutsConstants.HIGHLIGHTING_PROPERTY_FILE +")");
                        return null;
                    }

                    return new LabelValueBean (label, sourceKey, "");
                }
            }
        } else {
            logger.warn("Unable to load the properties file: " + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE);
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
    public static ArrayList getAuthorizedBehaviour (String anHighlightmentMethod) {

        ArrayList behaviours = new ArrayList ();

        // read the Highlighting.proterties file
        Properties properties = IntactUserI.HIGHLIGHTING_PROPERTIES;;

        if (null != properties) {

            String behaviourList = properties.getProperty ("highlightment.behaviour." + anHighlightmentMethod + ".allowed");

            if ((null == behaviourList) || (behaviourList.length() < 1)) {
                logger.info ("No behaviour defined for the source called:" + anHighlightmentMethod);

                // As there are no specified allowed list of behaviour for this method,
                // we try to load the global definition of defined behaviour.
                behaviourList = properties.getProperty ("highlightment.behaviour.existing");

                if ((null == behaviourList) || (behaviourList.length() < 1)) {
                    logger.warn ("Unable to find the property: highlightment.behaviour.existing ("+
                                  StrutsConstants.HIGHLIGHTING_PROPERTY_FILE +")");
                    return null;
                }
            }

            // parse behaviour list
            String token = properties.getProperty ("highlightment.behaviour.token");

            if ((null == token) || (token.length() < 1)) {
                logger.warn ("Unable to find the property: highlightment.behaviour.token ("+
                             StrutsConstants.HIGHLIGHTING_PROPERTY_FILE +")");
                return null;
            }

            StringTokenizer st = new StringTokenizer (behaviourList, token);

            while (st.hasMoreTokens()) {
                String sourceKey = st.nextToken();
                String labelProp = "highlightment.behaviour." + sourceKey + ".label";
                String classProp = "highlightment.behaviour." + sourceKey + ".class";
                String label = properties.getProperty ( labelProp );
                String value = properties.getProperty ( classProp );

                if ((null == label) || (label.length() < 1) || (null == value) || (value.length() < 1)) {
                    logger.warn ("Unable to find either properties:  ("+ labelProp + ", " + classProp + " ("+
                                 StrutsConstants.HIGHLIGHTING_PROPERTY_FILE +")");
                    continue; // don't add this element
                }

                behaviours.add (new LabelValueBean(label, value, ""));
            } // while

        } // if

        return behaviours;

    } // getAuthorizedBehaviour
}
