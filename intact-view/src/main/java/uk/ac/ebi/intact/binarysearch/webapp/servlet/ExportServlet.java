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
package uk.ac.ebi.intact.binarysearch.webapp.servlet;

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
import uk.ac.ebi.intact.binarysearch.webapp.SearchWebappException;
import uk.ac.ebi.intact.binarysearch.webapp.application.AppConfigBean;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.binarysearch.webapp.util.WebappUtils;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntActColumnHandler;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabWriter;
import uk.ac.ebi.intact.psimitab.search.IntActSearchEngine;

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

        if (format == null) {
            throw new ServletException("Parameter 'format' is missing in the URL");
        }

        if ("mitab".equals(format)) {
            exportToMiTab(searchQuery, request, response);
        } else if ("mitab_intact".equals(format)) {
            exportToMiTabIntact(searchQuery, request, response);
        } else if ("xml".equals(format)) {
            exportToMiXml(searchQuery, request, response);
        } else {
            throw new ServletException("Format is not correct: " + format + ". Possible values: mitab, mitab_intact.");
        }
    }

    private void exportToMiTab(String searchQuery, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Writer out = response.getWriter();

        String sortColumn = request.getParameter(PARAM_SORT);
        String asc = request.getParameter(PARAM_SORT_ASC);
        String indexDir = defaultIndex;

        List interactions;
        Integer firstResult = 0;
        Integer maxResults = 50;

        boolean headerEnabled = true;

        do {

            Sort sort = null;

            if (sortColumn != null && sortColumn.length() > 0) {
                sort = new Sort(sortColumn, !Boolean.parseBoolean(asc));
            }

            SearchResult result = Searcher.search(searchQuery, indexDir, firstResult, maxResults, sort);
            interactions = result.getInteractions();

            PsimiTabWriter writer = new PsimiTabWriter();
            writer.setHeaderEnabled(headerEnabled);
            try {
                writer.write(interactions, out);
            } catch (ConverterException e) {
                throw new ServletException("Problem exporting interactions", e);
            }

            headerEnabled = false;

            firstResult = firstResult + maxResults;

        } while (!interactions.isEmpty());
    }

    private void exportToMiTabIntact(String searchQuery, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Writer out = response.getWriter();

        String sortColumn = request.getParameter(PARAM_SORT);
        String asc = request.getParameter(PARAM_SORT_ASC);
        String indexDir = defaultIndex;

        List interactions;
        Integer firstResult = 0;
        Integer maxResults = 50;

        boolean headerEnabled = true;

        do {

            Sort sort = null;

            if (sortColumn != null && sortColumn.length() > 0) {
                sort = new Sort(sortColumn, !Boolean.parseBoolean(asc));
            }

            IntActSearchEngine engine;
            try {
                engine = new IntActSearchEngine(indexDir);
            }
            catch (IOException e) {
                throw new SearchEngineException(e);
            }

            SearchResult<IntActBinaryInteraction> result = engine.search(searchQuery, firstResult, maxResults, sort);
            interactions = result.getInteractions();

            PsimiTabWriter writer = new IntactPsimiTabWriter();
            writer.setHeaderEnabled(headerEnabled);
            try {
                writer.write(interactions, out);
            } catch (ConverterException e) {
                throw new ServletException("Problem exporting interactions", e);
            }

            headerEnabled = false;

            firstResult = firstResult + maxResults;

        } while (!interactions.isEmpty());
    }

    private void exportToMiXml(String searchQuery, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Writer out = response.getWriter();

        IntActSearchEngine engine;
            try {
                engine = new IntActSearchEngine(defaultIndex);
            }
            catch (IOException e) {
                throw new SearchEngineException(e);
            }

        // count first as a security measure
        SearchResult<IntActBinaryInteraction> result1 = engine.search(searchQuery, 0, 1);
        if (result1.getTotalCount() > 1000) {
            throw new SearchWebappException("Too many interactions to export to XML. Maximum is 1000");
        }

        SearchResult<IntActBinaryInteraction> result = engine.search(searchQuery, null, null);
        Collection<IntActBinaryInteraction> interactions = result.getInteractions();

        Tab2Xml tab2Xml = new Tab2Xml();
        tab2Xml.setColumnHandler(new IntActColumnHandler());
        tab2Xml.setBinaryInteractionClass(IntActBinaryInteraction.class);

        final EntrySet entrySet;
        try {
            entrySet = tab2Xml.convert(new ArrayList<BinaryInteraction>(interactions));
        } catch (Exception e) {
            throw new SearchWebappException("Problem converting interactions from MITAB to XML", e);
        }

        PsimiXmlWriter writer = new PsimiXmlWriter();
        try {
            writer.write(entrySet, out);
        } catch (Exception e) {
            throw new SearchWebappException("Problem writing XML", e);
        }
    }
}