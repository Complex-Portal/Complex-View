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
package uk.ac.ebi.intact.confidence.model.parser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;

/**
 * Parser for BlastInput files.
 * (>uniprotAc|description
 * sequence)
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        14-Jan-2008
 *        </pre>
 */
public class BlastInputParserUtils {

     /**
	 * Sets up a logger for that class.
	 */
	public static final Log log	= LogFactory.getLog( BlastInputParserUtils.class);

    public static UniprotAc parseUniprotInfo(String field){
        String [] aux = field.split("\\|");
        if (aux.length != 2){
            if (log.isWarnEnabled()){
                log.warn("Different format than expected: " + field);
            }
        }
        return new UniprotAc ( aux[0]. substring( 1 ));        
    }

    public static Sequence parseSequence(String field){
        return new Sequence(field);
    }
}
