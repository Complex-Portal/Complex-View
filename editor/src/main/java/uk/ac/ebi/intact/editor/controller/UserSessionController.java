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
package uk.ac.ebi.intact.editor.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.users.model.Preference;
import uk.ac.ebi.intact.core.users.model.Role;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.core.users.persistence.dao.UserDao;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "session" )
public class UserSessionController extends JpaAwareController implements DisposableBean {

    private static final Log log = LogFactory.getLog( UserSessionController.class );

    @Autowired
    private UserDao userDao;

    private User currentUser;

    public UserSessionController() {
    }

    public User getCurrentUser() {
        return getCurrentUser(false);
    }

    public User getCurrentUser(boolean refresh) {
        if (refresh) {
            currentUser = getUsersDaoFactory().getUserDao().getByLogin(currentUser.getLogin());
        }

        return currentUser;
    }

    public void setCurrentUser( User currentUser ) {
        this.currentUser = currentUser;
    }

    public boolean hasRole(String role) {
        if (role == null) throw new NullPointerException("Role is null");

        for (Role userRole : currentUser.getRoles()) {
            if ("ADMIN".equals(userRole.getName())) {
                return true;
            }
        }

        for (Role userRole : currentUser.getRoles()) {
            if (role.equals(userRole.getName())) {
                return true;
            }
        }

        return false;
    }

    @Transactional
    public void notifyLastActivity() {
        DateTime dateTime = new DateTime();
        String dateTimeStr = dateTime.toString("dd/MM/yyyy HH:mm");

        if (currentUser != null) {
            Preference pref = currentUser.getPreference("last.activity");

            if (pref == null) {
                pref = new Preference(currentUser, "last.activity");
            }

            pref.setValue(dateTimeStr);

            currentUser.getPreferences().add(pref);

            userDao.saveOrUpdate(currentUser);
        }
    }

    @Override
    public void destroy() throws Exception {
        log.info( "UserSessionController for user '" + currentUser.getLogin() + "' destroyed" );
    }
}
