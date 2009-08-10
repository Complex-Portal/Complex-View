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
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class LegacyRedirectionServlet extends HttpServlet {

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


        if (url.contains("/site/")) {

            if (!absContextPath.contains("/site/")) {
                response.sendRedirect(absContextPath);
            }

        } else if (url.contains("/binary-search/")) {

            if (queryStr.contains("query=")) {
                String query = queryStr.substring(url.indexOf("query=")+7);

                response.sendRedirect(absContextPath+"/pages/interactions/interactions.xhtml?query="+query);
            }
        } else if (url.contains("/search/")) {
            // http://www.ebi.ac.uk/intact/search/do/search?binary=EBI-2323272,EBI-1046727
            if (queryStr.contains("binary=")) {
                String interactors = queryStr.substring(url.indexOf("binary=")+8);

                response.sendRedirect(absContextPath+"/pages/details/details.xhtml?binary="+interactors);
            } else if (queryStr.contains("searchString=")) {
                String query = queryStr.substring(url.indexOf("searchString="+14));

                response.sendRedirect(absContextPath+"/pages/interactions/interactions.xhtml?query="+query);
            }
        }
    }
}
