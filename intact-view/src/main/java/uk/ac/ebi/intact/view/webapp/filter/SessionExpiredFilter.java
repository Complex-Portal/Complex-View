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
package uk.ac.ebi.intact.view.webapp.filter;

import javax.faces.application.ResourceHandler;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SessionExpiredFilter implements Filter {

    private String timeoutPage = "home.xhtml";

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest request,
                         ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        if ((request instanceof HttpServletRequest)
                && (response instanceof HttpServletResponse)) {

            HttpServletRequest httpServletRequest = (HttpServletRequest) request;

            HttpServletResponse httpServletResponse = (HttpServletResponse) response;

            // is session expire control required for this request?

            if (isSessionControlRequiredForThisResource(httpServletRequest)) {
                if (!httpServletRequest.getRequestURI().startsWith(httpServletRequest.getContextPath() + ResourceHandler.RESOURCE_IDENTIFIER)) { // Skip JSF resources (CSS/JS/Images/etc)
                    httpServletResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
                    httpServletResponse.setHeader("Pragma", "no-cache"); // HTTP 1.0.
                    httpServletResponse.setDateHeader("Expires", 0); // Proxies.
                }

                // is session invalid?

                if (isSessionInvalid(httpServletRequest)) {

                    String timeoutUrl = httpServletRequest.getContextPath()
                            + "/" + getTimeoutPage()+"?status=exp";

                    // TODO handle session expiration in partial request
//                    final boolean isPartialRequest = "true".equals(httpServletRequest.getHeader("Tr-XHR-Message"));
//
//                    if (isPartialRequest) {
//                        final PrintWriter writer = httpServletResponse.getWriter();
//                        final XmlResponseWriter rw = new XmlResponseWriter(writer, "UTF-8");
//
//                        rw.startDocument();
//                        rw.write("<?Tr-XHR-Response-Type ?>\n");
//                        rw.startElement("redirect", null);
//                        rw.writeText(timeoutUrl, null);
//                        rw.endElement("redirect");
//                        rw.endDocument();
//                        rw.close();
//
//                        return;
//
//                    }
                }

            }

        }

        if (filterChain != null) {
            filterChain.doFilter(request, response);
        }

    }

    private boolean isSessionControlRequiredForThisResource(HttpServletRequest httpServletRequest) {
        String requestPath = httpServletRequest.getRequestURI();

        return !requestPath.contains(getTimeoutPage());

    }

    private boolean isSessionInvalid(HttpServletRequest httpServletRequest) {

        boolean sessionInValid = (httpServletRequest.getRequestedSessionId() != null)

                && !httpServletRequest.isRequestedSessionIdValid();

        return sessionInValid;

    }

    public void destroy() {

    }

    public String getTimeoutPage() {

        return timeoutPage;

    }

}