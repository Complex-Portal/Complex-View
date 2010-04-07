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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.users.model.User;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "singleton" )
public class AppController extends BaseController {

    private Collection<User> loggedInUsers;

    private String database;

    public AppController() {
        loggedInUsers = new ArrayList<User>();
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase( String database ) {
        this.database = database;
    }

    public Collection<User> getLoggedInUsers() {
        return loggedInUsers;
    }

    public void setLoggedInUsers( Collection<User> loggedInUsers ) {
        this.loggedInUsers = loggedInUsers;
    }

    public String getLoggedInUserCount() {
        return String.valueOf( loggedInUsers.size() );
    }
}
