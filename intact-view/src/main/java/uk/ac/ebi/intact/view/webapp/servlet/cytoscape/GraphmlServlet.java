package uk.ac.ebi.intact.view.webapp.servlet.cytoscape;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import psidev.psi.mi.tab.converter.tab2graphml.Tab2Cytoscapeweb;
import psidev.psi.mi.xml.converter.ConverterException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Servlet enabling the conversion of a MITAB Stream into GraphML.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.2
 */
@Component
public class GraphmlServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(GraphmlServlet.class);

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {

        String mitabUrl = request.getParameter("mitabUrl");
        if (log.isDebugEnabled()) log.debug("mitabUrl: " + mitabUrl );

        mitabUrl = mitabUrl.replaceAll( " ", "%20" );

        final URL inputUrl = new URL( mitabUrl );
        final InputStream is = inputUrl.openStream();

        ServletOutputStream stream = response.getOutputStream();
        response.setContentType("text/plain");

        final Tab2Cytoscapeweb tab2Cytoscapeweb = new Tab2Cytoscapeweb();

        String output = null;
        try {
            output = tab2Cytoscapeweb.convert(is);
        } catch (ConverterException e) {
            throw new IllegalStateException( "Could not parse input MITAB.", e );
        }
        stream.write( output.getBytes() );

        stream.flush();
        stream.close();
    }
}
