package uk.ac.ebi.intact.service.imex;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.model.Publication;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * ImexExporter Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since TODO artifact version
 */
public class ImexExporterTest extends IntactBasicTestCase {

    @Test
    @Ignore
    public void exportImexFile() throws Exception {

    }

    private Publication buildPublication( String pmid, int experimentCount, int interactionCount ) {
        IntactMockBuilder mockBuilder = new IntactMockBuilder();
        Publication pub = mockBuilder.createPublication( pmid );
        mockBuilder.createPrimaryReferenceXref( pub, pmid );
        for ( int i = 0; i < experimentCount; i++ ) {
            Experiment e1 = mockBuilder.createExperimentRandom( interactionCount );
            mockBuilder.createPrimaryReferenceXref( e1, pmid );
            Assert.assertEquals( interactionCount, e1.getInteractions().size() );
            e1.setPublication( pub );
            pub.addExperiment( e1 );
        }
        return pub;
    }

    private File getTargetDirectory() {
        String outputDirPath = ImexExporterTest.class.getResource( "/" ).getFile();
        Assert.assertNotNull( outputDirPath );
        File outputDir = new File( outputDirPath );
        // we are in test-classes, move one up
        outputDir = outputDir.getParentFile();
        Assert.assertNotNull( outputDir );
        Assert.assertTrue( outputDir.isDirectory() );
        Assert.assertEquals( "target", outputDir.getName() );
        return outputDir;
    }

    @Test
    public void buildEntry() throws Exception {

        File target = getTargetDirectory();
        System.out.println( target.getAbsolutePath() );

        final String pmid = "12345678";
        Publication pub1 = buildPublication( pmid, 2, 3 );
        Publication pub2 = buildPublication( pmid, 1, 5 );
        Publication pub3 = buildPublication( pmid, 3, 2 );

        ImexExporter imexExporter = new ImexExporter();

        Collection<IntactEntry> entries = new ArrayList<IntactEntry>();
        entries.add( imexExporter.buildEntry( pub1 ) );
        entries.add( imexExporter.buildEntry( pub2 ) );
        entries.add( imexExporter.buildEntry( pub3 ) );

        File outputFile = new File( "C:\\imex.xml" );
        if ( outputFile.exists() ) {
            outputFile.delete();
        }

        imexExporter.exportImexFile( entries, target, true, true );

        for ( int i = 0; i < target.listFiles().length; i++ ) {
            File f = target.listFiles()[i];
            System.out.println( f.getName() );
        }
    }
}
