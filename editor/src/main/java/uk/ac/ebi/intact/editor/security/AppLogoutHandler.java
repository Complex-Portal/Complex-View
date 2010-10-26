package uk.ac.ebi.intact.editor.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.Authentication;
import org.springframework.security.ui.logout.LogoutHandler;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.UserListener;
import uk.ac.ebi.intact.editor.controller.UserSessionController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Custom behaviour at logout.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
public class AppLogoutHandler implements LogoutHandler {

    private static final Log log = LogFactory.getLog( AppLogoutHandler.class );

    public AppLogoutHandler() {
        super();
    }

    @Override
    public void logout( HttpServletRequest request, HttpServletResponse response, Authentication authentication ) {

        log.debug( "Logout [" + authentication.getPrincipal() + "]" );

        final HttpSession session = request.getSession();

        log.debug( "Session Id: " + session.getId() );

        final ConfigurableApplicationContext springContext = IntactContext.getCurrentInstance().getSpringContext();
        UserSessionController userSessionController = ( UserSessionController ) springContext.getBean( "userSessionController" );

        if ( userSessionController != null ) {
            log.debug( "Destroying session for user " + authentication.getPrincipal() + ": " + session.getId() );

            // get all the "user listener" beans and notify the logout
            final Map<String,UserListener> userListeners = springContext.getBeansOfType(UserListener.class);

            for (UserListener userListener : userListeners.values()) {
                userListener.userLoggedIn(userSessionController.getCurrentUser());
            }
        } else {
            throw new IllegalStateException( "Destroying HTTP session - no UserSessionController available." );
        }
    }
}
