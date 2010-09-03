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

import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.LazyDataModel;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class LazyDataModelFactory {

    private static final Log log = LogFactory.getLog( LazyDataModelFactory.class );

    private LazyDataModelFactory() {

    }

    public static <T> LazyDataModel<T> createEmptyDataModel() {
        return new LazyDataModel<T>() {
            @Override
            public List<T> load(int first, int pageSize, String sortField, boolean sortOrder, Map<String, String> filters) {
                return Collections.EMPTY_LIST;
            }
        };
    }

    public static LazyDataModel createLazyDataModel( EntityManager entityManager, String query, String countQuery ) {
        return createLazyDataModel( entityManager, query, countQuery, Maps.<String, String>newHashMap() );
    }

    public static LazyDataModel createLazyDataModel( EntityManager entityManager,
                                                     String query,
                                                     String countQuery,
                                                     Map<String, String> params ) {

        log.debug( "HQL Count: " + countQuery );

        int totalNumRows = 0;
        try {
            Query q = entityManager.createQuery( countQuery );

            if ( params != null ) {
                for ( Map.Entry<String, String> entry : params.entrySet() ) {
                    log.debug( "HQL Count param: " + entry.getKey() + " -> " + entry.getValue() );
                    q.setParameter( entry.getKey(), entry.getValue() );
                }
            }

            totalNumRows = ( ( Long ) q.getSingleResult() ).intValue();
        } catch (Throwable e) {
            throw new IllegalArgumentException("Problem running query: "+query, e);
        }

        return createLazyDataModel( entityManager, query, totalNumRows, params );
    }

    public static LazyDataModel createLazyDataModel( EntityManager entityManager,
                                                     String query,
                                                     int totalNumRows,
                                                     Map<String, String> params ) {
        LazyDataModel lazyDataModel = new HqlLazyDataModel( entityManager, query, params );
        lazyDataModel.setPageSize(10);
        lazyDataModel.setRowCount(totalNumRows);

        return lazyDataModel;
    }
}