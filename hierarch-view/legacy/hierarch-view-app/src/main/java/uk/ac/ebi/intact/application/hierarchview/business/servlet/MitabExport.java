/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUser;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.Constants;
import uk.ac.ebi.intact.application.hierarchview.business.graph.Network;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * Displays BinaryInteractions in a new Window.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-Snapshot
 */
public class MitabExport extends HttpServlet {

    private static final Log logger = LogFactory.getLog( MitabExport.class );

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {

        response.setContentType( "text/plain" );

        IntactUserI user = (IntactUserI) request.getSession().getAttribute(Constants.USER_KEY);

        if ( user != null ) {
            Network network = user.getInteractionNetwork();
            if ( network != null ) {
                Collection<BinaryInteraction> interactions = network.getBinaryInteraction();

                PrintWriter out = response.getWriter();
                PsimiTabWriter writer = new IntactPsimiTabWriter();
                
                try {
                    writer.write( interactions, out );
                } catch ( Exception e ) {
                    throw new ServletException( e );
                }
            }
        }


    }
}
