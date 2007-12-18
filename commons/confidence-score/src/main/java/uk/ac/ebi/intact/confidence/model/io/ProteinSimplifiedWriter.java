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
package uk.ac.ebi.intact.confidence.model.io;

import uk.ac.ebi.intact.confidence.model.ProteinSimplified;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        13-Dec-2007
 *        </pre>
 */
public interface ProteinSimplifiedWriter {

    void append ( ProteinSimplified protein, File outFile ) throws IOException;
    void append ( List<ProteinSimplified> proteins, File outFile) throws IOException;
    void write ( List<ProteinSimplified> proteins, File outFile ) throws IOException;

    void appendGO( ProteinSimplified protein, File outFile ) throws IOException;

    void appendGO( List<ProteinSimplified> proteins, File outFile ) throws IOException;

    void writeGO( List<ProteinSimplified> proteins, File outFile ) throws IOException;

    void appendIp( ProteinSimplified protein, File outFile ) throws IOException;

    void appendIp( List<ProteinSimplified> proteins, File outFile ) throws IOException;

    void writeIp( List<ProteinSimplified> proteins, File outFile ) throws IOException;

    void appendSeq( ProteinSimplified protein, File outFile ) throws IOException;

    void appendSeq( List<ProteinSimplified> proteins, File outFile ) throws IOException;

    void writeSeq( List<ProteinSimplified> proteins, File outFile ) throws IOException;

}
