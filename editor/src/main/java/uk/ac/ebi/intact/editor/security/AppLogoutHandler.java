package uk.ac.ebi.intact.editor.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.Authentication;
import org.springframework.security.ui.logout.LogoutHandler;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.controller.admin.UserManagerController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
        UserManagerController userManagerController = ( UserManagerController ) springContext.getBean( "userManagerController" );


        if ( userSessionController != null ) {
            log.debug( "Destroying session for user " + authentication.getPrincipal() + ": " + session.getId() );

            userManagerController.notifyLogout(userSessionController.getCurrentUser());
//            final Collection<User> users = userManagerController.getLoggedInUsers();
//            boolean found = false;
//            for ( Iterator<User> iterator = users.iterator(); iterator.hasNext(); ) {
//                User user = iterator.next();
//                if ( user.getLogin().equals( authentication.getPrincipal() ) ) {
//                    found = true;
//                    iterator.remove();
//                    log.debug( "Removed user user " + authentication.getPrincipal() + " from the list of connected users, " +
//                               appController.getLoggedInUsers().size() + " remaining." );
//                    break;
//                }
//            }

//            if ( !found ) {
//                log.error( "Could not find user '" + userSessionController.getCurrentUser().getLogin() + "' in the list of logged in users" );
//            }
        } else {
            throw new IllegalStateException( "Destroying HTTP session - no UserSessionController available." );
        }
    }
}
