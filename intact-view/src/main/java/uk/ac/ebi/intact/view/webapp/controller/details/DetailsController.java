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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persistence.dao.InteractionDao;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.view.webapp.controller.ContextController;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.details.complex.SimilarInteraction;
import uk.ac.ebi.intact.view.webapp.controller.details.complex.SimilarInteractionsMatrix;
import uk.ac.ebi.intact.view.webapp.controller.details.complex.SimpleInteractor;
import uk.ac.ebi.intact.view.webapp.controller.details.complex.TableHeaderController;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;
import uk.ac.ebi.intact.view.webapp.model.ParticipantLazyDataModel;

import javax.faces.context.FacesContext;
import javax.persistence.Query;
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
    private static final String DATASET = "MI:0875";
    private static final String CONTACT_EMAIL = "MI:0634";

    private static final String _variable_condition = "variable_condition";
    private static final String _variable_condition_2 = "variable_condition_2";
    private static final String _variable = "variable";
    private static final String _variable_2 = "variable_2";

    static {
        publicationTopics.add( AUTHOR_LIST );
        publicationTopics.add( JOURNAL );
        publicationTopics.add( PUBLICATION_YEAR );
        publicationTopics.add( DATASET );
    }

    private String interactionAc;
    private String experimentAc;
    private String binary;

    private Interaction interaction;

    private Experiment experiment;
    private int numberInteractions=0;
    private int numberParticipants=0;
    private ParticipantLazyDataModel participants;
    private boolean featureAvailable;
    private int numberOfInteractorsInExperiment=0;

    private Map variableName2title;
    private Map<String,Map<String,List<Integer>>> variableName2conditions;

    private String jsonExperimentInteractions;

    public DetailsController(){
        variableName2title = new HashMap<String,String>();
        variableName2conditions = new HashMap<String,Map<String,List<Integer>>>();
    }

    @Transactional(readOnly = true)
    public void loadData() {
        if (!FacesContext.getCurrentInstance().isPostback()) {
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
                loadInteraction();

            } else if ( experimentAc != null ) {

                if ( log.isDebugEnabled() ) log.debug( "Parameter " + EXPERIMENT_AC_PARAM + " was specified" );
                loadExperiment();
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
                    loadParticipants();
                    loadJsonExperimentInteractions();

                    // Update interaction search
                    userQuery.reset();
                    userQuery.setSearchQuery(FieldNames.ID+":"+interactorAcs[0] + " AND " + FieldNames.ID+":" + interactorAcs[1] );
                    SolrQuery solrQuery = userQuery.createSolrQuery();
                    searchController.doBinarySearch( solrQuery );

                } else {
                    addErrorMessage("No interactions were found", "");
                    return;
                }

                ContextController contextController = (ContextController) getBean("contextController");
                contextController.setActiveTabIndex(5);
            }

            this.numberInteractions = countInteractionNumbers();
            loadNumberOfInteractorsInExperiment();
        }
    }

    private int countInteractionNumbers(){
        Experiment exp = getExperiment();

        if (exp == null){
            return 0;
        }

        return getIntactContext().getDaoFactory().getExperimentDao().countInteractionsForExperimentWithAc(exp.getAc());
    }

    private int countParticipantNumbers(){

        if (interaction == null){
            return 0;
        }
        Long l = (Long) getIntactContext().getDaoFactory().getEntityManager().createQuery("select count(distinct p.ac) from InteractionImpl i join i.components as p " +
                "where i.ac = :ac").setParameter("ac", interactionAc).getSingleResult();

        return l.intValue();
    }

    public int getNumberInteractions() {
        return numberInteractions;
    }

    public int getNumberParticipants() {
        return numberParticipants;
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

    public void setExperimentAc( String experimentAc ) {
        this.experimentAc = experimentAc;
    }

    @Transactional(readOnly = true)
    public void loadExperiment( ) {
        if ( log.isDebugEnabled() ) log.debug( "Calling setExperimentAc( '" + experimentAc + "' )..." );
        experiment = getDaoFactory().getExperimentDao().getByAc( experimentAc );
        if ( experiment == null ) {
            addErrorMessage( "No experiment found in the database for ac: " + experimentAc, "Please try with an other AC." );
        } else {
            if ( log.isDebugEnabled() ) log.debug( "Found experiment: " + experiment.getShortLabel() );
        }

        loadNumberOfInteractorsInExperiment();
        loadJsonExperimentInteractions();
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
        this.interactionAc = interaction.getAc();
    }

    public void setInteractionAc( String interactionAc ) {
        this.interactionAc = interactionAc;
    }

    @Transactional(readOnly = true)
    public void loadInteraction( ) {
        if ( log.isDebugEnabled() ) log.debug( "Calling setInteractionAc( '" + interactionAc + "' )..." );
        interaction = getDaoFactory().getInteractionDao().getByAc( interactionAc );
        experiment = getExperiment();

        if (interaction == null) {
            interaction = getDaoFactory().getInteractionDao().getByXref( interactionAc );
        }

        if ( interaction == null ) addErrorMessage( "No interaction found in the database for ac: " + interactionAc, "" );
        loadParticipants();
        loadNumberOfInteractorsInExperiment();
        loadJsonExperimentInteractions();
    }

    private void loadParticipants(){
        this.numberParticipants = countParticipantNumbers();

        this.participants = new ParticipantLazyDataModel(getIntactContext().getDataContext(), getIntactContext().getDaoFactory().getEntityManager(), this.interactionAc, this.numberParticipants);
        loadFeatureNumber();
    }

    public Experiment getExperiment() {
        Experiment exp = null;
        if( experiment != null ) {
            return experiment;
        }

        if( interaction != null && !interaction.getExperiments().isEmpty() ) {

            exp = interaction.getExperiments().iterator().next();
        }
        return exp;
    }

    /**
     * Get number of interactors looking at all the interactions in one experiment
     * @return
     */
    @Transactional(readOnly = true)
    public void loadNumberOfInteractorsInExperiment(){
        Long number = (Long) getIntactContext().getDaoFactory().getEntityManager().createQuery("select count(distinct interactor.ac) " +
                "from Experiment e join e.interactions as i join i.components as comp join comp.interactor as interactor " +
                "where e.ac = :experimentAc").setParameter("experimentAc", getExperiment().getAc()).getSingleResult();
        this.numberOfInteractorsInExperiment = number.intValue();
    }

    public int getNumberOfInteractorsInExperiment(){
        return numberOfInteractorsInExperiment;
    }

    /**
     * Get a JSON string needed to visualize all the interactions in one experiment.
     * It also takes into account dynamic interactions looking at the experiment and
     * interaction annotations.
     * @return
     */
    @Transactional(readOnly = true)
    public void loadJsonExperimentInteractions(){
        this.variableName2conditions.clear();
        this.variableName2title.clear();

        final Experiment experiment = getExperiment();

        //Get experiment dynamic variables
        Collection<Annotation> experimentAnnotations = experiment.getAnnotations();
        for(Annotation annotation:experimentAnnotations){
            if(annotation.getCvTopic() != null){
                if(annotation.getCvTopic() != null && (annotation.getCvTopic().getShortLabel().equalsIgnoreCase(_variable)
                        || annotation.getCvTopic().getShortLabel().equalsIgnoreCase(_variable_2))){
                    variableName2title.put(annotation.getCvTopic().getShortLabel(),annotation.getAnnotationText());
                    variableName2conditions.put(annotation.getCvTopic().getShortLabel(), new HashMap<String, List<Integer>>());
                }
            }
        }

        // we can export dynamic interactions only if experiment has variable annotations
        if (!variableName2conditions.isEmpty() && !variableName2title.isEmpty()){

            //Look for interactions
            Integer baitPreyCount = 0;
            Map<Integer,String> nonRedundantInteractions = new HashMap<Integer, String>();
            Collection<Interaction> interactions = experiment.getInteractions();
            for(Interaction interaction:interactions){
                List<String> otherInteractors = new ArrayList<String>();
                String mainInteractor = "";
                Collection<Component> components = interaction.getComponents();
                //Find dynamic annotations
                Map<String,String> conditions = new HashMap<String,String>();
                for(Annotation annotation:interaction.getAnnotations()){
                    if(annotation.getCvTopic() != null){
                        if(annotation.getCvTopic().getShortLabel().equalsIgnoreCase(_variable_condition)
                                || annotation.getCvTopic().getShortLabel().equalsIgnoreCase(_variable_condition_2)){
                            conditions.put(annotation.getCvTopic().getShortLabel(),annotation.getAnnotationText());
                        }
                    }
                }


                //Collect main and other interactors
                for(Component component:components){
                    Collection<CvExperimentalRole> experimentalRoles = component.getExperimentalRoles();
                    boolean isBait = false;
                    boolean isSomethingElse = false;
                    for (CvExperimentalRole experimentalRole:experimentalRoles){
                        if(experimentalRole.getShortLabel().equalsIgnoreCase("bait")){
                            isBait = true;
                        } else {
                            isSomethingElse = true;
                        }
                    }
                    Interactor interactor = component.getInteractor();
                    String interactorAc =interactor.getShortLabel();
                    if(interactorAc.length() == 0){
                        interactorAc = interactor.getAc();
                    }
                    if(isSomethingElse){
                        if(mainInteractor.length() ==0){
                            mainInteractor = interactorAc;
                        } else {
                            otherInteractors.add(interactorAc);
                        }
                    } else {
                        if(isBait){
                            mainInteractor = interactorAc;
                        } else {
                            otherInteractors.add(interactorAc);
                        }
                    }

                }


                //Create nonredundat interactions and a mapping from conditions to non redundant interactions
                for(String otherInteractor:otherInteractors){
                    String ohterMain = "'"+otherInteractor+"','"+mainInteractor+"'";
                    String mainOther = "'"+mainInteractor+"','"+otherInteractor+"'";
                    if(nonRedundantInteractions.values().contains(mainOther) || nonRedundantInteractions.values().contains(ohterMain)){
                        for(Integer interactionKey:nonRedundantInteractions.keySet()){
                            if(nonRedundantInteractions.get(interactionKey).equals(mainOther) ||  nonRedundantInteractions.get(interactionKey).equals(ohterMain)){
                                for(String conditionKey:conditions.keySet()){
                                    String conditionValue = conditions.get(conditionKey);
                                    String variableKey = extractConditionNameFor(conditionKey);
                                    Map<String,List<Integer>> conditionToNonRedundantInteractions = variableName2conditions.get(variableKey);
                                    if(conditionToNonRedundantInteractions.get(conditionValue) != null){
                                        conditionToNonRedundantInteractions.get(conditionValue).add(interactionKey);
                                    } else {
                                        List<Integer> interactionList = new ArrayList<Integer>();
                                        interactionList.add(interactionKey);
                                        conditionToNonRedundantInteractions.put(conditionValue, interactionList);
                                    }
                                }
                            }

                        }
                    } else {
                        nonRedundantInteractions.put(baitPreyCount, mainOther);
                        for(String conditionKey:conditions.keySet()){
                            String conditionValue = conditions.get(conditionKey);
                            String variableKey = extractConditionNameFor(conditionKey);
                            Map<String,List<Integer>> conditionToNonRedundantInteractions = variableName2conditions.get(variableKey);
                            if(conditionToNonRedundantInteractions.get(conditionValue) != null){
                                conditionToNonRedundantInteractions.get(conditionValue).add(baitPreyCount);
                            } else {
                                List<Integer> interactionList = new ArrayList<Integer>();
                                interactionList.add(baitPreyCount);
                                conditionToNonRedundantInteractions.put(conditionValue, interactionList);
                            }
                        }
                        baitPreyCount++;
                    }
                }
            }
            //create json INTERACTIONS
            StringBuffer json = new StringBuffer();
            json.append("'interactions':{");
            int index = 0;
            for(int i=0; i< nonRedundantInteractions.size(); i++){
                String nonRedundantInteraction = nonRedundantInteractions.get(i);
                json.append("'").append(i+"':[").append(nonRedundantInteraction).append("]");
                if (index < nonRedundantInteractions.size() - 1){
                    json.append(",");
                }
                index++;
            }
            json.append("}");


            //create json FILTERS
            json.append(",'filters':{");
            index = 0;
            for(String variableName:variableName2conditions.keySet()){
                json.append("'").append(variableName).append("': {'title': '").append(variableName2title.get(variableName)).append("','presentation': 'radio', 'active': true, 'dataType': 'edges', 'data': {");
                Map<String,List<Integer>> conditionToNonRedundantInteractions = variableName2conditions.get(variableName);
                int index3 = 0;

                for(String condition:conditionToNonRedundantInteractions.keySet()){
                    List<Integer> listOfnonRedundantInteractions = conditionToNonRedundantInteractions.get(condition);
                    json.append("'").append(condition).append("':[");
                    if(listOfnonRedundantInteractions != null) {
                        int index2 = 0;
                        for(Integer intKey:listOfnonRedundantInteractions){
                            json.append("'"+intKey).append("'");
                            if (index2 < listOfnonRedundantInteractions.size() - 1){
                                json.append(",");
                            }
                            index2++;
                        }
                    }
                    json.append("]");
                    if (index3 < conditionToNonRedundantInteractions.size() - 1){
                        json.append(",");
                    }
                    index3++;
                }
                json.append("}}");
                if(index < variableName2conditions.size() - 1){
                    json.append(",");
                }
                index++;
            }

            json.append("}");
            this.jsonExperimentInteractions = json.toString();
        }
        else {
            this.jsonExperimentInteractions=null;
        }
    }

    private String extractConditionNameFor(String variableName) {
        if (variableName == null){
            return null;
        }
        else if (_variable_condition.equalsIgnoreCase(variableName)){
            return _variable;
        }
        else if (_variable_condition_2.equalsIgnoreCase(variableName)){
            return _variable_2;
        }

        return null;
    }

    /**
     * Get a JSON string needed to visualize all the interactions in one experiment.
     * It also takes into account dynamic interactions looking at the experiment and
     * interaction annotations.
     * @return
     */
    public String getJsonExperimentInteractions(){
        return jsonExperimentInteractions;
    }

    /**
     * Get dynamic annotations from the experiment
     * @return
     */
    public Collection<Annotation> getExperimentDynamicAnnotations() {
        Collection<Annotation> annotations = new ArrayList<Annotation>();
        for(Annotation annotation:getExperimentAnnotations()){
            if(annotation.getCvTopic().getShortLabel().equalsIgnoreCase(_variable) || annotation.getCvTopic().getShortLabel().equalsIgnoreCase(_variable_2)){
                annotations.add(annotation);
            }
        }
        return annotations;
    }

    public Collection<Annotation> getExperimentAnnotations() {
        final Experiment experiment = getExperiment();
        Collection<Annotation> selectedAnnotations = new ArrayList<Annotation>( experiment.getAnnotations().size() );
        for ( Annotation annotation : experiment.getAnnotations() ) {
            
            if (!publicationTopics.contains( annotation.getCvTopic().getIdentifier() ) ) {
                selectedAnnotations.add( annotation );
            }
        }
        return selectedAnnotations;
    }
    
    public Collection<Annotation> getPublicationAnnotations() {
        final Publication publication = getExperiment().getPublication();
        Collection<Annotation> selectedAnnotations = new ArrayList<Annotation>( publication.getAnnotations().size() );
        
        for ( Annotation annotation : publication.getAnnotations() ) {
            if ( publicationTopics.contains( annotation.getCvTopic().getIdentifier() )
                    && !CvTopic.AUTHOR_LIST_MI_REF.equals(annotation.getCvTopic().getIdentifier())
                    && !CvTopic.PUBLICATION_YEAR_MI_REF.equals(annotation.getCvTopic().getIdentifier())
                    && !CvTopic.JOURNAL_MI_REF.equals(annotation.getCvTopic().getIdentifier())) {
                selectedAnnotations.add( annotation );
            }
        }
            
        return selectedAnnotations;
    }

    public String getAuthorList() {
        return getAnnotationTextByMi( getExperiment().getPublication(), AUTHOR_LIST );
    }

    public String getJournal() {
        return getAnnotationTextByMi( getExperiment().getPublication(), JOURNAL );
    }

    public String getPublicationYear() {
        return getAnnotationTextByMi( getExperiment().getPublication(), PUBLICATION_YEAR );
    }

    public String getContactEmail() {
        return getAnnotationTextByMi( getExperiment().getPublication(), CONTACT_EMAIL );
    }

    @Transactional(readOnly = true)
    public boolean isFeaturesAvailable(){
        return featureAvailable;
    }

    @Transactional(readOnly = true)
    public void loadFeatureNumber(){
        if (interactionAc == null){
            featureAvailable=false;
            return;
        }
        Long number = (Long) getIntactContext().getDaoFactory().getEntityManager().createQuery("select count(f.ac) from Feature f join f.component as c " +
                "join c.interaction as i where i.ac = :ac").setParameter("ac", interactionAc).getSingleResult();

        featureAvailable = number > 0;
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

        final long start = System.currentTimeMillis();

        DaoFactory factory = getDaoFactory();
        InteractionDao interactionDao = factory.getInteractionDao();
        Query query = factory.getEntityManager().
                createQuery("select distinct i.ac from InteractionImpl i join i.components as comp join comp.interactor as inter where i.ac <> :interactionAc and " +
                        "inter.ac in (select inter2.ac from Component comp2 join comp2.interaction as i2 join comp2.interactor as inter2 where i2.ac = :interactionAc)");

        query.setParameter("interactionAc", interaction.getAc());

        List<String> similarInteractionAcs = query.getResultList();
        TreeSet<SimilarInteraction> similarInteractionTreeSet = new TreeSet<SimilarInteraction>(new Comparator<SimilarInteraction>() {
            public int compare( SimilarInteraction i1, SimilarInteraction i2 ) {
                return i2.getMemberCount() - i1.getMemberCount();
            }
        });

        final TreeSet<SimpleInteractor> referenceMembers = prepareMembers( interaction );

        for ( String ac : similarInteractionAcs ) {

            InteractionImpl i = interactionDao.getByAc(ac);

            SimilarInteraction si = new SimilarInteraction( i.getAc(), i.getShortLabel(), i.getComponents().size() );
            similarInteractionTreeSet.add(si);

            // update si
            TreeSet<SimpleInteractor> members = prepareMembers( i );
            for ( SimpleInteractor member : members ) {
                si.addMember( member );
            }
        }

        final long stop = System.currentTimeMillis();

        if ( log.isDebugEnabled() ) {
            log.debug( "Time elapsed: " + ( stop - start ) + "ms" );
            log.debug( "Results collected (" + similarInteractionTreeSet.size() + " interactions):" );

            for ( SimilarInteraction si : similarInteractionTreeSet ) {

                log.debug( StringUtils.rightPad( si.getShortLabel(), 20 ) + " " +
                           StringUtils.rightPad( si.getMemberCount() + "/" + si.getTotalParticipantCount(), 10 ) + "\t[" +
                           printSimpleInteractors( si.getMembers() ) + "]" );
            }
        }

        matrix = new SimilarInteractionsMatrix( new SimpleInteractor( interaction.getAc(),
                                                                      interaction.getShortLabel(),
                                                                      interaction.getFullName() ),
                                                similarInteractionTreeSet,
                                                referenceMembers );

        tableHeaderController.setLabels( referenceMembers );

        return matrix;
    }

    public String printSimpleInteractors( Collection<SimpleInteractor> participants ) {
        StringBuilder sb = new StringBuilder( 512 );
        for ( SimpleInteractor i : participants ) {
            sb.append( i.getShortLabel() ).append( " " );
        }
        return sb.toString().trim();
    }

    private TreeSet<SimpleInteractor> prepareMembers( Interaction interaction ) {
        TreeSet<SimpleInteractor> members = new TreeSet<SimpleInteractor>(new Comparator<SimpleInteractor>() {
            public int compare( SimpleInteractor o1, SimpleInteractor o2 ) {
                return o1.getShortLabel().compareTo( o2.getShortLabel() );
            }
        } );

        for ( Component component : interaction.getComponents() ) {
            final Interactor interactor = component.getInteractor();
            members.add( new SimpleInteractor( interactor.getAc(), interactor.getShortLabel() ) );
        }
        return members;
    }

    public String getPublicationTitle(){
        Experiment exp = getExperiment();

        if (exp != null && exp.getPublication() != null && exp.getPublication().getFullName() != null){
            return exp.getPublication().getFullName();
        }
        else if (exp != null && exp.getFullName() != null){
            return exp.getFullName();
        }
        return "-";
    }

    public ParticipantLazyDataModel getParticipants() {
        return participants;
    }
}