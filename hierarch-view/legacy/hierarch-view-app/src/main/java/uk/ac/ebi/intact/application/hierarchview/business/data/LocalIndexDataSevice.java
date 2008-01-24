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
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.search.Searcher;
import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.application.hierarchview.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchview.business.graph.HVNetworkBuilder;
import uk.ac.ebi.intact.application.hierarchview.exception.HierarchViewDataException;
import uk.ac.ebi.intact.application.hierarchview.exception.MultipleResultException;
import uk.ac.ebi.intact.application.hierarchview.exception.ProteinNotFoundException;
import uk.ac.ebi.intact.application.hierarchview.struts.StrutsConstants;
import uk.ac.ebi.intact.psimitab.search.IntActSearchEngine;
import uk.ac.ebi.intact.searchengine.CriteriaBean;
import uk.ac.ebi.intact.searchengine.SearchHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class LocalIndexDataSevice implements DataService {

    private static final Log logger = LogFactory.getLog( LocalIndexDataSevice.class );

    private SearchResult<?> searchResult;
    private Collection<String> centralProteins;
    private String query;
    private String localIndexPath;

    public LocalIndexDataSevice() {
        centralProteins = new ArrayList<String>();
        Properties searchProperties = IntactUserI.SEARCH_PROPERTIES;

        localIndexPath = searchProperties.getProperty( "search.source.local.index.path" );
        logger.debug("Path to the local index: " + localIndexPath);
    }

    public Collection<String> getCentralProteins() {

        if ( query.contains( ", " ) ) {
            for ( String q : query.split( "," ) ) {
                findCentralProteins( searchResult.getInteractions(), q.trim() );
            }
        } else {
            findCentralProteins( searchResult.getInteractions(), query );
        }

        return centralProteins;
    }

    public Collection<BinaryInteraction> getBinaryInteractionsByQueryString( String query ) throws HierarchViewDataException, MultipleResultException, ProteinNotFoundException {
        this.query = query;
        if ( localIndexPath == null ) {
            throw new HierarchViewDataException( "NO local index path is found. Please specify one in " + StrutsConstants.SEARCH_PROPERTY_FILE );
        }         

        try {
            IntActSearchEngine searchEngine = new IntActSearchEngine( localIndexPath );
            this.searchResult = Searcher.search( query, searchEngine );
            
        } catch ( IOException e ) {
            throw new HierarchViewDataException( "Could not find index-files" );
        }

        if ( searchResult.getTotalCount() > HVNetworkBuilder.getMaxInteractions() ) {
            throw new MultipleResultException( "Result of " + query + " get more than " + HVNetworkBuilder.getMaxInteractions() + " interactions." );
        }

        return ( Collection<BinaryInteraction> ) searchResult.getInteractions();
    }

    public Collection<CriteriaBean> getSearchCritera() {
        return new SearchHelper().getSearchCritera();
    }

    public String getDbName() throws HierarchViewDataException {
        return "local index";
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