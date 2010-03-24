package uk.ac.ebi.intact.editor.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.editor.controller.AppController;
import uk.ac.ebi.intact.editor.controller.UserSessionController;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Session listener.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
public class AppSessionListener implements HttpSessionListener {

    private static final Log log = LogFactory.getLog( AppSessionListener.class );

    @Autowired
    private AppController appController;

    @Autowired
    private UserSessionController userSessionController;

    @Override
    public void sessionCreated( HttpSessionEvent se ) {

    }

    @Override
    public void sessionDestroyed( HttpSessionEvent se ) {
        if( userSessionController != null ) {
            final User user = userSessionController.getCurrentUser();
            log.info( "Destroying session for user " + user.getLogin());
            appController.getLoggedInUsers().remove( user );
        }
    }
}
