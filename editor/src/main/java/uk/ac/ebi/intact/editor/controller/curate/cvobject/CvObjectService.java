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
package uk.ac.ebi.intact.editor.controller.curate.cvobject;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Lazy
public class CvObjectService extends JpaAwareController {

    private static final Log log = LogFactory.getLog( CvObjectService.class );

    private List<CvObject> allCvObjects;
    private Map<CvKey, CvObject> allCvObjectMap;
    private Map<String, CvObject> acCvObjectMap;

    private List<CvTopic> publicationTopics;
    private List<CvTopic> experimentTopics;
    private List<CvTopic> interactionTopics;
    private List<CvTopic> interactorTopics;
    private List<CvTopic> participantTopics;
    private List<CvTopic> featureTopics;
    private List<CvTopic> cvObjectTopics;
    private List<CvTopic> bioSourceTopics;

    private List<SelectItem> publicationTopicSelectItems;
    private List<SelectItem> experimentTopicSelectItems;
    private List<SelectItem> interactionTopicSelectItems;
    private List<SelectItem> interactorTopicSelectItems;
    private List<SelectItem> participantTopicSelectItems;
    private List<SelectItem> featureTopicSelectItems;
    private List<SelectItem> cvObjectTopicSelectItems;
    private List<SelectItem> bioSourceTopicSelectItems;

    private List<CvDatabase> databases;
    private List<SelectItem> databaseSelectItems;

    private List<CvXrefQualifier> qualifiers;
    private List<SelectItem> qualifierSelectItems;

    private List<CvAliasType> aliasTypes;
    private List<SelectItem> aliasTypeSelectItems;

    private List<CvInteraction> interactionDetectionMethods;
    private List<SelectItem> interactionDetectionMethodSelectItems;

    private List<CvIdentification> participantDetectionMethods;
    private List<SelectItem> participantDetectionMethodSelectItems;

    private List<CvExperimentalPreparation> participantExperimentalPreparations;
    private List<SelectItem> participantExperimentalPreparationsSelectItems;

    private List<CvInteractionType> interactionTypes;
    private List<SelectItem> interactionTypeSelectItems;

    private List<CvExperimentalRole> experimentalRoles;
    private List<SelectItem> experimentalRoleSelectItems;

    private List<CvBiologicalRole> biologicalRoles;
    private List<SelectItem> biologicalRoleSelectItems;

    private List<CvFeatureIdentification> featureDetectionMethods;
    private List<SelectItem> featureDetectionMethodSelectItems;

    private List<CvFeatureType> featureTypes;
    private List<SelectItem> featureTypeSelectItems;

    private List<CvFuzzyType> fuzzyTypes;
    private List<SelectItem> fuzzyTypeSelectItems;

    private List<CvCellType> cellTypes;
    private List<SelectItem> cellTypeSelectItems;

    private List<CvTissue> tissues;
    private List<SelectItem> tissueSelectItems;

    private Multimap<String, CvTopic> cvObjectsByUsedInClass;
    private Multimap<Class, CvObject> cvObjectsByClass;

    public CvObjectService() {
    }


    @PostConstruct
    public void loadData() {
        refresh( null );
    }

    @Transactional
    public synchronized void refresh( ActionEvent evt ) {
        if ( log.isDebugEnabled() ) log.debug( "Loading Controlled Vocabularies" );

        final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        publicationTopicSelectItems = new ArrayList<SelectItem>();

        allCvObjects = getDaoFactory().getCvObjectDao().getAll();

        allCvObjectMap = new HashMap<CvKey, CvObject>( allCvObjects.size() * 2 );
        acCvObjectMap = new HashMap<String, CvObject>( allCvObjects.size() );

        cvObjectsByUsedInClass = new HashMultimap<String, CvTopic>();
        cvObjectsByClass = new HashMultimap<Class, CvObject>();

        for ( CvObject cvObject : allCvObjects ) {
            acCvObjectMap.put( cvObject.getAc(), cvObject );
            cvObjectsByClass.put( cvObject.getClass(), cvObject );

            if ( cvObject.getIdentifier() != null ) {
                CvKey keyId = new CvKey( cvObject.getIdentifier(), cvObject.getClass() );
                CvKey keyLabel = new CvKey( cvObject.getShortLabel(), cvObject.getClass() );
                allCvObjectMap.put( keyId, cvObject );
                allCvObjectMap.put( keyLabel, cvObject );
            }

            if ( cvObject instanceof CvTopic ) {
                String[] usedInClasses = findUsedInClass( cvObject );

                for ( String usedInClass : usedInClasses ) {
                    cvObjectsByUsedInClass.put( usedInClass, ( CvTopic ) cvObject );
                }

                if ( usedInClasses.length == 0 ) {
                    cvObjectsByUsedInClass.put( "no_class", ( CvTopic ) cvObject );
                }
            }
        }

        publicationTopics = getSortedTopicList( Experiment.class.getName(), cvObjectsByUsedInClass);
        experimentTopics = getSortedTopicList( Experiment.class.getName(), cvObjectsByUsedInClass);
        interactionTopics = getSortedTopicList( Interaction.class.getName(), cvObjectsByUsedInClass);
        interactorTopics = getSortedTopicList( Interactor.class.getName(), cvObjectsByUsedInClass);
        participantTopics = getSortedTopicList( Component.class.getName(), cvObjectsByUsedInClass);
        featureTopics = getSortedTopicList( Feature.class.getName(), cvObjectsByUsedInClass);
        bioSourceTopics = getSortedTopicList( Feature.class.getName(), cvObjectsByUsedInClass);

        cvObjectTopics = getSortedTopicList( CvObject.class.getName(), cvObjectsByUsedInClass);
        cvObjectTopics.addAll(getSortedTopicList("no_class", cvObjectsByUsedInClass));

        publicationTopicSelectItems = createSelectItems( publicationTopics, "-- Select topic --" );
        experimentTopicSelectItems = createSelectItems( experimentTopics, "-- Select topic --" );
        interactionTopicSelectItems = createSelectItems( interactionTopics, "-- Select topic --" );
        interactorTopicSelectItems = createSelectItems( interactorTopics, "-- Select topic --" );
        participantTopicSelectItems = createSelectItems( participantTopics, "-- Select topic --" );
        featureTopicSelectItems = createSelectItems( featureTopics, "-- Select topic --" );
        cvObjectTopicSelectItems = createSelectItems( cvObjectTopics, "-- Select topic --" );
        bioSourceTopicSelectItems = createSelectItems( bioSourceTopics, "-- Select topic --" );

        databases = getSortedList( CvDatabase.class, cvObjectsByClass);
        databaseSelectItems = createSelectItems( databases, "-- Select database --" );

        qualifiers = getSortedList( CvXrefQualifier.class, cvObjectsByClass);
        qualifierSelectItems = createSelectItems( qualifiers, "-- Select qualifier --" );

        aliasTypes = getSortedList( CvAliasType.class, cvObjectsByClass);
        aliasTypeSelectItems = createSelectItems( aliasTypes, "-- Select type --" );

        interactionDetectionMethods = getSortedList( CvInteraction.class, cvObjectsByClass);
        interactionDetectionMethodSelectItems = createSelectItems( interactionDetectionMethods, "-- Select method --" );

        participantDetectionMethods = getSortedList( CvIdentification.class, cvObjectsByClass);
        participantDetectionMethodSelectItems = createSelectItems( participantDetectionMethods, "-- Select method --" );

        participantExperimentalPreparations = getSortedList( CvExperimentalPreparation.class, cvObjectsByClass);
        participantExperimentalPreparationsSelectItems = createSelectItems( participantExperimentalPreparations, "-- Select experimental preparation --" );
        
        interactionTypes = getSortedList( CvInteractionType.class, cvObjectsByClass);
        interactionTypeSelectItems = createSelectItems( interactionTypes, "-- Select type --" );

        experimentalRoles = getSortedList( CvExperimentalRole.class, cvObjectsByClass);
        experimentalRoleSelectItems = createSelectItems( experimentalRoles, "-- Select role --" );

        biologicalRoles = getSortedList( CvBiologicalRole.class, cvObjectsByClass);
        biologicalRoleSelectItems = createSelectItems( biologicalRoles, "-- Select role --" );

        featureDetectionMethods = getSortedList( CvFeatureIdentification.class, cvObjectsByClass);
        featureDetectionMethodSelectItems = createSelectItems( featureDetectionMethods, "-- Select method --" );

        featureTypes = getSortedList( CvFeatureType.class, cvObjectsByClass);
        featureTypeSelectItems = createSelectItems( featureTypes, "-- Select type --" );

        fuzzyTypes = getSortedList( CvFuzzyType.class, cvObjectsByClass);
        fuzzyTypeSelectItems = createSelectItems( fuzzyTypes, "-- Select type --" );

        cellTypes = getSortedList( CvCellType.class, cvObjectsByClass);
        cellTypeSelectItems = createSelectItems( cellTypes, "-- Select cell type --" );

        tissues = getSortedList( CvTissue.class, cvObjectsByClass);
        tissueSelectItems = createSelectItems( tissues, "-- Select tissue --" );

        IntactContext.getCurrentInstance().getDataContext().commitTransaction( transactionStatus );
    }
    
    public List<CvTopic> getSortedTopicList( String key, Multimap<String, CvTopic> topicMultimap ) {
        if ( topicMultimap.containsKey( key ) ) {
            List<CvTopic> list = new ArrayList<CvTopic>( topicMultimap.get( key ) );
            Collections.sort( list, new CvObjectComparator() );
            return list;
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    public <T extends CvObject> List<T> getSortedList( Class<T> key, Multimap<Class, CvObject> classMultimap ) {
        if ( classMultimap.containsKey( key ) ) {
            List<CvObject> list = new ArrayList<CvObject>( classMultimap.get( key ) );
            Collections.sort( list, new CvObjectComparator() );
            return (List<T>) list;
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    private String[] findUsedInClass( CvObject cvObject ) {
        final Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel( cvObject, CvTopic.USED_IN_CLASS );

        if ( annotation != null ) {
            String annotText = annotation.getAnnotationText();
            annotText = annotText.replaceAll( " ", "" );
            return annotText.split( "," );
        } else {
            return new String[0];
        }
    }

    public List<SelectItem> createSelectItems( Collection<? extends CvObject> cvObjects, String noSelectionText ) {
        List<SelectItem> selectItems = new CopyOnWriteArrayList<SelectItem>();

        if ( noSelectionText != null ) {
            selectItems.add( new SelectItem( null, noSelectionText, noSelectionText, false, false, true ) );
        }

        for ( CvObject cvObject : cvObjects ) {
            selectItems.add( createSelectItem( cvObject ) );
        }

        return selectItems;
    }

    private SelectItem createSelectItem( CvObject cv ) {
        boolean obsolete = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel( cv, CvTopic.OBSOLETE_MI_REF ) != null;
        return new SelectItem( cv, cv.getShortLabel(), cv.getFullName(), obsolete );
    }

    public CvObject findCvObjectByAc( String ac ) {
        return acCvObjectMap.get( ac );
    }

    public <T extends CvObject> T findCvObject( Class<T> clazz, String idOrLabel ) {
        CvKey keyId = new CvKey( idOrLabel, clazz );
        CvKey keyLabel = new CvKey( idOrLabel, clazz );

        if ( allCvObjectMap.containsKey( keyId ) ) {
            return ( T ) allCvObjectMap.get( keyId );
        } else if ( allCvObjectMap.containsKey( keyLabel ) ) {
            return ( T ) allCvObjectMap.get( keyLabel );
        }

        return null;
    }


    public class CvKey {
        private String id;
        private String className;
        private String classSimpleName;

        private CvKey( String id, Class clazz ) {
            this.id = id;
            this.className = clazz.getName();
            this.classSimpleName = clazz.getSimpleName();
        }

        public String getId() {
            return id;
        }

        public String getClassName() {
            return className;
        }

        public String getClassSimpleName() {
            return classSimpleName;
        }

        @Override
        public boolean equals( Object o ) {
            if ( this == o ) return true;
            if ( o == null || getClass() != o.getClass() ) return false;

            CvKey cvKey = ( CvKey ) o;

            if ( className != null ? !className.equals( cvKey.className ) : cvKey.className != null ) return false;
            if ( id != null ? !id.equals( cvKey.id ) : cvKey.id != null ) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + ( className != null ? className.hashCode() : 0 );
            return result;
        }
    }

    public List<SelectItem> getPublicationTopicSelectItems() {
        return publicationTopicSelectItems;
    }

    public List<SelectItem> getExperimentTopicSelectItems() {
        return experimentTopicSelectItems;
    }

    public List<SelectItem> getInteractionTopicSelectItems() {
        return interactionTopicSelectItems;
    }

    public List<SelectItem> getInteractorTopicSelectItems() {
        return interactorTopicSelectItems;
    }

    public List<SelectItem> getParticipantTopicSelectItems() {
        return participantTopicSelectItems;
    }

    public List<SelectItem> getFeatureTopicSelectItems() {
        return featureTopicSelectItems;
    }

    public List<SelectItem> getCvObjectTopicSelectItems() {
        return cvObjectTopicSelectItems;
    }

    public List<SelectItem> getBioSourceTopicSelectItems() {
        return bioSourceTopicSelectItems;
    }

    public List<SelectItem> getDatabaseSelectItems() {
        return databaseSelectItems;
    }

    public List<SelectItem> getQualifierSelectItems() {
        return qualifierSelectItems;
    }

    public List<SelectItem> getAliasTypeSelectItems() {
        return aliasTypeSelectItems;
    }

    public List<SelectItem> getInteractionDetectionMethodSelectItems() {
        return interactionDetectionMethodSelectItems;
    }

    public List<SelectItem> getParticipantDetectionMethodSelectItems() {
        return participantDetectionMethodSelectItems;
    }

    public List<SelectItem> getParticipantExperimentalPreparationsSelectItems() {
        return participantExperimentalPreparationsSelectItems;
    }

    public List<SelectItem> getInteractionTypeSelectItems() {
        return interactionTypeSelectItems;
    }

    public List<SelectItem> getFeatureDetectionMethodSelectItems() {
        return featureDetectionMethodSelectItems;
    }

    public List<SelectItem> getFeatureTypeSelectItems() {
        return featureTypeSelectItems;
    }

    public List<SelectItem> getBiologicalRoleSelectItems() {
        return biologicalRoleSelectItems;
    }

    public List<SelectItem> getExperimentalRoleSelectItems() {
        return experimentalRoleSelectItems;
    }

    public List<SelectItem> getFuzzyTypeSelectItems() {
        return fuzzyTypeSelectItems;
    }

    public List<SelectItem> getTissueSelectItems() {
        return tissueSelectItems;
    }

    public List<SelectItem> getCellTypeSelectItems() {
        return cellTypeSelectItems;
    }

    public List<CvObject> getAllCvObjects() {
        return allCvObjects;
    }

    public Collection<CvObject> getCvObjectsByClass(Class clazz) {
        return cvObjectsByClass.get(clazz);
    }

    public Collection<CvTopic> getCvTopicsByUsedInClass(String className) {
        return cvObjectsByUsedInClass.get(className);
    }

    private class CvObjectComparator implements Comparator<CvObject> {
        @Override
        public int compare( CvObject o1, CvObject o2 ) {
            if ( o1 == null || o1.getShortLabel() == null ) {
                return 1;
            }

            if ( o2 == null || o2.getShortLabel() == null ) {
                return -1;
            }

            return o1.getShortLabel().compareTo( o2.getShortLabel() );
        }
    }
}
