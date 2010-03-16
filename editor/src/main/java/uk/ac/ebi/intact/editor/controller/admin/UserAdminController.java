package uk.ac.ebi.intact.editor.controller.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.DualListModel;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DelegatingTransactionDefinition;
import uk.ac.ebi.intact.core.users.model.Role;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.core.users.persistence.dao.UserDao;
import uk.ac.ebi.intact.core.users.persistence.dao.UsersDaoFactory;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
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
@Scope( "conversation.access" )
public class UserAdminController extends JpaAwareController {

    private static final Log log = LogFactory.getLog( UserAdminController.class );

    @Autowired
    private UsersDaoFactory daoFactory;

    // User creation

    private String userLogin;
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    
    private Collection<String> userRoles;

    // roles

    private DualListModel<String> roles;

    // User update

    private User userToUpdate;

    // User list

    private LazyDataModel<User> allUsers;


    /////////////////
    // Users

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin( String userLogin ) {
        this.userLogin = userLogin;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail( String userEmail ) {
        this.userEmail = userEmail;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName( String userFirstName ) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName( String userLastName ) {
        this.userLastName = userLastName;
    }

    ///////////////
    // Actions



    public String saveUser() {
        final UserDao userDao = daoFactory.getUserDao();

        if ( userDao.getByLogin( userLogin ) != null ) {
            addWarningMessage( "A user with this login already exist", "" );
            return "error";
        }

        if ( userDao.getByEmail( userEmail ) != null ) {
            addWarningMessage( "A user with this email address already exist", "" );
            return "error";
        }

        final User newUser = new User( userLogin, userFirstName, userLastName, userEmail );
        userDao.persist( newUser );
        userDao.flush();

        addInfoMessage( "New user " + userLogin + " was created successfully", "" );

        return "admin.users.list";
    }

    public void loadRoles() {

        List<String> source = new ArrayList<String>();
        List<String> target = new ArrayList<String>();
        
        Collection<Role> allRoles = getUsersDaoFactory().getRoleDao().getAll();
        log.info( "Found " + allRoles.size() + " role(s) in the database." );
        if( userToUpdate == null ) {
            for ( Role role : allRoles ) {
                source.add( role.getName() );
            }
        } else {
            for ( Role role : allRoles ) {
                if( userToUpdate.getRoles().contains( role )) {
                    target.add( role.getName() );
                } else {
                    source.add( role.getName() );
                }
            }
        }

        roles = new DualListModel<String>( source, target );
    }

    public void setRoles( DualListModel<String> roles ) {
        this.roles = roles;
    }

    public DualListModel<String> getRoles() {
        return roles;
    }

    public void loadUserToUpdate() {
        System.out.println( "Loading user by login '" + userLogin + "'..." );
        if ( userLogin != null ) {
            userToUpdate = daoFactory.getUserDao().getByLogin( userLogin );
            if ( userToUpdate == null ) {
                addWarningMessage( "Could not find user by login: " + userLogin, "" );
            }
        }
    }

    public String updateUser() {
        daoFactory.getUserDao().update( userToUpdate );
        return "admin.users.list";
    }

    public void loadData( ComponentSystemEvent event ) {
        allUsers = LazyDataModelFactory.createLazyDataModel( getUsersEntityManager(),
                                                             "select u from User u order by u.login asc",
                                                             "select count(u) from User u" );
    }

    public LazyDataModel<User> getAllUsers() {
        return allUsers;
    }
}
