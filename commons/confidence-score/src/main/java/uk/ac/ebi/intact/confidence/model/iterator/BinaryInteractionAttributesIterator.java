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
package uk.ac.ebi.intact.confidence.model.iterator;

import uk.ac.ebi.intact.confidence.model.BinaryInteractionAttributes;
import uk.ac.ebi.intact.confidence.model.Confidence;
import uk.ac.ebi.intact.confidence.model.parser.BinaryInteractionParserUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        12-Dec-2007
 *        </pre>
 */
public class BinaryInteractionAttributesIterator implements Iterator<BinaryInteractionAttributes> {

    private BufferedReader interactionAttribsStreamReader;
    private Confidence conf = Confidence.UNKNOWN;

    private String nextLine;
    private boolean lineConsummed;

    public BinaryInteractionAttributesIterator( InputStream binaryInteractionStream){
        interactionAttribsStreamReader = new BufferedReader(new InputStreamReader(binaryInteractionStream));
    }

    public void setConfidence(Confidence confidence){
        this.conf = confidence;
    }

    //////////////
    // Iterator.
    public boolean hasNext() {
        try {
            if (lineConsummed){
                nextLine = readNextLine();
                lineConsummed = false;
            }
        } catch ( IOException e ) {
            closeStreamReader();
            return false;
        }
        return (nextLine != null);
    }

    public BinaryInteractionAttributes next() {
        if ( nextLine == null && !hasNext() ) {
            throw new NoSuchElementException();
        }

        BinaryInteractionAttributes intAttribs = BinaryInteractionParserUtils.parseBinaryInteractionAttributes( nextLine, conf );

        lineConsummed = true;
        nextLine = null;
        return intAttribs;
    }

    public void remove() {
        throw new UnsupportedOperationException( );
    }

    ////////////////////
    // Private Methods.

      private String readNextLine() throws IOException {
        String line = null;
        if ( interactionAttribsStreamReader != null ) {
            line = interactionAttribsStreamReader.readLine();
            if ( line == null ) {
                closeStreamReader();
                interactionAttribsStreamReader = null;
            }
        }
        return line;
    }

     private void closeStreamReader() {
        if ( interactionAttribsStreamReader != null ) {
            try {
                interactionAttribsStreamReader.close();
            } catch ( IOException e ) {
                // ignore
            }
        }
    }
}

