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
import uk.ac.ebi.intact.confidence.FileMethods;
import uk.ac.ebi.intact.confidence.attribute.Attribute;
import uk.ac.ebi.intact.confidence.weights.inputs.model.ArffAttribute;
import uk.ac.ebi.intact.confidence.weights.inputs.model.ArffAttributeType;

import java.io.*;
import java.util.*;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *                                                         21-Nov-2007
 *                                                         </pre>
 */
public class WekaMaxEntClassifier extends AbstractMaxEnt {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( WekaMaxEntClassifier.class );

    public WekaMaxEntClassifier( String attribsPath, String weightsPath ) throws MaxEntClassifierException {
        super( attribsPath, weightsPath );
    }

    @Override
    protected void initWeightLists( String attribPath, String weightPath ) throws MaxEntClassifierException {
        List<ArffAttribute> attribList = readAttribs( attribPath );

        List<Double> weights = readWeights( weightPath );
        if (log.isInfoEnabled() && ( attribList.size() != ( weights.size() - 1 ) )) {
            log.info("Number of Attributes not equivalent for the number of weights!\n" +
                    "Total attributes = " + attribList.size() + ", total weights = " + weights.size() );
        } else if ( log.isInfoEnabled() ) {
            String comment = attribList.size() + " attributes, " + weights.size() +
                             " weights read from files.\n";
            log.info( comment );
        }

        trueWeightMap = new HashMap<Attribute, Double>();
        falseWeightMap = new HashMap<Attribute, Double>();
        //   int j = 0;
        for ( int i = 0; i < attribList.size(); i++ ) {
            ArffAttribute arff = attribList.get( i );
            Attribute attrib = getAttribute( arff );
            if ( arff.getType().equals( ArffAttributeType.HIGH ) ) {
                trueWeightMap.put( attrib, weights.get( i ) );
                falseWeightMap.put( attrib, weights.get( i ) );
            } else if ( arff.getType().equals( ArffAttributeType.LOW ) ) {
                trueWeightMap.put( attrib, -weights.get( i ) );
                falseWeightMap.put( attrib, -weights.get( i ) );
            } else if ( arff.getType().equals( ArffAttributeType.BOTH ) ) {
                trueWeightMap.put( attrib, weights.get( i ) );
                falseWeightMap.put( attrib, -weights.get( i ) );
            } else {
                if ( log.isInfoEnabled() ) {
                    log.info( "In this context ArffType should be HIGH, LOW or BOTH. Please check ArffAttribute: " + arff );
                }
            }
//                trueWeightMap.put( a, weights.get( j ) );
//                j++;
//                falseWeightMap.put( a, -weights.get( j ) );
//                j++;
        }

        File outWeightsMap = new File("E:\\tmp\\weightsMapping.txt");
       try {
            Writer writer = new FileWriter(outWeightsMap);
            writer.append("true weights: \n");
            printMaps(writer, trueWeightMap);
            writer.append("false weights: \n");
            printMaps(writer, falseWeightMap);
            writer.close();
       } catch ( IOException e ) {
           e.printStackTrace();
       }
        if ( log.isInfoEnabled() ) {
            log.info( "Weight maps successfully assigned." );
        }
    }

    protected void printMaps( Writer writer, Map<Attribute,Double> weightMap) {
        for ( Iterator<Attribute> iterator = weightMap.keySet().iterator(); iterator.hasNext(); ) {
            Attribute attr =  iterator.next();
            try {
                writer.append(attr.toString() + "= " + weightMap.get( attr) + "\n");
            } catch ( IOException e ) {
                e.printStackTrace();
            }

        }
    }

    protected Attribute getAttribute( ArffAttribute arff ) {
        String attr = arff.getAttribute();
        return FileMethods.parseAttribute( attr );
    }

    private List<ArffAttribute> readAttribs( String attribsPath ) throws MaxEntClassifierException {
        if ( log.isInfoEnabled() ) {
            log.info( "reading attributes ..." );
        }
        List<ArffAttribute> attribs = new ArrayList<ArffAttribute>();
        try {
            BufferedReader br = new BufferedReader( new FileReader( attribsPath ) );
            String line;
            int count = 0;
            while ( ( line = br.readLine() ) != null ) {
                if ( !line.startsWith( "class" ) ) {
                    ArffAttribute a = parseAttribute( line );
                    if ( !attribs.contains( a ) ) {
                        attribs.add( a );
                    }
                    count++;
                    if ( log.isInfoEnabled() && ( count % 20 ) == 0 ) {
                        log.info( "read " + attribs.size() + " from " + count + " lines" );
                    }
                }
            }
            br.close();
            if ( log.isInfoEnabled() ) {
                log.info( "reading attributes finished." );
                log.info( "read " + attribs.size() + " from " + count + " lines" );
            }
        } catch ( FileNotFoundException e ) {
            throw new MaxEntClassifierException( e );
        } catch ( IOException e ) {
            throw new MaxEntClassifierException( e );
        }

        return attribs;

    }

    private ArffAttribute parseAttribute( String line ) throws MaxEntClassifierException {
        String[] aux = line.split( "=" );
        if ( aux.length == 2 ) {
            ArffAttributeType type = getType( aux[1] );
            ArffAttribute arff = new ArffAttribute( aux[0], type );
            return arff;
        } else {
            throw new MaxEntClassifierException( "attributesFile not expected format: " + line );
        }


    }

    private ArffAttributeType getType( String s ) throws MaxEntClassifierException {
        if ( ArffAttributeType.HIGH.toString().equalsIgnoreCase( s ) ) {
            return ArffAttributeType.HIGH;
        } else if ( ArffAttributeType.LOW.toString().equalsIgnoreCase( s ) ) {
            return ArffAttributeType.LOW;
        } else if ( ArffAttributeType.BOTH.toString().equalsIgnoreCase( s ) ) {
            return ArffAttributeType.BOTH;
        } else if ( ArffAttributeType.NONE.toString().equalsIgnoreCase( s ) ) {
            return ArffAttributeType.NONE;
        } else {
            throw new MaxEntClassifierException( "ArffAttributeType not valid: " + s );
        }
    }
}
