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

import com.google.gdata.client.projecthosting.ProjectHostingService;
import com.google.gdata.data.HtmlTextConstruct;
import com.google.gdata.data.Person;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.projecthosting.*;
import com.google.gdata.util.AuthenticationException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.ac.ebi.intact.editor.config.EditorConfig;
import uk.ac.ebi.intact.editor.controller.BaseController;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.model.user.Preference;
import uk.ac.ebi.intact.model.user.User;

import javax.faces.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
@Scope("session")
public class ErrorController extends BaseController {

    private String title;
    private String explanation;
    private String viewId;
    private String referer;
    private Throwable throwable;

    @Autowired
    private EditorConfig editorConfig;

    @Autowired
    private UserSessionController userSessionController;

    public ErrorController() {
    }

    public void submitIssue(ActionEvent evt) {
        ProjectHostingService service = new ProjectHostingService("intact-editor");
        try {
            service.setUserCredentials(editorConfig.getGoogleUsername(), editorConfig.getGooglePassword());
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Problem with the google credentials to submit the issue: "+editorConfig.getGoogleUsername(), e);
        }

        User currentUser = userSessionController.getCurrentUser();

        // Create the entry to insert
        IssuesEntry entry = new IssuesEntry();
        entry.setTitle(new PlainTextConstruct(title));
        entry.setSummary(new PlainTextConstruct(title));

        entry.setContent(new HtmlTextConstruct(createIssueContent()));

        entry.setStatus(new Status("New"));
        entry.addLabel(new Label("Component-Editor"));
        entry.addLabel(new Label("Priority-Medium"));
        entry.addLabel(new Label("Difficulty-Unknown"));
        entry.addLabel(new Label("Type-Defect"));
        entry.addLabel(new Label("Effect-Unstability"));
        entry.addLabel(new Label("ReportedVersion-" + editorConfig.getBuildVersion()));

        Person author = new Person();
        author.setName(currentUser.getLogin());
        entry.getAuthors().add(author);

        Preference googleUsernamePref = currentUser.getPreference(AbstractUserController.GOOGLE_USERNAME);

        if (googleUsernamePref != null) {
            final Cc cc = new Cc();
            final Username username = new Username(googleUsernamePref.getValue());
            cc.setUsername(username);
            entry.addCc(cc);
        }

        IssuesEntry insertedIssue = null;

        // Ask the service to insert the new entry
        try {
            URL postUrl = new URL("http://code.google.com/feeds/issues/p/intact/issues/full");

            insertedIssue = service.insert(postUrl, entry);

            addMessage("Thanks for submitting the issue", "Submitted issue ID: " + insertedIssue.getIssueId());
        } catch (Exception e) {
            throw new RuntimeException("Problem submitting the issue", e);
        }

        // post the exception
        final List<Throwable> throwables = ExceptionUtils.getThrowableList(throwable);

        for (Throwable t : throwables) {
            // Create the entry to insert
            createComment(service, author, insertedIssue.getIssueId().getValue(), ExceptionUtils.getStackTrace(t));
        }
    }

    private String createIssueContent() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        ps.println("<b>Reporter:</b> "+userSessionController.getCurrentUser().getLogin()+" <br/>");
        ps.println("<b>ViewId:</b> "+viewId+"<br/>");
        ps.println("<b>Referer:</b> "+referer+"<br/>");
        ps.println("<b>Exception message:</b> "+throwable.getMessage()+"<br/>");

        ps.println("<p>"+explanation+"</p>");
        ps.close();

        return baos.toString();
    }

    private void createComment(ProjectHostingService service, Person author, Integer issueId, String commentContent) {
        IssueCommentsEntry comment = new IssueCommentsEntry();
        comment.setContent(new PlainTextConstruct(commentContent));
        comment.getAuthors().add(author);

        try {
            URL postUrl = new URL("http://code.google.com/feeds/issues/p/intact/issues/" + issueId + "/comments/full");
            service.insert(postUrl, comment);
        } catch (Exception e) {
            throw new RuntimeException("Problem attaching exception to the issue "+issueId, e);
        }
    }

    public String createExceptionMessage() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        ps.println("ViewId: " + viewId);
        ps.println("Referer: " + referer);
        ps.println("---------------------------------------------");

        if (throwable != null) throwable.printStackTrace(ps);

        ps.close();

        return baos.toString();
    }

    public boolean isIssueSubmissionAvalable() {
       return (editorConfig.getGoogleUsername() != null && editorConfig.getGoogleUsername().length() > 0);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public EditorConfig getEditorConfig() {
        return editorConfig;
    }

    public void setEditorConfig(EditorConfig editorConfig) {
        this.editorConfig = editorConfig;
    }
}
