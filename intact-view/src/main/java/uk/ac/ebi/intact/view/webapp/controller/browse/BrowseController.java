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
package uk.ac.ebi.intact.view.webapp.controller.browse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.trinidad.event.DisclosureEvent;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.IntactViewException;

import javax.faces.event.ActionEvent;
import javax.faces.context.FacesContext;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.IOException;

/**
 * Controller for the browse tab
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
@Controller( "browseBean" )
@Scope( "conversation.access" )
@ConversationName( "general" )
public class BrowseController extends JpaBaseController {

    private static final Log log = LogFactory.getLog( BrowseController.class );

    @Autowired
    private SearchController searchController;

    //browsing
    private String interproIdentifierList;
    private String chromosomalLocationIdentifierList;
    private String mRNAExpressionIdentifierList;
    private String reactomeIdentifierList;

    //URL Links
    public static final String INTERPROURL = "http://www.ebi.ac.uk/interpro/ISpy?ac=";
    public static final String CHROMOSOMEURL = "http://www.ensembl.org/Homo_sapiens/featureview?type=ProteinAlignFeature;id=";
    public static final String EXPRESSIONURL = "http://www.ebi.ac.uk/microarray-as/atlas/qr?q_gene=";
    //public static final String REACTOMEURL = "";




    public BrowseController() {

    }

    /**
     * Returns uniprot identifiers
     *
     * @return Set of unique identifiers
     */
    private Set<String> prepareUniqueListofIdentifiers() {

        Set<String> uniqueIdentifiers = new HashSet<String>();
        List<IntactBinaryInteraction> results;

        if ( searchController.getInteractorResults() != null ) {
            results = searchController.getInteractorResults().getResult().getData();


            for ( IntactBinaryInteraction result : results ) {
                final Collection<CrossReference> crossReferences = result.getInteractorA().getIdentifiers();

                for ( CrossReference xRef : crossReferences ) {

                    if ( CvDatabase.UNIPROT.equals( xRef.getDatabase() ) ) {
                        uniqueIdentifiers.add( xRef.getIdentifier() );
                    }
                }
            }
        }
        return uniqueIdentifiers;
    }


    /**
     * list of genenames used by mRNA expression
     *
     * @return Set of unique gene names
     */
    private Set<String> prepareUniqueListOfGeneNames() {

        Set<String> uniqueGeneNames = new HashSet<String>();
        List<IntactBinaryInteraction> results;

        if ( searchController.getInteractorResults() != null ) {
            results = searchController.getInteractorResults().getResult().getData();

            for ( IntactBinaryInteraction result : results ) {
                final Collection<Alias> aliases = result.getInteractorA().getAliases();

                for ( Alias alias : aliases ) {
                    uniqueGeneNames.add( alias.getName() );
                }
            }
        }
        return uniqueGeneNames;
    }

    /**
     * A DiscloserListener that generates the urls for all the links in the browse page
     *
     * @param evt DisclosureEvent
     */
    public void createListofIdentifiers( DisclosureEvent evt ) {

        this.interproIdentifierList = appendIdentifiers( prepareUniqueListofIdentifiers(), "," );
        this.chromosomalLocationIdentifierList = appendIdentifiers( prepareUniqueListofIdentifiers(), ";id=" );
        this.mRNAExpressionIdentifierList = appendIdentifiers( prepareUniqueListOfGeneNames(), ",+" );
        this.reactomeIdentifierList = appendIdentifiers( prepareUniqueListofIdentifiers(), "\n" );

        if ( log.isTraceEnabled() ) {
            log.trace( "interproIdentifierList " + interproIdentifierList );
            log.trace( "chromosomalLocationIdentifierList " + chromosomalLocationIdentifierList );
            log.trace( "mRNAExpressionIdentifierList " + mRNAExpressionIdentifierList );
            log.trace( "reactomeIdentifierList " + reactomeIdentifierList );
        }
    }


    private String appendIdentifiers( Set<String> uniqueIdentifiers, String seperator ) {
        if ( uniqueIdentifiers != null && seperator != null ) {
            return StringUtils.join( uniqueIdentifiers, seperator );
        }

        return "";
    }

    private Set<String> getSelectedListOfUniqueGeneNames() {
        final List<IntactBinaryInteraction> selected = getSelected( searchController.PROTEINS_TABLE_ID );

        Set<String> uniqueGeneNames = new HashSet<String>();

        for ( IntactBinaryInteraction result : selected ) {
            final Collection<Alias> aliases = result.getInteractorA().getAliases();

            for ( Alias alias : aliases ) {
                uniqueGeneNames.add( alias.getName() );
            }
        }
        return uniqueGeneNames;
    }


    private Set<String> getSelectedListOfUniqueUniprotIdentifiers() {
        final List<IntactBinaryInteraction> selected = getSelected( searchController.PROTEINS_TABLE_ID );

        Set<String> uniqueIdentifiers = new HashSet<String>();

        for ( IntactBinaryInteraction intactBinaryInteraction : selected ) {
            for ( CrossReference xref : intactBinaryInteraction.getInteractorA().getIdentifiers() ) {
                if ( CvDatabase.UNIPROT.equals( xref.getDatabase() ) ) {
                    uniqueIdentifiers.add( xref.getIdentifier() );
                }
            }
        }
        return uniqueIdentifiers;
    }

    public void linkToInterproFromListSelection( ActionEvent evt ) {

        this.interproIdentifierList = appendIdentifiers( getSelectedListOfUniqueUniprotIdentifiers(), "," );
        String redirectURL = INTERPROURL + interproIdentifierList;
        try {
            redirectTo( redirectURL );
        } catch ( Exception e ) {
            throw new IntactViewException( "Exception in redirecting to " + redirectURL );
        }

    }

    public void linkToExpressionFromListSelection( ActionEvent evt ) {

        this.mRNAExpressionIdentifierList = appendIdentifiers( getSelectedListOfUniqueGeneNames(), ",+" );
        String redirectURL = EXPRESSIONURL + mRNAExpressionIdentifierList;
        try {
            redirectTo( redirectURL );
        } catch ( Exception e ) {
            throw new IntactViewException( "Exception in redirecting to " + redirectURL );
        }

    }

    public void linkToChromosomeFromListSelection( ActionEvent evt ) {

        this.chromosomalLocationIdentifierList = appendIdentifiers( getSelectedListOfUniqueUniprotIdentifiers(), ";id=" );
        String redirectURL = CHROMOSOMEURL + chromosomalLocationIdentifierList;
        try {
            redirectTo( redirectURL );
        } catch ( Exception e ) {
            throw new IntactViewException( "Exception in redirecting to " + redirectURL );
        }

    }

    public void linkToPathwayFromListSelection( ActionEvent evt ) {

        this.reactomeIdentifierList = appendIdentifiers( getSelectedListOfUniqueUniprotIdentifiers(), "\n" );
        //todo
    }


    private void redirectTo( String redirectURL ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug( "Redirecting to " + redirectURL );
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response =
                ( HttpServletResponse ) facesContext.getExternalContext().getResponse();
        response.sendRedirect( redirectURL );
        facesContext.responseComplete();
    }


    public String getInterproIdentifierList() {
        return interproIdentifierList;
    }

    public void setInterproIdentifierList( String interproIdentifierList ) {
        this.interproIdentifierList = interproIdentifierList;
    }

    public String getChromosomalLocationIdentifierList() {
        return chromosomalLocationIdentifierList;
    }

    public void setChromosomalLocationIdentifierList( String chromosomalLocationIdentifierList ) {
        this.chromosomalLocationIdentifierList = chromosomalLocationIdentifierList;
    }

    public String getMRNAExpressionIdentifierList() {
        return mRNAExpressionIdentifierList;
    }

    public void setMRNAExpressionIdentifierList( String mRNAExpressionIdentifierList ) {
        this.mRNAExpressionIdentifierList = mRNAExpressionIdentifierList;
    }

    public String getReactomeIdentifierList() {
        return reactomeIdentifierList;
    }

    public void setReactomeIdentifierList( String reactomeIdentifierList ) {
        this.reactomeIdentifierList = reactomeIdentifierList;
    }
}
