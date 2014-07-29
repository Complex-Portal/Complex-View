package uk.ac.ebi.intact.editor.controller.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.DualListModel;
import org.primefaces.model.SelectableDataModelWrapper;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.persistence.dao.user.UserDao;
import uk.ac.ebi.intact.core.persistence.svc.UserService;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.editor.controller.misc.AbstractUserController;
import uk.ac.ebi.intact.editor.util.SelectableCollectionDataModel;
import uk.ac.ebi.intact.model.user.Role;
import uk.ac.ebi.intact.model.user.User;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.DataModel;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    private DataModel<User> allUsers;

    private User[] selectedUsers;

    private boolean reset;

    @Autowired
    private UserService userService;

    private List<UserWrapper> usersToImport = new ArrayList<UserWrapper>(  );

    private boolean importUpdateEnabled = true;

    private UserWrapper[] selectedUsersToImport;

    private UploadedFile uploadedFile;

    private boolean fileUploaded;

    private List<SelectItem> reviewerSelectItems;

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

        loadReviewerSelectItems();

        if ( loginParam != null ) {
            // load user and prepare for update
            log.debug( "Loading user by login '" + loginParam + "'..." );
            User user = getDaoFactory().getUserDao().getByLogin( loginParam );
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

    private void loadReviewerSelectItems() {
        this.reviewerSelectItems = new ArrayList<SelectItem>();
        reviewerSelectItems.add(new SelectItem(null, "-- Random --", "Correction assigner", false, true, false));

        List<User> reviewers = getDaoFactory().getUserDao().getReviewers();

        for (User reviewer : reviewers) {
            reviewerSelectItems.add(new SelectItem(reviewer, reviewer.getLogin()));
        }
    }

    public void loadData() {
        log.debug( "AbstractUserController.loadParticipants" );

        Collection<User> users = getDaoFactory().getUserDao().getAll();
        allUsers = new SelectableDataModelWrapper(new SelectableCollectionDataModel<User>(users), users);
    }


    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED)
    public String saveUser() {
        final UserDao userDao = getDaoFactory().getUserDao();

        User user = getUser();

        boolean created = false;
        if ( !IntactCore.isManaged( user ) && ! IntactCore.isDetached( user ) ) {
            userDao.persist( user );
            created = true;
        }

        // We need to attach the object to the session if we want that orphan removal=true works when
        // we delete a preference (e.g. mentor.reviewer)
        if(IntactCore.isDetached( user )){
            User newUser = getCoreEntityManager().merge(user);
            setUser(newUser);
            user = newUser;
        }

        // handle roles
        final List<String> includedRoles = roles.getTarget();
        for ( String roleName : includedRoles ) {
            if ( !user.hasRole( roleName ) ) {
                final Role r = getDaoFactory().getRoleDao().getRoleByName( roleName );
                user.addRole( r );
                log.info( "Added role " + roleName + "to user " + user.getLogin() );
            }
        }

        final List<String> excludedRoles = roles.getSource();
        for ( String roleName : excludedRoles ) {
            if ( user.hasRole( roleName ) ) {
                final Role r = getDaoFactory().getRoleDao().getRoleByName( roleName );
                user.removeRole( r );
                log.info( "Removed role " + roleName + "to user " + user.getLogin() );
            }
        }

        userDao.saveOrUpdate( user );

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

        Collection<Role> allRoles = getDaoFactory().getRoleDao().getAll();
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

    public DataModel<User> getAllUsers() {
        if (allUsers == null) {
            loadData();
        }

        return allUsers;
    }

    public User[] getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers( User[] selectedUsers ) {
        this.selectedUsers = selectedUsers;
    }

    public boolean hasSelectedUsers() {
        if( this.selectedUsers != null ) {
            for ( int i = 0; i < selectedUsers.length; i++ ) {
                if ( selectedUsers[i] != null ) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getSelectedUsersLogin( String separator ) {
        String logins = null;
        if( this.selectedUsers != null ) {
            StringBuilder sb = new StringBuilder( 128 );
            for ( int i = 0; i < selectedUsers.length; i++ ) {
                User selectedUser = selectedUsers[i];
                if( selectedUser != null ) {
                    sb.append( selectedUser.getLogin() );
                    if((i+1) < selectedUsers.length ) {
                        sb.append( separator );
                    }
                }
            }
            logins = sb.toString();
        }

        return logins;
    }

    public void exportSelectedUsers( ActionEvent evt ) {
        if( ! hasSelectedUsers() ) {
           addWarningMessage( "Cannot export empty user selection.", "Please select at least one user." );
            return;
        }

        try {
            final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            final HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
            final HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

            response.sendRedirect(request.getContextPath()+"/service/export/users/byLogin?logins="+getSelectedUsersLogin( "," ));

            FacesContext.getCurrentInstance().responseComplete();
        } catch (IOException e) {
            handleException(e);
        }
    }

    ///////////////////////
    // User Import


    public boolean isReset() {
        return reset;
    }

    public void setReset( boolean reset ) {
        this.reset = reset;
    }

    public void initUserImport() {
        if( reset ) {
            usersToImport = new ArrayList<UserWrapper>();
            fileUploaded = false;
            selectedUsersToImport = null;
            reset = false;
        }
    }

    public void upload() {
        usersToImport = new ArrayList<UserWrapper>();
        try {
            InputStream inputStream = uploadedFile.getInputstream();
            Collection<User> users = Collections.EMPTY_LIST;
            try{
                users = userService.parseUsers( inputStream );
            }
            finally {
                inputStream.close();
            }

            if( ! users.isEmpty() ) {
                for ( User user : users ) {
                    final UserWrapper uw = new UserWrapper( user );
                    User dbUser = getDaoFactory().getUserDao().getByLogin( user.getLogin() );
                    if( dbUser != null ) {
                        uw.setAlreadyExistsInDB( true );
                    }
                    usersToImport.add( uw );
                }
            }

            addInfoMessage( "Successful", usersToImport.size() + " users loaded from file " + uploadedFile.getFileName() );
            fileUploaded = true;

        } catch ( Exception e ) {
            addWarningMessage( "Failed", "Could not parse user file: " + uploadedFile.getFileName() );
        }
    }

    public void importSelectedUsers() {
        if( selectedUsersToImport == null || selectedUsersToImport.length == 0 ) {
            addWarningMessage( "Failed", "You must select at least one user to import." );
            return;
        }

        Collection<User> users = new ArrayList<User>( );
        int newUser = 0;
        int existingUsers = 0;
        for ( int i = 0; i < selectedUsersToImport.length; i++ ) {
            UserWrapper wrapper = selectedUsersToImport[i];
            users.add( wrapper.getUser() );
            if( wrapper.isAlreadyExistsInDB() ) {
                existingUsers++;
            } else {
                newUser++;
            }
        }

        userService.importUsers( users, importUpdateEnabled );

        // clear up data
        usersToImport = new ArrayList<UserWrapper>();
        fileUploaded = false;
        selectedUsersToImport = null;

        String msg = null;
        if( importUpdateEnabled ) {
            msg = newUser + " created, " + existingUsers + " updated users.";
        } else {
            msg = newUser + " users created.";
        }
        addInfoMessage( "Successful", msg );
    }

    public List<UserWrapper> getUsersToImport() {
        return usersToImport;
    }

    public void setUsersToImport( List<UserWrapper> usersToImport ) {
        this.usersToImport = usersToImport;
    }

    public boolean isImportUpdateEnabled() {
        return importUpdateEnabled;
    }

    public void setImportUpdateEnabled( boolean importUpdateEnabled ) {
        this.importUpdateEnabled = importUpdateEnabled;
    }

    public class UserWrapper extends User {

        private User user;

        private boolean alreadyExistsInDB;

        public UserWrapper( User user ) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        public void setUser( User user ) {
            this.user = user;
        }

        public boolean isAlreadyExistsInDB() {
            return alreadyExistsInDB;
        }

        public void setAlreadyExistsInDB( boolean alreadyExistsInDB ) {
            this.alreadyExistsInDB = alreadyExistsInDB;
        }
    }

    public UserWrapper[] getSelectedUsersToImport() {
        return selectedUsersToImport;
    }

    public void setSelectedUsersToImport( UserWrapper[] selectedUsersToImport ) {
        this.selectedUsersToImport = selectedUsersToImport;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile( UploadedFile uploadedFile ) {
        this.uploadedFile = uploadedFile;
    }

    public boolean isFileUploaded() {
        return fileUploaded;
    }

    public void setFileUploaded( boolean fileUploaded ) {
        this.fileUploaded = fileUploaded;
    }

    public List<SelectItem> getReviewerSelectItems() {
        if (reviewerSelectItems == null) {
            loadReviewerSelectItems();
        }

        return reviewerSelectItems;
    }

    public void setReviewerSelectItems(List<SelectItem> reviewerSelectItems) {
        this.reviewerSelectItems = reviewerSelectItems;
    }
}
