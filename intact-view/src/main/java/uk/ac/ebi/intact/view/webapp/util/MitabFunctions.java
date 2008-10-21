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

import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.psimitab.search.IntactSearchEngine;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.CvInteractorType;
import uk.ac.ebi.intact.view.webapp.controller.SearchWebappException;
import uk.ac.ebi.intact.view.webapp.controller.browse.OntologyTermWrapper;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.commons.collections.map.LRUMap;

import java.io.IOException;
import java.util.Map;

/**
 * Functions to be used in the UI to control the display in interactions_tab
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
public final class MitabFunctions {

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

            //long startTime = System.currentTimeMillis();

            QueryParser parser = new MultiFieldQueryParser(defaultFields, new StandardAnalyzer());

            Query query = parser.parse(searchQuery);
            Hits hits = searcher.search(query);

            //System.out.println("Counted: "+query.toString()+" - "+hits.length()+" / Elapsed time: "+(System.currentTimeMillis()-startTime)+" ms - Directory: "+directory);

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
            return otw;
        }

        String proteinSearchQuery = otw.getSearchQuery()+" AND typeA:\""+ CvInteractorType.PROTEIN_MI_REF+"\"";

        if (interactorCountCache.containsKey(proteinSearchQuery)) {
            interactorCount = (Integer) interactorCountCache.get(proteinSearchQuery);
        } else {
            interactorCount = countHits(proteinSearchQuery, interactorDirectory);

            interactorCountCache.put(proteinSearchQuery, interactorCount);
        }

        if (interactorCount > 0) {
            if (interactionCountCache.containsKey(otw.getSearchQuery())) {
                interactionCount = (Integer) interactionCountCache.get(otw.getSearchQuery());
            } else {
                interactionCount = countHits(otw.getSearchQuery(), interactionDirectory);

                interactionCountCache.put(otw.getSearchQuery(), interactorCount);
            }
        }

        otw.setInteractorCount(interactorCount);
        otw.setInteractionCount(interactionCount);

        return otw;
    }



}
