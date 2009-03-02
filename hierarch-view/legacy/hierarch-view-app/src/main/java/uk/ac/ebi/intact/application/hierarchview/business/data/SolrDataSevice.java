/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.application.hierarchview.business.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.HVNetworkBuilder;
import uk.ac.ebi.intact.application.hierarchview.exception.HierarchViewDataException;
import uk.ac.ebi.intact.application.hierarchview.exception.MultipleResultException;
import uk.ac.ebi.intact.application.hierarchview.exception.ProteinNotFoundException;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.SolrDocumentConverter;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;
import uk.ac.ebi.intact.searchengine.CriteriaBean;
import uk.ac.ebi.intact.searchengine.SearchHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.net.MalformedURLException;

/**
 * Solr based data source.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class SolrDataSevice implements DataService {

    private static final Log log = LogFactory.getLog( SolrDataSevice.class );

    private Collection<String> centralProteins;
    private UserQuery userQuery;
    private String solrServerUrl;
    Collection<BinaryInteraction> interactions;
    SolrServer solrServer;

    public SolrDataSevice() {
        centralProteins = new ArrayList<String>();
        interactions = new ArrayList<BinaryInteraction>( HVNetworkBuilder.getMaxInteractions() );

        Properties searchProperties = IntactUserI.SEARCH_PROPERTIES;

        solrServerUrl = searchProperties.getProperty( "search.source.solr.server.url" );

        if ( solrServerUrl == null ) {
            throw new IllegalArgumentException( "No Solr server was provided. Please specify one in " +
                                                StrutsConstants.SEARCH_PROPERTY_FILE );
        }

        if ( log.isDebugEnabled() ) log.debug( "Solr server URL: " + solrServerUrl );

        try {
            solrServer = new CommonsHttpSolrServer( solrServerUrl );
        } catch ( MalformedURLException e ) {
            throw new IllegalArgumentException( "The given Solr URL is malformed. Please update " +
                                                StrutsConstants.SEARCH_PROPERTY_FILE, e );
        }

    }

    public Collection<String> getCentralProteins() {

        String query = userQuery.getQuery();

        if ( query.contains( ", " ) ) {
            for ( String q : query.split( "," ) ) {
                findCentralProteins( interactions, q.trim() );
            }
        } else {
            findCentralProteins( interactions, query );
        }

        return centralProteins;
    }

    public Collection<BinaryInteraction> getBinaryInteractionsByQueryString( UserQuery userQuery ) throws HierarchViewDataException, MultipleResultException, ProteinNotFoundException {

        this.userQuery = userQuery;

        SolrQuery query = userQuery.getSolrQuery();

        try {

            if ( log.isDebugEnabled() ) {
                log.debug( "Solr query: " + query );
            }


            QueryResponse solrResponse = solrServer.query( query );

            if ( solrResponse.getResults().getNumFound() > HVNetworkBuilder.getMaxInteractions() ) {
                throw new MultipleResultException( "Query '" + query +
                                                   "' returns more interactions than the maximum allowed (" +
                                                   HVNetworkBuilder.getMaxInteractions() + ")." );
            }

            // Now build a collection of interaction based on these SolrDocument.
            final Iterator<SolrDocument> documentIterator = solrResponse.getResults().iterator();
            SolrDocumentConverter converter = new SolrDocumentConverter( new IntactDocumentDefinition() );
            interactions.clear();
            while ( documentIterator.hasNext() ) {
                SolrDocument solrDocument = documentIterator.next();
                interactions.add( ( IntactBinaryInteraction ) converter.toBinaryInteraction( solrDocument ) );
            }

        } catch ( SolrServerException e ) {
            final String msg = "An error occured while searching for '" + query +
                               "' on the Solr index (" + solrServerUrl + ")";
            log.error( msg, e );
            throw new HierarchViewDataException( msg, e );
        }

        return interactions;
    }

    public Collection<CriteriaBean> getSearchCritera() {
        return new SearchHelper().getSearchCritera();
    }

    public String getDataSourceName() throws HierarchViewDataException {
        return "solr index";
    }

    protected void findCentralProteins( Collection<? extends BinaryInteraction> binaryInteractions, String query ) {
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
}