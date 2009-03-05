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

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import java.io.*;
import java.text.MessageFormat;
import java.net.URLEncoder;

import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;

/**
 * TODO comment that class header
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 */
public class CytoscapeServlet extends HttpServlet {

    private WebApplicationContext applicationContext;

    public static final String PARAM_QUERY = "query";
    public static final String PARAM_FORMAT = "format";


    @Override
    public void init( ServletConfig config ) throws ServletException {
        super.init( config );

        ServletContext context = getServletContext();
        applicationContext = WebApplicationContextUtils.getWebApplicationContext( context );

    }

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {

        String searchQuery = request.getParameter(PARAM_QUERY);
        searchQuery = encodeURL( searchQuery );
        String format = request.getParameter(PARAM_FORMAT);

        //final String requestURI = "http://"+request.getRemoteHost()+":"+request.getRemotePort()+request.getContextPath();
        String urlToXmlExportServlet = "http://localhost:9092/intact/view/export?query="+searchQuery+"&format="+format;

        response.setContentType("application/x-java-jnlp-file");

        // Read the cytoscape.jnlp from from WEB-INF directory.
        String filename = "/WEB-INF/cytoscape/cytoscape.jnlp";
        ServletContext context = getServletContext();
        InputStream is = context.getResourceAsStream(filename);
        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            PrintWriter writer = response.getWriter();
            String text = "";

            while ((text = reader.readLine()) != null) {
                if(text.contains("filePath")){
                    text = text.replace( "filePath",urlToXmlExportServlet);
                }
                System.out.println( text );
                writer.println(text);
            }
            writer.close();
		}

        
    }

    private static String encodeURL(String stringToBeEncoded) throws UnsupportedEncodingException {
           String encodedString="";
           if(stringToBeEncoded!=null){
           encodedString = URLEncoder.encode(stringToBeEncoded,"UTF-8");
           }
           return encodedString;
       }

}
