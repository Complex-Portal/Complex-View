/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.application.editor.hibernate;

import uk.ac.ebi.intact.context.UserContext;

import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionEvent;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ConnectionSessionListener implements HttpSessionListener {

    public void sessionCreated(HttpSessionEvent se) {
        // nothing
        System.out.println("Session started: ");
        UserContext userContext = (UserContext) se.getSession().getAttribute(UserContext.class.getName());
        if (userContext != null) {
            System.out.println("USER: "+userContext.getUserId());
        }
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        UserContext userContext = (UserContext) se.getSession().getAttribute(UserContext.class.getName());
        String user = userContext.getUserId();
        ConnectionManager.getInstance(se.getSession().getServletContext()).evictConnectionForUser(user);
    }
}