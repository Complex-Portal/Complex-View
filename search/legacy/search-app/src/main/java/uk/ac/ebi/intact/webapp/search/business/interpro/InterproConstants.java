/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.webapp.search.business.interpro;

/**
 * Constants specific to the Interpro search.
 *
 * @author ckohler (ckohler@ebi.ac.uk)
 * @version $Id$
 * @since <pre>19-Jan-2006</pre>
 */
public interface InterproConstants {

    /**
     * The URL used to forward to interpro.
     * Format: http://www.ebi.ac.uk/interpro/ISpy?ac=P12345C
     */
    public static final String INTERPRO_URL = "http://www.ebi.ac.uk/interpro/ISpy?ac=";

    /**
     * String separator between 2 protein ID in the interpro URL.
     */
    public static final String PROTEIN_ID_SEPARATOR = "%2C";

    /**
     * The label of the Continue button.
     */
    public static final String CONTINUE_BUTTON_LABEL = "Continue";

    /**
     * The label of the 'new search' button.
     */
    public static final String NEW_SEARCH_BUTTON_LABEL = "New search";
}