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
package uk.ac.ebi.intact.editor.batch.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.editor.util.CurateUtils;
import uk.ac.ebi.intact.model.Experiment;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExperimentSyncWriter implements ItemWriter<Experiment> {

    private static final Log log = LogFactory.getLog( ExperimentSyncWriter.class );

    @PersistenceContext( unitName = "intact-core-default" )
    private EntityManager entityManager;

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED)
    public void write( List<? extends Experiment> items ) throws Exception {
        for ( Experiment expItem : items ) {
             if ( log.isDebugEnabled() ) log.debug( "Processing experiment: " + expItem.getShortLabel() );
            Experiment exp = expItem;
            if (!entityManager.contains(expItem)){
                exp = entityManager.merge(expItem);
            }
            CurateUtils.copyPublicationAnnotationsToExperiment(exp);
        }
    }
}
