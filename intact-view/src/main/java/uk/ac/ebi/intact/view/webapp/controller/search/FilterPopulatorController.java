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
package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.view.webapp.application.SpringInitializedService;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Loads the values for the filters.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("filterPopulator")
@ApplicationScoped
public class FilterPopulatorController extends SpringInitializedService{

    private static final Log log = LogFactory.getLog( FilterPopulatorController.class );

    public static final String NOT_SPECIFIED_VALUE = "-";
    public static final String EXPANSION_SPOKE_VALUE = "MI:1060";

    private List<SelectItem> stoichiometrySelectItems;
    private List<SelectItem> negativeSelectItems;
    private List<SelectItem> parametersSelectItems;
    private List<SelectItem> datasetSelectItems;
    private List<SelectItem> sourceSelectItems;
    private List<SelectItem> expansionSelectItems;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    public FilterPopulatorController() {

    }

    @Override
    public synchronized void initialize(){
        if (datasetSelectItems == null
                || stoichiometrySelectItems == null || negativeSelectItems == null
                || sourceSelectItems == null || expansionSelectItems == null){

            if (log.isInfoEnabled()) log.info("Preloading filters");

            if (log.isDebugEnabled()) log.debug("\tPreloading datasets");

            datasetSelectItems = listDatasets();

            if (log.isDebugEnabled()) log.debug("\tPreloading sources");

            sourceSelectItems = listSources();

            if (log.isDebugEnabled()) log.debug("\tPreloading expansions");

            expansionSelectItems = listExpansionSelectItems();
            if (log.isDebugEnabled()) log.debug("\tPreloading negative values");
            negativeSelectItems = listNegativeSelectItems();
            if (log.isDebugEnabled()) log.debug("\tPreloading parameter values");
            parametersSelectItems = listParametersSelectItems();
            if (log.isDebugEnabled()) log.debug("\tPreloading stoichiometry values");
            stoichiometrySelectItems = listStoichiometrySelectItems();
        }
    }

    public synchronized void reload(){
        if (log.isInfoEnabled()) log.info("Preloading filters");

        if (log.isDebugEnabled()) log.debug("\tPreloading datasets");

        datasetSelectItems = listDatasets();

        if (log.isDebugEnabled()) log.debug("\tPreloading sources");

        sourceSelectItems = listSources();

        if (log.isDebugEnabled()) log.debug("\tPreloading expansions");

        expansionSelectItems = listExpansionSelectItems();
        if (log.isDebugEnabled()) log.debug("\tPreloading negative values");
        negativeSelectItems = listNegativeSelectItems();
        if (log.isDebugEnabled()) log.debug("\tPreloading parameter values");
        parametersSelectItems = listParametersSelectItems();
        if (log.isDebugEnabled()) log.debug("\tPreloading stoichiometry values");
        stoichiometrySelectItems = listStoichiometrySelectItems();
    }

    private List<SelectItem> listDatasets() {

        Query query = entityManagerFactory.createEntityManager()
                .createQuery("select distinct a.annotationText " +
                        "from Annotation a " +
                        "where a.cvTopic.identifier = :datasetMi");

        query.setParameter("datasetMi", CvTopic.DATASET_MI_REF);

        List<String> datasetResults = query.getResultList();

        List<SelectItem> datasets = new ArrayList<SelectItem>(datasetResults.size()+1);

        for (String dataset : datasetResults) {
            String[] ds = dataset.split("-");
            String value;
            String description;

            if (ds.length > 1) {
                value = ds[0].trim();
                description = ds[1].trim();
            } else {
                value = ds[0];
                description = ds[0];
            }

            datasets.add(new SelectItem(dataset, value, description));
        }

        return datasets;
    }

    private List<SelectItem> listSources() {

        Query query = entityManagerFactory.createEntityManager().createQuery("select distinct i.owner.shortLabel from InteractionImpl i");
        List<String> sourceResults = query.getResultList();

        List<SelectItem> sources = new ArrayList<SelectItem>(sourceResults.size());

        for (String source : sourceResults) {
            sources.add(new SelectItem(source));
        }

        return sources;
    }

    private List<SelectItem> listExpansionSelectItems() {
        List<SelectItem> expansions = new ArrayList<SelectItem>(2);
        expansions.add(new SelectItem(NOT_SPECIFIED_VALUE, "Experimental binary (e.g. 2 hybrid)"));
        expansions.add(new SelectItem(EXPANSION_SPOKE_VALUE, "Spoke expanded co-complex (e.g. pulldown)"));
        return expansions;
    }

    private List<SelectItem> listNegativeSelectItems() {
        List<SelectItem> negativeItems = new ArrayList<SelectItem>(2);
        negativeItems.add(new SelectItem("true", "Only negative interactions"));
        negativeItems.add(new SelectItem("(false OR true)", "Includes negative interactions"));
        negativeItems.add(new SelectItem("false", "Only positive interactions (default)"));
        return negativeItems;
    }

    private List<SelectItem> listParametersSelectItems() {
        List<SelectItem> parameters = new ArrayList<SelectItem>(2);
        parameters.add(new SelectItem("true", "Only interactions having parameters available"));
        parameters.add(new SelectItem("false", "Excludes interactions having parameters available"));
        return parameters;
    }

    private List<SelectItem> listStoichiometrySelectItems() {
        List<SelectItem> stoichiometry = new ArrayList<SelectItem>(2);
        stoichiometry.add(new SelectItem("true", "Only interactions having stoichiometry information"));
        stoichiometry.add(new SelectItem("false", "Excludes interactions having stoichiometry information"));
        return stoichiometry;
    }

    public List<SelectItem> getDatasetSelectItems() {
        return datasetSelectItems;
    }

    public List<SelectItem> getSourceSelectItems() {
        return sourceSelectItems;
    }

    public List<SelectItem> getStoichiometrySelectItems() {
        return stoichiometrySelectItems;
    }

    public List<SelectItem> getParametersSelectItems() {
        return parametersSelectItems;
    }

    public List<SelectItem> getNegativeSelectItems() {
        return negativeSelectItems;
    }

    public List<SelectItem> getExpansionSelectItems() {
        return expansionSelectItems;
    }
}