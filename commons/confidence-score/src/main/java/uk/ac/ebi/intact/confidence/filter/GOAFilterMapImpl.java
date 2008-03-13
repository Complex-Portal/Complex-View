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
import uk.ac.ebi.intact.confidence.model.GoIdentifierImpl;
import uk.ac.ebi.intact.confidence.model.Identifier;
import uk.ac.ebi.intact.confidence.model.ProteinAnnotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Filter for GO annotation using a Map.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 19.02.2008
 */
public class GOAFilterMapImpl implements GOAFilter {
    private static boolean initialized = false;
    private static Multimap<String, Identifier> goas;

    public void initialize( File goaFile ) throws FilterException {
        if (!initialized){
            try {
                initMap(goaFile);
                initialized = true;
            } catch ( IOException e ) {
                throw new FilterException( e);
            }
        }
    }

    public void clean(){
        goas.clear();
        initialized = false;
    }

  public void filterGO (Identifier id,  Set<Identifier> gos) throws FilterException {
      if (gos != null){
          gos.remove( forbiddenGO );
          Collection<Identifier> forbidden = goas.get(id.getId()  );
          gos.removeAll( forbidden);
      }
  }

    public void filterGO( ProteinAnnotation proteinAnnotation ) throws FilterException {
        Collection<Identifier> gos = goas.get( proteinAnnotation.getId().getId() );
        proteinAnnotation.getAnnotations().remove(forbiddenGO );
        proteinAnnotation.getAnnotations().removeAll( gos );
    }

    public void filterGO( Collection<ProteinAnnotation> proteinAnnotations ) throws FilterException {
        for ( Iterator<ProteinAnnotation> iter = proteinAnnotations.iterator(); iter.hasNext(); ) {
            ProteinAnnotation proteinAnnotation = iter.next();
            filterGO(proteinAnnotation);
        }
    }


    /////////////////////
    // Private Method(s).
    private void initMap( File goaFile ) throws IOException {
        goas = new HashMultimap();
        BufferedReader br = new BufferedReader(new FileReader(goaFile));
        String line = "";
        while ((line = br.readLine())!= null){
            String [] aux = line.split("\t");
            String uniprotAc = aux[1];
            String goTerm = aux[4];
            String evidenceCode = aux[6];
            if (forbiddenCode.equalsIgnoreCase( evidenceCode )){
                goas.put( uniprotAc, new GoIdentifierImpl(goTerm) );
            }
        }
        br.close();
    }
}
