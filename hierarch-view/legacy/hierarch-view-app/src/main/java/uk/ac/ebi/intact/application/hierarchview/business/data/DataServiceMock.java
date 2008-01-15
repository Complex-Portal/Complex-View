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

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.intact.application.hierarchview.exception.HierarchViewDataException;
import uk.ac.ebi.intact.application.hierarchview.exception.MultipleResultException;
import uk.ac.ebi.intact.application.hierarchview.exception.ProteinNotFoundException;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntActColumnHandler;
import uk.ac.ebi.intact.searchengine.CriteriaBean;
import uk.ac.ebi.intact.searchengine.SearchHelper;
import uk.ac.ebi.intact.searchengine.SearchHelperI;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Uses a static psimitabfile to get Information for building graph.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 1.6.0-SNAPSHOT
 */
public class DataServiceMock implements DataService {

    private String query;

    private final SearchHelperI searchHelper = new SearchHelper();

    public Collection<String> getCentralProteins() {
        List<String> centralProteins = new ArrayList<String>();
        if ( query.equals( "brca2" ) ) {
            centralProteins.add( "EBI-1034100" );
            centralProteins.add( "EBI-79792" );
        }
        if ( query.equals( "rad51" ) ) {
            centralProteins.add( "EBI-297202" );
        }
        if ( query.equals( "EBI-359343, EBI-297202, EBI-1034100, EBI-79792, EBI-539895, EBI-297202" ) ) {
            centralProteins.add( "EBI-359343" );
            centralProteins.add( "EBI-297202" );
            centralProteins.add( "EBI-1034100" );
            centralProteins.add( "EBI-79792" );
            centralProteins.add( "EBI-539895" );
            centralProteins.add( "EBI-297202" );
        }
        return centralProteins;
    }

    public Collection<BinaryInteraction> getBinaryInteractionsByQueryString( String query ) throws HierarchViewDataException, MultipleResultException, ProteinNotFoundException {
        this.query = query;

        try {
            File file = null;

            if ( query.equals( "brca2" ) ) {
                file = getFileByResources( "/test-files/brca2.txt", DataServiceMock.class );
            }
            if ( query.equals( "rad51" ) ) {
                file = getFileByResources( "/test-files/rad51.txt", DataServiceMock.class );
            }
            if ( query.equals( "EBI-359343, EBI-297202, EBI-79792, EBI-539895, EBI-297202" ) ) {
                file = getFileByResources( "/test-files/brca2_expanded.txt", DataServiceMock.class );
            }
            PsimiTabReader reader = new PsimiTabReader( true );
            reader.setBinaryInteractionClass( IntActBinaryInteraction.class );
            reader.setColumnHandler( new IntActColumnHandler() );

            return reader.read( file );

        } catch ( UnsupportedEncodingException e ) {
            throw new HierarchViewDataException( "Problems during encoding" );
        } catch ( ConverterException e ) {
            throw new HierarchViewDataException( "Problems during converting" );
        } catch ( IOException e ) {
            throw new ProteinNotFoundException( "File not found" );
        }
    }

    public Collection<CriteriaBean> getSearchCritera() {
        return searchHelper.getSearchCritera();
    }

    public String getDbName() throws HierarchViewDataException {
        return "mock";
    }

    File getFileByResources( String fileName, Class clazz ) throws UnsupportedEncodingException {
        String strFile = clazz.getResource( fileName ).getFile();
        return new File( URLDecoder.decode( strFile, "utf-8" ) );
    }
}