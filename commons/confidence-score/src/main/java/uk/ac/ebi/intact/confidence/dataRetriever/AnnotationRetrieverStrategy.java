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
package uk.ac.ebi.intact.confidence.dataRetriever;

import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.ProteinSimplified;

import java.util.List;
import java.util.Set;

/**
 * Strategy to get the InterPro and GO annotation for proteins.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public interface AnnotationRetrieverStrategy {

    public Sequence getSeq( Identifier id );

    public Sequence getSeq( UniprotAc ac );

    public Set<Identifier> getGOs( Identifier id );

    public Set<Identifier> getGOs( UniprotAc ac );

    public Set<Identifier> getIps( Identifier id );

    public Set<Identifier> getIps( UniprotAc ac );

    public void getSequences( List<ProteinSimplified> proteins );

    public void getGOs( List<ProteinSimplified> proteins );

    public void getIps( List<ProteinSimplified> proteins );
}
