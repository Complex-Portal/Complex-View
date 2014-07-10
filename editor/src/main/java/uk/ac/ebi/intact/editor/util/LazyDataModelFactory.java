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
import org.primefaces.model.SortOrder;
import uk.ac.ebi.intact.jami.service.IntactService;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

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
            public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
                return Collections.EMPTY_LIST;
            }
        };
    }

    public static LazyDataModel createLazyDataModel( EntityManager entityManager, String query) {
        return createLazyDataModel( entityManager, query, Maps.<String, String>newHashMap(), null, null, false  );
    }

    public static LazyDataModel createLazyDataModel( EntityManager entityManager, String query, String countQuery, Map<String, String> params) {
        return createLazyDataModel( entityManager, query, countQuery, params, null, null, false );
    }

    public static LazyDataModel createLazyDataModel( EntityManager entityManager, String query, String var, String sortField, boolean sortOrder) {
        return createLazyDataModel( entityManager, query, Maps.<String, String>newHashMap(), var, sortField, sortOrder );
    }

    public static LazyDataModel createLazyDataModel( EntityManager entityManager, String query, String countQuery, String var, String sortField, boolean sortOrder) {
        return createLazyDataModel( entityManager, query, countQuery, Maps.<String, String>newHashMap(), var, sortField, sortOrder );
    }

    public static LazyDataModel createLazyDataModel( IntactService service, String query, String countQuery, String var, String sortField, boolean sortOrder) {
        return createLazyDataModel( service, query, countQuery, Maps.<String, String>newHashMap(), var, sortField, sortOrder );
    }

    public static LazyDataModel createLazyDataModel( EntityManager entityManager, String query, Map<String, String> params, String var, String sortField, boolean sortOrder) {
        String countQuery = query.substring(query.indexOf("from"), query.length());
        countQuery = "select count(*) "+countQuery;

        return createLazyDataModel( entityManager, query, countQuery, params, var, sortField, sortOrder );
    }

    public static LazyDataModel createLazyDataModel( EntityManager entityManager, String query, String countQuery ) {
        return createLazyDataModel( entityManager, query, countQuery, Maps.<String, String>newHashMap(), null, null, false);
    }

    public static LazyDataModel createLazyDataModel( EntityManager entityManager,
                                                     String query,
                                                     String countQuery,
                                                     Map<String, String> params,
                                                     String var,
                                                     String sortField,
                                                     boolean sortOrder) {

        log.debug( "HQL Count Query: " + countQuery );

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

            log.debug( "HQL Count: " + totalNumRows );

        } catch (Throwable e) {
            throw new IllegalArgumentException("Problem running query: "+query, e);
        }

        return createLazyDataModel( entityManager, query, totalNumRows, params, var, sortField, sortOrder );
    }

    public static LazyDataModel createLazyDataModel( IntactService service,
                                                     String query,
                                                     String countQuery,
                                                     Map<String, String> params,
                                                     String var,
                                                     String sortField,
                                                     boolean sortOrder) {

        log.debug( "HQL Count Query: " + countQuery );

        int totalNumRows = 0;
        try {
            totalNumRows = ((Long)service.countAll(countQuery, params)).intValue();

            log.debug( "HQL Count: " + totalNumRows );

        } catch (Throwable e) {
            throw new IllegalArgumentException("Problem running query: "+query, e);
        }

        return createLazyDataModel( service, query, totalNumRows, params, var, sortField, sortOrder );
    }

    public static LazyDataModel createLazyDataModel( EntityManager entityManager,
                                                     String query,
                                                     int totalNumRows,
                                                     Map<String, String> params,
                                                     String var,
                                                     String sortField,
                                                     boolean sortOrder) {
        LazyDataModel lazyDataModel = new HqlLazyDataModel( entityManager, query, params, sortField, sortOrder, var );
        lazyDataModel.setPageSize(10);
        lazyDataModel.setRowCount(totalNumRows);

        return lazyDataModel;
    }

    public static LazyDataModel createLazyDataModel( IntactService service,
                                                     String query,
                                                     int totalNumRows,
                                                     Map<String, String> params,
                                                     String var,
                                                     String sortField,
                                                     boolean sortOrder) {
        LazyDataModel lazyDataModel = new HqlServiceLazyDataModel( service, query, params, sortField, sortOrder, var );
        lazyDataModel.setPageSize(10);
        lazyDataModel.setRowCount(totalNumRows);

        return lazyDataModel;
    }

    public static <T> LazyDataModel<T> createLazyDataModel( final Collection<T> collection ) {
        LazyDataModel<T> lazyDataModel = new LazyDataModel<T>() {

            @Override
            public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
                List<T> list = new ArrayList<T>(collection);

                System.out.println("COL: "+collection.size());

                if (first >= list.size()) {
                    return Collections.EMPTY_LIST;
                }

                if (list.size() > pageSize) {
                    list = list.subList(first, Math.min(first+pageSize, list.size()-1));
                }

                System.out.println("RET: "+list.size());

                return list;
            }
        };

        lazyDataModel.setPageSize(10);
        lazyDataModel.setRowCount(collection.size());

        return lazyDataModel;
    }
}
