package uk.ac.ebi.intact.service.proteinupdaterunner;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dbupdate.prot.ProteinUpdateContext;
import uk.ac.ebi.intact.dbupdate.prot.ProteinUpdateProcessor;
import uk.ac.ebi.intact.dbupdate.prot.ProteinUpdateProcessorConfig;
import uk.ac.ebi.intact.dbupdate.prot.report.FileReportHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will update a specific set of proteins
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/08/11</pre>
 */

public class ProteinSelectionUpdateRunner {

    public static void main(String[] args) {

        // three possible arguments
        if( args.length != 3 ) {
            System.err.println( "Usage: GlobalUpdate <folder> <blast> <inputFile>" );
            System.exit( 1 );
        }
        final String filename = args[0];
        final String fileInputName = args[2];

        boolean isBlastEnabled = Boolean.parseBoolean(args[1]);

        System.out.println( "folder where are the log files = " + filename );
        System.out.println("Blast enabled = " + isBlastEnabled);
        System.out.println("File containing protein acs to update = " + fileInputName);

        IntactContext.initContext(new String[]{"/META-INF/jpa-protein-update.spring.xml", "/META-INF/proteinupdaterunner.spring.xml"});

        ProteinUpdateProcessorConfig config = ProteinUpdateContext.getInstance().getConfig();
        config.setDeleteProteinTranscriptWithoutInteractions(true);
        config.setDeleteProtsWithoutInteractions(true);
        config.setGlobalProteinUpdate(true);
        config.setFixDuplicates(true);
        config.setProcessProteinNotFoundInUniprot(true);
        config.setBlastEnabled(isBlastEnabled);
        try {
            System.out.println("Reading file containing protein acs to update...");
            List<String> proteinAcs = new ArrayList<String>();

            File inputFile = new File(fileInputName);
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));

            String line = reader.readLine();

            while (line != null){
                proteinAcs.add(line);
                line = reader.readLine();
            }

            reader.close();

            config.setReportHandler(new FileReportHandler(new File(filename)));

            ProteinUpdateProcessor updateProcessor = new ProteinUpdateProcessor();
            System.out.println("Starting the protein update for a selection of "+proteinAcs.size()+" proteins");
            updateProcessor.updateByACs(proteinAcs);

            System.out.println("Finished the update of " + proteinAcs.size() + " proteins");
            config.getUniprotService().close();

        } catch (IOException e) {
            System.err.println("The repository " + filename + " cannot be found. We cannot write log files and so we cannot run a global protein update.");
            e.printStackTrace();
        }
    }
}
