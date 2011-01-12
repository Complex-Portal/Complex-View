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
package uk.ac.ebi.intact.view.webapp.controller.moleculeview;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.view.webapp.controller.ContextController;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;

import javax.faces.context.FacesContext;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("moleculeViewBean")
@Scope("conversation.access")
@ConversationName("general")
public class MoleculeViewController extends JpaBaseController{

    private static final Log log = LogFactory.getLog( MoleculeViewController.class );

    private static final String INTERACTOR_AC_PARAM = "interactorAc";

    private String interactorAc;
    private Interactor interactor;

    public MoleculeViewController() {

    }

    public void loadInteractor() {
        FacesContext context = FacesContext.getCurrentInstance();

        if ( interactorAc != null ) {
            log.debug( "Parameter " + INTERACTOR_AC_PARAM + " was specified" );
            setInteractorAc( interactorAc );

            interactor = getDaoFactory().getInteractorDao().getByAc(interactorAc);

            UserQuery userQuery = (UserQuery) getBean("userQuery");
            SearchController searchController = (SearchController) getBean("searchBean");

            // Update interaction search
            userQuery.reset();
            userQuery.setSearchQuery( "id:" + interactorAc );
            SolrQuery solrQuery = userQuery.createSolrQuery();
            searchController.doBinarySearch( solrQuery );

            ContextController contextController = (ContextController) getBean("contextController");
            contextController.setActiveTabIndex(6);
        }

    }

    public String open(String interactorAc) {
        setInteractorAc(interactorAc);
        return "/pages/molecule/molecule.xhtml?faces-redirect=true&includeViewParams=true";
    }

    public String getInteractorAc() {
        return interactorAc;
    }

    public void setInteractorAc(String interactorAc) {
        this.interactorAc = interactorAc;
    }

    public Interactor getInteractor() {
        return interactor;
    }

    public void setInteractor(Interactor interactor) {
        this.interactor = interactor;
    }
}
