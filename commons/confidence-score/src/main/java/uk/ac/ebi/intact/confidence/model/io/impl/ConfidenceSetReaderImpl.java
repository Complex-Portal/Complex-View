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
import uk.ac.ebi.intact.confidence.model.Confidence;
import uk.ac.ebi.intact.confidence.model.ConfidenceSet;
import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionAttributesReader;
import uk.ac.ebi.intact.confidence.model.io.ConfidenceSetReader;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Implementation of the reader for the confidence set.
 * The input should be in the format:
 * <identifier1>;<identifier2>(,<attr11>;<attr12>)*
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        18-Jan-2008
 *        </pre>
 */
public class ConfidenceSetReaderImpl implements ConfidenceSetReader {

    public ConfidenceSet read( File inFile, Confidence confidence) throws IOException {
        BinaryInteractionAttributesReader biar = new BinaryInteractionAttributesReaderImpl();
        biar.setConfidence( confidence );
        List<BinaryInteractionAttributes> attribs = biar.read( inFile );

        ConfidenceSet cs = new ConfidenceSet(confidence, attribs);
        return cs;  
    }
}
