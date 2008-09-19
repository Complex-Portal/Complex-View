/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.controller.application;

import org.springframework.stereotype.Controller;
import org.springframework.context.annotation.Scope;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import uk.ac.ebi.intact.psimitab.search.IntactSearchEngine;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;
import uk.ac.ebi.intact.view.webapp.IntactViewException;
import uk.ac.ebi.intact.binarysearch.webapp.generated.Index;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.util.ols.Term;
import uk.ac.ebi.intact.util.ols.OlsUtils;
import uk.ac.ebi.intact.util.ols.OlsClient;
import uk.ac.ebi.ook.web.services.Query;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.rmi.RemoteException;

import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.tab.model.InteractionType;
import psidev.psi.mi.tab.model.InteractionDetectionMethod;
import psidev.psi.mi.tab.model.CrossReference;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * Backing bean for Ontology Search and Autocomplete feature
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
@Controller( "ontologyBean" )
@Scope( "singleton" )
public class OntologyBean implements Serializable {

    private static final Log log = LogFactory.getLog( OntologyBean.class );

    public static final String DEFAULT_CONFIG_FILE_INIT_PARAM = "intact.DEFAULT_CONFIG_FILE";
    public static final String DEFAULT_INDEX_LOCATION_INIT_PARAM = "intact.DEFAULT_INDEX";
    public static final String DEFAULT_INTERACTOR_INDEX_LOCATION_INIT_PARAM = "intact.DEFAULT_INTERACTOR_INDEX";

    private SearchConfig config;
    private String configFileLocation;

    //ontology list
    private List<OntologyVO> ontologyList;
    private List<OntologyVO> matchedOntologyList;
    private List<IntactBinaryInteraction> binaryInteractionList = new ArrayList<IntactBinaryInteraction>( 12000 );

    public OntologyBean() {
        FacesContext context = FacesContext.getCurrentInstance();

        configFileLocation = context.getExternalContext().getInitParameter( DEFAULT_CONFIG_FILE_INIT_PARAM );

        try {
            if ( log.isInfoEnabled() ) log.info( "Trying to read configuration from: " + configFileLocation );
            config = WebappUtils.readConfiguration( configFileLocation );
        }
        catch ( IntactViewException e ) {
            e.printStackTrace();
        }

        if ( config == null ) {
            if ( log.isInfoEnabled() ) log.info( "No configuration File found. First time setup" );
        } else {
            try {
                loadOntologies();
            } catch ( IOException io ) {
                io.printStackTrace();
            }
        }


    }

    public void loadOntologies() throws IOException {

        final Index defaultIndex = WebappUtils.getDefaultIndex( config );

        IntactSearchEngine engine = new IntactSearchEngine( defaultIndex.getLocation() );


        SearchResult<IntactBinaryInteraction> results ;

        int firstResult = 0;
        int maxResults = 100;
        do {
            if ( log.isDebugEnabled() ) {
            log.debug( "from loadontologies " + firstResult + "  " + maxResults );
            }


            results = engine.search( "*", firstResult, maxResults );


            this.getBinaryInteractionList().addAll( results.getData() );

            firstResult += maxResults;
        } while ( firstResult < results.getTotalCount() );


        populateOntologyList( binaryInteractionList );
    }

    /**
     *  Main method that populates the ontology list
     *  Currently loads the term and its parents for InteractionType,InteractionDetMethod and Interactor Properties
      * @param results List of BinaryInteractions from search
     */
    private void populateOntologyList( List<IntactBinaryInteraction> results ) {

        if ( results == null ) {
            throw new NullPointerException( "You must give a non null results" );
        }

        Set<OntologyVO> allOntologies = new HashSet<OntologyVO>();
        Set<OntologyVO> ontologySetIntType = null;
        Set<OntologyVO> ontologySetDecMethod = null;
        Set<OntologyVO> ontologySetPropertiesA = null;
        Set<OntologyVO> ontologySetPropertiesB = null;

        //ols
        OlsClient olsClient = new OlsClient();
        Query ontologyQuery = olsClient.getOntologyQuery();

        int counter=1;
        for ( IntactBinaryInteraction binary : results ) {

            List<InteractionType> intTypes = binary.getInteractionTypes();
            ontologySetIntType = getOntologySet( intTypes );

            List<InteractionDetectionMethod> intDetmethod = binary.getDetectionMethods();
            ontologySetDecMethod = getOntologySet( intDetmethod );

            ExtendedInteractor interactorA = binary.getInteractorA();
            List<CrossReference> propertiesA = interactorA.getProperties();
            ontologySetPropertiesA = getOntologySet( propertiesA );

            ExtendedInteractor interactorB = binary.getInteractorB();
            List<CrossReference> propertiesB = interactorB.getProperties();
            ontologySetPropertiesB = getOntologySet( propertiesB );

           counter++;
        }

        if ( log.isInfoEnabled() ) {
                log.info( "Loaded ontologies for  "+counter+ " Interactions" );
            }
        if ( ontologySetIntType != null && ontologySetIntType.size() > 0 ) {
            allOntologies.addAll( ontologySetIntType );
        }
        if ( ontologySetDecMethod != null && ontologySetDecMethod.size() > 0 ) {
            allOntologies.addAll( ontologySetDecMethod );
        }
        if ( ontologySetPropertiesA != null && ontologySetPropertiesA.size() > 0 ) {
            allOntologies.addAll( ontologySetPropertiesA );
        }
        if ( ontologySetPropertiesB != null && ontologySetPropertiesB.size() > 0 ) {
            allOntologies.addAll( ontologySetPropertiesB );
        }

        //for the given set load parents
        log.debug( "allOntologies size before adding  parents: " + allOntologies.size() );

        List<OntologyVO> parentsList = new ArrayList<OntologyVO>();
        for ( OntologyVO ontologyVO : allOntologies ) {
        List<OntologyVO> parentsVO = getAllOntologyParentsTerm(ontologyVO.getIdentifier(),ontologyQuery);
        parentsList.addAll( parentsVO);
        }

        allOntologies.addAll( parentsList );
        if ( log.isDebugEnabled() ) {
            log.debug( "allOntologies size after adding parents : " + allOntologies.size() );
            //comment this for block later
            for ( OntologyVO vo : allOntologies ) {
                log.debug( "OntologyList  ->" + vo.getName() );
            }
        }

        this.ontologyList = new ArrayList<OntologyVO>( allOntologies );

        Collections.sort( ontologyList, new Comparator<OntologyVO>() {
            public int compare( OntologyVO o1, OntologyVO o2 ) {
                String name1 = o1.getName();
                String name2 = o2.getName();

                return name1.compareTo( name2 );
            }

        } );
    }

    public class OntologyVO {


        private String identifier;
        private String name;

        public OntologyVO( String identifier, String name ) {
            this.identifier = identifier;
            this.name = name;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier( String identifier ) {
            this.identifier = identifier;
        }

        public String getName() {
            return name;
        }

        public void setName( String name ) {
            this.name = name;
        }

    } //end inner class


    private List<OntologyVO> getAllOntologyParentsTerm( String identifier,Query ontologyQuery ) {
        if ( identifier == null ) {
            throw new NullPointerException( "You must give a non null identifier" );
        }

        List<Term> allParentsWithoutRoot;
        List<OntologyVO> parentOntologyList = new ArrayList<OntologyVO>();

        String ontologyClass;
        if(identifier.toUpperCase().startsWith( "MI:" )){
         ontologyClass = OlsUtils.PSI_MI_ONTOLOGY;
        }else if(identifier.toUpperCase().startsWith( "GO:" )){
         ontologyClass = OlsUtils.GO_ONTOLOGY;
        }else{

            if ( log.isDebugEnabled() ) {
                log.debug( "Currently unsupported Ontology  "+identifier );
            }
          return parentOntologyList;
        }
        try {//get Parent terms for the given Identifier

            allParentsWithoutRoot = OlsUtils.getAllParents( identifier, ontologyClass, ontologyQuery, new ArrayList<Term>(), true );

            String ontologyId;
            String ontologyName;
            OntologyVO ontology;
            for ( Term parentTerm : allParentsWithoutRoot ) {
                ontologyId =  parentTerm.getId();
                ontologyName = parentTerm.getName() + " (" + ontologyId + ")";

                ontology = new OntologyVO( parentTerm.getId(), ontologyName );
                parentOntologyList.add( ontology );
            }

        } catch ( RemoteException rex ) {
            rex.printStackTrace();
        }

        return parentOntologyList;
    }



    private Set<OntologyVO> getOntologySet( List xRefList) {
        if ( xRefList == null ) {
            throw new NullPointerException( "You must give a non null xRefList" );
        }

        Set<OntologyVO> ontologySet = new HashSet<OntologyVO>();
        OntologyVO ontology;
        for ( Object o : xRefList ) {
            if ( o instanceof CrossReference ) {
                ontology = fetchOntologyVO( ( CrossReference ) o );
                if ( ontology != null ) {
                    //add the ontology of the current term
                    ontologySet.add( ontology );
                }
            }
        }

        return ontologySet;
    }

    private OntologyVO fetchOntologyVO( CrossReference xref ) {
        if ( xref == null ) {
            throw new NullPointerException( "You must give a non null xref" );
        }

        String ontologyName;
        String ontologyId;
        OntologyVO ontology = null;
        if ( "MI".equals( xref.getDatabase() ) || "GO".equals( xref.getDatabase() ) ) {
            ontologyId =  xref.getDatabase() + ":" + xref.getIdentifier();
            ontologyName = xref.getText() + " (" + ontologyId + ")";
            ontology = new OntologyVO( xref.getDatabase() + ":" + xref.getIdentifier(), ontologyName );
        }


        return ontology;
    }

    //For autocomplete


    protected Object getParameterValue( String parameterName, javax.faces.event.ActionEvent event ) {
        for ( Object uiObject : event.getComponent().getChildren() ) {
            if ( uiObject instanceof javax.faces.component.UIParameter ) {
                final javax.faces.component.UIParameter param = ( javax.faces.component.UIParameter ) uiObject;
                if ( param.getName().equals( parameterName ) ) {
                    return param.getValue();
                }
            }
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    public void fillAutocomplete( javax.faces.event.ActionEvent event ) {

        final FacesContext facesContext = javax.faces.context.FacesContext.getCurrentInstance();

        final java.util.Map parameters = facesContext.getExternalContext().getRequestParameterMap();
        final Object fieldValue = parameters.get( this.getParameterValue( "searchFieldRequestParamName", event ) );
        final String strFieldValue = fieldValue == null || fieldValue.toString().trim().length() == 0 ? "" : fieldValue.toString().trim().toLowerCase();
        final List<OntologyVO> result = new ArrayList<OntologyVO>();

        for ( OntologyVO vo : ontologyList ) {
            if ( vo.getName().toLowerCase().startsWith( strFieldValue ) ) {
                result.add( vo );
            }
        }

        Collections.sort( result, new Comparator<OntologyVO>() {
            public int compare( OntologyVO o1, OntologyVO o2 ) {
                String name1 = o1.getName();
                String name2 = o2.getName();

                return name1.compareTo( name2 );
            }

        } );

        
        final ValueBinding vb = facesContext.getApplication().createValueBinding( "#{autocompleteResult}" );
        vb.setValue( facesContext, result );
    }


    public List<IntactBinaryInteraction> getBinaryInteractionList() {
        return binaryInteractionList;
    }

    public void setBinaryInteractionList( List<IntactBinaryInteraction> binaryInteractionList ) {
        this.binaryInteractionList = binaryInteractionList;
    }

    public List<OntologyVO> getOntologyList() {
        return ontologyList;
    }

    public void setOntologyList( List<OntologyVO> ontologyList ) {
        this.ontologyList = ontologyList;
    }

    public List<OntologyVO> getMatchedOntologyList() {
        return matchedOntologyList;
    }

    public void setMatchedOntologyList( List<OntologyVO> matchedOntologyList ) {
        this.matchedOntologyList = matchedOntologyList;
    }


    public SearchConfig getConfig() {
        return config;
    }

    public void setConfig( SearchConfig config ) {
        this.config = config;
    }
}
