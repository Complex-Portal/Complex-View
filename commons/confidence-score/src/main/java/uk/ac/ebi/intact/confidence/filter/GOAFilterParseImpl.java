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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.confidence.model.GoIdentifierImpl;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.ProteinAnnotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Filter for the GO annotation by parsing the annotation file.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 19.02.2008
 */
public class GOAFilterParseImpl implements GOAFilter {
         /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog( GOAFilterParseImpl.class);

    private static File goaFile;
    private static Multimap<Identifier, Identifier> processed;

     private static boolean initialized = false;

    public void initialize( File goaFile ) throws FilterException {
        if (!initialized){
            this.goaFile = goaFile;
            processed = new HashMultimap();
            initialized = true;
        }  else {
             if (log.isDebugEnabled()){
                 log.debug("Filter already initialized!");
             }
         }
    }

    public void clean() {
        processed.clear();
        initialized = true;
    }

    public void filterGO( Identifier id, Set<Identifier> gos ) throws FilterException {
       if (gos != null ){
           gos.remove( forbiddenGO );
           try {
               if ( processed.containsKey( id ) ) {
                   Collection<Identifier> forbidden = processed.get( id );
                   gos.removeAll( forbidden );
               } else{
                    Set<Identifier> forbidden = getForbidden( id );
                    gos.removeAll( forbidden );
               }
           } catch ( IOException e ) {
               throw new FilterException( e );
           }
       }
    }

    public void filterGO( ProteinAnnotation proteinAnnotation ) throws FilterException {
        if (proteinAnnotation != null && proteinAnnotation.getAnnotations() != null){
            proteinAnnotation.getAnnotations().remove( forbiddenGO );
            try {
                if (processed.containsKey( proteinAnnotation.getId() )){
                    Collection<Identifier> forbidden = processed.get( proteinAnnotation.getId() );
                    proteinAnnotation.getAnnotations(). remove(forbidden);
                } else {
                   Set<Identifier> forbidden = getForbidden(proteinAnnotation.getId());
                   proteinAnnotation.getAnnotations().removeAll( forbidden );
                }
            } catch (NullPointerException e ){
                System.out.println(proteinAnnotation.convertToString());
                e.printStackTrace();
            } catch ( IOException e ) {
                throw new FilterException( e);
            }
        }
    }

    public void filterGO( Collection<ProteinAnnotation> proteinAnnotations ) throws FilterException {
        for ( Iterator<ProteinAnnotation> iter = proteinAnnotations.iterator(); iter.hasNext(); ) {
            ProteinAnnotation proteinAnnotation = iter.next();
            filterGO(proteinAnnotation);
        }
    }


    /////////////////////
    // Private Method(s).
     private static Set<Identifier> getForbidden(Identifier id) throws IOException {
         Set<Identifier> gos = new HashSet<Identifier>();
         BufferedReader br = new BufferedReader(new FileReader(goaFile));
         String line = "";
         while ((line = br.readLine() )!= null){
            String [] aux = line.split("\t");
            String uniprotAc = aux[1];
            String goTerm = aux[4];
            String evidenceCode = aux[6];
            if (id.getId().equalsIgnoreCase( uniprotAc )){
               if (evidenceCode.equalsIgnoreCase( forbiddenCode )){
                   Identifier go = new GoIdentifierImpl( goTerm);
                   processed.put( id, go);
                   gos.add( go );
               }
            }
         }
         br.close();

        return gos;
    }
}
