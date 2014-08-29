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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.jami.model.OntologyTerm;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.CvTermDao;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.utils.IntactUtils;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Feature;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.SelectItem;
import java.util.*;

/**
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 */
@Service
@Lazy
public class EditorCvTermService extends JpaAwareController {

    private static final Log log = LogFactory.getLog( EditorCvTermService.class );

    private List<SelectItem> featureTypeSelectItems;
    private List<SelectItem> featureRoleSelectItems;
    private List<SelectItem> aliasTypeSelectItems;
    private List<SelectItem> featureTopicSelectItems;
    private List<SelectItem> featureDatabaseSelectItems;
    private List<SelectItem> qualifierSelectItems;
    private List<SelectItem> fuzzyTypeSelectItems;

    private List<SelectItem> participantTopicSelectItems;
    private List<SelectItem> participantDatabaseSelectItems;
    private List<SelectItem> biologicalRoleSelectItems;

    private List<SelectItem> complexTopicSelectItems;
    private List<SelectItem> complexDatabaseSelectItems;
    private List<SelectItem> interactionTypeSelectItems;
    private List<SelectItem> interactorTypeSelectItems;
    private List<SelectItem> evidenceTypeSelectItems;
    private List<SelectItem> confidenceTypeSelectItems;
    private List<SelectItem> parameterTypeSelectItems;
    private List<SelectItem> parameterUnitSelectItems;

    private boolean isInitialised = false;

    private Map<String, IntactCvTerm> acCvObjectMap;

    public EditorCvTermService() {
        acCvObjectMap = new HashMap<String, IntactCvTerm>();
    }

    public synchronized void clearAll(){
        this.featureTypeSelectItems = null;
        this.featureRoleSelectItems = null;
        this.aliasTypeSelectItems = null;
        this.featureTopicSelectItems = null;
        this.featureDatabaseSelectItems = null;
        this.qualifierSelectItems = null;
        this.fuzzyTypeSelectItems = null;
        this.participantTopicSelectItems = null;
        this.participantDatabaseSelectItems = null;
        this.biologicalRoleSelectItems = null;
        this.complexTopicSelectItems = null;
        this.complexDatabaseSelectItems = null;
        this.interactionTypeSelectItems = null;
        this.interactorTypeSelectItems = null;
        this.evidenceTypeSelectItems = null;
        this.confidenceTypeSelectItems = null;
        this.parameterTypeSelectItems = null;
        this.parameterUnitSelectItems = null;
        isInitialised = false;
        acCvObjectMap.clear();
    }

    private void loadCvs() {
        clearAll();

        featureTypeSelectItems = new ArrayList<SelectItem>();
        featureTypeSelectItems.add(new SelectItem(null, "select type", "select type", false, false, true));
        featureRoleSelectItems = new ArrayList<SelectItem>();
        featureRoleSelectItems.add(new SelectItem(null, "select role", "select role", false, false, true));
        aliasTypeSelectItems = new ArrayList<SelectItem>();
        aliasTypeSelectItems.add(new SelectItem(null, "select type", "select type", false, false, true));
        featureTopicSelectItems = new ArrayList<SelectItem>();
        featureTopicSelectItems.add(new SelectItem(null, "select topic", "select topic", false, false, true));
        featureDatabaseSelectItems = new ArrayList<SelectItem>();
        featureDatabaseSelectItems.add(new SelectItem(null, "select database", "select database", false, false, true));
        qualifierSelectItems = new ArrayList<SelectItem>();
        qualifierSelectItems.add(new SelectItem(null, "select qualifier", "select qualifier", false, false, true));
        fuzzyTypeSelectItems = new ArrayList<SelectItem>();
        fuzzyTypeSelectItems.add(new SelectItem(null, "select status", "select status", false, false, true));
        participantTopicSelectItems = new ArrayList<SelectItem>();
        participantTopicSelectItems.add(new SelectItem(null, "select topic", "select topic", false, false, true));
        participantDatabaseSelectItems = new ArrayList<SelectItem>();
        participantDatabaseSelectItems.add(new SelectItem(null, "select database", "select database", false, false, true));
        biologicalRoleSelectItems = new ArrayList<SelectItem>();
        biologicalRoleSelectItems.add(new SelectItem(null, "select biological role", "select biological role", false, false, true));
        complexTopicSelectItems = new ArrayList<SelectItem>();
        complexTopicSelectItems.add(new SelectItem(null, "select topic", "select topic", false, false, true));
        complexDatabaseSelectItems = new ArrayList<SelectItem>();
        complexDatabaseSelectItems.add(new SelectItem( null, "select database", "select database", false, false, true ));
        interactionTypeSelectItems = new ArrayList<SelectItem>();
        interactionTypeSelectItems.add(new SelectItem(null, "select interaction type", "select interaction type", false, false, true));
        interactorTypeSelectItems = new ArrayList<SelectItem>();
        interactorTypeSelectItems.add(new SelectItem(null, "select complex type", "select complex type", false, false, true));
        evidenceTypeSelectItems = new ArrayList<SelectItem>();
        evidenceTypeSelectItems.add(new SelectItem(null, "select eco code", "select eco code", false, false, true));
        confidenceTypeSelectItems = new ArrayList<SelectItem>();
        confidenceTypeSelectItems.add(new SelectItem(null, "select confidence type", "select confidence type", false, false, true));
        parameterTypeSelectItems = new ArrayList<SelectItem>();
        parameterTypeSelectItems.add(new SelectItem(null, "select parameter type", "select parameter type", false, false, true));
        parameterUnitSelectItems = new ArrayList<SelectItem>();
        parameterUnitSelectItems.add(new SelectItem(null, "select parameter unit", "select parameter unit", false, false, true));

        CvObjectService intactCvService = ApplicationContextProvider.getBean("cvObjectService");

        IntactDao intactDao = getIntactDao();
        CvTermDao cvDao = intactDao.getCvTermDao();

        Collection<IntactCvTerm> featureTypes = cvDao.getByObjClass(IntactUtils.FEATURE_TYPE_OBJCLASS);
        // TODO when we have a better hierarchy use ontology only
        //IntactCvTerm typeParent = cvDao.getByMIIdentifier("MI:0116", IntactUtils.FEATURE_TYPE_OBJCLASS);
        //if (typeParent != null){
            //loadChildren(typeParent, featureTypeSelectItems, false);
        //}
        if (!featureTypes.isEmpty()){
            loadCollectionCv(featureTypes, featureTypeSelectItems, false);
        }

        IntactCvTerm roleParent = cvDao.getByMIIdentifier("MI:0925", IntactUtils.TOPIC_OBJCLASS);
        SelectItem item2 = roleParent != null ? createSelectItem(roleParent, false):null;
        if (item2 != null){
            featureRoleSelectItems.add(item2);
        }
        if (roleParent != null){
            loadChildren(roleParent, featureRoleSelectItems, false);
        }

        Collection<IntactCvTerm> aliaseTypes = cvDao.getByObjClass(IntactUtils.ALIAS_TYPE_OBJCLASS);
        // TODO when we have a better hierarchy use ontology only
        //IntactCvTerm aliasTypeParent = cvDao.getByMIIdentifier("MI:0300", IntactUtils.ALIAS_TYPE_OBJCLASS);
        //if (aliasTypeParent != null){
        //    loadChildren(aliasTypeParent, aliasTypeSelectItems, false);
        //}
        if (!aliaseTypes.isEmpty()){
            loadCollectionCv(aliaseTypes, aliasTypeSelectItems, false);
        }

        IntactCvTerm featureTopicParent = cvDao.getByMIIdentifier("MI:0668", IntactUtils.TOPIC_OBJCLASS);
        List<String> processTopicAcs = Collections.EMPTY_LIST;
        if (featureTopicParent != null){
            processTopicAcs = loadChildren(featureTopicParent, featureTopicSelectItems, false);
        }
        loadMissingCvsFromIntactCvService(intactCvService, cvDao, processTopicAcs, featureTopicSelectItems, Feature.class.getCanonicalName());

        Collection<IntactCvTerm> databases = cvDao.getByObjClass(IntactUtils.DATABASE_OBJCLASS);
        // TODO when we have a better hierarchy use ontology only
        //IntactCvTerm databaseParent = cvDao.getByMIIdentifier("MI:0447", IntactUtils.DATABASE_OBJCLASS);
        //if (databaseParent != null){
            //loadChildren(databaseParent, featureDatabaseSelectItems, false);
        //}
        if (!databases.isEmpty()){
            loadCollectionCv(databases, featureDatabaseSelectItems, false);
        }

        Collection<IntactCvTerm> qualifiers = cvDao.getByObjClass(IntactUtils.QUALIFIER_OBJCLASS);
        // TODO when we have a better hierarchy use ontology only
        //IntactCvTerm qualifierParent = cvDao.getByMIIdentifier("MI:0353", IntactUtils.QUALIFIER_OBJCLASS);
        //if (qualifierParent != null){
        //    loadChildren(qualifierParent, qualifierSelectItems, false);
        //}
        if (!qualifiers.isEmpty()){
            loadCollectionCv(qualifiers, qualifierSelectItems, false);
        }

        IntactCvTerm statusParent = cvDao.getByMIIdentifier("MI:0333", IntactUtils.RANGE_STATUS_OBJCLASS);
        if (statusParent != null){
            loadChildren(statusParent, fuzzyTypeSelectItems, false);
        }

        IntactCvTerm participantTopicParent = cvDao.getByMIIdentifier("MI:0666", IntactUtils.TOPIC_OBJCLASS);
        if (participantTopicParent != null){
            processTopicAcs = loadChildren(participantTopicParent, participantTopicSelectItems, false);
        }
        loadMissingCvsFromIntactCvService(intactCvService, cvDao, processTopicAcs, participantTopicSelectItems, Component.class.getCanonicalName());

        // TODO when we have a better hierarchy use ontology only
        //IntactCvTerm participantDbParent = cvDao.getByMIIdentifier("MI:0473", IntactUtils.DATABASE_OBJCLASS);
        //if (participantDbParent != null){
        //    loadChildren(participantDbParent, participantDatabaseSelectItems, false);
        //}
        if (!databases.isEmpty()){
            loadCollectionCv(databases, participantDatabaseSelectItems, false);
        }

        IntactCvTerm bioRoleParent = cvDao.getByMIIdentifier("MI:0500", IntactUtils.BIOLOGICAL_ROLE_OBJCLASS);
        if (bioRoleParent != null){
            loadChildren(bioRoleParent, biologicalRoleSelectItems, false);
        }

        // TODO when we have a better hierarchy use ontology only
        IntactCvTerm complexTopicParent = cvDao.getByMIIdentifier("MI:0664", IntactUtils.TOPIC_OBJCLASS);
        processTopicAcs = new ArrayList<String>();
        if (complexTopicParent != null){
            processTopicAcs.addAll(loadChildren(complexTopicParent, complexTopicSelectItems, false));
        }
        if (participantTopicParent != null){
            processTopicAcs.addAll(loadChildren(participantTopicParent, complexTopicSelectItems, false));
        }
        loadMissingCvsFromIntactCvService(intactCvService, cvDao, processTopicAcs, complexTopicSelectItems, IntactComplex.class.getCanonicalName());


        // TODO when we have a better hierarchy use ontology only
        //IntactCvTerm complexDatabaseParent = cvDao.getByMIIdentifier("MI:0473", IntactUtils.DATABASE_OBJCLASS);
        //IntactCvTerm complexDatabaseParent2 = cvDao.getByMIIdentifier("MI:0461", IntactUtils.DATABASE_OBJCLASS);
        //if (complexDatabaseParent != null){
        //    loadChildren(complexDatabaseParent, complexDatabaseSelectItems, false);
        //}
        //if (complexDatabaseParent2 != null){
        //    loadChildren(complexDatabaseParent2, complexDatabaseSelectItems, false);
        //}
        if (!databases.isEmpty()){
            loadCollectionCv(databases, complexDatabaseSelectItems, false);
        }

        IntactCvTerm interactionTypeParent = cvDao.getByMIIdentifier("MI:0190", IntactUtils.INTERACTION_TYPE_OBJCLASS);
        if (interactionTypeParent != null){
            loadChildren(interactionTypeParent, interactionTypeSelectItems, false);
        }

        IntactCvTerm interactorTypeParent = cvDao.getByMIIdentifier("MI:0314", IntactUtils.INTERACTOR_TYPE_OBJCLASS);
        SelectItem item = interactorTypeParent != null ? createSelectItem(interactorTypeParent, true):null;
        if (item != null){
            interactorTypeSelectItems.add(item);
        }
        if (interactorTypeParent != null){
            loadChildren(interactorTypeParent, interactorTypeSelectItems, false);
        }

        IntactCvTerm evidenceTypeParent = cvDao.getByMIIdentifier("MI:1331", IntactUtils.DATABASE_OBJCLASS);
        SelectItem item3 = evidenceTypeParent != null ? createSelectItem(evidenceTypeParent, true):null;
        if (item3 != null){
            evidenceTypeSelectItems.add(item3);
        }
        if (evidenceTypeParent != null){
            loadChildren(evidenceTypeParent, evidenceTypeSelectItems, true);
        }

        Collection<IntactCvTerm> confidenceTypes = cvDao.getByObjClass(IntactUtils.CONFIDENCE_TYPE_OBJCLASS);
        // TODO when we have a better hierarchy use ontology only
        //IntactCvTerm confidenceTypeParent = cvDao.getByMIIdentifier("MI:1064", IntactUtils.CONFIDENCE_TYPE_OBJCLASS);
        //if (confidenceTypeParent != null){
        //    loadChildren(confidenceTypeParent, confidenceTypeSelectItems, false);
        //}
        if (!confidenceTypes.isEmpty()){
            loadCollectionCv(confidenceTypes, confidenceTypeSelectItems, false);
        }

        Collection<IntactCvTerm> parameterTypes = cvDao.getByObjClass(IntactUtils.PARAMETER_TYPE_OBJCLASS);
        // TODO when we have a better hierarchy use ontology only
        //IntactCvTerm parameterTypeParent = cvDao.getByMIIdentifier("MI:0640", IntactUtils.PARAMETER_TYPE_OBJCLASS);
        //if (parameterTypeParent != null){
        //    loadChildren(parameterTypeParent, parameterTypeSelectItems, false);
        //}
        if (!parameterTypes.isEmpty()){
            loadCollectionCv(parameterTypes, parameterTypeSelectItems, false);
        }

        Collection<IntactCvTerm> parameterUnits = cvDao.getByObjClass(IntactUtils.UNIT_OBJCLASS);
        // TODO when we have a better hierarchy use ontology only
        //IntactCvTerm parameterUnit = cvDao.getByMIIdentifier("MI:0647", IntactUtils.UNIT_OBJCLASS);
        //if (parameterUnit != null){
        //    loadChildren(parameterUnit, parameterTypeSelectItems, false);
        //}
        if (!parameterUnits.isEmpty()){
            loadCollectionCv(parameterUnits, parameterUnitSelectItems, false);
        }

        Comparator<SelectItem> labelComparator = new Comparator<SelectItem>() {
            @Override
            public int compare(SelectItem selectItem, SelectItem selectItem2) {
                return selectItem.getLabel().compareTo(selectItem2.getLabel());
            }
        };

        Collections.sort(featureTypeSelectItems, labelComparator);
        Collections.sort(featureRoleSelectItems, labelComparator);
        Collections.sort(aliasTypeSelectItems, labelComparator);
        Collections.sort(featureTopicSelectItems, labelComparator);
        Collections.sort(featureDatabaseSelectItems, labelComparator);
        Collections.sort(qualifierSelectItems, labelComparator);
        Collections.sort(fuzzyTypeSelectItems, labelComparator);
        Collections.sort(participantTopicSelectItems, labelComparator);
        Collections.sort(participantDatabaseSelectItems, labelComparator);
        Collections.sort(biologicalRoleSelectItems, labelComparator);
        Collections.sort(complexTopicSelectItems, labelComparator);
        Collections.sort(complexDatabaseSelectItems, labelComparator);
        Collections.sort(interactionTypeSelectItems, labelComparator);
        Collections.sort(interactorTypeSelectItems, labelComparator);
        Collections.sort(evidenceTypeSelectItems, labelComparator);
        Collections.sort(confidenceTypeSelectItems, labelComparator);
        Collections.sort(parameterTypeSelectItems, labelComparator);
        Collections.sort(parameterUnitSelectItems, labelComparator);
    }

    protected void loadMissingCvsFromIntactCvService(CvObjectService intactCvService, CvTermDao cvDao, List<String> processTopicAcs, List<SelectItem> items, String usedInClass) {
        Collection<CvTopic> topics = intactCvService.getCvTopicsByUsedInClass(usedInClass);
        for (CvTopic topic : topics){
            if (!processTopicAcs.contains(topic.getAc())){
                SelectItem topicItem = createSelectItem(cvDao.getByAc(topic.getAc()), false);
                if (topicItem != null){
                   items.add(topicItem);
                }
            }
        }
    }

    protected void loadCollectionCv(Collection<IntactCvTerm> featureTypes, List<SelectItem> items, boolean ignoreHidden) {
        List<String> list = new ArrayList<String>(featureTypes.size());
        for (IntactCvTerm term : featureTypes){
            SelectItem item = createSelectItem(term, ignoreHidden);
            if (item != null){
                list.add(term.getAc());
                 items.add(item);
            }
        }
    }

    private List<String> loadChildren(IntactCvTerm parent, List<SelectItem> selectItems, boolean ignoreHidden){
        List<String> list = new ArrayList<String>(parent.getChildren().size());
        for (OntologyTerm child : parent.getChildren()){
            IntactCvTerm cv = (IntactCvTerm)child;
            SelectItem item = createSelectItem(cv, ignoreHidden);
            if (item != null){
                list.add(cv.getAc());
                selectItems.add(item);
            }
            if (!cv.getChildren().isEmpty()){
                list.addAll(loadChildren(cv, selectItems, ignoreHidden));
            }
        }
        return list;
    }

    private SelectItem createSelectItem( IntactCvTerm cv, boolean ignoreHidden ) {
        if (!ignoreHidden && AnnotationUtils.collectAllAnnotationsHavingTopic(cv.getAnnotations(), null, "hidden").isEmpty()){
            acCvObjectMap.put(cv.getAc(), cv);
            boolean obsolete = !AnnotationUtils.collectAllAnnotationsHavingTopic(cv.getAnnotations(), CvTopic.OBSOLETE_MI_REF, CvTopic.OBSOLETE).isEmpty();
            return new SelectItem( cv, cv.getShortName()+((obsolete? " (obsolete)" : "")),
                    cv.getFullName() + (cv.getMIIdentifier() != null ? "("+cv.getMIIdentifier()+")":""));
        }
        else if (ignoreHidden){
            acCvObjectMap.put(cv.getAc(), cv);
            boolean obsolete = !AnnotationUtils.collectAllAnnotationsHavingTopic(cv.getAnnotations(), CvTopic.OBSOLETE_MI_REF, CvTopic.OBSOLETE).isEmpty();
            return new SelectItem( cv, cv.getShortName()+((obsolete? " (obsolete)" : "")),
                    cv.getFullName()+ (cv.getMIIdentifier() != null ? "("+cv.getMIIdentifier()+")":""));
        }
        return null;
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public synchronized void loadDataIfNotDone( ComponentSystemEvent event ) {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            if (!isInitialised){
                loadCvs();
                isInitialised = true;
            }
        }
    }

    public List<SelectItem> getFeatureTypeSelectItems() {
        return featureTypeSelectItems;
    }

    public List<SelectItem> getFeatureRoleSelectItems() {
        return featureRoleSelectItems;
    }

    public List<SelectItem> getAliasTypeSelectItems() {
        return aliasTypeSelectItems;
    }

    public List<SelectItem> getFeatureTopicSelectItems() {
        return featureTopicSelectItems;
    }

    public List<SelectItem> getFeatureDatabaseSelectItems() {
        return featureDatabaseSelectItems;
    }

    public List<SelectItem> getQualifierSelectItems() {
        return qualifierSelectItems;
    }

    public List<SelectItem> getFuzzyTypeSelectItems() {
        return fuzzyTypeSelectItems;
    }

    public List<SelectItem> getParticipantTopicSelectItems() {
        return participantTopicSelectItems;
    }

    public List<SelectItem> getParticipantDatabaseSelectItems() {
        return participantDatabaseSelectItems;
    }

    public List<SelectItem> getBiologicalRoleSelectItems() {
        return biologicalRoleSelectItems;
    }

    public List<SelectItem> getComplexTopicSelectItems() {
        return complexTopicSelectItems;
    }

    public List<SelectItem> getComplexDatabaseSelectItems() {
        return complexDatabaseSelectItems;
    }

    public List<SelectItem> getInteractionTypeSelectItems() {
        return interactionTypeSelectItems;
    }

    public List<SelectItem> getInteractorTypeSelectItems() {
        return interactorTypeSelectItems;
    }

    public List<SelectItem> getEvidenceTypeSelectItems() {
        return evidenceTypeSelectItems;
    }

    public List<SelectItem> getConfidenceTypeSelectItems() {
        return confidenceTypeSelectItems;
    }

    public List<SelectItem> getParameterTypeSelectItems() {
        return parameterTypeSelectItems;
    }

    public List<SelectItem> getParameterUnitSelectItems() {
        return parameterUnitSelectItems;
    }

    public IntactCvTerm findCvByAc(String ac){
        return acCvObjectMap.get(ac);
    }
}
