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


    @Override
    public void init( ServletConfig config ) throws ServletException {
        super.init( config );

        ServletContext context = getServletContext();
        applicationContext = WebApplicationContextUtils.getWebApplicationContext( context );

    }

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        IntactViewConfiguration intactViewConfiguration = ( IntactViewConfiguration ) applicationContext.getBean( "intactViewConfiguration" );

        String urlToXmlFile = "http://www.ebi.ac.uk/~skerrien/cytoscape-webstart/data/10220385.xml";

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
                if(text.matches("<argument>\\{export-file-url\\}</argument>")){
                    text = "<argument>"+urlToXmlFile+"</argument>";
                }
				writer.println(text);
			}
            writer.close();
		}


        
    }
}
