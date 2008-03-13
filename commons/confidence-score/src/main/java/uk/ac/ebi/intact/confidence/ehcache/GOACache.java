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
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.confidence.model.GoIdentifierImpl;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Cache class containing the GOA for the uniprot proteins in IntAct
 * with the evidence code IEA;
 * IEA = inferred by electronic evidence | a evidence code for GOA
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class GOACache {
     /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog( GOACache.class);

    private static Cache cache;
    private static String codeToFilter = "IEA";

    private final static GOACache instance = new GOACache();

    private GOACache(){}

    /////////////////////
    // Public Method(s).
    public static GOACache getInstance(){
        return instance;
    }

    public void loadGOA( File goaFile, List<String> uniprotAcs) throws IOException, CacheException {
        BufferedReader br = new BufferedReader(new FileReader(goaFile));
        String line ="";
        String uniprot = null;
        List<GoIdentifierImpl> gos = new ArrayList<GoIdentifierImpl>();
        while ((line = br.readLine())!= null){
            String [] aux = line.split("\t");
            String uniprotAc = aux[1];
            String goTerm = aux[4];
            String evidenceCode = aux[6];
           if (uniprotAcs.size() == 0 || uniprotAcs.contains( uniprotAc )){
            if (evidenceCode.equalsIgnoreCase( codeToFilter )){
               if (uniprot == null){
                   uniprot = uniprotAc;
                   gos.add( new GoIdentifierImpl( goTerm) );
               } else {
                   if (uniprot.equalsIgnoreCase( uniprotAc )){
                       gos.add( new GoIdentifierImpl( goTerm) );
                   } else {
                       getCache().put( new Element(uniprot, gos) );
//                       printCache();
                       uniprot = uniprotAc;
                       gos = new ArrayList<GoIdentifierImpl>();
                       gos.add( new GoIdentifierImpl( goTerm) );
                   }
               }
            }
           }
        }
        getCache().put( new Element(uniprot, gos) );
    }

    public List<GoIdentifierImpl> fetchByUniprotAc(String uniprotAc) throws CacheException {
         if (getInstance() == null){
             log.info( "Instance not initialized!" );
             return null;
         }
        Element element = getCache().get( uniprotAc );
        if (element == null){
             log.info( "no element foud with the specified key " + uniprotAc );
             return new ArrayList<GoIdentifierImpl>(0);
         } else {
             return (ArrayList) element.getObjectValue();
         }         
     }

    public static void put(String uniprotAc, List<GoIdentifierImpl> gos) throws CacheException {
        getCache().put( new Element(uniprotAc, gos) );
    }

    public void clean() throws IOException, CacheException {
        getCache().removeAll();
    }


    ////////////////////////
    // Private Methods(s).

    private static Cache getCache() throws CacheException {
        if (cache == null){
            InputStream ehcacheConfig = EhCachePlay.class.getResourceAsStream("/META-INF/ehcache-play.xml");
            CacheManager cacheManager = CacheManager.create(ehcacheConfig);
            cache = cacheManager.getCache( "GOA" );
        }
        return cache;
    }

    public static void printCache() throws CacheException {
        List<String>  keys = getCache().getKeys();
        for ( Iterator<String> iter = keys.iterator(); iter.hasNext(); ) {
            String key = iter.next();
            System.out.print(key + ",");
            List<GoIdentifierImpl> gos = (ArrayList) getCache().get( key ).getObjectValue();
            for (Iterator<GoIdentifierImpl> iterGo = gos.iterator(); iterGo.hasNext();){
                GoIdentifierImpl goId = iterGo.next();
                System.out.print(goId.convertToString()+", ");
            }
            System.out.println();
        }
    }

}
