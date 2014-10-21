package uk.ac.ebi.intact.editor.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.primefaces.model.LazyDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.editor.controller.curate.AnnotatedObjectController;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.IntactModelledFeature;
import uk.ac.ebi.intact.jami.model.extension.IntactModelledParticipant;
import uk.ac.ebi.intact.model.*;

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;

/**
 * Search controller.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
@Controller
@Scope( "conversation.access" )
@ConversationName ("search")
@SuppressWarnings("unchecked")
public class SearchController extends AnnotatedObjectController {

    private static final Log log = LogFactory.getLog( SearchController.class );

    @Autowired
    private SearchQueryService searchService;


    private AnnotatedObject annotatedObject;
    private IntactPrimaryObject jamiObject;

    //////////////////
    // Constructors

    public SearchController() {
    }

    @Override
    public AnnotatedObject getAnnotatedObject() {
        return annotatedObject;
    }

    @Override
    public void setAnnotatedObject(AnnotatedObject annotatedObject) {
        this.annotatedObject = annotatedObject;
    }

    @Override
    public IntactPrimaryObject getJamiObject() {
        return jamiObject;
    }

    @Override
    public void setJamiObject(IntactPrimaryObject annotatedObject) {
        this.jamiObject = annotatedObject;
    }

    ///////////////
    // Actions

    public void searchIfQueryPresent(ComponentSystemEvent evt) {
        searchService.searchIfQueryPresent();
        searchService.searchJamiIfQueryPresent();
    }

    public String doQuickSearch() {
        String action = searchService.doQuickSearch();
        searchService.doQuickJamiSearch();
        return action;
    }

    public void clearQuickSearch(ActionEvent evt) {
        this.searchService.clearQuickSearch();
    }

    public boolean isEmptyQuery() {
        return searchService.isEmptyQuery();
    }

    public boolean hasNoResults() {
        return searchService.hasNoResults();

    }

    public boolean matchesSingleType() {

        return searchService.matchesSingleType();
    }

    public int countInteractionsByMoleculeAc( Interactor molecule ) {
        return searchService.countInteractionsByMoleculeAc(molecule);
    }

    public int countFeaturesByParticipantAc( Component comp ) {
        return searchService.countFeaturesByParticipantAc(comp);
    }

    public int countModelledFeaturesByParticipantAc( String ac ) {
        return searchService.countModelledFeaturesByParticipantAc(ac);
    }

	public int countParticipantsExpressIn( String biosourceAc ) {
		return searchService.countParticipantsExpressIn(biosourceAc);
	}

	public int countExperimentsByHostOrganism( String biosourceAc ) {
		return searchService.countExperimentsByHostOrganism(biosourceAc);
	}

	public int countInteractorsByOrganism( String biosourceAc ) {
		return searchService.countInteractorsByOrganism(biosourceAc);
	}

    public CvExperimentalRole getExperimentalRoleForParticipantAc( Component comp ) {
        return searchService.getExperimentalRoleForParticipantAc(comp);
    }

    public String getIdentityXref( Interactor molecule ) {
        return searchService.getIdentityXref(molecule);
    }

    public Experiment getFirstExperiment( Interaction interaction ) {
        return searchService.getFirstExperiment(interaction);
    }

    public int countExperimentsForPublication( Publication publication ) {
        return searchService.countExperimentsForPublication(publication);
    }

    public int countInteractionsForPublication( Publication publication ) {
        return searchService.countInteractionsForPublication(publication);
    }

    ///////////////////////////
    // Getters and Setters

    public String getQuery() {
        return searchService.getQuery();
    }

    public void setQuery( String query ) {
        searchService.setQuery(query);
    }

    public LazyDataModel<Publication> getPublications() {
        return searchService.getPublications();
    }

    public LazyDataModel<Experiment> getExperiments() {
        return searchService.getExperiments();
    }

    public LazyDataModel<Interaction> getInteractions() {
        return searchService.getInteractions();
    }

    public LazyDataModel<Interactor> getMolecules() {
        return searchService.getMolecules();
    }

    public LazyDataModel<CvObject> getCvobjects() {
        return searchService.getCvobjects();
    }

     public LazyDataModel<Feature> getFeatures() {
        return searchService.getFeatures();
    }

    public LazyDataModel<BioSource> getOrganisms() {
        return searchService.getOrganisms();
    }

    public LazyDataModel<Component> getParticipants() {
        return searchService.getParticipants();
    }

    public LazyDataModel<IntactComplex> getComplexes() {
        return searchService.getComplexes();
    }

    public LazyDataModel<IntactModelledParticipant> getModelledParticipants() {
        return searchService.getModelledParticipants();
    }

    public LazyDataModel<IntactModelledFeature> getModelledFeatures() {
        return searchService.getModelledFeatures();
    }

    public String getQuickQuery() {
        return searchService.getQuickQuery();
    }

    public void setQuickQuery(String quickQuery) {
        searchService.setQuickQuery(quickQuery);
    }

    public int getThreadTimeOut() {
        return searchService.getThreadTimeOut();
    }

    public void setThreadTimeOut(int threadTimeOut) {
        searchService.setThreadTimeOut(threadTimeOut);
    }

    public boolean isPublicationSearchEnabled() {
        return searchService.isPublicationSearchEnabled();
    }

    public boolean isComplexSearchEnabled() {
        return searchService.isComplexSearchEnabled();
    }
}
