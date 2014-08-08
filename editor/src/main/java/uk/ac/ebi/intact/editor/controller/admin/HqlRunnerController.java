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
package uk.ac.ebi.intact.editor.controller.admin;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.model.IntactObject;

import javax.faces.event.ActionEvent;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("session")
public class HqlRunnerController extends JpaAwareController {

    private static final int MAX_RESULTS = 200;

    private String hqlQuery;
    private String jamiHqlQuery;
    private int maxResults;

    private Collection<? extends IntactObject> results;
    private Collection<? extends IntactPrimaryObject> jamiResults;

    public HqlRunnerController() {
        maxResults = MAX_RESULTS;
    }

    @SuppressWarnings({"unchecked"})
    public void runQuery(ActionEvent evt) {
        maxResults = Math.min(maxResults, MAX_RESULTS);

        hqlQuery = cleanQuery(hqlQuery);

        try {
            EntityManager em = getCoreEntityManager();
            Query query = em.createQuery(hqlQuery);
            query.setMaxResults(maxResults);

            long startTime = System.currentTimeMillis();

            results = query.getResultList();

            long duration = System.currentTimeMillis() - startTime;

            addInfoMessage("Execution successful ("+duration+"ms)", "Results: "+(results.size() == MAX_RESULTS ? "More than " : "")+results.size());

        } catch (Throwable e) {
            addErrorMessage("Problem running query", e.getMessage());
        }
    }

    @SuppressWarnings({"unchecked"})
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void runJamiQuery(ActionEvent evt) {
        maxResults = Math.min(maxResults, MAX_RESULTS);

        jamiHqlQuery = cleanQuery(jamiHqlQuery);

        try {
            EntityManager em = getJamiEntityManager();
            Query query = em.createQuery(jamiHqlQuery);
            query.setMaxResults(maxResults);

            long startTime = System.currentTimeMillis();

            jamiResults = query.getResultList();

            long duration = System.currentTimeMillis() - startTime;

            addInfoMessage("Execution successful ("+duration+"ms)", "Results: "+(results.size() == MAX_RESULTS ? "More than " : "")+results.size());

        } catch (Throwable e) {
            addErrorMessage("Problem running query", e.getMessage());
        }
    }

    @SuppressWarnings({"unchecked"})
    public void executeQuery(ActionEvent evt) {
        hqlQuery = cleanQuery(hqlQuery);

        try {
            EntityManager em = getCoreEntityManager();
            Query query = em.createQuery(hqlQuery);

            long startTime = System.currentTimeMillis();

            int updated = query.executeUpdate();

            long duration = System.currentTimeMillis() - startTime;

            addInfoMessage("Execution successful ("+duration+"ms)", "Affected records: "+updated);

        } catch (Throwable e) {
            addErrorMessage("Problem executing query", e.getMessage());
        }
    }

    @SuppressWarnings({"unchecked"})
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void executeJamiQuery(ActionEvent evt) {
        jamiHqlQuery = cleanQuery(jamiHqlQuery);

        try {
            EntityManager em = getJamiEntityManager();
            Query query = em.createQuery(jamiHqlQuery);

            long startTime = System.currentTimeMillis();

            int updated = query.executeUpdate();

            long duration = System.currentTimeMillis() - startTime;

            addInfoMessage("Execution successful ("+duration+"ms)", "Affected records: "+updated);

        } catch (Throwable e) {
            addErrorMessage("Problem executing query", e.getMessage());
        }
    }

    private String cleanQuery(String hqlQuery) {
        return hqlQuery.replaceAll(";", "");
    }

    public String getHqlQuery() {
        return hqlQuery;
    }

    public void setHqlQuery(String hqlQuery) {
        this.hqlQuery = hqlQuery;
    }

    public Collection<? extends IntactObject> getResults() {
        return results;
    }

    public void setResults(Collection<? extends IntactObject> results) {
        this.results = results;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public String getJamiHqlQuery() {
        return jamiHqlQuery;
    }

    public void setJamiHqlQuery(String jamiHqlQuery) {
        this.jamiHqlQuery = jamiHqlQuery;
    }

    public Collection<? extends IntactPrimaryObject> getJamiResults() {
        return jamiResults;
    }

    public void setJamiResults(Collection<? extends IntactPrimaryObject> jamiResults) {
        this.jamiResults = jamiResults;
    }
}
