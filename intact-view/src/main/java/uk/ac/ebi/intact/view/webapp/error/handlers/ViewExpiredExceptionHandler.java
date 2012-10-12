package uk.ac.ebi.intact.view.webapp.error.handlers;

import uk.ac.ebi.intact.view.webapp.IntactViewException;

import javax.faces.FacesException;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import java.io.IOException;
import java.util.Iterator;

/**
 * Handler for view expired
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/10/12</pre>
 */

public class ViewExpiredExceptionHandler extends ExceptionHandlerWrapper {

    private javax.faces.context.ExceptionHandler wrapped;

    public ViewExpiredExceptionHandler(javax.faces.context.ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void handle() throws FacesException {

        for (Iterator<ExceptionQueuedEvent> iter = getUnhandledExceptionQueuedEvents().iterator(); iter.hasNext();) {
            ExceptionQueuedEvent evt = iter.next();
            Throwable exception = evt.getContext().getException();
            FacesContext fc = evt.getContext().getContext();
            if (exception instanceof javax.faces.application.ViewExpiredException) {

                try {
                    fc.getExternalContext().redirect("/intact/main.xhtml?status=exp");
                    fc.responseComplete();
                } catch (IOException e) {
                    throw new IntactViewException(e);
                }
                finally{
                    iter.remove();
                }
            }
        }

        getWrapped().handle();
    }

    @Override
    public javax.faces.context.ExceptionHandler getWrapped() {
        return wrapped;
    }

}

