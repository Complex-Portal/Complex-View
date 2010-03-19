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

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.cdb.webservice.Author;
import uk.ac.ebi.cdb.webservice.Citation;
import uk.ac.ebi.intact.bridges.citexplore.CitexploreClient;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import java.util.Date;
import java.util.Iterator;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("conversation.access")
@ConversationName("general")
public class PublicationController extends JpaAwareController {

    private Publication publication;
    private String ac;

    private Date lastSaved;

    private String identifier;
    private String authors;
    private String firstAuthor;
    private String journal;
    private short year;

    public PublicationController() {
    }

    public void loadData(ComponentSystemEvent event) {
        loadByAc();
    }

    private void loadByAc() {

        if (ac != null) {
            if (publication == null || !ac.equals(publication.getAc())) {
                publication = getDaoFactory().getPublicationDao().getByAc(ac);

                loadExtraFields();

            }
        } else if (publication != null) {
            ac = publication.getAc();
        }
    }

    private void loadExtraFields() {
        journal = findAnnotationText(CvTopic.JOURNAL_MI_REF);
        authors = findAnnotationText(CvTopic.AUTHOR_LIST_MI_REF);

        if (authors != null) {
            firstAuthor = authors.split(" ")[0];
        }

        String strYear = findAnnotationText(CvTopic.PUBLICATION_YEAR_MI_REF);
        year = Short.parseShort(strYear.trim());
    }

    private String findAnnotationText(String topicId) {
        Annotation pubAnnot = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(publication, topicId);

        if (pubAnnot != null) {
            return pubAnnot.getAnnotationText();
        }

        return null;

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
            journal = citation.getJournalIssue().getJournal().getISOAbbreviation()+" ("+
                    citation.getJournalIssue().getJournal().getISSN()+")";
            year = citation.getJournalIssue().getYearOfPublication();

            StringBuilder sbAuthors = new StringBuilder(64);

            Iterator<Author> authorIterator = citation.getAuthorCollection().iterator();
            while (authorIterator.hasNext()) {
                Author author =  authorIterator.next();
                sbAuthors.append(author.getLastName()).append(" ").append(author.getInitials()).append(".");
                if (authorIterator.hasNext()) sbAuthors.append(", ");
            }

            authors = sbAuthors.toString();

            if (authors != null) firstAuthor = authors.split(" ")[0];

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
        if (publication == null) {
            addErrorMessage("No publication to save", "How did I get here?");
            return;
        }
        
        if (publication.getAc() == null) {
            getDaoFactory().getPublicationDao().persist(publication);
        } else {
            getDaoFactory().getPublicationDao().merge(publication);
        }
        lastSaved = new Date();

        addInfoMessage("Publication saved", "AC: "+publication.getAc());
    }

    public void doClose(ActionEvent evt) {
        publication = null;
        ac = null;
    }

    public void doSaveAndClose(ActionEvent evt) {
        System.out.println("SAVE AND CLOSE");
        doSave(evt);
        doClose(evt);
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
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public short getYear() {
        return year;
    }

    public void setYear(short year) {
        this.year = year;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getFirstAuthor() {
        return firstAuthor;
    }

    public Date getLastSaved() {
        return lastSaved;
    }
}
