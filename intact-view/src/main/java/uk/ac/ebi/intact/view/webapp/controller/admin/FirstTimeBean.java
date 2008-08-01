/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.controller.admin;

import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.view.webapp.controller.application.AppConfigBean;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.orchestra.viewController.annotations.InitView;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
//@Controller("firstTimeBean")
//@Scope("request")
@ViewController(viewIds = "/first_time_config.xhtml")
public class FirstTimeBean implements Serializable {

    private SearchConfig config;
    private SearchConfig.Users.User user;
    private String directPassword;

    private SearchConfig.Indexes.Index index;

    private boolean indexLocationExists;

    @InitView
    public void hey() {
        System.out.println("HEY!!");
    }

    public FirstTimeBean() {
        FacesContext context = FacesContext.getCurrentInstance();

        this.user = new SearchConfig.Users.User();
        this.index = new SearchConfig.Indexes.Index();

        String indexLocation = context.getExternalContext().getInitParameter(AppConfigBean.DEFAULT_INDEX_LOCATION_INIT_PARAM);

        index.setLocation(indexLocation);
        index.setDefault(true);

        checkLocation(indexLocation);
    }

    public void indexLocationChanged(ValueChangeEvent vce) {
        index.setLocation((String) vce.getNewValue());
        checkLocation(index.getLocation());
    }

    public String processConfiguration() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        user.setPassword(WebappUtils.encrypt(facesContext, directPassword));

        SearchConfig.Users.User.Roles roles = new SearchConfig.Users.User.Roles();
        roles.getRoles().add("admin");

        user.setRoles(roles);

        config = new SearchConfig();

        SearchConfig.Users users = new SearchConfig.Users();
        users.getUsers().add(user);
        config.setUsers(users);

        SearchConfig.Indexes indexes = new SearchConfig.Indexes();
        indexes.getIndices().add(index);

        try {
            index.setSize(WebappUtils.countItemsInIndex(index.getLocation()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        config.setIndexes(indexes);

        AppConfigBean configBean = (AppConfigBean) BeanHelper.getManagedBean(facesContext, "appConfigBean");

        WebappUtils.writeConfiguration(config, new File(configBean.getConfigFileLocation()));
        configBean.setConfig(config);

        return "main";
    }

    private void checkLocation(String strLocation) {
        if (strLocation == null) {
            indexLocationExists = false;
            index.setSize(0);
            return;
        }
        if (strLocation.trim().length() == 0) {
            indexLocationExists = false;
            index.setSize(0);
            return;
        }

        File location = new File(strLocation);
        indexLocationExists = location.exists() && location.isDirectory() && isLuceneDirectory(location);

        if (indexLocationExists) {
            index.setName(location.getName());
        }
    }

    private boolean isLuceneDirectory(File directory) {
        try {
            index.setSize(WebappUtils.countItemsInIndex(directory.toString()));
        }
        catch (IOException e) {
            return false;
        }

        return true;
    }

    public SearchConfig.Users.User getUser() {
        return user;
    }

    public void setUser(SearchConfig.Users.User user) {
        this.user = user;
    }

    public String getDirectPassword() {
        return directPassword;
    }

    public void setDirectPassword(String directPassword) {
        this.directPassword = directPassword;
    }

    public SearchConfig.Indexes.Index getIndex() {
        return index;
    }

    public void setIndex(SearchConfig.Indexes.Index index) {
        this.index = index;
    }

    public boolean isIndexLocationExists() {
        return indexLocationExists;
    }

    public SearchConfig getConfig() {
        return config;
    }

    public void setConfig(SearchConfig config) {
        this.config = config;
    }
}
