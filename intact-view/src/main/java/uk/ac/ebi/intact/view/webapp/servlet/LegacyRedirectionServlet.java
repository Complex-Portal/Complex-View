/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet that does take care of redirecting old URLs to appropriate locations in this web site.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class LegacyRedirectionServlet extends HttpServlet {
    private static final String QUERY = "query=";
    private static final String BINARY = "binary=";
    private static final String SEARCH_STRING = "searchString=";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //ServletContext context = getServletContext();
        //WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);

        String absContextPath = request.getScheme() + "://" +
               request.getServerName() + ":" +
               request.getServerPort() +
               request.getContextPath();

        String url = request.getRequestURI();
        String queryStr = request.getQueryString();

        if (queryStr == null) {
            queryStr = "";
        }

        if (url.contains("/site")) {

            if (!absContextPath.contains("/site")) {
                response.sendRedirect(absContextPath);
            }

        } else if (url.contains("/binary-search")) {

            if (queryStr.contains( QUERY )) {
                String query = queryStr.substring(url.indexOf( QUERY )+QUERY.length());

                response.sendRedirect(fixUrl(absContextPath+"/pages/interactions/interactions.xhtml?query="+adaptQuery(query)));
            } else {
               response.sendRedirect(fixUrl(absContextPath));
            }

        } else if (url.contains("/search")) {
            // possibilities:
            // http://www.ebi.ac.uk/intact/search/do/search?binary=EBI-2323272,EBI-1046727
            // http://www.ebi.ac.uk/intact/search/do/search?filter=ac&searchString=EBI-1638729
            // http://www.ebi.ac.uk/intact/search/do/search?searchString=EBI-1379264&filter=ac&searchClass=Experiment&filter=ac

            if (queryStr.contains( BINARY )) {

                String interactors = queryStr.substring( url.indexOf( BINARY ) + BINARY.length() );
                response.sendRedirect(fixUrl(absContextPath+"/pages/details/details.xhtml?binary="+interactors));

            } else if (queryStr.contains( SEARCH_STRING )) {

                String query = queryStr.substring( queryStr.indexOf( SEARCH_STRING ) + SEARCH_STRING.length() );

                if( queryStr.contains("searchClass=Experiment") && queryStr.contains("filter=ac") ) {

                    // we cannot guaranty the order of the parameters in this string, so identify first which param
                    // comes after searchString before extracting the experimentAc.

                    int searchStrIdx = queryStr.indexOf( SEARCH_STRING );
                    int endIdx = Math.max( queryStr.indexOf( "&", searchStrIdx ), queryStr.indexOf( "&amp;", searchStrIdx ) );
                    if( endIdx == -1 ) {
                        // no param following
                        endIdx = queryStr.length();
                    }

                    String experimentAc = queryStr.substring( searchStrIdx + SEARCH_STRING.length(), endIdx );
                    response.sendRedirect(fixUrl(absContextPath+"/pages/details/details.xhtml?experimentAc="+experimentAc));
                } else {
                    response.sendRedirect(fixUrl(absContextPath+"/pages/interactions/interactions.xhtml?query="+adaptQuery(query)));
                }
            } else {

                response.sendRedirect(fixUrl(absContextPath));
            }
        }
    }

    private String fixUrl(String url) {
        url = url.replaceAll("==", "=");
        return url;
    }

    private String adaptQuery( String query ) {
        query = query.replaceAll("identifiers\\:", "id:"); // identifiers field does not exist anymore
        return query;
    }
}
