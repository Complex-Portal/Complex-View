package uk.ac.ebi.intact.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.user.Role;
import uk.ac.ebi.intact.model.user.User;

import java.util.List;

/**
 * Application initializer.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
@Service
public class ApplicationInitializer extends JpaAwareController implements InitializingBean {

    private static final Log log = LogFactory.getLog( ApplicationInitializer.class );

    @Autowired
    private DaoFactory daoFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    @Transactional("transactionManager")
    public void afterPropertiesSet() throws Exception {
        createDefaultRoles();
        createDefaultUsers();
    }

    public void persistCvObjectWithLabel(String shortLabel, String usedInClass) {
        CvTopic correctionCommentTopic = getDaoFactory().getCvObjectDao(CvTopic.class).getByShortLabel(shortLabel);

        if (correctionCommentTopic == null) {
            // create it
            correctionCommentTopic = new CvTopic(shortLabel);
            correctionCommentTopic.addAnnotation(new Annotation(getDaoFactory().getCvObjectDao(CvTopic.class).getByShortLabel(CvTopic.USED_IN_CLASS), usedInClass));
            getCorePersister().saveOrUpdate(correctionCommentTopic);
        }
    }

    private void createDefaultUsers() {
        User admin = daoFactory.getUserDao().getByLogin( "admin" );
        if ( admin == null ) {
            admin = new User( "admin", "Admin", "N/A", "intact-admin@ebi.ac.uk" );
            admin.setPassword( "d033e22ae348aeb5660fc2140aec35850c4da997" );
            daoFactory.getUserDao().persist( admin );

            final Role adminRole = daoFactory.getRoleDao().getRoleByName( "ADMIN" );
            admin.addRole( adminRole );
            daoFactory.getUserDao().saveOrUpdate( admin );
        }
    }

    private void createDefaultRoles() {
        final List<Role> allRoles = daoFactory.getRoleDao().getAll();
        addMissingRole( allRoles, "ADMIN" );
        addMissingRole( allRoles, "CURATOR" );
        addMissingRole( allRoles, "REVIEWER" );

        log.info( "After loadData: found " + daoFactory.getRoleDao().getAll().size() + " role(s) in the database." );
    }

    private void addMissingRole( List<Role> allRoles, String roleName ) {
        boolean found = false;
        for ( Role role : allRoles ) {
            if ( role.getName().equals( roleName ) ) {
                found = true;
            }
        }

        if ( !found ) {
            Role role = new Role( roleName );
            daoFactory.getRoleDao().persist( role );
            if ( log.isInfoEnabled() ) {
                log.info( "Created user role: " + roleName );
            }
        }
    }
}
