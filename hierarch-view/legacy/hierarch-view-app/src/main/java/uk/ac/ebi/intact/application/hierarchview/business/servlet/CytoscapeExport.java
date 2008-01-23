/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.hierarchview.business.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.context.IntactContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;


/**
 * Writes and opens a JNLP-file.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class CytoscapeExport extends HttpServlet {

    private static final Log logger = LogFactory.getLog( CytoscapeExport.class );

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {

        try {
            IntactUserI user = ( IntactUserI ) IntactContext.getCurrentInstance().getSession().getAttribute( Constants.USER_KEY );

            response.setContentType( "application/x-java-jnlp-file" );

            if ( user != null ) {

                String url25 = user.getExportUrl();
                if ( url25 == null ) {
                    logger.warn( "PSI-MI XML 2.5 URL is null" );
                }

                // Path or URL to the Cytoscape Folder
//            String codebase = "http://www.ebi.ac.uk/~nneuhaus/Cytoscape_v2.5.1";
                String codebase = "file:///C:/Program Files/Cytoscape_v2.5.1";

                //Overwrite the current
//            String cytoscapeUrl = "H:/public_html/Cytoscape_v2.5.1/cy1.jnlp";
                String cytoscapeUrl = "C:/Program Files/Cytoscape_v2.5.1/cy1.jnlp";

                PrintWriter out = response.getWriter();

                String path = CytoscapeExport.class.getResource( StrutsConstants.CYTOSCAPE_WEBSTART_FILE ).getFile();
                File defaultJnlpFile = new File( URLDecoder.decode( path, "utf-8" ) );
                File localJnlpFile = new File( URLDecoder.decode( cytoscapeUrl, "utf-8" ) );
                if ( !localJnlpFile.canWrite() )
                    logger.error( "Could not write to " + localJnlpFile.getAbsolutePath() );

                BufferedWriter writer = new BufferedWriter( new FileWriter( localJnlpFile ) );
                BufferedReader reader = new BufferedReader( new FileReader( defaultJnlpFile ) );

                try {
                    String line = "";

                    while ( ( line = reader.readLine() ) != null ) {
                        if ( line.contains( "default-codebase" ) ) {
                            line = line.replace( "default-codebase", codebase );
                        }

                        if ( line.contains( "default-data" ) ) {
                            line = line.replace( "default-data", url25 );
                        }

                        writer.write( line + "\n" );
                        out.print( line );
                    }
                } finally {
                    reader.close();
                    writer.close();
                }

            }
        } catch ( IllegalStateException e ) {

        }

    }
}
