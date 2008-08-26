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

import org.apache.lucene.search.Sort;
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.search.Searcher;
import psidev.psi.mi.search.engine.SearchEngineException;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.converter.tab2xml.Tab2Xml;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabWriter;
import uk.ac.ebi.intact.psimitab.IntactTab2Xml;
import uk.ac.ebi.intact.psimitab.search.IntactSearchEngine;
import uk.ac.ebi.intact.view.webapp.IntactViewException;
import uk.ac.ebi.intact.view.webapp.io.BinaryInteractionsExporter;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;
import uk.ac.ebi.intact.view.webapp.controller.application.AppConfigBean;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExportServlet extends HttpServlet {

    public static final String PARAM_SORT = "sort";
    public static final String PARAM_SORT_ASC = "asc";
    public static final String PARAM_QUERY = "query";
    public static final String PARAM_FORMAT = "format";

    private String defaultIndex;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String configFile = config.getServletContext().getInitParameter(AppConfigBean.DEFAULT_CONFIG_FILE_INIT_PARAM);

        SearchConfig searchConfig = WebappUtils.readConfiguration(configFile);
        this.defaultIndex = WebappUtils.getDefaultIndex(searchConfig).getLocation();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String searchQuery = request.getParameter(PARAM_QUERY);
        String format = request.getParameter(PARAM_FORMAT);

        String sortColumn = null;
        String sortAsc = null;

        String sortColumn = request.getParameter(PARAM_SORT);
        String sortAsc = request.getParameter(PARAM_SORT_ASC);

        BinaryInteractionsExporter exporter = new BinaryInteractionsExporter(defaultIndex, sortColumn, Boolean.parseBoolean(sortAsc));
        exporter.searchAndExport(response.getOutputStream(), searchQuery, format);
    }


}