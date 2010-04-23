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
package uk.ac.ebi.intact.view.webapp.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CurrentSearchScope implements Scope, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private Object current;
    private String currentQuery;

    public CurrentSearchScope() {
    }

    public Object get(String name, ObjectFactory objectFactory) {
        String searchQuery = getUserQuery().getSearchQuery();

        if (searchQuery == null) {
            searchQuery = "*";
        }

        if (current == null || !searchQuery.equals(currentQuery)) {
            current = objectFactory.getObject();
            currentQuery = searchQuery;
        }

        return current;
    }

    public Object remove(String name) {
        Object obj = current;
        current = null;
        currentQuery = null;

        return obj;
    }

    public void registerDestructionCallback(String name, Runnable callback) {
         // nothing
    }

    public String getConversationId() {
        if (currentQuery != null) {
            return currentQuery;
        }
        return null;
    }

    public UserQuery getUserQuery() {
        return (UserQuery) applicationContext.getBean("userQuery");
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
