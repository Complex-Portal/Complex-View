/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.controller.details;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persistence.dao.InteractionDao;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.details.complex.SimilarInteraction;
import uk.ac.ebi.intact.view.webapp.controller.details.complex.SimilarInteractionsMatrix;
import uk.ac.ebi.intact.view.webapp.controller.details.complex.SimpleInteractor;
import uk.ac.ebi.intact.view.webapp.controller.details.complex.TableHeaderController;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;

import javax.faces.context.FacesContext;
import java.util.*;

/**
 * Details controller.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
@Controller( "detailsBean" )
@Scope( "conversation.access" )
@ConversationName( "general" )
public class DetailsController extends JpaBaseController {

    private static final Log log = LogFactory.getLog( DetailsController.class );

    private static final String INTERACTION_AC_PARAM = "interactionAc";
    private static final String EXPERIMENT_AC_PARAM = "experimentAc";
    private static final String BINARY_PARAM = "binary";

    private static final String AUTHOR_LIST = "MI:0636";

    private static final Collection<String> publicationTopics = new ArrayList<String>();
    private static final String JOURNAL = "MI:0885";
    private static final String PUBLICATION_YEAR = "MI:0886";

    private static final String CONTACT_EMAIL = "MI:0634";

    static {
        publicationTopics.add( AUTHOR_LIST );
        publicationTopics.add( JOURNAL );
        publicationTopics.add( PUBLICATION_YEAR );
    }

    private String interactionAc;
    private String experimentAc;
    private String binary;

    private Interaction interaction;

    private Experiment experiment;

    @Transactional(readOnly = true)
    public void loadData() {

        log.info( "DetailsController.loadData" );

        UserQuery userQuery = (UserQuery) getBean("userQuery");
        SearchController searchController = (SearchController) getBean("searchBean");

        if( interactionAc != null && experimentAc != null ) {
            addErrorMessage( "Please either request an interaction or an experiment accession number.",
                             "Both were specified." );
            return;
        }

        if ( interactionAc != null ) {
            if ( log.isDebugEnabled() ) log.debug( "Parameter " + INTERACTION_AC_PARAM + " was specified" );
            setInteractionAc( interactionAc );

            // Update interaction search
            userQuery.reset();
            userQuery.setSearchQuery( "interaction_id:" + interactionAc );
            SolrQuery solrQuery = userQuery.createSolrQuery();
            searchController.doBinarySearch( solrQuery );

        } else if ( experimentAc != null ) {

            if ( log.isDebugEnabled() ) log.debug( "Parameter " + EXPERIMENT_AC_PARAM + " was specified" );
            setExperimentAc( experimentAc );

            // TODO Update interaction search when experiment ACs are integrated in the Solr index
            userQuery.reset();
        }

        if (binary != null) {
            String[] interactorAcs = binary.split(",");

            if (interactorAcs.length != 2) {
                addErrorMessage("When the binary parameter is specified, two comma-separated interactor ACs are expected",
                        "Found: "+interactorAcs.length);
                return;
            }

            List<Interaction> interactions = getDaoFactory().getInteractionDao()
                    .getInteractionsForProtPairAc(interactorAcs[0], interactorAcs[1]);

            if (interactions.size() > 0) {
                Interaction binaryInteraction = interactions.get(0);
                setInteraction(binaryInteraction);

                // Update interaction search
                userQuery.reset();
                userQuery.setSearchQuery( interactorAcs[0] + " AND " + interactorAcs[1] );
                SolrQuery solrQuery = userQuery.createSolrQuery();
                searchController.doBinarySearch( solrQuery );

            } else {
                addErrorMessage("No interactions were found", "");
                return;
            }
        }
    }

    public String getInteractionAc() {
        return interactionAc;
    }

    public String getExperimentAc() {
        return experimentAc;
    }

    public String getBinary() {
        return binary;
    }

    public void setBinary( String binary ) {
        this.binary = binary;
    }

    @Transactional(readOnly = true)
    public void setExperimentAc( String experimentAc ) {
        if ( log.isDebugEnabled() ) log.debug( "Calling setExperimentAc( '" + experimentAc + "' )..." );
        experiment = getDaoFactory().getExperimentDao().getByAc( experimentAc );
        if ( experiment == null ) {
            addErrorMessage( "No experiment found in the database for ac: " + experimentAc, "Please try with an other AC." );
        } else {
            if ( log.isDebugEnabled() ) log.debug( "Found experiment: " + experiment.getShortLabel() );
        }
    }

    public boolean hasExperiment() {
        return experiment != null;
    }

    public void setExperiment( Experiment experiment ) {
        this.experiment = experiment;
    }

    public boolean hasInteraction() {
        return interaction != null;
    }

    public Interaction getInteraction() {
        return interaction;
    }

    public void setInteraction( Interaction interaction ) {
        this.interaction = interaction;
    }

    @Transactional(readOnly = true)
    public void setInteractionAc( String interactionAc ) {
        if ( log.isDebugEnabled() ) log.debug( "Calling setInteractionAc( '" + interactionAc + "' )..." );
        interaction = getDaoFactory().getInteractionDao().getByAc( interactionAc );

        if (interaction == null) {
            interaction = getDaoFactory().getInteractionDao().getByXref( interactionAc );
        }

        if ( interaction == null ) addErrorMessage( "No interaction found in the database for ac: " + interactionAc, "" );
    }

    public Experiment getExperiment() {
        Experiment exp = null;
        if( experiment != null ) {
            exp = experiment;
        }

        // TODO handle multiple experiment (so far we don't have that kind of data in IntAct).
        // fall back on the experiment of the interaction loaded.
        if( interaction != null ) {
            exp = interaction.getExperiments().iterator().next();
        }
        return exp;
    }

    public Collection<Annotation> getExperimentAnnotations() {
        final Experiment experiment = getExperiment();
        Collection<Annotation> selectedAnnotations = new ArrayList<Annotation>( experiment.getAnnotations().size() );
        for ( Annotation annotation : experiment.getAnnotations() ) {
            boolean found = false;
            
            for (Annotation pubAnnotation : experiment.getPublication().getAnnotations()) {
                if ( pubAnnotation.getCvTopic().getIdentifier().equals(annotation.getCvTopic().getIdentifier()) ) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                selectedAnnotations.add( annotation );
            }
        }
        return selectedAnnotations;
    }
    
    public Collection<Annotation> getPublicationAnnotations() {
        final Publication publication = getExperiment().getPublication();
        Collection<Annotation> selectedAnnotations = new ArrayList<Annotation>( publication.getAnnotations().size() );
        
        for ( Annotation annotation : publication.getAnnotations() ) {
            if ( !publicationTopics.contains( annotation.getCvTopic().getIdentifier() ) ) {
                selectedAnnotations.add( annotation );
            }
        }
            
        return selectedAnnotations;
    }

    public String getAuthorList() {
        return getAnnotationTextByMi( getExperiment(), AUTHOR_LIST );
    }

    public String getJournal() {
        return getAnnotationTextByMi( getExperiment(), JOURNAL );
    }

    public String getPublicationYear() {
        return getAnnotationTextByMi( getExperiment(), PUBLICATION_YEAR );
    }

    public String getContactEmail() {
        return getAnnotationTextByMi( getExperiment(), CONTACT_EMAIL );
    }

    public boolean isFeaturesAvailable(){
        boolean featuresAvailable = false;
        Interaction interaction = getInteraction();
        for(Component component : interaction.getComponents()){
            featuresAvailable = featuresAvailable || (component.getBindingDomains().size() > 0);
        }
        return featuresAvailable;
    }

    private String getAnnotationTextByMi( AnnotatedObject ao, final String mi ) {
        final Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel( ao, mi );
        if ( annotation != null ) {
            return annotation.getAnnotationText();
        }
        return null;
    }

    ///////////////////
    // Complex View

    private SimilarInteractionsMatrix matrix;

    @Autowired
    private TableHeaderController tableHeaderController;

    @Transactional(readOnly = true)
    public SimilarInteractionsMatrix getSimilarInteractionMatrix() {

        if( matrix != null ) {

            if( matrix.getInvestigatedInteraction().getAc().equals( interaction.getAc() ) ) {
                return matrix;
            }

            // reset cache
            matrix = null;
        }

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        final InteractionDao interactionDao = daoFactory.getInteractionDao();
        final long start = System.currentTimeMillis();

        if ( log.isDebugEnabled() ) {
            StringBuilder sb = new StringBuilder( 512 );
            for ( Component component : interaction.getComponents() ) {
                sb.append( component.getInteractor().getShortLabel() ).append( "   " );
            }
            log.debug( "It has " + interaction.getComponents().size() + " participants [" + sb.toString().trim() + "]" );
        }

        final Set<SimpleInteractor> referenceMembers = prepareMembers( interaction );

        Map<String, SimilarInteraction> map = Maps.newHashMap();

        for ( Component component : interaction.getComponents() ) {
            final Interactor interactor = component.getInteractor();
            final List<Interaction> interactions = interactionDao.getInteractionsByInteractorAc( interactor.getAc() );

            for ( Interaction i : interactions ) {

                if ( i.getAc().equals( interaction.getAc() ) ) {
                    continue; // skipping this interaction
                }

                SimilarInteraction si = null;
                final String key = i.getAc();
                if ( map.containsKey( key ) ) {
                    si = map.get( key );
                } else {
                    si = new SimilarInteraction( i.getAc(), i.getShortLabel(), i.getComponents().size() );
                    map.put( key, si );
                }

                // update si
                Set<SimpleInteractor> members = prepareMembers( i );
                for ( SimpleInteractor member : members ) {
                    if ( referenceMembers.contains( member ) ) {
                        si.addMember( member );
                    } else {
                        si.addOthers( member );
                    }
                }
            }
        }

        // sort interaction by decreasing order of member interactors
        List<SimilarInteraction> similarInteractions = Lists.newArrayList( map.values() );
        Collections.sort( similarInteractions, new Comparator<SimilarInteraction>() {
            public int compare( SimilarInteraction i1, SimilarInteraction i2 ) {
                return i2.getMemberCount() - i1.getMemberCount();
            }
        } );

        final long stop = System.currentTimeMillis();

        if ( log.isDebugEnabled() ) {
            log.debug( "Time elapsed: " + ( stop - start ) + "ms" );
            log.debug( "Results collected (" + map.size() + " interactions):" );

            for ( SimilarInteraction si : similarInteractions ) {

                log.debug( StringUtils.rightPad( si.getShortLabel(), 20 ) + " " +
                           StringUtils.rightPad( si.getMemberCount() + "/" + si.getTotalParticipantCount(), 10 ) + "\t[" +
                           printSimpleInteractors( si.getMembers() ) + "]" );
            }
        }

        // ordering reference members alphabetically
        List<SimpleInteractor> orderedReferenceMembers = Lists.newArrayList( referenceMembers );
        Collections.sort(  orderedReferenceMembers, new Comparator<SimpleInteractor>() {
            public int compare( SimpleInteractor o1, SimpleInteractor o2 ) {
                return o1.getShortLabel().compareTo( o2.getShortLabel() );
            }
        } );

        matrix = new SimilarInteractionsMatrix( new SimpleInteractor( interaction.getAc(),
                                                                      interaction.getShortLabel(),
                                                                      interaction.getFullName() ),
                                                similarInteractions,
                                                orderedReferenceMembers );

        tableHeaderController.setLabels( orderedReferenceMembers );

        return matrix;
    }

    public String printSimpleInteractors( Collection<SimpleInteractor> participants ) {
        StringBuilder sb = new StringBuilder( 512 );
        for ( SimpleInteractor i : participants ) {
            sb.append( i.getShortLabel() ).append( " " );
        }
        return sb.toString().trim();
    }

    private Set<SimpleInteractor> prepareMembers( Interaction interaction ) {
        Set<SimpleInteractor> members = Sets.newHashSet();
        for ( Component component : interaction.getComponents() ) {
            final Interactor interactor = component.getInteractor();
            members.add( new SimpleInteractor( interactor.getAc(), interactor.getShortLabel() ) );
        }
        return members;
    }
}