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
import uk.ac.ebi.intact.jami.dao.CvTermDao;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.utils.IntactUtils;
import uk.ac.ebi.intact.model.CvTopic;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void clearAll(){
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

        IntactDao intactDao = getIntactDao();
        CvTermDao cvDao = intactDao.getCvTermDao();

        IntactCvTerm typeParent = cvDao.getByMIIdentifier("MI:0116", IntactUtils.FEATURE_TYPE_OBJCLASS);
        loadChildren(typeParent, featureTypeSelectItems, false);

        IntactCvTerm roleParent = cvDao.getByMIIdentifier("MI:0925", IntactUtils.TOPIC_OBJCLASS);
        SelectItem item2 = roleParent != null ? createSelectItem(roleParent, false):null;
        if (item2 != null){
            featureRoleSelectItems.add(item2);
        }
        loadChildren(roleParent, featureRoleSelectItems, false);

        IntactCvTerm aliasTypeParent = cvDao.getByMIIdentifier("MI:0300", IntactUtils.ALIAS_TYPE_OBJCLASS);
        loadChildren(aliasTypeParent, aliasTypeSelectItems, false);

        IntactCvTerm featureTopicParent = cvDao.getByMIIdentifier("MI:0668", IntactUtils.TOPIC_OBJCLASS);
        loadChildren(featureTopicParent, featureTopicSelectItems, false);

        IntactCvTerm databaseParent = cvDao.getByMIIdentifier("MI:0447", IntactUtils.DATABASE_OBJCLASS);
        loadChildren(databaseParent, featureDatabaseSelectItems, false);

        IntactCvTerm qualifierParent = cvDao.getByMIIdentifier("MI:0353", IntactUtils.QUALIFIER_OBJCLASS);
        loadChildren(qualifierParent, qualifierSelectItems, false);

        IntactCvTerm statusParent = cvDao.getByMIIdentifier("MI:0333", IntactUtils.RANGE_STATUS_OBJCLASS);
        loadChildren(statusParent, fuzzyTypeSelectItems, false);

        IntactCvTerm participantTopicParent = cvDao.getByMIIdentifier("MI:0666", IntactUtils.TOPIC_OBJCLASS);
        loadChildren(participantTopicParent, participantTopicSelectItems, false);

        IntactCvTerm participantDbParent = cvDao.getByMIIdentifier("MI:0473", IntactUtils.DATABASE_OBJCLASS);
        loadChildren(participantDbParent, participantDatabaseSelectItems, false);

        IntactCvTerm bioRoleParent = cvDao.getByMIIdentifier("MI:0500", IntactUtils.BIOLOGICAL_ROLE_OBJCLASS);
        loadChildren(bioRoleParent, biologicalRoleSelectItems, false);

        IntactCvTerm complexTopicParent = cvDao.getByMIIdentifier("MI:0664", IntactUtils.TOPIC_OBJCLASS);
        loadChildren(complexTopicParent, complexTopicSelectItems, false);

        IntactCvTerm complexDatabaseParent = cvDao.getByMIIdentifier("MI:0473", IntactUtils.DATABASE_OBJCLASS);
        IntactCvTerm complexDatabaseParent2 = cvDao.getByMIIdentifier("MI:0461", IntactUtils.DATABASE_OBJCLASS);
        loadChildren(complexDatabaseParent, complexDatabaseSelectItems, false);
        loadChildren(complexDatabaseParent2, complexDatabaseSelectItems, false);

        IntactCvTerm interactionTypeParent = cvDao.getByMIIdentifier("MI:0190", IntactUtils.INTERACTION_TYPE_OBJCLASS);
        loadChildren(interactionTypeParent, interactionTypeSelectItems, false);

        IntactCvTerm interactorTypeParent = cvDao.getByMIIdentifier("MI:0314", IntactUtils.INTERACTOR_TYPE_OBJCLASS);
        loadChildren(interactorTypeParent, interactorTypeSelectItems, false);

        IntactCvTerm evidenceTypeParent = cvDao.getByMIIdentifier("MI:1331", IntactUtils.DATABASE_OBJCLASS);
        SelectItem item = evidenceTypeParent != null ? createSelectItem(evidenceTypeParent, true):null;
        if (item != null){
            evidenceTypeSelectItems.add(item);
        }
        loadChildren(evidenceTypeParent, evidenceTypeSelectItems, true);

        IntactCvTerm confidenceTypeParent = cvDao.getByMIIdentifier("MI:1064", IntactUtils.CONFIDENCE_TYPE_OBJCLASS);
        loadChildren(confidenceTypeParent, confidenceTypeSelectItems, false);

        IntactCvTerm parameterTypeParent = cvDao.getByMIIdentifier("MI:0640", IntactUtils.PARAMETER_TYPE_OBJCLASS);
        loadChildren(parameterTypeParent, parameterTypeSelectItems, false);

        IntactCvTerm parameterUnit = cvDao.getByMIIdentifier("MI:0647", IntactUtils.UNIT_OBJCLASS);
        loadChildren(parameterUnit, parameterTypeSelectItems, false);
    }

    private void loadChildren(IntactCvTerm parent, List<SelectItem> selectItems, boolean ignoreHidden){
        for (OntologyTerm child : parent.getChildren()){
            IntactCvTerm cv = (IntactCvTerm)child;
            SelectItem item = createSelectItem(cv, ignoreHidden);
            if (item != null){
                selectItems.add(item);
            }
            if (!cv.getChildren().isEmpty()){
                loadChildren(cv, selectItems, ignoreHidden);
            }
        }
    }

    private SelectItem createSelectItem( IntactCvTerm cv, boolean ignoreHidden ) {
        if (!ignoreHidden && AnnotationUtils.collectAllAnnotationsHavingTopic(cv.getAnnotations(), null, "hidden").isEmpty()){
            acCvObjectMap.put(cv.getAc(), cv);
            boolean obsolete = !AnnotationUtils.collectAllAnnotationsHavingTopic(cv.getAnnotations(), CvTopic.OBSOLETE_MI_REF, CvTopic.OBSOLETE).isEmpty();
            return new SelectItem( cv, cv.getShortName()+((obsolete? " (obsolete)" : "")), cv.getFullName());
        }
        else if (ignoreHidden){
            acCvObjectMap.put(cv.getAc(), cv);
            boolean obsolete = !AnnotationUtils.collectAllAnnotationsHavingTopic(cv.getAnnotations(), CvTopic.OBSOLETE_MI_REF, CvTopic.OBSOLETE).isEmpty();
            return new SelectItem( cv, cv.getShortName()+((obsolete? " (obsolete)" : "")), cv.getFullName());
        }
        return null;
    }

    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public void loadDataIfNotDone( ComponentSystemEvent event ) {
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
