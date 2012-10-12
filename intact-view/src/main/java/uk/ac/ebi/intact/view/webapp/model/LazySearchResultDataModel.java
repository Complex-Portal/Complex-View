/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.params.FacetParams;
import org.hupo.psi.mi.psicquic.model.PsicquicSolrException;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearchResult;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearcher;
import uk.ac.ebi.intact.view.webapp.util.MitabFunctions;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class LazySearchResultDataModel extends LazyDataModel<BinaryInteraction> {

    private static final Log log = LogFactory.getLog(LazySearchResultDataModel.class);

    private SolrQuery solrQuery;
    private IntactSolrSearcher solrSearcher;

    private IntactSolrSearchResult result;

    private List<BinaryInteraction> binaryInteractions;
    private int numberOfBinaryInteractionsToShow;

    public LazySearchResultDataModel(SolrServer solrServer, SolrQuery solrQuery) {
        this.solrSearcher = new IntactSolrSearcher(solrServer);
        this.solrQuery = solrQuery != null ? solrQuery.getCopy() : null;

        prepareNegativeSolrQuery();
    }

    @Override
    public List<BinaryInteraction> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        if (solrQuery == null) {

            // add a error message
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null){
                FacesMessage facesMessage = new FacesMessage( FacesMessage.SEVERITY_ERROR, "Query is empty", "Cannot fetch any results because query is empty. Please enter a query." );
                context.addMessage( null, facesMessage );
            }

            return Collections.EMPTY_LIST;
        }
        else {

            this.binaryInteractions = null;
            prepareSolrQuery(first, pageSize, sortField, sortOrder);

            List<BinaryInteraction> interactions = new ArrayList<BinaryInteraction>(pageSize);

            if (result != null) {
                try {
                    for (BinaryInteraction<Interactor> ibi : result.getBinaryInteractionList()) {
                        flipIfNecessary(ibi);
                        interactions.add(ibi);
                    }
                } catch (PsimiTabException e) {
                    FacesContext context = FacesContext.getCurrentInstance();
                    if (context != null){
                        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Temporarily impossible to retrieve results", solrQuery.getQuery());
                        context.addMessage(null, facesMessage);
                    }

                    log.fatal("Impossible to retrieve results for query " + solrQuery.getQuery(), e);
                } catch (IOException e) {
                    FacesContext context = FacesContext.getCurrentInstance();
                    if (context != null){
                        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Temporarily impossible to retrieve results", solrQuery.getQuery());
                        context.addMessage(null, facesMessage);
                    }
                    log.fatal("Impossible to retrieve results for query " + solrQuery.getQuery(), e);
                }
            }

            this.binaryInteractions = interactions;

            return interactions;
        }
    }

    private void prepareSolrQuery(int first, int pageSize, String sortField, SortOrder sortOrder) {
        SolrQuery copyQuery = solrQuery.getCopy();
        copyQuery.setStart(first)
                .setRows(pageSize)
                .setFacet(true)
                .setFacetMissing(false)
                .addFacetField(FieldNames.COMPLEX_EXPANSION_FACET)
                .addFacetField(FieldNames.NEGATIVES_FACET);

        // add some limit to faceting for performances improvement
        copyQuery.set(FacetParams.FACET_OFFSET, 0);
        // we know that we have only 4 type of expansion methods : spoke expanded, no expansion, matrix or bipartite
        copyQuery.setFacetLimit(4);

        // sort by intact mi score desc
        if (copyQuery.getSortField() == null && sortField != null) {
            if (sortOrder.equals(SortOrder.ASCENDING)){
                copyQuery.setSortField(sortField, SolrQuery.ORDER.asc);
            }
            else if (sortOrder.equals(SortOrder.DESCENDING)){
                copyQuery.setSortField(sortField, SolrQuery.ORDER.desc);
            }
        }

        if (log.isDebugEnabled()) {
            try {
                log.debug("Fetching results: "+ URLDecoder.decode(copyQuery.toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.fatal(e);
            }
        }

        try {
            result = solrSearcher.search(copyQuery);
        } catch (PsicquicSolrException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null){
                FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Temporarily impossible to retrieve results", copyQuery.getQuery());
                context.addMessage(null, facesMessage);
            }

            log.fatal("Impossible to retrieve results for query " + copyQuery.getQuery(), e);
            result = null;
        } catch (SolrServerException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null){
                FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Temporarily impossible to retrieve results", copyQuery.getQuery());
                context.addMessage(null, facesMessage);
            }
            log.fatal("Impossible to retrieve results for query " + copyQuery.getQuery(), e);
            result = null;
        }
    }

    private void prepareNegativeSolrQuery() {
        SolrQuery copyQuery = solrQuery.getCopy();
        copyQuery.setStart(0)
                .setRows(0);

        // add negative filter
        copyQuery.addFilterQuery(FieldNames.NEGATIVE+":true");

        try {
            IntactSolrSearchResult negativeResults = solrSearcher.search(copyQuery);
            this.numberOfBinaryInteractionsToShow = Long.valueOf(negativeResults.getNumberResults()).intValue();

        } catch (PsicquicSolrException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null){
                FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Temporarily impossible to count negative interactions", copyQuery.getQuery());
                context.addMessage(null, facesMessage);
            }

            log.fatal("Impossible to retrieve results for query " + copyQuery.getQuery(), e);
            this.numberOfBinaryInteractionsToShow = 0;
        } catch (SolrServerException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null){
                FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Temporarily impossible to count negative interactions", copyQuery.getQuery());
                context.addMessage(null, facesMessage);
            }
            log.fatal("Impossible to retrieve results for query " + copyQuery.getQuery(), e);
            this.numberOfBinaryInteractionsToShow = 0;
        }
    }

    public int getRowCount() {
        if (result == null) {
            load(0,0, null, SortOrder.ASCENDING, null);
        }
        if (result != null){
            return Long.valueOf(result.getNumberResults()).intValue();
        }

        return 0;
    }

    private void flipIfNecessary(BinaryInteraction<Interactor> binaryInteraction) {
        final Interactor interactorA = binaryInteraction.getInteractorA();
        final Interactor interactorB = binaryInteraction.getInteractorB();

        final boolean matchesA = matchesQuery(interactorA);
        final boolean matchesB = matchesQuery(interactorB);

        if (matchesA && !matchesB) {
            // nothing
        } else if (!matchesA && matchesB) {
            binaryInteraction.flip();
        }
        else {
            String id1 = interactorA != null ? (!interactorA.getIdentifiers().isEmpty()  ? interactorA.getIdentifiers().iterator().next().getIdentifier() : "") : "";
            String id2 = interactorB != null ? (!interactorB.getIdentifiers().isEmpty()  ? interactorB.getIdentifiers().iterator().next().getIdentifier() : "") : "";

            int comp = id1.compareTo(id2);
            if (comp > 0){
                binaryInteraction.flip();
            }
        }
    }

    private boolean matchesQuery(Interactor interactor) {
        String queries[] = solrQuery.getQuery().split(" ");

        for (String query : queries) {
            if ("NOT".equalsIgnoreCase(query) ||
                "AND".equalsIgnoreCase(query) ||
                "OR".equalsIgnoreCase(query)) {
                continue;
            }

            if (matchesQueryAliases(query, interactor.getAliases())) {
                return true;
            } else if (matchesQueryXrefs(query, interactor.getIdentifiers())) {
                return true;
            }else if (matchesQueryXrefs(query, interactor.getAlternativeIdentifiers())) {
                return true;
            }
            else if (matchesQueryXrefs(query, interactor.getXrefs())) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesQueryAliases(String query, Collection<Alias> aliases) {
        for (Alias alias : aliases) {
            if (query.toLowerCase().toLowerCase().endsWith(alias.getName().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesQueryXrefs(String query, Collection<CrossReference> xrefs) {
        for (CrossReference xref : xrefs) {
            if (query.toLowerCase().endsWith(xref.getIdentifier().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public IntactSolrSearchResult getResult() {
        return result;
    }

    public SolrQuery getSearchQuery() {
        return solrQuery;
    }

    public boolean isSameThanPrevious() {
        if (getRowIndex() > 0) {
            final BinaryInteraction previousInteraction = getInteraction(getRowIndex() - 1);
            final BinaryInteraction currentInteraction = getInteraction(getRowIndex());

            String previousInteractorAName = previousInteraction.getInteractorA() != null ? MitabFunctions.getIntactIdentifierFromCrossReferences(previousInteraction.getInteractorA().getAlternativeIdentifiers()) : null;
            String previousInteractorBName = previousInteraction.getInteractorB() != null ? MitabFunctions.getIntactIdentifierFromCrossReferences(previousInteraction.getInteractorB().getAlternativeIdentifiers()) : null;
            String currentInteractorAName = previousInteraction.getInteractorB() != null ? MitabFunctions.getIntactIdentifierFromCrossReferences(currentInteraction.getInteractorA().getAlternativeIdentifiers()) : null;
            String currentInteractorBName = previousInteraction.getInteractorB() != null ? MitabFunctions.getIntactIdentifierFromCrossReferences(currentInteraction.getInteractorB().getAlternativeIdentifiers()) : null;


            return areEquals(previousInteractorAName, currentInteractorAName) &&
                    areEquals(previousInteractorBName, currentInteractorBName)
                    ;

        }

        return false;
    }

    public boolean isNegative() {
        final BinaryInteraction currentInteraction = getInteraction(getRowIndex());

        return currentInteraction.isNegativeInteraction();
    }

    private boolean areEquals(String name1, String name2){
        if (name1 == null && name2 == null){
            return true;
        }
        else if (name1 != null && name2 != null){
            return name1.equalsIgnoreCase(name2);
        }

        return false;
    }

    private BinaryInteraction getInteraction(int rowIndex) {
        final BinaryInteraction binaryInteraction = binaryInteractions.get(rowIndex);
        return binaryInteraction;
    }

    public int getNumberOfBinaryInteractionsToShow() {
        return numberOfBinaryInteractionsToShow;
    }
}