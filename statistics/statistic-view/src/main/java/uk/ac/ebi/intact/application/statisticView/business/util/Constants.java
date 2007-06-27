/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.statisticView.business.util;


/**
 * Constants for the statisticView application.
 *
 * @author Michael Kleen  (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public interface Constants {

    /**
     * The name of the logger specific of statisticView
     * cf. config/log4j.properties
     */
    public static final String FORWARD_RESULT_PAGE = "sucess";

    public static final String FORWARD_ERROR_PAGE = "error";

    public static final String VIEWBEAN = "viewbean";

    public static final int MIN_DISPLAY_PROTEINS = 30;

    public static final int MIN_BINARY_INTERACTIONS = 30;

    public static final int MIN_DETECTION_METHODS = 50;


}
