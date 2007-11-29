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
package uk.ac.ebi.intact.confidence.analyze;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * splits the files for the k-Fold cross validation
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *                                                                       12-Nov-2007
 *                                                                       </pre>
 */
public class XvFold {

    public static int k = 5;

     /**
     * 80% training set, 20% test set
     *
     * @param inFile : the interaction set
     * @param workDir: where the splited files will be saved
      * @param title: title of the files = 'title'i.txt
     */
    public static void fold (File inFile, File workDir, String title){
        Set<String> intSet = getInteractionsAttribs( inFile );
        Set<Set<String>> folds = getFolds( k, intSet );
        printFolds( folds, workDir, title );
    }
    
    /**
     * 80% training set, 20% test set
     *
     * @param hcFile
     * @param lcFile
     */
    public static void fold( File hcFile, File lcFile, File workDir) {
        fold(hcFile, workDir, "highconf");
        fold(lcFile, workDir, "lowconf");
    }

    private static void printFolds( Set<Set<String>> folds, File workDir, String title ) {
        int i = 0;
        for ( Iterator<Set<String>> setIterator = folds.iterator(); setIterator.hasNext(); ) {
            Set<String> fold = setIterator.next();
            printFold( fold, workDir, title + i );
            i++;
        }

    }

    private static void printFold( Set<String> fold, File workDir, String title ) {
        try {
            File out = new File( workDir, title + ".txt" );
            FileWriter w = new FileWriter( out );

            for ( Iterator<String> iterator = fold.iterator(); iterator.hasNext(); ) {
                String s = iterator.next();
                w.append( s + "\n" );
            }

            w.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * it splits into k equal folds, if total != m * k, nut total == (m-1) *k , each set will contain m-1 elements
     */
    private static Set<Set<String>> getFolds( int k, Set<String> set ) {
        int total = set.size();
        int foldSize = ( int ) Math.floor( total / k );

        Set<Set<String>> folds = new HashSet<Set<String>>(foldSize);

        for ( int i = 0; i < k; i++ ) {
            Set<String> fold = new HashSet<String>();
//            if ( i == k - 1 ) {
//                int j = foldSize * i;
//                while ( j < total ) {
//                    fold.add( ( String ) set.toArray()[j] );
//                    j++;
//                }
//            } else {
            for ( int j = foldSize * i; j < foldSize * ( i + 1 ); j++ ) {
                fold.add( ( String ) set.toArray()[j] );
            }
//            }
            folds.add( fold );
        }

        return folds;
    }

    private static Set<String> getInteractionsAttribs( File inFile ) {
        try {
            FileReader fr = new FileReader( inFile );
            BufferedReader br = new BufferedReader( fr );
            Set<String> set = new HashSet<String>();
            String line = "";
            while ( ( line = br.readLine() ) != null ) {
                set.add( line );
            }
            br.close();
            fr.close();
            return set;
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return null;
    }
}
