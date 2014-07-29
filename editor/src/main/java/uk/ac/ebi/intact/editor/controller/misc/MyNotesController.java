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
import org.joda.time.DateTime;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.controller.admin.UserAdminController;
import uk.ac.ebi.intact.editor.util.LazyDataModelFactory;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.user.Preference;
import uk.ac.ebi.intact.model.user.User;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.DataModel;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "session" )
public class MyNotesController extends JpaAwareController {

    private static final Log log = LogFactory.getLog(MyNotesController.class);
    private static final int MAX_RESULTS = 200;

    private String rawNotes;
    private String formattedNotes;
    private boolean editMode;
    private String absoluteContextPath;
    private List<QueryMacro> queryMacros;

    @Autowired
    private UserSessionController userSessionController;

    public MyNotesController() {
        this.queryMacros = new ArrayList<QueryMacro>();

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        this.absoluteContextPath = request.getScheme() + "://" +
               request.getServerName() + ":" +
               request.getServerPort() +
               request.getContextPath();
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void loadPage(ComponentSystemEvent evt) {
        User user = userSessionController.getCurrentUser(true);

        Preference pref = user.getPreference(UserAdminController.RAW_NOTES);

        if (pref == null) {
            pref = new Preference(user, UserAdminController.RAW_NOTES);
            pref.setValue("These are your notes. You can write anything you wish here. \nYou can link to publications like EBI-2928483 or an interaction EBI-2928497.\n" +
                    "You can use it as well for experiments, participants, etc.");

            user.getPreferences().add(pref);

            getDaoFactory().getPreferenceDao().persist(pref);
        }

        rawNotes = pref.getValue();

        if (rawNotes == null) {
            rawNotes = "";
        }

        processNotes();
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED)
    public void saveNotes(ActionEvent evt) {
        User user = userSessionController.getCurrentUser();

        Preference pref = user.getPreference(UserAdminController.RAW_NOTES);
        pref.setValue(rawNotes);

        getDaoFactory().getPreferenceDao().update(pref);

        processNotes();

        editMode = false;
    }

    public void copyToNotes(String newContent, String message) {
        addInfoMessage("Information copied", message);

        rawNotes = rawNotes + "\n\n=== Copied "+new DateTime().toString()+" ===\n\n"+newContent;

        saveNotes(null);
    }

    private void processNotes() {
        queryMacros.clear();

        if (rawNotes == null) {
            rawNotes = "";
        }

        String[] lines = rawNotes.split("\n");

        String acPrefix = getIntactContext().getConfig().getAcPrefix();
        Pattern acPattern = Pattern.compile(acPrefix + "-\\d+");

        Pattern macroPattern = Pattern.compile("\\{(\\w+):(\\w+)\\s(.+)\\}");

        StringBuilder sb = new StringBuilder();

        for (String line : lines) {

            if (line.trim().startsWith("{")) {
                line = "<strong>"+processMacro(line, macroPattern)+"</strong>";
            } else {
                line = replaceACs(line, acPattern);
            }

            sb.append(line+"<br/>");
        }

        formattedNotes = sb.toString();
    }

    private String processMacro(String line, Pattern pattern) {
        String macro = line.trim();

        Matcher matcher = pattern.matcher(macro);

        String outcome = "";

        while (matcher.find()) {
            if (matcher.groupCount() < 3) {
                outcome = "[invalid macro: "+macro+"]";
            } else {
                String macroType = matcher.group(1);
                String macroName = matcher.group(2);
                String macroStatement = matcher.group(3);

                if ("query".equals(macroType)) {
                    if (!macroStatement.toLowerCase().startsWith("select")) {
                        outcome = "[only select queries allowed]";
                    } else {
                        try {
                            DataModel results = createDataModel(macroStatement);

                            QueryMacro queryMacro = new QueryMacro(macroName, macroStatement, results);
                            queryMacros.add(queryMacro);

                            outcome = "<a href=\""+absoluteContextPath+"/notes/query/"+macroName+"\">[query: "+macroName+"]</a><br/>";
                        } catch (Exception e) {
                            addErrorMessage("Cannot run query: "+macroName, e.getMessage());
                            outcome = "[query cannot be run: "+macroName+"]";
                        }
                    }


                } else {
                    outcome = "[macro with unexpected type: "+macroName+"]";
                    addErrorMessage("Invalid macro", "Macro with unexpected type: "+macroName);
                }
            }
        }

        return outcome;
    }

    private DataModel createDataModel(String hqlQuery) {
        LazyDataModel dataModel = LazyDataModelFactory.createLazyDataModel(getCoreEntityManager(), hqlQuery);
        return dataModel;
    }

    public String getFormattedNotes() {
        return formattedNotes;
    }

    private String replaceACs(String line, Pattern pattern) {
        Matcher matcher = pattern.matcher(line);

        StringBuffer sb = new StringBuffer(line.length()*2);

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
                addWarningMessage("Accession problem: "+ac, "Some accession numbers in the note could not be auto-linked because there is no object type for that accession, or it does not exist in the database");

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

    public List<QueryMacro> getQueryMacros() {
        return queryMacros;
    }

    public void setQueryMacros(List<QueryMacro> queryMacros) {
        this.queryMacros = queryMacros;
    }

    public static void main(String[] args) {
        Pattern p = Pattern.compile("\\{(\\w+):(\\w+)\\s(.+)\\}");

        Matcher matcher = p.matcher("{query:Lalalala select exp from Experiment exp where exp.bioSource.cvTissue.ac = 'EBI-2609142'}");

        while (matcher.find()) {
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(2));
            System.out.println(matcher.group(3));
        }
    }
}
