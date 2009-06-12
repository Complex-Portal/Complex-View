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

import org.springframework.stereotype.Controller;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;

import javax.faces.context.FacesContext;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("moleculeViewBean")
@Scope("conversation.access")
@ConversationName("general")
@ViewController( viewIds = {"/pages/molecule/molecule.xhtml"} )
public class MoleculeViewController extends JpaBaseController{

    private static final Log log = LogFactory.getLog( MoleculeViewController.class );

    private static final String INTERACTOR_AC_PARAM = "interactorAc";

    private Interactor interactor;

    @Autowired
    private SearchController searchController;

    @Autowired
    private UserQuery userQuery;

    public MoleculeViewController() {

    }

    @PreRenderView
    public void initialParams() {
        FacesContext context = FacesContext.getCurrentInstance();
        final String interactorAc = context.getExternalContext().getRequestParameterMap().get(INTERACTOR_AC_PARAM);

        if ( interactorAc != null ) {
            log.debug( "Parameter " + INTERACTOR_AC_PARAM + " was specified" );
            setInteractorAc( interactorAc );

            // Update interaction search
            userQuery.reset();
            userQuery.setSearchQuery( "id:" + interactorAc );
            SolrQuery solrQuery = userQuery.createSolrQuery();
            searchController.doBinarySearch( solrQuery );

        }

    }

    public void setInteractorAc(String ac) {
        interactor = getDaoFactory().getInteractorDao().getByAc(ac);
    }

    public Interactor getInteractor() {
        return interactor;
    }

    public void setInteractor(Interactor interactor) {
        this.interactor = interactor;
    }
}
