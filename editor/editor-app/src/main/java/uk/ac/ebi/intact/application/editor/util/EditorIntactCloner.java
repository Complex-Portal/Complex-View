/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.application.editor.util;

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.clone.IntactCloner;
import uk.ac.ebi.intact.model.clone.IntactClonerException;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EditorIntactCloner extends IntactCloner {
    private static final Log log = LogFactory.getLog( EditorIntactCloner.class );

    private static Set<String> EXCLUDED_ANNOTATION_TOPICS = new HashSet<String>();
    static{
        EXCLUDED_ANNOTATION_TOPICS.add( CvTopic.ACCEPTED );
        EXCLUDED_ANNOTATION_TOPICS.add( CvTopic.TO_BE_REVIEWED );
    }

    public EditorIntactCloner() {
        setExcludeACs(true);
    }

    @Override
    public Experiment cloneExperiment( Experiment experiment ) throws IntactClonerException {
        return super.cloneExperiment( experiment );
    }

    @Override
    public BioSource cloneBioSource(BioSource bioSource) throws IntactClonerException {
        return bioSource;
    }

    @Override
    public Institution cloneInstitution(Institution institution) throws IntactClonerException {
        return institution;
    }

    @Override
    protected AnnotatedObject cloneAnnotatedObjectCommon(AnnotatedObject<?, ?> ao, AnnotatedObject clone) throws IntactClonerException {

        if( clone == null ){
            return null;
        }

        if (ao == clone) {
            return ao;
        }

        clone = super.cloneAnnotatedObjectCommon(ao, clone);

        if (clone instanceof Interaction) {

            Interaction interaction = (Interaction) clone;
            interaction.getExperiments().clear();

        } else if (clone instanceof Experiment) {

            Experiment experiment = (Experiment) clone;
            experiment.getInteractions().clear();

            experiment = (Experiment) super.cloneAnnotatedObjectCommon(ao, clone);

            System.out.println( "Attempt to filter the experiment's annotations" );
            Iterator<Annotation> annotIterator = experiment.getAnnotations().iterator();

            while (annotIterator.hasNext()) {
                Annotation annotation = annotIterator.next();
                System.out.println( annotation );
                if( EXCLUDED_ANNOTATION_TOPICS.contains( annotation.getCvTopic().getShortLabel()) ) {
                    annotIterator.remove();
                    System.out.println( annotation.getCvTopic().getShortLabel() + " EXCLUDED !" );
                }
            }
        }

        return clone;
    }

    @Override
    public Component cloneComponent(Component component) throws IntactClonerException {
        return super.cloneComponent(component);
    }
}