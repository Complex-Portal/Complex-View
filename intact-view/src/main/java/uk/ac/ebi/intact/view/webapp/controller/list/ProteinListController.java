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
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.context.RequestContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.InteractorXref;
import uk.ac.ebi.intact.view.webapp.model.InteractorWrapper;

import javax.faces.event.ActionEvent;
import java.util.*;

/**
 * Controller for ProteinList View
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since 0.9
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class ProteinListController extends InteractorListController {

    public ProteinListController() {
    }

    public String[] getSelectedUniprotIds() {
        final InteractorWrapper[] selected = getSelected();

        if (selected == null) {
            return new String[0];
        }

        final List<InteractorWrapper> interactorWrappers = Arrays.asList(selected);

        Set<String> uniprotIds = new HashSet<String>();

        for (InteractorWrapper interactorWrapper : interactorWrappers) {

            if (interactorWrapper != null) {
                Collection<InteractorXref> interactorXrefs = interactorWrapper.getXrefs();

                InteractorXref interactorXref = getUniprotXrefFrom(interactorXrefs);

                if (interactorXref != null) {
                    uniprotIds.add(interactorXref.getPrimaryId());
                }
            }
        }

        return uniprotIds.toArray( new String[uniprotIds.size()] );
    }

    private InteractorXref getUniprotXrefFrom(Collection<InteractorXref> interactorXrefs) {
        for (InteractorXref xref : interactorXrefs) {
            CvXrefQualifier qualifier = xref.getCvXrefQualifier();
            if (qualifier != null) {
                String qualifierIdentity = qualifier.getIdentifier();
                if (qualifierIdentity != null && CvXrefQualifier.IDENTITY_MI_REF.equals(qualifierIdentity)) {
                    CvDatabase database = xref.getCvDatabase();
                    String databaseIdentity = database.getIdentifier();
                    if (databaseIdentity != null && CvDatabase.UNIPROT_MI_REF.equals(databaseIdentity)) {
                        return xref;
                    }
                }
            }
        }
        return null;
    }

    public void openInReactome(ActionEvent evt) {
        RequestContext context = RequestContext.getCurrentInstance();

        if (getSelected().length > 0) {
            context.execute("ia_submitToReactome('"+ StringUtils.join(getSelectedUniprotIds(), ",")+"')");
        } else {
            alertNoSelection();
        }
    }

    private void alertNoSelection() {
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("alert('Select at least a protein in the table below')");
    }

    public void openInInterpro(ActionEvent evt) {
        String url = "http://www.ebi.ac.uk/interpro/IProtein?ac="+StringUtils.join(getSelectedUniprotIds(), ",");
        executeUrlRedirection(url);
    }

    public void openInEnsembl(ActionEvent evt) {
        String url = "http://www.ensembl.org/Homo_sapiens/featureview?type=ProteinAlignFeature;id="+StringUtils.join(getSelectedUniprotIds(), ";id=");
        executeUrlRedirection(url);
    }

    public void openInArrayExpress(ActionEvent evt) {
        String url = "http://www.ebi.ac.uk/gxa/query?geneQuery="+StringUtils.join(getSelectedUniprotIds(), "+");
        executeUrlRedirection(url);
    }

    private void executeUrlRedirection(String url) {
        RequestContext context = RequestContext.getCurrentInstance();

        if (getSelected().length > 0) {
            context.execute("var win = window.open('','_blank'); win.location='" + url + "';");
        } else {
            alertNoSelection();
        }

    }
}
