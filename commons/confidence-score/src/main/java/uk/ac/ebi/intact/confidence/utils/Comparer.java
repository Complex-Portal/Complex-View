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

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *                                    15-Nov-2007
 *                                    </pre>
 */
public class Comparer {

    public Comparer() {

    }

    /**
     * method for statistics for the medconf score files
     * "<uniprotAc>;<uniprotAc>: <score>"
     *
     * @param inFile
     */
    public void stats( File inFile ) {
        Map<String, Double> info = readFile( inFile );
        Double max = getMax( info );
        System.out.println( "max: " + max );
        Double min = getMin( info );
        System.out.println( "min: " + min );
    }

    private Double getMin( Map<String, Double> info ) {
        Set<String> keys = info.keySet();
        Double min = new Double( 2 );
        String minKey = "";
        for ( Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            Double d = info.get( key );
            if ( min > d ) {
                min = d;
                minKey = key;
            }
        }
        System.out.println( "minKey:minVal: " + minKey + ": " + min );
        return min;
    }


    private Double getMax( Map<String, Double> info ) {
        Set<String> keys = info.keySet();
        Double max = new Double( 0 );
        String maxKey = "";
        for ( Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            Double d = info.get( key );
            if ( max < d ) {
                max = d;
                maxKey = key;
            }
        }
        System.out.println( "maxKey:maxVal: " + maxKey + ": " + max );

        return max;
    }

    private Map<String, Double> readFile( File inFile ) {
        Map<String, Double> info = new HashMap<String, Double>();
        try {
            BufferedReader br = new BufferedReader( new FileReader( inFile ) );
            String line = "";
            while ( ( line = br.readLine() ) != null ) {
                String[] aux = line.split( ": " );
                if ( aux.length != 2 ) {
                    System.out.println( "line not in expected format: " + line );
                }
                info.put( aux[0], new Double( aux[1] ) );

            }
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return info;
    }

    /**
     * compares the scores in setB against setA. If there are new interactions in setB => added in the return map.
     * If the interactions are present in both sets but with different scores => add in return map <interaction>: scoreA-scoreB
     *
     * @param scoreSetA
     * @param scoreSetB
     * @return
     */
    public Map<String, Double> compareScores( File scoreSetA, File scoreSetB ) {
        Map<String, Double> diffs = new HashMap<String, Double>();
        try {
            Map<String, Double> mapSetA = readFile( scoreSetA );
            BufferedReader br = new BufferedReader( new FileReader( scoreSetB ) );
            String line = "";
            while ( ( line = br.readLine() ) != null ) {
                String[] aux = line.split( ": " );
                if ( mapSetA.containsKey( aux[0] ) ) {
                    double setA = mapSetA.get( aux[0] );
                    double setB = new Double( aux[1] );
                    if ( setA != setB ) {
                        diffs.put( aux[0], setA - setB );
                    }
                } else {
                    diffs.put( aux[0], new Double( aux[1] ) );
                }
            }
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return diffs;
    }

    public void print( Map<String, Double> scores, File outFile ) {
        try {
            Writer w = new FileWriter( outFile );
            for ( Iterator<String> iterStr = scores.keySet().iterator(); iterStr.hasNext(); ) {
                String key = iterStr.next();
                w.append(key + ": " + scores.get( key) + "\n");

            }
            w.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }


    }

    public static void main( String[] args ) {
        Comparer c = new Comparer();
        File wekaScore = new File( "E:\\iarmean\\ConfidenceScore\\components\\mc_all_FINAL_SCORE.txt" );
        File tadmScore = new File( "E:\\iarmean\\ConfidenceScore\\components\\medconf_all_FINAL_score.txt" );
        Map<String, Double> diffs = c.compareScores( wekaScore, tadmScore );
        File outFile =  new File("E:\\iarmean\\ConfidenceScore\\components\\diff_mc_medconf_all_score.txt");
       c.print( diffs, outFile);
//        File inFile = new File("H:\\tmp\\ConfidenceModel\\components\\medconf_go_FINAL_score.txt");
//        c.stats( inFile);
//        inFile = new File("H:\\tmp\\ConfidenceModel\\components\\medconf_ip_FINAL_score.txt");
//        c.stats( inFile);
//        inFile = new File("H:\\tmp\\ConfidenceModel\\components\\medconf_align_FINAL_score.txt");
//        c.stats( inFile);
//        inFile = new File("H:\\tmp\\ConfidenceModel\\components\\medconf_all_FINAL_score.txt");
//        c.stats( inFile);
    }
}
