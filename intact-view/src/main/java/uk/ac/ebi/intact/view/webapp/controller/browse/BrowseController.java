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

import org.springframework.stereotype.Controller;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.trinidad.event.DisclosureEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.model.CvDatabase;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Collection;

import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Alias;

/**
 * Controller for the browse tab
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "general" )
public class BrowseController {

    private static final Log log = LogFactory.getLog( BrowseController.class );

    @Autowired
    private SearchController searchController;

    //browsing
    private String interproIdentifierList;
    private String chromosomalLocationIdentifierList;
    private String mRNAExpressionIdentifierList;
    private String reactomeIdentifierList;


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
                final Collection<Alias> aliases = result.getInteractorB().getAliases();

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
