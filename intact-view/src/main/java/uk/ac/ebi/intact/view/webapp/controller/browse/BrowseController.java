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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.util.ExternalDbLinker;

import javax.faces.event.ActionEvent;
import java.util.Set;

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

    @Autowired
    private ExternalDbLinker dbLinker;

    //browsing
    private String interproIdentifierList;
    private String chromosomalLocationIdentifierList;
    private String mRNAExpressionIdentifierList;
    private String[] reactomeIdentifierList;

   
    public BrowseController() {

    }

     /**
     * A DiscloserListener that generates the urls for all the links in the browse page
     *
     * @param evt DisclosureEvent
     */
    public void createListofIdentifiers( DisclosureEvent evt ) {

         // TODO fix this
         if (true) throw new UnsupportedOperationException("Fix this");

//        List<IntactBinaryInteraction> interactions;
//        if ( searchController.getProteinResultDataModel() != null ) {
//            interactions = searchController.getProteinResultDataModel().getResult().getData();
//
//            this.interproIdentifierList = appendIdentifiers( dbLinker.getUniqueUniprotIds( interactions ), dbLinker.INTERPRO_SEPERATOR );
//            this.chromosomalLocationIdentifierList = appendIdentifiers( dbLinker.getUniqueUniprotIds( interactions ), dbLinker.CHROMOSOME_SEPERATOR );
//            this.mRNAExpressionIdentifierList = appendIdentifiers( dbLinker.getUniqueGeneNames( interactions ), dbLinker.EXPRESSION_SEPERATOR );
//            this.reactomeIdentifierList =  dbLinker.getUniqueUniprotIds( interactions ).toArray( new String[]{} );
//        }

        if ( log.isTraceEnabled() ) {
            log.trace( "interproIdentifierList " + interproIdentifierList );
            log.trace( "chromosomalLocationIdentifierList " + chromosomalLocationIdentifierList );
            log.trace( "mRNAExpressionIdentifierList " + mRNAExpressionIdentifierList );
            log.trace( "reactomeIdentifierList " + reactomeIdentifierList );
        }
    }


    private String appendIdentifiers( Set<String> uniqueIdentifiers, String separator ) {
        if ( uniqueIdentifiers != null && separator != null ) {
            return StringUtils.join( uniqueIdentifiers, separator );
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

    public String[] getReactomeIdentifierList() {
        return reactomeIdentifierList;
    }

    public void setReactomeIdentifierList( String[] reactomeIdentifierList ) {
        this.reactomeIdentifierList = reactomeIdentifierList;
    }

    public void goReactome( ActionEvent evt ) {
        String[] selected = reactomeIdentifierList;
        //the carriage return has to be escaped as it is used in the JavaScript
        dbLinker.reactomeLinker( dbLinker.REACTOMEURL, "\\r", selected, "/view/pages/browse/browse.xhtml" );

    }    
}
