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

import uk.ac.ebi.intact.confidence.model.InteractionSimplified;
import uk.ac.ebi.intact.confidence.model.io.InteractionSimplifiedWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;

/**
 * Implementation of the writer for the InteractionSimplified.
 *
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre> 13-Dec-2007 </pre>
 */
public class InteractionSimplifiedWriterImpl implements InteractionSimplifiedWriter {
    public void append( InteractionSimplified interactionSimplified, File outFile ) throws IOException {
       Writer writer = new FileWriter(outFile, true);
       writer.append( interactionSimplified.convertToString() + "\n");
       writer.close();
    }

    public void append( Collection<InteractionSimplified> interactions, File outFile ) throws IOException {
        Writer writer = new FileWriter(outFile, true);
        for ( Iterator<InteractionSimplified> iterator = interactions.iterator(); iterator.hasNext(); ) {
            InteractionSimplified interactionSimplified = iterator.next();
            writer.append(interactionSimplified.convertToString()  + "\n");
        }
        writer.close();
    }

    public void write( Collection<InteractionSimplified> interactions, File outFile ) throws IOException {
        Writer writer = new FileWriter(outFile);
        for ( Iterator<InteractionSimplified> iterator = interactions.iterator(); iterator.hasNext(); ) {
            InteractionSimplified interactionSimplified = iterator.next();
            writer.write(interactionSimplified.convertToString()  + "\n");
        }
        writer.close();
    }
}
