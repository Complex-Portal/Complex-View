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
package uk.ac.ebi.intact.confidence;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Ignore;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import uk.ac.ebi.intact.confidence.model.io.BinaryInteractionAttributesReader;
import uk.ac.ebi.intact.confidence.model.io.impl.BinaryInteractionAttributesReaderImpl;
import uk.ac.ebi.intact.confidence.model.BinaryInteractionAttributes;

/**
 * Class used only for creating statistics, and not with a test purpose.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
//@Ignore
public class StatisticsTest {

    @Test
  @Ignore
    public void scoreDistribution() throws Exception {
        File medConfFile = new File("H:\\tmp\\medconf_set_scores.txt");
        Statistics.scoreDistribution( medConfFile );
    }


    @Test
    @Ignore
    public void attributesCoverage() throws Exception {
        File hcFile = new File("H:\\tmp\\lowconf_set_attributes.txt");
        Statistics.attributesCoverage( hcFile );            
    }

}
