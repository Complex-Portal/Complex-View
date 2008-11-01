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
package uk.ac.ebi.intact.view.webapp.util;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.model.CvAliasType;
import uk.ac.ebi.intact.model.CvInteractorType;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.InteractorAlias;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.psimitab.search.IntactSearchEngine;
import uk.ac.ebi.intact.view.webapp.controller.SearchWebappException;
import uk.ac.ebi.intact.view.webapp.controller.browse.OntologyTermWrapper;

import java.util.Collection;
import java.util.Map;

/**
 * Functions to be used in the UI to control the display in interactions_tab
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */
public final class MitabFunctions {

    private static final Log log = LogFactory.getLog( MitabFunctions.class );


    private static final String PROTEIN_MI_REF = "MI:0326";
    private static final String SMALLMOLECULE_MI_REF = "MI:0328";

    private static Map interactorCountCache = new LRUMap(2500);
    private static Map interactionCountCache = new LRUMap(2500);

    private MitabFunctions() {
    }

    public static boolean isProtein( ExtendedInteractor interactor ) {

        if ( interactor.getInteractorType() != null ) {
            return ( PROTEIN_MI_REF.equals( interactor.getInteractorType().getIdentifier() ) );
        }

        return false;
    }

    public static boolean isSmallMolecule( ExtendedInteractor interactor ) {

        if ( interactor.getInteractorType() != null ) {
            return ( SMALLMOLECULE_MI_REF.equals( interactor.getInteractorType().getIdentifier() ) );
        }

        return false;
    }

    public static int countHits(String searchQuery, String directory) {

        try {
            IndexSearcher searcher = new IndexSearcher(directory);

            String[] defaultFields = new IntactSearchEngine("").getSearchFields();

            long startTime = System.currentTimeMillis();

            QueryParser parser = new MultiFieldQueryParser(defaultFields, new StandardAnalyzer());

            Query query = parser.parse(searchQuery);
            Hits hits = searcher.search(query);

            if ( log.isTraceEnabled() ) {
                log.trace("Counted: "+query.toString()+" - "+hits.length()+" / Elapsed time: "+(System.currentTimeMillis()-startTime)+" ms - Directory: "+directory  );
            }

            int count = hits.length();

            searcher.close();

            return count;

        } catch (Exception e) {
            throw new SearchWebappException("Cannot count hits using query: "+searchQuery+" / in index: "+directory, e);
        }
    }

    public static OntologyTermWrapper populateCounts(OntologyTermWrapper otw, String interactorDirectory, String interactionDirectory) {
        int interactorCount;
        int interactionCount = 0;

        if (otw.getInteractorCount() > 0) {
            // counts have been calculated already, stop here.
            return otw;
        }

        // (?) do we have a field that contains the expanded version of properties of A, if not, we need an extra column that contains this expansion for the protein query.

        String proteinSearchQuery = otw.getSearchQuery()+" AND typeA:\""+ CvInteractorType.PROTEIN_MI_REF+"\"";

        if ( log.isTraceEnabled() ) {
            log.trace( "ProteinSearchQuery: " +proteinSearchQuery );
        }

        if (interactorCountCache.containsKey(proteinSearchQuery)) {
            interactorCount = (Integer) interactorCountCache.get(proteinSearchQuery);
        } else {
            interactorCount = countHits(proteinSearchQuery, interactorDirectory);

            interactorCountCache.put(proteinSearchQuery, interactorCount);
        }

        if (interactorCount > 0) {
            if ( log.isTraceEnabled() ) {
                log.trace(" InteractionSearchQuery: " + otw.getSearchQuery()  );
            }

            if (interactionCountCache.containsKey(otw.getSearchQuery())) {
                interactionCount = (Integer) interactionCountCache.get(otw.getSearchQuery());
            } else {
                interactionCount = countHits(otw.getSearchQuery(), interactionDirectory);

                interactionCountCache.put(otw.getSearchQuery(), interactionCount);
            }
        }

        otw.setInteractorCount(interactorCount);
        otw.setInteractionCount(interactionCount);

        return otw;
    }

    public static String getIntactIdentifierFromCrossReferences(Collection xrefs) {
        return getIdentifierFromCrossReferences(xrefs, "intact");
    }

     public static String getUniprotIdentifierFromCrossReferences(Collection xrefs) {
        return getIdentifierFromCrossReferences(xrefs, "uniprotkb");
    }

    public static String getChebiIdentifierFromCrossReferences(Collection xrefs) {
        return getIdentifierFromCrossReferences(xrefs, "chebi");
    }

    public static String getIdentifierFromCrossReferences(Collection xrefs, String databaseLabel) {
        for (CrossReference xref : (Collection<CrossReference>) xrefs) {
            if (databaseLabel.equals(xref.getDatabase())) {
                return xref.getIdentifier();
            }
        }
        return null;
    }

    /**
     * Gets the name for a protein, getting the first available after evaluating in this order:
     * gene name > commercial name > synonim > locus > orf > AC.
     * @param interactor
     * @return
     */
    public static String getProteinDisplayName(ExtendedInteractor interactor) {
       String name = null;

        if (!interactor.getAliases().isEmpty()) {
            name = interactor.getAliases().iterator().next().getName();
        } else {
            for (CrossReference xref : interactor.getAlternativeIdentifiers()) {
                
                if ("commercial name".equals(xref.getText())) {
                    name = xref.getIdentifier();
                }
            }

            if (name == null) {
                String intactAc = getIntactIdentifierFromCrossReferences(interactor.getIdentifiers());

                if (intactAc != null) {
                    Interactor intactInteractor = Functions.getInteractorByAc(intactAc);
                    InteractorAlias alias = getAliasByPriority(intactInteractor, CvAliasType.GENE_NAME_MI_REF,
                                                                        "MI:2003", // commercial name
                                                                        CvAliasType.GO_SYNONYM_MI_REF,
                                                                        CvAliasType.LOCUS_NAME_MI_REF,
                                                                        CvAliasType.ORF_NAME_MI_REF);
                    if (alias != null) {
                        name = alias.getName();
                    } else {
                        name = intactInteractor.getAc();
                    }
                }
            }
        }

        return name;
    }

    private static InteractorAlias getAliasByPriority(Interactor intactInteractor, String ... aliasTypes) {
        for (String aliasType : aliasTypes) {
            for (InteractorAlias alias : intactInteractor.getAliases()) {
                if (alias.getCvAliasType() != null && aliasType.equals(alias.getCvAliasType().getIdentifier())) {
                    return alias;
                }
            }
        }

        return null;
    }


}
