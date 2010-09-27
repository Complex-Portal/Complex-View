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
import org.primefaces.model.LazyDataModel;
import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearcher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.SolrSearchResult;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.view.webapp.util.MitabFunctions;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class LazySearchResultDataModel extends LazyDataModel<IntactBinaryInteraction> {

    private static final Log log = LogFactory.getLog(LazySearchResultDataModel.class);

    private static String DEFAULT_SORT_COLUMN = "rigid";

    private SolrQuery solrQuery;
    private SolrServer solrServer;

    private SolrSearchResult result;

     public LazySearchResultDataModel(SolrServer solrServer, SolrQuery solrQuery) {
        if (solrQuery == null) {
            throw new IllegalArgumentException("Trying to create data model with a null SolrQuery");
        }

        this.solrServer = solrServer;
        this.solrQuery = solrQuery.getCopy();
    }

    @Override
    public List<IntactBinaryInteraction> load(int first, int pageSize, String sortField, boolean sortOrder, Map<String, String> filters) {
        if (solrQuery == null) {
            throw new IllegalStateException("Trying to fetch results for a null SolrQuery");
        }

        solrQuery.setStart(first)
            .setRows(pageSize)
            .setFacet(true)
            .setFacetMissing(true)
            .addFacetField(FieldNames.EXPANSION)
            .addFacetField("interactorType_id");

        if (solrQuery.getSortField() == null) {
            solrQuery.setSortField(DEFAULT_SORT_COLUMN, SolrQuery.ORDER.asc);
        }

        if (log.isDebugEnabled()) {
            try {
                log.debug("Fetching results: "+ URLDecoder.decode(solrQuery.toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        IntactSolrSearcher searcher = new IntactSolrSearcher(solrServer);
        result = searcher.search(solrQuery);

        List<IntactBinaryInteraction> interactions = new ArrayList<IntactBinaryInteraction>(result.getBinaryInteractionList().size());

        for (IntactBinaryInteraction ibi : result.getBinaryInteractionList()) {
            flipIfNecessary(ibi);
            interactions.add(ibi);
        }

        return interactions;
    }

    public int getRowCount() {
        if (result == null) {
            load(0,0, null, false, null);
        }
        return Long.valueOf(result.getTotalCount()).intValue();
    }

    private void flipIfNecessary(IntactBinaryInteraction binaryInteraction) {
        final ExtendedInteractor interactorA = binaryInteraction.getInteractorA();
        final ExtendedInteractor interactorB = binaryInteraction.getInteractorB();

        final boolean matchesA = matchesQuery(interactorA);
        final boolean matchesB = matchesQuery(interactorB);

        if (matchesA && !matchesB) {
            // nothing
        } else if (!matchesA && matchesB) {
            binaryInteraction.flip();
        } else if (!MitabFunctions.isSmallMolecule(interactorA) && MitabFunctions.isSmallMolecule(interactorB)) {
            binaryInteraction.flip();
        } else {
            final String interactorAName = MitabFunctions.getInteractorDisplayName(interactorA);
            final String interactorBName = MitabFunctions.getInteractorDisplayName(interactorB);

            if (interactorAName.compareTo(interactorBName) > 0) {
                binaryInteraction.flip();
            }
        }
    }

    private boolean matchesQuery(ExtendedInteractor interactor) {
        String queries[] = solrQuery.getQuery().split(" ");

        for (String query : queries) {
            if ("NOT".equalsIgnoreCase(query) ||
                "AND".equalsIgnoreCase(query) ||
                "OR".equalsIgnoreCase(query) ||
                 query.contains(":")) {
                continue;
            }
            if (matchesQueryAliases(query, interactor.getAliases())) {
                return true;
            } else if (matchesQueryXrefs(query, interactor.getIdentifiers())) {
                return true;
            }else if (matchesQueryXrefs(query, interactor.getAlternativeIdentifiers())) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesQueryAliases(String query, Collection<Alias> aliases) {
        for (Alias alias : aliases) {
            if (alias.getName().toLowerCase().contains(query.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesQueryXrefs(String query, Collection<CrossReference> xrefs) {
        for (CrossReference xref : xrefs) {
            if (xref.getIdentifier().toLowerCase().contains(query.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean isSameThanPrevious() {
        if (getRowIndex() > 0) {
            final IntactBinaryInteraction previousInteraction = getInteraction(getRowIndex() - 1);
            final IntactBinaryInteraction currentInteraction = getInteraction(getRowIndex());

            final String previousInteractorAName = MitabFunctions.getIntactIdentifierFromCrossReferences(previousInteraction.getInteractorA().getIdentifiers());
            final String previousInteractorBName = MitabFunctions.getIntactIdentifierFromCrossReferences(previousInteraction.getInteractorB().getIdentifiers());
            final String currentInteractorAName = MitabFunctions.getIntactIdentifierFromCrossReferences(currentInteraction.getInteractorA().getIdentifiers());
            final String currentInteractorBName = MitabFunctions.getIntactIdentifierFromCrossReferences(currentInteraction.getInteractorB().getIdentifiers());

            return previousInteractorAName.equalsIgnoreCase(currentInteractorAName) &&
                   previousInteractorBName.equalsIgnoreCase(currentInteractorBName);

        }

        return false;
    }

    private IntactBinaryInteraction getInteraction(int rowIndex) {
        List<IntactBinaryInteraction> interactions = new ArrayList<IntactBinaryInteraction>(result.getBinaryInteractionList());

        final IntactBinaryInteraction binaryInteraction = interactions.get(rowIndex);
        return binaryInteraction;
    }

    public SolrSearchResult getResult() {
        return result;
    }

    public SolrQuery getSearchQuery() {
        return solrQuery;
    }
}