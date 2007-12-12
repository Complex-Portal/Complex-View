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

import uk.ac.ebi.intact.confidence.model.ProteinAnnotation;
import uk.ac.ebi.intact.confidence.model.parser.ProteinAnnotationParserUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator for ProteinAnnotation File.
 * (template psidev.psi.mi.tab.PsimiTabIterator)
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 *        <pre>
 *               12-Dec-2007
 *               </pre>
 */
public class ProteinAnnotationIterator implements Iterator<ProteinAnnotation> {

    /**
     * Reader on the data we are going to iterate.
     */
    private BufferedReader interactionStreamReader;
    /**
     * Next line to be processed.
     */
    private String nextLine;

    /**
     * Line number currently being parsed.
     */
    private int lineIndex = 0;

    /**
     * indicate if the line that has been read was already consummed by the user via the next() nethod.
     */
    private boolean lineConsummed = true;

    public ProteinAnnotationIterator( InputStream proteinAnnotationStream ) {
        interactionStreamReader = new BufferedReader( new InputStreamReader( proteinAnnotationStream ) );
    }

    /////////////
    // Iterator
    public boolean hasNext() {
        try {
            if ( lineConsummed ) {
                nextLine = readNextLine();
                lineIndex++;
                lineConsummed = false;
            }
        }
        catch ( IOException e ) {
            closeStreamReader();
            return false;
        }

        return ( nextLine != null );
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public ProteinAnnotation next() {
     if ( nextLine == null && !hasNext() ) {
            throw new NoSuchElementException();
        }

        ProteinAnnotation protAnnotation = ProteinAnnotationParserUtils.parseProteinAnnotation( nextLine );

        lineConsummed = true;
        nextLine = null;

        return protAnnotation;
    }

     /////////////////////
    // Private method(s).

    private String readNextLine() throws IOException {
        String line = null;
        if ( interactionStreamReader != null ) {
            line = interactionStreamReader.readLine();
            if ( line == null ) {
                closeStreamReader();
                interactionStreamReader = null;
            }
        }
        return line;
    }

    private void closeStreamReader() {
        if ( interactionStreamReader != null ) {
            try {
                interactionStreamReader.close();
            } catch ( IOException e ) {
                // ignore
            }
        }
    }
}
