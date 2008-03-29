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
package uk.ac.ebi.intact.confidence.intact;

import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.bridges.blast.BlastConfig;

import java.io.File;

/**
 * Test class for the whole run.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class TrainModelTest {

    @Test
	 @Ignore
    public void runTrainModel() throws Exception {
        File workDir = new File("");
        File pgConfigFile = new File("");
        File yeastFasta = new File("");
        File goaIntact = new File("");

        BlastConfig blastConfig = new BlastConfig("x@ebi.ac.uk");

        TrainModel trainModel = new TrainModel(workDir, pgConfigFile, yeastFasta, goaIntact, blastConfig);
        trainModel.generateModel();
    }

}
