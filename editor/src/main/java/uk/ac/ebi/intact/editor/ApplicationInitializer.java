package uk.ac.ebi.intact.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.users.model.Role;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.core.users.persistence.dao.UsersDaoFactory;

import java.util.List;

/**
 * Application initializer.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
@Controller
public class ApplicationInitializer implements InitializingBean {

    private static final Log log = LogFactory.getLog( ApplicationInitializer.class );

    @Autowired
    private UsersDaoFactory usersDaoFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    @Transactional( "users" )
    public void afterPropertiesSet() throws Exception {
        createDefaultRoles();
        createDefaultUsers();
    }

    private void createDefaultUsers() {
        User admin = usersDaoFactory.getUserDao().getByLogin( "admin" );
        if( admin == null ) {
            admin = new User( "admin", "N/A", "N/A", "intact-admin@ebi.ac.uk" );
            admin.setPassword( "d033e22ae348aeb5660fc2140aec35850c4da997" );
            usersDaoFactory.getUserDao().persist( admin );

            final Role adminRole = usersDaoFactory.getRoleDao().getRoleByName( "ADMIN" );
            admin.addRole( adminRole );
            usersDaoFactory.getUserDao().saveOrUpdate( admin );
        }
    }

    private void createDefaultRoles() {
        final List<Role> allRoles = usersDaoFactory.getRoleDao().getAll();
        addMissingRole( allRoles, "ADMIN" );
        addMissingRole( allRoles, "CURATOR" );
        addMissingRole( allRoles, "REVIEWER" );

        log.info( "After init: found " + usersDaoFactory.getRoleDao().getAll().size() + " role(s) in the database." );
    }

    private void addMissingRole( List<Role> allRoles, String roleName ) {
        boolean found = false;
        for ( Role role : allRoles ) {
            if( role.getName().equals( roleName ) ) {
                 found = true;
            }
        }

        if( !found ) {
            Role role = new Role( roleName );
            usersDaoFactory.getRoleDao().persist( role );
            if ( log.isInfoEnabled() ) {
                log.info( "Created user role: " + roleName );
            }
        }
    }
}
