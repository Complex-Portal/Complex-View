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
package uk.ac.ebi.intact.editor.controller.bulk;

import org.apache.commons.collections.CollectionUtils;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.persister.BulkOperations;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.curate.cvobject.CvObjectService;
import uk.ac.ebi.intact.model.*;

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "bulk" )
public class BulkAnnotationController extends JpaAwareController {

    private String acs[];
    private String updatedAcs[];
    private String couldNotUpdateAcs[];
    private String aoClassName;
    private List<SelectItem> topicSelectItems;
    private boolean replaceIfTopicExists = true;

    private Annotation annotation;

    @Autowired
    private BulkOperations bulkOperations;

    public BulkAnnotationController() {
        topicSelectItems = new ArrayList<SelectItem>(1);
        topicSelectItems.add(new SelectItem(null, "* Choose object type first"));
    }

    public void load(ComponentSystemEvent evt) {
        annotation = new Annotation();
    }

    public void addBulkAnnotation(ActionEvent evt) {

        Class aoClass = null;
        try {
            aoClass = Thread.currentThread().getContextClassLoader().loadClass(aoClassName);

            updatedAcs = bulkOperations.addAnnotation(annotation, acs, aoClass, replaceIfTopicExists);

            if (acs.length > 0 && updatedAcs.length == 0) {
                addErrorMessage("Operation failed", "None of the ACs could be updated (do they exist?)");
                couldNotUpdateAcs = acs;
            } else if (acs.length != updatedAcs.length) {
                List<String> acsList = Arrays.asList(acs);
                List<String> updatedAcsList = Arrays.asList(updatedAcs);

                Collection<String> couldNotUpdateList = CollectionUtils.subtract(acsList, updatedAcsList);
                couldNotUpdateAcs = couldNotUpdateList.toArray(new String[couldNotUpdateList.size()]);

                addWarningMessage("Finished with warnings", updatedAcs.length + " objects were updated, "+
                        couldNotUpdateAcs.length+" objects couldn't be updated (do they exist?)");
            } else {
                addInfoMessage("Operation successful", updatedAcs.length+" objects were updated");
            }

        } catch (ClassNotFoundException e) {
            addErrorMessage("Could not find class: "+aoClassName, e.getMessage());
        }
    }

    public void aoClassNameChanged() {

        String newClassname = aoClassName;

        CvObjectService cvObjectService = (CvObjectService) getSpringContext().getBean("cvObjectService");

        if (Publication.class.getName().equals(newClassname)) {
            topicSelectItems = cvObjectService.getPublicationTopicSelectItems();
        } else if (Experiment.class.getName().equals(newClassname)) {
            topicSelectItems = cvObjectService.getExperimentTopicSelectItems();
        } else if (InteractionImpl.class.getName().equals(newClassname)) {
            topicSelectItems = cvObjectService.getInteractionTopicSelectItems();
        } else if (InteractorImpl.class.getName().equals(newClassname)) {
            topicSelectItems = cvObjectService.getInteractorTopicSelectItems();
        } else if (Component.class.getName().equals(newClassname)) {
            topicSelectItems = cvObjectService.getParticipantTopicSelectItems();
        } else if (Feature.class.getName().equals(newClassname)) {
            topicSelectItems = cvObjectService.getFeatureTopicSelectItems();
        } else if (BioSource.class.getName().equals(newClassname)) {
            topicSelectItems = cvObjectService.getBioSourceTopicSelectItems();
        } else if (CvObject.class.getName().equals(newClassname)) {
            topicSelectItems = cvObjectService.getCvObjectTopicSelectItems();
        } else {
            addErrorMessage("Error", "No class for type: "+newClassname);
        }
    }

    public String[] getAcs() {
        return acs;
    }

    public void setAcs(String[] acs) {
        this.acs = acs;
    }

    public String[] getUpdatedAcs() {
        return updatedAcs;
    }

    public void setUpdatedAcs(String[] updatedAcs) {
        this.updatedAcs = updatedAcs;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public String getAoClassName() {
        return aoClassName;
    }

    public void setAoClassName(String aoClassName) {
        this.aoClassName = aoClassName;
    }

    public List<SelectItem> getTopicSelectItems() {
        return topicSelectItems;
    }

    public void setTopicSelectItems(List<SelectItem> topicSelectItems) {
        this.topicSelectItems = topicSelectItems;
    }

    public String[] getCouldNotUpdateAcs() {
        return couldNotUpdateAcs;
    }

    public void setCouldNotUpdateAcs(String[] couldNotUpdateAcs) {
        this.couldNotUpdateAcs = couldNotUpdateAcs;
    }

    public boolean isReplaceIfTopicExists() {
        return replaceIfTopicExists;
    }

    public void setReplaceIfTopicExists(boolean replaceIfTopicExists) {
        this.replaceIfTopicExists = replaceIfTopicExists;
    }
}
