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

import org.primefaces.model.LazyDataModel;
import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.core.context.IntactContext;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class HqlLazyDataModel<T> extends LazyDataModel<T>{

    private EntityManager entityManager;
    private String hqlQuery;
    private String countHqlQuery;
    private boolean counted;

    public HqlLazyDataModel(EntityManager entityManager, String hqlQuery, String countHqlQuery) {
        this.entityManager = entityManager;
        this.hqlQuery = hqlQuery;
        this.countHqlQuery = countHqlQuery;
    }

    public HqlLazyDataModel(EntityManager entityManager, String hqlQuery, int totalNumRows) {
        super(totalNumRows);
        this.entityManager = entityManager;
        this.hqlQuery = hqlQuery;
        counted = true;
    }

    @Override
    public List<T> fetchLazyData(int firstResult, int maxResults) {
        if (!counted) {
            final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

            Query countQuery = entityManager.createQuery(countHqlQuery);
            int total = (Integer) countQuery.getSingleResult();

            IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus);

            setRowIndex(total);

            counted = true;
        }

        final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        Query query = entityManager.createQuery(hqlQuery);
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);

        List<T> results = query.getResultList();
        
        IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus);

        return results;
    }
}
