package uk.ac.ebi.intact.view.webapp.error.factory;

import uk.ac.ebi.intact.view.webapp.error.handlers.ViewExpiredExceptionHandler;

import javax.faces.context.ExceptionHandlerFactory;

/**
 * Factory for ViewExpiredExceptionHandler
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/10/12</pre>
 */

public class ViewExpiredExceptionHandlerFactory extends ExceptionHandlerFactory {

    private ExceptionHandlerFactory parent;

    public ViewExpiredExceptionHandlerFactory(ExceptionHandlerFactory parent) {
        this.parent = parent;
    }

    @Override
    public javax.faces.context.ExceptionHandler getExceptionHandler() {
        return new ViewExpiredExceptionHandler(parent.getExceptionHandler());
    }

}

