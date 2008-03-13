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
package uk.ac.ebi.intact.confidence.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.confidence.intact.TrainModel;
import uk.ac.ebi.intact.confidence.model.GoIdentifierImpl;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.persistence.dao.ProteinDao;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 */
public class EhCachePlay {

    public static void main(String[] args) throws Exception {
         List<String> uniprotAcs = getAcs();

         File goaFile = new File("E:\\iarmean\\data\\gene_association.goa_uniprot");
        Cache cache = fillCache (uniprotAcs, goaFile);

        String key = "P12345";
        Element el = cache.get(key);
        System.out.println(el.getKey());

        List<GoIdentifierImpl> list = (ArrayList) el.getObjectValue ();
        for ( Iterator<GoIdentifierImpl> iter = list.iterator(); iter.hasNext(); ) {
            GoIdentifierImpl goIdentifier =  iter.next();
            System.out.println(goIdentifier.convertToString());

        }
        System.out.print( list.getClass().toString() );

    }

    private static Cache fillCache( List<String> uniprotAcs, File goaFile ) throws IOException {
         //create cache
        InputStream ehcacheConfig = EhCachePlay.class.getResourceAsStream("/META-INF/ehcache-play.xml");
        CacheManager cacheManager = CacheManager.create(ehcacheConfig);
        Cache cache = cacheManager.getCache( "GOA" );

        BufferedReader br = new BufferedReader(new FileReader(goaFile));
        String line ="";
        String uniprot = null;
        List<GoIdentifierImpl> gos = new ArrayList<GoIdentifierImpl>();
        while ((line = br.readLine())!= null){
            String [] aux =line.split("\t");
            String uniprotAc = aux[1];
            String goTerm = aux[4];
            String evidenceCode = aux[6];
            if (evidenceCode.equalsIgnoreCase( "IEA" )){
               if (uniprot == null){
                   uniprot = uniprotAc;
                   gos.add( new GoIdentifierImpl( goTerm) );
               } else {
                   if (uniprot.equalsIgnoreCase( uniprotAc )){
                       gos.add( new GoIdentifierImpl( goTerm) );
                   } else {
                       cache.put( new Element(uniprot, gos) );
                       uniprot = uniprotAc;
                       gos.clear();
                       gos.add( new GoIdentifierImpl( goTerm) );
                   }
               }
            }
        }
        return cache;
    }

    private static List<String> getAcs() throws IntactTransactionException {
        File pgConfigFile = new File( TrainModel.class.getResource( "/hibernate.iweb2.cfg.xml" ).getFile() );
        IntactContext.initStandaloneContext( pgConfigFile );

        ProteinDao proteinDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao();
        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        List<String> list = proteinDao.getAllUniprotAcs();
        int totalNr = list.size();
        System.out.println( "totalNr: " + totalNr );
        IntactContext.getCurrentInstance().getDataContext().commitTransaction();

        return list;
    }


}
