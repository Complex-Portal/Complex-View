package uk.ac.ebi.intact.service.imex;

/**
 * TODO comment that class header
 *
 * @author Samuel Kerrien
 * @version $Id$
 * @since TODO specify the maven artifact version
 */
public class ImexExporterException extends Exception {
    public ImexExporterException() {
        super();
    }

    public ImexExporterException( String message ) {
        super( message );
    }

    public ImexExporterException( String message, Throwable cause ) {
        super( message, cause );
    }

    public ImexExporterException( Throwable cause ) {
        super( cause );
    }
}
