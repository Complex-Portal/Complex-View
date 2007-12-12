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
package uk.ac.ebi.intact.confidence.attribute;

import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.model.Identifier;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        07-Dec-2007
 *        </pre>
 */
public class BlastFilter {

    public static void filterBlastHits( Set<Identifier> blastHits, File highConf){
        Set<Identifier> wantedHits = getHighConfidenceProteins(highConf);
        blastHits.retainAll( wantedHits);
    }

    private static Set<Identifier> getHighConfidenceProteins(File highConf) {
         //TODO:
        try {
            BinaryInteractionSet bis = new BinaryInteractionSet(highConf.getPath());
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return null;
    }

}
