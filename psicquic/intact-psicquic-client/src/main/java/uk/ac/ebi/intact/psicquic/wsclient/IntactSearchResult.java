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
package uk.ac.ebi.intact.psicquic.wsclient;

import uk.ac.ebi.intact.model.IntactEntry;

/**
 * Contains the intact entry and result metadata.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactSearchResult {

    private IntactEntry entry;
    private int firstResult;
    private int maxResults;
    private int totalResults;

    public IntactSearchResult() {
    }

    public IntactSearchResult(IntactEntry entry, int firstResult, int maxResults, int totalResults) {
        this.entry = entry;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
        this.totalResults = totalResults;
    }

    public IntactEntry getEntry() {
        return entry;
    }

    public void setEntry(IntactEntry entry) {
        this.entry = entry;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
}