package uk.ac.ebi.intact.service;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.dbupdate.cv.CvUpdateManager;

/**
 * Hello world!
 *
 */
public class CvUpdateRunner
{
    public static void main( String[] args )
    {
        IntactContext.initContext(new String[]{"/META-INF/jpa-cv-update.spring.xml", "/META-INF/cvupdaterunner.spring.xml",
                "/META-INF/cvupdate.spring.xml"});

        CvUpdateManager cm = (CvUpdateManager)
                IntactContext.getCurrentInstance().getSpringContext().getBean("cvManager");

        System.out.println( "folder where are the log files = " + cm.getReportDirectory().getAbsolutePath() );

        System.out.println("Starting the global update for PSI-MI and PSI-MOD");
        cm.updateAll();

        System.out.println("Finished the global cv update.");
    }
}
