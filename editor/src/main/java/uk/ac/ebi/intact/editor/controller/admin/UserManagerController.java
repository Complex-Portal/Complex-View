/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.editor.controller.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.editor.controller.UserListener;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.context.UserContext;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.model.user.User;

import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@EnableTransactionManagement
@Configuration
public class UserManagerController extends BaseController implements UserListener {

    private static final Log log = LogFactory.getLog( UserManagerController.class );

    private Set<User> loggedInUsers;

    public UserManagerController() {
        loggedInUsers = new HashSet<User>();
    }

    public Collection<User> getLoggedInUsers() {
        return new ArrayList<User>(loggedInUsers);
    }

    public void setLoggedInUsers( Set<User> loggedInUsers ) {
        this.loggedInUsers = loggedInUsers;
    }

    public String getLoggedInUserCount() {
        return String.valueOf( loggedInUsers.size() );
    }

    public User getUser(String userId) {
        for (User user : loggedInUsers) {
            if (userId.equalsIgnoreCase(user.getLogin())) {
                return user;
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "User " + userId + " not found, users available at this time are:" );
            for ( User user : loggedInUsers ) {
                log.debug( "- " + user.getLogin() );
            }
        }

        return null;
    }

    @Override
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void userLoggedIn(User user) {
        if (user == null) return;

        user.setLastLogin(new Date());
        UserSessionController userSessionController = ( UserSessionController ) getSpringContext().getBean( "userSessionController" );
        userSessionController.setCurrentUser(user);

        // set the user to be used when writing into the database
        IntactContext.getCurrentInstance().getUserContext().setUser( user );

        UserContext jamiUserContext = ApplicationContextProvider.getBean("jamiUserContext");
        IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
        jamiUserContext.setUser(intactDao.getUserDao().getByLogin(user.getLogin()));
        Hibernate.initialize(jamiUserContext.getUser().getPreferences());
        Hibernate.initialize(jamiUserContext.getUser().getRoles());

        loggedInUsers.add(user);
    }

    @Override
    public void userLoggedOut(User user) {
        loggedInUsers.remove(user);
    }

    /**
     *
     * @return the time zone of the application so it does not show one hour of difference when logging
     */
    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

}
