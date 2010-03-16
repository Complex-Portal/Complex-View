package uk.ac.ebi.intact.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import uk.ac.ebi.intact.core.users.model.Role;
import uk.ac.ebi.intact.core.users.persistence.dao.UsersDaoFactory;

import java.util.Collection;
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
        final List<Role> allRoles = usersDaoFactory.getRoleDao().getAll();
        addMissing( allRoles, "ADMIN" );
        addMissing( allRoles, "CURATOR" );
        addMissing( allRoles, "REVIEWER" );

        log.info( "After init: found " + usersDaoFactory.getRoleDao().getAll().size() + " role(s) in the database." );
    }

    private void addMissing( List<Role> allRoles, String roleName ) {
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
