/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.confidence.global.GlobalTestData;

import java.io.File;

/**
 * Test class for creating folds for the cross validation. 
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 *        <pre>
 *        13-Nov-2007
 *        </pre>
 */
public class XvFoldTest {
    
    @Test
    public void testFoldTestSet() throws Exception {
        File hcFile = new File(XvFoldTest.class.getResource( "highconf_all_attribs.txt").getPath());
        File lcFile = new File(XvFoldTest.class.getResource( "lowconf_all_attribs.txt").getPath());
        File workDir = new File( GlobalTestData.getInstance().getTargetDirectory(), "XvFoldTest");
        if (!workDir.isDirectory()) {
            workDir.mkdir();
        }
        XvFold.k =5;
        XvFold.fold( hcFile, lcFile, workDir);
        Assert.assertTrue((new File(workDir, "highconf0.txt").exists()));
        Assert.assertTrue((new File(workDir, "highconf1.txt").exists()));
        Assert.assertTrue((new File(workDir, "highconf2.txt").exists()));
        Assert.assertTrue((new File(workDir, "highconf3.txt").exists()));
        Assert.assertTrue((new File(workDir, "highconf4.txt").exists()));

    }

    @Test
    public void testFold() throws Exception {
        File hcFile = new File(XvFoldTest.class.getResource( "highconf_all_attribs.txt").getPath());
        File lcFile = new File(XvFoldTest.class.getResource( "lowconf_all_attribs.txt").getPath());
        File workDir = new File( GlobalTestData.getInstance().getTargetDirectory(), "XvFoldTest");
        if (!workDir.isDirectory()) {
            workDir.mkdir();
        }
        XvFold.fold( hcFile, lcFile, workDir);
    }


}
