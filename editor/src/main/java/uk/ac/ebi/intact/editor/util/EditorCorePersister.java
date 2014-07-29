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

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persister.CorePersisterImpl;
import uk.ac.ebi.intact.core.persister.Finder;
import uk.ac.ebi.intact.core.persister.stats.PersisterStatistics;
import uk.ac.ebi.intact.model.AnnotatedObject;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EditorCorePersister extends CorePersisterImpl {

    public EditorCorePersister() {
    }

    public EditorCorePersister(IntactContext intactContext, Finder finder) {
        super(intactContext, finder);
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED)
    public PersisterStatistics saveOrUpdate( EntityManager em, AnnotatedObject ao ) {

        em.setFlushMode(FlushModeType.COMMIT);
        //dataContext.getDaoFactory().getDataConfig().setAutoFlush(false);

        try {
            synchronize( ao );
            commit();
        } finally {
            em.setFlushMode(FlushModeType.AUTO);
        }

        reload( ao );

        return getStatistics();
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED)
    public PersisterStatistics saveOrUpdate( AnnotatedObject ao ) {
        throw new UnsupportedOperationException("Use saveOrUpdate(EntityManager, AnnotatedObject) instead");
    }
}
