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

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.io.BinaryInteractionsExporter;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;


/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExportServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog( ExportServlet.class );

    public static final String PARAM_SORT = "sort";
    public static final String PARAM_SORT_ASC = "asc";
    public static final String PARAM_QUERY = "query";
    public static final String PARAM_FORMAT = "format";

    private WebApplicationContext applicationContext;

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

        if ( log.isDebugEnabled() ) {
            log.debug( "Export query prior to decoding: " + searchQuery );
            if( searchQuery != null ) {
                searchQuery = URLDecoder.decode( searchQuery, "UTF-8" );
                log.debug( "Export query after decoding: " + searchQuery );
            }
        }

        String format = request.getParameter(PARAM_FORMAT);

        String sortColumn = request.getParameter(PARAM_SORT);
        String sortAsc = request.getParameter(PARAM_SORT_ASC);

        BinaryInteractionsExporter exporter = new BinaryInteractionsExporter(solrServer, sortColumn, (Boolean.parseBoolean(sortAsc))? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
        exporter.searchAndExport(response.getOutputStream(), searchQuery, format);
    }
}