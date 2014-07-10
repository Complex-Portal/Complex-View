/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.jami.service.IntactService;

import java.util.List;
import java.util.Map;

/**
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 */
public class HqlServiceLazyDataModel<T extends IntactPrimaryObject> extends LazyDataModel<T> {

    private static final Log log = LogFactory.getLog( HqlServiceLazyDataModel.class );

    private IntactService service;
    private String hqlQuery;
    private Map<String, String> queryParameters;

    private String initialSortField;
    private boolean initialSortOrder;
    private String var;

    public HqlServiceLazyDataModel(IntactService service, String hqlQuery, Map<String, String> params) {
        super();
        this.service = service;
        this.hqlQuery = hqlQuery;
        this.queryParameters = params;
    }

    public HqlServiceLazyDataModel(IntactService service, String hqlQuery, Map<String, String> params, String initialSortField, boolean initialSortOrder, String var) {
        this(service, hqlQuery, params);
        this.initialSortField = initialSortField;
        this.initialSortOrder = initialSortOrder;
        this.var = var;
    }

    @Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        log.debug("Loading the lazy data between " + first + " and " + (first + pageSize));

        String queryToRun = hqlQuery;

        if (var != null){
            if (sortField != null) {
                queryToRun = queryToRun+" order by "+var+"."+sortField+" "+(sortOrder == SortOrder.DESCENDING? "desc" : "asc");
            } else if (initialSortField != null) {
                queryToRun = queryToRun+" order by "+var+"."+initialSortField+" "+(initialSortOrder? "asc" : "desc");
            }
        }

        log.debug("HQL: " + queryToRun);

        List<T> results = this.service.fetchIntactObjects(queryToRun, queryParameters, first, pageSize);

        log.debug("Returning "+results.size()+" results");

        return results;
    }


}
