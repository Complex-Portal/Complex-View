/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang.StringUtils;

import javax.faces.event.ActionEvent;
import javax.mail.MessagingException;

import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.commons.util.MailSender;

/**
 * TODO comment that class header
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 */
@Controller("contactBean")
@Scope("conversation.access")
public class ContactController extends BaseController {

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    private String type;
    private String userEmail;
    private String mainMessage;
    private String contextualMessage;

    public ContactController() {
        type = "bug";
    }

    public void sendEmail( ActionEvent event ) {
        String mailContent = prepareMessage();

        MailSender mailSender = new MailSender( MailSender.EBI_SETTINGS );
        try {
            mailSender.postMail( intactViewConfiguration.getMailRecipients().split(","),
                                 "["+intactViewConfiguration.getWebappName()+"] "+ StringUtils.capitalize(type), mailContent, userEmail );
            addInfoMessage( "Your email has been sent.", "Our support team is going to answer as soon as possible." );
        } catch ( MessagingException e ) {
            e.printStackTrace();
            addErrorMessage( "Your email hasn't been sent.", e.getMessage() );
        }
    }

    private String prepareMessage() {
        StringBuilder sb = new StringBuilder(2048);

        sb.append("<b>Webapp</b>: "+intactViewConfiguration.getWebappName()+"<br/>");
        sb.append("<b>Version</b>: "+intactViewConfiguration.getWebappVersion()+"<br/>");
        sb.append("<b>Build number</b>: "+intactViewConfiguration.getWebappBuildNumber()+"<br/>");
        sb.append("<b>Reporter</b>: "+userEmail+"<br/>");

        if (contextualMessage != null) {
            sb.append("<br/>");
            sb.append("<b>What was the user doing?</b><br/>");
            sb.append("<pre>"+contextualMessage+"</pre><br/>");
        }

        sb.append("<br/>");
        sb.append("<b>Description</b><br/>");
        sb.append("<pre>"+mainMessage+"</pre><br/>");

        return sb.toString();
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail( String userEmail ) {
        this.userEmail = userEmail;
    }

    public String getMainMessage() {
        return mainMessage;
    }

    public void setMainMessage( String mainMessage) {
        this.mainMessage = mainMessage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContextualMessage() {
        return contextualMessage;
    }

    public void setContextualMessage(String contextualMessage) {
        this.contextualMessage = contextualMessage;
    }
}
