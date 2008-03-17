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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.confidence;

import uk.ac.ebi.intact.confidence.model.BinaryInteractionAttributes;
import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionAttributesReader;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionAttributesReaderImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Class to get different statistics.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class Statistics {

    public static void scoreDistribution( File scoreFile) throws Exception{
        BufferedReader br = new BufferedReader (new FileReader(scoreFile));
        String line ="";
        Map<String, Double> scores = new HashMap<String,Double>();
        Double min = 0.5;
        Double max = 0.5;
        int equal05 = 0;
        List<Integer> groups = new ArrayList<Integer>(10);
        for (int i = 0; i< 10; i++){
            groups.add( 0 );
        }
        while ((line = br.readLine()) != null){
            String [] aux = line.split( ":" );
            if (aux.length != 2){
                System.out.println("Line not proper formated: " + line);
            }
            Double score = Double.valueOf( aux[1] );
            scores.put( aux[0], score);
            if (min > score){
                min = score;
            }
            if (max< score){
                max = score;
            }
        }
        for ( Iterator<String> iterator = scores.keySet().iterator(); iterator.hasNext(); ) {
            String str =  iterator.next();
            Double score = scores.get( str );
            if (score == 0.5){
                equal05 ++;
            }
            if (score >= 0.0 && score <0.1){
                groups.add( 0, groups.remove( 0 ) + 1);
            } else if (score >= 0.1 && score < 0.2){
                groups.add(1, groups.remove( 1 ) + 1);
            } else if (score >= 0.2 && score < 0.3){
                groups.add(2, groups.remove( 2 ) + 1);
            }else if (score >= 0.3 && score < 0.4){
                groups.add(3, groups.remove( 3 ) + 1);
            }else if (score >= 0.4 && score < 0.5){
                groups.add(4, groups.remove( 4 ) + 1);
            }else if (score >= 0.5 && score < 0.6){
                groups.add(5, groups.remove( 5 ) + 1);
            }else if (score >= 0.6 && score < 0.7){
                groups.add(6, groups.remove( 6 ) + 1);
            }else if (score >= 0.7 && score < 0.8){
                groups.add(7, groups.remove( 7 ) + 1);
            }else if (score >= 0.8 && score < 0.9){
                groups.add(8, groups.remove( 8 ) + 1);
            }else if (score >= 0.9 && score < 1){
                groups.add(9, groups.remove( 9 ) + 1);
            } else {
                System.out.println(score);
            }
        }
        br.close();

        System.out.println("size map: " + scores.size());
        System.out.println("max: " + max);
        System.out.println("min: " + min);
        System.out.println("equal05:" + equal05);
       for (int i =0; i< groups.size(); i++){
           System.out.println("group["+i+"]: " + groups.get( i ));
       }
    }

    public static void attributesCoverage(File hcFile)throws Exception{
        BinaryInteractionAttributesReader biar = new BinaryInteractionAttributesReaderImpl();
        Map<String, Integer> coverage = new HashMap<String, Integer>();
        coverage.put( "GO-total",0 );
        coverage.put( "IP-total",0 );
        coverage.put( "Seq-total",0 );
        coverage.put( "GO-IP-total",0 );
        coverage.put( "IP-Seq-total",0 );
        coverage.put( "GO-Seq-total",0 );
        coverage.put( "GO-IP-Seq-total",0 );



        int total =0;
        for ( Iterator<BinaryInteractionAttributes> iter = biar.iterate( hcFile ); iter.hasNext(); ) {
            BinaryInteractionAttributes bia = iter.next();
            int gos = bia.getGoAttributes().size();
            int ips = bia.getIpAttributes().size();
            int seqs = bia.getSeqAttributes().size();

            if (gos != 0){
                coverage.put( "GO-total", coverage.get( "GO-total" ) +1 );
                if (ips != 0){
                    coverage.put("GO-IP-total",coverage.get( "GO-IP-total") +1);
                    if (seqs != 0){
                        coverage.put( "GO-IP-Seq-total", coverage.get( "GO-IP-Seq-total" ) +1 );
                    }
                }
                if (seqs != 0){
                    coverage.put( "GO-Seq-total", coverage.get( "GO-Seq-total" ) +1 );
                }
            }

            if (ips != 0){
                coverage.put( "IP-total", coverage.get( "IP-total" ) +1 );
                if (seqs != 0){
                    coverage.put( "IP-Seq-total", coverage.get( "IP-Seq-total" ) +1 );
                }
            }


            if (seqs != 0){
                coverage.put( "Seq-total", coverage.get( "Seq-total" ) +1 );
            }

            total++;
        }

        System.out.println("total interactions: " +total);

        for ( Iterator<String> iterator = coverage.keySet().iterator(); iterator.hasNext(); ) {
             String str =  iterator.next();
            System.out.println(str +": " + coverage.get( str ));

        }


    }
}
