package uk.ac.ebi.intact.service;

import org.apache.commons.lang.exception.ExceptionUtils;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralClient;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.bridges.imexcentral.Operation;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.ImexCentralManager;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.listener.ReportWriterListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will remove IntAct admin group from some publications.
 *
 * It will add the intact_curators admin group and reset the institution group
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/08/13</pre>
 */

public class ImexIntactAdminGroupUpdater {
    public static void main( String[] args )
    {
        String localTrustStore = System.getProperty( "javax.net.ssl.trustStore" );
        String localTrustStorePwd = System.getProperty( "javax.net.ssl.keyStorePassword" );
        if(localTrustStore==null) {
            System.out.println( "It appears you haven't setup a local trust store (other than the one embedded in the JDK)." +
                    "\nShould you want to specify one, use: -Djavax.net.ssl.trustStore=<path.to.keystore> " +
                    "\nAnd if it is password protected, use: -Djavax.net.ssl.keyStorePassword=<password>" );
        } else {
            System.out.println( "Using local trust store: " + localTrustStore + (localTrustStorePwd == null ? " (no password set)" : " (with password set)" ) );
        }

        // two possible arguments
        if( args.length != 1 ) {
            System.err.println( "Usage: selectionAssigner <inputFile>" );
            System.exit( 1 );
        }
        final String fileInputName = args[0];

        System.out.println("File containing publication acs for which we want to reset ADMIN group = " + fileInputName);

        IntactContext.initContext(new String[]{"/META-INF/jpa-imex-assigner.spring.xml", "/META-INF/imex-assigner.spring.xml"});

        try {
            System.out.println("Reading file containing publication acs to reset...");
            List<String> publicationAcs = new ArrayList<String>();

            File inputFile = new File(fileInputName);
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));

            try{
                String line = reader.readLine();

                while (line != null){
                    publicationAcs.add(line);
                    line = reader.readLine();
                }
            }
            finally {
                reader.close();
            }

            ImexCentralManager ia = (ImexCentralManager)
                    IntactContext.getCurrentInstance().getSpringContext().getBean("imexCentralManager");
            ia.registerListenersIfNotDoneYet();

            ImexCentralClient client = ia.getImexAdminGroupSynchronizer().getImexCentralClient();

            System.out.println("folder where are the log files = " + ia.getImexUpdateConfig().getUpdateLogsDirectory().getAbsolutePath());

            System.out.println("Starting the IMEx ADMIN reset a selection of publication in " + fileInputName);

            for (String ac : publicationAcs){
                try {
                    System.out.println("Reset ADMIN group from " + ac);

                    client.updatePublicationAdminGroup(ac, Operation.ADD, "INTACT CURATORS");
                    client.updatePublicationAdminGroup(ac, Operation.DROP, "INTACT");

                    ia.updateIntactPublicationHavingIMEx(IntactContext.getCurrentInstance().getDaoFactory().getPublicationDao().getByShortLabel(ac).getAc());

                } catch (PublicationImexUpdaterException e) {
                    e.printStackTrace();
                } catch (ImexCentralException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            ReportWriterListener[] writers = ia.getListenerList().getListeners(ReportWriterListener.class);

            if (writers != null){
                for (ReportWriterListener writer : writers){
                    try {
                        writer.close();
                    } catch (IOException e) {
                        System.out.println("Impossible to close report listener writers, " + ExceptionUtils.getFullStackTrace(e));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Impossible to find the input file or log file repository.");
            e.printStackTrace();
        }

        System.out.println("Finished the selection IMEx assigner.");
    }
}
