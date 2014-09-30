/*
 * Copyright 2001-2008 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;
import uk.ac.ebi.intact.view.webapp.io.MoleculeListExporter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;


/**
 */
public class ListServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog( ListServlet.class );

    public static final String PARAM_SORT = "sort";
    public static final String PARAM_SORT_ASC = "asc";
    public static final String PARAM_QUERY = "query";
    public static final String PARAM_FORMAT = "format";
    public static final String PARAM_FILTER_NEGATIVE = "negative";
    public static final String PARAM_FILTER_SPOKE = "spoke";
    public static final String PARAM_ONTOLOGY_QUERY = "ontology";

    private WebApplicationContext applicationContext;
    private MoleculeListExporter exporter;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ServletContext context = getServletContext();
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        IntactViewConfiguration intactViewConfiguration = (IntactViewConfiguration) applicationContext.getBean("intactViewConfiguration");
        SolrServer solrServer = intactViewConfiguration.getInteractionSolrServer();

        String searchQuery = request.getParameter(PARAM_QUERY);
        String negative = request.getParameter(PARAM_FILTER_NEGATIVE);
        String spoke = request.getParameter(PARAM_FILTER_SPOKE);

        boolean includeNegative = negative != null ? Boolean.parseBoolean(negative) : false;
        boolean filterSpoke = spoke != null ? Boolean.parseBoolean(spoke) : false;

        if( searchQuery != null ) {
            searchQuery = URLDecoder.decode( searchQuery, "UTF-8" );
        } else {
            searchQuery = (String) request.getSession().getAttribute(UserQuery.SESSION_SOLR_QUERY_KEY);
            searchQuery = URLDecoder.decode( searchQuery, "UTF-8" );
        }

        if (searchQuery == null || searchQuery.length() == 0) {
            searchQuery = URLDecoder.decode( UserQuery.STAR_QUERY, "UTF-8" );
        }

        if (exporter == null){
            exporter = new MoleculeListExporter(solrServer);
        }

        response.setContentType("text/plain");

        try {
            exporter.writeMoleculeAcs(response.getOutputStream(), searchQuery, filterSpoke, includeNegative);
        } catch (IOException e) {
            log.error("Cannot export list of molecules", e);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));

            try{
                writer.write("Impossible to export the list of molecules, an internal error occurred.");
            }
            finally {
                writer.close();
            }

        }
    }
}