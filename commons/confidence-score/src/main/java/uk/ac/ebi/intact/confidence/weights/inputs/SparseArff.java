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
package uk.ac.ebi.intact.confidence.weights.inputs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.confidence.weights.inputs.model.ArffAttribute;
import uk.ac.ebi.intact.confidence.weights.inputs.model.ArffAttributeType;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *               21-Nov-2007
 *               </pre>
 */
public class SparseArff {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( SparseArff.class );

    private int nrHc; // count of high confidence interactions
    private int nrLc; // count of low confidence interactions

    public SparseArff() {
    }

    public void createArff( String hcPath, String lcPath, String outAttribs, String outPath ) {
        if ( log.isInfoEnabled() ) {
            log.info( "starting create Arff ..." );
        }
        long start = System.currentTimeMillis();
        List<ArffAttribute> attribs = getAttribs( hcPath, lcPath );
        if ( log.isInfoEnabled() ) {
            log.info( "read " + attribs.size() + " attribs; from " + nrHc + " high confidence interactions and " + nrLc + " low confidence interactions" );
        }
        File attribsFile = new File( outAttribs );
        printAttribs( attribs, attribsFile );
        writeHeaderArff( attribs, outPath );
        log.info( "printed attribs to file: " + attribsFile.getPath() );
        try {
            Writer w = new FileWriter( outPath, true );
            List<String> names = getNames( attribs );
            writeArff( names, hcPath, "high", w );
            w.close();
            w = new FileWriter( outPath, true );
            writeArff( names, lcPath, "low", w );
            w.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        if ( log.isInfoEnabled() ) {
            long total = ( end - start ) / 6000;
            log.info( "finished creating arff: " + total + " min" );
        }
    }

    private List<String> getNames( List<ArffAttribute> attribs ) {
        List<String> names = new ArrayList<String>( attribs.size() );
        for ( int i = 0; i < attribs.size(); i++ ) {
            names.add( attribs.get( i ).getAttribute() );
        }

        return names;
    }

    private void writeHeaderArff( List<ArffAttribute> attribs, String outPath ) {
        try {
            Writer writer = new FileWriter( outPath );
            writer.append( "@relation confidence\n\n" );
            for ( int i = 0; i < attribs.size() - 1; i++ ) {
                writer.append( "@attribute " + attribs.get( i ).getAttribute() + " {0,1}\n" );
            }
            writer.append( "@attribute class {high, low}\n\n" );
            writer.append( "@data\n" );
            writer.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private void writeArff( List<String> attribs, String setPath, String type, Writer writer ) {
        try {
            long start = System.currentTimeMillis();
            if ( log.isInfoEnabled() ) {
                log.info( "writing arff for : " + setPath );
            }
            BufferedReader br = new BufferedReader( new FileReader( setPath ) );
            String line = "";
            int count = 0;
            while ( ( line = br.readLine() ) != null ) {
                String[] aux = line.split( "," );
                int[] encoding = getEncoding( attribs, aux );
                String code = "{" + getCode( encoding );
                code += attribs.indexOf( "class" ) + " " + type + "}\n";
                writer.append( code );
                count++;
                if ( log.isInfoEnabled() ) {
                    if ( ( count % 20 ) == 0 ) {
                        log.info( "written " + count + " from " + ( type.equalsIgnoreCase( "high" ) ? nrHc : nrLc ) );
                    }
                }
            }
            if ( log.isInfoEnabled() ) {
                log.info( "written " + count + " from " + ( type.equalsIgnoreCase( "high" ) ? nrHc : nrLc ) );
            }
            br.close();
            if ( log.isInfoEnabled() ) {
                long end = System.currentTimeMillis();
                long total = ( end - start ) / 60000;
                log.info( "finished writing arff for : " + setPath + " in " + total + " min" );
            }
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private String getCode( int[] encoding ) {
        Arrays.sort( encoding );
        String code = "";
        for ( int i = 0; i < encoding.length; i++ ) {
            code += encoding[i] + " 1,";
        }
        return code;
    }

    private int[] getEncoding( List<String> attribs, String[] aux ) {
        int[] code = new int[aux.length - 1];
        for ( int i = 1; i < aux.length; i++ ) {
            code[i - 1] = attribs.indexOf( aux[i] );
        }
        return code;
    }

    private void printAttribs( List<ArffAttribute> attribs, File attribsFile ) {
        try {
            Writer w = new FileWriter( attribsFile );
            for ( int i = 0; i < attribs.size(); i++ ) {
                w.append( attribs.get( i ).toString() + "\n" );
            }
            w.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }


    private List<ArffAttribute> getAttribs( String hcSetPath, String lcSetPath ) {
        if ( log.isInfoEnabled() ) {
            log.info( "getting attribs ... " );
        }
        long start = System.currentTimeMillis();
        List<ArffAttribute> attribs = new ArrayList<ArffAttribute>();
        if ( log.isInfoEnabled() ) {
            log.info( "getting attribs form hc" );
        }
        nrHc = addAttribs( attribs, hcSetPath, ArffAttributeType.HIGH );
        if ( attribs == null ) {
            System.out.println( "no attribs fetched form hcSet!!!!" );
            System.exit( 0 );
        }

        if ( log.isInfoEnabled() ) {
            log.info( "hc attribs : " + attribs.size() );
            log.info( "getting attribs form lc" );
        }
        nrLc = addAttribs( attribs, lcSetPath, ArffAttributeType.LOW );

        if ( log.isInfoEnabled() ) {
            log.info( "hc + lc unique attribs : " + attribs.size() );
            long end = System.currentTimeMillis();
            long total = ( end - start ) / 60000;
            log.info( "finished getting attribs(" + attribs.size() + "): " + total + " min" );
        }
        attribs.add( new ArffAttribute( "class", ArffAttributeType.BOTH ) );
        return attribs;
    }

    private int addAttribs( List<ArffAttribute> attribs, String setPath, ArffAttributeType type ) {
        int nrLines = 0;
        try {
            Reader reader = new FileReader( setPath );
            BufferedReader br = new BufferedReader( reader );
            String line = "";
            while ( ( line = br.readLine() ) != null ) {
                nrLines++;
                String[] aux = line.split( "," );
                for ( int i = 1; i < aux.length; i++ ) {
                    ArffAttribute attr = new ArffAttribute( aux[i], type );
                    if ( !attribs.contains( attr ) ) {
                        attribs.add( attr );
                    } else {
                        ArffAttribute oldAttr = attribs.get( attribs.indexOf( attr ) );
                        if ( type != oldAttr.getType() ) {
                            oldAttr.setType( ArffAttributeType.BOTH );
                        }
                    }
                }
                if ( log.isInfoEnabled() && ( nrLines % 20 ) == 0 ) {
                    log.info( "attributes added for #lines:" + nrLines );
                }
            }
            br.close();
            if ( log.isInfoEnabled() ) {
                log.info( "attributes added for #lines:" + nrLines );
            }
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return nrLines;
    }
}
