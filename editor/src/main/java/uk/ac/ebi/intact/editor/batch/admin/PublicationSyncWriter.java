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
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PublicationSyncWriter implements ItemWriter<Publication> {

    private static final Log log = LogFactory.getLog( PublicationSyncWriter.class );

    @PersistenceContext( unitName = "intact-core-default" )
    private EntityManager entityManager;

    @Override
    public void write( List<? extends Publication> items ) throws Exception {
        for ( Publication pub : items ) {

            // copy xrefs
            if ( !pub.getExperiments().isEmpty() ) {
                if ( log.isDebugEnabled() ) log.debug( "Updating publication: " + pub.getShortLabel() );
                Experiment exp = pub.getExperiments().iterator().next();

                pub.setFullName( exp.getFullName() );
                pub.setCreated(exp.getCreated());
                pub.setUpdated(exp.getUpdated());

                for ( ExperimentXref expXref : exp.getXrefs() ) {
                    if ( !hasXrefWithPrimaryId( expXref.getPrimaryId(), pub ) ) {
                        PublicationXref pubXref = new PublicationXref( IntactContext.getCurrentInstance().getInstitution(),
                                                                       expXref.getCvDatabase(), expXref.getPrimaryId(), expXref.getSecondaryId(),
                                                                       expXref.getDbRelease(), expXref.getCvXrefQualifier() );
                        pub.addXref( pubXref );
                        entityManager.merge( pubXref );
                    }
                }

                for ( Annotation expAnnot : exp.getAnnotations() ) {
                    if ( !hasAnnotWithTopicId( expAnnot.getCvTopic().getIdentifier(), pub ) ) {
                        Annotation pubAnnot = new Annotation( expAnnot.getCvTopic(), expAnnot.getAnnotationText() );
                        pub.addAnnotation( pubAnnot );
                        entityManager.merge( pubAnnot );
                    }
                }
            }
        }
    }

    private boolean hasXrefWithPrimaryId( String primaryId, Publication pub ) {
        for ( Xref xref : pub.getXrefs() ) {
            if ( primaryId.equals( xref.getPrimaryId() ) ) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnnotWithTopicId( String topicId, Publication pub ) {
        for ( Annotation annot : pub.getAnnotations() ) {
            if ( topicId.equals( annot.getCvTopic().getIdentifier() ) ) {
                return true;
            }
        }
        return false;
    }
}
