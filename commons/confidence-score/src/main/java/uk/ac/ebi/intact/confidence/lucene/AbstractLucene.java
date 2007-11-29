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
package uk.ac.ebi.intact.confidence.lucene;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *               20-Nov-2007
 *               </pre>
 */
public abstract class AbstractLucene {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( AbstractLucene.class );

    protected static final String DEFAULT_COL_SEPARATOR = ",";

    private Directory indexDir;
    private Analyzer analyzer;
    private IndexWriter indexWriter;

    protected AbstractLucene(Directory dir, Analyzer analyzer, boolean bool) {
        indexDir = dir;
        this.analyzer =analyzer;
        try {
            indexWriter = new IndexWriter( indexDir, analyzer, bool);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

      protected Document createDocumentFromAttribsLine( String confSetAttribsLine, String type ) {
        String[] tokens = confSetAttribsLine.split( DEFAULT_COL_SEPARATOR );
        Document doc = new Document();
        // raw fields
        String interaction = tokens[0];
         String attributes ="";
        for ( int i = 1; i < tokens.length; i++ ) {
            if ( !attributes.equalsIgnoreCase( "" ) ) {
                attributes += "," + tokens[i];
            } else {
                attributes += tokens[i];
            }
        }
        doc.add( new Field( "interaction", interaction, Field.Store.YES, Field.Index.TOKENIZED ) );
        doc.add( new Field( "attribute", attributes, Field.Store.YES, Field.Index.TOKENIZED ) );
        doc.add( new Field( "type", type, Field.Store.YES, Field.Index.TOKENIZED ) );

        return doc;
    }

    public void indexFile( File inFile, String type ) {
          try {
            FileReader fr = new FileReader( inFile );
            BufferedReader br = new BufferedReader( fr );
            String line = "";
            while ( ( line = br.readLine() ) != null ) {
                Document doc = createDocumentFromAttribsLine( line, type );
                indexWriter.addDocument( doc );
            }
            br.close();
            fr.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public void indexFiles(String [] paths){

        try {
            IndexWriter indexWriter = new IndexWriter( indexDir, analyzer, true );
            // TODO: ask what this line does indexWriter.setMergeFactor(MERGE_FACTOR);
            indexWriter.setMaxMergeDocs( Integer.MAX_VALUE );
            for ( String path : paths ) {
                File inFile = new File( path );
                String[] aux = path.split( "_" );
                indexFile( inFile, aux[1] );
            }
            indexWriter.optimize();
            indexWriter.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param q
     * @param nr : the expected nr, else -1
     * @return
     * @throws Exception
     */
    public String search( String q, int nr ) throws Exception {
        IndexSearcher is = new IndexSearcher( indexDir );
        QueryParser parser = new QueryParser( "interaction", analyzer );
        Query query = parser.parse( q );
        Hits hits = is.search( query );
        if ( nr > -1 && hits.length() != nr && log.isInfoEnabled() ) {
            log.info( "Found " + hits.length() + " document(s) that match query '" + q + "', but expected " + nr );
        }
        String result = q;
        for ( int i = 0; i < hits.length(); i++ ) {
            Document doc = hits.doc( i );
            String attrib = doc.get( "attribute" );
            if ( attrib != null && !attrib.equalsIgnoreCase( "" ) ) {
                result += "," + attrib;
            }
//            System.out.println( "doc.get interaction: " + doc.get( "interaction" ) );
//            System.out.println( "doc.get attribute: " + doc.get("attribute") );
//            System.out.println( "doc.get type: " + doc.get("type") );
        }
        is.close();
        return result;
    }

}
