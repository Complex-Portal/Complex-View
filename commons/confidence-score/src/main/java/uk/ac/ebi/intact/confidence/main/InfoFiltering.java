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
package uk.ac.ebi.intact.confidence.main;

import uk.ac.ebi.intact.confidence.filter.FilterException;
import uk.ac.ebi.intact.confidence.filter.GOAFilter;
import uk.ac.ebi.intact.confidence.filter.GOAFilterMapImpl;
import uk.ac.ebi.intact.confidence.filter.SeqAlignFilter;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.ProteinAnnotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Filter the Go, InterPro, Blast hits characteristics
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *               07-Dec-2007
 *               </pre>
 */
public class InfoFiltering {
    private static GOAFilter filter = new GOAFilterMapImpl();

    @Deprecated
    public static void filterGo (Set<ProteinAnnotation> proteinAnnotations) throws FilterException {
         for ( Iterator<ProteinAnnotation> iter = proteinAnnotations.iterator(  ); iter.hasNext();){
             ProteinAnnotation pa = iter.next();
             Set<Identifier> gos = new HashSet<Identifier>( pa.getAnnotations() );
             filter.filterGO( proteinAnnotations );                                     // TODO: filter gos on collection
             pa.setAnnotations( gos );
         }
    }

    public static void filterGO( Collection<ProteinAnnotation> proteinAnnotations, File goaFile) throws IOException, FilterException {
        filter.initialize( goaFile );
        filter.filterGO( proteinAnnotations );
    }

    public static void filterGO( ProteinAnnotation proteinAnnotation, File goaFile ) throws IOException, FilterException {
        filter.initialize( goaFile );
        filter.filterGO( proteinAnnotation );
    }

    public static void filterBlastHits( Set<Identifier> uniprotAcs, File highConf ) throws FileNotFoundException {
        // finlaize the filter
        SeqAlignFilter.setHighConfProteins( highConf );
        SeqAlignFilter.filterHighConfAlign( uniprotAcs );
    }
}
