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
package uk.ac.ebi.intact.view.webapp.controller.list;

import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.browse.BrowseController;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.util.Functions;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("conversation.access")
public class ProteinListController extends BaseController {

    public ProteinListController() {
    }

    private String[] getSelectedUniprotIds() {

        final List<IntactBinaryInteraction> interactions = getSelected(SearchController.PROTEINS_TABLE_ID);

        Set<String> uniprotIds = new HashSet<String>();

        for (IntactBinaryInteraction interaction : interactions) {
            String uniprotId = Functions.getUniprotIdentifierFromCrossReferences(interaction.getInteractorA().getIdentifiers());

            if (uniprotId != null) {
                uniprotIds.add(uniprotId);
            }
        }

        return uniprotIds.toArray(new String[uniprotIds.size()]);
    }

    public void goDomains(ActionEvent evt) {
        String[] selectedUniprotIds = getSelectedUniprotIds();

        // externalLinker.goExternalLink(selectedUniprotIds);
        
        goExternalLink(BrowseController.INTERPROURL, selectedUniprotIds);
    }

    public void goExternalLink(String baseUrl, String[] selected) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExtendedRenderKitService service = Service.getRenderKitService(facesContext, ExtendedRenderKitService.class);

        if (selected.length > 0) {
            String url = baseUrl + StringUtils.join(selected, ',');
            service.addScript(facesContext, "window.open('"+url+"');");
        } else {
            service.addScript(facesContext, "alert('Selection is empty');");
        }
    }

    public void rerender(ActionEvent evt) {
        RequestContext requestContext = RequestContext.getCurrentInstance();
        requestContext.addPartialTarget(getComponentFromView("lalal"));
        requestContext.addPartialTarget(getComponentFromView("buttonBar"));
    }
}
