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
package uk.ac.ebi.intact.confidence.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.ProteinImpl;
import uk.ac.ebi.intact.persistence.dao.ProteinDao;

import java.io.*;
import java.util.List;

/**
 * Trimms the GOA uniprot file to contain only the proteins in IntAct.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0-SNAPSHOT
 */
public class GOAFileTrimmer {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( GOAFileTrimmer.class );

    public static void trimOneByOne( File goaFile, File outFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(goaFile));

        Writer writer = new FileWriter(outFile);
        ProteinDao proteinDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao();
        String line = "";
        String auxAc ="";
        boolean ok = false;
        while ((line = br.readLine()) != null){
            String [] aux = line.split("\t");
            String uniprotAc = aux[1];
            if (auxAc.equalsIgnoreCase( uniprotAc ) && ok){
                writer.write( line +"\n" );
            } else {
                if (!auxAc.equalsIgnoreCase( uniprotAc )){
                   List<ProteinImpl> proteins = proteinDao.getByUniprotId( uniprotAc );
                    if ( proteins.size() != 0 ) {
                        writer.write( line + "\n" );
                        auxAc = uniprotAc;
                        ok = true;
                    }
                }
            }
           
        }
        br.close();
        writer.close();        
    }


    public static void trim(File goaFile, File outFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(goaFile));

        Writer writer = new FileWriter(outFile);
        List<String> proteins = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao().getAllUniprotAcs();
        log.info("found: " + proteins.size() + " proteins");
        String line = "";
        int nr = 0;
        while ((line = br.readLine()) != null){
            String [] aux = line.split("\t");
            String uniprotAc = aux[1];
            if (proteins.contains( uniprotAc )){
                writer.write( line + "\n" );
            }
            nr ++;
            if ((nr % 1000) == 0){
                log.info("processed " + nr + " lines");
            }

        }
        br.close();
        writer.close();
    }
}
