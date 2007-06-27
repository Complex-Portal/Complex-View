/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.predict.business;

import uk.ac.ebi.intact.application.commons.util.UrlUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * This class provides the general services common to all the users.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class PredictService {

    /**
     * The search server URL.
     */
    private String mySearchUrl;

    /**
     * Returns the relative link to the search application.
     * @param request the request object to get the context path.
     * This is only used once when this method is called for the first time.
     * For subsequent calls, the cached value is returned.
     * @return the relative path to the search page.
     */
    public String getSearchURL(HttpServletRequest request) {
        if (mySearchUrl == null) {
            String relativePath = UrlUtil.absolutePathWithoutContext(request);
            // Hard coded link; this could move to a resource file as with the editor.
            // Since we have only a single property, we can get away by hard coding.
            mySearchUrl = relativePath.concat("search/do/hvWelcome");
        }
        return mySearchUrl;
    }
}
