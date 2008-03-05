package uk.ac.ebi.intact.services.faces;

import javax.faces.FacesException;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AthenaFacesException extends FacesException{
    
    public AthenaFacesException() {
    }

    public AthenaFacesException(Throwable cause) {
        super(cause);
    }

    public AthenaFacesException(String message) {
        super(message);
    }

    public AthenaFacesException(String message, Throwable cause) {
        super(message, cause);
    }
}
