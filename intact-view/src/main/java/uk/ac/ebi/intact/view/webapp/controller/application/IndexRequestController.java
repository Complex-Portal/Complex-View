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
package uk.ac.ebi.intact.view.webapp.controller.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.URLEncoder;

import uk.ac.ebi.intact.view.webapp.util.WebappUtils;
import uk.ac.ebi.intact.view.webapp.controller.browse.OntologyTreeModel;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexSearcher;

/**
 * Contains index info.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("request")
public class IndexRequestController extends BaseController {

    @Autowired
    private AppConfigBean appConfigBean;

    private String defaultInteractionIndex;
    private String defaultInteractorIndex;
    private String defaultOntologiesIndex;

    private OntologyIndexSearcher ontologyIndexSearcher;

    public IndexRequestController() {

    }

@PostConstruct
public void init() {
    try {
        String ontologiesIndexDir = WebappUtils.getDefaultOntologiesIndex(appConfigBean.getConfig()).getLocation();
        ontologyIndexSearcher = new OntologyIndexSearcher(ontologiesIndexDir);
        //interactionIndexSearcher = new IndexSearcher(configuration.getDefaultIndexLocation());
        //interactorIndexSearcher = new IndexSearcher(configuration.getDefaultInteractorIndexLocation());
    } catch (Exception e) {
        addErrorMessage("Problem creating ontology index searcher", e.getMessage());
    }
}


    @PreDestroy
    public void destroy() {
        try {
            //interactionIndexSearcher.close();
            //interactorIndexSearcher.close();
            ontologyIndexSearcher.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getDefaultInteractionIndex() {
        if (defaultInteractionIndex == null) {
           defaultInteractionIndex = WebappUtils.getDefaultInteractionIndex(appConfigBean.getConfig()).getLocation();
        }

        return defaultInteractionIndex;
    }

    public String getDefaultInteractorIndex() {
        if (defaultInteractorIndex == null) {
           defaultInteractorIndex = WebappUtils.getDefaultInteractorIndex(appConfigBean.getConfig()).getLocation();
        }

        return defaultInteractorIndex;
    }

    public String getDefaultOntologiesIndex() {
        if (defaultOntologiesIndex == null) {
           defaultOntologiesIndex = WebappUtils.getDefaultOntologiesIndex(appConfigBean.getConfig()).getLocation();
        }

        return defaultOntologiesIndex;
    }

    public OntologyIndexSearcher getOntologyIndexSearcher() {
        return ontologyIndexSearcher;
    }
}