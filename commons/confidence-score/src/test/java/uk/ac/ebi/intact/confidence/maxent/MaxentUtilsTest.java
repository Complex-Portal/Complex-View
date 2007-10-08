/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.confidence.maxent;

import opennlp.maxent.GISModel;
import org.junit.Test;
import org.junit.Ignore;

import java.io.*;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class MaxentUtilsTest {

    @Test
    public void createModel() throws Exception {

        File file = new File(MaxentUtilsTest.class.getResource("gameLocation.dat").getFile());

        GISModel model = MaxentUtils.createModel(new FileInputStream(file));

        System.out.println(model.getNumOutcomes());

    }

    @Test
    public void writeModel() throws Exception {

        File file = new File(MaxentUtilsTest.class.getResource("gameLocation.dat").getFile());
        File outputFile = File.createTempFile("gameLocation", "out");

        GISModel model = MaxentUtils.createModel(new FileInputStream(file));

        MaxentUtils.writeModelToFile(model, outputFile);

        System.out.println("\n\n==== OUTPUT =====\n\n");

        printFile(outputFile);


    }

    private static void printFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;
        while ((line = reader.readLine()) != null)
        {
            System.out.println(line);
        }
    }

}