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
import uk.ac.ebi.intact.confidence.dataRetriever.IntactAnnotationRetrieverImpl;
import uk.ac.ebi.intact.confidence.ehcache.GOACache;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.ProteinAnnotation;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Filter for the GO annotation using the EhCache.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 19.02.2008
 */
public class GOAFilterCacheImpl implements GOAFilter {
     /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog( GOAFilterCacheImpl.class);

    private static boolean initialized = false;

    public void initialize( File goaFile ) throws FilterException {
        if (!initialized){
             IntactAnnotationRetrieverImpl intact = new IntactAnnotationRetrieverImpl();
            try {
                GOACache.getInstance().loadGOA( goaFile, intact.getUniprotProteins() );
                initialized = true;
            } catch ( IOException e ) {
                throw new FilterException( e);
            }
        } else {
             if (log.isDebugEnabled()){
                 log.debug("Filter already initialized!");
             }
         }
    }

    public void clean(){
        GOACache.getInstance().clean();
        initialized = false;
    }

    public void filterGO( Identifier id, Set<Identifier> gos ) throws FilterException {
        if (gos != null){
            gos.remove( forbiddenGO );
            gos.removeAll( GOACache.getInstance().fetchByUniprotAc( id.getId() ));
        }
    }

    public void filterGO( ProteinAnnotation proteinAnnotation ) throws FilterException {
        if (!initialized) {
            return;
        }
         if (proteinAnnotation != null && proteinAnnotation.getAnnotations() != null){
            proteinAnnotation.getAnnotations().remove( forbiddenGO );
            try {
                proteinAnnotation.getAnnotations().removeAll( GOACache.getInstance().fetchByUniprotAc( proteinAnnotation.getId().getId()) );
            } catch (NullPointerException e ){
                //TODO: solve this case
                System.out.println(proteinAnnotation.convertToString());
                e.printStackTrace();
            }
        }
    }

    public void filterGO( Collection<ProteinAnnotation> proteinAnnotations ) throws FilterException {
        for ( Iterator<ProteinAnnotation> iter = proteinAnnotations.iterator(); iter.hasNext(); ) {
            ProteinAnnotation proteinAnnotation = iter.next();
            filterGO(proteinAnnotation);
        }
    }
}
