package uk.ac.ebi.intact.services.validator;

import psidev.psi.mi.xml.stylesheets.XslTransformerUtils;

import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * Data model the validator can deal with.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
public enum DataModel {

    // Enums declaration

    PSI_MI( true ),
    PSI_PAR( true );


    // Enum behaviour

    private static final Log log = LogFactory.getLog( DataModel.class );

    private boolean hasHtmlViewBuilder;

    DataModel( boolean hasHtmlViewBuilder ) {
        this.hasHtmlViewBuilder = hasHtmlViewBuilder;
    }

    public boolean hasHtmlViewBuilder() {
        return hasHtmlViewBuilder;
    }

    /**
     * Creates the HTML view using Xstl Transformation, and sets it to the report
     *
     * @param report The report to set the view
     * @param is     The input stream with the PSI XML file
     */
    public void createHtmlView( PsiReport report, InputStream is ) {
        String transformedOutput = null;
        try {
            // we transform the xml to html using an utility class that returns
            // the output stream with the html content
            final ByteArrayOutputStream os = new ByteArrayOutputStream( 4096 );
            XslTransformerUtils.viewPsiMi25( is, os );
            transformedOutput = os.toString();
        }
        catch ( Exception e ) {
            log.error( "Failed to produce the HTML view", e );
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage( "Failed to produce the HTML view of your data: " + e.getMessage() );
            context.addMessage( null, message );
        }
        report.setHtmlView( transformedOutput );
    }
}
