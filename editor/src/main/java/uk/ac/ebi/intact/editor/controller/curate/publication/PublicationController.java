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
package uk.ac.ebi.intact.editor.controller.curate.publication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.cdb.webservice.Author;
import uk.ac.ebi.cdb.webservice.Citation;
import uk.ac.ebi.intact.bridges.citexplore.CitexploreClient;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.UserSessionController;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.model.*;

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.SelectItem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class PublicationController extends AnnotatedObjectController {

    private static final Log log = LogFactory.getLog( PublicationController.class );

    public static final String SUBMITTED = "MI:0878";
    public static final String CURATION_REQUEST = "MI:0873";

    private Publication publication;
    private String ac;

    private String identifier;

    private String datasetToAdd;
    private String[] datasetsToRemove;
    private List<SelectItem> datasetsSelectItems;

    public PublicationController() {
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return getPublication();
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        setPublication((Publication)annotatedObject);
    }

    public void loadData( ComponentSystemEvent event ) {
        datasetsSelectItems = new ArrayList<SelectItem>();

        loadByAc();
    }

    private void loadByAc() {

        if ( ac != null ) {
            if ( publication == null || !ac.equals( publication.getAc() ) ) {
                publication = getDaoFactory().getPublicationDao().getByAc( ac );

                if ( publication != null ) {
                    loadFormFields();
                }

            }
        } else if ( publication != null ) {
            ac = publication.getAc();
        }
    }

    private void loadFormFields() {
        for ( Annotation annotation : publication.getAnnotations() ) {
            if ( CvTopic.DATASET_MI_REF.equals( annotation.getCvTopic().getIdentifier() ) ) {
                String datasetText = annotation.getAnnotationText();

                SelectItem datasetSelectItem = getDatasetPopulator().createSelectItem( datasetText );
                datasetsSelectItems.add( datasetSelectItem );
            }
        }
    }

    public boolean isCitexploreOnline() {
        try {
            CitexploreClient citexploreClient = new CitexploreClient();
        } catch ( Exception e ) {
            return false;
        }

        return true;
    }

    public void newAutocomplete( ActionEvent evt ) {
        if ( identifier == null ) {
            addErrorMessage( "Cannot auto-complete", "ID is empty" );
            return;
        }

        // check if already exists
        Publication existingPublication = getDaoFactory().getPublicationDao().getByPubmedId( identifier );

        if ( existingPublication != null ) {
            publication = existingPublication;
            addWarningMessage( "Publication already exists", "Loaded from the database" );
            return;
        }

        publication = new Publication( IntactContext.getCurrentInstance().getInstitution(), identifier );
        autocomplete( publication, identifier );

        identifier = null;

        getUnsavedChangeManager().markAsUnsaved(publication);
    }

    public void doFormAutocomplete( ActionEvent evt ) {
        if ( publication.getShortLabel() != null ) {
            autocomplete( publication, publication.getShortLabel() );
        }
    }

    private void autocomplete( Publication publication, String id ) {
        CitexploreClient citexploreClient = null;

        try {
            citexploreClient = new CitexploreClient();
        } catch ( Exception e ) {
            addErrorMessage( "Cannot auto-complete", "Citexplore service is down at the moment" );
            return;
        }

        try {
            final Citation citation = citexploreClient.getCitationById( id );

            if ( citation == null ) {
                addErrorMessage( "No citation was found", "PMID: " + id );
                setPublication(null);
                return;
            }

            setPrimaryReference( id );

            publication.setFullName( citation.getTitle() );
            setJournal( citation.getJournalIssue().getJournal().getISOAbbreviation() + " (" +
                        citation.getJournalIssue().getJournal().getISSN() + ")" );
            setYear( citation.getJournalIssue().getYearOfPublication() );

            StringBuilder sbAuthors = new StringBuilder( 64 );

            Iterator<Author> authorIterator = citation.getAuthorCollection().iterator();
            while ( authorIterator.hasNext() ) {
                Author author = authorIterator.next();
                sbAuthors.append( author.getLastName() ).append( " " ).append( author.getInitials() ).append( "." );
                if ( authorIterator.hasNext() ) sbAuthors.append( ", " );
            }

            setAuthors( sbAuthors.toString() );

            addInfoMessage( "Auto-complete successful", "Fetched details for: " + id );

        } catch ( Throwable e ) {
            addErrorMessage( "Problem auto-completing publication", e.getMessage() );
            e.printStackTrace();
        }
    }

    public void newEmpty( ActionEvent evt ) {
        // check if already exists
        Publication existingPublication = getDaoFactory().getPublicationDao().getByPubmedId( identifier );

        if ( existingPublication != null ) {
            publication = existingPublication;
            addInfoMessage( "Publication already exists", "Loaded from the database" );
            return;
        }

        publication = new Publication( IntactContext.getCurrentInstance().getInstitution(), identifier );
        ac = null;

        getUnsavedChangeManager().markAsUnsaved(publication);
    }

    public void openByPmid( ActionEvent evt ) {
        if ( identifier == null || identifier.trim().length() == 0 ) {
            addErrorMessage( "PMID is empty", "No PMID was supplied" );
        } else {
            Publication publicationToOpen = getDaoFactory().getPublicationDao().getByPubmedId( identifier );

            if ( publicationToOpen == null ) {
                addErrorMessage( "PMID not found", "There is no publication with PMID '" + identifier + "'" );
            } else {
                publication = publicationToOpen;
                ac = publication.getAc();
            }
        }

    }

    public void doClose( ActionEvent evt ) {
        publication = null;
        ac = null;
    }

    public void doSaveAndClose( ActionEvent evt ) {
        doSave( evt );
        doClose( evt );
    }

    public void addDataset( ActionEvent evt ) {
        if ( datasetToAdd != null ) {
            datasetsSelectItems.add( getDatasetPopulator().createSelectItem( datasetToAdd ) );

            addAnnotation( CvTopic.DATASET_MI_REF, datasetToAdd );

            setUnsavedChanges( true );
        }
    }

    public void removeDatasets( ActionEvent evt ) {
        if ( datasetsToRemove != null ) {
            for ( String datasetToRemove : datasetsToRemove ) {
                Iterator<SelectItem> iterator = datasetsSelectItems.iterator();

                while ( iterator.hasNext() ) {
                    SelectItem selectItem = iterator.next();
                    if ( datasetToRemove.equals( selectItem.getValue() ) ) {
                        iterator.remove();
                    }
                }

                removeAnnotation( CvTopic.DATASET_MI_REF, datasetToRemove );
            }
            setUnsavedChanges( true );
        }
    }

    private String createExperimentShortLabel() {
        return getFirstAuthor()+"-"+getYear();
    }

    public String getAc() {
        if ( ac == null && publication != null ) {
            return publication.getAc();
        }
        return ac;
    }

    public void setAc( String ac ) {
        this.ac = ac;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication( Publication publication ) {
        this.publication = publication;
        this.ac = publication.getAc();
    }


    public String getJournal() {
        final String annot = findAnnotationText( CvTopic.JOURNAL_MI_REF );
        return annot;
    }


    public void setJournal( String journal ) {
        setAnnotation( CvTopic.JOURNAL_MI_REF, journal );
    }

    public String getContactEmail() {
        return findAnnotationText( CvTopic.CONTACT_EMAIL_MI_REF );
    }

    public void setContactEmail( String contactEmail ) {
        setAnnotation( CvTopic.CONTACT_EMAIL_MI_REF, contactEmail );
    }

    public String getSubmitted() {
        return findAnnotationText(SUBMITTED);
    }

    public void setSubmitted( String submitted ) {
        setAnnotation(SUBMITTED, submitted );
    }

    public String getCurationRequest() {
        return findAnnotationText(CURATION_REQUEST);
    }

    public void setCurationRequest( String requestedCuration ) {
        setAnnotation(CURATION_REQUEST, requestedCuration );
    }

    public Short getYear() {
        String strYear = findAnnotationText( CvTopic.PUBLICATION_YEAR_MI_REF );

        if ( strYear != null ) {
            return Short.valueOf( strYear );
        }

        return null;
    }

    public void setYear( Short year ) {
        setAnnotation( CvTopic.PUBLICATION_YEAR_MI_REF, year );
    }

    public String getIdentifier() {
        if ( publication != null ) {
            String id = getPrimaryReference();

            if ( id != null ) {
                identifier = id;
            }
        }

        return identifier;
    }

    public void setIdentifier( String identifier ) {
        this.identifier = identifier;

        if ( identifier != null && getAnnotatedObject() != null ) {
            setPrimaryReference( identifier );
        }
    }

    public String getPrimaryReference() {
        return findXrefPrimaryId( CvDatabase.PUBMED_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF );
    }

    public void setPrimaryReference( String id ) {
        setXref( CvDatabase.PUBMED_MI_REF, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF, id );
    }

    public String getAuthors() {
        return findAnnotationText( CvTopic.AUTHOR_LIST_MI_REF );
    }

    public void setAuthors( String authors ) {
        setAnnotation( CvTopic.AUTHOR_LIST_MI_REF, authors );
    }

    public String getOnHold() {
        return findAnnotationText( CvTopic.ON_HOLD );
    }

    public void setOnHold( String reason ) {
        setAnnotation( CvTopic.ON_HOLD, reason );
    }

    public String getAcceptedMessage() {
        return findAnnotationText( CvTopic.ACCEPTED );
    }

    public void setAcceptedMessage( String message ) {
        setAnnotation( CvTopic.ACCEPTED, message );
    }

    public String getImexId() {
        return findXrefPrimaryId(CvDatabase.IMEX_MI_REF, CvXrefQualifier.IMEX_PRIMARY_MI_REF);
    }

    public void setImexId(String imexId) {
        setXref( CvDatabase.IMEX_MI_REF, CvXrefQualifier.IMEX_PRIMARY_MI_REF, imexId );
    }


    public String getFirstAuthor() {
        final String authors = getAuthors();

        if ( authors != null ) {
            return authors.split( " " )[0];
        }

        return null;
    }

    public void acceptPublication(ActionEvent actionEvent) {
        UserSessionController userSessionController = (UserSessionController) getSpringContext().getBean("userSessionController");

        setAcceptedMessage("Accepted "+new SimpleDateFormat("yyyy-MMM-dd").format(new Date())+" by "+userSessionController.getCurrentUser().getLogin().toUpperCase());
    }

    public List<SelectItem> getDatasetsSelectItems() {
        return datasetsSelectItems;
    }

    public String getDatasetToAdd() {
        return datasetToAdd;
    }

    public void setDatasetToAdd( String datasetToAdd ) {
        this.datasetToAdd = datasetToAdd;
    }

    public String[] getDatasetsToRemove() {
        return datasetsToRemove;
    }

    public void setDatasetsToRemove( String[] datasetsToRemove ) {
        this.datasetsToRemove = datasetsToRemove;
    }

    public DatasetPopulator getDatasetPopulator() {
        return ( DatasetPopulator ) IntactContext.getCurrentInstance().getSpringContext().getBean( "datasetPopulator" );
    }


}
