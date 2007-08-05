package uk.ac.ebi.intact.service.imex.xmlformatter;

/**
 * TODO comment that class header
 *
 * @author Samuel Kerrien
 * @version $Id$
 * @since TODO specify the maven artifact version
 */
public class XmlFormatterException extends Exception {
    public XmlFormatterException() {
        super();
    }

    public XmlFormatterException( String message ) {
        super( message );
    }

    public XmlFormatterException( String message, Throwable cause ) {
        super( message, cause );
    }

    public XmlFormatterException( Throwable cause ) {
        super( cause );
    }
}
