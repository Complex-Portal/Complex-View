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
package uk.ac.ebi.intact.confidence.model.io.impl;

import uk.ac.ebi.intact.confidence.model.BinaryInteraction;
import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the BinaryInteractionWriter.
 * Writes the BinaryInteraction in the form of
 * <identifier1>;<identifier2>
 * ignoring if there is or not a confidence value stored
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        11-Dec-2007
 *        </pre>
 */
public class BinaryInteractionWriterImpl implements BinaryInteractionWriter {

    public void append( BinaryInteraction binaryInteraction, File outFile ) throws IOException {
        Writer writer = new FileWriter(outFile, true);
        writer.append( binaryInteraction.convertToString() + "\n");
        writer.close();
    }

    public void append (List<BinaryInteraction> binaryInteractions, File outFile) throws IOException{
        Writer writer = new FileWriter(outFile, true);
        for ( Iterator<BinaryInteraction> iterator = binaryInteractions.iterator(); iterator.hasNext(); ) {
            BinaryInteraction binaryInteraction = iterator.next();
            writer.append( binaryInteraction.convertToString() + "\n");
        }
    }

    public void write( Collection<BinaryInteraction> binaryInteractions, File outFile ) throws IOException {
         Writer writer = new FileWriter(outFile);
        for ( Iterator<BinaryInteraction> interactionIterator = binaryInteractions.iterator(); interactionIterator.hasNext(); )
        {
            BinaryInteraction binaryInteractionAttributes = interactionIterator.next();
            writer.append( binaryInteractionAttributes.convertToString() + "\n");
        }
        writer.close();
    }
}
