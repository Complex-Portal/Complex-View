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
package uk.ac.ebi.intact.editor.controller.curate.publication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.CvTopic;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller( "datasetPopulator" )
@Lazy
public class DatasetPopulator extends JpaAwareController {

    private static final Log log = LogFactory.getLog( DatasetPopulator.class );

    private List<String> allDatasets;
    private List<SelectItem> allDatasetSelectItems;

    public DatasetPopulator() {

    }

    @PostConstruct
    public void loadData() {
        refresh( null );
    }

    @Transactional(value = "transactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void refresh( ActionEvent evt ) {
        if ( log.isInfoEnabled() ) log.info( "Loading datasets" );

        final Query query = getCoreEntityManager()
                .createQuery( "select distinct(a.annotationText) from Annotation a where a.cvTopic.identifier = :datasetTopicId order by a.annotationText asc" );
        query.setParameter( "datasetTopicId", CvTopic.DATASET_MI_REF );

        allDatasets = query.getResultList();

        allDatasetSelectItems = new ArrayList<SelectItem>( allDatasets.size() + 1 );
        allDatasetSelectItems.add( new SelectItem( null, "-- Select Dataset --" ) );

        for ( String dataset : allDatasets ) {
            if (dataset != null) {
                final SelectItem selectItem = createSelectItem(dataset);
                allDatasetSelectItems.add(selectItem);
            }
        }
    }

    public List<String> getAllDatasets() {
        return allDatasets;
    }

    public List<SelectItem> getAllDatasetSelectItems() {
        return allDatasetSelectItems;
    }

    public SelectItem createSelectItem( String dataset ) {
        if (dataset == null) throw new IllegalArgumentException("null dataset passed");
        SelectItem selectItem = null;

        if ( dataset.contains( "-" ) ) {
            String[] tokens = dataset.split( "-" );
            selectItem = new SelectItem( dataset, tokens[0].trim() );
        } else {
            selectItem = new SelectItem( dataset );
        }

        return selectItem;
    }
}
