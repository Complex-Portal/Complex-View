package uk.ac.ebi.intact.editor.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.context.UserContext;
import uk.ac.ebi.intact.editor.controller.UserListener;
import uk.ac.ebi.intact.editor.controller.admin.UserManagerController;
import uk.ac.ebi.intact.model.user.User;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Map;

/**
 * Session listener.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
public class AppSessionListener implements HttpSessionListener {

    private final Logger log = LoggerFactory.getLogger(AppSessionListener.class);

    @Override
    public void sessionCreated( HttpSessionEvent se ) {
        log.debug( "Session created [" + se.getSession().getId() + "]" );
    }

    @Override
    public void sessionDestroyed( HttpSessionEvent se ) {
        final HttpSession session = se.getSession();
        log.debug( "Session destroyed [{}]", session.getId() );

        // We need access to the UserContext for the current session
        final UserContext userContext = ( UserContext ) session.getAttribute( "scopedTarget.userContext" );

        if( userContext != null ) {
            log.info( "Attempting to logout user..." );
            final ConfigurableApplicationContext springContext = IntactContext.getCurrentInstance().getSpringContext();
            final UserManagerController userManagerController = (UserManagerController) springContext.getBean("userManagerController");

            final String userId = userContext.getUserId();
            final User user = userManagerController.getUser( userId );
            if( user != null ) {
                // get all the "user listener" beans and notify the logout
                final Map<String,UserListener> userListeners = springContext.getBeansOfType(UserListener.class);

                for (UserListener userListener : userListeners.values()) {
                    log.debug( "Calling {}.userLoggedOut({});", userListener.getClass().getName(), user.getLogin() );
                    userListener.userLoggedOut(user);
                }
            } else {
                log.debug( "Destroying HTTP session - no User available to perform logout: {}", userId );
            }
        } else {
            log.debug( "No UserContext available in the session !!!!" );
        }
    }
}
