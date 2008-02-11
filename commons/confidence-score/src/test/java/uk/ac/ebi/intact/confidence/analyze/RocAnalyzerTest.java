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
package uk.ac.ebi.intact.confidence.analyze;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Test class mainly for executing the RocAnalyer.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class RocAnalyzerTest {

    @Test
    @Ignore
    public void runAnalyzer() throws Exception {
        File gisModel = new File("H:\\tmp\\gisModel.txt");
        String posPath ="H:\\tmp\\highconf_set_attributes.txt";
        String negPath = "H:\\tmp\\lowconf_set_attributes.txt";
        RocAnalyzer ra = new RocAnalyzer( gisModel, posPath, negPath, false);
        ra.printRocPoints( 30 );
    }

    @Test
    @Ignore
    public void analyze() throws Exception {
        RocAnalyze roc = new RocAnalyze();
    }


    @Test
    @Ignore
    public void OneFoldRoc() throws Exception {
//        File gisModel = new File("E:\\tmp\\xvalid\\folds\\gisModel0.txt");
//        String posPath ="E:\\tmp\\xvalid\\folds\\highconf0.txt";
//        String negPath = "E:\\tmp\\xvalid\\folds\\lowconf0.txt";
        for (int i = 0; i< 10; i++){
            File gisModel = new File("/net/nfs6/vol1/homes/iarmean/test/roc/gisModel"+i+".txt");
            String posPath = "/net/nfs6/vol1/homes/iarmean/test/roc/highconf"+i+".txt";
            String negPath = "/net/nfs6/vol1/homes/iarmean/test/roc/lowconf"+i+".txt";

            RocAnalyzer ra = new RocAnalyzer( gisModel, posPath, negPath, false);
            ra.printRocPoints( 30 );
        }
    }


}
