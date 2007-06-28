/**
 * Copyright 2006 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.webapp.search;

/**
 * Configuration constant names
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:SearchEnvironment.java 6452 2006-10-16 17:09:42 +0100 (Mon, 16 Oct 2006) baranda $
 * @since 1.5
 */
public interface SearchEnvironment
{
    /**
     * Number of results shown per page, by default
     */
    static final String MAX_RESULTS_PER_PAGE = "uk.ac.ebi.intact.search.MAX_RESULTS_PER_PAGE";

    /**
     * The help link (relative to the intact root)
     */
    static final String HELP_LINK = "uk.ac.ebi.intact.search.HELP_LINK";

    /**
     * The search link (relative to context root)
     */
    static final String SEARCH_LINK = "uk.ac.ebi.intact.search.SEARCH_LINK";

    /**
     * Relative URL for hierarchView
     */
    static final String HIERARCH_VIEW_URL = "uk.ac.ebi.intact.search.HIERARCH_VIEW_URL";

    /**
     *  Default hierarchView depth
     */
    static final String HIERARCH_VIEW_DEPTH = "uk.ac.ebi.intact.search.HIERARCH_VIEW_DEPTH";

    /**
     * Hierarch view method to use
     */
    static final String HIERARCH_VIEW_METHOD = "uk.ac.ebi.intact.search.HIERARCH_VIEW_METHOD";

    /**
     *  Relative URL for mine
     */
    static final String MINE_URL = "uk.ac.ebi.intact.search.MINE_URL";

   
}
