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

import java.io.*;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        26-Nov-2007
 *        </pre>
 */
public class OpenNLP {

    public static void createInput(String hcAttribsPath, String lcAttribsPath, String outPath){
        File outFile = new File(outPath);
        try {
            Writer writer = new FileWriter(outFile, false);
            createInput(hcAttribsPath, writer, "high");
            writer.close();
            writer =  new FileWriter(outFile, true);
            createInput( lcAttribsPath, writer, "low");
            writer.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private static void createInput( String hcAttribsPath, Writer writer , String type) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(hcAttribsPath));
            String line="";
            while ((line = br.readLine()) != null){
                String input =  getInfo(line);
                input +=type + "\n";
                writer.append( input);
            }
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public static String getInfo( String line) {
        String [] aux = line.split(",");
        String info ="";
        for (int i =1; i< aux.length; i++){
            info +=aux[i] + " ";
        }
        return info;       
    }

    public static String[] getAttribsFromLine( String line ) {
       String [] aux = line.split(",");
        if (aux.length == 1){
            return new String[0];
        } else {
            String result = aux[1];
            for (int i=2; i< aux.length; i++){
                result += "," + aux[i];
            }
            return result.split(",");
        }
    }

    /**
     *    TODO: implement this
     * @param inFilePath : the outputfile of the GISmodel
     * @param outFilePath : (nrAttribs * 2 ) lines , each line has a double
     */
    public static void parseWeights(String inFilePath, String outFilePath){
        try {
            BufferedReader br = new BufferedReader(new FileReader(inFilePath));
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        }
    }
}
