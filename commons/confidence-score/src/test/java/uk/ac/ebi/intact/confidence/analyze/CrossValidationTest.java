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
 * Class used to prepare the data for the cross-validation.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class CrossValidationTest {

    @Test
    @Ignore
    public void xxx() throws Exception {
        File hcFile = new File("H:\\test\\xvalid\\highconf_set_attribs.txt");
        File lcFile = new File("H:\\test\\xvalid\\lowconf_set_attribs.txt");
        File workDir = new File("H:\\test\\xvalid");
        CrossValidation xvalid = new CrossValidation(hcFile.getPath(), lcFile.getPath(), 10, workDir);
//        xvalid.foldSets( hcFile, lcFile, 10, workDir );
    }

    @Test
    @Ignore
    public void fold10() throws Exception {
        File hcFile = new File("H:\\test\\xvalid\\highconf_set_attribs.txt");
        File lcFile = new File("H:\\test\\xvalid\\lowconf_set_attribs.txt");
        File workDir = new File("H:\\test\\xvalid");
        XvFold.k = 10;
        XvFold.fold( hcFile, lcFile, workDir);
    }
}
