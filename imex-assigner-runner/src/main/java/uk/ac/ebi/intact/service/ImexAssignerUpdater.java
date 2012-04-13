package uk.ac.ebi.intact.service;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.GlobalImexPublicationUpdater;

/**
 * Script to run the global IMEx assigner/updater in IntAct
 *
 */
public class ImexAssignerUpdater
{
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

        IntactContext.initContext(new String[]{"/META-INF/jpa-imex-assigner.spring.xml", "/META-INF/imex-assigner.spring.xml"});

        GlobalImexPublicationUpdater ia = (GlobalImexPublicationUpdater)
                IntactContext.getCurrentInstance().getSpringContext().getBean("globalImexPublicationUpdater");

        System.out.println( "folder where are the log files = " + ia.getImexCentralManager().getImexUpdateConfig().getUpdateLogsDirectory().getAbsolutePath() );

        System.out.println("Starting the global IMEx updater in IntAct");
        ia.updateExistingImexPublications();
        System.out.println("Starting the global IMEx assigner in IntAct");
        ia.assignNewImexIdsToPublications();

        System.out.println("Finished the global IMEx assigner and updater.");
    }
}
