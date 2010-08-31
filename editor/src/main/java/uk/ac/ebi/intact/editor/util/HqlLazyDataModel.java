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

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class HqlLazyDataModel<T> extends LazyDataModel<T> {

    private static final Log log = LogFactory.getLog( HqlLazyDataModel.class );

    private EntityManager entityManager;
    private String hqlQuery;
    private Map<String, String> queryParameters;

    public HqlLazyDataModel( EntityManager entityManager, String hqlQuery, Map<String, String> params ) {
        super();
        this.entityManager = entityManager;
        this.hqlQuery = hqlQuery;
        this.queryParameters = params;
        //setRowCount(totalNumRows);
    }

    @Override
    public List<T> load(int first, int pageSize, String sortField, boolean sortOrder, Map<String, String> filters) {
        log.debug("Loading the lazy data between "+first+" and "+(first+pageSize));

        Query query = entityManager.createQuery( hqlQuery );
        log.debug( "HQL: " + hqlQuery );

        if ( queryParameters != null ) {
            for ( Map.Entry<String, String> entry : queryParameters.entrySet() ) {
                log.debug( "HQL param: " + entry.getKey() + " -> " + entry.getValue() );
                query.setParameter( entry.getKey(), entry.getValue() );
            }
        }

        query.setFirstResult( first );
        query.setMaxResults( pageSize );

        List<T> results = query.getResultList();

        log.debug("Returning "+results.size()+" results");

        return results;
    }
}
