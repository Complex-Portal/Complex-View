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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.core.users.model.Preference;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.model.*;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("session")
public class MyNotesController extends JpaAwareController {

    private static final Log log = LogFactory.getLog(MyNotesController.class);

    private String rawNotes;
    private boolean editMode;
    private String absoluteContextPath;

    @Autowired
    private UserSessionController userSessionController;

    public MyNotesController() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        this.absoluteContextPath = request.getScheme() + "://" +
               request.getServerName() + ":" +
               request.getServerPort() +
               request.getContextPath();
    }

    @Transactional("users")
    public void loadPage(ComponentSystemEvent evt) {
        User user = userSessionController.getCurrentUser();

        Preference pref = user.getPreference("editor.notes");

        if (pref == null) {
            pref = new Preference(user, "editor.notes");
            pref.setValue("These are your notes. You can write anything you wish here. \nYou can link to publications like EBI-2928483 or an interaction EBI-2928497.\n" +
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

        String acPrefix = getIntactContext().getConfig().getAcPrefix();

        Pattern pattern = Pattern.compile(acPrefix+"-\\d+");
        Matcher matcher = pattern.matcher(formatted);

        StringBuffer sb = new StringBuffer(formatted.length()*2);

        while (matcher.find()) {
            String ac = matcher.group();

            String replacement;

            try {
                Class aoClass = IntactCore.classForAc(getIntactContext(), ac);

                String urlFolderName = null;

                if (Publication.class.isAssignableFrom(aoClass)) {
                    urlFolderName = "publication";
                } else if (Experiment.class.isAssignableFrom(aoClass)) {
                    urlFolderName = "experiment";
                } else if (Interaction.class.isAssignableFrom(aoClass)) {
                    urlFolderName = "interaction";
                } else if (Interactor.class.isAssignableFrom(aoClass)) {
                    urlFolderName = "interactor";
                } else if (Component.class.isAssignableFrom(aoClass)) {
                    urlFolderName = "participant";
                } else if (Feature.class.isAssignableFrom(aoClass)) {
                    urlFolderName = "feature";
                } else if (BioSource.class.isAssignableFrom(aoClass)) {
                    urlFolderName = "organism";
                } else if (CvObject.class.isAssignableFrom(aoClass)) {
                    urlFolderName = "cvobject";
                }

                replacement = "<a href=\""+absoluteContextPath+"/"+urlFolderName+"/"+ac+"\">"+ac+"</a>";

            } catch (Throwable e) {
                addWarningMessage("Accession problem", "Some accession numbers in the note could not be auto-linked because there is no object type for that accession");

                replacement = ac;
            }

            matcher.appendReplacement(sb, replacement);
        }

        matcher.appendTail(sb);

        return sb.toString();
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
