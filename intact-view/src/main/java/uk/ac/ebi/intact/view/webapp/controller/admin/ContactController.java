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

import javax.faces.event.ActionEvent;
import javax.mail.MessagingException;

import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.util.MailSender;

/**
 * TODO comment that class header
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 */
@Controller("contactBean")
@Scope("request")
public class ContactController extends BaseController {

    private String userEmail;
    private String mailContent;

    public ContactController() {
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail( String userEmail ) {
        this.userEmail = userEmail;
    }

    public String getMailContent() {
        return mailContent;
    }

    public void setMailContent( String mailContent ) {
        this.mailContent = mailContent;
    }

    public void sendEmail( ActionEvent event ) {
        MailSender mailSender = new MailSender( );
        try {
            mailSender.postMail( new String[]{ "skerrien@ebi.ac.uk" }, "web site email", mailContent, userEmail );
            addInfoMessage( "Your email has been sent.", "Our support team is going to answer as soon as possible." );
        } catch ( MessagingException e ) {
            addErrorMessage( "Your email hasn't been sent.", e.getMessage() );
        }
    }
}
