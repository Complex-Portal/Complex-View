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
package uk.ac.ebi.intact.editor.controller.publication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.cdb.webservice.Author;
import uk.ac.ebi.cdb.webservice.Citation;
import uk.ac.ebi.intact.bridges.citexplore.CitexploreClient;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.shared.AnnotatedObjectController;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Publication;

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("conversation.access")
@ConversationName("general")
public class PublicationController extends AnnotatedObjectController {

    private static final Log log = LogFactory.getLog( PublicationController.class );

    private Publication publication;
    private String ac;

    private Date lastSaved;

    private String identifier;

    private String datasetToAdd;
    private String[] datasetsToRemove;
    private List<SelectItem> datasetsSelectItems;

    @Autowired
    private DatasetPopulator datasetPopulator;

    public PublicationController() {
    }
    
    @Override
    public AnnotatedObject getAnnotatedObject() {
        return getPublication();
    }

    public void loadData(ComponentSystemEvent event) {
        datasetsSelectItems = new ArrayList<SelectItem>();
        
        loadByAc();
    }

    private void loadByAc() {

        if (ac != null) {
            if (publication == null || !ac.equals(publication.getAc())) {
                publication = getDaoFactory().getPublicationDao().getByAc(ac);

                if (publication != null) {
                    loadFormFields();
                }

            }
        } else if (publication != null) {
            ac = publication.getAc();
        }
    }

    private void loadFormFields() {
        for (Annotation annotation : publication.getAnnotations()) {
            if (CvTopic.DATASET_MI_REF.equals(annotation.getCvTopic().getIdentifier())) {
                String datasetText = annotation.getAnnotationText();

                SelectItem datasetSelectItem = datasetPopulator.createSelectItem(datasetText);
                datasetsSelectItems.add(datasetSelectItem);
            }
        }
    }

    public boolean isCitexploreOnline() {
         try {
            CitexploreClient citexploreClient = new CitexploreClient();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public void newAutocomplete(ActionEvent evt) {
        if (identifier == null) {
            addErrorMessage("Cannot auto-complete", "ID is empty");
            return;
        }

        // check if already exists
        Publication existingPublication = getDaoFactory().getPublicationDao().getByPubmedId(identifier);

        if (existingPublication != null) {
            publication = existingPublication;
            addWarningMessage("Publication already exists", "Loaded from the database");
            return;
        }

        publication = new Publication(IntactContext.getCurrentInstance().getInstitution(), identifier);
        autocomplete(publication, identifier);

        identifier = null;
    }

    public void doFormAutocomplete(ActionEvent evt) {
        if (publication.getShortLabel() != null) {
            autocomplete(publication, publication.getShortLabel());
        }
    }

    private void autocomplete(Publication publication, String id) {
        CitexploreClient citexploreClient = null;

        try {
            citexploreClient = new CitexploreClient();
        } catch (Exception e) {
            addErrorMessage("Cannot auto-complete", "Citexplore service is down at the moment");
            return;
        }

        try {
            final Citation citation = citexploreClient.getCitationById(id);

            if (citation == null) {
                addErrorMessage("No citation was found", "PMID: "+id);
                return;
            }

            publication.setFullName(citation.getTitle());
            setJournal(citation.getJournalIssue().getJournal().getISOAbbreviation()+" ("+
                    citation.getJournalIssue().getJournal().getISSN()+")");
            setYear(citation.getJournalIssue().getYearOfPublication());

            StringBuilder sbAuthors = new StringBuilder(64);

            Iterator<Author> authorIterator = citation.getAuthorCollection().iterator();
            while (authorIterator.hasNext()) {
                Author author =  authorIterator.next();
                sbAuthors.append(author.getLastName()).append(" ").append(author.getInitials()).append(".");
                if (authorIterator.hasNext()) sbAuthors.append(", ");
            }

            setAuthors(sbAuthors.toString());

        } catch (Throwable e) {
            addErrorMessage("Problem auto-completing publication", e.getMessage());
            e.printStackTrace();
        }

        addInfoMessage("Auto-complete successful", "Fetched details for: "+id);
    }

    public void newEmpty(ActionEvent evt) {
        // check if already exists
        Publication existingPublication = getDaoFactory().getPublicationDao().getByPubmedId(identifier);

        if (existingPublication != null) {
            publication = existingPublication;
            addInfoMessage("Publication already exists", "Loaded from the database");
            return;
        }
        
        publication = new Publication(IntactContext.getCurrentInstance().getInstitution(), identifier);
        ac = null;
    }

    public void openByPmid(ActionEvent evt) {
        if (identifier == null || identifier.trim().length() == 0) {
            addErrorMessage("PMID is empty", "No PMID was supplied");
        } else {
            Publication publicationToOpen = getDaoFactory().getPublicationDao().getByPubmedId(identifier);

            if (publicationToOpen == null) {
                addErrorMessage("PMID not found", "There is no publication with PMID '"+ identifier +"'");
            } else {
                publication = publicationToOpen;
                ac = publication.getAc();
            }
        }

    }

    @Transactional
    public void doSave(ActionEvent evt) {
        if (log.isDebugEnabled()) log.debug("Saving publication: "+publication);
        
        if (publication == null) {
            addErrorMessage("No publication to save", "How did I get here?");
            return;
        }

        getIntactContext().getCorePersister().saveOrUpdate(publication);

        lastSaved = new Date();

        addInfoMessage("Publication saved", "AC: "+publication.getAc());

        setUnsavedChanges(false);
    }

    public void doClose(ActionEvent evt) {
        publication = null;
        ac = null;
    }

    public void doSaveAndClose(ActionEvent evt) {
        doSave(evt);
        doClose(evt);
    }

    public void addDataset(ActionEvent evt) {
        if (datasetToAdd != null) {
            datasetsSelectItems.add(datasetPopulator.createSelectItem(datasetToAdd));

            addAnnotation(CvTopic.DATASET_MI_REF, datasetToAdd);
        }
    }

    public void removeDatasets(ActionEvent evt) {
        if (datasetsToRemove != null) {
            for (String datasetToRemove : datasetsToRemove) {
                Iterator<SelectItem> iterator = datasetsSelectItems.iterator();

                while (iterator.hasNext()) {
                    SelectItem selectItem = iterator.next();
                    if (datasetToRemove.equals(selectItem.getValue())) {
                        iterator.remove();
                    }
                }

                removeAnnotation(CvTopic.DATASET_MI_REF, datasetToRemove);
            }
        }
    }    

    public String getAc() {
        if (ac == null && publication != null) {
            return publication.getAc();
        }
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public String getJournal() {
        return findAnnotationText(publication, CvTopic.JOURNAL_MI_REF);
    }

    public void setJournal(String journal) {
        setAnnotation(CvTopic.JOURNAL_MI_REF, journal);
    }

    public Short getYear() {
        String strYear = findAnnotationText(publication, CvTopic.PUBLICATION_YEAR_MI_REF);

        if (strYear != null) {
            return Short.valueOf(strYear);
        }

        return null;
    }

    public void setYear(Short year) {
        setAnnotation(CvTopic.PUBLICATION_YEAR_MI_REF, year);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getAuthors() {
        return findAnnotationText(publication, CvTopic.AUTHOR_LIST_MI_REF);
    }

    public void setAuthors(String authors) {
        setAnnotation(CvTopic.AUTHOR_LIST_MI_REF, authors);
    }

    public String getOnHold() {
        return findAnnotationText(publication, CvTopic.ON_HOLD);
    }

    public void setOnHold(String reason) {
        setAnnotation(CvTopic.ON_HOLD, reason);
    }

    public String getAcceptedMessage() {
        return findAnnotationText(publication, CvTopic.ACCEPTED);
    }

    public void setAcceptedMessage(String message) {
        setAnnotation(CvTopic.ACCEPTED, message);
    }


    public String getFirstAuthor() {
        final String authors = getAuthors();

        if (authors != null) {
            return authors.split(" ")[0];
        }

        return null;
    }

    public Date getLastSaved() {
        return lastSaved;
    }

    public List<SelectItem> getDatasetsSelectItems() {
        return datasetsSelectItems;
    }

    public String getDatasetToAdd() {
        return datasetToAdd;
    }

    public void setDatasetToAdd(String datasetToAdd) {
        this.datasetToAdd = datasetToAdd;
    }

    public String[] getDatasetsToRemove() {
        return datasetsToRemove;
    }

    public void setDatasetsToRemove(String[] datasetsToRemove) {
        this.datasetsToRemove = datasetsToRemove;
    }   
}
