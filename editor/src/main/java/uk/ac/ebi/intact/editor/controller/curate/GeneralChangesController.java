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
package uk.ac.ebi.intact.editor.controller.curate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.editor.controller.UserListener;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.model.IntactObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
public class GeneralChangesController extends BaseController implements UserListener {

    private static final Log log = LogFactory.getLog( GeneralChangesController.class );

    private Map<String,Map<Object,UnsavedChangeManager>> userUnsavedMap;

    public GeneralChangesController() {
        userUnsavedMap = new HashMap<String, Map<Object, UnsavedChangeManager>>();
    }

    @Override
    public void userLoggedIn(User user) {
        if (user == null) return;

        userUnsavedMap.put(user.getLogin(), new HashMap<Object, UnsavedChangeManager>());
    }

    @Override
    public void userLoggedOut(User user) {
        final Map<Object, UnsavedChangeManager> unsavedMap = userUnsavedMap.get(user.getLogin());

        if (unsavedMap == null) {
            throw new IllegalStateException("No unsaved changes map found for user: "+user.getLogin());
        }

        if (!unsavedMap.isEmpty()) {
            if (log.isInfoEnabled()) log.info("User logged out with "+unsavedMap.size()+" pending changes: "+user.getLogin());

            for (UnsavedChangeManager unsavedChangeManager : unsavedMap.values()) {
                unsavedChangeManager.clearChanges();
            }
        }

        userUnsavedMap.remove(user.getLogin());
    }

    public Map<Object,UnsavedChangeManager> getUnsavedMapForUser(String username) {
        Map<Object,UnsavedChangeManager> unsavedMap;

        if (userUnsavedMap.containsKey(username)) {
            unsavedMap = userUnsavedMap.get(username);
        } else {
            unsavedMap = new HashMap<Object, UnsavedChangeManager>();
        }

        return unsavedMap;
    }

    public List<UnsavedChange> getUnsavedChangesForUser(String userName) {
        List<UnsavedChange> userChanges = new ArrayList<UnsavedChange>();

        for (UnsavedChangeManager unsavedChangeManager : getUnsavedMapForUser(userName).values()) {
            userChanges.addAll(unsavedChangeManager.getChanges());
        }

        return userChanges;
    }

    public List<String> getUsernames() {
        return new ArrayList<String>(userUnsavedMap.keySet());
    }

    public boolean isObjectBeingEdited(IntactObject io, boolean includeMyself) {
        if (io.getAc() == null) return false;

        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");
        String me = userSessionController.getCurrentUser().getLogin();

        for (String user : getUsernames()) {
            if (!includeMyself && user.equals(me)) continue;

            for (UnsavedChangeManager unsavedChangeManager : getUnsavedMapForUser(user).values()) {
                  if (unsavedChangeManager.constainsAc(io.getAc())) {
                      return true;
                  }
            }
        }

        return false;
    }

    public String whoIsEditingObject(IntactObject io) {
        if (io.getAc() == null) return null;

        for (String user : getUsernames()) {
            for (UnsavedChangeManager unsavedChangeManager : getUnsavedMapForUser(user).values()) {
                  if (unsavedChangeManager.constainsAc(io.getAc())) {
                      return user;
                  }
            }
        }

        return null;
    }
}
