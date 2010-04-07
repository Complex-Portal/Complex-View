package uk.ac.ebi.intact.editor.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    @Override
    public void sessionCreated( HttpSessionEvent se ) {
        log.debug( "========================== SESSION CREATED [" + se.getSession().getId() + "] ==========================" );
    }

    @Override
    public void sessionDestroyed( HttpSessionEvent se ) {
        log.debug( "========================== SESSION DESTROYED [" + se.getSession().getId() + "] ==========================" );
    }
}
