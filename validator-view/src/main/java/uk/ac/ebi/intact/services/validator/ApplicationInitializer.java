package uk.ac.ebi.intact.services.validator;

import uk.ac.ebi.intact.services.validator.context.ValidatorWebContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22-Jun-2010</pre>
 */

public class ApplicationInitializer implements ServletContextListener {

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("#############################################");
        System.out.println("Server started");
        System.out.println("#############################################");
        System.out.println("Prepare ontology and validator rules");
        ValidatorWebContext.getInstance();

    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("#############################################");
        System.out.println("Server shutting down");
        System.out.println("#############################################");
    }
}
