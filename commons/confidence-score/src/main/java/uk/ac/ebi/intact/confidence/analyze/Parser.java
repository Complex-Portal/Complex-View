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

import uk.ac.ebi.intact.confidence.ProteinPair;

/**
 * Class that parses attribute files
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        12-Nov-2007
 *        </pre>
 */
public class Parser {

    public static void parseAttribLine(String line){
        // line of the format : P31946;P33176,O60282;Q04917,IPR000308;IPR001752
        // it does not check if the information makes sence aslong it is of the format .*;.*,{ .*;.*,}

        String [] components = line.split(",");
        String [] proteins = components[0].split(";");
        ProteinPair pp = new ProteinPair(proteins[0], proteins[1]);
        for(int i=1; i< components.length; i++){
            String [] attrib = components[i].split(";");
//            Attribute attribute = new IpPairAttribute(attrib[0], attrib[1]);
        }
    }
    
}
