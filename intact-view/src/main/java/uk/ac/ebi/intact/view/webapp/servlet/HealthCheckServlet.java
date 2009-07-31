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

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class HealthCheckServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletContext context = getServletContext();
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);

        IntactViewConfiguration config = (IntactViewConfiguration) applicationContext.getBean("intactViewConfiguration");


        resp.setContentType("text/plain");
        resp.getWriter().write("Application: OK\n");

        // db check
        final IntactContext intactContext = (IntactContext) applicationContext.getBean("intactContext");
        final DaoFactory daoFactory = intactContext.getDataContext().getDaoFactory();
        final int count = daoFactory.getInstitutionDao().countAll();

        boolean dbOk = (count > 0);
        resp.getWriter().write("Database: "+(dbOk? "OK" : "FAILED"));
        resp.getWriter().write("\n");

        // index check
        final SolrPingResponse solrPingResponse;
        try {
            solrPingResponse = config.getInteractionSolrServer().ping();
        } catch (SolrServerException e) {
            throw new ServletException(e);
        }

        boolean solrOk = (solrPingResponse.getStatus() == 0);
        resp.getWriter().write("SOLR Index: "+(solrOk? "OK" : "FAILED ("+solrPingResponse.getStatus()+")"));
    }
}
