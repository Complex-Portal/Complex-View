/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.hierarchview.business.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.application.hierarchview.business.graph.HVNetworkBuilder;
import uk.ac.ebi.intact.application.hierarchview.exception.HierarchViewDataException;
import uk.ac.ebi.intact.application.hierarchview.exception.MultipleResultException;
import uk.ac.ebi.intact.application.hierarchview.exception.ProteinNotFoundException;
import uk.ac.ebi.intact.binarysearch.wsclient.BinarySearchServiceClient;
import uk.ac.ebi.intact.searchengine.CriteriaBean;
import uk.ac.ebi.intact.searchengine.SearchHelper;
import uk.ac.ebi.intact.searchengine.SearchHelperI;
import uk.ac.ebi.intact.util.Chrono;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Uses BinarySearchServiceClient to get Information for building graph.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-SNAPSHOT
 */
public class BinaryWebService implements DataService {

    private static final Log logger = LogFactory.getLog( BinaryWebService.class );

    private final BinarySearchServiceClient client = new BinarySearchServiceClient();

    private final SearchHelperI searchHelper = new SearchHelper();

    private Collection<String> centralProteins;

    public Collection<String> getCentralProteins() {
        return centralProteins;
    }

    public Collection<BinaryInteraction> getBinaryInteractionsByQueryString( String query ) throws HierarchViewDataException, MultipleResultException, ProteinNotFoundException {
        Chrono chrono = new Chrono();
        chrono.start();
        centralProteins = new ArrayList<String>();
        Collection<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();
        SearchResult result = client.findBinaryInteractions( query );
        if ( result.getTotalCount() > HVNetworkBuilder.getMaxInteractions() ) {
            throw new MultipleResultException( "Result of " + query + " get more than " + HVNetworkBuilder.getMaxInteractions() + " interactions" );
        }

        binaryInteractions.addAll( result.getData() );
        if ( query.contains( ", " ) ) {
            for ( String q : query.split( "," ) ) {
                findCentralProteins( binaryInteractions, q.trim() );
            }
        } else {
            findCentralProteins( binaryInteractions, query );
        }

        chrono.stop();

        String msg;
        if ( binaryInteractions.isEmpty() ) {
            msg = new StringBuffer( 128 ).append( "No result(s) by search in database for query " )
                    .append( query ).append( chrono ).toString();
        } else {
            msg = new StringBuffer( 128 ).append( "Time for retreiving data from database(" )
                    .append( binaryInteractions.size() ).append( " BinaryInteractions) :" )
                    .append( chrono ).toString();
        }
        logger.info( msg );

        return binaryInteractions;
    }

    void findCentralProteins( Collection<? extends BinaryInteraction> binaryInteractions, String query ) {
        for ( BinaryInteraction bi : binaryInteractions ) {
            for ( CrossReference xref : bi.getInteractorA().getIdentifiers() ) {
                if ( xref.getIdentifier().equalsIgnoreCase( query ) ) {
                    setCentralProtein( bi, true, false );
                    break;
                }
            }

            for ( CrossReference xref : bi.getInteractorB().getIdentifiers() ) {
                if ( xref.getIdentifier().equalsIgnoreCase( query ) ) {
                    setCentralProtein( bi, false, true );
                    break;
                }
            }

            for ( CrossReference xref : bi.getInteractorA().getAlternativeIdentifiers() ) {
                if ( xref.getIdentifier().equalsIgnoreCase( query ) ) {
                    setCentralProtein( bi, true, false );
                    break;
                }
            }

            for ( CrossReference xref : bi.getInteractorB().getAlternativeIdentifiers() ) {
                if ( xref.getIdentifier().equalsIgnoreCase( query ) ) {
                    setCentralProtein( bi, false, true );
                    break;
                }
            }

            for ( Alias alias : bi.getInteractorA().getAliases() ) {
                if ( alias.getName().equalsIgnoreCase( query ) ) {
                    setCentralProtein( bi, true, false );
                    break;
                }
            }

            for ( Alias alias : bi.getInteractorB().getAliases() ) {
                if ( alias.getName().equalsIgnoreCase( query ) ) {
                    setCentralProtein( bi, false, true );
                    break;
                }
            }
        }
    }

    private void setCentralProtein( BinaryInteraction bi, boolean a, boolean b ) {
        String id = null;
        if ( a ) {
            for ( CrossReference xref : bi.getInteractorA().getIdentifiers() ) {
                if ( id == null ) {
                    id = xref.getIdentifier();
                }
                if ( xref.getDatabase().equals( "intact" ) ) {
                    id = xref.getIdentifier();
                }
            }
        }
        if ( b ) {
            for ( CrossReference xref : bi.getInteractorB().getIdentifiers() ) {
                if ( id == null ) {
                    id = xref.getIdentifier();
                }
                if ( xref.getDatabase().equals( "intact" ) ) {
                    id = xref.getIdentifier();
                }
            }
        }
        centralProteins.add( id );
    }

    public Collection<CriteriaBean> getSearchCritera() {
        return searchHelper.getSearchCritera();
    }

    public String getDbName() throws HierarchViewDataException {
        return client.getBinarySearchPort().getVersion();
    }
}
