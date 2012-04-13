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
