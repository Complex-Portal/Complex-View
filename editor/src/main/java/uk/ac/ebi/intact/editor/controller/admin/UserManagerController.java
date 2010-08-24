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
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.editor.controller.UserSessionController;

import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class UserManagerController extends BaseController {

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

    public void notifyLogin(User user) {
        user.setLastLogin(new Date());
        UserSessionController userSessionController = ( UserSessionController ) getSpringContext().getBean( "userSessionController" );
        userSessionController.setCurrentUser(user);

        // set the user to be used when writing into the database
        IntactContext.getCurrentInstance().getUserContext().setUserId( user.getLogin().toUpperCase() );

        loggedInUsers.add(user);
    }

    public void notifyLogout(User user) {
        loggedInUsers.remove(user);
    }
}
