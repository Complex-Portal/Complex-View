package uk.ac.ebi.intact.editor.controller.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.DualListModel;
import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.users.model.Role;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.core.users.persistence.dao.UserDao;
import uk.ac.ebi.intact.editor.controller.misc.AbstractUserController;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;

import javax.faces.event.ComponentSystemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Controller used when administrating users.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
@Controller
@Scope( "session" )
//@ConversationName( "admin" )
public class UserAdminController extends AbstractUserController {

    private static final Log log = LogFactory.getLog( UserAdminController.class );


    private String loginParam;
    private DualListModel<String> roles;

    private LazyDataModel<User> allUsers;

    /////////////////
    // Users

    public String getLoginParam() {
        return loginParam;
    }

    public void setLoginParam( String loginParam ) {
        this.loginParam = loginParam;
    }

    ///////////////
    // Actions

    public void loadUserToUpdate( ComponentSystemEvent event ) {

        log.info( "AbstractUserController.loadUserToUpdate" );

        if ( loginParam != null ) {
            // load user and prepare for update
            log.debug( "Loading user by login '" + loginParam + "'..." );
            User user = getUsersDaoFactory().getUserDao().getByLogin( loginParam );
            setUser(user);

            if ( user == null ) {
                addWarningMessage( "Could not find user by login: " + loginParam, "Please try again." );
            } else {
                log.debug( "User password hash: " + user.getPassword() );
            }
        } else {
            // prepare for the creation of the new user
            setUser(new User());
        }
    }

    public void loadData() {
        log.debug( "AbstractUserController.loadData" );
        allUsers = LazyDataModelFactory.createLazyDataModel( getUsersEntityManager(),
                "select u from User u order by u.login asc",
                "select count(u) from User u" );
    }


    @Transactional( "users" )
    public String saveUser() {
        final UserDao userDao = getUsersDaoFactory().getUserDao();

        User user = getUser();

        boolean created = false;
        if ( !userDao.isManaged( user ) && !userDao.isDetached( user ) ) {
            userDao.persist( user );
            created = true;
        }

        // handle roles
        final List<String> includedRoles = roles.getTarget();
        for ( String roleName : includedRoles ) {
            if ( !user.hasRole( roleName ) ) {
                final Role r = getUsersDaoFactory().getRoleDao().getRoleByName( roleName );
                user.addRole( r );
                log.info( "Added role " + roleName + "to user " + user.getLogin() );
            }
        }

        final List<String> excludedRoles = roles.getSource();
        for ( String roleName : excludedRoles ) {
            if ( user.hasRole( roleName ) ) {
                final Role r = getUsersDaoFactory().getRoleDao().getRoleByName( roleName );
                user.removeRole( r );
                log.info( "Removed role " + roleName + "to user " + user.getLogin() );
            }
        }

        userDao.saveOrUpdate( user );

//        for (Preference pref : user.getPreferences()) {
//            if (pref.getPk() == null) {
//                daoFactory.getPreferenceDao().persist(pref);
//            } else {
//                daoFactory.getPreferenceDao().update(pref);
//            }
//
//        }

        addInfoMessage( "User " + user.getLogin() + " was " + ( created ? "created" : "updated" ) + " successfully", "" );

        // reset user before redirecting to the user list.
        user = null;

        return "admin.users.list";
    }

    public String newUser() {
        loginParam = null;
        setUser(null);
        return "/admin/users/edit?faces-redirect=true";
    }

    public void loadRoles( ComponentSystemEvent event ) {

        log.info( "AbstractUserController.loadRoles" );

        List<String> source = new ArrayList<String>();
        List<String> target = new ArrayList<String>();

        Collection<Role> allRoles = getUsersDaoFactory().getRoleDao().getAll();
        log.debug( "Found " + allRoles.size() + " role(s) in the database." );

        User user = getUser();

        if ( user == null ) {
            for ( Role role : allRoles ) {
                source.add( role.getName() );
            }
        } else {
            for ( Role role : allRoles ) {
                if ( user.getRoles().contains( role ) ) {
                    target.add( role.getName() );
                } else {
                    source.add( role.getName() );
                }
            }
        }

        roles = new DualListModel<String>( source, target );
    }

    public List<Role> createRoleList( User user ) {
        if ( user != null ) {
            return new ArrayList<Role>( user.getRoles() );
        }
        return null;
    }

    public void setRoles( DualListModel<String> roles ) {
        this.roles = roles;
    }

    public DualListModel<String> getRoles() {
        return roles;
    }

    public LazyDataModel<User> getAllUsers() {
        if (allUsers == null) {
            loadData();
        }

        return allUsers;
    }
}
