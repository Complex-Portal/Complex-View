package uk.ac.ebi.intact.service.complex.view;

import java.io.IOException;

/**
 * Created by maitesin on 02/07/2014.
 */
public class ComplexPortalException extends Exception {

    public ComplexPortalException() {
    }

    public ComplexPortalException(String s) {
        super(s);
    }

    public ComplexPortalException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ComplexPortalException(Throwable throwable) {
        super(throwable);
    }
}
