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

import opennlp.maxent.*;
import opennlp.maxent.io.GISModelWriter;
import opennlp.maxent.io.SuffixSensitiveGISModelWriter;

import java.io.*;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class MaxentUtils {

    // some parameters if you want to play around with the smoothing option
    // for model training.  This can improve model accuracy, though training
    // will potentially take longer and use more memory.  Model size will also
    // be larger.  Initial testing indicates improvements for models built on
    // small data sets and few outcomes, but performance degradation for those
    // with large data sets and lots of outcomes.
    private static final boolean USE_SMOOTHING = false;
    private static final double SMOOTHING_OBSERVATION = 0.1;

    public static GISModel createModel(InputStream inputData) {

        Reader datafr = new InputStreamReader(inputData);
        EventStream es =
          new BasicEventStream(new PlainTextByLineDataStream(datafr));
        GIS.SMOOTHING_OBSERVATION = SMOOTHING_OBSERVATION;
        GISModel model = GIS.trainModel(es,USE_SMOOTHING);

        return model;
    }

    public static void writeModelToFile(GISModel model, File outputFile) throws IOException
    {
        GISModelWriter writer =
          new SuffixSensitiveGISModelWriter(model, outputFile);
        writer.persist();
    }

}