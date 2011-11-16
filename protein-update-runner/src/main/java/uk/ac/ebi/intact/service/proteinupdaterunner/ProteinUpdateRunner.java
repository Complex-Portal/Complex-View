/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
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
 * limitations under the License.
 */
package uk.ac.ebi.intact.service.proteinupdaterunner;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dbupdate.prot.ProteinUpdateContext;
import uk.ac.ebi.intact.dbupdate.prot.ProteinUpdateProcessor;
import uk.ac.ebi.intact.dbupdate.prot.ProteinUpdateProcessorConfig;
import uk.ac.ebi.intact.dbupdate.prot.report.FileReportHandler;

import java.io.File;
import java.io.IOException;

/**
 * Main class for protein update runner
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ProteinUpdateRunner {

    public static void main(String[] args) {

        final String filename = args[0];

        boolean isBlastEnabled = false;

        if (args.length == 2){
            isBlastEnabled = Boolean.parseBoolean(args[1]);
        }

        System.out.println( "folder where are the log files = " + filename );
        System.out.println( "Blast enabled = " + isBlastEnabled );

        IntactContext.initContext(new String[]{"/META-INF/jpa-protein-update.spring.xml", "/META-INF/proteinupdaterunner.spring.xml"});

        ProteinUpdateProcessorConfig config = ProteinUpdateContext.getInstance().getConfig();
        config.setDeleteProteinTranscriptWithoutInteractions(true);
        config.setDeleteProtsWithoutInteractions(true);
        config.setGlobalProteinUpdate(true);
        config.setFixDuplicates(true);
        config.setProcessProteinNotFoundInUniprot(true);
        config.setBlastEnabled(isBlastEnabled);
        try {
            config.setReportHandler(new FileReportHandler(new File(filename)));

            ProteinUpdateProcessor updateProcessor = new ProteinUpdateProcessor();
            System.out.println("Starting the global update");
            updateProcessor.updateAll();

            System.out.println("Finished the global protein update.");
            config.getUniprotService().close();

        } catch (IOException e) {
            System.err.println("The repository " + filename + " cannot be found. We cannot write log files and so we cannot run a global protein update.");
            e.printStackTrace();
        }
    }

}
