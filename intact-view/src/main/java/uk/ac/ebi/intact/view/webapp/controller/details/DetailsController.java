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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;

import javax.faces.context.FacesContext;
import java.util.Collection;
import java.util.ArrayList;

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
@ViewController(viewIds = {"/pages/details/details.xhtml"})
public class DetailsController extends JpaBaseController {

    private static final Log log = LogFactory.getLog( DetailsController.class );

    private static final String INTERACTION_AC_PARAM = "interaction_ac";

    private Interaction interaction;

    @Autowired
    private SearchController searchController;

    @Autowired
    private UserQuery userQuery;

    @PreRenderView
    public void initialParams() {
        FacesContext context = FacesContext.getCurrentInstance();
        final String interactionAc = context.getExternalContext().getRequestParameterMap().get( INTERACTION_AC_PARAM );
        if ( log.isDebugEnabled() ) {
            log.debug( "DetailsController: @PreRenderView invoked" );
        }


        if ( interactionAc != null ) {
            log.debug( "Parameter " + INTERACTION_AC_PARAM + " was specified" );
            setInteractionAc( interactionAc );

            // Update interaction search
            userQuery.reset();
            userQuery.setSearchQuery( "interaction_id:"+interactionAc );
            SolrQuery solrQuery = userQuery.createSolrQuery();
            searchController.doBinarySearch( solrQuery );

        } else {
            log.debug( "Parameter " + INTERACTION_AC_PARAM + " was not specified" );
        }
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

    public void setInteractionAc( String interactionAc ) {
        if ( log.isDebugEnabled() ) {
            log.debug( "Calling setInteractionAc( '"+ interactionAc +"' )..." );
        }
        interaction = getDaoFactory().getInteractionDao().getByAc( interactionAc );
        if( interaction == null ) {
            addErrorMessage( "No interaction found in the database for ac: " + interactionAc, "" );
        }
    }

    public Experiment getExperiment() {
        // TODO handle multiple experiment (so far we don't have that kind of data in IntAct).
        return interaction.getExperiments().iterator().next();
    }

    private static Collection<String> publicationTopics = new ArrayList<String>( );

    private static final String AUTHOR_LIST = "MI:0636";
    private static final String JOURNAL = "MI:0885";
    private static final String PUBLICATION_YEAR = "MI:0886";
    private static final String CONTACT_EMAIL = "MI:0634";

    static {
        publicationTopics.add( AUTHOR_LIST );
        publicationTopics.add( JOURNAL );
        publicationTopics.add( PUBLICATION_YEAR );
        publicationTopics.add( CONTACT_EMAIL );
    }

    public Collection<Annotation> getExperimentAnnotations() {
        final Experiment experiment = getExperiment();
        Collection<Annotation> selectedAnnotations = new ArrayList<Annotation>( experiment.getAnnotations().size() );
        for ( Annotation annotation : experiment.getAnnotations() ) {
            if( ! publicationTopics.contains( annotation.getCvTopic().getIdentifier() )) {
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

    private String getAnnotationTextByMi( AnnotatedObject ao, final String mi ) {
        final Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel( ao, mi );
        if( annotation != null ) {
            return annotation.getAnnotationText();
        }
        return null;

    }
}