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
package uk.ac.ebi.intact.confidence.model.parser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.confidence.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Parser util class for BinaryInteractin files.
 * line: <uniprotAc>;<uniprotAc>
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 *        <pre>
 *        12-Dec-2007
 *        </pre>
 */
public class BinaryInteractionParserUtils {
    /**
	 * Sets up a logger for that class.
	 */
	public static final Log log	= LogFactory.getLog( BinaryInteractionParserUtils.class);

    public static Identifier parseIdentifier(String field){
        if ( Pattern.matches( UniprotIdentifierImpl.getRegex(), field )){
            return new UniprotIdentifierImpl( field);
        } else if (Pattern.matches( InterProIdentifierImpl.getRegex(), field)){
            return new InterProIdentifierImpl(field);
        } else if (Pattern.matches( GoIdentifierImpl.getRegex(), field )){
            return new GoIdentifierImpl( field);
        } else if (Pattern.matches( IntActIdentifierImpl.getRegex(), field )){
            return new IntActIdentifierImpl( field);
        } else {
            if ( log.isInfoEnabled() ) {
                    log.info( "Field not in the expected format! " + field );
             }
            return null;
        }
    }

    public static BinaryInteraction parseBinaryInteraction(String field, Confidence conf){
        BinaryInteraction biInt = null;
        String [] aux = field.split (";");
        if (aux.length == 2 ){
            Identifier idA = BinaryInteractionParserUtils.parseIdentifier( aux[0] );
            Identifier idB = BinaryInteractionParserUtils.parseIdentifier( aux[1] );
            if (idA != null && idB != null){
                biInt = new BinaryInteraction(idA, idB, conf);
            } else {
                if ( log.isInfoEnabled() ) {
                    log.info( "Line not in the expected format! <uniprotAc>;<uniprotAc>" );
                }
             }
        }
        return biInt;
    }


    public static Attribute<Identifier> parseAttribute(String field){
        String [] aux = field.split (";");
        if (aux.length == 2 ){
            Identifier idA = BinaryInteractionParserUtils.parseIdentifier( aux[0] );
            Identifier idB = BinaryInteractionParserUtils.parseIdentifier( aux[1] );
            if (idA != null && idB != null){
                return new IdentifierAttributeImpl(idA, idB );
            } else {
                if ( log.isInfoEnabled() ) {
                    log.info( "Line not in the expected format! <uniprotAc>;<uniprotAc>" );
                }
             }
        }
        return null;
    }

    public static BinaryInteractionAttributes parseBinaryInteractionAttributes(String line, Confidence conf){
        BinaryInteractionAttributes intAttribs = null;
        String [] aux = line.split( ";");
        BinaryInteraction biInt = parseBinaryInteraction( aux[0], conf);
        if (biInt != null && aux.length > 1){
            List<Attribute> attribs = new ArrayList<Attribute>(aux.length -1);
            for (int i =1; i< aux.length; i++){

            }
        }
        return intAttribs;
    }


    /////////////////////
    // Private method(s)
}
