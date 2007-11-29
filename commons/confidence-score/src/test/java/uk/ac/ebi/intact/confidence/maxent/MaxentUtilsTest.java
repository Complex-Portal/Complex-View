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

import opennlp.maxent.Context;
import opennlp.maxent.GISModel;
import opennlp.maxent.MutableContext;
import org.junit.Test;

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

     //  file = new File(MaxentUtilsTest.class.getResource("confScore.txt").getFile());

        GISModel model = MaxentUtils.createModel(new FileInputStream(file));

        MaxentUtils.writeModelToFile(model, outputFile);

        System.out.println("\n\n==== OUTPUT =====\n\n");

        printFile(outputFile);

       String [] attrib = {"Sunny", "Happy", "Dry"};
       double []  eval = model.eval( attrib);
        System.out.println("for 'Sunny Happy Dry' =" + eval[0]);
        String [] attribs = new String [0];
        eval = model.eval(attribs);
       System.out.println(" for '' =" + eval[0]);
    }

    @Test
    public void writeParameters() throws Exception{
        File file = new File(MaxentUtilsTest.class.getResource("gameLocation.dat").getFile());
        File outputFile = File.createTempFile("gameLocation", "out");

       // file = new File(MaxentUtilsTest.class.getResource("confScore.txt").getFile());

        GISModel model = MaxentUtils.createModel(new FileInputStream(file));

        Object [] structures = model.getDataStructures();
        for (int i = 0; i<structures.length; i++){
            System.out.println("data structure nr : " + i);
            if (structures[i] instanceof MutableContext) {
                Context con = (MutableContext) (structures[i]);
                double [] p = con.getParameters();
            }
            System.out.println(structures[i]);
            System.out.println("nr outcomes: " + model.getNumOutcomes());
        }
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