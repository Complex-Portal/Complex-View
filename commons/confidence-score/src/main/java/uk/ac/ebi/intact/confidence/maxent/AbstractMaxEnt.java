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
package uk.ac.ebi.intact.confidence.maxent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.attribute.AnnotationConstants;
import uk.ac.ebi.intact.confidence.attribute.Attribute;
import uk.ac.ebi.intact.confidence.attribute.NullAttribute;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *                                                                21-Nov-2007
 *                                                                </pre>
 */
public abstract class AbstractMaxEnt {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( AbstractMaxEnt.class );

    private String attribPath;
    private String weightPath;

    protected HashMap<Attribute, Double> trueWeightMap;
    protected HashMap<Attribute, Double> falseWeightMap;

    protected AbstractMaxEnt() {
    }

    protected AbstractMaxEnt( String attribPath, String weightPath ) throws MaxEntClassifierException {
        this.attribPath = attribPath;
        this.weightPath = weightPath;
        initWeightLists( attribPath, weightPath );
    }

    protected void initWeightLists( String attribPath, String weightPath ) throws MaxEntClassifierException {
            ArrayList<Attribute> attribList = readAttribs( attribPath );

            List<Double> weights = readWeights( weightPath );
            if ( ( attribList.size() * 2 ) != weights.size() ) {
                throw new IllegalArgumentException(
                        "Pair of weights not present for each attribute!\n" +
                        "Total attributes = " + attribList.size() + ", total weights = " + weights.size() );
            } else if ( log.isInfoEnabled() ) {
                String comment = attribList.size() + " attributes, " + weights.size() +
                                 " weights read from files.\n";
                log.info( comment );
            }

            trueWeightMap = new HashMap<Attribute, Double>();
            falseWeightMap = new HashMap<Attribute, Double>();
            int j = 0;
            for ( int i = 0; i < attribList.size(); i++ ) {
                Attribute a = attribList.get( j );
                trueWeightMap.put( a, weights.get( j ) );
                j++;
                falseWeightMap.put( a, -weights.get( j ) );
                j++;
            }
        if ( log.isInfoEnabled() ) {
            log.info( "Weight maps successfully assigned." );
        }

    }

    public void printScoreProfile( String inPath ) throws MaxEntClassifierException {
        try {
            FileReader fr = new FileReader( inPath );
            BufferedReader br = new BufferedReader( fr );

            String line;
            double totalPairs = 0.0;
            double lowCount = 0.0;
            double medCount = 0.0;
            double hiCount = 0.0;
            while ( ( line = br.readLine() ) != null ) {
                totalPairs++;
                Double tScore = trueScoreFromLine( line );
                //sb.append(",");
                //sb.append(tScore);
                if ( tScore < 0.5 ) {
                    lowCount++;
                } else if ( tScore > 0.5 ) {
                    hiCount++;
                } else {
                    medCount++;
                }
            }
            fr.close();

            String out = totalPairs + " total interactions.";
            System.out.println( out );
            double lowPercent = ( lowCount / totalPairs ) * 100;
            double medPercent = ( medCount / totalPairs ) * 100;
            double hiPercent = ( hiCount / totalPairs ) * 100;

            out = "Low-confidence interactions:  " + lowCount + " -- " + lowPercent + "%";
            System.out.println( out );
            out = "Medium-confidence interactions:  " + medCount + " -- " + medPercent + "%";
            System.out.println( out );
            out = "High-confidence interactions:  " + hiCount + " -- " + hiPercent + "%";
            System.out.println( out );
        } catch ( FileNotFoundException e ) {
            throw new MaxEntClassifierException( e );
        } catch ( IOException e ) {
            throw new MaxEntClassifierException( e );
        }
    }

    public double trueScoreFromLine( String line ) {
        //  ProteinPair pair = FileMethods.getProteinPair(line);
        HashSet<Attribute> attribs = null;//FileMethods.parseAttributeLine( line );
        Double[] probs = probs( attribs );
        Double tScore = probs[0];
        return tScore;
    }

    public void writeInteractionScores( String inPath, String outPath ) throws MaxEntClassifierException {
        // inPath = path to an interaction & attribute file in standard format
        // outPath = path for output
        try {
            FileReader fr = new FileReader( inPath );
            BufferedReader br = new BufferedReader( fr );
            FileWriter fw = new FileWriter( outPath );
            PrintWriter pw = new PrintWriter( fw );

            String out = "> " ;// + FileMethods.getDateTime();
            pw.println( out );
            out = "> True-interaction probabilities: Input " + inPath;
            pw.println( out );
            out = "> Maxent weight file " + weightPath + ", attribute list " + attribPath;
            pw.println( out );
            String line;
            ProteinPair pair;
            HashSet<Attribute> attribs;
            while ( ( line = br.readLine() ) != null ) {
                pair = null;//FileMethods.getProteinPair( line );
                StringBuilder sb = new StringBuilder( pair.toString() );
                attribs = null;//FileMethods.parseAttributeLine( line );
                Double[] probs = probs( attribs );
                Double tScore = probs[0];
                sb.append( "," );
                sb.append( tScore );
                pw.println( sb.toString() );
            }


            fr.close();
            fw.close();
        } catch ( FileNotFoundException e ) {
            throw new MaxEntClassifierException( e );
        } catch ( IOException e ) {
            throw new MaxEntClassifierException( e );
        }
    }

    public Double score( Collection<Attribute> attribs, boolean scoreType ) {
        // un-normalised score for true/false outcome, given an attribute set

        Double sum = 0.0;
        if ( attribs == null || attribs.isEmpty() ) {
            return 1.0;
        }

        if ( scoreType == true ) {
            for ( Attribute a : attribs ) {
                if ( trueWeightMap.get( a ) != null ) {
                    sum = sum + trueWeightMap.get( a );
                }
            }
        } else {
            for ( Attribute a : attribs ) {
                if ( falseWeightMap.get( a ) != null ) {
                    sum = sum + falseWeightMap.get( a );
                }
            }
        }

        Double score = Math.exp( sum );
        return score;

    }

    public Double[] probs( Collection<Attribute> attribs ) {
        // return two-element array containing normalised probabilities

        Double tScore = score( attribs, true );
        Double fScore = score( attribs, false );
        Double normalizer = tScore + fScore;
        Double[] probs = new Double[2];
        probs[0] = tScore / normalizer;
        probs[1] = fScore / normalizer;
        return probs;
    }

    private ArrayList<Attribute> readAttribs( String attribPath ) throws MaxEntClassifierException {
        ArrayList<Attribute> attribs = new ArrayList<Attribute>();
        attribs.add( new NullAttribute() );
        try {
            FileReader fr = new FileReader( attribPath );
            BufferedReader br = new BufferedReader( fr );
            String line;
            while ( ( line = br.readLine() ) != null ) {
                if ( Pattern.matches( AnnotationConstants.commentExpr, line ) ) {
                    continue;
                }
                Attribute a = null;//FileMethods.parseAttribute( line );
                if ( a.getType() == Attribute.NULL_TYPE ) {
                    continue;
                }
                attribs.add( a );
            }
            fr.close();
        } catch ( FileNotFoundException e ) {
            throw new MaxEntClassifierException( e );
        } catch ( IOException e ) {
            throw new MaxEntClassifierException( e );
        }
        return attribs;
    }

    protected List<Double> readWeights( String weightPath ) throws MaxEntClassifierException {
        List<Double> weights = new ArrayList<Double>();
        try {
            FileReader fr = new FileReader( weightPath );
            BufferedReader br = new BufferedReader( fr );
            String line;
            while ( ( line = br.readLine() ) != null ) {
                weights.add( new Double( line ) );
            }
            fr.close();
            if ( log.isInfoEnabled() ) {
                log.info( "read " + weights.size() + " weights" );
            }
        } catch ( FileNotFoundException e ) {
            throw new MaxEntClassifierException( e );
        } catch ( IOException e ) {
            throw new MaxEntClassifierException( e );
        }
        return weights;
    }

}
