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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.filter.GOAFilterTest;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Protein;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Test class for trimming the GOA file.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0-SNAPSHOT
 */
public class GOAFileTrimmerTest extends IntactBasicTestCase {

    @Test
    public void trimming() throws Exception {
        Protein prot = getMockBuilder().createProtein( "A0JPZ8", "AP2L3_ARATH" );
        PersisterHelper.saveOrUpdate(prot);
        File goaFile = new File( GOAFilterTest.class.getResource( "goaTest.txt" ).getFile());
        File outFile = new File( GlobalTestData.getTargetDirectory(), "goaTrimmed.txt");
        if (outFile.exists()){
            outFile.delete();
        }
        GOAFileTrimmer.trimOneByOne(goaFile, outFile  );
        Assert.assertTrue( outFile.exists() );
        BufferedReader br = new BufferedReader(new FileReader(outFile));
        String str = "";
        int nr = 0;
        while ((str = br.readLine() )!= null){
               nr ++;
        }
        br.close();
        Assert.assertEquals(5, nr); 
    }

    @Test
    @Ignore
    public void trimAll() throws Exception {
        IntactContext.initStandaloneContext( new File( GOAFileTrimmerTest.class.getResource( "/hibernate.iweb2.cfg.xml" ).getFile() ) );
//        File goaFile = new File( "E:\\iarmean\\data\\gene_association.goa_uniprot");
//        File outFile = new File( "E:\\iarmean\\data\\", "goaTrimmed.txt");
        File goaFile = new File("/net/nfs7/vol22/sp-pro5/20080401_iarmean/gene_association.goa_uniprot");
        File outFile = new File("/net/nfs7/vol22/sp-pro5/20080401_iarmean/goaTrimmed.goa_intact");
        if (outFile.exists()){
            outFile.delete();
        }
        GOAFileTrimmer.trim(goaFile, outFile  );
        Assert.assertTrue( outFile.exists() );
    }
}
