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
package uk.ac.ebi.intact.confidence.util;

import uk.ac.ebi.intact.bridges.blast.BlastConfig;
import uk.ac.ebi.intact.bridges.blast.BlastServiceException;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.model.Attribute;

import java.util.List;
import java.util.Set;

/**
 * Strategy for creating the attributes for an interaction represented by a protein pair.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        29-Nov-2007
 *        </pre>
 */
public interface AttributeGetter {

    public List<Attribute> fetchGoAttributes(ProteinPair proteinPair) throws AttributeGetterException;
    public List<Attribute> fetchIpAttributes(ProteinPair proteinPair);
    public List<Attribute> fetchAlignAttributes(ProteinPair proteinPair, Set<UniprotAc> againstProteins, BlastConfig blastConfig ) throws BlastServiceException;

    public List<Attribute> fetchAllAttributes(ProteinPair proteinPair, Set<UniprotAc> againstProteins, BlastConfig blastConfig ) throws AttributeGetterException;

}
