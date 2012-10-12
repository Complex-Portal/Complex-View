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
package uk.ac.ebi.intact.view.webapp.servlet.cytoscape;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

/**
 * Servlet used to export a JNLP file in order to laod Cytoscape as a web start.
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 0.9
 */
public class CytoscapeServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog( CytoscapeServlet.class );

    public static final String DATA_URL = "intactDataURL";

    public static final String PARAM_QUERY = "query";
    public static final String PARAM_FORMAT = "format";
    public static final String PARAM_FILTER_NEGATIVE = "negative";
    public static final String PARAM_FILTER_SPOKE = "spoke";
    public static final String PARAM_ONTOLOGY_QUERY = "ontology";
    public static final String PARAM_SORT = "sort";
    public static final String PARAM_SORT_ASC = "asc";

    @Override
    public void init( ServletConfig config ) throws ServletException {
        super.init( config );
    }

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {

        String searchQuery = request.getParameter(PARAM_QUERY);
        String format = request.getParameter(PARAM_FORMAT);
        String negative = request.getParameter(PARAM_FILTER_NEGATIVE);
        String spoke = request.getParameter(PARAM_FILTER_SPOKE);
        String ontology = request.getParameter(PARAM_ONTOLOGY_QUERY);
        String sort = request.getParameter(PARAM_SORT);
        String asc = request.getParameter(PARAM_SORT_ASC);

        if (searchQuery == null) {
            throw new ServletException("Parameter '"+ PARAM_QUERY +"' was expected");
        }
        if ( log.isDebugEnabled() ) log.debug( "Generating a JNLP file to start Cytoscape with query: " + searchQuery );

        searchQuery = encodeURL( searchQuery );
        if ( log.isTraceEnabled() ) log.trace( "Encoded query: " + searchQuery );

        final String serverContext = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        final String exportUrl = serverContext + "/export?query=" + searchQuery + "&format=" + format+"&negative="+negative+"&spoke="+spoke+"&ontology="+ontology+"&sort="+sort+"&asc="+asc;;
        response.setContentType( "application/x-java-jnlp-file" );

        // Read the template cytoscape.jnlp from from WEB-INF directory.
        String filename = "/WEB-INF/cytoscape/cytoscape.jnlp";
        ServletContext context = getServletContext();
        InputStream is = context.getResourceAsStream( filename );

        PrintWriter writer = null;
        try {
            if ( is != null ) {
                BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
                writer = response.getWriter();
                String text = null;

                try{
                    if ( log.isTraceEnabled() ) log.trace( "Starting JNLP export ..." );

                    while ( ( text = reader.readLine() ) != null ) {
                        if ( text.contains( DATA_URL ) ) {
                            text = text.replace( DATA_URL, exportUrl );
                        }

                        if ( log.isTraceEnabled() ) log.trace( text );

                        writer.println( text );
                        writer.flush();
                    }
                    if ( log.isTraceEnabled() ) log.trace( "Completed JNLP export." );

                }
                finally {
                    reader.close();
                }
            }
        } finally {
            if ( writer != null ) {
                writer.close();
            }
            is.close();
        }
    }

    private static String encodeURL( String url ) throws UnsupportedEncodingException {
        if ( url == null ) {
            throw new IllegalArgumentException( "You must give a non null url" );
        }
        return URLEncoder.encode( url, "UTF-8" );
    }
}