/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.graph2MIF;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.graph2MIF.exception.MIFSerializeException;
import uk.ac.ebi.intact.application.graph2MIF.exception.NoGraphRetrievedException;
import uk.ac.ebi.intact.application.graph2MIF.exception.NoInteractorFoundException;
import uk.ac.ebi.intact.business.IntactException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;


/**
 * Graph2MIFServlet
 * <p/>
 * This is a Servlet for retrieving a MIF Document using a URL-Call directly.
 *
 * @author Henning Mersch <hmersch@ebi.ac.uk>
 * @version $Id$
 */

public class Graph2MIFWSServlet extends HttpServlet {


    private static final Log logger = LogFactory.getLog(Graph2MIFWSServlet.class);

    /**
     * doGet - the "main" method of a servlet.
     *
     * @param aRequest  The HttpRequest - should include ac,depth and strict as parameters
     * @param aResponse HttpServletResponse for giving ansewer
     */
    public void doGet( HttpServletRequest aRequest, HttpServletResponse aResponse )
            throws ServletException, IOException {
        //parsing parameters
        PrintWriter out = null;

        try {
            String ac = aRequest.getParameter( "ac" );
            if( ac == null ) {
                String msg = "You have to give an accession number in order to get an " +
                             "Interactor from the database";
                logger.error( msg );
                giveErrorMsg( msg, aResponse );

            } else {

                Boolean strictmif;
                String strictParam = aRequest.getParameter( "strict" );
                if( strictParam != null && strictParam.equalsIgnoreCase( "true" ) ) {
                    strictmif = Boolean.TRUE;
                } else {
                    strictmif = Boolean.FALSE;
                }

                Integer depth = new Integer( aRequest.getParameter( "depth" ) );

                String psiVersion = aRequest.getParameter("version");

                Graph2MIFWSService ws = new Graph2MIFWSService();
                String mif = null;
                try
                {
                    mif = ws.getMIF(ac, depth, strictmif, psiVersion);
                }
                catch (MIFSerializeException e)
                {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(baos);

                    e.printStackTrace();
                    e.printStackTrace(ps);

                    mif = baos.toString();

                    ps.close();
                    baos.close();
                }

                logger.info( "Set MIME type to: text/xml" );
                aResponse.setContentType( "text/xml" );

                logger.info( "Printing XML data on the output." );
                out = aResponse.getWriter();
                out.println( mif );
            }
        } catch ( NumberFormatException e ) {
            giveErrorMsg( "depth should be an integer", aResponse );
            e.printStackTrace();
        } catch ( IntactException e ) {
            giveErrorMsg( "ERROR: Search for interactor failed (" + e.toString() + ")", aResponse );
            e.printStackTrace();
        } catch ( NoInteractorFoundException e ) {
            giveErrorMsg( "ERROR: No Interactor found for this ac (" + e.toString() + ")", aResponse );
            e.printStackTrace();
        } catch ( NoGraphRetrievedException e ) {
            giveErrorMsg( "ERROR: Could not retrieve graph from interactor (" + e.toString() + ")", aResponse );
            e.printStackTrace();
        } catch ( NullPointerException e ) {
            giveErrorMsg( "ERROR: wrong parameters:\n usage is: <host>/graph2mif/getXML?ac=<ac>&amp;depth=<int>&amp;strict=(true|false)\n" +
                          "\tac\taccession number\n" +
                          "\tdepth\tdepth of graph\n" +
                          "\tstrict\t(true|false) for retrieval of strict MIF or not.\n" +
                          "\tversion\t(1|2.5) for the PSI XML Version.", aResponse );
            e.printStackTrace();
        } finally {
            if( out != null ) {
                out.close();
            }
        }
    }

    /**
     * giveErrorMsg will give return an error message to the user as text/HTML !
     *
     * @param errormsg The string included in the errormessage
     * @param res      HttpResponse
     */
    private void giveErrorMsg( String errormsg, HttpServletResponse res ) {
        res.setContentType( "text/html" );
        PrintWriter out = null;
        try {
            out = res.getWriter();
        } catch ( IOException e ) {
            logger.error( e ); // we cant give back an error ... give up.
        }
        out.println( "<html><head><title>An error occoured ...</title></head><body>" );
        out.println( "<h1>Sorry - an error occoured during processing your request:<h1><pre>" );
        out.println( errormsg );
        out.println( "</pre></body></html>" );
    }
}
