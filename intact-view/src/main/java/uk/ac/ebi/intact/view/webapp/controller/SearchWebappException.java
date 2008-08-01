package uk.ac.ebi.intact.view.webapp.controller;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SearchWebappException extends RuntimeException{
    public SearchWebappException() {
    }

    public SearchWebappException(String s) {
        super(s);
    }

    public SearchWebappException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SearchWebappException(Throwable throwable) {
        super(throwable);
    }
}
