package uk.ac.ebi.intact.service;

import uk.ac.ebi.intact.core.context.IntactContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Hello world!
 *
 */
public class CvUpdateRunner
{
    public static void main( String[] args )
    {
        final String filename = args[0];

        System.out.println( "folder where are the log files = " + filename );

        IntactContext.initContext(new String[]{"/META-INF/jpa-cv-update.spring.xml", "/META-INF/cvupdaterunner.spring.xml"});

        try {
            InputStream ontologyConfig = CvUpdateRunner.class.getResource("/ontologies.xml").openStream();

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
