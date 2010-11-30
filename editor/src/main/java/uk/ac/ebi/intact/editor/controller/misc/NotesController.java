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
package uk.ac.ebi.intact.editor.controller.misc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.users.model.Preference;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.util.Rewriter;

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;

/**
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("session")
public class NotesController extends JpaAwareController {

    private String rawNotes;
    private boolean editMode;

    @Autowired
    private UserSessionController userSessionController;

    public NotesController() {
    }

    @Transactional("users")
    public void loadPage(ComponentSystemEvent evt) {
        User user = userSessionController.getCurrentUser();

        Preference pref = user.getPreference("editor.notes");

        if (pref == null) {
            pref = new Preference(user, "editor.notes");
            pref.setValue("This is an example of notes. \nYou can link to publications like publication:EBI-2928483 or an interaction interaction:EBI-2928497.\n" +
                    "You can use it as well for experiments, participants, etc.");

            user.getPreferences().add(pref);

            getUsersDaoFactory().getPreferenceDao().persist(pref);
        }

        rawNotes = pref.getValue();
    }

    @Transactional("users")
    public void saveNotes(ActionEvent evt) {
        User user = userSessionController.getCurrentUser();

        Preference pref = user.getPreference("editor.notes");
        pref.setValue(rawNotes);

        getUsersDaoFactory().getPreferenceDao().update(pref);

        editMode = false;
    }

    public String getFormattedNotes() {
        String formatted = rawNotes.replaceAll("\n", "<br/>");

        Rewriter rewriter = new Rewriter("(\\w+):(EBI-\\d+)", "<a href=\"/editor/{1}/{2}\">{2}</a>");
        formatted = rewriter.rewrite(formatted);

        return formatted;
    }

    public String getRawNotes() {
        return rawNotes;
    }

    public void setRawNotes(String rawNotes) {
        this.rawNotes = rawNotes;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }
}
