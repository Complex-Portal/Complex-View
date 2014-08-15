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
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persister.IntactCore;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.persistence.Query;
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

    public static final String NO_CLASS = "no_class";

    private List<CvObject> allCvObjects;
    private Map<CvKey, CvObject> allCvObjectMap;
    private Map<String, CvObject> acCvObjectMap;

    private List<CvTopic> publicationTopics;
    private List<CvTopic> experimentTopics;
    private List<CvTopic> interactionTopics;
    private List<CvTopic> interactorTopics;
    private List<CvTopic> proteinTopics;
    private List<CvTopic> nucleicAcidTopics;
    private List<CvTopic> smallMoleculeTopics;
    private List<CvTopic> participantTopics;
    private List<CvTopic> featureTopics;
    private List<CvTopic> cvObjectTopics;
    private List<CvTopic> bioSourceTopics;
    private List<CvTopic> noClassTopics;

    private List<SelectItem> publicationTopicSelectItems;
    private List<SelectItem> experimentTopicSelectItems;
    private List<SelectItem> interactionTopicSelectItems;
    private List<SelectItem> interactorTopicSelectItems;
    private List<SelectItem> proteinTopicSelectItems;
    private List<SelectItem> nucleicAcidTopicSelectItems;
    private List<SelectItem> smallMoleculeTopicSelectItems;
    private List<SelectItem> participantTopicSelectItems;
    private List<SelectItem> featureTopicSelectItems;
    private List<SelectItem> cvObjectTopicSelectItems;
    private List<SelectItem> bioSourceTopicSelectItems;
    private List<SelectItem> noClassSelectItems;

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

    private List<CvInteractorType> interactorTypes;
    private List<SelectItem> interactorTypeSelectItems;

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

    private List<CvParameterType> parameterTypes;
    private List<SelectItem> parameterTypeSelectItems;

    private List<CvParameterUnit> parameterUnits;
    private List<SelectItem> parameterUnitSelectItems;

    private List<CvConfidenceType> confidenceTypes;
    private List<SelectItem> confidenceTypeSelectItems;

    private CvExperimentalRole defaultExperimentalRole;
    private CvBiologicalRole defaultBiologicalRole;

    private Multimap<String, CvTopic> cvObjectsByUsedInClass;
    private Multimap<Class, CvObject> cvObjectsByClass;

    public CvObjectService() {
    }

    @PostConstruct
    public void loadData() {
        createAdditionalCVs();

        refresh( null );
    }

    private void createAdditionalCVs() {
         createCvTopicIfNecessary(CvTopic.TO_BE_REVIEWED);
    }

    @Transactional(value = "transactionManager", propagation = Propagation.NEVER, readOnly = true)
    public synchronized void refresh( ActionEvent evt ) {
        if ( log.isDebugEnabled() ) log.debug( "Loading Controlled Vocabularies" );

        final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        publicationTopicSelectItems = new ArrayList<SelectItem>();

        String cvQuery = "select c from CvObject c " +
                "where c.ac not in (" +
                " select c2.ac from CvObject c2 join c2.annotations as a join a.cvTopic as t " +
                "where t.shortLabel = :hidden)";
        Query query = getCoreEntityManager().createQuery(cvQuery);
        query.setParameter("hidden",CvTopic.HIDDEN);

        allCvObjects = query.getResultList();

        allCvObjectMap = new HashMap<CvKey, CvObject>( allCvObjects.size() * 2 );
        acCvObjectMap = new HashMap<String, CvObject>( allCvObjects.size() );

        cvObjectsByUsedInClass = HashMultimap.create();
        cvObjectsByClass = HashMultimap.create();

        for ( CvObject cvObject : allCvObjects ) {
            acCvObjectMap.put( cvObject.getAc(), cvObject );
            cvObjectsByClass.put(cvObject.getClass(), cvObject);

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
                    cvObjectsByUsedInClass.put(NO_CLASS, ( CvTopic ) cvObject );
                }
            }
        }

        // topics
        publicationTopics = getSortedTopicList(Publication.class.getName(), cvObjectsByUsedInClass);
        experimentTopics = getSortedTopicList(Experiment.class.getName(), cvObjectsByUsedInClass);
        interactionTopics = getSortedTopicList( Interaction.class.getName(), cvObjectsByUsedInClass);
        interactorTopics = getSortedTopicList( Interactor.class.getName(), cvObjectsByUsedInClass);
        nucleicAcidTopics = getSortedTopicList( NucleicAcid.class.getName(), cvObjectsByUsedInClass);
        smallMoleculeTopics = getSortedTopicList(SmallMolecule.class.getName(), cvObjectsByUsedInClass);
        proteinTopics = getSortedTopicList( Protein.class.getName(), cvObjectsByUsedInClass);
        participantTopics = getSortedTopicList( Component.class.getName(), cvObjectsByUsedInClass);
        featureTopics = getSortedTopicList( Feature.class.getName(), cvObjectsByUsedInClass);
        bioSourceTopics = getSortedTopicList( BioSource.class.getName(), cvObjectsByUsedInClass);
        cvObjectTopics = getSortedTopicList( CvObject.class.getName(), cvObjectsByUsedInClass);
        noClassTopics = getSortedTopicList( NO_CLASS, cvObjectsByUsedInClass);

        // select items
        noClassSelectItems = createSelectItems(noClassTopics, null);

        SelectItemGroup noClassSelectItemGroup = new SelectItemGroup("Not classified");
        noClassSelectItemGroup.setSelectItems(noClassSelectItems.toArray(new SelectItem[noClassSelectItems.size()]));

        SelectItemGroup pubSelectItemGroup = new SelectItemGroup("Publication");
        List<SelectItem> pubTopicSelectItems = createSelectItems(publicationTopics, null);
        pubSelectItemGroup.setSelectItems(pubTopicSelectItems.toArray(new SelectItem[pubTopicSelectItems.size()]));

        SelectItemGroup expSelectItemGroup = new SelectItemGroup("Experiment");
        List<SelectItem> expTopicSelectItems = createSelectItems(experimentTopics, null);
        expSelectItemGroup.setSelectItems(expTopicSelectItems.toArray(new SelectItem[expTopicSelectItems.size()]));

        publicationTopicSelectItems = new ArrayList<SelectItem>();
        publicationTopicSelectItems.add( new SelectItem( null, "-- Select topic --", "-- Select topic --", false, false, true ) );
        publicationTopicSelectItems.add(pubSelectItemGroup);
        publicationTopicSelectItems.add(expSelectItemGroup);
        publicationTopicSelectItems.add(noClassSelectItemGroup);

        experimentTopicSelectItems = createSelectItems( experimentTopics, "-- Select topic --" );
        experimentTopicSelectItems.add(noClassSelectItemGroup);

        interactionTopicSelectItems = createSelectItems( interactionTopics, "-- Select topic --" );
        interactionTopicSelectItems.add(noClassSelectItemGroup);


        SelectItemGroup proteinSelectItemGroup = new SelectItemGroup("Protein");
        proteinTopicSelectItems = createSelectItems(proteinTopics, null);
        proteinSelectItemGroup.setSelectItems(proteinTopicSelectItems.toArray(new SelectItem[proteinTopicSelectItems.size()]));

        SelectItemGroup nucleicAcidSelectItemGroup = new SelectItemGroup("Nucleic Acid");
        nucleicAcidTopicSelectItems = createSelectItems(nucleicAcidTopics, null);
        nucleicAcidSelectItemGroup.setSelectItems(nucleicAcidTopicSelectItems.toArray(new SelectItem[nucleicAcidTopicSelectItems.size()]));

        SelectItemGroup smallMoleculeSelectItemGroup = new SelectItemGroup("Small Molecule");
        smallMoleculeTopicSelectItems = createSelectItems(smallMoleculeTopics, null);
        smallMoleculeSelectItemGroup.setSelectItems(smallMoleculeTopicSelectItems.toArray(new SelectItem[smallMoleculeTopicSelectItems.size()]));

        SelectItemGroup interactorSelectItemGroup = new SelectItemGroup("Interactor");
        interactorTopicSelectItems = createSelectItems(interactorTopics, null);
        interactorSelectItemGroup.setSelectItems(interactorTopicSelectItems.toArray(new SelectItem[interactorTopicSelectItems.size()]));

        interactorTopicSelectItems = new ArrayList<SelectItem>();
        interactorTopicSelectItems.add( new SelectItem( null, "-- Select topic --", "-- Select topic --", false, false, true ) );
        interactorTopicSelectItems.add(proteinSelectItemGroup);
        interactorTopicSelectItems.add(nucleicAcidSelectItemGroup);
        interactorTopicSelectItems.add(smallMoleculeSelectItemGroup);
        interactorTopicSelectItems.add(interactorSelectItemGroup);
        interactorTopicSelectItems.add(noClassSelectItemGroup);

        participantTopicSelectItems = createSelectItems( participantTopics, "-- Select topic --" );
        participantTopicSelectItems.add(noClassSelectItemGroup);

        featureTopicSelectItems = createSelectItems( featureTopics, "-- Select topic --" );
        featureTopicSelectItems.add(noClassSelectItemGroup);

        cvObjectTopicSelectItems = createSelectItems( cvObjectTopics, "-- Select topic --" );
        cvObjectTopicSelectItems.add(noClassSelectItemGroup);

        bioSourceTopicSelectItems = createSelectItems( bioSourceTopics, "-- Select topic --" );
        bioSourceTopicSelectItems.add(noClassSelectItemGroup);

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

        interactorTypes = getSortedList( CvInteractorType.class, cvObjectsByClass);
        interactorTypeSelectItems = createSelectItems( interactorTypes, "-- Select type --" );

        experimentalRoles = getSortedList( CvExperimentalRole.class, cvObjectsByClass);
        // must have one experimental role
        experimentalRoleSelectItems = createExperimentalRoleSelectItems( experimentalRoles, null );

        biologicalRoles = getSortedList( CvBiologicalRole.class, cvObjectsByClass);
        // must have one biological role
        biologicalRoleSelectItems = createBiologicalRoleSelectItems( biologicalRoles, null );

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

        parameterTypes = getSortedList( CvParameterType.class, cvObjectsByClass);
        parameterTypeSelectItems = createSelectItems( parameterTypes, "-- Select type --" );

        parameterUnits = getSortedList( CvParameterUnit.class, cvObjectsByClass);
        parameterUnitSelectItems = createSelectItems( parameterUnits, "-- Select unit --" );

        confidenceTypes = getSortedList( CvConfidenceType.class, cvObjectsByClass);
        confidenceTypeSelectItems = createSelectItems( confidenceTypes, "-- Select type --" );

        // add all obsoletes and hidden now to the map of class objects
        List<CvObject> allCvs = getDaoFactory().getCvObjectDao().getAll();
        for ( CvObject cvObject : allCvs ) {
            if (!cvObjectsByClass.containsKey(cvObject.getClass())){
                cvObjectsByClass.put(cvObject.getClass(), cvObject);
            }
        }

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

    public List<SelectItem> createExperimentalRoleSelectItems( Collection<? extends CvObject> cvObjects, String noSelectionText ) {
        List<SelectItem> selectItems = new CopyOnWriteArrayList<SelectItem>();

        if ( noSelectionText != null ) {
            selectItems.add( new SelectItem( null, noSelectionText, noSelectionText, false, false, true ) );
        }

        for ( CvObject cvObject : cvObjects ) {
            selectItems.add( createSelectItem( cvObject ) );

            if (cvObject.getIdentifier() != null && cvObject.getIdentifier().equals(CvExperimentalRole.UNSPECIFIED_PSI_REF)){
                defaultExperimentalRole = (CvExperimentalRole) cvObject;
            }
        }

        return selectItems;
    }

    public List<SelectItem> createBiologicalRoleSelectItems( Collection<? extends CvObject> cvObjects, String noSelectionText ) {
        List<SelectItem> selectItems = new CopyOnWriteArrayList<SelectItem>();

        if ( noSelectionText != null ) {
            selectItems.add( new SelectItem( null, noSelectionText, noSelectionText, false, false, true ) );
        }

        for ( CvObject cvObject : cvObjects ) {
            selectItems.add( createSelectItem( cvObject ) );

            if (cvObject.getIdentifier() != null && cvObject.getIdentifier().equals(CvBiologicalRole.UNSPECIFIED_PSI_REF)){
                defaultBiologicalRole = (CvBiologicalRole) cvObject;
            }
        }

        return selectItems;
    }

    private SelectItem createSelectItem( CvObject cv ) {
        if (!IntactCore.isInitialized(cv.getAnnotations())) {
            cv = getDaoFactory().getCvObjectDao().getByAc(cv.getAc());
        }

        boolean obsolete = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel( cv, CvTopic.OBSOLETE_MI_REF ) != null;
        return new SelectItem( cv, cv.getShortLabel()+((obsolete? " (obsolete)" : "")), cv.getFullName());
    }

    public CvObject findCvObjectByAc( String ac ) {
        return acCvObjectMap.get( ac );
    }

    public <T extends CvObject> T findCvObjectByIdentifier( Class<T> cvClass, String identifier ) {
        return (T) allCvObjectMap.get( new CvKey(identifier, cvClass) );
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

    private void createCvTopicIfNecessary(String label) {
        if (getDaoFactory().getCvObjectDao(CvTopic.class).getByShortLabel(label) == null) {
            CvTopic topic = new CvTopic(label);
            getCorePersister().saveOrUpdate(topic);
        }
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

    public List<SelectItem> getProteinTopicSelectItems() {
        return proteinTopicSelectItems;
    }

    public void setProteinTopicSelectItems(List<SelectItem> proteinTopicSelectItems) {
        this.proteinTopicSelectItems = proteinTopicSelectItems;
    }

    public List<SelectItem> getNucleicAcidTopicSelectItems() {
        return nucleicAcidTopicSelectItems;
    }

    public void setNucleicAcidTopicSelectItems(List<SelectItem> nucleicAcidTopicSelectItems) {
        this.nucleicAcidTopicSelectItems = nucleicAcidTopicSelectItems;
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

    public List<SelectItem> getInteractorTypeSelectItems() {
        return interactorTypeSelectItems;
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

    public List<SelectItem> getParameterTypeSelectItems() {
        return parameterTypeSelectItems;
    }

    public List<SelectItem> getParameterUnitSelectItems() {
        return parameterUnitSelectItems;
    }

    public List<SelectItem> getConfidenceTypeSelectItems() {
        return confidenceTypeSelectItems;
    }

    public List<SelectItem> getNoClassSelectItems() {
        return noClassSelectItems;
    }

    public CvExperimentalRole getDefaultExperimentalRole() {
        return defaultExperimentalRole;
    }

    public CvBiologicalRole getDefaultBiologicalRole() {
        return defaultBiologicalRole;
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
