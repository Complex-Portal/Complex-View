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

import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.binarysearch.webapp.generated.Index;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.binarysearch.webapp.generated.User;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.application.AppConfigBean;
import uk.ac.ebi.intact.view.webapp.controller.application.OntologyBean;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import java.io.File;
import java.io.IOException;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("firstTimeBean")
@Scope("request")
@ViewController(viewIds = "/first_time_config.xhtml")
public class FirstTimeBean extends BaseController {

    private User user;
    private String directPassword;

    private Index interactionIndex;
    private Index interactorIndex;
    private Index ontologiesIndex;

    private boolean interactionIndexLocationExists;
    private boolean interactorIndexLocationExists;
    private boolean ontologiesIndexLocationExists;

    @Autowired
    private AppConfigBean configBean;

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    @Autowired
    private OntologyBean ontologyBean;

    public FirstTimeBean() {
        this.user = new User();
        this.interactionIndex = new Index();
        this.interactorIndex = new Index();
        this.ontologiesIndex = new Index();
    }

    @PostConstruct
    public void setup() {
        String interactionIndexLocation = intactViewConfiguration.getDefaultIndexLocation();
        String interactorIndexLocation = intactViewConfiguration.getDefaultInteractorIndexLocation();
        String ontologiesIndexLocation = intactViewConfiguration.getDefaultOntologiesIndexLocation();

        interactionIndex.setLocation(interactionIndexLocation);
        interactionIndex.setDefault(true);

        interactorIndex.setLocation(interactorIndexLocation);
        interactorIndex.setDefault(true);

        ontologiesIndex.setLocation(ontologiesIndexLocation);
        ontologiesIndex.setDefault(true);

        interactionIndexLocationExists = checkLocation(interactionIndex);
        interactorIndexLocationExists = checkLocation(interactorIndex);
        ontologiesIndexLocationExists = checkLocation(ontologiesIndex);
    }

    public void interactionIndexLocationChanged(ValueChangeEvent vce) {
        interactionIndex.setLocation((String) vce.getNewValue());
        interactionIndexLocationExists = checkLocation(interactionIndex);
    }

    public void interactorIndexLocationChanged(ValueChangeEvent vce) {
        interactorIndex.setLocation((String) vce.getNewValue());
        interactorIndexLocationExists = checkLocation(interactorIndex);
    }

    public void ontologiesIndexLocationChanged(ValueChangeEvent vce) {
        ontologiesIndex.setLocation((String) vce.getNewValue());
        ontologiesIndexLocationExists = checkLocation(interactorIndex);
    }

    public String processConfiguration() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        user.setPassword(WebappUtils.encrypt(facesContext, directPassword));

        user.getRoles().add("admin");

        SearchConfig config = new SearchConfig();
        config.getUsers().add(user);

        try {
            interactionIndex.setSize(WebappUtils.countItemsInIndex(interactionIndex.getLocation()));
            interactorIndex.setSize(WebappUtils.countItemsInIndex(interactorIndex.getLocation()));
            ontologiesIndex.setSize(WebappUtils.countItemsInIndex(ontologiesIndex.getLocation()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        config.getInteractionIndices().add(interactionIndex);
        config.getInteractorIndices().add(interactorIndex);
        config.getOntologiesIndices().add(ontologiesIndex);

        WebappUtils.writeConfiguration(config, new File(configBean.getConfigFileLocation()));
        configBean.setConfig(config);

        try {
            ontologyBean.loadOntologies(config);
        } catch (IOException e) {
            addErrorMessage("Problem loading ontologies", e.getMessage());
            e.printStackTrace();
        }

        return "main";
    }

    private boolean checkLocation(Index indexToCheck) {
        boolean indexLocationExists = false;

        final String strLocation = indexToCheck.getLocation();
        if (strLocation == null || strLocation.trim().length() == 0) {
            indexToCheck.setSize(0);
            return indexLocationExists;
        }

        File location = new File(indexToCheck.getLocation());
        indexLocationExists = location.exists() && location.isDirectory() && isLuceneDirectory(indexToCheck);

        if (indexLocationExists) {
            indexToCheck.setName(location.getName());
        }

        return indexLocationExists;
    }

    private boolean isLuceneDirectory(Index indexToCheck) {
        try {
            indexToCheck.setSize(WebappUtils.countItemsInIndex(indexToCheck.getLocation()));
        }
        catch (IOException e) {
            return false;
        }

        return true;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDirectPassword() {
        return directPassword;
    }

    public void setDirectPassword(String directPassword) {
        this.directPassword = directPassword;
    }

    public Index getInteractionIndex() {
        return interactionIndex;
    }

    public void setInteractionIndex(Index interactionIndex) {
        this.interactionIndex = interactionIndex;
    }

    public boolean isInteractionIndexLocationExists() {
        return interactionIndexLocationExists;
    }

    public Index getInteractorIndex() {
        return interactorIndex;
    }

    public void setInteractorIndex(Index interactorIndex) {
        this.interactorIndex = interactorIndex;
    }

    public boolean isInteractorIndexLocationExists() {
        return interactorIndexLocationExists;
    }

    public void setInteractorIndexLocationExists(boolean interactorIndexLocationExists) {
        this.interactorIndexLocationExists = interactorIndexLocationExists;
    }

    public Index getOntologiesIndex() {
        return ontologiesIndex;
    }

    public void setOntologiesIndex(Index ontologiesIndex) {
        this.ontologiesIndex = ontologiesIndex;
    }

    public boolean isOntologiesIndexLocationExists() {
        return ontologiesIndexLocationExists;
    }

    public void setOntologiesIndexLocationExists(boolean ontologiesIndexLocationExists) {
        this.ontologiesIndexLocationExists = ontologiesIndexLocationExists;
    }
}
