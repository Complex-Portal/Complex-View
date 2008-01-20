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
package uk.ac.ebi.intact.confidence.model.iterator;

import uk.ac.ebi.intact.bridges.blast.model.BlastInput;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.confidence.model.Confidence;
import uk.ac.ebi.intact.confidence.model.BinaryInteraction;
import uk.ac.ebi.intact.confidence.model.parser.BinaryInteractionParserUtils;
import uk.ac.ebi.intact.confidence.model.parser.BlastInputParserUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Iterator for a BlastInput file.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        14-Jan-2008
 *        </pre>
 */
public class BlastInputIterator implements Iterator<BlastInput> {

    /**
     * Reader on the data we are going to iterate.
     */
    private BufferedReader blastInputStreamReader;

    /**
     * Next line to be processed.
     */
    private String nextLine;

    /**
     * indicate if the line that has been read was already consummed by the user via the next() nethod.
     */
    private boolean lineConsummed = true;


    public BlastInputIterator( InputStream blastInputStream){
        blastInputStreamReader = new BufferedReader( new InputStreamReader(blastInputStream));
    }

    /////////////
    // Iterator

    public boolean hasNext() {
        try {
            if ( lineConsummed ) {
                nextLine = readNextLine();
                lineConsummed = false;
            }
        }
        catch ( IOException e ) {
            closeStreamReader();
            return false;
        }

        return ( nextLine != null );
    }

    public BlastInput next() {
       if ( nextLine == null && !hasNext() ) {
            throw new NoSuchElementException();
        }

        UniprotAc ac = BlastInputParserUtils.parseUniprotInfo(  nextLine);
        try {
            nextLine = blastInputStreamReader.readLine();
        } catch (IOException e){
            // ignore
        }
        Sequence seq = BlastInputParserUtils.parseSequence( nextLine );
        BlastInput interaction = new BlastInput(ac, seq);

        lineConsummed = true;
        nextLine = null;

        return interaction;
    }

    public void remove() {
       throw new UnsupportedOperationException();
    }

    /////////////////////
    // Private method(s).

    private String readNextLine() throws IOException {
        String line = null;
        if ( blastInputStreamReader != null ) {
            line = blastInputStreamReader.readLine();
            if ( line == null ) {
                closeStreamReader();
                blastInputStreamReader = null;
            }
        }
        return line;
    }

    private void closeStreamReader() {
        if ( blastInputStreamReader != null ) {
            try {
                blastInputStreamReader.close();
            } catch ( IOException e ) {
                // ignore
            }
        }
    }

}
