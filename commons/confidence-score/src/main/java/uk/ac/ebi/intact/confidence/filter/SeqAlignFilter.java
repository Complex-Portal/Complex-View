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
package uk.ac.ebi.intact.confidence.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.confidence.model.BinaryInteraction;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.ProteinAnnotation;
import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionReader;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionReaderImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Filter for the seq. alignment output.
 * (retaining only the high confidence proteins)
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        15-Jan-2008
 *        </pre>
 */
public class SeqAlignFilter {
     /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( SeqAlignFilter.class );

    private static Set<Identifier> highconfProts;

    public static void setHighConfProteins(File highConf) throws FileNotFoundException {
        highconfProts = new HashSet<Identifier>();
        BinaryInteractionReader bir = new BinaryInteractionReaderImpl();
        for ( Iterator<BinaryInteraction> iter = bir.iterate( highConf ); iter.hasNext();){
            BinaryInteraction bi = iter.next();
            if (!highconfProts.contains( bi.getFirstId() )){
                highconfProts.add( bi.getFirstId() );
            }
            if (!highconfProts.contains( bi.getSecondId() )){
                highconfProts.add( bi.getSecondId() );
            }
        }
        if (log.isInfoEnabled()){
            log.info( "Read : " + highconfProts.size() + " highconf proteins.");
        }
    }

    public static void setHighConfProteins(Set<Identifier> proteins){
        highconfProts = proteins;
    }

    public static void filterHighConfAlign( Set<Identifier> uniprotIds){
        if (uniprotIds != null){
            uniprotIds.retainAll( highconfProts );
        }
    }

    public static void filter(Set<ProteinAnnotation> proteinAnnotations){
        for ( Iterator<ProteinAnnotation> iter = proteinAnnotations.iterator(); iter.hasNext(); )
        {
            ProteinAnnotation pa =  iter.next();
            Set<Identifier> prots = new HashSet<Identifier>( pa.getAnnotations() );
             SeqAlignFilter.filterHighConfAlign( prots );
             pa.setAnnotations( prots );
        }        
    }
}
