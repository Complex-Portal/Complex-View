package uk.ac.ebi.intact.service;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dbupdate.dataset.DatasetException;
import uk.ac.ebi.intact.dbupdate.dataset.DatasetWriter;
import uk.ac.ebi.intact.dbupdate.dataset.selectors.SynapseDatasetSelector;

/**
 * Script to run a synapse dataset update
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/01/12</pre>
 */

public class SynapseDatasetUpdater {

    public static void main( String[] args ){

        // two possible arguments
        if( args.length != 2 ) {
            System.err.println( "Usage: GlobalUpdate <datasetReports> <selectionReport>" );
            System.exit( 1 );
        }
        final String datasetReport = args[0];
        final String selectionReport = args[1];

        System.out.println( "Log file for dataset update = " + datasetReport );
        System.out.println( "Log file for protein selection update = " + selectionReport );

        IntactContext.initContext(new String[]{"/META-INF/jpa-dataset-update.spring.xml", "/META-INF/datasetupdaterunner.spring.xml"});

        DatasetWriter datasetScript = new DatasetWriter(datasetReport);
        SynapseDatasetSelector selector = null;
        try {
            selector = new SynapseDatasetSelector(selectionReport);

            datasetScript.setSelector(selector);

            datasetScript.addDatasetAnnotationToExperimentsAndPublications();
        } catch (DatasetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
