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
package uk.ac.ebi.intact.confidence.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import static org.junit.Assert.assertTrue;
import uk.ac.ebi.intact.confidence.lucene.AbstractLucene;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Merging of the different attributes files.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre> 14-Nov-2007 </pre>
 */
public class Merge extends AbstractLucene {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( Merge.class );

    private static final java.lang.String DEFAULT_COL_SEPARATOR = ",";

    private Directory indexDir;
    private Analyzer analyzer;
    private IndexWriter indexWriter;

    public Merge() {
           // indexDir = FSDirectory.getDirectory( new File( GlobalTestData.getInstance().getTargetDirectory(), "indexDir" ) );
            //ava.io.IOException: Cannot delete H:\projects\intact-current\service\commons\confidence-score\target\indexDir\segments_p5
            super(new RAMDirectory( ), new StandardAnalyzer( ), true);

            indexDir = new RAMDirectory();
            analyzer = new StandardAnalyzer();
        try {
            IndexWriter indexWriter = new IndexWriter( indexDir, analyzer, true );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

//    private Document createDocumentFromAttribsLine( String confSetAttribsLine, String type ) {
//        String[] tokens = confSetAttribsLine.split( DEFAULT_COL_SEPARATOR );
//        Document doc = new Document();
//        // raw fields
//        String interaction = tokens[0];
//        String attributes = "";
//        for ( int i = 1; i < tokens.length; i++ ) {
//            if ( !attributes.equalsIgnoreCase( "" ) ) {
//                attributes += "," + tokens[i];
//            } else {
//                attributes += tokens[i];
//            }
//        }
//        doc.add( new Field( "interaction", interaction, Field.Store.YES, Field.Index.TOKENIZED ) );
//        doc.add( new Field( "attribute", attributes, Field.Store.YES, Field.Index.TOKENIZED ) );
//        doc.add( new Field( "type", type, Field.Store.YES, Field.Index.TOKENIZED ) );
//
//        return doc;
//    }


//    private void createDocumentFromAttribsFile( IndexWriter indexWriter, File inFile, String type ) throws IOException {
//        try {
//            FileReader fr = new FileReader( inFile );
//            BufferedReader br = new BufferedReader( fr );
//            String line = "";
//            while ( ( line = br.readLine() ) != null ) {
//                Document doc = createDocumentFromAttribsLine( line, type );
//                indexWriter.addDocument( doc );
//            }
//            br.close();
//            fr.close();
//        } catch ( IOException e ) {
//            e.printStackTrace();
//        }
//    }

//    private void indexFiles( String[] paths ) {
//        try {
//            IndexWriter indexWriter = new IndexWriter( indexDir, analyzer, true );
//            // TODO: ask what this line does indexWriter.setMergeFactor(MERGE_FACTOR);
//            indexWriter.setMaxMergeDocs( Integer.MAX_VALUE );
//            for ( String path : paths ) {
//                File inFile = new File( path );
//                String[] aux = path.split( "_" );
//                indexFile( indexWriter, inFile, aux[1] );
//            }
//            indexWriter.optimize();
//            indexWriter.close();
//        } catch ( IOException e ) {
//            e.printStackTrace();
//        }
//    }

    private void lookupInfo( String[] paths, String outPath ) throws Exception {
        Set<String> processed = new HashSet<String>();
        Writer w = new FileWriter(outPath);
        for ( String path : paths ) {
            BufferedReader br = new BufferedReader( new FileReader( path ) );
            String line = "";
            while ( ( line = br.readLine() ) != null ) {
                String[] aux = line.split( "," );
                String q = aux[0];
                if ( !processed.contains( q ) ) {
                    String result = search( q, paths.length );
                    w.append( result + "\n" );
                    processed.add( q);
                }
            }
            br.close();
        }
        w.close();
    }

//    private String search( Directory indexDir, String q, int nr ) throws Exception {
//        IndexSearcher is = new IndexSearcher( indexDir );
//        QueryParser parser = new QueryParser( "interaction", analyzer );
//        Query query = parser.parse( q );
//        Hits hits = is.search( query );
//        if ( hits.length() != nr && log.isInfoEnabled() ) {
//            log.info( "Found " + hits.length() + " document(s) that match query '" + q + "', but expected " + nr );
//        }
//        String result = q;
//        for ( int i = 0; i < hits.length(); i++ ) {
//            Document doc = hits.doc( i );
//            String attrib = doc.get( "attribute" );
//            if ( attrib != null && !attrib.equalsIgnoreCase( "" ) ) {
//                result += "," + attrib;
//            }
////            System.out.println( "doc.get interaction: " + doc.get( "interaction" ) );
////            System.out.println( "doc.get attribute: " + doc.get("attribute") );
////            System.out.println( "doc.get type: " + doc.get("type") );
//        }
//        is.close();
//        return result;
//    }

    public void merge( String[] paths, String outPath ) throws Exception {
        indexFiles( paths );
//        System.out.println("for Q9VCX6;Q9VYE9:" + search( indexDir, "Q9VCX6;Q9VYE9", paths.length ));
//        System.out.println("for Q9VLM9;Q9VPU6:" + search(indexDir, "Q9VLM9;Q9VPU6", paths.length));
        lookupInfo( paths, outPath );
        indexDir.close();
    }

    public static void main( String[] args ) throws Exception {
        System.out.println( "memory: " + ( Runtime.getRuntime().maxMemory() ) / ( 1024 * 1024 ) );
        String goPath = "E:\\tmp\\lucene\\set_go_attributes.txt";      //E:\tmp\lucene
        String ipPath = "E:\\tmp\\lucene\\set_ip_attributes.txt";
        String seqPath = "E:\\tmp\\lucene\\set_align_attributes.txt";
        String[] paths = {seqPath, ipPath, goPath};
        String outPath = "E:\\tmp\\lucene\\outPath.txt";
        ( new Merge() ).merge( paths, outPath );
        assertTrue( new File( outPath ).exists() );
    }


}
