/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.commons.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility methods to handle url paths
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28-Mar-2006</pre>
 */
@Deprecated
public class UrlUtil
{
    /**
     * Gets the absolute path stripping off the context name from the URL
     * @param request The request in use
     * @return The absolute path without the context part
     */
    public static String absolutePathWithoutContext(HttpServletRequest request)
    {
        String ctxtPath = request.getContextPath();
        String absolutePathWithoutContext = ctxtPath.substring(0, ctxtPath.lastIndexOf( "/" )+1);

        return absolutePathWithoutContext;
    }
}
