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
package uk.ac.ebi.intact.confidence.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

/**
 * Wrapper for pointer to Files, used either as input, or input/output files.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class Report {
       /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( Report.class );

    private File highconfFile;
    private File highconfIpFile;
    private File highconfGOFile;
    private File highconfSeqFile;

    private File medconfFile;
    private File medconfIpFile;
    private File medconfGOFile;
    private File medconfSeqFile;

    private File lowconfFile;
    private File lowconfIpFile;
    private File lowconfGOFile;
    private File lowconfSeqFile;


    public Report(File hcFile, File mcFile){
        if (hcFile.exists()  || mcFile.exists()){
            if (log.isInfoEnabled()){
               log.warn("HighConfidence and MediumConfidence files " + hcFile.getPath() + ", " + mcFile.getPath() + " already exist!");
           }
        } 
        highconfFile = hcFile;
        medconfFile = mcFile;
    }

    public File getHighconfFile() {
        return highconfFile;
    }

    public File getMedconfFile() {
        return medconfFile;
    }

    public File getLowconfFile() {
        if (lowconfFile == null || !lowconfFile.exists()){
            lowconfFile = new File(highconfFile.getParentFile(), "lowconf_set.txt");
        }
        return lowconfFile;
    }

    public void setLowconfFile( File lowconfFile ) {
        this.lowconfFile = lowconfFile;
    }

    public File getHighconfIpFile() {      
        return highconfIpFile;
    }

    public void setHighconfIpFile( File highconfIpFile ) {
        this.highconfIpFile = highconfIpFile;
    }

    public File getHighconfGOFile() {
        return highconfGOFile;
    }

    public void setHighconfGOFile( File highconfGOFile ) {
        this.highconfGOFile = highconfGOFile;
    }

    public File getHighconfSeqFile() {
        return highconfSeqFile;
    }

    public void setHighconfSeqFile( File highconfSeqFile ) {
        this.highconfSeqFile = highconfSeqFile;
    }

    public File getMedconfIpFile() {
        return medconfIpFile;
    }

    public void setMedconfIpFile( File medconfIpFile ) {
        this.medconfIpFile = medconfIpFile;
    }

    public File getMedconfGOFile() {
        return medconfGOFile;
    }

    public void setMedconfGOFile( File medconfGOFile ) {
        this.medconfGOFile = medconfGOFile;
    }

    public File getMedconfSeqFile() {
        return medconfSeqFile;
    }

    public void setMedconfSeqFile( File medconfSeqFile ) {
        this.medconfSeqFile = medconfSeqFile;
    }

    public File getLowconfIpFile() {
        return lowconfIpFile;
    }

    public void setLowconfIpFile( File lowconfIpFile ) {
        this.lowconfIpFile = lowconfIpFile;
    }

    public File getLowconfGOFile() {
        return lowconfGOFile;
    }

    public void setLowconfGOFile( File lowconfGOFile ) {
        this.lowconfGOFile = lowconfGOFile;
    }

    public File getLowconfSeqFile() {
        return lowconfSeqFile;
    }

    public void setLowconfSeqFile( File lowconfSeqFile ) {
        this.lowconfSeqFile = lowconfSeqFile;
    }
}
