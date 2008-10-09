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
package uk.ac.ebi.intact.view.webapp.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Ignore;
import org.junit.Test;
import org.obo.datamodel.OBOSession;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.cvutils.CvUpdater;
import uk.ac.ebi.intact.dataexchange.cvutils.OboUtils;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;
import uk.ac.ebi.intact.psimitab.search.IntactPsimiTabIndexWriter;

import java.io.InputStream;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologiesIndexWriterTest {

    @Test
    @Ignore
    public void case1() throws Exception {
        InputStream file = OntologiesIndexWriterTest.class.getResourceAsStream( "/psimitab/DGI-5424.tsv" );
        Directory binaryInteractionIndex = new RAMDirectory( "/tmp" );

        IntactPsimiTabIndexWriter indexWriter = new IntactPsimiTabIndexWriter();
        indexWriter.index( binaryInteractionIndex, file, true, true );

        IntactContext.initStandaloneContextInMemory();

        OBOSession oboSession = OboUtils.createOBOSessionFromLatestMi();

        CvObjectOntologyBuilder cvBuilder = new CvObjectOntologyBuilder( oboSession );

        CvUpdater cvUpdater = new CvUpdater();
        cvUpdater.createOrUpdateCVs( cvBuilder.getAllCvs() );

        Directory newDirectory = new RAMDirectory();

        OntologiesIndexWriter writer = new OntologiesIndexWriter();
        writer.createIndex( binaryInteractionIndex, newDirectory );

        IndexSearcher searcher = new IndexSearcher( newDirectory );
        IndexReader reader = searcher.getIndexReader();

        for ( int i = 0; i < reader.maxDoc(); i++ ) {
            Document doc = reader.document( i );

            System.out.println( doc.getField( "identifier" ).stringValue() + " " +
                                doc.getField( "label" ).stringValue() + " - " +
                                doc.getField( "databaseLabel" ).stringValue() );
        }
    }
}
