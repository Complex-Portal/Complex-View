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
package uk.ac.ebi.intact.confidence.model.io.impl;

import uk.ac.ebi.intact.confidence.model.BinaryInteractionAttributes;
import uk.ac.ebi.intact.confidence.model.ConfidenceSet;
import uk.ac.ebi.intact.confidence.model.io.ConfidenceSetWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

/**
 * Implementation of the ConfidenceSetWriter.
 * Writes a confidence set in the format expected as input for training the model.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        18-Jan-2008
 *        </pre>
 */
public class ConfidenceSetModelInputWriterImpl implements ConfidenceSetWriter {
    public void write( ConfidenceSet confidenceSet, File outFile ) throws IOException {
        Writer writer = new FileWriter(outFile);
        for ( Iterator<BinaryInteractionAttributes> iter = confidenceSet.getBinaryInteractions().iterator(); iter.hasNext(); ) {
            BinaryInteractionAttributes bia =  iter.next();
            writer.write( bia.convertToModleInputString() + "\n" );
        }
        writer.close();
    }
}
