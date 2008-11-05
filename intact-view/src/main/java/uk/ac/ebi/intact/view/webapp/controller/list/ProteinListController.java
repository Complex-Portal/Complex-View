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


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.myfaces.trinidad.context.RequestContext;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.util.ExternalDbLinker;

import javax.faces.event.ActionEvent;
import java.util.List;
import java.util.Set;

/**
 * Controller for ProteinList View
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since 0.9
 */
@Controller
@Scope( "conversation.access" )
public class ProteinListController extends BaseController {

    @Autowired
    private ExternalDbLinker dbLinker;

    public ProteinListController() {
    }

    private String[] getSelectedUniprotIds() {

        final List<IntactBinaryInteraction> interactions = getSelected( SearchController.PROTEINS_TABLE_ID );
        Set<String> uniprotIds = dbLinker.getUniqueUniprotIds( interactions );
        return uniprotIds.toArray( new String[uniprotIds.size()] );
    }

    private String[] getSelectedGeneNames() {

        final List<IntactBinaryInteraction> interactions = getSelected( SearchController.PROTEINS_TABLE_ID );
        Set<String> geneNames = dbLinker.getUniqueGeneNames( interactions );
        return geneNames.toArray( new String[geneNames.size()] );
    }

    public void goDomains( ActionEvent evt ) {
        String[] selectedUniprotIds = getSelectedUniprotIds();
        dbLinker.goExternalLink( dbLinker.INTERPROURL, dbLinker.INTERPRO_SEPERATOR, selectedUniprotIds );
    }

    public void goExpression( ActionEvent evt ) {
        String[] selectedGeneNames = getSelectedGeneNames();
        dbLinker.goExternalLink( dbLinker.EXPRESSIONURL_PREFIX, dbLinker.EXPRESSIONURL_SUFFIX, dbLinker.EXPRESSION_SEPERATOR, selectedGeneNames );
    }

    public void goChromosomalLocation( ActionEvent evt ) {
        String[] selectedUniprotIds = getSelectedUniprotIds();
        dbLinker.goExternalLink( dbLinker.CHROMOSOMEURL, dbLinker.CHROMOSOME_SEPERATOR, selectedUniprotIds );
    }

    public void goReactome( ActionEvent evt ) {
        String[] selected = getSelectedUniprotIds();
        //the carriage return has to be escaped as it is used in the JavaScript
        dbLinker.reactomeLinker( dbLinker.REACTOMEURL, "\\r", selected, "/view/pages/list/protein_list.xhtml" );
    }

    public void rerender(ActionEvent evt) {
        RequestContext requestContext = RequestContext.getCurrentInstance();
        requestContext.addPartialTarget(getComponentFromView("buttonBar"));
    }
}
