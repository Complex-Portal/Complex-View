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
package uk.ac.ebi.intact.confidence;

import java.io.File;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        30-Nov-2007
 *        </pre>
 */
public interface ConfidenceModel {
    public void retrieveHighConfidenceSetWithAnnotations(File workDir);

    public void retrieveHighConfidenceSetAndMediumConfidenceSetWithAnnotations(File workDir);

    /**
     *
     * @param workDir : must contain a highconf_set.txt and a medconf_set.txt file
     * @param fastaFile : for the set of proteins the low confidences will be generated form.
     * @param nr : optional, if n >0 n low confidence interactions will be generated, if n<=0 will generate as many low confidences as high confidences found
     */
    public void retrieveLowConfidenceSet(File workDir, File fastaFile, int nr);

    public void retrieveLowConfidenceSetAnnotations(File workDir, File lowconfFile);

    /**
     * Filters the GO annotations. Runs the blast and filters the results.
     * @param workDir: where the anotations are.
     */
    public void processAnnotations(File workDir);

    public void buildAttributes();

    /**
     * 
     * @return : a file with the persisted model
     */
    public File trainModel();  


}
