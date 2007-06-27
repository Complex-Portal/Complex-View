/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.predict.struts.framework;

/**
 * Defines constants required for the Predict application.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public interface PredictConstants {

    // Session keys

    /**
     * The key to access a user session object.
     */
    public static final String USER = "user";

    /**
     * The key for the prediction results.
     */
    public static final String PREDICTION = "prediction";

    /**
     * The key to access the service handler.
     */
    public static final String SERVICE = "service";
}
