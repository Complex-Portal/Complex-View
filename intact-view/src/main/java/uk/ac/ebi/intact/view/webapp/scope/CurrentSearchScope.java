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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.request.SessionScope;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CurrentSearchScope extends SessionScope implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public CurrentSearchScope() {
    }

    public Object get(String name, ObjectFactory objectFactory) {
        String searchQuery = getSearchQuery();

        final String uniqueName = name + "::" + simplify(searchQuery);

        return super.get(uniqueName, objectFactory);
    }

    private String simplify(String searchQuery) {
        if ("*".equals(searchQuery)) {
            return "";
        }
        return searchQuery;
    }

    public Object remove(String name) {
        String searchQuery = getSearchQuery();
        return super.remove(name+"::"+searchQuery);
    }

    private String getSearchQuery() {
        String searchQuery = getUserQuery().getSearchQuery();

        if (searchQuery == null) {
            searchQuery = "*";
        }
        return searchQuery;
    }

    public String getConversationId() {
        String searchQuery = getUserQuery().getSearchQuery();

        if (searchQuery != null) {
            return super.getConversationId()+"::"+searchQuery;
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
