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
import org.hibernate.Hibernate;
import org.joda.time.DateTime;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.Source;
import uk.ac.ebi.intact.core.persistence.dao.InstitutionDao;
import uk.ac.ebi.intact.editor.controller.misc.AbstractUserController;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.context.IntactContext;
import uk.ac.ebi.intact.jami.dao.SourceDao;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.user.Preference;
import uk.ac.ebi.intact.model.user.Role;
import uk.ac.ebi.intact.model.user.User;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "session" )
public class UserSessionController extends JpaAwareController implements DisposableBean {

    private static final Log log = LogFactory.getLog( UserSessionController.class );

    private User currentUser;
    private uk.ac.ebi.intact.jami.model.user.User currentJamiUser;

    public UserSessionController() {
    }

    public User getCurrentUser() {
        return getCurrentUser(false);
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public User getCurrentUser(boolean refresh) {
        if (refresh) {
            currentUser = getDaoFactory().getUserDao().getByLogin(currentUser.getLogin());
            Hibernate.initialize(currentUser.getRoles());
            Hibernate.initialize(currentUser.getPreferences());
        }

        return currentUser;
    }

    public void setCurrentUser( User currentUser ) {
        this.currentUser = currentUser;
    }

    public uk.ac.ebi.intact.jami.model.user.User getCurrentJamiUser() {
        return currentJamiUser;
    }

    public void setCurrentJamiUser(uk.ac.ebi.intact.jami.model.user.User currentJamiUser) {
        this.currentJamiUser = currentJamiUser;
    }

    public boolean hasRole(String role) {
        if (role == null) throw new NullPointerException("Role is null");

        for (Role userRole : currentUser.getRoles()) {
            if ("ADMIN".equals(userRole.getName()) || role.equals(userRole.getName())) {
                return true;
            }
        }

        return false;
    }

    public boolean isItMe(User user) {
        if (user == null) return false;

        return user.equals(currentUser);
    }

    public boolean isJamiUserMe(uk.ac.ebi.intact.jami.model.user.User user) {
        if (user == null) return false;

        return user.getLogin().equals(currentUser.getLogin());
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED)
    public void notifyLastActivity() {
        DateTime dateTime = new DateTime();
        String dateTimeStr = dateTime.toString("dd/MM/yyyy HH:mm");

        if (currentUser != null) {
            // merge current user because detached
            if (!getCoreEntityManager().contains(currentUser)){
                currentUser = getCoreEntityManager().merge(currentUser);
            }

            Preference pref = currentUser.getPreference("last.activity");

            if (pref == null) {
                pref = new Preference(currentUser, "last.activity");
            }

            pref.setValue(dateTimeStr);

            currentUser.getPreferences().add(pref);

            getDaoFactory().getPreferenceDao().saveOrUpdate(pref);
        }
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, readOnly = true)
    public Institution getUserInstitution() {
        Preference instiPref = currentUser.getPreference(AbstractUserController.INSTITUTION_AC);

        if (instiPref == null) {
            return getIntactContext().getInstitution();
        }

        InstitutionDao institutionDao = getDaoFactory().getInstitutionDao();

        Institution institution = institutionDao.getByAc(instiPref.getValue());

        if (institution == null) {
            return getIntactContext().getInstitution();
        }

        return institution;
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public Source getUserSource() {
        Preference instiPref = currentUser.getPreference(AbstractUserController.INSTITUTION_AC);

        if (instiPref == null) {
            IntactContext jamiContext = ApplicationContextProvider.getBean("jamiIntactContext");
            return jamiContext.getIntactConfiguration().getDefaultInstitution();
        }

        SourceDao institutionDao = getIntactDao().getSourceDao();

        Source institution = institutionDao.getByAc(instiPref.getValue());

        if (institution == null) {
            IntactContext jamiContext = ApplicationContextProvider.getBean("jamiIntactContext");
            return jamiContext.getIntactConfiguration().getDefaultInstitution();
        }

        return institution;
    }

    @Override
    public void destroy() throws Exception {
        log.info( "UserSessionController for user '" + currentUser.getLogin() + "' destroyed" );
    }
}
